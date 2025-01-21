plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
    signing
}

group = "com.falkordb"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:connect-api:3.9.0")
    implementation("io.lettuce:lettuce-core:6.5.2.RELEASE")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation("org.springframework:spring-core:6.2.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.github.ben-manes.caffeine:caffeine:3.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    exclude("org.apache.kafka")
    archiveFileName.set("${project.name}-uber.jar")
}

tasks.named("build") {
    dependsOn(tasks.named("shadowJar"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").get())

            pom {
                name.set("FalkorDB Kafka Connect Redis Sink")
                description.set("FalkorDB Kafka Connect Redis Sink.")
                url.set("https://github.com/FalkorDB/falkordb-kafka-connect")

                licenses {
                    license {
                        name.set("BSD 3-Clause License")
                        url.set("https://opensource.org/license/bsd-3-clause")
                    }
                }

                developers {
                    developer {
                        id.set("barakb")
                        name.set("Barak Bar Orion")
                        email.set("barak.bar@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/FalkorDB/falkordb-kafka-connect.git")
                    developerConnection.set("scm:git:ssh://github.com/FalkorDB/falkordb-kafka-connect.git")
                    url.set("https://github.com/FalkorDB/falkordb-kafka-connect")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }
}
