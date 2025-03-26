import org.gradle.kotlin.dsl.invoke

plugins {
    kotlin("jvm")
}

group = "org.syt.parser.apk"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":base"))
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")

    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}