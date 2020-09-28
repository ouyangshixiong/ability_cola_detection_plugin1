import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:ability_cola_detection_plugin1/ability_cola_detection_plugin1.dart';

void main() {
  const MethodChannel channel = MethodChannel('ability_cola_detection_plugin1');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await AbilityColaDetectionPlugin1.platformVersion, '42');
  });
}
