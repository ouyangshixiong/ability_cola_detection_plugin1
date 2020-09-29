
import 'dart:async';

import 'package:flutter/services.dart';

class AbilityColaDetectionPlugin1 {
  static const MethodChannel _channel =
      const MethodChannel('abilitygame.cn/cola_detection');

  static Future<String> get version async {
    final bool loadModelFlag = await _channel.invokeMethod("loadModel");
    final bool loadLabelFlag = await _channel.invokeMethod("loadLabel");
    final String version = await _channel.invokeMethod('version');
    return loadModelFlag.toString() + "|" + loadLabelFlag.toString() + "|" + version;
  }

  static Future<void> get detectCola async {
    final String imageFile = await _channel.invokeMethod("detectCola");
  }
}
