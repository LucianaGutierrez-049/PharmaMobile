package pe.edu.upeu.pharmamobilee.data.local

import pe.edu.upeu.pharmamobilee.domain.model.Sesion

interface AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun guardarSesion(sesion: Sesion) = Unit
    fun leerSesion(): Sesion? = null
    fun borrarToken()
}

internal object AlmacenSeguroVacio : AlmacenSeguro {
    override fun guardarToken(token: String) = Unit
    override fun leerToken(): String? = null
    override fun borrarToken() = Unit
}



