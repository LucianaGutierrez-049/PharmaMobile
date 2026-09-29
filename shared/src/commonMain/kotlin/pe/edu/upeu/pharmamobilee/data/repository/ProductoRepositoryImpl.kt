package pe.edu.upeu.pharmamobilee.data.repository

import pe.edu.upeu.pharmamobilee.data.mapper.toDomain
import pe.edu.upeu.pharmamobilee.data.remote.ProductoApi
import pe.edu.upeu.pharmamobilee.data.remote.toProductoCargaException
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

/**
 * Repositorio conectado para las consultas GET. Las operaciones de escritura de
 * la Unidad 1 se conservan localmente porque esta práctica todavía no incorpora
 * POST ni PUT.
 */
class ProductoRepositoryImpl(
    private val api: ProductoApi,
    private val repositorioLocal: ProductoRepositorioEnMemoria
) : ProductoRepository {

    override suspend fun registrar(producto: Producto): Producto =
        repositorioLocal.registrar(producto)

    override suspend fun actualizar(producto: Producto): Producto =
        repositorioLocal.actualizar(producto)

    override suspend fun listar(): Result<List<Producto>> = runCatching {
        val productosRemotos = api.obtenerProductos().map { it.toDomain() }
        val productosLocales = repositorioLocal.listar().getOrThrow()
        productosLocales + productosRemotos
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it.toProductoCargaException()) }
    )
}
