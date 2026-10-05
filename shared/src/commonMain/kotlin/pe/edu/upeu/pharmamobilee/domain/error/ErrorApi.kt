package pe.edu.upeu.pharmamobilee.domain.error

sealed interface ErrorApi {
    data class Validacion(val porCampo: Map<String, String>) : ErrorApi
    data object NoEncontrado : ErrorApi
    data class Conflicto(val mensaje: String) : ErrorApi
    data object Servidor : ErrorApi
    data object SinConexion : ErrorApi
    data object TiempoAgotado : ErrorApi
}

class ErrorApiException(
    val error: ErrorApi,
    cause: Throwable? = null
) : Exception(error.mensajeUsuario(), cause)

fun ErrorApi?.mensajeUsuario(): String = when (this) {
    is ErrorApi.Validacion -> "Revisa los datos ingresados."
    ErrorApi.NoEncontrado -> "No se encontró el producto solicitado."
    is ErrorApi.Conflicto -> mensaje
    ErrorApi.Servidor -> "El servidor no pudo completar la operación. Inténtalo nuevamente."
    ErrorApi.SinConexion -> "No se pudo conectar con el servicio. Verifica tu conexión a Internet."
    ErrorApi.TiempoAgotado -> "La conexión tardó demasiado. Verifica tu Internet e inténtalo nuevamente."
    null -> "Ocurrió un error inesperado. Inténtalo nuevamente."
}
