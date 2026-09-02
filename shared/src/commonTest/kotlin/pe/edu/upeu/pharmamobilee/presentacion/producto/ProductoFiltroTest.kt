package pe.edu.upeu.pharmamobilee.presentacion.producto

import kotlin.test.Test
import kotlin.test.assertEquals

class ProductoFiltroTest {

    @Test
    fun filtraProductosActivos() {
        val nombres = filtrarProductosInventario(
            productos = productosInventarioInicial,
            filtro = ProductoFiltro.Activos
        ).map { it.nombre }

        assertEquals(
            listOf("Paracetamol", "Ibuprofeno", "Amoxicilina", "Diclofenaco"),
            nombres
        )
    }

    @Test
    fun filtraProductosInactivos() {
        val nombres = filtrarProductosInventario(
            productos = productosInventarioInicial,
            filtro = ProductoFiltro.Inactivos
        ).map { it.nombre }

        assertEquals(
            listOf("Loratadina"),
            nombres
        )
    }

    @Test
    fun bajoStockIncluyeStockCincoYCero() {
        val nombres = filtrarProductosInventario(
            productos = productosInventarioInicial,
            filtro = ProductoFiltro.BajoStock
        ).map { it.nombre }

        assertEquals(
            listOf("Amoxicilina", "Loratadina", "Diclofenaco"),
            nombres
        )
    }
}
