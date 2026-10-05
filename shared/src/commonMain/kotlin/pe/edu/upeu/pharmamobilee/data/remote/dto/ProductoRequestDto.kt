package pe.edu.upeu.pharmamobilee.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean,
    val categoriaId: Long
)
