package pe.edu.upeu.pharmamobilee.demo

import pe.edu.upeu.pharmamobilee.domain.model.Cliente

fun probarCliente(){
    val cliente = Cliente(
        id = 1L,
        nombre = "Farmacia Nueva Vida",
        correo = "ventas@central.pe",
        telefono = null
    )
    println(cliente.obtenerTelefono())
}