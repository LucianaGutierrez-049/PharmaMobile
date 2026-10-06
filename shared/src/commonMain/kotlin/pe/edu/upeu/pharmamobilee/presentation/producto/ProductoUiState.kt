package pe.edu.upeu.pharmamobilee.presentation.producto

data class ProductoUiState(
    val nombre: String = "",
    val precio: String = "",
    val stock: String = "",
    val activo: Boolean = true,
    val nombreError: String? = null,
    val precioError: String? = null,
    val stockError: String? = null,
    val mensajeExito: String? = null,
    val productoEnEdicionId: Long? = null,
    val categoriaEnEdicionId: Long? = null,
    val productos: List<ProductoUi> = emptyList(),
    val fase: ProductoFase = ProductoFase.Cargando,
    val operacion: ProductoOperacion = ProductoOperacion.Inactiva
)

sealed interface ProductoFase {
    data object Cargando : ProductoFase
    data object SinProductos : ProductoFase
    data object ConProductos : ProductoFase
    data class Error(val mensaje: String) : ProductoFase
}

sealed interface ProductoOperacion {
    data object Inactiva : ProductoOperacion
    data class EnCurso(val tipo: Tipo) : ProductoOperacion
    data class Fallida(val mensaje: String) : ProductoOperacion

    enum class Tipo {
        Crear,
        Actualizar,
        Eliminar
    }
}
