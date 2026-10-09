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
    urlBase: String,
    tiempoEsperaSolicitudMillis: Long = 15_000,
    ignorarCamposDesconocidos: Boolean = true
): HttpClient = HttpClient(engine) {
    expectSuccess = true

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = ignorarCamposDesconocidos
                isLenient = true
                encodeDefaults = true
            }
        )
    }

    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.ALL
    }

    install(HttpTimeout) {
        requestTimeoutMillis = tiempoEsperaSolicitudMillis
        connectTimeoutMillis = 10_000
    }

    defaultRequest {
        url(urlBase)
        contentType(ContentType.Application.Json)
    }
}
