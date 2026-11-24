# getInfo() 输出 JSON 字段说明

本文说明 `ApkFile`、`ApksFile`、`ByteArrayApkFile`、`AabFile` 的 `getInfo()` 返回的 JSON 中**每一个字段**的含义、类型、出现条件和计算方式。App 模块解析后展示 / 分享 / 上传的数据都以这份 JSON 为基础。

> 注意：`AbstractApkFile.toJson()` 是另一套调试用的中间态输出（`dexClasses`、`apkMeta` 字符串、`apkSigners`、`resourceTable` 等），App 不展示、不上传，也不在本文范围内。

字段由以下几层代码写入：

| 写入层 | 方法 | 负责字段 |
| --- | --- | --- |
| `BaseApkFile` | `getInfo()` | `pkg`、`label`、`launcher`、`version`、`versionCode`、`MinSdkVersion`、`MaxSdkVersion`、`TargetSdkVersion`、11 个 SDK 判定布尔字段 |
| `AbstractApkFile` | `getInfo()` | `sign`、`SHA1`、`SHA256`、`fbkey`、`signatureInfo` |
| `ApkFile` / `ByteArrayApkFile` / `ApksFile` | `getInfo()` | `apkSize`、`apkFileMd5`、`apkFileSha256` |
| `AabFile` | `getInfo()` | `aabSize`、`aabFileMd5`、`aabFileSha1`、`aabFileSha256`、`signatureInfo`、`sign`、`SHA1`、`SHA256`、`fbkey` |

## 0. 先看这几条通用规则

1. **值为 `null` 的字段不会出现。** 所有字段都用 `JSONObject.putOpt` 写入，值为 `null` 时直接跳过。所以 `label`、`launcher`、`MaxSdkVersion`、`sign` 等字段可能整条缺失，缺失不等于空字符串。
2. **布尔字段只要 DEX 解析成功就一定出现**，取值为 `true` / `false`，不会缺失。
3. **只有 `versionCode`、`apkSize`、`aabSize`、`apkLength` 是 JSON number**，其余字段（包括所有摘要、`version`、SDK 版本）都是字符串。注意 `MinSdkVersion`、`MaxSdkVersion`、`TargetSdkVersion` 是**字符串**（如 `"27"`），不是数字。
4. **十六进制一律小写、无 `0x` 前缀**；Base64 使用标准字母表并带 `=` 填充。
5. JSON 对象的键顺序按代码写入顺序排列，但 JSON 语义上无序，解析时不要依赖顺序。
6. 各段解析都有 try/catch：某一段失败只会导致对应字段缺失，不会让整个 JSON 失败。

## 1. 应用基础信息

| 字段 | 类型 | 出现条件 | 含义 | 取值来源 |
| --- | --- | --- | --- | --- |
| `pkg` | string | 正常 APK/AAB 必有 | 应用包名 | manifest 的 `package` 属性（AAB 为 protobuf manifest 的 `package`） |
| `label` | string | `application` 声明了 `android:label` 且能解析出值 | 应用显示名 | APK：字面量直接返回；若为资源引用，从 `resources.arsc` 按 `preferredLocale` 解析（默认 `Locale.US`，可在解析前用 `setPreferredLocale` 修改）。AAB：尝试按资源名回查字符串，**失败时保留原样引用**，如 `"@string/app_name"` |
| `launcher` | string | 存在 `MAIN` + `LAUNCHER` 的 Activity | 启动 Activity 的类名 | 按 manifest 中 Activity 声明顺序，第一个同时含有 `android.intent.action.MAIN` 与 `android.intent.category.LAUNCHER` 的 Activity 的 `android:name` 原值（可能以 `.` 开头，不做补全；**不包含** `activity-alias`） |
| `version` | string | 声明了 `android:versionName` | **版本名**（不是版本号） | `android:versionName` |
| `versionCode` | number (long) | 声明了 `android:versionCode` | 版本号 | APK：`android:versionCode`；若同时声明了 `versionCodeMajor`，取 `(versionCodeMajor << 32) \| versionCode`。AAB：只取 `versionCode` 本身，不合并 major |
| `MinSdkVersion` | string | `uses-sdk` 声明了 `android:minSdkVersion` | 最低 SDK | `uses-sdk` 的 `android:minSdkVersion`（注意字段名首字母大写、值为字符串） |
| `MaxSdkVersion` | string | `uses-sdk` 声明了 `android:maxSdkVersion` | 最高 SDK | `uses-sdk` 的 `android:maxSdkVersion`；**AAB 不解析该属性，字段不会出现** |
| `TargetSdkVersion` | string | `uses-sdk` 声明了 `android:targetSdkVersion` | 目标 SDK | `uses-sdk` 的 `android:targetSdkVersion` |

## 2. 内嵌 SDK 判定字段（布尔）

| 字段 | 匹配的 DEX 类名子串 |
| --- | --- |
| `isUnityGame` | `com/unity3d/player` |
| `isUnityAds` | `com/unity3d/ads` |
| `isFacebookAds` | `com/facebook/ads` |
| `isGoogleAdmob` | `com/google/ads` |
| `isIronsource` | `com/ironsource` |
| `isMintegral` | `com/mbridge` |
| `isApplovin` | `com/applovin` |
| `isAdjust` | `com/adjust/sdk` |
| `isAppsflyer` | `com/appsflyer` |
| `isFirebaseAnalytics` | `com/google/firebase/analytics/FirebaseAnalytics` |
| `isFacebookAnalytics` | `com/facebook/appevents` |

判定方式：

- 遍历 `classes*.dex` 中的所有类描述符（形如 `Lcom/unity3d/player/UnityPlayer;`），对描述符做**子串匹配**，命中任意一个类即为 `true`。
- 只代表“包里存在该包名下的类”，**不是版本号识别**；SDK 被混淆、裁剪或改包名后可能识别不到。
- DEX 解析抛异常时，这 11 个字段全部缺失（而不是全为 `false`）。
- 新增/修改 SDK 判定改 `BaseApkFile.getInfo()` 里的 `adFrameworks` map。

## 3. 旧版签名摘要字段

| 字段 | 类型 | 出现条件 | 实际内容 |
| --- | --- | --- | --- |
| `sign` | string | 存在可取到的签名证书 | **证书 DER 的十六进制（小写）**。字段名和工具类名容易误导：`MD5.toHexString()` 只做字节转 hex，**没有做 MD5**。取值与 `signatureInfo.signatures[]` 中同一张证书的 `signature` 完全相同 |
| `SHA1` | string (Base64) | 同上 | `Base64(SHA-1(公钥 DER))`，公钥编码为 X.509 SubjectPublicKeyInfo |
| `SHA256` | string (Base64) | 同上 | `Base64(SHA-256(公钥 DER))` |
| `fbkey` | string (Base64) | 同上 | `Base64(SHA-1(证书 DER))`，即 Facebook 后台登记的 Key Hash |

证书选取规则：

- **APK / APKS**：先取 `getApkSingers()` 的第一张 v1 证书（`META-INF/*.RSA|*.DSA|*.EC`）；取不到时回退到 `getApkV2Singers()` 的第一张证书（V2 签名块）。**不会回退到 v3**，所以只有 v3 签名的包这四个字段会全部缺失。
- **APKS**：v1 证书来自内部所有 split 的聚合结果（base 排在最前）；v2 回退读的是外层 `.apks` 文件本身（通常没有签名块）。
- **AAB**：只取 `.aab` 容器 `META-INF/` 下的 v1 证书，没有 v2 回退。
- 未签名时四个字段全部缺失。
- 若 `sign` 等于 `signatureInfo.signatures[]` 中某个元素的 `signature`，说明两者取到的是同一张证书（常见情况）。

## 4. `signatureInfo`（详细签名信息）

### 4.1 `signatureInfo` 顶层

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| `apkLength` | number (long) | 参与摘要计算的字节数：`ApkFile` / `ByteArrayApkFile` 为 APK 字节数；`ApksFile` 为**基准包**的字节数；`AabFile` 为 **.aab 文件**的字节数（历史命名，AAB 也沿用 `apk` 前缀） |
| `apkMd5` | string (hex) | 上述字节的 MD5 |
| `apkSha1` | string (hex) | 上述字节的 SHA-1 |
| `apkSha256` | string (hex) | 上述字节的 SHA-256 |
| `signatures` | array | 证书列表，见 4.2；**未签名时为空数组 `[]`，字段仍然存在** |

字段存在条件：`ApkFile` / `AabFile` 解析成功时一定有；`ByteArrayApkFile` 需要构造时传入了非空 `apkData`；`ApksFile` 需要能定位到基准包。解析异常时整个 `signatureInfo` 缺失。

### 4.2 `signatures[]` 每个元素

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| `scheme` | string | 签名方案：`"v1"`（JAR）、`"v2"`、`"v3"` |
| `principal` | string | 证书主体 Subject DN，格式为 `X500Principal.getName()`，如 `C=US,O=Android,CN=Android Debug`（与 openssl 默认输出顺序相反） |
| `issuer` | string | 证书颁发者 Issuer DN，格式同上 |
| `md5` | string (hex) | 证书 DER 的 MD5 |
| `sha1` | string (hex) | 证书 DER 的 SHA-1 |
| `sha256` | string (hex) | 证书 DER 的 SHA-256 |
| `signature` | string (hex) | 证书 DER 的完整十六进制（小写） |

### 4.3 收集顺序与去重

- 单个 APK 内顺序固定为：先全部 v1 证书，再 v2，再 v3。AAB 走 archive 模式时，`.aab` 容器自身的签名排在内嵌 APK 条目的签名之前。
- v1 来源：ZIP 内 `META-INF/*.RSA|*.DSA|*.EC` 的 PKCS#7 签名块，按 ZIP 条目顺序遍历；同一方案内按证书 DER 去重。
- v2 / v3 来源：APK Signing Block；只对 APK / APKS / 内嵌 APK 生效。
- **同一张证书可能同时出现多条**（一条 v1、一条 v2、一条 v3），这是正常现象，表示三种方案都签了名，不是重复 bug。
- `readArchiveSignatureInfo()`（AAB 使用）：先解析 `.aab` 容器自身的 v1 签名，再逐个解析容器内嵌 `.apk` 条目（v1/v2/v3），最后按 `scheme + ":" + 证书DER` 去重。因此 AAB 的列表里可能混有 `.aab` 容器和内部 APK 的签名。

## 5. 文件级摘要字段

| 字段 | 出现于 | 类型 | 含义 |
| --- | --- | --- | --- |
| `apkSize` | `ApkFile` / `ByteArrayApkFile` / `ApksFile` | number | 文件字节数。APKS 取基准包的字节数；找不到基准包时回退整个 `.apks` 文件 |
| `apkFileMd5` | APK 家族 | string (hex) | 与 `apkSize` 同一份字节的 MD5 |
| `apkFileSha256` | APK 家族 | string (hex) | 与 `apkSize` 同一份字节的 SHA-256 |
| `aabSize` | `AabFile` | number | `.aab` 文件字节数 |
| `aabFileMd5` | `AabFile` | string (hex) | `.aab` 文件 MD5 |
| `aabFileSha1` | `AabFile` | string (hex) | `.aab` 文件 SHA-1 |
| `aabFileSha256` | `AabFile` | string (hex) | `.aab` 文件 SHA-256 |

注意：

- **APK 顶层没有 SHA-1 字段**：目前只有 `apkFileMd5`、`apkFileSha256`；SHA-1 只有 `signatureInfo.apkSha1`。AAB 顶层则有 `aabFileSha1`。
- 同一份数据的摘要会在两处重复出现：`apkSize` = `signatureInfo.apkLength`，`apkFileMd5` = `signatureInfo.apkMd5`，`apkFileSha256` = `signatureInfo.apkSha256`；AAB 则是 `aabSize` / `aabFileMd5` / `aabFileSha1` / `aabFileSha256` 与 `signatureInfo.apk*` 一一对应。两处数值应当一致。

## 6. 不同输入类型的差异

| 输入类 | 文件级字段 | `signatureInfo` 的字节来源 | `sign` / `SHA1` / `SHA256` / `fbkey` 的证书来源 | v2 / v3 |
| --- | --- | --- | --- | --- |
| `ApkFile`（磁盘 `.apk`） | `apkSize`、`apkFileMd5`、`apkFileSha256` | 整个 APK 文件 | 首个 v1 证书，回退首个 v2 证书 | 支持 |
| `ByteArrayApkFile`（内存字节） | 同上 | 传入的字节数组 | 同 `ApkFile` | 支持 |
| `ApksFile`（`.apks` / `.xapk`） | 同上；优先基准包，找不到基准包时回退整个容器 | **仅基准包**的字节；找不到基准包时无 `signatureInfo` | 内部所有 split 的 v1 证书（基准包排最前），v2 回退读外层容器 | 基准包支持 |
| `AabFile`（`.aab`） | `aabSize`、`aabFileMd5`、`aabFileSha1`、`aabFileSha256` | 整个 `.aab` 文件 | 仅 `.aab` 容器 v1 证书，无 v2 回退 | 仅解析内嵌 APK 条目时支持 |

“基准包”的识别顺序：文件名等于 `base.apk`（xapk 常见）→ 文件名包含 `base-master`（bundletool 拆分包）→ 文件名包含 `base` → 以上都没有时取体积最大的 `.apk`。

## 7. 摘要 / 编码速查

| 字段 | 输入字节 | 算法与编码 |
| --- | --- | --- |
| `sign` | 证书 DER | 无摘要，直接转小写 hex |
| `signatureInfo.signatures[].signature` | 证书 DER | 无摘要，直接转小写 hex |
| `signatureInfo.signatures[].md5` / `.sha1` / `.sha256` | 证书 DER | MD5 / SHA-1 / SHA-256，小写 hex |
| `SHA1` / `SHA256` | 公钥 DER（SubjectPublicKeyInfo） | SHA-1 / SHA-256，**Base64** |
| `fbkey` | 证书 DER | SHA-1，**Base64** |
| `apkMd5` / `apkSha1` / `apkSha256` | APK / 基准包 / .aab 字节 | MD5 / SHA-1 / SHA-256，小写 hex |
| `apkFileMd5` / `apkFileSha256` | APK / 基准包 / .apks 字节 | MD5 / SHA-256，小写 hex |
| `aabFileMd5` / `aabFileSha1` / `aabFileSha256` | .aab 字节 | MD5 / SHA-1 / SHA-256，小写 hex |

## 8. App 上传接口的 payload（另一份 JSON）

`AppActivity.buildSignatureUploadJson()` 会从完整解析结果中抽取字段，作为请求体提交给签名接口。它与 `getInfo()` 的 JSON **不是同一份**：

| 字段 | 类型 | 取值 |
| --- | --- | --- |
| `pkg` | string | `pkg` |
| `apkMd5` | string | `signatureInfo.apkMd5` |
| `apkSha1` | string | `signatureInfo.apkSha1` |
| `apkSha256` | string | `signatureInfo.apkSha256` |
| `apkLength` | string | `signatureInfo.apkLength` **转成字符串**（接口约定） |
| `signatures` | string | `signatureInfo.signatures` 数组序列化后的 **JSON 字符串**（不是数组） |

当 `pkg` 为空、或缺少 `signatureInfo` / `signatures` 时返回 null，App 会禁用上传按钮。设备端只支持 `.apk` / `.apks`，不支持 `.aab`。

## 9. 示例（长字段已截断）

`app-debug.apk` 实际输出：

```json
{
    "pkg": "com.example",
    "version": "1.0",
    "versionCode": 1,
    "MinSdkVersion": "27",
    "TargetSdkVersion": "29",
    "isUnityGame": false,
    "isUnityAds": false,
    "isFacebookAds": false,
    "isGoogleAdmob": false,
    "isIronsource": false,
    "isMintegral": false,
    "isApplovin": false,
    "isAdjust": false,
    "isAppsflyer": false,
    "isFirebaseAnalytics": false,
    "isFacebookAnalytics": false,
    "sign": "308202e2…（证书 DER，1484 个十六进制字符）",
    "SHA1": "XoUd7c4U716jcymRLkcpgiSpeMs=",
    "SHA256": "8L2SoI+UBxdjQtTPzUnmXnVQnCay0oIauyNoA7lK8Ks=",
    "fbkey": "yIGb1y2+CgdCve2nApr5bYJAG9M=",
    "signatureInfo": {
        "apkLength": 5988,
        "apkMd5": "a962268c81cd2f0f680b09c2a6c589de",
        "apkSha1": "c42c984a017c8b2bdfda96cc4562020fefcf2b16",
        "apkSha256": "569a31c8049879c4d0c42130f3baf8f27f27cd26541ddba66b829534263bf20c",
        "signatures": [
            {
                "scheme": "v2",
                "principal": "C=US,O=Android,CN=Android Debug",
                "issuer": "C=US,O=Android,CN=Android Debug",
                "md5": "58755292e637993b3f25cd139e6111f0",
                "sha1": "c8819bd72dbe0a0742bdeda7029af96d82401bd3",
                "sha256": "76888d4b16da49ae1b19ebdffb90f6c6d49be0eaa9516e5901ec49efeef82755",
                "signature": "308202e2…（与 sign 相同）"
            }
        ]
    },
    "apkSize": 5988,
    "apkFileMd5": "a962268c81cd2f0f680b09c2a6c589de",
    "apkFileSha256": "569a31c8049879c4d0c42130f3baf8f27f27cd26541ddba66b829534263bf20c"
}
```

该 APK 无 `label` / `launcher` / `MaxSdkVersion`，且只有 v2 签名，所以 `sign` 来自 v2 证书回退，且与 `signatures[0].signature` 相同。

AAB 的差异字段（以签名过的 `qr.aab` 为例，证书长字段已截断）：

```json
{
    "aabSize": 5562530,
    "aabFileMd5": "276c82ed181bc06fde8c5b7cb480c232",
    "aabFileSha1": "f8728e073f332f8d2487c8ff440a4c8a8e0b7e8b",
    "aabFileSha256": "d2927ba7b985a9499befab708f532d923994396cfb32acdb962af16c5b259d48",
    "signatureInfo": {
        "apkLength": 5562530,
        "apkMd5": "276c82ed181bc06fde8c5b7cb480c232",
        "apkSha1": "f8728e073f332f8d2487c8ff440a4c8a8e0b7e8b",
        "apkSha256": "d2927ba7b985a9499befab708f532d923994396cfb32acdb962af16c5b259d48",
        "signatures": [
            {
                "scheme": "v1",
                "principal": "CN=qrsnap,OU=qrsnap,O=qrsnap,L=,ST=,C=",
                "issuer": "CN=qrsnap,OU=qrsnap,O=qrsnap,L=,ST=,C=",
                "md5": "4ed5c247e4ea3120701c27a8ebf94267",
                "sha1": "475b1bee715a399ff2030692a93830ca7fd47413",
                "sha256": "df0eefa8748a5d6faa7840febcaa393a29ccc5da589bac8a22c26c99a2ea8958",
                "signature": "30820549…（证书 DER）"
            }
        ]
    }
}
```

未签名 AAB（如测试用的 `debug.aab`）会保留 `signatureInfo`，但 `signatures` 为 `[]`，且没有 `sign` / `SHA1` / `SHA256` / `fbkey`。

## 10. 自行核对方法

| 想核对 | 命令 |
| --- | --- |
| 文件 MD5 / SHA-1 / SHA-256 | `md5 -q app.apk`、`shasum -a 1 app.apk`、`shasum -a 256 app.apk` |
| v1/v2/v3 证书摘要与 DN | `apksigner verify --print-certs -v app.apk` |
| v1 证书（JAR 签名） | `keytool -printcert -jarfile app.apk` |
| AAB 签名 | `jarsigner -verify -verbose -certs app.aab` |
| 证书 DER 的 MD5/SHA-1/SHA-256 | 先导出 DER，再分别执行 `openssl dgst -md5 cert.der`、`openssl dgst -sha1 cert.der`、`openssl dgst -sha256 cert.der` |
| 公钥的 SHA-1（对应 `SHA1`） | `openssl x509 -inform DER -in cert.der -pubkey -noout \| openssl pkey -pubin -outform DER \| openssl dgst -sha1 -binary \| base64` |
| `fbkey` | `openssl dgst -sha1 -binary cert.der \| base64` |
