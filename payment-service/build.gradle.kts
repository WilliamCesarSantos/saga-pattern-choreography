plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

group = "br.com.will.classes.saga.payment-service"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    // removed generic messaging dependency if present
    // implementation("io.awspring.cloud:spring-cloud-aws-messaging:3.0.0")
    // use awspring SQS and SNS modules
    implementation("io.awspring.cloud:spring-cloud-aws-sqs:3.4.2")
    implementation("io.awspring.cloud:spring-cloud-aws-sns:3.4.2")
    implementation("software.amazon.awssdk:sqs:2.20.0")
    implementation("software.amazon.awssdk:sns:2.20.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}