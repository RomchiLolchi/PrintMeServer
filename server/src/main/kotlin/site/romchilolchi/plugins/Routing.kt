package site.romchilolchi.plugins

import ServiceAttributeSet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import site.romchilolchi.PrintingTools
import java.io.File

fun Application.configureRouting() {
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        staticResources("/", "libs/wasm")

        post("/api/printme/SetAttributesAndGetPrinters") {
            val attrsString = call.receiveText()
            try {
                call.respond(HttpStatusCode.OK, PrintingTools.getPrintersNames(Gson().fromJson(attrsString, object : TypeToken<ServiceAttributeSet>(){})))
            } catch (e: Throwable) {
                println("Throwable caught in SetAttributesAndGetPrinters, proceeding with standard values")
                e.printStackTrace()
                call.respond(HttpStatusCode.OK, PrintingTools.getPrintersNames(ServiceAttributeSet()))
            }
        }

        post("/api/printme/SetChosenPrinterByName") {
            PrintingTools.setSelectedPrinter(call.receiveText())
            call.respond(HttpStatusCode.OK)
        }

        post("/api/printme/PostFile") {
            call.receiveMultipart().forEachPart {
                when (it) {
                    is PartData.FileItem -> {
                        PrintingTools.uploadedFile = File.createTempFile(it.originalFileName.toString().split(".")[0], it.originalFileName.toString().split(".")[1]).apply {
                            writeBytes(
                                it.streamProvider().readBytes()
                            )
                        }
                    }

                    else -> {}
                }
                it.dispose()
            }
            call.respond(HttpStatusCode.OK)
        }

        get("/api/printme/StartPrinting") {
            PrintingTools.print()
            call.respond(HttpStatusCode.OK)
        }
    }
}