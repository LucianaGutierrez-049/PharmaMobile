package pe.edu.upeu.pharmamobilee.domain.error

sealed interface ErrorSesion {
    data object CredencialesInvalidas : ErrorSesion
    data object SesionExpirada : ErrorSesion
    data object SinPermiso : ErrorSesion
    data object SinConexion : ErrorSesion
}

class ErrorSesionException(
    val error: ErrorSesion,
    cause: Throwable? = null
) : Exception(error.mensajeUsuario(), cause)

fun ErrorSesion.mensajeUsuario(): String = when (this) {
    ErrorSesion.CredencialesInvalidas -> "Usuario o contraseña incorrectos."
    ErrorSesion.SesionExpirada -> "Tu sesión expiró. Inicia sesión nuevamente."
    ErrorSesion.SinPermiso -> "No tienes permisos para realizar esta operación."
    ErrorSesion.SinConexion -> "No se pudo conectar con PharmaSoft."
}
