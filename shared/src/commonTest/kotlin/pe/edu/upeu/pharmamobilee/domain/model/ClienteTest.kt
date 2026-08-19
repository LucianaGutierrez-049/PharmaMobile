package pe.edu.upeu.pharmamobilee.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ClienteTest{


    @Test
    fun probarCliente(){

        val cliente = Cliente(
        id = 1L,
        nombre = "Farmacia Nueva Vida",
        correo = "ventas@central.pe",
        telefono = "989789123"
        )
          val resultado = cliente.obtenerTelefono()

        assertEquals(
            "989789123",
            resultado
        )
    }
}