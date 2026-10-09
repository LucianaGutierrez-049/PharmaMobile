package pe.edu.upeu.pharmamobilee.presentacion.producto

import pe.edu.upeu.pharmamobilee.presentation.producto.ProductoUi

enum class ProductoFiltro {
    Activos,
    Inactivos,
    BajoStock
}

fun filtrarProductosInventario(
    productos: List<ProductoUi>,
    filtro: ProductoFiltro
): List<ProductoUi> {
    return when (filtro) {
        ProductoFiltro.Activos -> productos.filter { it.activo }
        ProductoFiltro.Inactivos -> productos.filter { !it.activo }
        ProductoFiltro.BajoStock -> productos.filter { it.requiereReposicion }
    }
}
