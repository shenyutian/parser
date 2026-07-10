package org.apk.parser.app

/**
 * 解析结果上传的服务端配置。ENDPOINT 目前是占位地址，接入真实后端时替换即可，
 * 无需改动上传逻辑（见 [uploadJson]）。
 */
object UploadConfig {
    const val ENDPOINT = "https://your-server.example.com/api/apk/upload"

    /**
     * 安装包文件上传地址（multipart/form-data，字段名 file），
     * 返回 {"code":0,"msg":"<下载链接>"}。明文 HTTP 需在 network_security_config 放行该域名。
     */
    const val APK_UPLOAD_ENDPOINT = "http://spms.joy-mind.cn/minio/file/upload"

    fun isConfigured(): Boolean = !ENDPOINT.contains("your-server.example.com")
}
