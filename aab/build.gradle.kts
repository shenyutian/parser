plugins {
    kotlin("jvm")
}

group = "org.syt.parser.aab"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":base"))
    implementation(project(":apk"))

    // 支持aab解析的包
    implementation("com.android.tools.build:aapt2-proto:8.9.0-12782657")
    implementation("com.android.tools.build:bundletool:1.18.1")
    implementation("com.google.protobuf:protobuf-java:4.30.1")
    implementation("com.google.protobuf:protobuf-java-util:4.30.1")
    implementation("com.google.protobuf:protobuf-kotlin:4.30.1")
    implementation("com.google.guava:guava:33.4.0-jre")

    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
}

tasks.test {
    useJUnitPlatform()
}