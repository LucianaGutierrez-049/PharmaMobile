package pe.edu.upeu.pharmamobilee.domain.error

class ProductoCargaException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
