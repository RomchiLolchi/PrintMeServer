package site.romchilolchi

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.tomcat.*
import site.romchilolchi.plugins.configureHTTP
import site.romchilolchi.plugins.configureRouting
import site.romchilolchi.plugins.configureSerialization

fun main() {
    embeddedServer(Tomcat, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureRouting()
}