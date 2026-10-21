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
    val sincronizando: Boolean = false,
    val avisoRed: String? = null,
    val ultimaSincronizacionMillis: Long? = null,
    val operacion: ProductoOperacion = ProductoOperacion.Inactiva
) {
    // Inicio y el catálogo utilizan exactamente el mismo inventario, incluido
    // PharmaSoft (REMOTO). El origen no determina si un producto se contabiliza.
    val resumenInventario: ResumenInventarioUi
        get() = ResumenInventarioUi(
            total = productos.size,
            activos = productos.count { it.activo },
            inactivos = productos.count { !it.activo },
            bajoStock = productos.count { it.requiereReposicion }
        )
}

fun textoAntiguedadSincronizacion(ultimaMillis: Long?, ahoraMillis: Long): String {
    if (ultimaMillis == null) return "Nunca sincronizado"
    val minutos = ((ahoraMillis - ultimaMillis).coerceAtLeast(0L) / 60_000L)
    return when {
        minutos < 1 -> "Actualizado hace menos de un minuto"
        minutos == 1L -> "Actualizado hace 1 minuto"
        minutos < 60 -> "Actualizado hace $minutos minutos"
        minutos < 120 -> "Actualizado hace 1 hora"
        minutos < 1_440 -> "Actualizado hace ${minutos / 60} horas"
        minutos < 2_880 -> "Actualizado hace 1 día"
        else -> "Actualizado hace ${minutos / 1_440} días"
    }
}

data class ResumenInventarioUi(
    val total: Int,
    val activos: Int,
    val inactivos: Int,
    val bajoStock: Int
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
