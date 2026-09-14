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

            val fase = assertIs<ProductoFase.ConProductos>(viewModel.uiState.value.fase)
            assertEquals(productos, fase.productos)
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
            assertEquals("Error al cargar productos", fase.mensaje)
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

    private fun crearViewModel(productoRepository: ProductoRepository): ProductoViewModel {
        return ProductoViewModel(
            registrarProductoUseCase = RegistrarProductoUseCase(productoRepository),
            productoRepository = productoRepository
        )
    }

    private class ProductoRepositoryFalso(
        private val productos: List<Producto> = emptyList(),
        private val errorAlListar: Throwable? = null
    ) : ProductoRepository {
        var registros: Int = 0
            private set

        override suspend fun registrar(producto: Producto): Producto {
            registros++
            return producto.copy(id = 100L + registros)
        }

        override suspend fun listar(): List<Producto> {
            errorAlListar?.let { throw it }
            return productos
        }
    }
}
