package pe.edu.upeu.pharmamobilee.data.local

interface AlmacenSeguro {
    fun guardarToken(token: String)
    fun leerToken(): String?
    fun borrarToken()
}

internal object AlmacenSeguroVacio : AlmacenSeguro {
    override fun guardarToken(token: String) = Unit
    override fun leerToken(): String? = null
    override fun borrarToken() = Unit
}



