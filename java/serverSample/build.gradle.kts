plugins {
    `java`
    kotlin("jvm") version "1.5.31"
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":library"))
}

application {
    mainClass.set("JavaServerSample")
}