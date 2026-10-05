package pe.edu.upeu.pharmamobilee.domain.usecase

import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

class RegistrarProductoUseCase(
    private val productoRepository: ProductoRepository
) {
    suspend operator fun invoke(
        nombre: String,
        precio: String,
        stock: String,
        activo: Boolean
    ): Result<Producto> {
        val errores = validarDatosProducto(nombre, precio, stock)

        if (errores.tieneErrores) {
            return Result.failure(errores.toException())
        }

        val producto = Producto(
            id = 0L,
            nombre = nombre.trim(),
            precio = precio.toDouble(),
            stock = stock.toInt(),
            activo = activo
        )

        return productoRepository.registrar(producto)
    }
}

internal data class ProductoValidacion(
    val nombreError: String?,
    val precioError: String?,
    val stockError: String?
) {
    val tieneErrores: Boolean
        get() = nombreError != null || precioError != null || stockError != null

    fun toException() = ProductoRegistroException(
        nombreError = nombreError,
        precioError = precioError,
        stockError = stockError
    )
}

internal fun validarDatosProducto(
    nombre: String,
    precio: String,
    stock: String
): ProductoValidacion {
    val precioValor = precio.toDoubleOrNull()
    val stockValor = stock.toIntOrNull()
    return ProductoValidacion(
        nombreError = if (nombre.isBlank()) "El nombre es obligatorio" else null,
        precioError = when {
            precio.isBlank() -> "El precio es obligatorio"
            precioValor == null || !precioValor.isFinite() -> "El precio debe ser un número válido"
            precioValor <= 0 -> "El precio debe ser mayor a 0"
            else -> null
        },
        stockError = when {
            stock.isBlank() -> "El stock es obligatorio"
            stockValor == null -> "El stock debe ser un número entero"
            stockValor < 0 -> "El stock no puede ser negativo"
            else -> null
        }
    )
}

class ProductoRegistroException(
    val nombreError: String?,
    val precioError: String?,
    val stockError: String?
) : IllegalArgumentException("El producto no cumple las reglas de registro")
