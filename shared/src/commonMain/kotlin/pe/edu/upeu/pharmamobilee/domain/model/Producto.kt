package pe.edu.upeu.pharmamobilee.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int = 0,
    val activo: Boolean = true,
    val descripcion: String = "",
    val imagen: String = "",
    val categoria: String = "Sin categoría",
    val categoriaId: Long? = null,
    val stockDisponible: Boolean = true,
    val origen: OrigenProducto = OrigenProducto.LOCAL
) {
    companion object {
        const val STOCK_MINIMO = 5
    }

    val requiereReposicion: Boolean
        get() = stockDisponible && stock <= STOCK_MINIMO
}

enum class OrigenProducto {
    LOCAL,
    REMOTO
}
