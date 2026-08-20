package pe.edu.upeu.pharmamobilee.domain.result

import pe.edu.upeu.pharmamobilee.domain.model.Producto

sealed class ResultadoProductos {

    data object Cargando : ResultadoProductos()

    data class Exito(
        val productos: List<Producto>
    ) : ResultadoProductos()

    data class Error(
        val mensaje: String
    ) : ResultadoProductos()
}