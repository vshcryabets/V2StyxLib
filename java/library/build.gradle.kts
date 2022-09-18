plugins {
    id("java")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val version = "1.1.1"
//tasks.withType(JavaCompile) {
//  options.encoding = "UTF-8"
//}

//jar {
//    manifest {
////        attributes["Main-Class"] = "com.example.MainKt"
//        attributes["Implementation-Title"] = "V2StyxLib"
//        attributes["Implementation-Version"] = version
//    }
//}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    //testRuntime "org.junit.platform:junit-platform-launcher:1.2.0"
    //testRuntime "org.junit.platform:junit-platform-runner:1.2.0"
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
//
//test {
//  testLogging.showStandardStreams = true
//  testLogging {
////    events "PASSED", "FAILED", "SKIPPED", "STARTED"
//  }
//  useJUnitPlatform()
//  maxParallelForks 1
//  options {
////    setIncludeTags(["dev"] as Set)
//  }
//}