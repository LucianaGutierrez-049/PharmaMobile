package pe.edu.upeu.pharmamobilee.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobilee.data.local.ProductoLocal
import pe.edu.upeu.pharmamobilee.db.PharmaDb
import pe.edu.upeu.pharmamobilee.domain.error.*
import pe.edu.upeu.pharmamobilee.domain.model.*
import pe.edu.upeu.pharmamobilee.domain.port.*
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import kotlin.test.*

class IntegradorCorreccionesTest {
    private fun driver() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { PharmaDb.Schema.create(it) }
    private fun producto() = Producto(0, "Producto válido", 4.5, 10, categoriaId = 47, categoria = "Categoría real")

    @Test fun categoriaYOrigenSobrevivenAlGuardarLeerYEditar() = runTest {
        val d = driver()
        try {
            val local = ProductoLocal(d, "A")
            local.guardarSincronizado(producto().copy(id = 8))
            val leido = local.observar().first().single()
            assertEquals(47L, leido.categoriaId)
            assertEquals("Categoría real", leido.categoria)
            assertEquals(OrigenProducto.REMOTO, leido.origen)
            val fila = local.guardarPendiente(producto())
            local.actualizarPendiente(fila.copy(categoriaId = 99, categoria = "Otra"))
            assertEquals(99L, local.pendientes().single().categoriaId)
        } finally { d.close() }
    }

    @Test fun recuperacionExplicitaConservaHistorialYNoAutorizaEnvio() = runTest {
        val d = driver()
        try {
            val legado = ProductoLocal(d, "__legado_sin_asignar__")
            val fila = legado.guardarPendiente(producto())
            legado.registrarFallo(fila.id, "Fallo anterior", true, 9000L)
            val actual = ProductoLocal(d, "A")
            actual.guardarPendiente(producto().copy(nombre = "Actual"))
            actual.recuperarPendientesAnteriores()
            val recuperado = actual.pendientes().first { it.ultimoError == "Fallo anterior" }
            assertEquals(1, recuperado.intentos)
            assertEquals(9000L, recuperado.proximoIntentoMillis)
            assertTrue(recuperado.requiereRevision)
            assertEquals(2, actual.pendientes().map { it.id }.toSet().size)
            assertTrue(legado.pendientes().isEmpty())
        } finally { d.close() }
    }

    @Test fun mismaIdEnDosCuentasNoMezclaFilasCategoriasNiFechas() = runTest {
        val d = driver()
        try {
            val a = ProductoLocal(d, "A"); val b = ProductoLocal(d, "B")
            a.guardarSincronizado(producto().copy(id = 1, nombre = "De A"))
            b.guardarSincronizado(producto().copy(id = 1, nombre = "De B"))
            a.guardarCategorias(listOf(Categoria(47, "Solo A")))
            a.guardarUltimaSincronizacion(123L)
            b.reemplazarSincronizados(listOf(producto().copy(id = 2, nombre = "Nueva B")))
            assertEquals("De A", a.observar().first().single().nombre)
            assertEquals("Nueva B", b.observar().first().single().nombre)
            assertTrue(b.observarCategorias().first().isEmpty())
            assertNull(b.observarUltimaSincronizacion().first())
            assertEquals(123L, a.observarUltimaSincronizacion().first())
        } finally { d.close() }
    }

    @Test fun cambiarCuentaNoEnviaPendientesDeLaAnterior() = runTest {
        val d = driver()
        try {
            val local = ProductoLocal(d)
            local.paraUsuario("A").guardarPendiente(producto())
            val remoto = Remoto()
            var usuario = "B"
            val repo = ProductoRepositorioPorUsuario(local, remoto, TelemetriaDesactivada) { usuario }
            repo.sincronizar().getOrThrow()
            assertEquals(0, remoto.envios)
            assertTrue(repo.observar().first().isEmpty())
            usuario = "A"
            assertEquals(1, repo.observar().first().size)
            assertEquals(0, remoto.envios)
        } finally { d.close() }
    }

    @Test fun validacionDevuelveCamposYConservaUnSoloBorradorEditable() = runTest {
        val d = driver()
        try {
            val local = ProductoLocal(d)
            val remoto = Remoto(ErrorApi.Validacion(mapOf("categoriaId" to "Categoría inválida")))
            val repo = ProductoRepositorioOffline(local, remoto)
            val error = assertIs<ProductoPendienteException>(repo.registrar(producto()).exceptionOrNull())
            assertIs<ErrorApi.Validacion>(assertIs<ErrorApiException>(error.fallo).error)
            repo.actualizar(error.producto.copy(categoriaId = 48)).getOrThrow()
            assertEquals(1, local.pendientes().size)
            assertEquals(48L, local.pendientes().single().categoriaId)
            assertEquals(1, remoto.envios)
        } finally { d.close() }
    }

    @Test fun sesionVencidaSePropagaConElProductoConservado() = runTest {
        val d = driver()
        try {
            val local = ProductoLocal(d)
            val remoto = Remoto(ErrorApi.SesionExpirada)
            val repo = ProductoRepositorioOffline(local, remoto)
            val error = assertIs<ProductoPendienteException>(repo.registrar(producto()).exceptionOrNull())
            assertEquals(ErrorApi.SesionExpirada, assertIs<ErrorApiException>(error.fallo).error)
            assertEquals(1, local.pendientes().size)
            assertEquals(0, remoto.descargas)
        } finally { d.close() }
    }

    @Test fun cincoFallosQuedanBloqueadosYUnSyncNoAgregaIntentos() = runTest {
        val d = driver()
        try {
            val local = ProductoLocal(d)
            var ahora = 0L
            val remoto = Remoto(ErrorApi.NoEncontrado)
            val repo = ProductoRepositorioOffline(local, remoto, ahoraMillis = { ahora })
            repo.registrar(producto())
            repeat(4) { ahora = local.pendientes().single().proximoIntentoMillis; repo.reintentar(-1) }
            assertEquals(5, local.pendientes().single().intentos)
            repo.sincronizar()
            assertEquals(5, remoto.envios)
            assertTrue(local.pendientes().single().requiereRevision)
        } finally { d.close() }
    }

    @Test fun cancelacionDurantePostConservaMarcaDeRevision() = runTest {
        val d = driver()
        try {
            val local = ProductoLocal(d)
            val remoto = object : Remoto() {
                override suspend fun registrar(producto: Producto): Result<Producto> = throw CancellationException()
            }
            assertFailsWith<CancellationException> { ProductoRepositorioOffline(local, remoto).registrar(producto()) }
            assertTrue(local.pendientes().single().requiereRevision)
        } finally { d.close() }
    }

    @Test fun sincronizacionesConcurrentesNoRepitenElPost() = runTest {
        val d = driver()
        try {
            val local = ProductoLocal(d)
            local.guardarPendiente(producto())
            val remoto = Remoto()
            val repo = ProductoRepositorioOffline(local, remoto)
            val a = async { repo.sincronizar() }; val b = async { repo.sincronizar() }
            a.await().getOrThrow(); b.await().getOrThrow()
            assertEquals(1, remoto.envios)
            assertTrue(local.pendientes().isEmpty())
        } finally { d.close() }
    }

    @Test fun falloDeDescargaSeRegistraEnTelemetriaUnaVez() = runTest {
        val d = driver()
        try {
            var errores = 0
            val telemetria = object : Telemetria {
                override fun registrarError(error: Throwable, contexto: Map<String, String>) { errores++ }
                override fun registrarEvento(nombre: String, datos: Map<String, String>) = Unit
            }
            val remoto = object : Remoto() {
                override suspend fun listar() = Result.failure<List<Producto>>(ErrorApiException(ErrorApi.Servidor))
            }
            assertTrue(ProductoRepositorioOffline(ProductoLocal(d), remoto, telemetria).sincronizar().isFailure)
            assertEquals(1, errores)
        } finally { d.close() }
    }

    private open class Remoto(private val fallo: ErrorApi? = null) : ProductoRepository {
        var envios = 0; var descargas = 0
        val productos = mutableListOf<Producto>()
        override suspend fun listar(): Result<List<Producto>> { descargas++; return Result.success(productos.toList()) }
        override suspend fun obtener(id: Long) = Result.success(productos.first { it.id == id })
        override suspend fun registrar(producto: Producto): Result<Producto> {
            envios++
            return fallo?.let { Result.failure(ErrorApiException(it)) }
                ?: Result.success(producto.copy(id = envios.toLong(), pendienteSincronizacion = false).also { productos += it })
        }
        override suspend fun actualizar(producto: Producto) = Result.success(producto)
        override suspend fun eliminar(id: Long) = Result.success(Unit)
    }
}
