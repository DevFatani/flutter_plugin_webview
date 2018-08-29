package dev.codeplex.flutter_plugin_webview

import android.content.Context
import android.support.v4.widget.SwipeRefreshLayout
import android.util.AttributeSet
import android.view.MotionEvent

class WebViewSwipeRefreshLayout : SwipeRefreshLayout {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)


    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        event?.run {
            return super.onInterceptTouchEvent(event) && (y < (height / 5))
        }
        return super.onInterceptTouchEvent(event)
    }
}