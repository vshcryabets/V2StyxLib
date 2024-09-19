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
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.4.2")
}

application {
    mainClass.set("FolderServerSample")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("folder-server")
        archiveVersion.set("0.1.0")
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