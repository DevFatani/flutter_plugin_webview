import Flutter
import Foundation

class WebviewState {

    private init(){}

    static func onStateChange(_ channel: FlutterMethodChannel,_ data: NSDictionary,_ isIdleAfter: Bool = true) {
        channel.invokeMethod("onStateChange", arguments: data)
        if (isIdleAfter) {
            onStateIdle(channel)
        }
    }

    static func onStateIdle(_ channel: FlutterMethodChannel) {
        channel.invokeMethod("onStateChange", arguments: ["event": "idle"])
    }
}
