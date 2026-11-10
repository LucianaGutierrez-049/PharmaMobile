package pe.edu.upeu.pharmamobilee.presentacion.cliente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobilee.domain.error.mensajeUsuario
import pe.edu.upeu.pharmamobilee.domain.model.Cliente
import pe.edu.upeu.pharmamobilee.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobilee.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobilee.presentation.sesion.GestorSesion

data class ClienteFormulario(
    val dni: String = "",
    val nombres: String = "",
    val apellidos: String = "",
    val email: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val activo: Boolean = true,
    val errores: Map<String, String> = emptyMap()
)

data class ClienteUiState(
    val cargando: Boolean = true,
    val clientes: List<Cliente> = emptyList(),
    val busqueda: String = "",
    val error: String? = null,
    val formulario: ClienteFormulario = ClienteFormulario(),
    val editandoId: Long? = null,
    val formularioVisible: Boolean = false,
    val guardando: Boolean = false,
    val errorFormulario: String? = null,
    val mensaje: String? = null
) {
    val filtrados: List<Cliente>
        get() = busqueda.trim().lowercase().let { q ->
            if (q.isEmpty()) clientes
            else clientes.filter { q in it.nombreCompleto.lowercase() || q in it.dni || q in it.email.lowercase() }
        }
}

class ClienteViewModel(
    private val repositorio: ClienteRepository,
    private val authRepository: AuthRepository? = null,
    private val gestorSesion: GestorSesion? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClienteUiState())
    val uiState = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            repositorio.listar()
                .onSuccess { lista -> _uiState.update { it.copy(cargando = false, clientes = lista) } }
                .onFailure { e -> manejarSesion(e); _uiState.update { it.copy(cargando = false, error = mensaje(e)) } }
        }
    }

    fun buscar(texto: String) = _uiState.update { it.copy(busqueda = texto) }

    fun nuevo() = _uiState.update {
        it.copy(formulario = ClienteFormulario(), editandoId = null, formularioVisible = true, mensaje = null, errorFormulario = null)
    }

    fun editar(c: Cliente) = _uiState.update {
        it.copy(
            formulario = ClienteFormulario(c.dni, c.nombres, c.apellidos, c.email, c.telefono.orEmpty(), c.direccion.orEmpty(), c.activo),
            editandoId = c.id, formularioVisible = true, mensaje = null, errorFormulario = null
        )
    }

    fun cerrarFormulario() = _uiState.update { it.copy(formularioVisible = false, editandoId = null) }

    fun cambiar(transformar: (ClienteFormulario) -> ClienteFormulario) =
        _uiState.update { it.copy(formulario = transformar(it.formulario).copy(errores = emptyMap()), errorFormulario = null) }

    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }

    fun guardar() {
        val estado = _uiState.value
        if (estado.guardando) return
        val f = estado.formulario
        val errores = buildMap {
            ClienteValidator.validarDni(f.dni)?.let { put("dni", it) }
            ClienteValidator.validarNombre(f.nombres)?.let { put("nombres", it) }
            ClienteValidator.validarApellidos(f.apellidos)?.let { put("apellidos", it) }
            ClienteValidator.validarCorreo(f.email)?.let { put("email", it) }
            ClienteValidator.validarTelefono(f.telefono)?.let { put("telefono", it) }
            ClienteValidator.validarDireccion(f.direccion)?.let { put("direccion", it) }
        }
        if (errores.isNotEmpty()) {
            _uiState.update { it.copy(formulario = f.copy(errores = errores)) }
            return
        }
        val cliente = Cliente(
            id = estado.editandoId ?: 0L, dni = f.dni, nombres = f.nombres, apellidos = f.apellidos,
            email = f.email, telefono = f.telefono, direccion = f.direccion, activo = f.activo
        )
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            val resultado = if (estado.editandoId == null) repositorio.registrar(cliente) else repositorio.actualizar(cliente)
            resultado.onSuccess { guardado ->
                _uiState.update {
                    it.copy(
                        guardando = false, formularioVisible = false, editandoId = null,
                        mensaje = if (estado.editandoId == null) "Cliente ${guardado.nombreCompleto} registrado"
                        else "Cliente ${guardado.nombreCompleto} actualizado"
                    )
                }
                cargar()
            }.onFailure { e ->
                manejarSesion(e)
                val api = (e as? ErrorApiException)?.error
                _uiState.update {
                    it.copy(
                        guardando = false,
                        formulario = if (api is ErrorApi.Validacion) it.formulario.copy(errores = api.porCampo) else it.formulario,
                        errorFormulario = mensaje(e)
                    )
                }
            }
        }
    }

    fun darDeBaja(cliente: Cliente) {
        viewModelScope.launch {
            repositorio.darDeBaja(cliente.id)
                .onSuccess { _uiState.update { it.copy(mensaje = "Cliente ${cliente.nombreCompleto} dado de baja") }; cargar() }
                .onFailure { e -> manejarSesion(e); _uiState.update { it.copy(mensaje = mensaje(e)) } }
        }
    }

    private fun mensaje(e: Throwable): String = (e as? ErrorApiException)?.error.mensajeUsuario()

    private fun manejarSesion(e: Throwable) {
        if ((e as? ErrorApiException)?.error != ErrorApi.SesionExpirada) return
        viewModelScope.launch {
            authRepository?.cerrarSesion()
            gestorSesion?.sinSesion("Tu sesión expiró. Inicia sesión nuevamente.")
        }
    }
}
