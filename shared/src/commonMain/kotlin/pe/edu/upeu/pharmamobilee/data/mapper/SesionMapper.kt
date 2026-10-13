package pe.edu.upeu.pharmamobilee.data.mapper

import pe.edu.upeu.pharmamobilee.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.UsuarioActualDto
import pe.edu.upeu.pharmamobilee.domain.model.Sesion

fun LoginResponseDto.toDomain(): Sesion = Sesion(
    token = token,
    usuario = username,
    nombreCompleto = nombreCompleto,
    roles = roles.toSet(),
    permisos = permisos.toSet()
)

fun UsuarioActualDto.toDomain(token: String): Sesion = Sesion(
    token = token,
    usuario = username,
    nombreCompleto = empleadoNombre,
    roles = roles.toSet(),
    permisos = permisos.toSet()
)
