package me.maartenmarx.api

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

// URL obfuscated in source code
val webhookUrl: String = System.getenv("webhook-url")

// Set up API to post weather data to
fun Routing.dataApi() {
    // Create HTTP client to post to Discord API
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    // POST endpoint
    post("/data") {
        // Get data from request body
        val data = call.receive<Data>()
        transaction {
            // Insert data into database
            WeatherData.insert {
                it[temperature] = data.temperature
                it[pressure] = data.pressure
                it[lightLevel] = data.lightLevel
                it[time] = Instant.now()
            }
        }

        // Post a Discord webhook message
        client.request(webhookUrl) {
            method = HttpMethod.Post
            headers {
                set("Content-Type", "application/json")
            }
            setBody(weatherMessage(data))
        }
    }
}

// Request data format
@Serializable
data class Data(val temperature: Float, val pressure: Float, val lightLevel: Float)

// Database table schema
object WeatherData : IntIdTable() {
    val temperature = float("temperature")
    val pressure = float("pressure")
    val lightLevel = float("light_level")
    val time = timestamp("time")
}

// Record format
class WeatherMeasurement(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<WeatherMeasurement>(WeatherData)

    var temperature by WeatherData.temperature
    var pressure by WeatherData.pressure
    var lightLevel by WeatherData.lightLevel
    var time by WeatherData.time
}

// Webhook request format
@Serializable
data class DiscordWebhookMessage(val content: String, val embeds: List<DiscordEmbed>)

@Serializable
data class DiscordEmbed(val title: String, val description: String, val color: Int, val timestamp: String)

// Generates data for Discord webhook
fun weatherMessage(data: Data) = DiscordWebhookMessage(
    "", listOf(
        DiscordEmbed(
            "Weather Report",
            """
                **Temperature:** ${data.temperature} °C
                **Air Pressure:** ${data.pressure} Pa
                **Light Level:** ${data.lightLevel} Lux
            """.trimIndent(),
            0x57965c,
            Instant.now().toString()
        )
    )
)
