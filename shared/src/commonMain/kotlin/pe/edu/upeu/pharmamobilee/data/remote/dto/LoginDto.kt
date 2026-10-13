package pe.edu.upeu.pharmamobilee.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: Long,
    val username: String,
    val nombreCompleto: String,
    val roles: List<String>,
    val permisos: List<String>
)

@Serializable
data class UsuarioActualDto(
    val username: String,
    val empleadoNombre: String,
    val roles: List<String>,
    val permisos: List<String>
)
