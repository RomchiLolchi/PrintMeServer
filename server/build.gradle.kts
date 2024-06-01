plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.gretty)
    id("war")
}

gretty {
    servletContainer = "tomcat10"
    contextPath = "/"
    logbackConfigFile = "src/main/resources/logback.xml"
}

afterEvaluate {
    tasks.getByName("run") {
        dependsOn("appRun")
    }
}

group = "site.romchilolchi"
version = "1.0.0"
application {
    mainClass.set("site.romchilolchi.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=${extra["development"] ?: "false"}")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization.gson)
    implementation(libs.ktor.server.servlet.jakarta)
    implementation(libs.ktor.server.tomcat)
    implementation(libs.ktor.server.swagger)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
    implementation("org.apache.pdfbox:pdfbox:3.0.2")
    implementation("org.cups4j:cups4j:0.7.6")
}