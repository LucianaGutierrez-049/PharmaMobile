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
        val repo = ProductoRepositorioOffline(local, servidor.repositorio())
        repo.registrar(producto(0, "Pendiente")).getOrThrow()

        servidor.disponible = true
        repo.sincronizar().getOrThrow()

        val sincronizado = repo.observar().first().single()
        assertTrue(sincronizado.id > 0)
        assertFalse(sincronizado.pendienteSincronizacion)
        assertTrue(local.pendientes().isEmpty())
        assertEquals("Pendiente", servidor.productos.single().nombre)
        servidor.close()
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
