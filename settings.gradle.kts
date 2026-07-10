pluginManagement {
    // 插件解析仓库：AGP 在 google()，foojay/kotlin 在 gradlePluginPortal()
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "parser"
include(":base", ":apk", ":aab", ":app")
