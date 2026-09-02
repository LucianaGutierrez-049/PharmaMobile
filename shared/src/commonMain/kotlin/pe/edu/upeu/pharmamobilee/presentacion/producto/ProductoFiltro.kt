package pe.edu.upeu.pharmamobilee.presentacion.producto

import pe.edu.upeu.pharmamobilee.domain.model.Producto

enum class ProductoFiltro {
    Activos,
    Inactivos,
    BajoStock
}

fun filtrarProductosInventario(
    productos: List<Producto>,
    filtro: ProductoFiltro
): List<Producto> {
    return when (filtro) {
        ProductoFiltro.Activos -> productos.filter { it.activo }
        ProductoFiltro.Inactivos -> productos.filter { !it.activo }
        ProductoFiltro.BajoStock -> productos.filter { it.stock <= 5 }
    }
}
