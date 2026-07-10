# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Java/Kotlin 混合库，用于解析 Android 的 `.apk`、`.apks`（split apk 打包）和 `.aab`（App Bundle）文件，提取 manifest 元信息、图标、证书/签名、DEX 类、原生库以及内嵌的第三方 SDK（广告 / 统计 / Firebase）识别结果。核心 APK 解析代码源自 dongliu 的 apk-parser，并在此基础上做了大量扩展。

## 常用命令

JDK / Kotlin JVM target 均为 **17**（根 `build.gradle.kts` 中固定）。

```bash
./gradlew build                 # 全量构建三个模块
./gradlew test                  # 运行所有测试
./gradlew :apk:test             # 只测 apk 模块
./gradlew :aab:test             # 只测 aab 模块
./gradlew :apk:test --tests ApkFileTest                    # 单个测试类
./gradlew :apk:test --tests ApkFileTest.testParserMeta     # 单个测试方法
```

测试基于 **JUnit 5（jupiter）**。测试用的 apk/aab 样例放在各模块 `src/test/resources/apks/` 下，通过 `getClassLoader().getResource("apks/xxx.apk")` 加载。

发布到 Nexus 私服（每个模块单独版本号）：

```bash
./gradlew :base:publishToMaven
./gradlew :apk:publishToMaven
./gradlew :aab:publishToMaven
```

发布凭据从根目录 `local.properties` 读取（`mavenUrl` / `mavenSnaUrl` / `user` / `password`），逻辑在 `maven-publish.gradle`。版本号以 `-SNAPSHOT` 结尾时自动走 snapshot 仓库。

CLI 入口：`org.apk.parser.apk.Main`，参数为 `<action> <apkPath>`，action 取 `meta` / `manifest` / `signer`（未配置 application 插件，需自行用 `java -cp` 运行）。

## 模块结构与依赖

三个 Gradle 模块（见 `settings.gradle.kts`），依赖方向 **base ← apk ← aab**：

- **base** — 与文件格式无关的共享层。包含：
  - `BaseApkFile`（Kotlin 抽象基类）：定义 `getApkMeta()` / `getDexClasses()` / `getAllIcons()`，并实现 `getInfo()` —— 通过扫描 DEX 类名匹配包路径来识别内嵌 SDK（Unity、Facebook、AdMob、ironSource、Mintegral、AppLovin、Adjust、AppsFlyer、Firebase 等）。**新增 SDK 识别时改这里的 `adFrameworks` map。**
  - `entry/` —— 对外数据模型（`ApkMeta`、`Application`、`Activity`、`Service`、`Permission`、`DexClass`、`IconFace` 等），Kotlin 与 Java 混用。
  - 自带轻量 `json/`（org.json 风格实现）、`log/`（`Log`）、`util/MD5`，避免外部依赖。
- **apk** — APK/APKS 二进制解析的主体，纯 Java。
- **aab** — AAB（protobuf 格式）解析，依赖 Google `bundletool` + `aapt2-proto` + `protobuf`。

## APK 解析架构（apk 模块）

**入口层**：
- `ApkParsers` —— 静态便捷方法（`getMetaInfo` / `getManifestXml` 等），内部 try-with-resources 包一层 file 对象。
- 三种 `AbstractApkFile` 子类，区别只在"字节从哪来"：
  - `ApkFile`（基于 `ZipFile` + `FileChannel`，走磁盘文件）
  - `ByteArrayApkFile`（内存字节数组）
  - `ApksFile`（`.apks` 容器，内部把每个 split `.apk` 解析成 `ByteArrayApkFile`；注意内部有 TODO 标注内存占用偏大）

**核心抽象 `AbstractApkFile`**：承载所有实际解析逻辑（manifest 二进制 XML、资源表、V2 签名块、证书、原生库、DEX），带懒解析 + 缓存标志位（`manifestParsed`、`resourceTableParsed` 等）。**非线程安全**。子类只需实现字节访问原语：`getFileData(path)`、`getAllFile()`、`fileData()`（返回整个文件的 `ByteBuffer`）、`getAllCertificateData()`。

**parser/ 与 struct/**：
- `BinaryXmlParser` + `XmlTranslator` / `ApkMetaTranslator` —— 解码二进制 AndroidManifest.xml。
- `ResourceTableParser` —— 解析 `resources.arsc`，支持按 `preferredLocale` 与像素密度选择资源（图标、字符串本地化）。
- `DexParser` —— 读取 `classes*.dex` 的类列表（供 SDK 识别用）。
- 签名/证书：`ApkSignBlockParser`（APK Signing Block v2）；证书解析默认走 **JSSE**（`JSSECertificateParser`），可选 **BouncyCastle**（`BCCertificateParser`）——通过 `ApkParsers.useBouncyCastle(true)` 全局切换，需自行引入 bcprov/bcpkix。`cert/asn1/`、`cert/pkcs7/` 是自带的 ASN.1/PKCS#7 解码实现。
- `struct/` 下是各二进制格式的结构体定义（`resource/`、`xml/`、`signingv2/`、`zip/`）。

## AAB 解析架构（aab 模块）

`AabFile` 直接继承 `BaseApkFile`（**不走** `AbstractApkFile` 那套二进制解析）。因为 AAB 的 manifest 与资源是 **protobuf** 格式，通过 bundletool/aapt2-proto 读取：`AabMetaTranslator`（manifest → `ApkMeta`）、`AabUtil`、`ResourceTableBuilder`。`FirebaseKey` 定义了从 `google-services.json` 提取 Firebase 配置的键名。证书/DEX 解析复用 apk 模块的 `CertificateParser` / `DexParser`。

## 约定

- 新增对外字段优先加到 base 的 `entry/` 模型中，保持 apk 与 aab 输出结构一致（`getInfo()` 返回统一的 `JSONObject`）。
- 涉及"是否包含某 SDK/框架"的判断，统一在 `BaseApkFile.getInfo()` 里通过 DEX 类名或资源匹配实现。
- 仓库对话、注释、commit message 使用中文（见全局 CLAUDE.md 语言规范）。
