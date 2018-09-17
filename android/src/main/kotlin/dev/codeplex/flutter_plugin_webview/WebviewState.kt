package dev.codeplex.flutter_plugin_webview

import io.flutter.plugin.common.MethodChannel
import java.util.HashMap

class WebviewState {
    companion object {
        fun onStateChange(channel: MethodChannel, data: HashMap<String, Any>, callback: MethodChannel.Result? = null) {
            if (callback != null) {
                channel.invokeMethod("onStateChange", data, callback)
            } else {
                channel.invokeMethod("onStateChange", data)
            }
        }
    }
}