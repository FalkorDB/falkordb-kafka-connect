plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
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
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    // Exclude Kafka dependencies
    exclude("org.apache.kafka")

    // Set the name of the resulting uber JAR
    archiveFileName.set("${project.name}-uber.jar")
}