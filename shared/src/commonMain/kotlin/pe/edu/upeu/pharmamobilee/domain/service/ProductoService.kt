package pe.edu.upeu.pharmamobilee.domain.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobilee.domain.result.ResultadoProductos
import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobilee.data.productosSimulados
import pe.edu.upeu.pharmamobilee.domain.model.Producto

suspend fun obtenerProductos(): List<Producto> {
    delay(1000)
    return productosSimulados
}

fun cargarProductos(): Flow<ResultadoProductos> = flow {

    emit(ResultadoProductos.Cargando)

    try {
        val productos = obtenerProductos()

        emit(
            ResultadoProductos.Exito(productos)
        )

    } catch (e: Exception) {

        emit(
            ResultadoProductos.Error(
                e.message ?: "Error al cargar los productos"
            )
        )
    }
}