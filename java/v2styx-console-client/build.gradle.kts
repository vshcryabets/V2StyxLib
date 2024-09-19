plugins {
    java
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":v2styx-lib"))
    implementation("org.jline:jline:3.25.0")
}

application {
    mainClass.set("StyxConsoleClient")
}