import 'package:flutter/material.dart';
import 'dart:async';
import 'flutter_plugin_webview.dart';

export 'flutter_plugin_webview.dart';

class WebViewScaffold extends StatefulWidget {
  final PreferredSizeWidget appBar;
  final String url;
  final bool enableJavaScript;
  final bool clearCache;
  final bool clearCookies;
  final String userAgent;
  final bool enableZoom;
  final bool enableLocalStorage;
  final bool enableScroll;
  final Map<String, String> headers;
  final bool refreshOnResume;

  const WebViewScaffold({
    Key key,
    this.appBar,
    @required this.url,
    this.enableJavaScript = true,
    this.clearCache = false,
    this.clearCookies = false,
    this.userAgent,
    this.enableZoom = true,
    this.enableLocalStorage = true,
    this.enableScroll = true,
    this.headers,
    this.refreshOnResume = true,
  }) : super(key: key);

  @override
  State<StatefulWidget> createState() => WebViewScaffoldState();
}

class WebViewScaffoldState extends State<WebViewScaffold>
    with WidgetsBindingObserver {
  final webviewPlugin = WebViewPlugin.getInstance();
  Rect _rect;
  Timer _resizeTimer;

  @override
  void initState() {
    if (widget.refreshOnResume) {
      WidgetsBinding.instance.addObserver(this);
    }
    webviewPlugin.close();

    super.initState();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (widget.refreshOnResume && state == AppLifecycleState.resumed) {
      webviewPlugin.refresh();
    }

    super.didChangeAppLifecycleState(state);
  }

  @override
  void dispose() {
    if (widget.refreshOnResume) {
      WidgetsBinding.instance.removeObserver(this);
    }
    webviewPlugin.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_rect == null) {
      _rect = _buildRect(context);

      webviewPlugin.launch(
        widget.url,
        headers: widget.headers,
        enableJavaScript: widget.enableJavaScript,
        clearCache: widget.clearCache,
        clearCookies: widget.clearCookies,
        userAgent: widget.userAgent,
        rect: _rect,
        enableZoom: widget.enableZoom,
        enableLocalStorage: widget.enableLocalStorage,
        enableScroll: widget.enableScroll,
      );
    } else {
      final rect = _buildRect(context);

      if (_rect != rect) {
        _rect = rect;
        _resizeTimer?.cancel();
        _resizeTimer = Timer(const Duration(milliseconds: 300), () {
          // avoid resizing to fast when build is called multiple time
          webviewPlugin.resize(_rect);
        });
      }
    }

    return Scaffold(
      appBar: widget.appBar,
      body: const Center(
        child: const CircularProgressIndicator(),
      ),
    );
  }

  Rect _buildRect(BuildContext context) {
    final mediaQuery = MediaQuery.of(context);
    final topPadding = mediaQuery.padding.top;
    final top =
        ((widget.appBar == null) ? 0.0 : widget.appBar.preferredSize.height) +
            topPadding;

    var height = mediaQuery.size.height - top;

    if (height < 0.0) {
      height = 0.0;
    }

    return Rect.fromLTWH(0.0, top, mediaQuery.size.width, height);
  }
}
