package pe.edu.upeu.pharmamobilee.presentation.producto

import kotlin.test.Test
import kotlin.test.assertEquals

class AntiguedadSincronizacionTest {
    @Test
    fun informaCuandoNuncaSeSincronizo() {
        assertEquals("Nunca sincronizado", textoAntiguedadSincronizacion(null, 10_000L))
    }

    @Test
    fun expresaLaAntiguedadEnLenguajeNatural() {
        val ahora = 200_000_000L
        assertEquals(
            "Actualizado hace 2 días",
            textoAntiguedadSincronizacion(ahora - 2L * 24L * 60L * 60L * 1_000L, ahora)
        )
    }
}
