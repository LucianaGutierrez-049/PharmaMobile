package pe.edu.upeu.pharmamobilee.domain.model

data class DetalleVenta(
    val productoId: Long,
    val productoNombre: String,
    val cantidad: Int,
    val precio: Double,
    val subtotal: Double
)

/** Venta registrada en PharmaSoft. El total lo calcula siempre el servidor. */
data class Venta(
    val id: Long,
    val fecha: String,
    val clienteId: Long,
    val clienteNombre: String,
    val estado: String,
    val total: Double,
    val detalles: List<DetalleVenta>
)

/** Línea del carrito antes de enviar la venta (total estimado; el definitivo lo da el servidor). */
data class LineaVenta(val producto: Producto, val cantidad: Int) {
    val subtotal: Double get() = producto.precio * cantidad
}
