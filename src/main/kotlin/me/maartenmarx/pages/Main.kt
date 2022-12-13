package me.maartenmarx.pages

import com.github.nwillc.ksvg.elements.SVG
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import me.maartenmarx.api.WeatherData
import me.maartenmarx.api.WeatherMeasurement
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

suspend fun PipelineContext<*, ApplicationCall>.main() = call.respondHtml {
    // Generate HTML
    head {
        // Import fonts and CSS
        link("https://fonts.googleapis.com", "preconnect")
        link("https://fonts.gstatic.com", "preconnect")
        link(
            "https://fonts.googleapis.com/css2?family=Lexend&family=Roboto+Slab:wght@400;600&display=swap",
            "stylesheet"
        )
        link("/style.css", "stylesheet")
    }
    body {
        // Generate page content
        h1 { +"Weather Reports" }
        div("graph-container") {
            // Draw graphs
            getData().let { data ->
                graph(
                    "temperature",
                    "Graph 1: Temperature",
                    "Temperature (°C) in function of time.",
                    data
                ) { temperature }
                graph(
                    "pressure",
                    "Graph 2: Air Pressure",
                    "Air pressure (Pa) in function of time.",
                    data
                ) { pressure }
                graph(
                    "light-level",
                    "Graph 3: Light Level",
                    "Light level (Lux) in function of time.",
                    data
                ) { lightLevel }
            }
        }
    }
}

// Fetches the data from the database
fun getData() = transaction {
    WeatherData
        .selectAll()
        .orderBy(WeatherData.time)
        .map { WeatherMeasurement.wrapRow(it) }
        .toList()
}

// Generates an SVG-based graph based on the provided data
fun DIV.graph(
    id: String,
    title: String,
    description: String,
    data: List<WeatherMeasurement>,
    key: WeatherMeasurement.() -> Float
) = div {
    h2 { +title }
    p { +description }
    unsafe {
        raw(SVG.svg {
            val w = 400
            val h = 150

            this.id = id
            width = "100%"
            height = "100%"
            viewBox = "0 0 $w $h"

            val minValue = floor(data.minOf { it.key() }).roundToInt()
            val maxValue = ceil(data.maxOf { it.key() }).roundToInt()

            // Top label
            text {
                body = maxValue.toString()
                x = (5 * (4 - body.length)).toString()
                y = "10"
                fontFamily = "Lexend"
                fontSize = "10"
            }

            // Bottom label
            text {
                body = minValue.toString()
                x = (5 * (4 - body.length)).toString()
                y = h.toString()
                fontFamily = "Lexend"
                fontSize = "10"
            }

            // Get points from data
            val points = points(data, key, w.toFloat(), h.toFloat())

            // Color under graph
            polygon {
                this.points = "${w - 5},${h - 5} 25,${h - 5} " + points.joinToString(" ") { "${it.first},${it.second}" }
                fill = "#0005"
            }

            // Y axis
            line {
                x1 = "25"
                x2 = x1
                y1 = "5"
                y2 = (h - 5).toString()
                stroke = "#666"
            }

            // X axis
            line {
                x1 = "25"
                x2 = (w - 5).toString()
                y1 = (h - 5).toString()
                y2 = y1
                stroke = "#666"
            }

            // Graph
            path {
                d = "M ${points[0].first} ${points[0].second} " +
                        points.joinToString(" ") { "L ${it.first} ${it.second}" }
                stroke = "#000"
                fill = "#0000"
            }

            // Points on graph
            points.forEach { pair ->
                circle {
                    cx = "${pair.first}"
                    cy = "${pair.second}"
                    r = "2"
                    stroke = "#000"
                    fill = "#fff"
                }
            }
        }.toString())
    }
}

// Calculates point coordinates based on data
fun points(
    data: List<WeatherMeasurement>,
    key: WeatherMeasurement.() -> Float,
    width: Float,
    height: Float
): List<Pair<Float, Float>> {
    val minTime = data.minOf { it.time }.epochSecond
    val maxTime = data.maxOf { it.time }.epochSecond
    val timeSpan = maxTime - minTime

    val minValue = floor(data.minOf { it.key() })
    val maxValue = ceil(data.maxOf { it.key() })
    val valueSpan = maxValue - minValue

    return data.map {
        // Calculate coordinates
        val x = ((it.time.epochSecond - minTime) / timeSpan.toFloat()) * width
        val y = height - ((it.key() - minValue) / valueSpan) * height

        // Add padding
        Pair(x / width * (width - 30) + 25, y / height * (height - 10) + 5)
    }
}
