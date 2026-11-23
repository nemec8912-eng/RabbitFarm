package com.rabbit.farm

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.addCallback
import org.json.JSONArray
import org.json.JSONObject

/**
 * RabbitFarm 2.0. Offline Android shell for the new dashboard.
 * A separate applicationId lets users keep the original RabbitFarm installed,
 * since older debug builds may use a different signing certificate.
 */
class MainActivity : ComponentActivity() {
    private lateinit var web: WebView
    private var openFile: ValueCallback<Array<Uri>>? = null
    private var pendingBackup: String? = null
    private val oldPrefs by lazy { getSharedPreferences("rabbit_prefs", MODE_PRIVATE) }

    private val chooseFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val value = if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) arrayOf(uri) else null
        } else null
        openFile?.onReceiveValue(value)
        openFile = null
    }

    private val createBackup = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val json = pendingBackup
        if (uri != null && json != null) {
            try {
                contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                    stream.flush()
                }
            } catch (_: Exception) {
                android.widget.Toast.makeText(this, "Не удалось сохранить копию", android.widget.Toast.LENGTH_LONG).show()
            }
        }
        pendingBackup = null
    }

    inner class Bridge {
        @JavascriptInterface
        fun getLegacyData(): String {
            // Import if an update was installed over a compatible signed old version.
            // Never change or remove old preferences.
            val out = JSONObject()
            val keys = listOf("rabbit_list", "young_list", "mating_list",
                "adult_sales", "young_sales", "meat_list", "adult_death_list",
                "young_death_history")
            for (key in keys) {
                val arr = JSONArray()
                for (entry in oldPrefs.getStringSet(key, emptySet()) ?: emptySet()) arr.put(entry)
                out.put(key, arr)
            }
            return out.toString()
        }

        @JavascriptInterface
        fun saveBackup(text: String) {
            if (text.length > 8_000_000) return
            try {
                val json = JSONObject(text)
                if (!json.has("data")) return
                runOnUiThread {
                    pendingBackup = text
                    createBackup.launch("RabbitFarm-backup.json")
                }
            } catch (_: Exception) { }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(11, 21, 17)
        window.navigationBarColor = android.graphics.Color.rgb(11, 21, 17)

        val html = assets.open("rabbitfarm.html").bufferedReader(Charsets.UTF_8).use { it.readText() }
        web = WebView(this)
        web.setBackgroundColor(android.graphics.Color.rgb(11, 21, 17))
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = false
        web.settings.allowContentAccess = true
        web.settings.setSupportMultipleWindows(false)
        web.addJavascriptInterface(Bridge(), "AndroidBridge")
        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return request.url.host != "rabbitfarm.local"
            }
        }
        web.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                openFile?.onReceiveValue(null)
                openFile = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "application/json"
                    addCategory(Intent.CATEGORY_OPENABLE)
                }
                chooseFile.launch(Intent.createChooser(intent, "Импорт данных RabbitFarm"))
                return true
            }
        }
        setContentView(web)
        web.loadDataWithBaseURL("https://rabbitfarm.local/", html, "text/html", "UTF-8", null)

        onBackPressedDispatcher.addCallback(this) {
            if (web.canGoBack()) web.goBack() else finish()
        }
    }

    override fun onDestroy() {
        if (::web.isInitialized) web.destroy()
        super.onDestroy()
    }
}
