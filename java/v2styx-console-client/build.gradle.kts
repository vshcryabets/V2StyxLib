import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.artifacts.VersionCatalogsExtension

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val jdkLevel = rootProject.extra["jdkLevel"] as JavaLanguageVersion
val shadowArchiveVersion = libsCatalog.findVersion("shadow-archive").get().requiredVersion

plugins {
    java
    application
    alias(libs.plugins.shadow)
}

java {
    toolchain {
        languageVersion.set(jdkLevel)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":v2styx-lib"))
    implementation(libs.jline)
}

application {
    mainClass.set("com.v2soft.styxlib.StyxConsoleClient")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("console-client")
        archiveVersion.set(shadowArchiveVersion)
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "com.v2soft.styxlib.StyxConsoleClient"))
        }
    }
}