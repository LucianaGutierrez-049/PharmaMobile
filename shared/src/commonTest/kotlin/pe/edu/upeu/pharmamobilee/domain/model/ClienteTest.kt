package pe.edu.upeu.pharmamobilee.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ClienteTest {

    @Test
    fun probarCliente() {

        val cliente = Cliente(
            id = 1L,
            nombres = "Farmacia Nueva Vida",
            email = "ventas@central.pe",
            telefono = "989789123"
        )
        val resultado = cliente.obtenerTelefono()

        assertEquals(
            "989789123",
            resultado
        )
    }

    @Test
    fun `retorna No registrado cuando el telefono es null`() {

        val cliente = Cliente(
            id = 1L,
            nombres = "Farmacia Nueva Vida",
            email = "ventas@central.pe",
            telefono = null
        )
        val resultado = cliente.obtenerTelefono()

        assertEquals(
            "No registrado",
            resultado
        )
    }
}