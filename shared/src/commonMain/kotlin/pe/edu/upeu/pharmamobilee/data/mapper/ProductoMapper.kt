package pe.edu.upeu.pharmamobilee.data.mapper

import pe.edu.upeu.pharmamobilee.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import pe.edu.upeu.pharmamobilee.domain.model.Producto

fun ProductoDto.toDomain(): Producto = Producto(
    id = id.toLong(),
    nombre = title,
    precio = price,
    descripcion = description,
    imagen = images.firstOrNull().orEmpty(),
    categoria = categoria?.name ?: "Sin categoría",
    stockDisponible = false,
    origen = OrigenProducto.REMOTO
)
