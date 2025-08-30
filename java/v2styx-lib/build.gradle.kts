import ce.defs.domain.DirsConfiguration
import ce.domain.usecase.entry.BuildProjectUseCase
import ce.domain.usecase.load.LoadMetaFilesForTargetUseCase
import ce.domain.usecase.load.LoadProjectUseCaseImpl
import ce.domain.usecase.load.LoadXmlTreeUseCase
import ce.domain.usecase.store.StoreAstTreeUseCase
import ce.domain.usecase.store.StoreOutTreeUseCase
import ce.domain.usecase.transform.TransformInTreeToOutTreeUseCase
import javax.script.ScriptEngineManager

val jdkLevel: JavaLanguageVersion by rootProject.extra

plugins {
    `java-library`
    id("maven-publish")
}

buildscript {
    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
    }
    dependencies {
        classpath("org.codehaus.groovy:groovy-jsr223:3.0.17")
        classpath("com.github.vshcryabets:codegen:0ff0f65344")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
