plugins {
    id("java")
    kotlin("jvm") version "1.5.31"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

// config JVM target to 1.8 for kotlin compilation tasks
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

val version = "1.0.0"
//jar {
//    manifest {
//        attributes 'Implementation-Title': 'V2StyxLibServerDemo', 'Implementation-Version': version
//        attributes 'Main-Class': 'JavaServerSample'
//    }
//}

repositories {
    mavenCentral()
}


dependencies {
    project(":library")
}
