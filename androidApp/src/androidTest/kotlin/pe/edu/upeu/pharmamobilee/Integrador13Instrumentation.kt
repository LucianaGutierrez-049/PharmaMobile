package pe.edu.upeu.pharmamobilee

import android.app.Activity
import android.app.Instrumentation
import android.os.Bundle
import android.os.SystemClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.core.context.GlobalContext
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.model.Credenciales
import pe.edu.upeu.pharmamobilee.data.local.ProductoLocal
import pe.edu.upeu.pharmamobilee.domain.port.Telemetria
import pe.edu.upeu.pharmamobilee.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.presentation.sesion.GestorSesion
import org.koin.core.qualifier.named
import java.io.File
import java.time.OffsetDateTime

/** Opt-in, usa la sesión guardada o una cuenta local de pruebas temporal. No exporta credenciales.
 * Crea un producto identificado de prueba y al terminar le aplica la baja lógica.
 * No toca los pendientes existentes ni los asigna a una cuenta.
 */
class Integrador13Instrumentation : Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }

    override fun onStart() {
        val resultado = JSONObject().put("fecha", OffsetDateTime.now().toString())
        try {
            val limite = SystemClock.elapsedRealtime() + 15_000
            while (GlobalContext.getOrNull() == null && SystemClock.elapsedRealtime() < limite) SystemClock.sleep(100)
            val koin = GlobalContext.get()
            runBlocking {
                val almacen = koin.get<AlmacenSeguro>()
                // Archivo opt-in recibido por stdin de ADB desde el entorno local.
                // Consumirlo y eliminarlo antes de iniciar solicitudes; nunca va al repositorio.
                val temporal = File(targetContext.filesDir, "s13_acceso_temporal.json")
                val acceso = if (temporal.exists()) try { JSONObject(temporal.readText()) }
                    finally { check(temporal.delete()) } else null
                val sesion = if (acceso != null) koin.get<AuthRepository>().iniciarSesion(
                    Credenciales(acceso.getString("usuario"), acceso.getString("clave")))
                    else koin.get<AuthRepository>().sesionGuardada()
                // Inicializar SQLDelight incluso si falta sesión para verificar la migración nativa.
                val anteriores = koin.get<ProductoLocal>().observarPendientesAnteriores().first()
                resultado.put("pendientes_anteriores_conservados", anteriores)
                if (sesion == null) {
                    resultado.put("requiere_inicio_sesion", true)
                    return@runBlocking
                }
                koin.get<GestorSesion>().iniciar(sesion)
                // Verifica sustitución del perfil sin imprimir datos ni token.
                try {
                    almacen.guardarSesion(sesion.copy(nombreCompleto = "Perfil de prueba 13"))
                    check(almacen.leerSesion()?.nombreCompleto == "Perfil de prueba 13")
                    almacen.guardarSesion(sesion.copy(nombreCompleto = "Perfil vigente 13"))
                    check(almacen.leerSesion()?.nombreCompleto == "Perfil vigente 13")
                    check(almacen.leerToken() == sesion.token)
                    resultado.put("perfil_actualizado_sin_perder_token", true)
                } finally { almacen.guardarSesion(sesion) }

                val remoto = koin.get<ProductoRepository>(named("repositorioRemoto"))
                val categorias = remoto.listarCategorias().getOrThrow()
                check(categorias.isNotEmpty()) { "No hay categorías para la prueba" }
                resultado.put("categorias_disponibles", categorias.size)
                    .put("categoria_1_existe", categorias.any { it.id == 1L })
                val categoria = categorias.first()
                resultado.put("categoria_prueba", categoria.id)
                val repo = koin.get<ProductoRepository>()
                repo.listarCategorias().getOrThrow()
                val nombre = "S13_VERIFICACION_${System.currentTimeMillis()}"
                var idPrueba: Long? = null
                try {
                    val creado = repo.registrar(Producto(0, nombre, 2.5, 10,
                        categoriaId = categoria.id, categoria = categoria.nombre)).getOrThrow()
                    check(creado.id > 0 && !creado.pendienteSincronizacion)
                    idPrueba = creado.id
                    check(remoto.obtener(creado.id).getOrThrow().categoriaId == categoria.id)
                    resultado.put("alta_remota_confirmada", true).put("id_producto_prueba", creado.id)
                    val local = repo.observar().first().first { it.id == creado.id }
                    check(local.categoriaId == categoria.id)
                    check(local.categoria == categoria.nombre)
                    resultado.put("categoria_conservada_sqlite", true)
                    repo.actualizar(local.copy(stock = 11)).getOrThrow()
                    check(remoto.obtener(creado.id).getOrThrow().stock == 11)
                    resultado.put("edicion_remota_confirmada", true)
                    val inexistente = remoto.registrar(local.copy(id = 0, nombre = nombre + "_404", categoriaId = Long.MAX_VALUE))
                    check((inexistente.exceptionOrNull() as? ErrorApiException)?.error == ErrorApi.NoEncontrado)
                    resultado.put("categoria_inexistente_devuelve_404", true)
                } finally {
                    idPrueba?.let { id ->
                        repo.eliminar(id).getOrThrow()
                        check(!remoto.obtener(id).getOrThrow().activo)
                        check(!repo.observar().first().first { it.id == id }.activo)
                        resultado.put("baja_logica_confirmada", true)
                    }
                }
                koin.get<Telemetria>().registrarError(IllegalStateException("Prueba controlada"),
                    mapOf("operacion" to "prueba_sesion13", "tipo" to "prueba", "pantalla" to "productos"))
                resultado.put("prueba_crashlytics_solicitada", true)
                resultado.put("recepcion_consola_crashlytics", "pendiente")
                resultado.put("aprobado", true)
            }
            guardar(resultado)
            finish(Activity.RESULT_OK, Bundle().apply { putString("resultado", resultado.toString()) })
        } catch (error: Throwable) {
            // Nunca exportar mensajes/cause de red o de autenticación.
            resultado.put("aprobado", false).put("tipo_error", error::class.simpleName)
            guardar(resultado)
            finish(Activity.RESULT_CANCELED, Bundle().apply { putString("resultado", resultado.toString()) })
        }
    }

    private fun guardar(resultado: JSONObject) {
        File(targetContext.filesDir, "evidencias_s13").apply { mkdirs() }
            .resolve("validacion_integracion.json").writeText(resultado.toString(2))
    }
}
