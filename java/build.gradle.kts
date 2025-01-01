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
//  project.ext {
//    this.set("jdkLevel", 21)
//  }
}

val jdkLevel by extra { JavaLanguageVersion.of(21) }

plugins {
}