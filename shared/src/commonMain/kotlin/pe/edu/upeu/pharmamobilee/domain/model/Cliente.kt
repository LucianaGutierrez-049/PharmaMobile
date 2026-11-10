package pe.edu.upeu.pharmamobilee.domain.model

/** Cliente de PharmaSoft (misma forma que ClienteResponseDTO del backend). */
data class Cliente(
    val id: Long,
    val dni: String = "",
    val nombres: String,
    val apellidos: String = "",
    val email: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val activo: Boolean = true
) {
    val nombreCompleto: String
        get() = listOf(nombres, apellidos).filter { it.isNotBlank() }.joinToString(" ")

    fun obtenerTelefono(): String = telefono?.takeIf { it.isNotBlank() } ?: "No registrado"
}
