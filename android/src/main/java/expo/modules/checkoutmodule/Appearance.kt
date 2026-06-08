package expo.modules.checkoutmodule

import com.checkout.components.interfaces.uicustomisation.BorderRadius
import com.checkout.components.interfaces.uicustomisation.designtoken.ColorTokens
import com.checkout.components.interfaces.uicustomisation.designtoken.DesignTokens
import com.checkout.components.interfaces.uicustomisation.font.Font
import com.checkout.components.interfaces.uicustomisation.font.FontName

/**
 * Checkout Flow DesignTokens from JS.
 * Shape: { colorTokens: { action?, background?, ... }, borderRadius?: { all } | { radius, corners }, borderFormRadius?, fonts?: { button?, input?, ... } }
 * Also accepts legacy "colors" in place of "colorTokens".
 * Converts to SDK [DesignTokens] for [CheckoutComponentConfiguration.appearance].
 */
data class CheckoutAppearanceOptions(
    val colorTokens: Map<String, Long> = emptyMap(),
    val borderRadiusAll: Int? = null,
    val borderRadiusRadius: Int = 4,
    val borderRadiusCorners: List<String> = emptyList(),
    val borderFormRadiusAll: Int? = null,
    val borderFormRadiusRadius: Int = 4,
    val borderFormRadiusCorners: List<String> = emptyList(),
    val fonts: Map<String, FontDescriptor> = emptyMap(),
) {
    data class FontDescriptor(
        val fontFamily: String? = null,
        val fontSize: Double = 17.0,
        val fontWeight: String? = null,
        val fontStyle: String? = null,
        val letterSpacing: Double? = null,
        val lineHeight: Double? = null,
    )

    companion object {
        const val DEFAULT_ACTION = 0xFF186AFFL
        const val DEFAULT_BACKGROUND = 0xFFFFFFFFL
        const val DEFAULT_BORDER = 0xFFDDDDDDL
        const val DEFAULT_DISABLED = 0xFFB0B0B0L
        const val DEFAULT_ERROR = 0xFFAD283EL
        const val DEFAULT_FORM_BACKGROUND = 0xFFFFFFFFL
        const val DEFAULT_FORM_BORDER = 0xFF949494L
        const val DEFAULT_INVERSE = 0xFFFFFFFFL
        const val DEFAULT_OUTLINE = 0xFF91B2EEL
        const val DEFAULT_PRIMARY = 0xFF000000L
        const val DEFAULT_SECONDARY = 0xFF727272L
        const val DEFAULT_SUCCESS = 0xFF00856AL
        const val DEFAULT_SCROLLED_CONTAINER = 0xFFE8E6E6L

        /** Parses hex string "#RRGGBB" or "#AARRGGBB" to 0xAARRGGBB Long. */
        private fun parseHex(hex: String?): Long? {
            if (hex.isNullOrBlank()) return null
            val s = hex.trim().removePrefix("#")
            if (s.length != 6 && s.length != 8) return null
            return s.toLongOrNull(16)?.let { if (s.length == 6) 0xFF000000L or it else it }
        }

        private fun parseRadiusDict(map: Map<*, *>?): Triple<Int?, Int, List<String>> {
            if (map == null) return Triple(null, 4, emptyList())
            val all = (map["all"] as? Number)?.toInt()
            if (all != null) return Triple(all, all, emptyList())
            val radius = (map["radius"] as? Number)?.toInt() ?: 4
            val corners = (map["corners"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            return Triple(null, radius, corners)
        }

        private fun parseFontDescriptor(map: Map<*, *>?): FontDescriptor? {
            if (map == null) return null
            return FontDescriptor(
                fontFamily = map["fontFamily"] as? String,
                fontSize = (map["fontSize"] as? Number)?.toDouble() ?: 17.0,
                fontWeight = map["fontWeight"] as? String,
                fontStyle = map["fontStyle"] as? String,
                letterSpacing = (map["letterSpacing"] as? Number)?.toDouble(),
                lineHeight = (map["lineHeight"] as? Number)?.toDouble(),
            )
        }

        /**
         * Parses JS DesignTokens (Appearance) into [CheckoutAppearanceOptions].
         * Returns null if neither colorTokens nor colors present.
         */
        fun fromMap(options: Map<String, Any?>?): CheckoutAppearanceOptions? {
            val colorTokensDict = (options?.get("colorTokens") as? Map<*, *>) ?: (options?.get("colors") as? Map<*, *>) ?: return null
            val colorStrings = colorTokensDict.mapNotNull { (k, v) ->
                (k as? String)?.let { key -> (v as? String)?.let { key to it } }
            }.toMap()

            val colorTokens = colorStrings.mapValues { (_, hex) ->
                parseHex(hex) ?: DEFAULT_ACTION
            }.toMutableMap()
            if (colorTokens["action"] == null) colorTokens["action"] = DEFAULT_ACTION
            if (colorTokens["background"] == null) colorTokens["background"] = DEFAULT_BACKGROUND
            if (colorTokens["border"] == null) colorTokens["border"] = DEFAULT_BORDER
            if (colorTokens["disabled"] == null) colorTokens["disabled"] = DEFAULT_DISABLED
            if (colorTokens["error"] == null) colorTokens["error"] = DEFAULT_ERROR
            if (colorTokens["formBackground"] == null) colorTokens["formBackground"] = DEFAULT_FORM_BACKGROUND
            if (colorTokens["formBorder"] == null) colorTokens["formBorder"] = DEFAULT_FORM_BORDER
            if (colorTokens["inverse"] == null) colorTokens["inverse"] = DEFAULT_INVERSE
            if (colorTokens["outline"] == null) colorTokens["outline"] = DEFAULT_OUTLINE
            if (colorTokens["primary"] == null) colorTokens["primary"] = DEFAULT_PRIMARY
            if (colorTokens["secondary"] == null) colorTokens["secondary"] = DEFAULT_SECONDARY
            if (colorTokens["success"] == null) colorTokens["success"] = DEFAULT_SUCCESS

            val (borderRadiusAll, borderRadiusRadius, borderRadiusCorners) = parseRadiusDict(options?.get("borderRadius") as? Map<*, *>)
            val (borderFormRadiusAll, borderFormRadiusRadius, borderFormRadiusCorners) = parseRadiusDict(
                options?.get("borderFormRadius") as? Map<*, *> ?: options?.get("borderRadius") as? Map<*, *>
            )

            val fontsDict = options?.get("fonts") as? Map<*, *> ?: emptyMap<String, Any?>()
            val fonts = listOf("button", "footnote", "input", "label", "subheading").mapNotNull { key ->
                parseFontDescriptor(fontsDict[key] as? Map<*, *>)?.let { key to it }
            }.toMap()

            return CheckoutAppearanceOptions(
                colorTokens = colorTokens,
                borderRadiusAll = borderRadiusAll,
                borderRadiusRadius = borderRadiusRadius,
                borderRadiusCorners = borderRadiusCorners,
                borderFormRadiusAll = borderFormRadiusAll,
                borderFormRadiusRadius = borderFormRadiusRadius,
                borderFormRadiusCorners = borderFormRadiusCorners,
                fonts = fonts,
            )
        }
    }

    /**
     * Converts to SDK [DesignTokens] for [CheckoutComponentConfiguration.appearance].
     * SDK types live in com.checkout.components.interfaces.uicustomisation(.designtoken).
     */
    fun toSdkDesignTokens(): DesignTokens {
        val c = colorTokens
        val colorTokensSdk = ColorTokens(
            (c["action"] ?: DEFAULT_ACTION),
            (c["background"] ?: DEFAULT_BACKGROUND),
            (c["border"] ?: DEFAULT_BORDER),
            (c["disabled"] ?: DEFAULT_DISABLED),
            (c["error"] ?: DEFAULT_ERROR),
            (c["formBackground"] ?: DEFAULT_FORM_BACKGROUND),
            (c["formBorder"] ?: DEFAULT_FORM_BORDER),
            (c["inverse"] ?: DEFAULT_INVERSE),
            (c["outline"] ?: DEFAULT_OUTLINE),
            (c["primary"] ?: DEFAULT_PRIMARY),
            (c["secondary"] ?: DEFAULT_SECONDARY),
            (c["success"] ?: DEFAULT_SUCCESS),
            (c["scrolledContainer"] ?: DEFAULT_SCROLLED_CONTAINER),
        )
        val radiusButton = borderRadiusAll ?: borderRadiusRadius
        val radiusForm = borderFormRadiusAll ?: borderFormRadiusRadius
        val borderRadiusSdk = BorderRadius(radiusButton)
        val borderFormRadiusSdk = BorderRadius(radiusForm)
        return DesignTokens(
            colorTokens = colorTokensSdk,
            emptyMap<FontName, Font>(), // custom fonts not mapped from JS yet
            borderFormRadiusSdk,
            borderRadiusSdk,
        )
    }
}
