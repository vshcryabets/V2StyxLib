import ce.defs.domain.DirsConfiguration
import ce.domain.usecase.entry.BuildProjectUseCase
import ce.domain.usecase.load.LoadMetaFilesForTargetUseCase
import ce.domain.usecase.load.LoadProjectUseCaseImpl
import ce.domain.usecase.load.LoadXmlTreeUseCase
import ce.domain.usecase.store.StoreAstTreeUseCase
import ce.domain.usecase.store.StoreOutTreeUseCase
import ce.domain.usecase.transform.TransformInTreeToOutTreeUseCase
import org.gradle.api.artifacts.VersionCatalogsExtension
import javax.script.ScriptEngineManager

val jdkLevel = rootProject.extra["jdkLevel"] as JavaLanguageVersion

plugins {
    `java-library`
    id("maven-publish")
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
        classpath(libs.findLibrary("codegen-lib").get())
    }
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    toolchain {
        languageVersion.set(jdkLevel)
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    testLogging {
        events("passed", "skipped", "failed", "started")
    }
}

tasks.register("runCgen") {
    group = "Custom"
    description = "Run code generation"

    doLast {
        val engineMaps = mapOf<ce.defs.MetaEngine, javax.script.ScriptEngine>(
            ce.defs.MetaEngine.GROOVY to ScriptEngineManager().getEngineByName("groovy")
        )
        val dirsConfiguration = DirsConfiguration(
            workingDir = rootDir.parent + "/codegen/"
        )
        println("CGEN Project dir = ${dirsConfiguration.workingDir}")
        val buildProjectUseCase = BuildProjectUseCase(
            getProjectUseCase = LoadProjectUseCaseImpl(),
            storeInTreeUseCase = StoreAstTreeUseCase(),
            loadMetaFilesUseCase = LoadMetaFilesForTargetUseCase(engineMaps, LoadXmlTreeUseCase()),
            storeOutTreeUseCase = StoreOutTreeUseCase(),
            transformInTreeToOutTreeUseCase = TransformInTreeToOutTreeUseCase(),
        )
        buildProjectUseCase(
            projectFile = "project.json",
            dirsConfiguration = dirsConfiguration
        )
    }
}
