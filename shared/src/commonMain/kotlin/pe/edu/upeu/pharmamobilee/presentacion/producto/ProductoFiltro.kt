package pe.edu.upeu.pharmamobilee.presentacion.producto

import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto

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
        ProductoFiltro.Activos -> productos.filter {
            it.origen == OrigenProducto.REMOTO || it.activo
        }
        ProductoFiltro.Inactivos -> productos.filter { !it.activo }
        ProductoFiltro.BajoStock -> productos.filter { it.requiereReposicion }
    }
}
