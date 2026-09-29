package pe.edu.upeu.pharmamobilee.domain.repository

import pe.edu.upeu.pharmamobilee.domain.model.Producto

interface ProductoRepository {
    suspend fun registrar(producto: Producto): Producto
    suspend fun actualizar(producto: Producto): Producto
    suspend fun listar(): Result<List<Producto>>
}
