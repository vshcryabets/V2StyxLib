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

val jdkLevel by extra { JavaLanguageVersion.of(17) }

plugins {
}