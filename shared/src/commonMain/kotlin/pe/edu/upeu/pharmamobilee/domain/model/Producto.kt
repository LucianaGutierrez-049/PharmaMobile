package pe.edu.upeu.pharmamobilee.domain.model

data class Producto (
    val id:Long,
    val nombre:String,
    val precio:Double,
    val stock: Int,
    val activo: Boolean = true
){
    companion object {
        const val STOCK_MINIMO = 10
    }

    val requiereReposicion: Boolean
        get() = stock < STOCK_MINIMO
}
