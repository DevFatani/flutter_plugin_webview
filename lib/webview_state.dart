abstract class WebViewStateEvent {}

abstract class WithUrl {
  String get url => _url;
  String _url;
}

class WebViewStateEventLoadStarted extends WebViewStateEvent with WithUrl {
  WebViewStateEventLoadStarted(String url) : super() {
    this._url = url;
  }
}

class WebViewStateEventLoadFinished extends WebViewStateEvent with WithUrl {
  WebViewStateEventLoadFinished(String url) : super() {
    this._url = url;
  }
}

class WebViewStateEventError extends WebViewStateEvent with WithUrl {
  final String statusCode;

  WebViewStateEventError(String url, this.statusCode) : super() {
    this._url = url;
  }
}

class WebViewStateEventIdle extends WebViewStateEvent {}

class WebViewStateEventClosed extends WebViewStateEvent {}

class WebViewState {
  final WebViewStateEvent event;

  WebViewState(this.event);

  factory WebViewState.fromMap(Map<String, dynamic> map) =>
      WebViewState(_getEvent(map['event'], map));

  static WebViewStateEvent _getEvent(
      String event, Map<String, dynamic> extraData) {
    switch (event) {
      case 'loadStarted':
        return WebViewStateEventLoadStarted(extraData['url']);
      case 'loadFinished':
        return WebViewStateEventLoadFinished(extraData['url']);
      case 'error':
        return WebViewStateEventError(
          extraData['url'],
          extraData['statusCode'],
        );
      default:
        return WebViewStateEventIdle();
    }
  }
}
