package pe.edu.upeu.pharmamobilee.presentation.sesion

data class SesionUiState(
    val usuario: String = "",
    val clave: String = "",
    val usuarioError: String? = null,
    val claveError: String? = null,
    val cargando: Boolean = false,
    val error: String? = null
)
