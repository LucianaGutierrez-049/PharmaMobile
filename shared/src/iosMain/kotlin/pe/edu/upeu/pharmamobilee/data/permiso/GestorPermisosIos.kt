@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package pe.edu.upeu.pharmamobilee.data.permiso

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.*
import pe.edu.upeu.pharmamobilee.domain.permiso.*

class GestorPermisosIos : GestorPermisos {
    private val centro get() = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun estado(permiso: Permiso): EstadoPermiso = suspendCancellableCoroutine { continuation ->
        centro.getNotificationSettingsWithCompletionHandler { configuracion ->
            val estado = when (configuracion?.authorizationStatus) {
                UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional,
                UNAuthorizationStatusEphemeral -> EstadoPermiso.Concedido
                UNAuthorizationStatusDenied -> EstadoPermiso.DenegadoParaSiempre
                else -> EstadoPermiso.Denegado
            }
            if (continuation.isActive) continuation.resume(estado)
        }
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        suspendCancellableCoroutine<Unit> { continuation ->
            centro.requestAuthorizationWithOptions(UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge) { _, _ ->
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
        return estado(permiso)
    }

    override fun abrirAjustes() {
        NSURL.URLWithString(UIApplicationOpenSettingsURLString)?.let {
            UIApplication.sharedApplication.openURL(it, emptyMap<Any?, Any?>(), null)
        }
    }

    override fun mostrarNotificacionPrueba() {
        val contenido = UNMutableNotificationContent().apply {
            setTitle("PharmaMobil")
            setBody("Las notificaciones de inventario están habilitadas.")
        }
        val solicitud = UNNotificationRequest.requestWithIdentifier("inventario-prueba", contenido,
            UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(2.0, false))
        centro.addNotificationRequest(solicitud, null)
    }
}
