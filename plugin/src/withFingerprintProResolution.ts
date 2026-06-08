import { ConfigPlugin, withProjectBuildGradle } from 'expo/config-plugins';

const FINGERPRINT_RESOLUTION_MARKER = '// Resolve Fingerprint Pro conflict between Checkout SDK and SumSub (Expo config plugin)';

// Checkout Risk expects Configuration without allowUseOfLocationData/locationTimeoutMillis (added in 2.10).
const FINGERPRINT_VERSION = '2.9.0';

const FINGERPRINT_RESOLUTION_BLOCK = `
${FINGERPRINT_RESOLUTION_MARKER}
allprojects {
    configurations.all {
        resolutionStrategy {
            force 'com.fingerprint.android:pro:${FINGERPRINT_VERSION}'
            dependencySubstitution {
                substitute(module('com.fingerprintjs.android:fpjs_pro')).using(module('com.fingerprint.android:pro:${FINGERPRINT_VERSION}'))
            }
        }
    }
}
`;

/**
 * Forces a single version of Fingerprint Pro across the Android build.
 * Checkout SDK (checkout-android-components / Risk) and SumSub (idensic-mobile-sdk-fisherman)
 * can pull different versions, causing NoSuchMethodError on Configuration at runtime.
 */
export const withFingerprintProResolution: ConfigPlugin = config => {
    return withProjectBuildGradle(config, config => {
        if (config.modResults.language !== 'groovy') {
            return config;
        }

        const contents = config.modResults.contents;
        if (contents.includes(FINGERPRINT_RESOLUTION_MARKER)) {
            return config;
        }

        config.modResults.contents = contents.trimEnd() + '\n' + FINGERPRINT_RESOLUTION_BLOCK.trim() + '\n';
        return config;
    });
};
