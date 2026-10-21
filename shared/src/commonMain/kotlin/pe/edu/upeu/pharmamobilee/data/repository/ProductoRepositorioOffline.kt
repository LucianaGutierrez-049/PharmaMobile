package pe.edu.upeu.pharmamobilee.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import pe.edu.upeu.pharmamobilee.data.local.ProductoLocal
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoRepository,
    private val ahoraMillis: () -> Long = { kotlin.time.Clock.System.now().toEpochMilliseconds() }
) : ProductoRepository {
    override fun observar(): Flow<List<Producto>> = local.observar()
    override fun observarUltimaSincronizacion(): Flow<Long?> = local.observarUltimaSincronizacion()

    override suspend fun sincronizar(): Result<Unit> = runCatching {
        local.pendientes().forEach { pendiente ->
            val sincronizado = remoto.registrar(pendiente).getOrThrow()
            local.borrarPendiente(pendiente.id)
            local.guardarSincronizado(sincronizado)
        }
        local.reemplazarSincronizados(remoto.listar().getOrThrow())
        local.guardarUltimaSincronizacion(ahoraMillis())
    }

    override suspend fun listar(): Result<List<Producto>> = runCatching { local.observar().first() }

    override suspend fun obtener(id: Long): Result<Producto> = runCatching {
        local.observar().first().first { it.id == id }
    }

    override suspend fun registrar(producto: Producto): Result<Producto> = runCatching {
        val guardado = local.guardarPendiente(producto)
        sincronizar()
        guardado
    }

    override suspend fun actualizar(producto: Producto): Result<Producto> = runCatching {
        if (producto.id < 0) {
            local.actualizarPendiente(producto)
            producto
        } else {
            remoto.actualizar(producto).getOrThrow().also(local::guardarSincronizado)
        }
    }

    override suspend fun eliminar(id: Long): Result<Unit> = runCatching {
        local.eliminar(id)
        if (id >= 0) remoto.eliminar(id).getOrThrow()
    }
}
