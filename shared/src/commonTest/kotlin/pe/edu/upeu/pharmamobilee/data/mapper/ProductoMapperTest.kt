package pe.edu.upeu.pharmamobilee.data.mapper

import pe.edu.upeu.pharmamobilee.data.remote.dto.CategoriaDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ProductoMapperTest {

    @Test
    fun dtoCompletoSeMapeaSinAcoplarElDominioALaRed() {
        val producto = ProductoDto(
            id = 7,
            title = "Producto API",
            price = 19.90,
            description = "Descripción remota",
            images = listOf("https://example.com/producto.png"),
            categoria = CategoriaDto(id = 2, name = "Salud")
        ).toDomain()

        assertEquals(7L, producto.id)
        assertEquals("Producto API", producto.nombre)
        assertEquals(19.90, producto.precio)
        assertEquals("Descripción remota", producto.descripcion)
        assertEquals("https://example.com/producto.png", producto.imagen)
        assertEquals("Salud", producto.categoria)
        assertEquals(OrigenProducto.REMOTO, producto.origen)
        assertFalse(producto.stockDisponible)
    }

    @Test
    fun dtoSinImagenNiCategoriaUsaValoresSeguros() {
        val producto = ProductoDto(
            id = 8,
            title = "Sin metadatos",
            price = 4.0
        ).toDomain()

        assertEquals("", producto.imagen)
        assertEquals("Sin categoría", producto.categoria)
    }
}
