package pe.edu.upeu.pharmamobilee.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguroVacio

fun crearHttpClient(
    engine: HttpClientEngine,
    urlBase: String,
    almacenSeguro: AlmacenSeguro = AlmacenSeguroVacio,
    esDepuracion: Boolean = false,
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

    install(Auth) {
        bearer {
            loadTokens {
                almacenSeguro.leerToken()?.let { BearerTokens(it, "") }
            }
            sendWithoutRequest { peticion ->
                !peticion.url.build().encodedPath.endsWith("/auth/login")
            }
        }
    }

    install(Logging) {
        logger = Logger.SIMPLE
        level = if (esDepuracion) LogLevel.HEADERS else LogLevel.NONE
        sanitizeHeader { nombre ->
            nombre.equals(HttpHeaders.Authorization, ignoreCase = true)
        }
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
