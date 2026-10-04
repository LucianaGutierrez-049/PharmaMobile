package pe.edu.upeu.pharmamobilee.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun crearHttpClient(
    engine: HttpClientEngine,
    urlBase: String
): HttpClient = HttpClient(engine) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            }
        )
    }

    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.HEADERS
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
    }

    defaultRequest {
        url(urlBase)
        contentType(ContentType.Application.Json)
    }
}
