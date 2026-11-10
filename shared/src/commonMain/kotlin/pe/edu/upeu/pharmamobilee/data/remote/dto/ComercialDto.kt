package pe.edu.upeu.pharmamobilee.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClienteRequestDto(
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val estado: Boolean
)

@Serializable
data class ClienteResponseDto(
    val id: Long,
    val dni: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val estado: Boolean = true
)

@Serializable
data class DetalleVentaRequestDto(val productoId: Long, val cantidad: Int)

@Serializable
data class VentaRequestDto(val clienteId: Long, val detalles: List<DetalleVentaRequestDto>)

@Serializable
data class DetalleVentaResponseDto(
    val productoId: Long,
    val productoNombre: String? = null,
    val cantidad: Int,
    val precio: Double,
    val subtotal: Double
)

@Serializable
data class VentaResponseDto(
    val id: Long,
    val fecha: String,
    val clienteId: Long,
    val clienteNombre: String? = null,
    val estado: String,
    val total: Double,
    val detalles: List<DetalleVentaResponseDto> = emptyList()
)
