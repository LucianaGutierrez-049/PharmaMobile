package pe.edu.upeu.pharmamobilee.domain.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobilee.data.productosSimulados
import pe.edu.upeu.pharmamobilee.domain.model.Producto

fun observarProductos(): Flow<List<Producto>> = flow {

    emit(emptyList())

    delay(1000)

    emit(productosSimulados)

    delay(1000)

    emit(
        productosSimulados.map { producto ->
            producto.copy(stock = producto.stock - 1)
        }
    )
}
