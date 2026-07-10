package org.apk.parser.app

import android.graphics.drawable.Drawable

/**
 * 已安装应用的展示信息（来自系统 PackageManager，非解析库）
 */
data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable,
    val versionName: String,
    val versionCode: Int,
    val installTime: Long,
    val updateTime: Long,
    val targetSdkVersion: Int,
    // 签名证书签发者（Issuer DN），解析失败或无签名时为 null
    val certIssuer: String?,
)
