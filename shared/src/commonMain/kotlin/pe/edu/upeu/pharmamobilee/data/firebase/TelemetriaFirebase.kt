package pe.edu.upeu.pharmamobilee.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobilee.domain.port.Telemetria

/** Solo admite contexto técnico conocido, sin tokens, nombres ni mensajes del servidor. */
class TelemetriaFirebase : Telemetria {
    private fun filtrar(datos: Map<String, String>) = datos.filterKeys {
        it in setOf("pantalla", "operacion", "codigo", "tipo")
    }.mapValues { (_, valor) -> valor.take(80).filter { it.isLetterOrDigit() || it in "_.-" } }

    override fun registrarError(error: Throwable, contexto: Map<String, String>) {
        runCatching {
            filtrar(contexto).forEach { (clave, valor) -> Firebase.crashlytics.setCustomKey(clave, valor) }
            // No enviar el mensaje/cause original: puede contener datos del formulario o la API.
            Firebase.crashlytics.recordException(IllegalStateException("Fallo de sincronizacion"))
        }
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        if (nombre !in setOf("sincronizacion_completada", "sincronizacion_fallida")) return
        runCatching { Firebase.crashlytics.log("$nombre ${filtrar(datos)}") }
    }
}
