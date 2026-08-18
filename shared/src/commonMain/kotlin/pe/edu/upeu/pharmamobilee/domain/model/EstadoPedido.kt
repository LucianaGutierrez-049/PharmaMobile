package pe.edu.upeu.pharmamobilee.domain.model

sealed class EstadoPedido {
    data object Pendiente : EstadoPedido()
    data object Procesando : EstadoPedido()
    data object Entregado : EstadoPedido()
    data class Rechazado(
        val motivo : String
    ): EstadoPedido()
}