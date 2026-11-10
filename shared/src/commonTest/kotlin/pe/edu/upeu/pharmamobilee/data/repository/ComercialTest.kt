package pe.edu.upeu.pharmamobilee.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobilee.data.remote.ComercialApi
import pe.edu.upeu.pharmamobilee.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobilee.domain.model.Cliente
import pe.edu.upeu.pharmamobilee.domain.model.LineaVenta
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.model.Venta
import pe.edu.upeu.pharmamobilee.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.domain.repository.VentaRepository
import pe.edu.upeu.pharmamobilee.presentacion.cliente.ClienteValidator
import pe.edu.upeu.pharmamobilee.presentacion.pedido.VentaViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ComercialTest {

    @Test
    fun validadorAplicaLasReglasDePharmaSoft() {
        assertNotNull(ClienteValidator.validarDni("1234567"))
        assertNull(ClienteValidator.validarDni("12345678"))
        assertNotNull(ClienteValidator.validarTelefono("98765432"))
        assertNull(ClienteValidator.validarTelefono(""))
        assertNull(ClienteValidator.validarTelefono("987654321"))
        assertNotNull(ClienteValidator.validarNombre("A"))
        assertNotNull(ClienteValidator.validarCorreo("sin-arroba"))
        assertNull(ClienteValidator.validarCorreo("ana@pharma.pe"))
    }

    @Test
    fun lineasRepetidasSeAgrupanPorProducto() {
        val p = Producto(5, "Paracetamol", 2.5, stock = 10)
        val q = Producto(6, "Ibuprofeno", 3.0, stock = 10)
        val detalles = agruparLineas(listOf(LineaVenta(p, 2), LineaVenta(q, 1), LineaVenta(p, 3)))
        assertEquals(listOf(5L to 5, 6L to 1), detalles.map { it.productoId to it.cantidad })
    }

    @Test
    fun listarClientesRecorreTodasLasPaginas() = runTest {
        var llamadas = 0
        val engine = MockEngine { req ->
            val pagina = req.url.parameters["pagina"]!!.toInt()
            llamadas++
            respond(
                """{"contenido":[{"id":${pagina + 1},"dni":"1234567$pagina","nombres":"N$pagina","apellidos":"A","email":"c$pagina@x.pe","estado":true}],
                   "pagina":$pagina,"tamanio":1,"totalElementos":2,"totalPaginas":2,"ultima":${pagina == 1}}""",
                HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val repo = ClienteRepositorioRest(ComercialApi(crearHttpClient(engine, "http://x/api/v1/")))
        val clientes = repo.listar().getOrThrow()
        assertEquals(2, llamadas)
        assertEquals(listOf("N0 A", "N1 A"), clientes.map { it.nombreCompleto })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun carritoNoSuperaElStockYExigeCliente() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val producto = Producto(5, "Paracetamol", 2.5, stock = 3)
            var registrada: List<LineaVenta>? = null
            val vm = VentaViewModel(
                ventas = object : VentaRepository {
                    override suspend fun listar() = Result.success(emptyList<Venta>())
                    override suspend fun registrar(clienteId: Long, lineas: List<LineaVenta>): Result<Venta> {
                        registrada = lineas
                        return Result.success(Venta(9, "2026-11-10T10:00:00", clienteId, "Ana", "REGISTRADA", 5.0, emptyList()))
                    }
                },
                clientes = object : ClienteRepository {
                    override suspend fun listar() = Result.success(listOf(Cliente(1, "12345678", "Ana", "Paz", "a@x.pe")))
                    override suspend fun registrar(cliente: Cliente) = Result.failure<Cliente>(ErrorApiException(ErrorApi.Servidor))
                    override suspend fun actualizar(cliente: Cliente) = Result.failure<Cliente>(ErrorApiException(ErrorApi.Servidor))
                    override suspend fun darDeBaja(id: Long) = Result.success(Unit)
                },
                productos = object : ProductoRepository {
                    override fun observar(): Flow<List<Producto>> = flowOf(listOf(producto, Producto(-1, "Pendiente", 1.0, stock = 9)))
                    override suspend fun listar() = Result.success(listOf(producto))
                    override suspend fun obtener(id: Long) = Result.success(producto)
                    override suspend fun registrar(producto: Producto) = Result.success(producto)
                    override suspend fun actualizar(producto: Producto) = Result.success(producto)
                    override suspend fun eliminar(id: Long) = Result.success(Unit)
                }
            )
            advanceUntilIdle()
            assertEquals(listOf(5L), vm.uiState.value.productos.map { it.id }) // sin pendientes locales
            vm.seleccionarProducto(5); vm.cambiarCantidad("4"); vm.agregar()
            assertTrue(vm.uiState.value.carrito.isEmpty())
            assertNotNull(vm.uiState.value.errorCarrito)
            vm.cambiarCantidad("2"); vm.seleccionarProducto(5); vm.agregar()
            vm.registrar()
            assertEquals("Selecciona un cliente.", vm.uiState.value.errorCarrito)
            vm.seleccionarCliente(1); vm.registrar(); advanceUntilIdle()
            assertEquals(2, registrada!!.single().cantidad)
            assertTrue(vm.uiState.value.carrito.isEmpty())
        } finally { Dispatchers.resetMain() }
    }
}
