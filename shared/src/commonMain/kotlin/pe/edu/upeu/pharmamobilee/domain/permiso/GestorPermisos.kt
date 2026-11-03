package pe.edu.upeu.pharmamobilee.domain.permiso

enum class Permiso { NOTIFICACIONES }

sealed interface EstadoPermiso {
    data object Concedido : EstadoPermiso
    data object Denegado : EstadoPermiso
    data object DenegadoParaSiempre : EstadoPermiso
}

interface GestorPermisos {
    suspend fun estado(permiso: Permiso): EstadoPermiso
    suspend fun solicitar(permiso: Permiso): EstadoPermiso
    fun abrirAjustes()
    fun mostrarNotificacionPrueba()
}

fun resolverEstadoPermiso(concedido: Boolean, yaSolicitado: Boolean, procedeExplicar: Boolean): EstadoPermiso = when {
    concedido -> EstadoPermiso.Concedido
    !yaSolicitado || procedeExplicar -> EstadoPermiso.Denegado
    else -> EstadoPermiso.DenegadoParaSiempre
}
