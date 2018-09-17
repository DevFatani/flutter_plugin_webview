import 'package:flutter/widgets.dart';

abstract class WebViewEvent {}

abstract class Url {
  String get url => _url;
  String _url;
}

//class WebViewEventUrlChange extends WebViewEvent with Url {
//  WebViewEventUrlChange(String url) : super() {
//    this._url = url;
//  }
//
//  @override
//  String toString() => 'WebViewEventUrlChange';
//}

class WebViewEventLoadStarted extends WebViewEvent {
//  WebViewEventLoadStarted(String url) : super() {
//    this._url = url;
//  }

  @override
  String toString() => 'WebViewEventLoadStarted';
}

class WebViewEventLoadFinished extends WebViewEvent {
//  WebViewEventLoadFinished(String url) : super() {
//    this._url = url;
//  }

  @override
  String toString() => 'WebViewEventLoadFinished';
}

class WebViewEventError extends WebViewEvent {
  final int statusCode;

  WebViewEventError(this.statusCode) : super();

  @override
  String toString() => 'WebViewEventError, statusCode: $statusCode';
}

class WebViewEventIdle extends WebViewEvent {
  @override
  String toString() => 'WebViewEventIdle';
}

class WebViewEventClosed extends WebViewEvent {
  @override
  String toString() => 'WebViewEventClosed';
}

class WebViewEventAuth extends WebViewEvent {
  @override
  String toString() => 'WebViewEventAuth';
}

class WebViewState {
  final WebViewEvent event;
  final String url;

//  final Rect rect;

  WebViewState({
    @required this.event,
    @required this.url,
//    @required this.rect,
  });

  factory WebViewState.reduce(
      WebViewState oldState, Map<String, dynamic> newData) {
    WebViewEvent event = _getEvent(newData['event'] ?? '', newData);
    String url = newData['url'];
//    Rect rect = newData['rect'] == null
//        ? null
//        : _getRect(Map<String, double>.from(newData['rect']));

    return WebViewState(
      event: event,
      url: (url.isNotEmpty ? url : oldState.url),
//      rect: (rect ?? oldState?.rect),
    );
  }

//  static Rect _getRect(Map<String, double> rect) => (rect == null
//      ? null
//      : Rect.fromLTWH(
//          rect['left'], rect['top'], rect['width'], rect['height']));

  static WebViewEvent _getEvent(String event, Map<String, dynamic> data) {
    switch (event) {
//      case 'urlChange':
//        return WebViewEventUrlChange(extraData['url']);
      case 'loadStarted':
        return WebViewEventLoadStarted();
      case 'loadFinished':
        return WebViewEventLoadFinished();
      case 'error':
        return WebViewEventError(data['statusCode']);
//      case 'closed':
//        return WebViewEventClosed();
      default:
        return null;
    }
  }

  @override
  String toString() => 'WebViewState, event: $event, url: $url';
}
