package pe.edu.upeu.pharmamobilee.domain.repository

import pe.edu.upeu.pharmamobilee.domain.model.Producto

interface ProductoRepository {
    suspend fun listar(): Result<List<Producto>>
    suspend fun obtener(id: Long): Result<Producto>
    suspend fun registrar(producto: Producto): Result<Producto>
    suspend fun actualizar(producto: Producto): Result<Producto>
    suspend fun eliminar(id: Long): Result<Unit>
}
