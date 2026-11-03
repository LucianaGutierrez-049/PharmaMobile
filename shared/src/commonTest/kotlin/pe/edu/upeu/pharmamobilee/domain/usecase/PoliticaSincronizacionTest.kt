package pe.edu.upeu.pharmamobilee.domain.usecase

import kotlin.test.*
import pe.edu.upeu.pharmamobilee.domain.model.*

class PoliticaSincronizacionTest {
    @Test
    fun esperaCreceYEstaAcotada() {
        assertEquals(listOf(1000L, 2000L, 4000L, 8000L, 16000L, 16000L),
            (0..5).map(PoliticaSincronizacion::esperaMillis))
    }

    @Test
    fun noEnviaFilasAgotadasBloqueadasONoVencidas() {
        val producto = Producto(-1, "Prueba", 1.0, pendienteSincronizacion = true)
        assertTrue(PoliticaSincronizacion.puedeEnviar(producto, 1000))
        assertFalse(PoliticaSincronizacion.puedeEnviar(producto.copy(intentos = 5), 1000))
        assertFalse(PoliticaSincronizacion.puedeEnviar(producto.copy(requiereRevision = true), 1000))
        assertFalse(PoliticaSincronizacion.puedeEnviar(producto.copy(proximoIntentoMillis = 2000), 1000))
    }
}
