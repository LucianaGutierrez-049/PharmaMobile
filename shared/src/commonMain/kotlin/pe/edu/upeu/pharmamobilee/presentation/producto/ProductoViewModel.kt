package pe.edu.upeu.pharmamobilee.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobilee.domain.error.ProductoCargaException
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobilee.domain.usecase.ProductoRegistroException
import pe.edu.upeu.pharmamobilee.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarProductoUseCase: ActualizarProductoUseCase,
    private val productoRepository: ProductoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun actualizarNombre(nombre: String) {
        _uiState.update {
            it.copy(
                nombre = nombre,
                nombreError = null,
                mensajeExito = null
            )
        }
    }

    fun actualizarPrecio(precio: String) {
        _uiState.update {
            it.copy(
                precio = precio,
                precioError = null,
                mensajeExito = null
            )
        }
    }

    fun actualizarStock(stock: String) {
        _uiState.update {
            it.copy(
                stock = stock,
                stockError = null,
                mensajeExito = null
            )
        }
    }

    fun actualizarActivo(activo: Boolean) {
        _uiState.update {
            it.copy(
                activo = activo,
                mensajeExito = null
            )
        }
    }

    fun registrarProducto() {
        viewModelScope.launch {
            limpiarErrores()

            val estado = uiState.value
            val resultado = estado.productoEnEdicionId?.let { id ->
                actualizarProductoUseCase(
                    id = id,
                    nombre = estado.nombre,
                    precio = estado.precio,
                    stock = estado.stock,
                    activo = estado.activo
                )
            } ?: registrarProductoUseCase(
                nombre = estado.nombre,
                precio = estado.precio,
                stock = estado.stock,
                activo = estado.activo
            )

            resultado
                .onSuccess { producto ->
                    _uiState.update {
                        it.copy(
                            nombre = "",
                            precio = "",
                            stock = "",
                            activo = true,
                            productoEnEdicionId = null,
                            mensajeExito = if (estado.productoEnEdicionId == null) {
                                "Producto \"${producto.nombre}\" registrado correctamente"
                            } else {
                                "Producto \"${producto.nombre}\" actualizado correctamente"
                            }
                        )
                    }
                    cargarProductos()
                }
                .onFailure { error ->
                    if (error is ProductoRegistroException) {
                        _uiState.update {
                            it.copy(
                                nombreError = error.nombreError,
                                precioError = error.precioError,
                                stockError = error.stockError,
                                mensajeExito = null
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                fase = ProductoFase.Error(
                                    error.message ?: "No se pudo guardar el producto"
                                ),
                                mensajeExito = null
                            )
                        }
                    }
                }
        }
    }

    fun editarProducto(producto: Producto) {
        if (producto.origen == OrigenProducto.REMOTO) return
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
                productoEnEdicionId = producto.id
            )
        }
    }

    fun cancelarEdicion() {
        _uiState.update {
            it.copy(
                nombre = "",
                precio = "",
                stock = "",
                activo = true,
                nombreError = null,
                precioError = null,
                stockError = null,
                productoEnEdicionId = null
            )
        }
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(fase = ProductoFase.Cargando)
            }

            productoRepository.listar().onSuccess { productos ->
                _uiState.update {
                    it.copy(
                        productos = productos,
                        fase = if (productos.isEmpty()) {
                            ProductoFase.SinProductos
                        } else {
                            ProductoFase.ConProductos
                        }
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        productos = emptyList(),
                        fase = ProductoFase.Error(
                            if (error is ProductoCargaException) {
                                error.message ?: "No se pudo cargar el inventario"
                            } else {
                                "No se pudo conectar con el servicio. Verifica tu conexión a Internet."
                            }
                        )
                    )
                }
            }
        }
    }

    private fun limpiarErrores() {
        _uiState.update {
            it.copy(
                nombreError = null,
                precioError = null,
                stockError = null,
                mensajeExito = null
            )
        }
    }
}
