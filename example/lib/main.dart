import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:ability_cola_detection_plugin1/ability_cola_detection_plugin1.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _aiModelInfo = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String aiModelInfo;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      aiModelInfo = await AbilityColaDetectionPlugin1.version;
    } on PlatformException {
      aiModelInfo = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _aiModelInfo = aiModelInfo;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('目标检测'),
        ),
        body: Column(
          children: <Widget>[
            Text('loadModel | loadLabel | Paddle-Lite版本号: $_aiModelInfo\n'),
            RaisedButton(
              onPressed: () => print("start detect"),
              color: Colors.lightBlueAccent,
              child: Text('开始测试', style: TextStyle(fontSize: 10)),
            ),
            Text('原图：'),
            Image.asset("images/11649.jpg"),
            RaisedButton(
              onPressed: () => print("view detect picture"),
              color: Colors.lightBlueAccent,
              child: Text('查看结果', style: TextStyle(fontSize: 10)),
            )
          ],

        ),
      ),
    );
  }
}
