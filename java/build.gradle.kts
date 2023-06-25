buildscript {
  repositories {
    mavenCentral()
    maven ("https://plugins.gradle.org/m2/")
  }
  dependencies {
    classpath("org.junit.platform:junit-platform-gradle-plugin:1.1.1")
  }
}

allprojects {
  group = "com.v2soft"
  version = "1.1.1"

  repositories {
    mavenCentral()
  }
}

plugins {
  kotlin("jvm") version Versions.kotlin apply false
//  id("org.jetbrains.compose") version Versions.compose apply false
}