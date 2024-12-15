import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
    mainClass.set("com.v2soft.styxlib.StyxConsoleClient")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("console-client")
        archiveVersion.set("0.1.0")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "com.v2soft.styxlib.StyxConsoleClient"))
        }
    }
}