import SwiftUI
import CheckoutComponentsSDK

extension Color {
  init(hex: String) {
    let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
    var int: UInt64 = 0
    Scanner(string: hex).scanHexInt64(&int)
    let a, r, g, b: UInt64
    switch hex.count {
    case 3: (a, r, g, b) = (255, (int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
    case 6: (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
    case 8: (a, r, g, b) = (int >> 24, int >> 16 & 0xFF, int >> 8 & 0xFF, int & 0xFF)
    default: (a, r, g, b) = (255, 0, 0, 0)
    }
    self.init(.sRGB, red: Double(r) / 255, green: Double(g) / 255, blue: Double(b) / 255, opacity: Double(a) / 255)
  }
}

enum CheckoutAppearance {
  static func makeDesignTokens(from options: NSDictionary) -> CheckoutComponents.DesignTokens? {
    let colorsDict = (options["colorTokens"] as? NSDictionary) ?? (options["colors"] as? NSDictionary)
    guard let colorsDict = colorsDict else { return nil }

    func hexColor(_ key: String) -> Color? {
      guard let hex = colorsDict[key] as? String, !hex.isEmpty else { return nil }
      return Color(hex: hex)
    }

    let action = hexColor("action") ?? Color(hex: "#186AFF")
    let background = hexColor("background") ?? Color(hex: "#FFFFFF")
    let border = hexColor("border") ?? Color(hex: "#DDDDDD")
    let disabled = hexColor("disabled") ?? Color(hex: "#B0B0B0")
    let error = hexColor("error") ?? Color(hex: "#AD283E")
    let formBackground = hexColor("formBackground") ?? Color(hex: "#FFFFFF")
    let formBorder = hexColor("formBorder") ?? Color(hex: "#949494")
    let inverse = hexColor("inverse") ?? Color(hex: "#FFFFFF")
    let outline = hexColor("outline") ?? Color(hex: "#91B2EE")
    let primary = hexColor("primary") ?? Color(hex: "#000000")
    let secondary = hexColor("secondary") ?? Color(hex: "#727272")
    let success = hexColor("success") ?? Color(hex: "#00856A")

    let colorTokens = CheckoutComponents.ColorTokens(
      action: action,
      background: background,
      border: border,
      disabled: disabled,
      error: error,
      formBackground: formBackground,
      formBorder: formBorder,
      inverse: inverse,
      outline: outline,
      primary: primary,
      secondary: secondary,
      success: success
    )

    let (radiusButton, radiusForm) = (parseBorderRadius(options["borderRadius"], defaultRadius: 4),
                                       parseBorderRadius(options["borderFormRadius"] ?? options["borderRadius"], defaultRadius: 4))

    let borderButtonRadius = CheckoutComponents.BorderRadius(radius: radiusButton, corners: .allCorners)
    let borderFormRadius = CheckoutComponents.BorderRadius(radius: radiusForm, corners: .allCorners)

    var designTokens = CheckoutComponents.DesignTokens(
      colorTokensMain: colorTokens,
      borderButtonRadius: borderButtonRadius,
      borderFormRadius: borderFormRadius
    )

    if let fontsDict = options["fonts"] as? NSDictionary {
      let fonts = parseFonts(fontsDict)
      if let fonts = fonts {
        designTokens = CheckoutComponents.DesignTokens(
          colorTokensMain: colorTokens,
          fonts: fonts,
          borderButtonRadius: borderButtonRadius,
          borderFormRadius: borderFormRadius
        )
      }
    }

    return designTokens
  }

  private static func parseBorderRadius(_ value: Any?, defaultRadius: CGFloat) -> CGFloat {
    guard let dict = value as? NSDictionary else { return defaultRadius }
    if let all = dict["all"] as? NSNumber {
      return CGFloat(all.doubleValue)
    }
    if let radius = dict["radius"] as? NSNumber {
      return CGFloat(radius.doubleValue)
    }
    return defaultRadius
  }

  private static func parseFonts(_ dict: NSDictionary) -> CheckoutComponents.DesignTokens.Fonts? {
    func makeFont(_ fontDict: NSDictionary?) -> CheckoutComponents.Font? {
      guard let d = fontDict else { return nil }
      let family = d["fontFamily"] as? String ?? "System"
      let size = (d["fontSize"] as? NSNumber)?.doubleValue ?? 17
      let lineHeight = (d["lineHeight"] as? NSNumber).map { CGFloat($0.doubleValue) }
      let letterSpacing = (d["letterSpacing"] as? NSNumber).map { CGFloat($0.doubleValue) }
      let font = Font.custom(family, size: size)
      return CheckoutComponents.Font(
        font: font,
        lineHeight: lineHeight ?? 4,
        letterSpacing: letterSpacing ?? 0
      )
    }
    let button = makeFont(dict["button"] as? NSDictionary)
    let footnote = makeFont(dict["footnote"] as? NSDictionary)
    let input = makeFont(dict["input"] as? NSDictionary)
    let label = makeFont(dict["label"] as? NSDictionary)
    let subheading = makeFont(dict["subheading"] as? NSDictionary)
    guard button != nil || footnote != nil || input != nil || label != nil || subheading != nil else { return nil }
    return CheckoutComponents.DesignTokens.Fonts(
      button: button ?? footnote ?? input ?? label ?? subheading!,
      footnote: footnote ?? button ?? input ?? label ?? subheading!,
      input: input ?? button ?? footnote ?? label ?? subheading!,
      label: label ?? button ?? footnote ?? input ?? subheading!,
      subheading: subheading ?? button ?? footnote ?? input ?? label!
    )
  }
}
