package pe.edu.upeu.pharmamobilee.data.firebase

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.crashlytics.crashlytics
import pe.edu.upeu.pharmamobilee.domain.port.Telemetria
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException

internal interface DestinoTelemetria {
    fun error(error: Throwable, contexto: Map<String, String>)
    fun evento(nombre: String, contexto: Map<String, String>)
}

private object DestinoCrashlytics : DestinoTelemetria {
    override fun error(error: Throwable, contexto: Map<String, String>) {
        // Sobrescribir todas las claves para no arrastrar contexto de otro error.
        listOf("pantalla", "operacion", "codigo", "tipo").forEach {
            Firebase.crashlytics.setCustomKey(it, contexto[it].orEmpty())
        }
        Firebase.crashlytics.recordException(error)
    }
    override fun evento(nombre: String, contexto: Map<String, String>) {
        Firebase.crashlytics.log("$nombre $contexto")
    }
}

/** Solo admite contexto técnico conocido, sin tokens, nombres ni mensajes del servidor. */
class TelemetriaFirebase internal constructor(private val destino: DestinoTelemetria) : Telemetria {
    constructor() : this(DestinoCrashlytics)
    private fun filtrar(datos: Map<String, String>) = datos.filterKeys {
        it in setOf("pantalla", "operacion", "codigo", "tipo")
    }.mapValues { (_, valor) -> valor.take(80).filter { it.isLetterOrDigit() || it in "_.-" } }

    override fun registrarError(error: Throwable, contexto: Map<String, String>) {
        val api = (error as? ErrorApiException)?.error
        val codigo = when (api) {
            is ErrorApi.Validacion -> "400"
            ErrorApi.NoEncontrado -> "404"
            is ErrorApi.Conflicto -> "409"
            ErrorApi.SesionExpirada -> "401"
            ErrorApi.SinPermiso -> "403"
            ErrorApi.Servidor -> "servidor"
            ErrorApi.SinConexion -> "sin_conexion"
            ErrorApi.TiempoAgotado -> "timeout"
            null -> "inesperado"
        }
        runCatching {
            // El diagnóstico conserva la categoría técnica, nunca el mensaje ni la causa originales.
            destino.error(IllegalStateException("Fallo tecnico: $codigo"), filtrar(contexto + ("codigo" to codigo)))
        }
    }

    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        if (nombre !in setOf("sincronizacion_completada", "sincronizacion_fallida")) return
        runCatching { destino.evento(nombre, filtrar(datos)) }
    }
}
