package dev.codeplex.flutter_plugin_webview

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import io.flutter.plugin.common.PluginRegistry

class WebChromeHandler(private val activity: Activity) : WebChromeClient(), PluginRegistry.ActivityResultListener {

    companion object {
        private const val FILE_CHOOSER_CODE = 1001
    }

    private var uploadMsgCallback: ValueCallback<Uri>? = null
    private var uploadMsgCallbackArray: ValueCallback<Array<Uri>>? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (requestCode == FILE_CHOOSER_CODE) {
            if (requestCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= 21) {
                    uploadMsgCallbackArray?.onReceiveValue(
                            arrayOf(
                                    Uri.parse(intent?.dataString)
                            )
                    )
                    uploadMsgCallbackArray = null
                } else {
                    uploadMsgCallback?.onReceiveValue(intent?.data)
                    uploadMsgCallback = null
                }

                return true
            }
        }

        return false
    }


    //For Android 4.1
    fun openFileChooser(uploadMsg: ValueCallback<Uri>, acceptType: String, capture: String) {
        uploadMsgCallback = uploadMsg
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "*/*"
        activity.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILE_CHOOSER_CODE)
    }

    //For Android 5.0+
    override fun onShowFileChooser(
            webView: WebView,
            filePathCallback: ValueCallback<Array<Uri>>,
            fileChooserParams: WebChromeClient.FileChooserParams
    ): Boolean {
        uploadMsgCallbackArray = filePathCallback

        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                .apply {
                    putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                    putExtra(
                            Intent.EXTRA_INTENT,
                            Intent(Intent.ACTION_GET_CONTENT)
                                    .apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "*/*"
                                    }
                    )
                }

        activity.startActivityForResult(chooserIntent, FILE_CHOOSER_CODE)

        return true
    }
}