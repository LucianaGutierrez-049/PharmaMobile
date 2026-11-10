package pe.edu.upeu.pharmamobilee.presentacion.pedido

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
import pe.edu.upeu.pharmamobilee.domain.model.LineaVenta
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.model.Venta
import pe.edu.upeu.pharmamobilee.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobilee.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.domain.repository.VentaRepository
import pe.edu.upeu.pharmamobilee.presentation.sesion.GestorSesion

data class VentaUiState(
    val cargando: Boolean = true,
    val ventas: List<Venta> = emptyList(),
    val error: String? = null,
    val clientes: List<Cliente> = emptyList(),
    val productos: List<Producto> = emptyList(),
    val clienteId: Long? = null,
    val productoId: Long? = null,
    val cantidad: String = "1",
    val carrito: List<LineaVenta> = emptyList(),
    val errorCarrito: String? = null,
    val registrando: Boolean = false,
    val mensaje: String? = null
) {
    val totalEstimado: Double get() = carrito.sumOf { it.subtotal }
    val clienteSeleccionado: Cliente? get() = clientes.firstOrNull { it.id == clienteId }
    val productoSeleccionado: Producto? get() = productos.firstOrNull { it.id == productoId }
}

class VentaViewModel(
    private val ventas: VentaRepository,
    private val clientes: ClienteRepository,
    private val productos: ProductoRepository,
    private val authRepository: AuthRepository? = null,
    private val gestorSesion: GestorSesion? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(VentaUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Solo se venden productos sincronizados (ID del servidor), activos y con stock.
        viewModelScope.launch {
            productos.observar().collect { lista ->
                _uiState.update { it.copy(productos = lista.filter { p -> p.id > 0 && p.activo && p.stock > 0 }.sortedBy { p -> p.nombre }) }
            }
        }
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            ventas.listar()
                .onSuccess { lista -> _uiState.update { it.copy(cargando = false, ventas = lista) } }
                .onFailure { e -> manejarSesion(e); _uiState.update { it.copy(cargando = false, error = mensaje(e)) } }
            clientes.listar()
                .onSuccess { lista -> _uiState.update { it.copy(clientes = lista.filter(Cliente::activo)) } }
                .onFailure { e -> manejarSesion(e) }
        }
    }

    fun seleccionarCliente(id: Long) = _uiState.update { it.copy(clienteId = id, errorCarrito = null) }
    fun seleccionarProducto(id: Long) = _uiState.update { it.copy(productoId = id, errorCarrito = null) }
    fun cambiarCantidad(v: String) = _uiState.update { it.copy(cantidad = v.filter(Char::isDigit).take(4), errorCarrito = null) }
    fun quitar(linea: LineaVenta) = _uiState.update { it.copy(carrito = it.carrito - linea) }
    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }

    fun agregar() {
        val s = _uiState.value
        val producto = s.productoSeleccionado
        val cantidad = s.cantidad.toIntOrNull() ?: 0
        val enCarrito = s.carrito.filter { it.producto.id == producto?.id }.sumOf { it.cantidad }
        val error = when {
            producto == null -> "Selecciona un producto."
            cantidad <= 0 -> "La cantidad debe ser mayor que cero."
            cantidad + enCarrito > producto.stock -> "Stock disponible de ${producto.nombre}: ${producto.stock - enCarrito}."
            else -> null
        }
        if (error != null) { _uiState.update { it.copy(errorCarrito = error) }; return }
        _uiState.update {
            val existente = it.carrito.firstOrNull { l -> l.producto.id == producto!!.id }
            val carrito = if (existente == null) it.carrito + LineaVenta(producto!!, cantidad)
            else it.carrito.map { l -> if (l == existente) l.copy(cantidad = l.cantidad + cantidad) else l }
            it.copy(carrito = carrito, productoId = null, cantidad = "1")
        }
    }

    fun registrar() {
        val s = _uiState.value
        if (s.registrando) return
        val clienteId = s.clienteId
        val error = when {
            clienteId == null -> "Selecciona un cliente."
            s.carrito.isEmpty() -> "Agrega al menos un producto."
            else -> null
        }
        if (error != null) { _uiState.update { it.copy(errorCarrito = error) }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(registrando = true) }
            ventas.registrar(clienteId!!, s.carrito)
                .onSuccess { venta ->
                    _uiState.update {
                        it.copy(registrando = false, carrito = emptyList(), clienteId = null,
                            mensaje = "Venta #${venta.id} registrada por ${formatoMonto(venta.total)}")
                    }
                    productos.sincronizar() // refleja el stock descontado por el servidor
                    cargar()
                }
                .onFailure { e ->
                    manejarSesion(e)
                    _uiState.update { it.copy(registrando = false, errorCarrito = mensaje(e)) }
                }
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

fun formatoMonto(valor: Double): String = pe.edu.upeu.pharmamobilee.platform.formatearSoles(valor)
