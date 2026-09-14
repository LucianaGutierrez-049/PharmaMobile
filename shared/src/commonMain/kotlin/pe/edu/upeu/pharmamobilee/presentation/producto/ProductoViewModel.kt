package pe.edu.upeu.pharmamobilee.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.domain.usecase.ProductoRegistroException
import pe.edu.upeu.pharmamobilee.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProductoUseCase: RegistrarProductoUseCase,
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

            val resultado = registrarProductoUseCase(
                nombre = uiState.value.nombre,
                precio = uiState.value.precio,
                stock = uiState.value.stock,
                activo = uiState.value.activo
            )

            resultado
                .onSuccess { producto ->
                    _uiState.update {
                        it.copy(
                            nombre = "",
                            precio = "",
                            stock = "",
                            activo = true,
                            mensajeExito = "Producto \"${producto.nombre}\" registrado correctamente"
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
                                    error.message ?: "No se pudo registrar el producto"
                                ),
                                mensajeExito = null
                            )
                        }
                    }
                }
        }
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(fase = ProductoFase.Cargando)
            }

            runCatching {
                productoRepository.listar()
            }.onSuccess { productos ->
                _uiState.update {
                    it.copy(
                        fase = if (productos.isEmpty()) {
                            ProductoFase.SinProductos
                        } else {
                            ProductoFase.ConProductos(productos)
                        }
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        fase = ProductoFase.Error(
                            error.message ?: "No se pudo cargar el inventario"
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
