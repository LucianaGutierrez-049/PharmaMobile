package pe.edu.upeu.pharmamobilee.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobilee.data.local.ProductoLocal
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobilee.domain.error.mensajeUsuario
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.model.PoliticaSincronizacion
import pe.edu.upeu.pharmamobilee.domain.port.Telemetria
import pe.edu.upeu.pharmamobilee.domain.port.TelemetriaDesactivada
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoRepository,
    private val telemetria: Telemetria = TelemetriaDesactivada,
    private val ahoraMillis: () -> Long = { kotlin.time.Clock.System.now().toEpochMilliseconds() }
) : ProductoRepository {
    private val mutex = Mutex()
    override fun observar(): Flow<List<Producto>> = local.observar()
    override fun observarUltimaSincronizacion(): Flow<Long?> = local.observarUltimaSincronizacion()

    private suspend fun <T> proteger(bloque: suspend () -> T): Result<T> = try {
        Result.success(bloque())
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun sincronizar(): Result<Unit> = proteger {
        mutex.withLock { sincronizarInterno() }
    }

    private suspend fun sincronizarInterno() {
        var primerError: Throwable? = null
        local.pendientes().filter { PoliticaSincronizacion.puedeEnviar(it, ahoraMillis()) }.forEach { pendiente ->
            // Persistir ANTES del POST: si el proceso muere, no repetir una creación ambigua.
            local.prepararEnvio(pendiente.id)
            val resultado = remoto.registrar(pendiente)
            val error = resultado.exceptionOrNull()
            if (error is CancellationException) throw error
            if (error == null) {
                local.confirmarEnvio(pendiente.id, resultado.getOrThrow())
            } else {
                val api = (error as? ErrorApiException)?.error
                val transitorio = api == ErrorApi.SinConexion || api == ErrorApi.TiempoAgotado || api == ErrorApi.Servidor
                // PharmaSoft no garantiza idempotencia del POST. El usuario confirma cada reenvío.
                val mensaje = when {
                    transitorio -> api.mensajeUsuario() + " Verifica si se creó en el servidor antes de reenviar."
                    api is ErrorApi.Validacion -> api.porCampo.entries.joinToString("; ") { "${it.key}: ${it.value}" }
                    else -> api.mensajeUsuario()
                }
                local.registrarFallo(pendiente.id, mensaje, true,
                    ahoraMillis() + PoliticaSincronizacion.esperaMillis(pendiente.intentos))
                telemetria.registrarError(error, mapOf("operacion" to "crear_producto", "tipo" to if (transitorio) "transitorio" else "permanente"))
                if (primerError == null) primerError = error
            }
        }
        val descarga = descargarConReintentos()
        descarga.exceptionOrNull()?.let { if (it is CancellationException) throw it }
        descarga.onSuccess {
            local.reemplazarSincronizados(it)
            local.guardarUltimaSincronizacion(ahoraMillis())
        }
        (primerError ?: descarga.exceptionOrNull())?.let { throw it }
        telemetria.registrarEvento("sincronizacion_completada")
    }

    private suspend fun descargarConReintentos(): Result<List<Producto>> {
        repeat(PoliticaSincronizacion.MAX_INTENTOS) { intento ->
            val resultado = remoto.listar()
            val error = resultado.exceptionOrNull()
            if (error is CancellationException) throw error
            val api = (error as? ErrorApiException)?.error
            val transitorio = api == ErrorApi.SinConexion || api == ErrorApi.TiempoAgotado || api == ErrorApi.Servidor
            if (!transitorio || intento == PoliticaSincronizacion.MAX_INTENTOS - 1) return resultado
            // GET es idempotente: aquí sí se permite el reintento automático acotado.
            delay(PoliticaSincronizacion.esperaMillis(intento))
        }
        error("Límite de intentos inválido")
    }

    override suspend fun reintentar(id: Long): Result<Unit> = proteger {
        mutex.withLock {
            val fila = local.pendientes().firstOrNull { it.id == id } ?: return@withLock
            // Mantener la espera persistida incluso cuando el reenvío lo confirma el usuario.
            if (fila.proximoIntentoMillis > ahoraMillis()) {
                throw IllegalStateException("Espera unos segundos antes de volver a intentar.")
            }
            local.reintentar(id)
            sincronizarInterno()
        }
    }

    override suspend fun listar(): Result<List<Producto>> = proteger { local.observar().first() }
    override suspend fun obtener(id: Long): Result<Producto> = proteger { local.observar().first().first { it.id == id } }

    override suspend fun registrar(producto: Producto): Result<Producto> = proteger {
        mutex.withLock {
            val guardado = local.guardarPendiente(producto)
            // Guardar localmente es suficiente; el estado observable muestra el resultado del envío.
            try { sincronizarInterno() } catch (c: CancellationException) { throw c } catch (_: Exception) { }
            guardado
        }
    }

    override suspend fun actualizar(producto: Producto): Result<Producto> = proteger {
        mutex.withLock {
            if (producto.id < 0) local.actualizarPendiente(producto)
            else remoto.actualizar(producto).getOrThrow().also(local::guardarSincronizado)
        }
    }

    override suspend fun eliminar(id: Long): Result<Unit> = proteger {
        mutex.withLock {
            // Conservar la copia local si el servidor rechaza el borrado.
            if (id >= 0) remoto.eliminar(id).getOrThrow()
            local.eliminar(id)
        }
    }
}
