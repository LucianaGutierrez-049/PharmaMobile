package pe.edu.upeu.pharmamobilee.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pe.edu.upeu.pharmamobilee.domain.model.Producto

interface ProductoRepository {
    fun observar(): Flow<List<Producto>> = flowOf(emptyList())
    fun observarUltimaSincronizacion(): Flow<Long?> = flowOf(null)
    suspend fun sincronizar(): Result<Unit> = listar().map { Unit }
    suspend fun listar(): Result<List<Producto>>
    suspend fun obtener(id: Long): Result<Producto>
    suspend fun registrar(producto: Producto): Result<Producto>
    suspend fun actualizar(producto: Producto): Result<Producto>
    suspend fun eliminar(id: Long): Result<Unit>
}
