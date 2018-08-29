[![pub package](https://img.shields.io/pub/v/flutter_plugin_webview.svg)](https://pub.dartlang.org/packages/flutter_plugin_webview) 


# flutter_plugin_webview

Plugin that allow Flutter to communicate with a native WebView.

***Warning:***
The webview is not integrated in the widget tree, it is a native view on top of the flutter view.
you won't be able to use snackbars, dialogs ...

## Getting Started

For help getting started with Flutter, view our online [documentation](http://flutter.io/).

### How it works

#### Launch WebView Fullscreen with Flutter navigation

```dart
new MaterialApp(
        routes: {
            "/": (_) => new WebViewScaffold(
                url: "https://www.google.com",
                appBar: new AppBar(
                title: new Text("Widget webview"),
            ),
        )
    },
);
```

`WebViewPlugin.getInstance()` provide a singleton instance linked to one unique webview,
so you can take control of the webview from anywhere in the app

#### Listen to state change

##### Breaking changes version >= 0.0.12
```dart
WebViewState = {
        WebViewEvent event;
        String url;
    }
```

on event closed state = null

WebViewState.event = { WebViewEventLoadStarted | WebViewEventLoadFinished | WebViewEventError }

```dart
final webviewPlugin = WebViewPlugin.getInstance();  

webviewPlugin.onStateChange.listen((WebViewState state) {
    state.event //get the event
});
```

#### Close launched WebView

```dart
webviewPlugin.close();
```

#### Webview inside custom Rectangle

```dart
final webviewPlugin = WebViewPlugin.getInstance();  

webviewPlugin.luanch(
        url,
        rect: new Rect.fromLTWH(
        0.0,
        0.0,
        MediaQuery.of(context).size.width,
        300.0,
    ));
```

***Don't forget to dispose webview***
`flutterWebviewPlugin.dispose()`

### Webview Functions

```dart
Future<Boolean> launch(String url, {
    Map<String, String> headers,
    bool enableJavaScript,
    bool clearCache,
    bool clearCookies,
    bool visible,
    Rect rect,
    String userAgent,
    bool enableZoom,
    bool enableLocalStorage,
    bool enableScroll,
    }
);
```
```dart
Future<String> evalJavascript(String code);
```
```dart
Future<Boolean> resize(Rect rect);
```
```dart
Future<Boolean> stopLoading();
```
```dart
Future<Boolean> refresh();
```
```dart
Future<Boolean> hasBack();
```
```dart
Future<Boolean> back();
```
```dart
Future<Boolean> hasForward();
```
```dart
Future<Boolean> forward();
```
```dart
Future<Boolean> clearCookies();
```
```dart
Future<Boolean> clearCache();
```
```dart
Future<Boolean> reloadUrl(String url);
```
