package me.maartenmarx.css

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import kotlinx.css.*

suspend fun PipelineContext<*, ApplicationCall>.style() = call.respondCss {
    // Generate CSS
    body {
        margin(0.px)
    }
    h1 {
        fontFamily = "Roboto Slab"
        textAlign = TextAlign.center
        color = Color("#478")
        fontWeight = FontWeight("600")
        fontSize = 3.5.rem
    }
    h2 {
        fontFamily = "Roboto Slab"
        color = Color("#478")
        fontWeight = FontWeight("600")
        fontSize = 2.rem
    }
    p {
        fontFamily = "Lexend"
    }
    rule(".graph-container") {
        display = Display.flex
        alignItems = Align.center
        justifyContent = JustifyContent.center
        flexWrap = FlexWrap.wrap
        gap = Gap("5rem")
    }
    rule(".graph-container div") {
        flex(0.0, 0.0, 800.px)
        height = 300.px
        marginBottom = 50.px
    }
    rule("#temperature polygon") {
        declarations["fill"] = "#f88b"
    }
    rule("#temperature path, #temperature circle") {
        declarations["stroke"] = "#c55"
    }
    rule("#temperature line") {
        declarations["stroke"] = "#b33"
    }
    rule("#temperature text") {
        declarations["fill"] = "#b33"
    }
    rule("#pressure polygon") {
        declarations["fill"] = "#dafb"
    }
    rule("#pressure path, #pressure circle") {
        declarations["stroke"] = "#85a"
    }
    rule("#pressure line") {
        declarations["stroke"] = "#96a"
    }
    rule("#pressure text") {
        declarations["fill"] = "#96a"
    }
    rule("#light-level polygon") {
        declarations["fill"] = "#adfb"
    }
    rule("#light-level path, #light-level circle") {
        declarations["stroke"] = "#58a"
    }
    rule("#light-level line") {
        declarations["stroke"] = "#69a"
    }
    rule("#light-level text") {
        declarations["fill"] = "#69a"
    }
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
