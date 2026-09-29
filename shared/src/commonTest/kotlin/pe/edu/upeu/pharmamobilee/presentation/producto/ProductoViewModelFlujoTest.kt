package pe.edu.upeu.pharmamobilee.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.RegistrarProductoUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelFlujoTest {

    @Test
    fun repositorioVacioProduceFaseSinProductos() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repositorio = ProductoRepositoryFalso(productos = emptyList())
            val viewModel = crearViewModel(repositorio)

            advanceUntilIdle()

            assertIs<ProductoFase.SinProductos>(viewModel.uiState.value.fase)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun repositorioConProductosProduceFaseConProductos() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val productos = listOf(
                Producto(id = 1L, nombre = "Paracetamol", precio = 3.5, stock = 20),
                Producto(id = 2L, nombre = "Ibuprofeno", precio = 5.0, stock = 8),
                Producto(id = 3L, nombre = "Alcohol", precio = 7.0, stock = 15)
            )
            val repositorio = ProductoRepositoryFalso(productos = productos)
            val viewModel = crearViewModel(repositorio)

            advanceUntilIdle()

            assertIs<ProductoFase.ConProductos>(viewModel.uiState.value.fase)
            assertEquals(productos, viewModel.uiState.value.productos)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun repositorioQueLanzaExcepcionProduceFaseError() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repositorio = ProductoRepositoryFalso(
                errorAlListar = IllegalStateException("Error al cargar productos")
            )
            val viewModel = crearViewModel(repositorio)

            advanceUntilIdle()

            val fase = assertIs<ProductoFase.Error>(viewModel.uiState.value.fase)
            assertEquals(
                "No se pudo conectar con el servicio. Verifica tu conexión a Internet.",
                fase.mensaje
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun registrarConPrecioCeroMuestraErrorYNoLlamaRepositorio() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repositorio = ProductoRepositoryFalso()
            val viewModel = crearViewModel(repositorio)
            advanceUntilIdle()

            viewModel.actualizarNombre("Vitamina C")
            viewModel.actualizarPrecio("0")
            viewModel.actualizarStock("12")
            viewModel.registrarProducto()
            advanceUntilIdle()

            assertEquals("El precio debe ser mayor a 0", viewModel.uiState.value.precioError)
            assertEquals(0, repositorio.registros)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun editarProductoActualizaElRegistroSinDuplicarlo() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val producto = Producto(id = 7L, nombre = "Alcohol", precio = 7.0, stock = 15)
            val repositorio = ProductoRepositoryFalso(productos = listOf(producto))
            val viewModel = crearViewModel(repositorio)
            advanceUntilIdle()

            viewModel.editarProducto(producto)
            viewModel.actualizarPrecio("8.50")
            viewModel.registrarProducto()
            advanceUntilIdle()

            assertEquals(0, repositorio.registros)
            assertEquals(1, repositorio.actualizaciones)
            assertEquals(8.50, viewModel.uiState.value.productos.single().precio)
            assertEquals(null, viewModel.uiState.value.productoEnEdicionId)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun crearViewModel(productoRepository: ProductoRepository): ProductoViewModel {
        return ProductoViewModel(
            registrarProductoUseCase = RegistrarProductoUseCase(productoRepository),
            actualizarProductoUseCase = ActualizarProductoUseCase(productoRepository),
            productoRepository = productoRepository
        )
    }

    private class ProductoRepositoryFalso(
        productos: List<Producto> = emptyList(),
        private val errorAlListar: Throwable? = null
    ) : ProductoRepository {
        private val productosGuardados = productos.toMutableList()
        var registros: Int = 0
            private set
        var actualizaciones: Int = 0
            private set

        override suspend fun registrar(producto: Producto): Producto {
            registros++
            return producto.copy(id = 100L + registros).also(productosGuardados::add)
        }

        override suspend fun actualizar(producto: Producto): Producto {
            actualizaciones++
            val indice = productosGuardados.indexOfFirst { it.id == producto.id }
            check(indice >= 0)
            productosGuardados[indice] = producto
            return producto
        }

        override suspend fun listar(): Result<List<Producto>> = runCatching {
            errorAlListar?.let { throw it }
            productosGuardados.toList()
        }
    }
}
