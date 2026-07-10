package org.apk.parser.app

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.ByteArrayInputStream
import java.io.File
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

/**
 * 首页：展示已安装应用列表，支持按名称/包名搜索，并支持从文件系统选取安装包进行解析
 */
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvInfo: TextView
    private lateinit var adapter: AppListAdapter
    private var allApps: List<AppInfo> = emptyList()

    // SAF 选文件：拿到 content Uri 后拷贝到 cacheDir，再交给 AppActivity 解析
    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@registerForActivityResult
        tvInfo.text = getString(R.string.loading)
        Thread {
            try {
                val temp = copyUriToCache(uri)
                runOnUiThread {
                    startActivity(Intent(this, AppActivity::class.java).apply {
                        putExtra("path", temp.absolutePath)
                    })
                }
            } catch (e: Exception) {
                runOnUiThread {
                    tvInfo.text = ""
                    Toast.makeText(this, "读取文件失败：${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                runOnUiThread { updateInfoText() }
            }
        }.start()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<android.view.View>(R.id.root).applySystemBarsPadding()
        tvInfo = findViewById(R.id.tv_info)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AppListAdapter { packageName ->
            startActivity(Intent(this, AppActivity::class.java).apply {
                putExtra("packageName", packageName)
            })
        }
        recyclerView.adapter = adapter

        allApps = getInstalledApps()
        adapter.submitList(allApps)
        updateInfoText()

        findViewById<EditText>(R.id.et_search).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = filterApps(s?.toString().orEmpty())
        })

        findViewById<FloatingActionButton>(R.id.btn_select).setOnClickListener {
            // 允许所有类型，实际按扩展名区分 apk / apks
            pickFile.launch("*/*")
        }
    }

    private fun filterApps(query: String) {
        val filtered = if (query.isBlank()) {
            allApps
        } else {
            allApps.filter {
                it.appName.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filtered)
        updateInfoText(filtered.size)
    }

    private fun updateInfoText(count: Int = allApps.size) {
        tvInfo.text = getString(R.string.installed_count, count)
    }

    /**
     * 把 content Uri 内容拷贝到应用缓存目录，返回真实 File（解析库需要磁盘文件）
     */
    private fun copyUriToCache(uri: Uri): File {
        val ext = resolveExtension(uri)
        val temp = File(cacheDir, "temp.$ext")
        contentResolver.openInputStream(uri)?.use { input ->
            temp.outputStream().use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("无法打开输入流")
        return temp
    }

    /**
     * 从显示名解析扩展名，失败时默认 apk
     */
    private fun resolveExtension(uri: Uri): String {
        var name: String? = null
        contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = c.getString(idx)
            }
        }
        val target = name ?: uri.path
        val ext = target?.substringAfterLast('.', "")?.lowercase()
        return if (ext.isNullOrEmpty()) "apk" else ext
    }

    /**
     * 获取所有已安装应用，按更新时间倒序
     */
    private fun getInstalledApps(): List<AppInfo> {
        val installedApps = mutableListOf<AppInfo>()
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in packages) {
            try {
                val appName = packageManager.getApplicationLabel(app).toString()
                val icon = packageManager.getApplicationIcon(app)
                val packageInfo = packageManager.getPackageInfo(app.packageName, 0)
                @Suppress("DEPRECATION")
                installedApps.add(
                    AppInfo(
                        appName,
                        app.packageName,
                        icon,
                        packageInfo.versionName ?: "",
                        packageInfo.versionCode,
                        packageInfo.firstInstallTime,
                        packageInfo.lastUpdateTime,
                        app.targetSdkVersion,
                        resolveCertIssuer(app.packageName)
                    )
                )
            } catch (_: Exception) {
                // 个别应用信息读取失败时跳过
            }
        }
        installedApps.sortByDescending { it.updateTime }
        return installedApps
    }

    /**
     * 解析已安装应用的签名证书签发者（Issuer DN），用于列表展示"发行者"信息。
     * API 28+ 走 GET_SIGNING_CERTIFICATES（兼容多签名场景取当前生效签名）；
     * 更低版本回退到已废弃的 GET_SIGNATURES。解析失败返回 null，不影响列表展示。
     */
    private fun resolveCertIssuer(packageName: String): String? {
        return try {
            val signature: Signature? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val signingInfo = packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                ).signingInfo
                if (signingInfo?.hasMultipleSigners() == true) {
                    signingInfo.apkContentsSigners?.firstOrNull()
                } else {
                    signingInfo?.signingCertificateHistory?.firstOrNull()
                }
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                    .signatures?.firstOrNull()
            }
            val cert = CertificateFactory.getInstance("X.509")
                .generateCertificate(ByteArrayInputStream(signature?.toByteArray() ?: return null))
                    as X509Certificate
            cert.issuerX500Principal.name
        } catch (_: Exception) {
            null
        }
    }
}
