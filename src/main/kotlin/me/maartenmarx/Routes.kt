package me.maartenmarx

import io.ktor.server.routing.*
import io.ktor.server.application.*
import me.maartenmarx.api.dataApi
import me.maartenmarx.css.style
import me.maartenmarx.pages.main

// Configure routes
fun Application.configureRouting() {
    routing {
        get("/") {
            main()
        }

        get("/style.css") {
            style()
        }

        dataApi()
    }
}
