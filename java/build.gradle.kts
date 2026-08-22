import org.gradle.api.artifacts.VersionCatalogsExtension

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val projectVersion = libs.findVersion("styx-project").get().requiredVersion
val javaVersion = libs.findVersion("java").get().requiredVersion.toInt()

allprojects {
  group = "com.v2soft"
  version = projectVersion

  repositories {
    mavenCentral()
  }
}

extra["jdkLevel"] = JavaLanguageVersion.of(javaVersion)
