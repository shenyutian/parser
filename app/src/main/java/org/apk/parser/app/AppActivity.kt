package org.apk.parser.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import org.apk.parser.apk.AbstractApkFile
import org.apk.parser.apk.ApkFile
import org.apk.parser.apk.ApksFile
import org.apk.parser.json.JSONObject
import java.io.File

/**
 * 详情页：解析指定安装包（已安装应用的 sourceDir 或选取的文件），展示 getInfo() 与图标，
 * 支持将解析结果 JSON 上传到服务端（见 [UploadConfig]），也支持通过系统分享面板分享出去。
 * 仅支持 apk / apks，aab 需在 JVM 环境解析。
 */
class AppActivity : AppCompatActivity() {

    private var filePath: String? = null
    private var parsedInfoJson: String? = null

    private lateinit var progress: ProgressBar
    private lateinit var tvAppInfo: TextView
    private lateinit var tvUploadStatus: TextView
    private lateinit var btnUpload: MaterialButton
    private lateinit var btnShare: MaterialButton
    private lateinit var btnExport: MaterialButton
    private lateinit var btnUploadApk: MaterialButton

    // API 28 及以下导出到公共 Downloads 需先申请写存储权限，授予后继续导出
    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            runExport()
        } else {
            btnExport.isEnabled = true
            tvUploadStatus.text = getString(R.string.export_need_permission)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<View>(R.id.root).applySystemBarsPadding()
        val tvFilePath = findViewById<TextView>(R.id.tv_file_path)
        val appIcon = findViewById<ImageView>(R.id.appIconImageView)
        progress = findViewById(R.id.progress)
        tvAppInfo = findViewById(R.id.tv_app_info)
        tvUploadStatus = findViewById(R.id.tv_upload_status)
        btnUpload = findViewById(R.id.btn_upload)
        btnUpload.setOnClickListener { uploadParsedInfo() }
        btnShare = findViewById(R.id.btn_share)
        btnShare.setOnClickListener { shareParsedInfo() }
        // 导出/上传安装包不依赖解析结果，只需源文件即可，故进入页面即可用
        btnExport = findViewById(R.id.btn_export)
        btnExport.setOnClickListener { startExport() }
        btnUploadApk = findViewById(R.id.btn_upload_apk)
        btnUploadApk.setOnClickListener { runUploadApk() }

        // 来自已安装应用则用包名取 sourceDir，否则用传入的文件路径
        val packageName = intent.getStringExtra("packageName")
        filePath = if (packageName != null) {
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).sourceDir
        } else {
            intent.getStringExtra("path")
        }

        if (TextUtils.isEmpty(filePath)) {
            Toast.makeText(this, "数据异常", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        tvFilePath.text = filePath

        val path = filePath!!
        if (path.lowercase().endsWith(".aab")) {
            progress.visibility = View.GONE
            tvAppInfo.text = "设备端暂不支持 aab 解析，请在 JVM 环境使用 :aab 模块"
            return
        }

        Thread {
            try {
                // apk / apks 均为 AbstractApkFile 且实现 Closeable，用 use 确保关闭
                val open: () -> AbstractApkFile = {
                    if (path.lowercase().endsWith(".apk")) ApkFile(path) else ApksFile(path)
                }
                open().use { apk ->
                    val infoText = apk.getInfo().toString(4)
                    // 图标解码失败（如损坏的图标数据）不应影响已解析成功的 JSON 信息展示
                    val iconBytes = runCatching { firstDecodableIcon(apk) }.getOrNull()
                    runOnUiThread {
                        progress.visibility = View.GONE
                        parsedInfoJson = infoText
                        tvAppInfo.text = infoText
                        btnUpload.isEnabled = true
                        btnShare.isEnabled = true
                        if (iconBytes != null) {
                            appIcon.setImageBitmap(
                                BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.size)
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progress.visibility = View.GONE
                    tvAppInfo.text = "解析失败：${e.message}"
                }
            }
        }.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    /**
     * 遍历图标，返回首个可被 BitmapFactory 解码的位图字节。
     * 单个图标解析异常（如某些自适应图标缺失前景/背景层）只跳过该图标，不影响后续图标或整体解析结果。
     */
    private fun firstDecodableIcon(apk: AbstractApkFile): ByteArray? {
        for (icon in apk.getAllIcons()) {
            val data = runCatching {
                val path = icon.path
                if (path != null && path.lowercase().endsWith(".xml")) return@runCatching null
                val bytes = icon.data ?: return@runCatching null
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                if (opts.outWidth > 0 && opts.outHeight > 0) bytes else null
            }.getOrNull()
            if (data != null) return data
        }
        return null
    }

    /**
     * 把当前解析结果 JSON 通过系统分享面板分享出去（文本形式，含文件路径便于对方识别）。
     */
    private fun shareParsedInfo() {
        val json = parsedInfoJson ?: return
        val shareText = "$filePath\n\n$json"
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)))
    }

    /**
     * 把当前解析结果 JSON 上传到 [UploadConfig.ENDPOINT]。
     */
    private fun uploadParsedInfo() {
        val json = parsedInfoJson ?: return
        if (!UploadConfig.isConfigured()) {
            tvUploadStatus.text = getString(R.string.upload_endpoint_not_configured)
            return
        }
        btnUpload.isEnabled = false
        tvUploadStatus.text = getString(R.string.uploading)
        Thread {
            try {
                val result = uploadJson(UploadConfig.ENDPOINT, json)
                runOnUiThread {
                    tvUploadStatus.text = getString(R.string.upload_success, result.code)
                    btnUpload.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    tvUploadStatus.text = getString(R.string.upload_failed, e.message ?: e.toString())
                    btnUpload.isEnabled = true
                }
            }
        }.start()
    }

    /**
     * 收集待导出的安装包源文件：
     * - 已安装应用：base apk（sourceDir）+ 全部 split（splitSourceDirs）
     * - 选取的文件：单个文件本身（apk / apks / xapk / zip / aab）
     * /data/app 下的 apk 世界可读，无需 root。
     */
    private fun collectSourceApks(): List<File> {
        val packageName = intent.getStringExtra("packageName")
        if (packageName != null) {
            val ai = packageManager.getApplicationInfo(packageName, 0)
            val list = mutableListOf<File>()
            ai.sourceDir?.let { list.add(File(it)) }
            ai.splitSourceDirs?.forEach { list.add(File(it)) }
            return list.distinctBy { it.absolutePath }
        }
        return filePath?.let { listOf(File(it)) } ?: emptyList()
    }

    /** 导出文件的基础名：已安装应用用包名，选取的文件用原文件名（不含扩展名） */
    private fun exportBaseName(): String =
        intent.getStringExtra("packageName") ?: File(filePath!!).nameWithoutExtension

    /**
     * 点击导出：API 28 及以下先确保写存储权限，再执行导出。
     */
    private fun startExport() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            btnExport.isEnabled = false
            requestStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        runExport()
    }

    /**
     * 在后台线程把安装包导出到 Downloads（单 apk 原样导出，多 apk 打包 zip）。
     */
    private fun runExport() {
        val sources = collectSourceApks()
        if (sources.isEmpty()) {
            btnExport.isEnabled = true
            tvUploadStatus.text = getString(R.string.export_no_source)
            return
        }
        btnExport.isEnabled = false
        tvUploadStatus.text = getString(R.string.exporting)
        Thread {
            try {
                val name = exportApksToDownloads(this, exportBaseName(), sources)
                runOnUiThread {
                    tvUploadStatus.text = getString(R.string.export_success, name)
                    btnExport.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    tvUploadStatus.text = getString(R.string.export_failed, e.message ?: e.toString())
                    btnExport.isEnabled = true
                }
            }
        }.start()
    }

    /**
     * 把安装包（单 apk 原样 / 多 apk 打包 zip）以 multipart 上传到 [UploadConfig.APK_UPLOAD_ENDPOINT]，
     * 解析返回的 {"code":0,"msg":"<下载链接>"} 并展示下载链接。多文件打包的临时 zip 上传后删除。
     */
    private fun runUploadApk() {
        val sources = collectSourceApks()
        if (sources.isEmpty()) {
            tvUploadStatus.text = getString(R.string.upload_apk_no_source)
            return
        }
        btnUploadApk.isEnabled = false
        tvUploadStatus.text = getString(R.string.uploading_apk)
        Thread {
            var temp: File? = null
            try {
                val (file, isTemp) = buildPackageFile(this, exportBaseName(), sources)
                if (isTemp) temp = file
                val result = uploadFile(UploadConfig.APK_UPLOAD_ENDPOINT, file)
                // 响应体形如 {"code":0,"msg":"<下载链接>"}，msg 即下载链接
                val json = JSONObject(result.body)
                val link = json.optString("msg")
                runOnUiThread {
                    if (result.code in 200..299 && json.optInt("code", -1) == 0 && link.isNotEmpty()) {
                        tvUploadStatus.text = getString(R.string.upload_apk_success, link)
                    } else {
                        tvUploadStatus.text = getString(R.string.upload_apk_failed, result.body)
                    }
                    btnUploadApk.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    tvUploadStatus.text =
                        getString(R.string.upload_apk_failed, e.message ?: e.toString())
                    btnUploadApk.isEnabled = true
                }
            } finally {
                temp?.delete()
            }
        }.start()
    }
}
