# 解析库依赖反射，若开启混淆需保留以下类，否则证书 / AAB 解析会失败。

# ASN.1 解析通过 Class.forName / 反射构造实例
-keep class org.apk.parser.apk.cert.asn1.** { *; }
-keepclassmembers class org.apk.parser.apk.cert.asn1.** { *; }

# entry 数据模型通过反射按字段填充
-keep class org.apk.parser.entry.** { *; }
-keepclassmembers class org.apk.parser.entry.** { *; }
