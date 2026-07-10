package org.apk.parser.app

import java.io.DataOutputStream
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/** 上传响应：HTTP 状态码 + 响应体 */
data class UploadResult(val code: Int, val body: String)

/**
 * 将 JSON 文本以 POST 方式上传（Content-Type: application/json）。
 * 网络/协议错误直接抛出，由调用方捕获并展示。
 */
@Throws(Exception::class)
fun uploadJson(url: String, json: String): UploadResult {
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = 10_000
        connection.readTimeout = 15_000
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")

        OutputStreamWriter(connection.outputStream, StandardCharsets.UTF_8).use { it.write(json) }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: ""
        return UploadResult(code, body)
    } finally {
        connection.disconnect()
    }
}

/**
 * 以 multipart/form-data 方式上传单个文件（字段名默认 file），流式写出避免大文件 OOM。
 * 网络/协议错误直接抛出，由调用方捕获并展示。
 */
@Throws(Exception::class)
fun uploadFile(url: String, file: File, formField: String = "file"): UploadResult {
    val boundary = "----ParserAppBoundary${System.currentTimeMillis()}"
    val lineEnd = "\r\n"
    val connection = URL(url).openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.useCaches = false
        connection.connectTimeout = 15_000
        connection.readTimeout = 60_000
        // 大文件分块流式传输，不在内存缓存整个 body
        connection.setChunkedStreamingMode(0)
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

        DataOutputStream(connection.outputStream).use { out ->
            out.writeBytes("--$boundary$lineEnd")
            out.writeBytes(
                "Content-Disposition: form-data; name=\"$formField\"; filename=\"${file.name}\"$lineEnd"
            )
            out.writeBytes("Content-Type: application/octet-stream$lineEnd")
            out.writeBytes(lineEnd)
            file.inputStream().use { it.copyTo(out) }
            out.writeBytes(lineEnd)
            out.writeBytes("--$boundary--$lineEnd")
        }

        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() } ?: ""
        return UploadResult(code, body)
    } finally {
        connection.disconnect()
    }
}
