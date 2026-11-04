package pe.edu.upeu.pharmamobilee.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobilee.db.PharmaDb
import pe.edu.upeu.pharmamobilee.domain.model.Categoria
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import pe.edu.upeu.pharmamobilee.domain.model.Producto

class ProductoLocal(private val driver: SqlDriver, private val propietario: String = "pruebas") {
    fun paraUsuario(usuario: String) = ProductoLocal(driver, usuario)

    private val consultas = PharmaDb(driver).productoQueries

    fun observar(): Flow<List<Producto>> = consultas.listarTodos(propietario)
        .asFlow()
        .mapToList(Dispatchers.Default)
        .map { filas -> filas.map { it.aDominio() } }

    fun observarUltimaSincronizacion(): Flow<Long?> = consultas.obtenerUltimaSincronizacion(propietario)
        .asFlow()
        .mapToList(Dispatchers.Default)
        .map { filas -> filas.firstOrNull()?.ultima_correcta }

    fun observarCategorias(): Flow<List<Categoria>> = consultas.listarCategorias(propietario)
        .asFlow().mapToList(Dispatchers.Default).map { filas -> filas.map { Categoria(it.id, it.nombre) } }

    fun observarPendientesAnteriores(): Flow<Long> = consultas.contarLegado()
        .asFlow().mapToList(Dispatchers.Default).map { it.firstOrNull() ?: 0L }

    fun recuperarPendientesAnteriores() = consultas.transaction {
        consultas.listarLegado().executeAsList().forEach { fila ->
            val nuevoId = minOf(consultas.siguienteIdLocal(propietario).executeAsOne(), 0L) - 1L
            consultas.asignarLegado(propietario, nuevoId, fila.id)
        }
    }

    fun guardarCategorias(categorias: List<Categoria>) = consultas.transaction {
        consultas.borrarCategorias(propietario)
        categorias.forEach { consultas.insertarCategoria(propietario, it.id, it.nombre) }
    }

    fun pendientes(): List<Producto> = consultas.listarPendientes(propietario)
        .executeAsList()
        .map { it.aDominio() }

    fun registrarFallo(id: Long, mensaje: String, revision: Boolean, proximo: Long) {
        consultas.registrarFallo(mensaje, if (revision) 1L else 0L, proximo, propietario, id)
    }

    fun prepararEnvio(id: Long) = consultas.prepararEnvio(propietario, id)
    fun reintentar(id: Long) = consultas.reintentar(propietario, id)

    fun confirmarEnvio(idLocal: Long, producto: Producto) {
        consultas.transaction {
            guardarSincronizado(producto)
            consultas.confirmarEnvio(propietario, idLocal)
        }
    }

    fun guardarPendiente(producto: Producto): Producto {
        val menor = consultas.siguienteIdLocal(propietario).executeAsOne()
        val local = producto.copy(
            id = minOf(menor, 0L) - 1L,
            pendienteSincronizacion = true
        )
        consultas.insertar(propietario, local.id, local.nombre, local.precio, local.stock.toLong(), if (local.activo) 1L else 0L, 1L, local.categoriaId, local.categoria)
        return local
    }

    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados(propietario)
            productos.forEach { producto ->
                consultas.insertar(propietario, producto.id, producto.nombre, producto.precio, producto.stock.toLong(), if (producto.activo) 1L else 0L, 0L, producto.categoriaId, producto.categoria)
            }
        }
    }

    fun actualizarPendiente(producto: Producto): Producto {
        val pendiente = producto.copy(pendienteSincronizacion = true)
        consultas.actualizarDatosPendientes(pendiente.nombre, pendiente.precio, pendiente.stock.toLong(), if (pendiente.activo) 1L else 0L, pendiente.categoriaId, pendiente.categoria, propietario, pendiente.id)
        return pendiente
    }

    fun guardarSincronizado(producto: Producto) {
        consultas.insertar(propietario, producto.id, producto.nombre, producto.precio, producto.stock.toLong(), if (producto.activo) 1L else 0L, 0L, producto.categoriaId, producto.categoria)
    }

    fun guardarUltimaSincronizacion(instanteMillis: Long) {
        consultas.guardarUltimaSincronizacion(propietario, instanteMillis)
    }

    fun borrarPendiente(id: Long) {
        consultas.borrarPorId(propietario, id)
    }

    fun eliminar(id: Long) {
        consultas.borrarPorId(propietario, id)
    }
}

private fun pe.edu.upeu.pharmamobilee.db.Producto.aDominio() = Producto(
    id = id,
    nombre = nombre,
    precio = precio,
    stock = stock.toInt(),
    activo = activo != 0L,
    categoriaId = categoriaId,
    categoria = categoria,
    origen = if (pendiente != 0L) OrigenProducto.LOCAL else OrigenProducto.REMOTO,
    pendienteSincronizacion = pendiente != 0L,
    intentos = intentos.toInt(),
    ultimoError = ultimoError,
    requiereRevision = requiereRevision != 0L,
    proximoIntentoMillis = proximoIntento
)
