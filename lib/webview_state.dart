abstract class WebViewStateEvent {}

abstract class Url {
  String get url => _url;
  String _url;
}

class WebViewStateEventUrlChange extends WebViewStateEvent with Url {
  WebViewStateEventUrlChange(String url) : super() {
    this._url = url;
  }

  @override
  String toString() => 'WebViewStateEventUrlChange, url: $url';
}

class WebViewStateEventLoadStarted extends WebViewStateEvent with Url {
  WebViewStateEventLoadStarted(String url) : super() {
    this._url = url;
  }

  @override
  String toString() => 'WebViewStateEventLoadStarted, url: $url';
}

class WebViewStateEventLoadFinished extends WebViewStateEvent with Url {
  WebViewStateEventLoadFinished(String url) : super() {
    this._url = url;
  }

  @override
  String toString() => 'WebViewStateEventLoadFinished, url: $url';
}

class WebViewStateEventError extends WebViewStateEvent with Url {
  final int statusCode;

  WebViewStateEventError(String url, this.statusCode) : super() {
    this._url = url;
  }

  @override
  String toString() =>
      'WebViewStateEventError, url: $url, statusCode: $statusCode';
}

class WebViewStateEventIdle extends WebViewStateEvent {
  @override
  String toString() => 'WebViewStateEventIdle';
}

class WebViewStateEventClosed extends WebViewStateEvent {
  @override
  String toString() => 'WebViewStateEventClosed';
}

class WebViewStateEventAuth extends WebViewStateEvent {
  @override
  String toString() => 'WebViewStateEventAuth';
}

class WebViewState {
  final WebViewStateEvent event;

  WebViewState(this.event);

  factory WebViewState.fromMap(Map<String, dynamic> map) =>
      WebViewState(_getEvent(map['event'], map));

  static WebViewStateEvent _getEvent(
      String event, Map<String, dynamic> extraData) {
    switch (event) {
      case 'urlChange':
        return WebViewStateEventUrlChange(extraData['url']);
      case 'loadStarted':
        return WebViewStateEventLoadStarted(extraData['url']);
      case 'loadFinished':
        return WebViewStateEventLoadFinished(extraData['url']);
      case 'error':
        return WebViewStateEventError(
          extraData['url'],
          extraData['statusCode'],
        );
      case 'closed':
        return WebViewStateEventClosed();
      case 'auth':
        return WebViewStateEventAuth();
      default:
        return WebViewStateEventIdle();
    }
  }

  @override
  String toString() => 'WebViewState, event: $event';
}
