# checkout-module

An [Expo native module](https://docs.expo.dev/modules/overview/) that wraps the **[Checkout.com Flow for Mobile](https://www.checkout.com/docs/payments/accept-payments/accept-a-payment-on-your-mobile-app)** native SDKs (iOS + Android) and exposes a small typed API to React Native — together with the Expo **config plugins** that make it build and run inside an Expo app.

> ⚠️ **This is an example, not a library.**
> This code is extracted from a private production app that I can't reveal. I'm publishing it as a **reference implementation** to accompany my article — not as an installable, general-purpose package. Read it, copy the parts you need, but don't expect `npm install` to give you a drop-in solution.

---

## Why this is _not_ a reusable library

I deliberately did **not** ship this as a reusable npm package, because it isn't one. Almost every piece here encodes a decision specific to **my** app:

- The native dependency conflicts it fixes (Fingerprint Pro, ML Kit) come from the **other** SDKs in my app — notably a KYC vendor (SumSub). Your app has a different dependency graph, so your conflicts (if any) will differ.
- The pinned versions (`CheckoutComponentsSDK 1.5.0`, `Risk ~> 4.0.1`, `Fingerprint Pro 2.9.0`) are simply the ones that lined up for my build at the time.
- The native presentation (bottom-sheet detents, Apple Pay merchant id, theming derived from my design system) is wired to my app's conventions.

Turning all of that into a configurable, general-purpose package would be a large separate project — and would hide exactly the details that make this worth reading. So instead of a black box, this repo is **the glue, exposed**.

## What this repo is actually for

The reason to publish it is to show, with real working code, **two things that are genuinely hard and badly documented in React Native / Expo**:

1. **Consuming an iOS SDK that ships only via Swift Package Manager (SPM).** React Native's iOS build is CocoaPods-based, and Expo's autolinking doesn't reliably wire SPM dependencies into the main app target. The fix repackages the SDK as a CocoaPod via a local podspec plus two config plugins.
2. **Resolving native dependency conflicts that only surface at build or run time.** Two SDKs disagreeing about a transitive native dependency produce a manifest-merge failure (build time) or a `NoSuchMethodError` (run time) — neither caused by your own code. The fix patches them declaratively with Expo config plugins so they survive every `prebuild`.

If you came from the article, **these patterns are the takeaway** — the Checkout-specific bits are just the concrete example they live in.

---

## What's inside

```
checkout-module/
  src/                                 # JS/TS bridge + types + theming helper
    CheckoutModule.ts                  # requireNativeModule() typed surface
    CheckoutModule.types.ts
    Appearance.ts
    getCheckoutAppearance.ts
  ios/                                 # Swift module (CheckoutModule.swift) + podspec
  android/                             # Kotlin module (CheckoutModule.kt) + build.gradle
  plugin/
    src/
      index.ts                         # entry plugin (auto-applied) — BUILT to plugin/build
      withFingerprintProResolution.ts  # Android: pin Fingerprint Pro to one version
      withMlKitDependenciesMerge.ts     # Android: merge ML Kit manifest meta-data
      withAddXcodeSourceFile.ts         # iOS: copy podspec into ios/ (referenced manually)
      withCustomPods.ts                 # iOS: inject pods into Podfile (referenced manually)
  app.plugin.js                        # module.exports = require('./plugin/build')
  expo-module.config.json              # autolinking entry
```

---

## The two kinds of config plugins

This is the part worth understanding: the four config plugins are used in **two different ways**.

### 1. Auto-applied plugins (compiled into `plugin/build`)

`withFingerprintProResolution` and `withMlKitDependenciesMerge` are composed into the module's entry plugin, `plugin/src/index.ts`:

```ts
const withCheckoutModulePlugin: ConfigPlugin = config => {
    config = withFingerprintProResolution(config);
    config = withMlKitDependenciesMerge(config);
    return config;
};
export default createRunOncePlugin(withCheckoutModulePlugin, info.name, info.version);
```

They take **no per-app parameters**, so you just register the module's plugin once and forget about them:

```js
// app.config.js
plugins: [
  './modules/checkout-module/app.plugin.js',
],
```

`app.plugin.js` is plain JavaScript (`module.exports = require('./plugin/build')`), and **Expo loads a package's `app.plugin.js` as plain Node — it does not run it through a TypeScript transpiler.** Everything it `require`s must therefore already be **compiled JavaScript**. That's why these two live behind a build step:

```bash
# compiles plugin/src -> plugin/build (see plugin/tsconfig.json)
yarn build:plugin:checkout-module      # or: npx expo-module-scripts build
```

### 2. Manually-referenced plugins (no build step needed)

`withAddXcodeSourceFile` and `withCustomPods` are the iOS linking plugins. Unlike the two above, they **need per-app parameters** (which podspec to copy, which `pod` lines to inject), so they're referenced **directly in your app config, with arguments**:

```ts
// app.config.ts
plugins: [
  // the module's auto-applied plugin (Android dependency fixes)
  './modules/checkout-module/app.plugin.js',

  // iOS step 1 — copy the podspec into the generated ios/ folder during prebuild,
  // so the Podfile can reference it by relative path
  [
    './modules/checkout-module/plugin/src/withAddXcodeSourceFile',
    { files: ['CheckoutComponentsSDK.podspec'] },
  ],

  // iOS step 2 — inject the pod lines into the Podfile (anchored before use_native_modules),
  // forcing the MAIN APP TARGET to link + embed the Checkout xcframework and its deps
  [
    './modules/checkout-module/plugin/src/withCustomPods',
    {
      pods: [
        "pod 'CheckoutComponentsSDK', :podspec => './CheckoutComponentsSDK.podspec'",
        "pod 'CheckoutEventLoggerKit', :git => 'https://github.com/checkout/checkout-event-logger-ios-framework.git', :tag => '1.2.4'",
        "pod 'Risk', '~> 4.0.1'",
      ],
    },
  ],
],
```

> You also need the real `CheckoutComponentsSDK.podspec` somewhere in your repo for `withAddXcodeSourceFile` to copy. Adjust the source/destination paths inside that plugin to match where you keep it.

#### Why these two don't need to be built

They're referenced **inline in `app.config.ts` by file path**, and `app.config.ts` is already evaluated under a TypeScript loader (this project registers `ts-node` / `tsx` at the top of the file). When Expo `require`s a plugin you referenced by a relative `.ts` path, that loader transpiles it on the fly — so the `.ts` source runs as-is, **no precompiled `build/` output required.**

In short:

| Plugin | Referenced via | Loaded as | Needs build? |
|---|---|---|---|
| `withFingerprintProResolution` | `app.plugin.js` → `require('./plugin/build')` | plain JS | **Yes** |
| `withMlKitDependenciesMerge` | `app.plugin.js` → `require('./plugin/build')` | plain JS | **Yes** |
| `withAddXcodeSourceFile` | inline path in `app.config.ts` | TS via ts-node/tsx | No |
| `withCustomPods` | inline path in `app.config.ts` | TS via ts-node/tsx | No |

If your `app.config` is plain JavaScript (no TS loader), point these two at compiled `.js` instead — or fold them into the build step like the other two.

### iOS: why SPM was replaced with CocoaPods

The Checkout.com iOS SDK is distributed as a **dynamic framework** (xcframework) via SPM. With the Expo-generated iOS project, the dynamic library was **not reliably embedded/linked** into the main app target, which surfaced as **runtime** errors ("Library not loaded", missing symbols) rather than clean build failures. The two plugins above replace that broken SPM path with a reproducible CocoaPods path: a local podspec points at Checkout's prebuilt xcframework zip, and the main app target is forced to depend on `CheckoutComponentsSDK`, `CheckoutEventLoggerKit`, and `Risk`.

### Android: what the auto plugins fix

- **`withFingerprintProResolution`** — Checkout's `Risk` component and SumSub both pull **Fingerprint Pro** at different versions. Gradle resolves to one; the API differs; you get a `NoSuchMethodError` on `Configuration` at runtime. The plugin forces a single version (`2.9.0`, the one `Risk` was compiled against) and substitutes the renamed Maven coordinate (`com.fingerprintjs.android:fpjs_pro` → `com.fingerprint.android:pro`).
- **`withMlKitDependenciesMerge`** — `expo-dev-launcher` declares ML Kit `barcode_ui`; SumSub declares `face`. The same `com.google.mlkit.vision.DEPENDENCIES` meta-data key collides during manifest merge. The plugin merges both values (`barcode_ui,face`) and adds `tools:replace`.

---

## The JS API

The native module exposes three methods and two events:

```ts
import { CheckoutModule } from '@modules/checkout-module';

// subscribe BEFORE starting the flow so you don't miss results
const successSub = CheckoutModule.addListener('onSuccess', ({ paymentId }) => { /* ... */ });
const failSub = CheckoutModule.addListener('onFail', ({ error }) => { /* ... */ });

await CheckoutModule.setCredentials({ publicKey: 'pk_sbox_xxxx', environment: 'sandbox' });
await CheckoutModule.initializeCheckout(sessionFromBackend);
await CheckoutModule.renderFlow({ enableGooglePay: true });

// later (e.g. on unmount): successSub.remove(); failSub.remove();
```

**Order matters:** `setCredentials` → `initializeCheckout` → `renderFlow`. Skip or reorder a step and the sheet may not open.

### Methods

| Method                        | Description                                                              |
| ----------------------------- | ----------------------------------------------------------------------- |
| `setCredentials(config)`      | Sets public key and environment. Call before initializing.              |
| `initializeCheckout(session)` | Sets the payment session from your backend. Call before rendering.      |
| `renderFlow(params?)`         | Opens the payment flow UI. On Android, pass `{ enableGooglePay }`.      |

### Events

| Event       | Payload                 | Description                      |
| ----------- | ----------------------- | -------------------------------- |
| `onSuccess` | `{ paymentId: string }` | Payment completed successfully.  |
| `onFail`    | `{ error: string }`     | Payment failed or was cancelled. |

### Types

- **`EnvironmentConfig`** — `{ publicKey: string; environment: 'sandbox' | 'production' }`
- **`InitializeCheckoutPayload`** — session object: `id`, `payment_session_token`, `payment_session_secret`, `_links.self.href`. This is the shape returned by Checkout's [create-a-payment-session](https://www.checkout.com/docs/payments/accept-payments/accept-a-payment-on-your-mobile-app/get-started-with-flow-for-mobile) API.
- **`RenderFlowPayload`** — `{ enableGooglePay?: boolean }`

### Backend

Your backend creates the payment session with your **secret key** and returns the session fields to the app. Never expose the secret key to the client — the app only receives the session token/secret and hands them to the SDK.

---

## Appearance (optional)

You can theme the native Flow UI with design tokens. `getCheckoutAppearance()` builds `DesignTokens` from your app theme (`UnistylesRuntime.getTheme()`; requires `react-native-unistyles`) so you keep a single source of truth:

```ts
import { CheckoutModule, getCheckoutAppearance } from '@modules/checkout-module';

await CheckoutModule.setAppearance(getCheckoutAppearance()); // before initializeCheckout
```

> Note: the native modules implement `setAppearance(...)` on both platforms, but the public TypeScript class (`CheckoutModule.ts`) doesn't declare it yet. Add it to that declaration if you want to call it from JS without a type error.

`Appearance` / `DesignTokens` carry `colorTokens` (hex strings), optional `borderRadius` / `borderFormRadius`, and optional per-role `fonts` (button, input, label, footnote, subheading). See `src/Appearance.ts` for the full shape.

---

## Testing

The Checkout native SDK is not in Expo Go. Use a [development build](https://docs.expo.dev/develop/development-builds/introduction/) (`npx expo run:ios` / `npx expo run:android`, or an EAS dev build).

## Native dependencies & caveats

- This wrapper **does not vendor** the Checkout.com / SumSub / Fingerprint SDKs. They're pulled as native dependencies (the iOS podspec points at Checkout's released `xcframework` zip; Android uses Maven coordinates). Each stays under its own license.
- Versions are pinned to what worked for my build. **Expect to re-pin** for your own dependency graph — that's the whole lesson.
- Any keys, merchant ids, and backend URLs from my app have been removed or replaced with placeholders. Swap in your own; never commit real secrets.
- Unofficial and **not affiliated with or endorsed by Checkout.com**. Provided as-is, no warranty.

## License

[MIT](./LICENSE) — covers **this glue code only**, not the third-party SDKs it integrates with.
