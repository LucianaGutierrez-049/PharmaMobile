package pe.edu.upeu.pharmamobilee.demo

import pe.edu.upeu.pharmamobilee.domain.model.Cliente

fun probarCliente(){
    val cliente = Cliente(
        id = 1L,
        nombres = "Farmacia Nueva Vida",
        email = "ventas@central.pe",
        telefono = null
    )
    println(cliente.obtenerTelefono())
}