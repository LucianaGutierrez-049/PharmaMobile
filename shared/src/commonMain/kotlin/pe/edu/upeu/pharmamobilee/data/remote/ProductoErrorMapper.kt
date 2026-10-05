package pe.edu.upeu.pharmamobilee.data.remote

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerializationException
import pe.edu.upeu.pharmamobilee.domain.error.ProductoCargaException

fun Throwable.toProductoCargaException(): ProductoCargaException {
    val mensaje = when {
        encontrarCausa<HttpRequestTimeoutException>() != null ->
            "La conexión tardó demasiado. Verifica tu Internet e inténtalo nuevamente."

        this is ClientRequestException && response.status == HttpStatusCode.NotFound ->
            "No se encontró el producto solicitado."

        this is ResponseException ->
            "El servidor respondió con el código ${response.status.value}. Inténtalo nuevamente."

        encontrarCausa<SerializationException>() != null ->
            "No se pudo interpretar la respuesta del servidor."

        else ->
            "No se pudo conectar con el servicio. Verifica tu conexión a Internet."
    }
    return ProductoCargaException(mensaje, this)
}

private inline fun <reified T : Throwable> Throwable.encontrarCausa(): T? {
    var actual: Throwable? = this
    while (actual != null) {
        if (actual is T) return actual
        actual = actual.cause
    }
    return null
}
