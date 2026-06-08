/**
 * Checkout.com Flow for Mobile – appearance types for CheckoutModule.setAppearance(appearance).
 * Mirrors native DesignTokens (iOS CheckoutComponents.DesignTokens / Android DesignTokens).
 * @see https://www.checkout.com/docs/payments/accept-payments/accept-a-payment-on-your-mobile-app/customize-flow-for-mobile
 */

/** Color tokens – hex strings (e.g. "#00CC2D"). Parsed to native color on each platform. */
export type ColorTokens = {
    action?: string;
    background?: string;
    border?: string;
    disabled?: string;
    error?: string;
    formBackground?: string;
    formBorder?: string;
    inverse?: string;
    outline?: string;
    primary?: string;
    secondary?: string;
    success?: string;
    /** Android only */
    scrolledContainer?: string;
};

/** Single radius for all corners, or radius + specific corners. */
export type BorderRadius =
    | { all: number }
    | {
          radius: number;
          corners: readonly ('all' | 'topLeft' | 'topRight' | 'bottomLeft' | 'bottomRight')[];
      };

/** Font descriptor – mapped to native Font on each platform. */
export type FontDescriptor = {
    fontFamily?: string;
    fontSize?: number;
    fontWeight?: 'normal' | 'light' | 'medium' | 'semibold' | 'bold' | 'extrabold';
    fontStyle?: 'normal' | 'italic';
    letterSpacing?: number;
    lineHeight?: number;
};

/** Fonts by role (button, input, label, footnote, subheading). */
export type DesignTokensFonts = {
    button?: FontDescriptor;
    footnote?: FontDescriptor;
    input?: FontDescriptor;
    label?: FontDescriptor;
    subheading?: FontDescriptor;
};

/**
 * Design tokens for Checkout Flow UI.
 * Pass to CheckoutModule.setAppearance(designTokens).
 */
export type DesignTokens = {
    colorTokens: ColorTokens;
    /** Pay button and primary controls. */
    borderRadius?: BorderRadius;
    /** Form fields (card inputs, etc.). */
    borderFormRadius?: BorderRadius;
    fonts?: DesignTokensFonts;
};

/**
 * Appearance = DesignTokens. Use with CheckoutModule.setAppearance(appearance).
 * Example:
 *   const appearance: Appearance = {
 *     colorTokens: {
 *       action: '#00CC2D',
 *       background: '#17201E',
 *       ...
 *     },
 *     borderRadius: { all: 20 },
 *     borderFormRadius: { all: 20 },
 *     fonts: { ... },
 *   };
 *   CheckoutModule.setAppearance(appearance);
 */
export type Appearance = DesignTokens;
