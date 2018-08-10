package dev.shovel.flutter_plugin_webview

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

open class WebHandler(private val callback: Callback) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        callback.onPageStarted(view, url, favicon)

//        val data = HashMap<String, Any>()
//        data["url"] = "$url"
//        data["event"] = "loadStarted"
//        onStateChange(channel, data)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        callback.onPageFinished(view, url)

//        val data = HashMap<String, Any>()
//        data["url"] = "$url"
//        data["event"] = "loadFinished"
//        onStateChange(channel, data)
//        onStateIdle(channel)
    }

    @Suppress("OverridingDeprecatedMember")
    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
        @Suppress("DEPRECATION")
        super.onReceivedError(view, errorCode, description, failingUrl)
        callback.onReceivedError(view, errorCode, description, failingUrl)

//        val data = HashMap<String, Any>()
//        data["url"] = "$failingUrl"
//        data["event"] = "error"
//        data["statusCode"] = "$errorCode"
//        onStateChange(channel, data)
//        onStateIdle(channel)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        super.onReceivedHttpError(view, request, errorResponse)
        callback.onReceivedHttpError(view, request, errorResponse)

//        val data = HashMap<String, Any>()
//        data["url"] = "${request?.url}"
//        data["event"] = "error"
//        data["statusCode"] = "${errorResponse?.statusCode ?: -1}"
//        onStateChange(channel, data)
//        onStateIdle(channel)
    }

//    override fun onReceivedHttpAuthRequest(view: WebView?, handler: HttpAuthHandler?, host: String?, realm: String?) {
//        super.onReceivedHttpAuthRequest(view, handler, host, realm)
//        val data = HashMap<String, Any>()
//        data["url"] = "${view?.url}"
//        data["host"] = "$host"
//        data["event"] = "auth"
//        onStateChange(channel, data, callback = object : MethodChannel.Result {
//            override fun notImplemented() {
//            }
//
//            override fun error(errorCode: String?, errorMessage: String?, errorDetails: Any?) {
//            }
//
//            override fun success(result: Any?) {
//                if (result is Map<*, *>) {
//                    val username = "${result["username"]}"
//                    val password = "${result["password"]}"
//                    handler?.proceed(username, password)
//                }
//            }
//        })
//        onStateIdle(channel)
//    }

    interface Callback {
        fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?)

        fun onPageFinished(view: WebView?, url: String?)

        fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?)

        fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?)
    }
}