import ce.defs.domain.DirsConfiguration
import ce.domain.usecase.entry.BuildProjectUseCase
import ce.domain.usecase.load.LoadMetaFilesForTargetUseCase
import ce.domain.usecase.load.LoadProjectUseCaseImpl
import ce.domain.usecase.store.StoreAstTreeUseCase
import ce.domain.usecase.store.StoreOutTreeUseCase
import ce.domain.usecase.transform.TransformInTreeToOutTreeUseCase
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import javax.script.ScriptEngineManager

val jdkLevel: JavaLanguageVersion by rootProject.extra

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

buildscript {
    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
    }
    dependencies {
        classpath("org.codehaus.groovy:groovy-jsr223:3.0.17")
        classpath("com.github.vshcryabets:codegen:4895044cf9")
    }
}


java {
    toolchain {
        languageVersion.set(jdkLevel)
    }
}

val version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":v2styx-lib"))
    implementation("org.jline:jline:3.25.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
}

application {
    mainClass.set("com.v2soft.folderserver.FolderServerSample")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("folder-server")
        archiveVersion.set("0.1.0")
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
