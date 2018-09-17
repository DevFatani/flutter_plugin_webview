package dev.codeplex.flutter_plugin_webview

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.*
import dev.codeplex.flutter_plugin_webview.WebviewState.Companion.onStateChange
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import java.util.*
import android.view.LayoutInflater
import android.widget.FrameLayout

class FlutterPluginWebview(
        private val channel: MethodChannel,
        private val activity: Activity
) : MethodChannel.MethodCallHandler, PluginRegistry.ActivityResultListener, WebHandler.Callback {

    companion object {
        @JvmStatic
        fun registerWith(registrar: PluginRegistry.Registrar) {
            val channel = MethodChannel(registrar.messenger(), "flutter_plugin_webview")
            with(FlutterPluginWebview(channel, registrar.activity())) {
                registrar.addActivityResultListener(this)
                channel.setMethodCallHandler(this)
            }
        }
    }

    private var root: FrameLayout? = null
    private var webView: WebView? = null
    private var swipeToRefresh: WebViewSwipeRefreshLayout? = null

    private var chromeHandler: WebChromeHandler? = null
    private var webHandler: WebHandler? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean =
            if (webView == null)
                false
            else
                chromeHandler?.onActivityResult(requestCode, resultCode, intent) ?: false

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "launch" -> launch(call, result)
            "reload" -> launch(call, result, false)
            "openUrl" -> openUrl(call, result)
            "back" -> back(result)
            "hasBack" -> result.success(hasBack())
            "forward" -> forward(result)
            "hasForward" -> result.success(hasForward())
            "refresh" -> refresh(result)
            "close" -> close(result)
            "clearCookies" -> clearCookies(result)
            "clearCache" -> clearCache(result)
            "eval" -> eval(call, result)
//            "hide" -> setVisibility(false, result)
//            "show" -> setVisibility(true, result)
            "resize" -> resize(call, result)
            "stopLoading" -> stopLoading(result)

            else -> result.notImplemented()
        }
    }

    private fun initWebView(layoutParams: FrameLayout.LayoutParams, enableSwipeToRefresh: Boolean) {
        if (webView == null) {
            val inflater = LayoutInflater.from(activity.applicationContext)
            root = inflater.inflate(
                    if (enableSwipeToRefresh)
                        R.layout.swipe_refresh_webview
                    else
                        R.layout.webview,
                    null,
                    false
            ) as FrameLayout

            webView = root!!.findViewById<WebView>(R.id.webView)
                    .apply {
                        chromeHandler = WebChromeHandler(activity)
                        webHandler = WebHandler(this@FlutterPluginWebview)
                        webChromeClient = chromeHandler
                        webViewClient = webHandler
                        setOnKeyListener { _, keyCode, event ->
                            if (event.action == KeyEvent.ACTION_DOWN) {
                                when (keyCode) {
                                    KeyEvent.KEYCODE_BACK -> {
                                        if (hasBack()) {
                                            back()
                                        } else {
                                            close()
                                        }
                                        return@setOnKeyListener true
                                    }
                                }
                            }

                            return@setOnKeyListener false
                        }
                    }
            if (enableSwipeToRefresh) {
                swipeToRefresh = root!!.findViewById<WebViewSwipeRefreshLayout>(R.id.swipeRefresh)
                        .apply {
                            isNestedScrollingEnabled = true
                            setOnRefreshListener { refresh() }
                        }
            }

            activity.addContentView(root, layoutParams)
        }
    }

    private fun launch(call: MethodCall, result: Result, initIfClosed: Boolean = true) {
        val url: Uri = Uri.parse(call.argument("url"))
        val userAgent: String? = call.argument("userAgent")
        val enableJavascript: Boolean = call.argument("enableJavaScript")
        val clearCache: Boolean = call.argument("clearCache")
        val clearCookies: Boolean = call.argument("clearCookies")
        val enableLocalStorage: Boolean = call.argument("enableLocalStorage")
        val headers: Map<String, String>? = call.argument("headers")
        val enableScroll: Boolean = call.argument("enableScroll")
        val enableSwipeToRefresh: Boolean = call.argument("enableSwipeToRefresh")

        if (initIfClosed) {
            initWebView(
                    buildLayoutParams(call),
                    enableSwipeToRefresh
            )
        }

        webView?.run {
            with(settings) {
                javaScriptEnabled = enableJavascript
                domStorageEnabled = enableLocalStorage || enableJavascript
                cacheMode = WebSettings.LOAD_NO_CACHE

                if (userAgent?.isNotBlank() == true) {
                    userAgentString = userAgent
                }
            }

            isVerticalScrollBarEnabled = enableScroll

            if (clearCache) {
                clearCache()
            }

            if (clearCookies) {
                clearCookies()
            }

            if (headers?.isNotEmpty() == true) {
                loadUrl("$url", headers)
            } else {
                loadUrl("$url")
            }
        }

        result.success(webView != null)
    }

    private fun openUrl(call: MethodCall, result: Result) {
        val url: Uri = Uri.parse(call.argument("url"))
        val headers: Map<String, String>? = call.argument("headers")

        if (headers?.isNotEmpty() == true) {
            webView?.loadUrl("$url", headers)
        } else {
            webView?.loadUrl("$url")
        }

        result.success(webView != null)
    }

    private fun hasBack(): Boolean = webView?.canGoBack() ?: false

    private fun back(result: Result? = null) {
        val hasBack = hasBack()
        if (hasBack) {
            webView?.goBack()
        }

        result?.success(hasBack)
    }

    private fun hasForward(): Boolean = webView?.canGoForward() ?: false

    private fun forward(result: Result) {
        val hasForward = hasForward()
        if (hasForward) {
            webView?.goForward()
        }

        result.success(hasForward)
    }

    private fun refresh(result: Result? = null) {
        webView?.reload()

        result?.success(true)
    }

    private fun close(result: Result? = null) {
        if (root != null) {
            (root?.parent as ViewGroup?)?.removeView(root)
            chromeHandler = null
            webHandler = null
            swipeToRefresh = null
            webView = null
            root = null

            val data = HashMap<String, Any>()
            data["event"] = "closed"
            onStateChange(channel, data)
        }

        result?.success(true)
    }

    private fun stopLoading(result: Result) {
        webView?.stopLoading()
        swipeToRefresh?.isRefreshing = false

        result.success(webView != null)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun eval(call: MethodCall, result: Result) {
        val code: String = call.argument("code")

        webView?.evaluateJavascript(code) { value -> result.success(value) }
    }

    private fun clearCache(result: Result? = null) {
        webView?.clearCache(true)
        webView?.clearFormData()

        result?.success(webView != null)
    }

    private fun clearCookies(result: Result? = null) {
        with(CookieManager.getInstance()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                removeAllCookies {
                    result?.success(true)
                }
            } else {
                @Suppress("DEPRECATION")
                removeAllCookie()
                result?.success(true)
            }
        }
    }

//    private fun setVisibility(visible: Boolean, result: Result? = null) {
//        webView?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
//
//        result?.success(true)
//    }

    private fun resize(call: MethodCall, result: Result) {
        val params = buildLayoutParams(call)

        root?.layoutParams = params

        result.success(root != null)
    }

    private fun buildLayoutParams(call: MethodCall): FrameLayout.LayoutParams {
        val rc: Map<String, Number>? = call.argument("rect")

        return if (rc != null) {
            FrameLayout.LayoutParams(
                    dp2px(
                            activity,
                            rc["width"]?.toFloat() ?: 0f
                    ),
                    dp2px(
                            activity,
                            rc["height"]?.toFloat() ?: 0f
                    )
            ).apply {
                setMargins(
                        dp2px(
                                activity,
                                rc["left"]?.toFloat() ?: 0f
                        ),
                        dp2px(
                                activity,
                                rc["top"]?.toFloat() ?: 0f
                        ),
                        0,
                        0
                )
            }
        } else {
            val size = Point()
            activity.windowManager.defaultDisplay.getSize(size)
            FrameLayout.LayoutParams(size.x, size.y)
        }
//                .apply {
//                    val data = HashMap<String, Any>()
//                    data["rect"] = HashMap<String, Number>().apply {
//                        this["left"] = px2dp(activity, leftMargin)
//                        this["top"] = px2dp(activity, topMargin)
//                        this["width"] = px2dp(activity, width)
//                        this["height"] = px2dp(activity, height)
//                    }
//                    onStateChange(channel, data)
//                }
    }

    private fun dp2px(context: Context, dp: Float): Int =
            (dp * context.resources.displayMetrics.density).toInt()

    private fun px2dp(context: Context, px: Int): Float =
            (px.toFloat() / context.resources.displayMetrics.density)

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        val data = HashMap<String, Any>()
        data["url"] = "$url"
        data["event"] = "loadStarted"
        onStateChange(channel, data)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        swipeToRefresh?.isRefreshing = false
        val data = HashMap<String, Any>()
        data["url"] = "$url"
        data["event"] = "loadFinished"
        onStateChange(channel, data)
    }

    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
        val data = HashMap<String, Any>()
        data["url"] = "$failingUrl"
        data["event"] = "error"
        data["statusCode"] = errorCode
        onStateChange(channel, data)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        val data = HashMap<String, Any>()
        data["url"] = "${request?.url}"
        data["event"] = "error"
        data["statusCode"] = errorResponse?.statusCode ?: -1
        onStateChange(channel, data)
    }
}
