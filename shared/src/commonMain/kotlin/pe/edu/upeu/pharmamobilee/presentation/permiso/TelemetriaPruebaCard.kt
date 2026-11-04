package pe.edu.upeu.pharmamobilee.presentation.permiso

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import pe.edu.upeu.pharmamobilee.domain.port.Telemetria

@Composable
fun TelemetriaPruebaCard() {
    val depuracion = koinInject<Boolean>(qualifier = named("esDepuracion"))
    if (!depuracion) return
    val telemetria = koinInject<Telemetria>()
    var solicitada by remember { mutableStateOf(false) }
    Column {
        OutlinedButton(enabled = !solicitada, onClick = {
            telemetria.registrarError(IllegalStateException("Prueba controlada"),
                mapOf("operacion" to "prueba_sesion13", "pantalla" to "productos", "tipo" to "prueba"))
            solicitada = true
        }) { Text("Registrar prueba de diagnóstico") }
        if (solicitada) Text("Prueba solicitada. Reinicia la app con Internet y comprueba Crashlytics en Firebase.")
    }
}
