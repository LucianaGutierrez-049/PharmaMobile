package pe.edu.upeu.pharmamobilee.presentation.sesion

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import pe.edu.upeu.pharmamobilee.domain.model.Sesion

data class EstadoSesionGlobal(
    val inicializando: Boolean = true,
    val sesion: Sesion? = null,
    val aviso: String? = null
)

class GestorSesion {
    private val _estado = MutableStateFlow(EstadoSesionGlobal())
    val estado = _estado.asStateFlow()

    fun iniciar(sesion: Sesion) {
        _estado.value = EstadoSesionGlobal(inicializando = false, sesion = sesion)
    }

    fun sinSesion(aviso: String? = null) {
        _estado.value = EstadoSesionGlobal(inicializando = false, aviso = aviso)
    }

    fun limpiarAviso() {
        _estado.update { it.copy(aviso = null) }
    }
}
