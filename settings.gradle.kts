pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        kotlin("jvm") version "1.8.21"
        kotlin("plugin.spring") version "1.8.21"
        kotlin("plugin.jpa") version "1.8.21"
        id("org.springframework.boot") version "3.2.0"
        id("io.spring.dependency-management") version "1.1.0"
    }
}

rootProject.name = "saga-pattern-choreography"

include(
    "order-service",
    "payment-service",
    "inventory-service",
    "shipping-service",
    "notification-service"
)
