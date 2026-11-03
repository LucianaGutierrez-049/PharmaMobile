package pe.edu.upeu.pharmamobilee.presentation.permiso

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import pe.edu.upeu.pharmamobilee.domain.permiso.*

@Composable
fun PermisoNotificacionesCard() {
    val gestor = koinInject<GestorPermisos>()
    val scope = rememberCoroutineScope()
    var estado by remember { mutableStateOf<EstadoPermiso?>(null) }
    var solicitando by remember { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(gestor) { estado = gestor.estado(Permiso.NOTIFICACIONES) }
    DisposableEffect(lifecycle, gestor) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) scope.launch { estado = gestor.estado(Permiso.NOTIFICACIONES) }
        }
        lifecycle.addObserver(observador)
        onDispose { lifecycle.removeObserver(observador) }
    }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Notificaciones de inventario", style = MaterialTheme.typography.titleMedium)
            Text("Permite mostrar avisos del inventario en este dispositivo. Puedes continuar usando la app sin este permiso.")
            Text(when (estado) {
                EstadoPermiso.Concedido -> "Permiso concedido"
                EstadoPermiso.Denegado -> "Permiso pendiente o denegado: puedes solicitarlo."
                EstadoPermiso.DenegadoParaSiempre -> "Permiso bloqueado: habilítalo desde Ajustes."
                null -> "Consultando permiso…"
            })
            OutlinedButton(enabled = estado != null && !solicitando, onClick = {
                when (estado) {
                    EstadoPermiso.DenegadoParaSiempre -> gestor.abrirAjustes()
                    EstadoPermiso.Concedido -> gestor.mostrarNotificacionPrueba()
                    else -> scope.launch {
                        solicitando = true
                        try { estado = gestor.solicitar(Permiso.NOTIFICACIONES) }
                        finally { solicitando = false }
                    }
                }
            }) { Text(when (estado) {
                EstadoPermiso.Concedido -> "Probar notificación"
                EstadoPermiso.DenegadoParaSiempre -> "Abrir Ajustes"
                else -> "Permitir notificaciones"
            }) }
        }
    }
}
