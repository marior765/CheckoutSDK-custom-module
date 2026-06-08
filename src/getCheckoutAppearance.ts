import { UnistylesRuntime } from 'react-native-unistyles';

import type { BorderRadius, DesignTokens } from './Appearance';

export function getCheckoutAppearance(): DesignTokens {
    const theme = UnistylesRuntime.getTheme()._DSTokens;
    const c = theme.color;
    const radius = theme.borderRadius;

    const borderRadiusAll: BorderRadius = {
        all: typeof radius?.input?.rounded === 'number' ? radius.input.rounded : 16,
    };

    return {
        colorTokens: {
            action: c.button.primary.fill.default,
            background: c.bg.default,
            border: c.border.default,
            disabled: c.fill.disabled,
            error: c.surface.critical.default,
            formBackground: c.fill.neutral.default,
            formBorder: c.border.strong,
            inverse: c.fill.inverted.default,
            outline: c.border.accent ?? c.icon.accent,
            primary: c.text.neutral.strong,
            secondary: c.text.neutral.muted,
            success: c.surface.accent.default,
        },
        borderRadius: borderRadiusAll,
        borderFormRadius: borderRadiusAll,
    };
}
