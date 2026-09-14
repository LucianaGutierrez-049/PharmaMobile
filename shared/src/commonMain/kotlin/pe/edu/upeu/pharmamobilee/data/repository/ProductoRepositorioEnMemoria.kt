package pe.edu.upeu.pharmamobilee.data.repository

import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

class ProductoRepositorioEnMemoria : ProductoRepository {
    private val productos = mutableListOf<Producto>()
    private var siguienteId = 1L

    override suspend fun registrar(producto: Producto): Producto {
        delay(400)
        val productoRegistrado = producto.copy(id = siguienteId++)
        productos.add(productoRegistrado)
        return productoRegistrado
    }

    override suspend fun listar(): List<Producto> {
        delay(400)
        return productos.toList()
    }
}
