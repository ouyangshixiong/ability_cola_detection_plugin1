
import 'dart:async';

import 'package:flutter/services.dart';

class AbilityColaDetectionPlugin1 {
  static const MethodChannel _channel =
      const MethodChannel('abilitygame.cn/cola_detection');

  static Future<String> get version async {
    final String version = await _channel.invokeMethod('version');
    return version;
  }
}
