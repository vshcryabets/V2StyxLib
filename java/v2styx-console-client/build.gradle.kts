plugins {
    java
    kotlin("jvm")
    application
}

kotlin {
    jvmToolchain(Versions.jvmLevel)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.jvmLevel))
    }
}

val version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":v2styx-lib"))
}

application {
    mainClass.set("JavaServerSample")
}