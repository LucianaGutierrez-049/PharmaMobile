package pe.edu.upeu.pharmamobilee.data.repository

import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobilee.data.productosSimulados
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

class ProductoRepositorioEnMemoria(
    productosIniciales: List<Producto> = productosSimulados
) : ProductoRepository {
    private val productos = productosIniciales.toMutableList()
    private var siguienteId = (productos.maxOfOrNull { it.id } ?: 0L) + 1L

    override suspend fun registrar(producto: Producto): Producto {
        delay(400)
        val productoRegistrado = producto.copy(id = siguienteId++)
        productos.add(productoRegistrado)
        return productoRegistrado
    }

    override suspend fun actualizar(producto: Producto): Producto {
        delay(400)
        val indice = productos.indexOfFirst { it.id == producto.id }
        require(indice >= 0) { "No existe el producto con id ${producto.id}" }
        productos[indice] = producto
        return producto
    }

    override suspend fun listar(): List<Producto> {
        delay(400)
        return productos.toList()
    }
}
