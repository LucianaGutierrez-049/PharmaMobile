package pe.edu.upeu.pharmamobilee.service

import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlin.test.Test
import kotlin.test.assertEquals
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.result.ResultadoProductos
import pe.edu.upeu.pharmamobilee.domain.service.cargarProductos
import pe.edu.upeu.pharmamobilee.domain.service.observarProductos

class ProductoServiceTest {
    @Test
    fun cargarProductos_debeEmitirCargandoYExito() = runTest {

        val resultados = cargarProductos().toList()

        assertEquals(2, resultados.size)

        assertTrue(resultados[0] is ResultadoProductos.Cargando)
        assertTrue(resultados[1] is ResultadoProductos.Exito)
    }

    @Test
    fun observarProductos_debeEmitirInventarioActualizado() = runTest {

        val emisiones = mutableListOf<List<Producto>>()

        observarProductos().collect { productos ->
            emisiones.add(productos)
        }

        assertEquals(3, emisiones.size)
        assertTrue(emisiones[0].isEmpty())
        assertEquals(100, emisiones[1].first().stock)
        assertEquals(99, emisiones[2].first().stock)
    }
}
