Pod::Spec.new do |s|
  s.name             = 'CheckoutComponentsSDK'
  s.version          = '1.5.0'
  s.summary          = 'Checkout.com Components SDK for iOS'
  s.homepage         = 'https://github.com/checkout/checkout-ios-components'
  s.author           = ''
  s.source           = {
    :http => "https://github.com/checkout/checkout-ios-components/releases/download/#{s.version}/CheckoutComponentsSDK.xcframework.zip"
  }

  s.ios.deployment_target = '15.0'
  s.swift_version = '5.9'

  s.ios.vendored_frameworks = 'CheckoutComponentsSDK.xcframework'

  s.dependency 'CheckoutEventLoggerKit', '~> 1.2.4'
  s.dependency 'Risk', '~> 4.0.1'
end
