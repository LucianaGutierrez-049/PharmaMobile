package pe.edu.upeu.pharmamobilee.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobilee.domain.error.ErrorSesionException
import pe.edu.upeu.pharmamobilee.domain.error.mensajeUsuario
import pe.edu.upeu.pharmamobilee.domain.model.Credenciales
import pe.edu.upeu.pharmamobilee.domain.repository.AuthRepository

class SesionViewModel(
    private val repositorio: AuthRepository,
    private val gestorSesion: GestorSesion
) : ViewModel() {
    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState = _uiState.asStateFlow()
    val estadoGlobal = gestorSesion.estado

    init {
        recuperarSesion()
    }

    fun actualizarUsuario(valor: String) {
        gestorSesion.limpiarAviso()
        _uiState.update { it.copy(usuario = valor, usuarioError = null, error = null) }
    }

    fun actualizarClave(valor: String) {
        gestorSesion.limpiarAviso()
        _uiState.update { it.copy(clave = valor, claveError = null, error = null) }
    }

    fun iniciarSesion() {
        val usuario = _uiState.value.usuario.trim()
        val clave = _uiState.value.clave
        val usuarioError = if (usuario.isBlank()) "Ingresa tu usuario." else null
        val claveError = if (clave.isBlank()) "Ingresa tu contraseña." else null
        if (usuarioError != null || claveError != null) {
            _uiState.update { it.copy(usuarioError = usuarioError, claveError = claveError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            runCatching {
                repositorio.iniciarSesion(Credenciales(usuario, clave))
            }.onSuccess { sesion ->
                _uiState.value = SesionUiState()
                gestorSesion.iniciar(sesion)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        cargando = false,
                        error = (error as? ErrorSesionException)?.error?.mensajeUsuario()
                            ?: "No se pudo iniciar sesión."
                    )
                }
            }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            repositorio.cerrarSesion()
            gestorSesion.sinSesion("Sesión cerrada correctamente.")
        }
    }

    private fun recuperarSesion() {
        viewModelScope.launch {
            runCatching { repositorio.sesionGuardada() }
                .onSuccess { sesion ->
                    if (sesion == null) gestorSesion.sinSesion()
                    else gestorSesion.iniciar(sesion)
                }
                .onFailure {
                    gestorSesion.sinSesion("No se pudo recuperar la sesión guardada.")
                }
        }
    }
}
