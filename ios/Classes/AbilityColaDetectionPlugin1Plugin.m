#import "AbilityColaDetectionPlugin1Plugin.h"

@implementation AbilityColaDetectionPlugin1Plugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"ability_cola_detection_plugin1"
            binaryMessenger:[registrar messenger]];
  AbilityColaDetectionPlugin1Plugin* instance = [[AbilityColaDetectionPlugin1Plugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else {
    result(FlutterMethodNotImplemented);
  }
}

@end
