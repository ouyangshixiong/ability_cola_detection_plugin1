import 'package:flutter/material.dart';
import 'package:flutter/scheduler.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:ability_cola_detection_plugin1/ability_cola_detection_plugin1.dart';

void main() {
  runApp(MyApp());
}

/*
*
* 这里嵌套StatelessWidget + StatefulWidget的原因是：
* 需要在initState方法中直接显示弹窗，但是弹窗类需要
* context，而这个context在MaterialApp类内部的。
* 还有几种方法可以实现开启App就弹窗。
* 参考 https://stackoverflow.com/questions/51766977/flutter-showdialogcontext-in-initstate-method
* */

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key}) : super(key: key);
  @override
  _MyHomePageState createState() => new _MyHomePageState();
}


class _MyHomePageState extends State<MyHomePage> {

  String _text1 = '原图：';

  String _aiModelInfo = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState().then((value) {
      showDialog<void>(
        context: context,
        builder: (_) => AlertDialog(
          content: Text('模型加载结果：$_aiModelInfo'),
          actions: <Widget>[
            FlatButton(
              child: const Text('知道了'),
              onPressed: () {
                Navigator.pop(context);
              },
            )
          ],
        ),
      );
    });

  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String aiModelInfo;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      aiModelInfo = await AbilityColaDetectionPlugin1.version;
    } on PlatformException {
      aiModelInfo = 'Failed to get paddle version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _aiModelInfo = aiModelInfo;
    });
  }

  void startDetect() {
    try {
      print("start detect");
      AbilityColaDetectionPlugin1.detectCola.then((rs){
        print("可乐瓶检测结果:" + (rs?"成功":"失败") );
        setState(() {
          _text1 = "检测结果：";
        });
      });
    } on PlatformException {
      print('method call detectCola failed!');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('可乐瓶检测'),
        ),
        body: Column(
          children: <Widget>[
            Text('$_text1'),
            Image.asset("images/11649.jpg"),
            RaisedButton(
              onPressed: () => startDetect(),
              color: Colors.lightBlueAccent,
              child: Text('可乐瓶检测', style: TextStyle(fontSize: 10)),
            ),

          ],

        ),
      );
  }

}
