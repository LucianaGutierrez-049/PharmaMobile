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
        val nombreError = validarNombre(nombre)
        val precioError = validarPrecio(precio)
        val stockError = validarStock(stock)

        if (nombreError != null || precioError != null || stockError != null) {
            return Result.failure(
                ProductoRegistroException(
                    nombreError = nombreError,
                    precioError = precioError,
                    stockError = stockError
                )
            )
        }

        val producto = Producto(
            id = 0L,
            nombre = nombre.trim(),
            precio = precio.toDouble(),
            stock = stock.toInt(),
            activo = activo
        )

        return Result.success(productoRepository.registrar(producto))
    }

    private fun validarNombre(nombre: String): String? {
        return if (nombre.isBlank()) "El nombre es obligatorio" else null
    }

    private fun validarPrecio(precio: String): String? {
        val precioValor = precio.toDoubleOrNull()
        return when {
            precio.isBlank() -> "El precio es obligatorio"
            precioValor == null || !precioValor.isFinite() -> "El precio debe ser un numero valido"
            precioValor <= 0 -> "El precio debe ser mayor a 0"
            else -> null
        }
    }

    private fun validarStock(stock: String): String? {
        val stockValor = stock.toIntOrNull()
        return when {
            stock.isBlank() -> "El stock es obligatorio"
            stockValor == null -> "El stock debe ser un numero entero"
            stockValor < 0 -> "El stock no puede ser negativo"
            else -> null
        }
    }
}

class ProductoRegistroException(
    val nombreError: String?,
    val precioError: String?,
    val stockError: String?
) : IllegalArgumentException("El producto no cumple las reglas de registro")
