package pe.edu.upeu.pharmamobilee.data.remote

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import pe.edu.upeu.pharmamobilee.data.remote.dto.ErrorResponseDto
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException

suspend fun <T> ejecutarLlamada(
    bloque: suspend () -> T
): Result<T> = try {
    Result.success(bloque())
} catch (cancelacion: CancellationException) {
    throw cancelacion
} catch (error: ClientRequestException) {
    Result.failure(ErrorApiException(traducirErrorCliente(error), error))
} catch (error: ServerResponseException) {
    Result.failure(ErrorApiException(ErrorApi.Servidor, error))
} catch (error: HttpRequestTimeoutException) {
    Result.failure(ErrorApiException(ErrorApi.TiempoAgotado, error))
} catch (error: IOException) {
    Result.failure(ErrorApiException(ErrorApi.SinConexion, error))
} catch (error: JsonConvertException) {
    Result.failure(ErrorApiException(ErrorApi.Servidor, error))
} catch (error: SerializationException) {
    Result.failure(ErrorApiException(ErrorApi.Servidor, error))
} catch (error: ResponseException) {
    Result.failure(ErrorApiException(ErrorApi.Servidor, error))
}

private suspend fun traducirErrorCliente(
    error: ClientRequestException
): ErrorApi {
    val cuerpo = runCatching {
        error.response.body<ErrorResponseDto>()
    }.getOrNull()

    return when (error.response.status.value) {
        400 -> ErrorApi.Validacion(cuerpo?.validationErrors.orEmpty())
        404 -> ErrorApi.NoEncontrado
        409 -> ErrorApi.Conflicto(
            cuerpo?.message ?: "La operación no está permitida."
        )
        else -> ErrorApi.Servidor
    }
}
