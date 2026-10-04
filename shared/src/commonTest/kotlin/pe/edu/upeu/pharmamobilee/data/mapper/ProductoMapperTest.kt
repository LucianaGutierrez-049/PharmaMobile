package pe.edu.upeu.pharmamobilee.data.mapper

import pe.edu.upeu.pharmamobilee.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoMapperTest {

    @Test
    fun dtoCompletoSeMapeaSinAcoplarElDominioALaRed() {
        val producto = ProductoResponseDto(
            id = 7L,
            nombre = "Producto API",
            precio = 19.90,
            stock = 42,
            estado = false,
            categoriaId = 2L,
            categoriaNombre = "Salud"
        ).toDomain()

        assertEquals(7L, producto.id)
        assertEquals("Producto API", producto.nombre)
        assertEquals(19.90, producto.precio)
        assertEquals(42, producto.stock)
        assertEquals(false, producto.activo)
        assertEquals("Salud", producto.categoria)
        assertEquals(OrigenProducto.REMOTO, producto.origen)
        assertTrue(producto.stockDisponible)
    }

    @Test
    fun dtoSinImagenNiCategoriaUsaValoresSeguros() {
        val producto = ProductoResponseDto(
            id = 8L,
            nombre = "Sin metadatos",
            precio = 4.0,
            stock = 0
        ).toDomain()

        assertEquals("Sin categoría", producto.categoria)
    }
}
