package pe.edu.upeu.pharmamobilee.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobilee.domain.error.mensajeUsuario
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.platform.Compartidor
import pe.edu.upeu.pharmamobilee.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.comoTextoParaCompartir
import pe.edu.upeu.pharmamobilee.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.ProductoRegistroException
import pe.edu.upeu.pharmamobilee.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobilee.presentation.sesion.GestorSesion

class ProductoViewModel(
    private val listarProductosUseCase: ListarProductosUseCase,
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarProductoUseCase: ActualizarProductoUseCase,
    private val eliminarProductoUseCase: EliminarProductoUseCase,
    private val compartidor: Compartidor,
    private val authRepository: AuthRepository? = null,
    private val gestorSesion: GestorSesion? = null,
    private val productoRepository: ProductoRepository? = null
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        if (productoRepository == null) {
            cargarProductos()
        } else {
            viewModelScope.launch {
                productoRepository.observar().collect { productos ->
                    _uiState.update {
                        it.copy(
                            productos = productos.map(Producto::toUi),
                            pendientes = productos.count { p -> p.pendienteSincronizacion && !p.requiereRevision && p.intentos < 5 },
                            fallidas = productos.count { p -> p.pendienteSincronizacion && (p.requiereRevision || p.intentos >= 5) },
                            fase = if (productos.isEmpty()) ProductoFase.SinProductos else ProductoFase.ConProductos
                        )
                    }
                }
            }
            viewModelScope.launch {
                productoRepository.observarUltimaSincronizacion().collect { instante ->
                    _uiState.update { it.copy(ultimaSincronizacionMillis = instante) }
                }
            }
            viewModelScope.launch {
                productoRepository.observarCategorias().collect { categorias ->
                    _uiState.update { it.copy(categorias = categorias) }
                }
            }
            viewModelScope.launch {
                productoRepository.observarPendientesAnteriores().collect { cantidad ->
                    _uiState.update { it.copy(pendientesAnteriores = cantidad) }
                }
            }
            sincronizarOffline()
        }
    }

    fun seleccionarCategoria(id: Long) {
        _uiState.update { it.copy(categoriaSeleccionadaId = id, categoriaEnEdicionId = id, categoriaError = null) }
    }

    fun recuperarPendientesAnteriores() {
        viewModelScope.launch {
            productoRepository?.recuperarPendientesAnteriores()?.exceptionOrNull()?.let(::manejarFalloOperacion)
        }
    }

    fun actualizarNombre(nombre: String) {
        _uiState.update {
            it.copy(nombre = nombre, nombreError = null, mensajeExito = null)
        }
    }

    fun actualizarPrecio(precio: String) {
        _uiState.update {
            it.copy(precio = precio, precioError = null, mensajeExito = null)
        }
    }

    fun actualizarStock(stock: String) {
        _uiState.update {
            it.copy(stock = stock, stockError = null, mensajeExito = null)
        }
    }

    fun actualizarActivo(activo: Boolean) {
        _uiState.update { it.copy(activo = activo, mensajeExito = null) }
    }

    fun guardarProducto() {
        if (_uiState.value.operacion is ProductoOperacion.EnCurso) return
        // D-06: PharmaSoft exige categoría; se valida en el formulario en vez de encolar un borrador inválido.
        val inicial = _uiState.value
        if (inicial.productoEnEdicionId == null && inicial.categoriaSeleccionadaId == null) {
            _uiState.update { it.copy(categoriaError = "Selecciona una categoría.") }
            return
        }

        viewModelScope.launch {
            limpiarMensajes()
            val estado = _uiState.value
            val editando = estado.productoEnEdicionId != null
            val tipo = if (editando) {
                ProductoOperacion.Tipo.Actualizar
            } else {
                ProductoOperacion.Tipo.Crear
            }
            _uiState.update {
                it.copy(operacion = ProductoOperacion.EnCurso(tipo))
            }

            val resultado = estado.productoEnEdicionId?.let { id ->
                actualizarProductoUseCase(
                    id = id,
                    nombre = estado.nombre,
                    precio = estado.precio,
                    stock = estado.stock,
                    activo = estado.activo,
                    categoriaId = estado.categoriaSeleccionadaId ?: estado.categoriaEnEdicionId
                )
            } ?: registrarProductoUseCase(
                nombre = estado.nombre,
                precio = estado.precio,
                stock = estado.stock,
                activo = estado.activo,
                categoriaId = estado.categoriaSeleccionadaId
            )

            val producto = resultado.getOrElse { error ->
                manejarFalloOperacion(error)
                return@launch
            }

            limpiarFormulario()
            if (productoRepository != null) {
                _uiState.update { it.copy(operacion = ProductoOperacion.Inactiva, mensajeExito = if (producto.pendienteSincronizacion) "Guardado en el dispositivo. Revisa el estado de sincronización." else if (editando) "Producto \"${producto.nombre}\" actualizado correctamente" else "Producto \"${producto.nombre}\" registrado correctamente") }
            } else recargarTrasOperacion(
                if (editando) {
                    "Producto \"${producto.nombre}\" actualizado correctamente"
                } else {
                    "Producto \"${producto.nombre}\" registrado correctamente"
                }
            )
        }
    }

    fun eliminarProducto(id: Long) {
        if (_uiState.value.operacion is ProductoOperacion.EnCurso) return

        viewModelScope.launch {
            limpiarMensajes()
            _uiState.update {
                it.copy(
                    operacion = ProductoOperacion.EnCurso(
                        ProductoOperacion.Tipo.Eliminar
                    )
                )
            }

            eliminarProductoUseCase(id).getOrElse { error ->
                manejarFalloOperacion(error)
                return@launch
            }

            if (_uiState.value.productoEnEdicionId == id) {
                limpiarFormulario()
            }
            if (productoRepository != null) {
                _uiState.update { it.copy(operacion = ProductoOperacion.Inactiva, mensajeExito = "Producto eliminado correctamente") }
            } else recargarTrasOperacion("Producto eliminado correctamente")
        }
    }

    fun editarProducto(producto: Producto) {
        if (_uiState.value.operacion is ProductoOperacion.EnCurso) return
        _uiState.update {
            it.copy(
                nombre = producto.nombre,
                precio = producto.precio.toString(),
                stock = producto.stock.toString(),
                activo = producto.activo,
                nombreError = null,
                precioError = null,
                stockError = null,
                mensajeExito = null,
                operacion = ProductoOperacion.Inactiva,
                productoEnEdicionId = producto.id,
                categoriaEnEdicionId = producto.categoriaId,
                categoriaSeleccionadaId = producto.categoriaId
            )
        }
    }

    fun cancelarEdicion() {
        if (_uiState.value.operacion is ProductoOperacion.EnCurso) return
        limpiarFormulario()
    }

    fun compartir(producto: Producto) {
        compartidor.compartir(producto.comoTextoParaCompartir())
    }

    fun cargarProductos() {
        viewModelScope.launch {
            cargarListado(mostrarCarga = true)
        }
    }

    fun reintentar(id: Long) {
        val repositorio = productoRepository ?: return
        if (_uiState.value.sincronizando) return
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true) }
            try {
                val resultado = repositorio.reintentar(id)
                resultado.exceptionOrNull()?.let(::manejarErrorSesion)
                _uiState.update { it.copy(avisoRed = resultado.exceptionOrNull()?.let { error ->
                    if (error is IllegalStateException) error.message else mensajeDe(error)
                }) }
            } finally { _uiState.update { it.copy(sincronizando = false) } }
        }
    }

    fun sincronizar() = sincronizarOffline()

    private fun sincronizarOffline() {
        val repositorio = productoRepository ?: return
        if (_uiState.value.sincronizando) return
        viewModelScope.launch {
            _uiState.update { it.copy(sincronizando = true) }
            val resultado = try { repositorio.sincronizar() }
                finally { _uiState.update { it.copy(sincronizando = false) } }
            resultado.exceptionOrNull()?.let(::manejarErrorSesion)
            if ((resultado.exceptionOrNull() as? ErrorApiException)?.error != ErrorApi.SesionExpirada) {
                repositorio.listarCategorias().exceptionOrNull()?.let { error ->
                    manejarErrorSesion(error)
                    _uiState.update { it.copy(categoriaError = "No se pudieron actualizar las categorías. Se conservan las descargadas.") }
                }
            }
            _uiState.update {
                it.copy(
                    sincronizando = false,
                    avisoRed = resultado.exceptionOrNull()?.let { error -> mensajeDe(error) }
                )
            }
        }
    }

    private suspend fun cargarListado(
        mostrarCarga: Boolean
    ): Result<List<Producto>> {
        if (mostrarCarga) {
            _uiState.update { it.copy(fase = ProductoFase.Cargando) }
        }

        val resultado = listarProductosUseCase()
        resultado.onSuccess { productos ->
            _uiState.update {
                it.copy(
                    productos = productos.map(Producto::toUi),
                    fase = if (productos.isEmpty()) {
                        ProductoFase.SinProductos
                    } else {
                        ProductoFase.ConProductos
                    }
                )
            }
        }.onFailure { error ->
            manejarErrorSesion(error)
            if (mostrarCarga) {
                _uiState.update {
                    it.copy(
                        productos = emptyList(),
                        fase = ProductoFase.Error(mensajeDe(error))
                    )
                }
            }
        }
        return resultado
    }

    private suspend fun recargarTrasOperacion(mensaje: String) {
        cargarListado(mostrarCarga = false).fold(
            onSuccess = {
                _uiState.update {
                    it.copy(
                        operacion = ProductoOperacion.Inactiva,
                        mensajeExito = mensaje
                    )
                }
            },
            onFailure = { error ->
                _uiState.update {
                    it.copy(
                        operacion = ProductoOperacion.Fallida(
                            "La operación se completó, pero no se pudo actualizar la lista: ${mensajeDe(error)}"
                        )
                    )
                }
            }
        )
    }

    private fun manejarFalloOperacion(errorRecibido: Throwable) {
        val guardado = errorRecibido as? pe.edu.upeu.pharmamobilee.domain.error.ProductoPendienteException
        val fallo = guardado?.fallo ?: errorRecibido
        if (guardado != null) {
            _uiState.update { it.copy(productoEnEdicionId = guardado.producto.id,
                mensajeExito = null, avisoRed = "Guardado en el dispositivo; revisa los datos y confirma el reenvío.") }
        }
        manejarErrorSesion(fallo)
        when (fallo) {
            is ProductoRegistroException -> _uiState.update {
                it.copy(
                    operacion = ProductoOperacion.Inactiva,
                    nombreError = fallo.nombreError,
                    precioError = fallo.precioError,
                    stockError = fallo.stockError
                )
            }

            is ErrorApiException -> when (val error = fallo.error) {
                is ErrorApi.Validacion -> _uiState.update {
                    it.copy(
                        operacion = ProductoOperacion.Inactiva,
                        nombreError = error.porCampo["nombre"],
                        precioError = error.porCampo["precio"],
                        stockError = error.porCampo["stock"],
                        categoriaError = error.porCampo["categoriaId"]
                    )
                }

                else -> _uiState.update {
                    it.copy(
                        operacion = ProductoOperacion.Fallida(
                            error.mensajeUsuario()
                        )
                    )
                }
            }

            else -> _uiState.update {
                it.copy(
                    operacion = ProductoOperacion.Fallida(mensajeDe(fallo))
                )
            }
        }
    }

    private fun manejarErrorSesion(fallo: Throwable) {
        val error = (fallo as? ErrorApiException)?.error
        if (error != ErrorApi.SesionExpirada) return
        viewModelScope.launch {
            authRepository?.cerrarSesion()
            gestorSesion?.sinSesion("Tu sesión expiró. Inicia sesión nuevamente.")
        }
    }

    private fun mensajeDe(error: Throwable): String =
        (error as? ErrorApiException)?.error.mensajeUsuario()

    private fun limpiarMensajes() {
        _uiState.update {
            it.copy(
                nombreError = null,
                precioError = null,
                stockError = null,
                mensajeExito = null,
                operacion = ProductoOperacion.Inactiva
            )
        }
    }

    private fun limpiarFormulario() {
        _uiState.update {
            it.copy(
                nombre = "",
                precio = "",
                stock = "",
                activo = true,
                nombreError = null,
                precioError = null,
                stockError = null,
                productoEnEdicionId = null,
                categoriaEnEdicionId = null,
                categoriaSeleccionadaId = null,
                categoriaError = null
            )
        }
    }
}
