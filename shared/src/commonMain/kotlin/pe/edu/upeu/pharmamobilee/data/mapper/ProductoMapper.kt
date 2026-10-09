package pe.edu.upeu.pharmamobilee.data.mapper

import pe.edu.upeu.pharmamobilee.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import pe.edu.upeu.pharmamobilee.domain.model.Producto

fun ProductoResponseDto.toDomain(): Producto = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock,
    activo = estado,
    categoria = categoriaNombre ?: "Sin categoría",
    categoriaId = categoriaId,
    stockDisponible = true,
    origen = OrigenProducto.REMOTO
)

fun Producto.toRequest(categoriaPorDefecto: Long): ProductoRequestDto =
    ProductoRequestDto(
        nombre = nombre,
        precio = precio,
        stock = stock,
        estado = activo,
        categoriaId = categoriaId ?: categoriaPorDefecto
    )
