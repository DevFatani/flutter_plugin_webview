package dev.shovel.flutter_plugin_webview

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.FrameLayout
import dev.shovel.flutter_plugin_webview.WebviewState.Companion.onStateIdle
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterPluginWebview(
        private val channel: MethodChannel,
        private val activity: Activity
) : MethodCallHandler, PluginRegistry.ActivityResultListener {

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "flutter_plugin_webview")
            with(FlutterPluginWebview(channel, registrar.activity())) {
                registrar.addActivityResultListener(this)
                channel.setMethodCallHandler(this)
            }
        }
    }

    private var webView: WebView? = null

    private var chromeFileHandler: WebChromeFileHandler? = null
    private var webHandler: WebHandler? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean =
            if (webView == null)
                false
            else
                chromeFileHandler?.onActivityResult(requestCode, resultCode, intent) ?: false

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "launch" -> launch(call, result)
            "loadUrl" -> launch(call, result, false)
            "back" -> back(result)
            "hasBack" -> result.success(hasBack())
            "forward" -> forward(result)
            "hasForward" -> result.success(hasForward())
            "refresh" -> refresh(result)
            "close" -> close(result)
            "clearCookies" -> clearCookies(result)
            "clearCache" -> clearCache(result)
            "eval" -> eval(call, result)
            "hide" -> setVisibility(false, result)
            "show" -> setVisibility(true, result)
            "resize" -> resize(call, result)
            "stopLoading" -> stopLoading(result)

            else -> result.notImplemented()
        }
    }

    private fun initWebView(layoutParams: FrameLayout.LayoutParams) {
        if (webView == null) {
            chromeFileHandler = WebChromeFileHandler(activity)
            webHandler = WebHandler(channel)
            webView = WebView(activity)
            webView!!.webChromeClient = chromeFileHandler
            webView!!.webViewClient = webHandler
            webView!!.setOnKeyListener { _, keyCode, event ->
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
            activity.addContentView(
                    webView,
                    layoutParams
            )
        }
    }

    private fun launch(call: MethodCall, result: Result, initIfClosed: Boolean = true) {
        val visible: Boolean = call.argument("visible")
        val url: String = call.argument("url")
        val userAgent: String? = call.argument("userAgent")
        val enableJavascript: Boolean = call.argument("enableJavaScript")
        val clearCache: Boolean = call.argument("clearCache")
        val clearCookies: Boolean = call.argument("clearCookies")
        val enableLocalStorage: Boolean = call.argument("enableLocalStorage")
        val headers: Map<String, String>? = call.argument("headers")
        val enableScroll: Boolean = call.argument("enableScroll")

        if (initIfClosed) {
            initWebView(
                    buildLayoutParams(call)
            )
        }

        if (webView != null) {
            with(webView!!) {
                with(settings) {
                    javaScriptEnabled = enableJavascript
                    domStorageEnabled = enableLocalStorage
                    setAppCacheEnabled(false)
                    cacheMode = WebSettings.LOAD_NO_CACHE

                    if (userAgent?.isNotBlank() == true) {
                        userAgentString = userAgent
                    }
                }

                isVerticalScrollBarEnabled = enableScroll
                setVisibility(visible)

                if (clearCache) {
                    clearCache()
                }

                if (clearCookies) {
                    clearCookies()
                }

                if (headers?.isNotEmpty() == true) {
                    webView!!.loadUrl(url, headers)
                } else {
                    webView!!.loadUrl(url)
                }
            }

            onStateIdle(channel)

            result.success(true)
        } else {
            result.success(false)
        }
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

    private fun refresh(result: Result) {
        webView?.reload()

        result.success(true)
    }

    private fun close(result: Result? = null) {
        (webView?.parent as ViewGroup?)?.removeView(webView)
        chromeFileHandler = null
        webHandler = null
        webView = null

        result?.success(true)
    }

    private fun stopLoading(result: Result){
        webView?.stopLoading()

        result.success(webView != null)
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun eval(call: MethodCall, result: Result) {
        val code = call.argument<String>("code")

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

    private fun setVisibility(visible: Boolean, result: Result? = null) {
        webView?.visibility = if (visible) View.VISIBLE else View.INVISIBLE

        result?.success(true)
    }

    private fun resize(call: MethodCall, result: Result) {
        val params = buildLayoutParams(call)
        webView?.layoutParams = params

        result.success(webView != null)
    }

    private fun buildLayoutParams(call: MethodCall): FrameLayout.LayoutParams {
        val rc = call.argument<Map<String, Number>>("rect")
        Log.d("Rect", "$rc")
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
    }

    private fun dp2px(context: Context, dp: Float): Int =
            (dp * context.resources.displayMetrics.density).toInt()

}
