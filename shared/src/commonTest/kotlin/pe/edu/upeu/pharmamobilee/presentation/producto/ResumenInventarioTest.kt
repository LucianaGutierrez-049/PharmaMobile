package pe.edu.upeu.pharmamobilee.presentation.producto

import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.presentacion.producto.ProductoFiltro
import pe.edu.upeu.pharmamobilee.presentacion.producto.filtrarProductosInventario
import kotlin.test.Test
import kotlin.test.assertEquals

class ResumenInventarioTest {
    @Test
    fun incluyeProductosPharmaSoftRemotosYNoSoloLocales() {
        val estado = ProductoUiState(productos = listOf(
            producto(1, true, 120), producto(2, true, 5), producto(3, false, 0),
            producto(4, true, 10, OrigenProducto.LOCAL)
        ), fase = ProductoFase.ConProductos)

        assertEquals(ResumenInventarioUi(4, 3, 1, 2), estado.resumenInventario)
        assertEquals(estado.resumenInventario.total,
            estado.resumenInventario.activos + estado.resumenInventario.inactivos)
        assertEquals(estado.resumenInventario.activos,
            filtrarProductosInventario(estado.productos, ProductoFiltro.Activos).size)
        assertEquals(estado.resumenInventario.inactivos,
            filtrarProductosInventario(estado.productos, ProductoFiltro.Inactivos).size)
        assertEquals(estado.resumenInventario.bajoStock,
            filtrarProductosInventario(estado.productos, ProductoFiltro.BajoStock).size)
    }

    @Test
    fun inventarioVacioConfirmadoTieneCeros() {
        assertEquals(ResumenInventarioUi(0, 0, 0, 0),
            ProductoUiState(fase = ProductoFase.SinProductos).resumenInventario)
    }

    @Test
    fun cambiosDeEstadoYStockRecalculanElResumenSinCopiasObsoletas() {
        val inicial = ProductoUiState(productos = listOf(producto(1, true, 10)))
        val actualizado = inicial.copy(productos = listOf(producto(1, false, 2)))
        val eliminado = actualizado.copy(productos = emptyList())
        assertEquals(ResumenInventarioUi(1, 1, 0, 0), inicial.resumenInventario)
        assertEquals(ResumenInventarioUi(1, 0, 1, 1), actualizado.resumenInventario)
        assertEquals(ResumenInventarioUi(0, 0, 0, 0), eliminado.resumenInventario)
    }

    private fun producto(id: Long, activo: Boolean, stock: Int,
        origen: OrigenProducto = OrigenProducto.REMOTO): ProductoUi =
        Producto(id, "Producto $id", 1.0, stock, activo, origen = origen).toUi()
}
