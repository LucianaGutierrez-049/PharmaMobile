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
import pe.edu.upeu.pharmamobilee.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.comoTextoParaCompartir
import pe.edu.upeu.pharmamobilee.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.ProductoRegistroException
import pe.edu.upeu.pharmamobilee.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val listarProductosUseCase: ListarProductosUseCase,
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarProductoUseCase: ActualizarProductoUseCase,
    private val eliminarProductoUseCase: EliminarProductoUseCase,
    private val compartidor: Compartidor
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarProductos()
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
                    categoriaId = estado.categoriaEnEdicionId
                )
            } ?: registrarProductoUseCase(
                nombre = estado.nombre,
                precio = estado.precio,
                stock = estado.stock,
                activo = estado.activo
            )

            val producto = resultado.getOrElse { error ->
                manejarFalloOperacion(error)
                return@launch
            }

            limpiarFormulario()
            recargarTrasOperacion(
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
            recargarTrasOperacion("Producto eliminado correctamente")
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
                categoriaEnEdicionId = producto.categoriaId
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

    private fun manejarFalloOperacion(fallo: Throwable) {
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
                        stockError = error.porCampo["stock"]
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
                categoriaEnEdicionId = null
            )
        }
    }
}
