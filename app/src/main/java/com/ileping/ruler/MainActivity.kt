package com.ileping.ruler

import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var _exitTime = 0L
    private val tag = this::class.simpleName
    private var isFromSplash = false
    private var isInBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取屏幕信息
        val realMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.getRealMetrics(realMetrics)
        } else {
            @Suppress("DEPRECATION") windowManager.defaultDisplay.getRealMetrics(realMetrics)
        }

        val density = realMetrics.density

        webView =
                WebView(this).apply {
                    settings.apply {
                        domStorageEnabled = true
                        javaScriptEnabled = true
                        blockNetworkImage = false
                    }

                    // 创建 JavaScript 接口类
                    class WebAppInterface {
                        @android.webkit.JavascriptInterface
                        fun getPixelsPerCm(): Float {

                            return realMetrics.xdpi / 2.54f / density
                        }

                        @android.webkit.JavascriptInterface
                        fun showScreenInfo() {
                            val realWidthPixels = realMetrics.widthPixels
                            val realHeightPixels = realMetrics.heightPixels
                            val xdpi = realMetrics.xdpi
                            val ydpi = realMetrics.ydpi

                            // 计算物理尺寸（厘米）
                            val widthInches = realWidthPixels / xdpi
                            val heightInches = realHeightPixels / ydpi
                            val widthCm = widthInches * 2.54f
                            val heightCm = heightInches * 2.54f

                            // 计算对角线长度（使用勾股定理）
                            val diagonalInches =
                                    Math.sqrt(
                                                    (widthInches * widthInches +
                                                                    heightInches * heightInches)
                                                            .toDouble()
                                            )
                                            .toFloat()
                            val diagonalCm = diagonalInches * 2.54f

                            // 显示信息
                            val screenInfo =
                                    """
                                【分辨率】
                                • ${realWidthPixels} × ${realHeightPixels} 像素
                                
                                【物理尺寸】
                                • ${String.format("%.1f", widthCm)} × ${String.format("%.1f", heightCm)} 厘米
                                • 对角线: ${String.format("%.1f", diagonalInches)}″ (${String.format("%.1f", diagonalCm)} 厘米)
                                
                                【屏幕密度】
                                • 缩放比: ${density}
                                • DPI: ${String.format("%.1f", xdpi)} × ${String.format("%.1f", ydpi)}
                            """.trimIndent()

                            runOnUiThread { showInfoDialog(screenInfo) }
                        }
                    }

                    // 添加 JavaScript 接口
                    addJavascriptInterface(WebAppInterface(), "Android")

                    webViewClient = object : WebViewClient() {}

                    webChromeClient = WebChromeClient()

                    loadUrl("file:///android_asset/main.html")
                }

        findViewById<LinearLayout>(R.id.main_container)
                .addView(
                        webView,
                        LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                        )
                )
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else if (System.currentTimeMillis() - _exitTime > 2000) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show()
            _exitTime = System.currentTimeMillis()
        } else {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        isInBackground = true
        isFromSplash = false
    }

    private fun showInfoDialog(message: String) {
        AlertDialog.Builder(this)
                .setTitle("屏幕信息")
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show()
    }
}
