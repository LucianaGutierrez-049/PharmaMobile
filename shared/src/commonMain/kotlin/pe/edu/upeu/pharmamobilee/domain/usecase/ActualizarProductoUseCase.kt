package pe.edu.upeu.pharmamobilee.domain.usecase

import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

class ActualizarProductoUseCase(
    private val productoRepository: ProductoRepository
) {
    suspend operator fun invoke(
        id: Long,
        nombre: String,
        precio: String,
        stock: String,
        activo: Boolean,
        categoriaId: Long? = null
    ): Result<Producto> {
        val errores = validarDatosProducto(nombre, precio, stock)
        if (errores.tieneErrores) {
            return Result.failure(errores.toException())
        }

        return productoRepository.actualizar(
            Producto(
                id = id,
                nombre = nombre.trim(),
                precio = precio.toDouble(),
                stock = stock.toInt(),
                activo = activo,
                categoriaId = categoriaId
            )
        )
    }
}
