import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
}

// release 签名凭据从根目录 local.properties 读取，不入库（见 .gitignore）
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "org.apk.parser.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.apk.parser.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            val storeFilePath = localProperties.getProperty("RELEASE_STORE_FILE")
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = rootProject.file("app/$storeFilePath")
                storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            // 默认关闭混淆；如需开启，proguard-rules.pro 已备好证书/AAB 反射的 keep 规则
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // local.properties 未配置签名信息时（如 CI 未注入凭据）storeFile 为空，交由默认签名兜底
            if (!localProperties.getProperty("RELEASE_STORE_FILE").isNullOrBlank()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        // 解析库及其依赖携带签名元数据（META-INF/*.SF/.RSA 等），打包时需排除以免冲突
        resources {
            excludes += "META-INF/*"
        }
    }
}

dependencies {
    implementation(project(":base"))
    // Android 端证书解析走默认 JSSE，排除 BouncyCastle，避免与系统内置 BC 冲突
    implementation(project(":apk")) {
        exclude(group = "org.bouncycastle")
    }
    // 不依赖 :aab（bundletool/protobuf/guava-jre 过重，设备端不解析 aab）

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}
