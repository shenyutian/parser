package org.apk.parser.app

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 把安装包源文件导出到公共 Downloads 目录：
 * - 只有一个文件时原样导出（保留原扩展名，如 base.apk 或已选取的 .apks）
 * - 有多个文件时（split apk：base + 各 split）打包成一个 zip 导出
 *
 * API 29+ 走 MediaStore.Downloads，免存储权限；API 28 及以下写公共 Downloads 目录，
 * 需调用方先申请 WRITE_EXTERNAL_STORAGE 权限。返回导出后的显示文件名，失败抛异常由调用方处理。
 *
 * @param baseName 导出文件的基础名（不含扩展名），通常为包名或原文件名
 * @param sources  源安装包文件列表
 */
@Throws(Exception::class)
fun exportApksToDownloads(context: Context, baseName: String, sources: List<File>): String {
    val files = sources.filter { it.exists() && it.isFile }
    require(files.isNotEmpty()) { "未找到可导出的安装包文件" }

    return if (files.size == 1) {
        // 单文件：原样导出，保留扩展名
        val src = files[0]
        val ext = src.extension.ifEmpty { "apk" }
        val displayName = "$baseName.$ext"
        writeToDownloads(context, displayName, mimeTypeOf(ext)) { out ->
            src.inputStream().use { it.copyTo(out) }
        }
        displayName
    } else {
        // 多文件：打包成 zip
        val displayName = "$baseName.zip"
        writeToDownloads(context, displayName, "application/zip") { out ->
            ZipOutputStream(out).use { zip ->
                val usedNames = HashSet<String>()
                files.forEach { f ->
                    // 极端情况下不同目录可能出现同名文件，做去重避免 ZipEntry 冲突
                    var entryName = f.name
                    var i = 1
                    while (!usedNames.add(entryName)) {
                        entryName = "${f.nameWithoutExtension}_$i.${f.extension}"
                        i++
                    }
                    zip.putNextEntry(ZipEntry(entryName))
                    f.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
        }
        displayName
    }
}

/**
 * 把安装包源文件构建成一个本地文件（供上传使用）：
 * - 单文件：直接返回源文件本身，不做复制
 * - 多文件：在 cacheDir 打包成 zip 后返回
 *
 * 与 [exportApksToDownloads] 共用打包规则，返回的 File 由调用方负责后续清理（若在 cacheDir）。
 *
 * @return Pair(用于上传的文件, 是否为 cacheDir 生成的临时文件——true 时上传后可删除)
 */
@Throws(Exception::class)
fun buildPackageFile(context: Context, baseName: String, sources: List<File>): Pair<File, Boolean> {
    val files = sources.filter { it.exists() && it.isFile }
    require(files.isNotEmpty()) { "未找到可上传的安装包文件" }

    if (files.size == 1) return files[0] to false

    val zip = File(context.cacheDir, "$baseName.zip")
    zip.outputStream().use { out ->
        ZipOutputStream(out).use { zos ->
            val usedNames = HashSet<String>()
            files.forEach { f ->
                var entryName = f.name
                var i = 1
                while (!usedNames.add(entryName)) {
                    entryName = "${f.nameWithoutExtension}_$i.${f.extension}"
                    i++
                }
                zos.putNextEntry(ZipEntry(entryName))
                f.inputStream().use { it.copyTo(zos) }
                zos.closeEntry()
            }
        }
    }
    return zip to true
}

/**
 * 把内容写入公共 Downloads 目录。API 29+ 用 MediaStore（先置 IS_PENDING，写完再解除），
 * 失败时删除半成品；API 28 及以下直接写外部存储 Downloads 目录。
 */
@Throws(Exception::class)
private fun writeToDownloads(
    context: Context,
    displayName: String,
    mime: String,
    writer: (OutputStream) -> Unit,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, displayName)
            put(MediaStore.Downloads.MIME_TYPE, mime)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("无法在 Downloads 中创建文件")
        try {
            resolver.openOutputStream(uri)?.use(writer)
                ?: throw IllegalStateException("无法打开输出流")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            throw e
        }
    } else {
        @Suppress("DEPRECATION")
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) dir.mkdirs()
        File(dir, displayName).outputStream().use(writer)
    }
}

/** 按扩展名推断 MIME 类型 */
private fun mimeTypeOf(ext: String): String = when (ext.lowercase()) {
    "apk" -> "application/vnd.android.package-archive"
    "zip", "apks", "xapk" -> "application/zip"
    else -> "application/octet-stream"
}
