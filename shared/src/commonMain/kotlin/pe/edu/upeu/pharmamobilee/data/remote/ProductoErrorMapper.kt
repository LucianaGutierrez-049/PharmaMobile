package pe.edu.upeu.pharmamobilee.data.remote

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.serialization.SerializationException
import pe.edu.upeu.pharmamobilee.domain.error.ProductoCargaException

fun Throwable.toProductoCargaException(): ProductoCargaException {
    val mensaje = when (this) {
        is HttpRequestTimeoutException ->
            "La conexión tardó demasiado. Verifica tu Internet e inténtalo nuevamente."

        is ResponseException ->
            "El servidor respondió con el código ${response.status.value}. Inténtalo nuevamente."

        is SerializationException ->
            "No se pudo interpretar la respuesta del servidor."

        else ->
            "No se pudo conectar con el servicio. Verifica tu conexión a Internet."
    }
    return ProductoCargaException(mensaje, this)
}
