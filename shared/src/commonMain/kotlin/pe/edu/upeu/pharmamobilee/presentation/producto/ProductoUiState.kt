package pe.edu.upeu.pharmamobilee.presentation.producto

import pe.edu.upeu.pharmamobilee.domain.model.Producto

data class ProductoUiState(
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val activo: Boolean = true,
    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null,
    val mensajeExito: String? = null,
    val fase: ProductoFase = ProductoFase.Cargando
)

sealed interface ProductoFase {
    data object Cargando : ProductoFase
    data object SinProductos : ProductoFase
    data class ConProductos(val productos: List<Producto>) : ProductoFase
    data class Error(val mensaje: String) : ProductoFase
}
