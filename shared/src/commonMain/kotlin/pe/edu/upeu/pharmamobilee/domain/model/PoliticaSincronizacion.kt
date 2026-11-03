package pe.edu.upeu.pharmamobilee.domain.model

object PoliticaSincronizacion {
    const val MAX_INTENTOS = 5
    fun esperaMillis(intentos: Int): Long = 1_000L shl intentos.coerceIn(0, 4)
    fun puedeEnviar(producto: Producto, ahora: Long): Boolean =
        producto.pendienteSincronizacion && !producto.requiereRevision &&
            producto.intentos < MAX_INTENTOS && producto.proximoIntentoMillis <= ahora
}
