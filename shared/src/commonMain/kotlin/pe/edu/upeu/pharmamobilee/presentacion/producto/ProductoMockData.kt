package pe.edu.upeu.pharmamobilee.presentacion.producto

import pe.edu.upeu.pharmamobilee.domain.model.Producto

val productosInventarioInicial = listOf(
    Producto(
        id = 1L,
        nombre = "Paracetamol",
        precio = 15.50,
        stock = 100,
        activo = true
    ),
    Producto(
        id = 2L,
        nombre = "Ibuprofeno",
        precio = 18.90,
        stock = 50,
        activo = true
    ),
    Producto(
        id = 3L,
        nombre = "Amoxicilina",
        precio = 25.00,
        stock = 5,
        activo = true
    ),
    Producto(
        id = 4L,
        nombre = "Loratadina",
        precio = 12.50,
        stock = 0,
        activo = false
    ),
    Producto(
        id = 5L,
        nombre = "Diclofenaco",
        precio = 20.00,
        stock = 3,
        activo = true
    )
)
