package pe.edu.upeu.pharmamobilee.data.firebase

import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import kotlin.test.*

class TelemetriaFirebaseTest {
    @Test fun conservaCodigoTecnicoSinMensajeCausaNiDatosPersonales() {
        var recibido: Throwable? = null
        var datos = emptyMap<String, String>()
        val destino = object : DestinoTelemetria {
            override fun error(error: Throwable, contexto: Map<String, String>) { recibido = error; datos = contexto }
            override fun evento(nombre: String, contexto: Map<String, String>) = Unit
        }
        TelemetriaFirebase(destino).registrarError(
            ErrorApiException(ErrorApi.Conflicto("dato privado"), IllegalStateException("token privado")),
            mapOf("operacion" to "crear_producto", "token" to "privado", "nombre" to "persona"))
        assertEquals("409", datos["codigo"])
        assertEquals(setOf("operacion", "codigo"), datos.keys)
        assertNull(recibido?.cause)
        assertFalse(recibido?.message.orEmpty().contains("privado"))
    }

    @Test fun unFalloDelProveedorNoRompeLaOperacionDeNegocio() {
        val destino = object : DestinoTelemetria {
            override fun error(error: Throwable, contexto: Map<String, String>): Unit = error("SDK no disponible")
            override fun evento(nombre: String, contexto: Map<String, String>): Unit = error("SDK no disponible")
        }
        val telemetria = TelemetriaFirebase(destino)
        telemetria.registrarError(IllegalStateException())
        telemetria.registrarEvento("sincronizacion_fallida")
    }

    @Test fun soloRegistraEventosTecnicosAdmitidos() {
        var contador = 0
        val destino = object : DestinoTelemetria {
            override fun error(error: Throwable, contexto: Map<String, String>) = Unit
            override fun evento(nombre: String, contexto: Map<String, String>) { contador++ }
        }
        val telemetria = TelemetriaFirebase(destino)
        telemetria.registrarEvento("mensaje_personal")
        telemetria.registrarEvento("sincronizacion_completada")
        assertEquals(1, contador)
    }
}
