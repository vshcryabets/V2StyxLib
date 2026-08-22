import ce.defs.domain.DirsConfiguration
import ce.domain.usecase.entry.BuildProjectUseCase
import ce.domain.usecase.load.LoadMetaFilesForTargetUseCase
import ce.domain.usecase.load.LoadProjectUseCaseImpl
import ce.domain.usecase.store.StoreAstTreeUseCase
import ce.domain.usecase.store.StoreOutTreeUseCase
import ce.domain.usecase.transform.TransformInTreeToOutTreeUseCase
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.artifacts.VersionCatalogsExtension
import javax.script.ScriptEngineManager

val libsCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val jdkLevel = rootProject.extra["jdkLevel"] as JavaLanguageVersion
val shadowArchiveVersion = libsCatalog.findVersion("shadow-archive").get().requiredVersion

plugins {
    java
    application
    alias(libs.plugins.shadow)
}

buildscript {
    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")

    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
    }
    dependencies {
        classpath(libs.findLibrary("groovy-jsr223").get())
        classpath(libs.findLibrary("codegen-server-sample").get())
    }
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
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
}

application {
    mainClass.set("com.v2soft.folderserver.FolderServerSample")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("folder-server")
        archiveVersion.set(shadowArchiveVersion)
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "com.v2soft.folderserver.FolderServerSample"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}
