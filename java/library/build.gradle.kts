plugins {
    id("java")
    id("maven-publish")
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

val version = "1.1.1"

//jar {
//    manifest {
////        attributes["Main-Class"] = "com.example.MainKt"
//        attributes["Implementation-Title"] = "V2StyxLib"
//        attributes["Implementation-Version"] = version
//    }
//}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

//uploadArchives {
//    repositories {
//        mavenDeployer {
//            repository(url: "file://${projectDir}/../mvnrepo/")
//        }
//    }
//}
//
//task sourcesJar(type: Jar, dependsOn: classes) {
//    classifier = "sources"
//    from sourceSets.main.allSource
//}
//
//task javadocJar(type: Jar, dependsOn: javadoc) {
//    classifier = "javadoc"
//    from javadoc.destinationDir
//}
//
//artifacts {
//    archives sourcesJar
//    archives javadocJar
//}

tasks.test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    testLogging {
        events("passed", "skipped", "failed", "started")
    }
}