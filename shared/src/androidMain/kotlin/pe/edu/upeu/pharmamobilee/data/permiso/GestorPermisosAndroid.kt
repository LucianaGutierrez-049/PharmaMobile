package pe.edu.upeu.pharmamobilee.data.permiso

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobilee.domain.permiso.*

class GestorPermisosAndroid(private val contexto: Context) : GestorPermisos {
    private val preferencias = contexto.getSharedPreferences("permisos", Context.MODE_PRIVATE)
    private var actividad: ComponentActivity? = null
    private var lanzador: ActivityResultLauncher<String>? = null
    private var respuesta: CompletableDeferred<Boolean>? = null
    private val mutex = Mutex()

    fun conectar(activity: ComponentActivity) {
        actividad = activity
        lanzador = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            respuesta?.complete(it)
        }
    }

    fun desconectar(activity: ComponentActivity) {
        if (actividad !== activity) return
        respuesta?.cancel()
        respuesta = null
        lanzador = null
        actividad = null
    }

    override suspend fun estado(permiso: Permiso): EstadoPermiso {
        val nombre = Manifest.permission.POST_NOTIFICATIONS
        val concedido = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(contexto, nombre) == PackageManager.PERMISSION_GRANTED
        if (concedido) return if (NotificationManagerCompat.from(contexto).areNotificationsEnabled())
            EstadoPermiso.Concedido else EstadoPermiso.DenegadoParaSiempre
        val activity = actividad ?: return EstadoPermiso.Denegado
        return resolverEstadoPermiso(false, preferencias.getBoolean(nombre, false),
            ActivityCompat.shouldShowRequestPermissionRationale(activity, nombre))
    }

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso = mutex.withLock {
        if (estado(permiso) != EstadoPermiso.Denegado || Build.VERSION.SDK_INT < 33)
            return@withLock estado(permiso)
        val actual = lanzador ?: return@withLock EstadoPermiso.Denegado
        val espera = CompletableDeferred<Boolean>()
        respuesta = espera
        try {
            preferencias.edit().putBoolean(Manifest.permission.POST_NOTIFICATIONS, true).apply()
            actual.launch(Manifest.permission.POST_NOTIFICATIONS)
            espera.await()
            estado(permiso)
        } finally { respuesta = null }
    }

    override fun abrirAjustes() {
        contexto.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${contexto.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun mostrarNotificacionPrueba() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(contexto,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel("inventario", "Inventario", NotificationManager.IMPORTANCE_DEFAULT))
        manager.notify(13, NotificationCompat.Builder(contexto, "inventario")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("PharmaMobil")
            .setContentText("Las notificaciones de inventario están habilitadas.")
            .setAutoCancel(true).build())
    }
}
