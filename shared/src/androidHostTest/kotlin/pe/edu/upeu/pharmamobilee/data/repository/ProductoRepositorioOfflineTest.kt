package pe.edu.upeu.pharmamobilee.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobilee.data.local.ProductoLocal
import pe.edu.upeu.pharmamobilee.data.remote.ProductoApi
import pe.edu.upeu.pharmamobilee.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobilee.db.PharmaDb
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductoRepositorioOfflineTest {
    @Test
    fun migracionConservaFilasExistentesYAgregaEstadoDeEnvio() = runTest {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(null, "CREATE TABLE producto (id INTEGER NOT NULL PRIMARY KEY, nombre TEXT NOT NULL, precio REAL NOT NULL, stock INTEGER NOT NULL, activo INTEGER NOT NULL DEFAULT 1, pendiente INTEGER NOT NULL DEFAULT 0)", 0)
        driver.execute(null, "CREATE TABLE sincronizacion_metadata (id INTEGER NOT NULL PRIMARY KEY, ultima_correcta INTEGER)", 0)
        driver.execute(null, "INSERT INTO producto VALUES(-1, 'Antes de migrar', 4.5, 20, 1, 1)", 0)
        PharmaDb.Schema.migrate(driver, 2, 4)
        val fila = ProductoLocal(driver, "__legado_sin_asignar__").pendientes().single()
        assertEquals("Antes de migrar", fila.nombre)
        assertEquals(0, fila.intentos)
        assertFalse(fila.requiereRevision)
        assertEquals(null, fila.ultimoError)
        assertTrue(ProductoLocal(driver, "cuenta_nueva").pendientes().isEmpty())
        assertEquals(1L, ProductoLocal(driver).observarPendientesAnteriores().first())
        driver.close()
    }

    @Test
    fun interrupcionAntesDeConfirmarNoAutorizaReenvioAutomatico() = runTest {
        val driver = baseEnMemoria()
        val local = ProductoLocal(driver)
        val fila = local.guardarPendiente(producto(0, "Enviado"))
        local.prepararEnvio(fila.id)
        val reabierto = ProductoLocal(driver)
        val recuperado = reabierto.pendientes().single()
        assertTrue(recuperado.requiereRevision)
        assertFalse(pe.edu.upeu.pharmamobilee.domain.model.PoliticaSincronizacion.puedeEnviar(recuperado, Long.MAX_VALUE))
        driver.close()
    }

    @Test
    fun sincronizacionConRedGuardaServidorYEmiteListaCompleta() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val servidor = ServidorMock(listOf(producto(10, "Paracetamol"), producto(20, "Ibuprofeno")))
        val instante = 1_700_000_000_000L
        val repo = ProductoRepositorioOffline(local, servidor.repositorio()) { instante }

        repo.sincronizar().getOrThrow()

        assertEquals(setOf(10L, 20L), repo.observar().first().map { it.id }.toSet())
        assertEquals(instante, repo.observarUltimaSincronizacion().first())
        servidor.close()
    }

    @Test
    fun registroSinRedQuedaPendienteYElFlowLoEmite() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val servidor = ServidorMock(disponible = false)
        val repo = ProductoRepositorioOffline(local, servidor.repositorio())

        val creado = repo.registrar(producto(0, "Producto offline")).getOrThrow()
        val emitido = repo.observar().first().single()

        assertTrue(creado.id < 0)
        assertTrue(emitido.pendienteSincronizacion)
        assertEquals("Producto offline", emitido.nombre)
        assertEquals(1, local.pendientes().size)
        servidor.close()
    }

    @Test
    fun envioAlReconectarQuitaMarcaPendiente() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val servidor = ServidorMock(disponible = false)
        var ahora = 0L
        val repo = ProductoRepositorioOffline(local, servidor.repositorio()) { ahora }
        repo.registrar(producto(0, "Pendiente")).getOrThrow()

        servidor.disponible = true
        ahora = 1_000L
        repo.reintentar(local.pendientes().single().id).getOrThrow()

        val sincronizado = repo.observar().first().single()
        assertTrue(sincronizado.id > 0)
        assertFalse(sincronizado.pendienteSincronizacion)
        assertTrue(local.pendientes().isEmpty())
        assertEquals("Pendiente", servidor.productos.single().nombre)
        servidor.close()
    }

    @Test
    fun falloTransitorioRecuerdaIntentoYNoRepiteCreacionAmbigua() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val servidor = ServidorMock(disponible = false)
        val repo = ProductoRepositorioOffline(local, servidor.repositorio()) { 0L }
        repo.registrar(producto(0, "Pendiente")).getOrThrow()

        val fila = local.pendientes().single()
        assertEquals(1, fila.intentos)
        assertEquals(1_000L, fila.proximoIntentoMillis)
        assertTrue(fila.requiereRevision)
        assertTrue(fila.ultimoError!!.contains("antes de reenviar"))
        servidor.disponible = true
        repo.sincronizar().getOrThrow()
        assertTrue(servidor.productos.isEmpty())
        assertEquals(1, local.pendientes().single().intentos)
        servidor.close()
    }

    @Test
    fun reenvioManualRespetaEsperaYCadaFalloIncrementaIntentos() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val servidor = ServidorMock(disponible = false)
        var ahora = 0L
        val repo = ProductoRepositorioOffline(local, servidor.repositorio()) { ahora }
        repo.registrar(producto(0, "Pendiente")).getOrThrow()
        val id = local.pendientes().single().id
        assertTrue(repo.reintentar(id).isFailure)
        assertEquals(1, local.pendientes().single().intentos)
        ahora = 1_000L
        repo.reintentar(id)
        val fila = local.pendientes().single()
        assertEquals(2, fila.intentos)
        assertEquals(3_000L, fila.proximoIntentoMillis)
        servidor.close()
    }

    @Test
    fun editarUnaCreacionAmbiguaNoBorraSuHistorialNiAutorizaReenvio() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val fila = local.guardarPendiente(producto(0, "Pendiente"))
        local.registrarFallo(fila.id, "Conflicto", true, 1_000L)
        local.actualizarPendiente(fila.copy(nombre = "Corregido"))
        val actualizado = local.pendientes().single()
        assertEquals("Corregido", actualizado.nombre)
        assertEquals(1, actualizado.intentos)
        assertTrue(actualizado.requiereRevision)
        assertEquals("Conflicto", actualizado.ultimoError)
    }

    @Test
    fun falloPermanenteNoSeReintentaYNoImpideEnviarOtrasFilas() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val fila = local.guardarPendiente(producto(0, "Inválido"))
        val otra = local.guardarPendiente(producto(0, "Válido"))
        val servidor = object : pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository {
            override suspend fun listar() = Result.success(listOf(producto(10, "Válido")))
            override suspend fun obtener(id: Long) = Result.success(producto(id, "Válido"))
            override suspend fun registrar(producto: Producto): Result<Producto> = if (producto.id == fila.id)
                Result.failure(pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException(
                    pe.edu.upeu.pharmamobilee.domain.error.ErrorApi.Validacion(mapOf("nombre" to "Inválido"))))
                else Result.success(producto.copy(id = 10, pendienteSincronizacion = false))
            override suspend fun actualizar(producto: Producto) = Result.success(producto)
            override suspend fun eliminar(id: Long) = Result.failure<Unit>(IllegalStateException("Rechazado"))
        }
        val repo = ProductoRepositorioOffline(local, servidor) { 0L }
        assertTrue(repo.sincronizar().isFailure)
        assertEquals(fila.id, local.pendientes().single().id)
        assertTrue(local.pendientes().single().requiereRevision)
        assertTrue(local.observar().first().any { it.id == 10L })
        assertFalse(local.observar().first().any { it.id == otra.id })
        assertTrue(repo.eliminar(10L).isFailure)
        assertTrue(local.observar().first().any { it.id == 10L })
    }

    @Test
    fun bajadaDelServidorNoBorraProductoPendiente() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val pendiente = local.guardarPendiente(producto(0, "Creado sin red"))

        local.reemplazarSincronizados(listOf(producto(50, "Producto servidor")))

        val productos = local.observar().first()
        assertEquals(setOf(pendiente.id, 50L), productos.map { it.id }.toSet())
        assertTrue(productos.first { it.id == pendiente.id }.pendienteSincronizacion)
    }

    @Test
    fun identificadoresLocalesYRemotosNoColisionan() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val localA = local.guardarPendiente(producto(0, "Local A"))
        val localB = local.guardarPendiente(producto(0, "Local B"))

        local.reemplazarSincronizados(listOf(producto(1, "Servidor")))

        val ids = local.observar().first().map { it.id }
        assertEquals(3, ids.toSet().size)
        assertTrue(localA.id < 0 && localB.id < localA.id)
        assertTrue(1L in ids)
    }

    @Test
    fun sincronizarDosVecesNoDuplicaFilas() = runTest {
        val local = ProductoLocal(baseEnMemoria())
        val servidor = ServidorMock(listOf(producto(7, "A"), producto(8, "B")))
        val repo = ProductoRepositorioOffline(local, servidor.repositorio())

        repo.sincronizar().getOrThrow()
        val primera = repo.observar().first()
        repo.sincronizar().getOrThrow()
        val segunda = repo.observar().first()

        assertEquals(2, segunda.size)
        assertEquals(primera, segunda)
        servidor.close()
    }

    private fun baseEnMemoria(): app.cash.sqldelight.db.SqlDriver =
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { PharmaDb.Schema.create(it) }

    private fun producto(id: Long, nombre: String) = Producto(
        id = id,
        nombre = nombre,
        precio = 4.5,
        stock = 20,
        origen = OrigenProducto.REMOTO
    )

    private inner class ServidorMock(
        productosIniciales: List<Producto> = emptyList(),
        var disponible: Boolean = true
    ) {
        val productos = productosIniciales.toMutableList()
        private val cliente = crearHttpClient(MockEngine { request ->
            if (!disponible) return@MockEngine respondError(HttpStatusCode.ServiceUnavailable)
            when (request.method) {
                HttpMethod.Post -> {
                    val nuevo = producto((productos.maxOfOrNull { it.id } ?: 0L) + 1L, "Pendiente")
                    productos += nuevo
                    respond(productoJson(nuevo), HttpStatusCode.Created, cabeceraJson)
                }
                HttpMethod.Get -> respond(paginaJson(productos), HttpStatusCode.OK, cabeceraJson)
                else -> respondError(HttpStatusCode.MethodNotAllowed)
            }
        }, "http://localhost/api/v1/")

        fun repositorio() = ProductoRepositoryImpl(ProductoApi(cliente), 1L)
        fun close() = cliente.close()
    }

    private val cabeceraJson = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun productoJson(producto: Producto) =
        """{"id":${producto.id},"nombre":"${producto.nombre}","precio":${producto.precio},"stock":${producto.stock},"estado":true}"""

    private fun paginaJson(productos: List<Producto>): String =
        """{"contenido":[${productos.joinToString { productoJson(it) }}],"pagina":0,"tamanio":20,"totalElementos":${productos.size},"totalPaginas":1,"ultima":true}"""
}
