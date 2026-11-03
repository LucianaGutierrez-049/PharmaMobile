package pe.edu.upeu.pharmamobilee.domain.usecase

import kotlin.test.*
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobilee.domain.permiso.*

class GestorPermisosTest {
    @Test
    fun distinguePrimeraSolicitudDeBloqueoDefinitivo() {
        assertEquals(EstadoPermiso.Denegado, resolverEstadoPermiso(false, false, false))
        assertEquals(EstadoPermiso.Denegado, resolverEstadoPermiso(false, true, true))
        assertEquals(EstadoPermiso.DenegadoParaSiempre, resolverEstadoPermiso(false, true, false))
        assertEquals(EstadoPermiso.Concedido, resolverEstadoPermiso(true, true, false))
    }

    @Test
    fun elContratoPermitePruebasSinDispositivo() = runTest {
        val falso = object : GestorPermisos {
            var resultado: EstadoPermiso = EstadoPermiso.Denegado
            override suspend fun estado(permiso: Permiso) = resultado
            override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
                resultado = EstadoPermiso.Concedido
                return resultado
            }
            override fun abrirAjustes() = Unit
            override fun mostrarNotificacionPrueba() = Unit
        }
        assertEquals(EstadoPermiso.Denegado, falso.estado(Permiso.NOTIFICACIONES))
        falso.solicitar(Permiso.NOTIFICACIONES)
        assertEquals(EstadoPermiso.Concedido, falso.estado(Permiso.NOTIFICACIONES))
    }
}
