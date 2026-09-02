package pe.edu.upeu.pharmamobilee.data

import pe.edu.upeu.pharmamobilee.domain.model.Producto

val productosSimulados = listOf(
    Producto(
        id = 1,
        nombre = "Paracetamol",
        precio = 15.50,
        stock = 100,
        activo = true
    ),
    Producto(
        id = 2,
        nombre = "Ibuprofeno",
        precio = 18.90,
        stock = 50,
        activo = true
    ),
    Producto(
        id = 3,
        nombre = "Amoxicilina",
        precio = 25.00,
        stock = 5,
        activo = true
    ),
    Producto(
        id = 4,
        nombre = "Loratadina",
        precio = 12.50,
        stock = 0,
        activo = false
    ),
    Producto(
        id = 5,
        nombre = "Diclofenaco",
        precio = 20.00,
        stock = 3,
        activo = true
    )
)
