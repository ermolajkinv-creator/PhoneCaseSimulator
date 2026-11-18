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
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL

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
        val offlinePhotos = try {
            OfflinePhotos(assets.open("photos.idx")) { assets.openFd("photos.pack").createInputStream() }
        } catch (_: FileNotFoundException) {
            null
        }
        val photoCache = try {
            val hashes = assets.open("photo-hashes.bin").use { it.readBytes() }
            val version = packageManager.getPackageInfo(packageName, 0).versionName
            val source = URL("https://raw.githubusercontent.com/ermolajkinv-creator/PhoneCaseSimulator/v$version/Signal/android/src/main/assets/art/")
            PhotoCache(File(cacheDir, "phone-pictures"), source, hashes)
        } catch (_: FileNotFoundException) {
            null
        }
        val origin = "https://signal.invalid/"
        val photoPath = Regex("""/art/(?:phone\d+\.webp|case\d+\.png|memory-wave\.png)""")
        game.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                if (request.url.toString() == origin) {
                    return WebResourceResponse("text/html", "UTF-8", assets.open("index.html"))
                }
                val uri = request.url
                val path = uri.path.orEmpty()
                if (uri.scheme == "https" && uri.host == "signal.invalid" && photoPath.matches(path)) {
                    val mime = if (path.endsWith(".webp")) "image/webp" else "image/png"
                    val id = path.removePrefix("/art/phone").removeSuffix(".webp").toIntOrNull()
                    val stream = try {
                        if (id != null && offlinePhotos != null) offlinePhotos.open(id)
                        else assets.open(path.removePrefix("/"))
                    } catch (_: FileNotFoundException) {
                        if (id == null || photoCache == null) return null
                        try {
                            photoCache.open(id)
                        } catch (_: IOException) {
                            val message = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 160"><g fill="#544e45" text-anchor="middle" font-family="sans-serif"><text x="100" y="72" font-size="16">Фото недоступно</text><text x="100" y="96" font-size="11">Проверь интернет и открой</text><text x="100" y="112" font-size="11">карточку ещё раз</text></g></svg>"""
                            return WebResourceResponse("image/svg+xml", "UTF-8", 200, "OK",
                                mapOf("Cache-Control" to "no-store"), message.byteInputStream())
                        }
                    }
                    return WebResourceResponse(mime, null, stream)
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
