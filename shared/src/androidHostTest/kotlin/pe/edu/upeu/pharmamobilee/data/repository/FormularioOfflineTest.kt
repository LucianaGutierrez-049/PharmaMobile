package pe.edu.upeu.pharmamobilee.data.repository

import androidx.lifecycle.ViewModelStore
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import pe.edu.upeu.pharmamobilee.data.local.ProductoLocal
import pe.edu.upeu.pharmamobilee.db.PharmaDb
import pe.edu.upeu.pharmamobilee.domain.error.*
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.platform.Compartidor
import pe.edu.upeu.pharmamobilee.domain.repository.*
import pe.edu.upeu.pharmamobilee.domain.model.*
import pe.edu.upeu.pharmamobilee.domain.usecase.*
import pe.edu.upeu.pharmamobilee.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobilee.presentation.sesion.GestorSesion
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FormularioOfflineTest {
    @Test fun error400RealDelRepositorioOfflineLlegaAlCampoSinDuplicarBorrador() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val d = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { PharmaDb.Schema.create(it) }
        val store = ViewModelStore()
        try {
            val local = ProductoLocal(d)
            val repo = ProductoRepositorioOffline(local, remoto(ErrorApi.Validacion(mapOf("categoriaId" to "Categoría inválida"))))
            val vm = vm(repo)
            store.put("producto", vm)
            advanceUntilIdle()
            vm.actualizarNombre("Formulario conservado"); vm.actualizarPrecio("2.50"); vm.actualizarStock("10"); vm.seleccionarCategoria(1L)
            vm.seleccionarCategoria(47)
            vm.guardarProducto()
            advanceUntilIdle()
            assertEquals("Categoría inválida", vm.uiState.value.categoriaError)
            assertEquals("Formulario conservado", vm.uiState.value.nombre)
            assertEquals(-1L, vm.uiState.value.productoEnEdicionId)
            vm.seleccionarCategoria(48); vm.guardarProducto()
            advanceUntilIdle()
            assertEquals(1, local.pendientes().size)
            assertEquals(48L, local.pendientes().single().categoriaId)
        } finally { store.clear(); Dispatchers.resetMain(); d.close() }
    }

    @Test fun error401DuranteCreacionCierraSesionYConservaElPendiente() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val d = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { PharmaDb.Schema.create(it) }
        val store = ViewModelStore()
        try {
            val local = ProductoLocal(d)
            val repo = ProductoRepositorioOffline(local, remoto(ErrorApi.SesionExpirada))
            var cerrado = false
            val auth = object : AuthRepository {
                override suspend fun iniciarSesion(credenciales: Credenciales): Sesion = error("No se usa")
                override suspend fun sesionGuardada(): Sesion? = null
                override suspend fun cerrarSesion() { cerrado = true }
            }
            val gestor = GestorSesion().apply { iniciar(Sesion("prueba", "A", "A", emptySet(), emptySet())) }
            val vm = vm(repo, auth, gestor)
            store.put("producto", vm)
            advanceUntilIdle()
            vm.actualizarNombre("Pendiente conservado"); vm.actualizarPrecio("2.50"); vm.actualizarStock("10"); vm.seleccionarCategoria(1L)
            vm.guardarProducto(); advanceUntilIdle()
            assertTrue(cerrado)
            assertNull(gestor.estado.value.sesion)
            assertEquals(1, local.pendientes().size)
        } finally { store.clear(); Dispatchers.resetMain(); d.close() }
    }

    private fun vm(repo: ProductoRepository, auth: AuthRepository? = null, gestor: GestorSesion? = null) = ProductoViewModel(
        ListarProductosUseCase(repo), RegistrarProductoUseCase(repo), ActualizarProductoUseCase(repo),
        EliminarProductoUseCase(repo), object : Compartidor { override fun compartir(texto: String) = Unit },
        auth, gestor, repo)

    private fun remoto(error: ErrorApi) = object : ProductoRepository {
        override suspend fun listar() = Result.success(emptyList<Producto>())
        override suspend fun obtener(id: Long) = Result.failure<Producto>(ErrorApiException(ErrorApi.NoEncontrado))
        override suspend fun registrar(producto: Producto) = Result.failure<Producto>(ErrorApiException(error))
        override suspend fun actualizar(producto: Producto) = Result.success(producto)
        override suspend fun eliminar(id: Long) = Result.success(Unit)
    }
}
