package pe.edu.upeu.pharmamobilee.domain.usecase

import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

class EliminarProductoUseCase(
    private val productoRepository: ProductoRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> =
        productoRepository.eliminar(id)
}
