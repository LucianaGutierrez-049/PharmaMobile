package pe.edu.upeu.pharmamobilee.domain.model

class Pedido (
    val id: Long,
    val cliente: Cliente,
    val detalles: List<DetallePedido>,
    val estado: EstadoPedido
)