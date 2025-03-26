plugins {
    // 应用到所有子项目的插件
    kotlin("jvm") version "1.9.20" apply false
}

allprojects {
    group = "org.syt.parser"
    version = "1.0.0"

    repositories {
        mavenCentral()
        google() // 公共仓库配置
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/jcenter")
        maven("https://maven.aliyun.com/repository/maven-public")
    }
}

subprojects {
    // 配置所有子项目的通用设置
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    repositories {
        mavenCentral() // 公共仓库配置
        google() // 公共仓库配置
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/jcenter")
        maven("https://maven.aliyun.com/repository/maven-public")
    }
}
