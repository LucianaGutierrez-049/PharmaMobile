package pe.edu.upeu.pharmamobilee.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobilee.data.local.ProductoLocal
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.port.Telemetria
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

/** Cada operación captura su cuenta. Una respuesta tardía nunca escribe en otra cuenta. */
class ProductoRepositorioPorUsuario(
    private val local: ProductoLocal,
    private val remoto: ProductoRepository,
    private val telemetria: Telemetria,
    private val usuarioActual: () -> String?
) : ProductoRepository {
    private val mutex = Mutex()

    private fun actual(): ProductoRepositorioOffline {
        val usuario = usuarioActual()?.takeIf { it.isNotBlank() }
            ?: throw ErrorApiException(ErrorApi.SesionExpirada)
        return ProductoRepositorioOffline(local.paraUsuario(usuario), remoto, telemetria,
            mutex = mutex, validarSesion = {
                if (usuarioActual() != usuario) throw ErrorApiException(ErrorApi.SesionExpirada)
            })
    }

    override fun observar() = actual().observar()
    override fun observarCategorias() = actual().observarCategorias()
    override fun observarPendientesAnteriores() = local.observarPendientesAnteriores()
    override fun observarUltimaSincronizacion() = actual().observarUltimaSincronizacion()
    private suspend fun <T> ejecutar(accion: suspend (ProductoRepositorioOffline) -> Result<T>): Result<T> = try {
        accion(actual())
    } catch (cancelacion: CancellationException) { throw cancelacion }
    catch (error: Exception) { Result.failure(error) }

    override suspend fun listarCategorias() = ejecutar { it.listarCategorias() }
    override suspend fun recuperarPendientesAnteriores() = ejecutar { it.recuperarPendientesAnteriores() }
    override suspend fun listar() = ejecutar { it.listar() }
    override suspend fun obtener(id: Long) = ejecutar { it.obtener(id) }
    override suspend fun registrar(producto: Producto) = ejecutar { it.registrar(producto) }
    override suspend fun actualizar(producto: Producto) = ejecutar { it.actualizar(producto) }
    override suspend fun eliminar(id: Long) = ejecutar { it.eliminar(id) }
    override suspend fun sincronizar() = ejecutar { it.sincronizar() }
    override suspend fun reintentar(id: Long) = ejecutar { it.reintentar(id) }
}
