package games.signal.collection

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.Build
import android.view.View
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebViewClient

class MainActivity : Activity() {
    private lateinit var game: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val paper = Color.rgb(248, 246, 240)
        window.statusBarColor = paper
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        if (Build.VERSION.SDK_INT >= 26) {
            window.navigationBarColor = paper
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        game = WebView(this)
        game.setBackgroundColor(paper)
        game.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            blockNetworkLoads = true
        }
        val origin = "https://signal.invalid/"
        game.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                if (request.url.toString() == origin) {
                    return WebResourceResponse("text/html", "UTF-8", assets.open("index.html"))
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        setContentView(game)
        game.loadUrl(origin)
    }

    @Deprecated("Compatibility with older Android versions")
    override fun onBackPressed() {
        game.evaluateJavascript("typeof signalBack === 'function' && signalBack()") { handled ->
            if (handled != "true") finish()
        }
    }
}
