package pe.edu.upeu.pharmamobilee.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobilee.db.PharmaDb
import pe.edu.upeu.pharmamobilee.domain.model.Producto

class ProductoLocal(driver: SqlDriver) {
    private val consultas = PharmaDb(driver).productoQueries

    fun observar(): Flow<List<Producto>> = consultas.listarTodos()
        .asFlow()
        .mapToList(Dispatchers.Default)
        .map { filas -> filas.map { it.aDominio() } }

    fun observarUltimaSincronizacion(): Flow<Long?> = consultas.obtenerUltimaSincronizacion()
        .asFlow()
        .mapToList(Dispatchers.Default)
        .map { filas -> filas.firstOrNull()?.ultima_correcta }

    fun pendientes(): List<Producto> = consultas.listarPendientes()
        .executeAsList()
        .map { it.aDominio() }

    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal().executeAsOne()
        val local = producto.copy(
            id = minOf(menor, 0L) - 1L,
            pendienteSincronizacion = true
        )
        consultas.insertar(local.id, local.nombre, local.precio, local.stock.toLong(), if (local.activo) 1L else 0L, 1L)
        return local
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()
            productos.forEach { producto ->
                consultas.insertar(producto.id, producto.nombre, producto.precio, producto.stock.toLong(), if (producto.activo) 1L else 0L, 0L)
            }
        }
    }

    fun actualizarPendiente(producto: Producto): Producto {
        val pendiente = producto.copy(pendienteSincronizacion = true)
        consultas.insertar(pendiente.id, pendiente.nombre, pendiente.precio, pendiente.stock.toLong(), if (pendiente.activo) 1L else 0L, 1L)
        return pendiente
    }

    fun guardarSincronizado(producto: Producto) {
        consultas.insertar(producto.id, producto.nombre, producto.precio, producto.stock.toLong(), if (producto.activo) 1L else 0L, 0L)
    }

    fun guardarUltimaSincronizacion(instanteMillis: Long) {
        consultas.guardarUltimaSincronizacion(instanteMillis)
    }

    fun borrarPendiente(id: Long) {
        consultas.borrarPorId(id)
    }

    fun eliminar(id: Long) {
        consultas.borrarPorId(id)
    }
}

private fun pe.edu.upeu.pharmamobilee.db.Producto.aDominio() = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock.toInt(),
    activo = activo != 0L,
    pendienteSincronizacion = pendiente != 0L
)
