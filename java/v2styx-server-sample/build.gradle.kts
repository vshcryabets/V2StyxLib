import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "7.1.2"
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
    mainClass.set("FolderServerSample")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("shadow")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "FolderServerSample"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}