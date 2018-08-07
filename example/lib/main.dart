import 'package:flutter/material.dart';
import 'package:flutter_plugin_webview/webview_scaffold.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) => MaterialApp(
        debugShowCheckedModeBanner: false,
        home: WebViewScaffold(
          url: "http://www.flutter.io",
          appBar: AppBar(),
        ),
      );
}
