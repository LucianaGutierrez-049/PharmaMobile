package pe.edu.upeu.pharmamobilee

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobilee.data.local.ProductoLocal
import pe.edu.upeu.pharmamobilee.domain.model.Credenciales
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobilee.presentation.sesion.GestorSesion
import java.io.File
import java.time.OffsetDateTime
import java.util.concurrent.CopyOnWriteArrayList

/** Recorrido opt-in en un perfil Android aislado. No forma parte del APK de producción.
 * Solo exporta estados, IDs y códigos HTTP; nunca cabeceras, cuerpos de login ni tokens.
 */
class Evidencias13Instrumentation : Instrumentation() {
    private val respuestas = CopyOnWriteArrayList<JSONObject>()
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }

    private fun raiz(): AccessibilityNodeInfo {
        uiAutomation.clearCache()
        val limite = SystemClock.elapsedRealtime() + 5_000
        do {
            uiAutomation.rootInActiveWindow?.let { return it }
            SystemClock.sleep(200)
        } while (SystemClock.elapsedRealtime() < limite)
        error("No hay ventana accesible")
    }

    override fun onStart() {
        val carpeta = File(targetContext.filesDir, "evidencias_aa13").apply { mkdirs() }
        val comando = File(carpeta, "comando.json")
        val salida = File(carpeta, "respuesta.json")
        try {
            val limite = SystemClock.elapsedRealtime() + 30_000
            // startKoin publica el contexto antes de terminar de cargar sus módulos.
            var listo: HttpClient? = null
            while (listo == null && SystemClock.elapsedRealtime() < limite) {
                listo = runCatching { GlobalContext.get().get<HttpClient>() }.getOrNull()
                if (listo == null) SystemClock.sleep(100)
            }
            val koin = GlobalContext.get()
            val cliente = checkNotNull(listo)
            cliente.plugin(HttpSend).intercept { request ->
                execute(request).also { call ->
                    respuestas.add(JSONObject().put("metodo", request.method.value)
                        .put("ruta", request.url.build().encodedPath).put("http", call.response.status.value))
                }
            }
            startActivitySync(Intent(targetContext, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            salida.writeText(JSONObject().put("listo", true).toString())
            var continuar = true
            val fin = SystemClock.elapsedRealtime() + 3_600_000
            while (continuar && SystemClock.elapsedRealtime() < fin) {
                if (!comando.exists()) { SystemClock.sleep(100); continue }
                val entrada = try { JSONObject(comando.readText()) } finally { check(comando.delete()) }
                val resultado = JSONObject().put("id", entrada.getString("id"))
                    .put("fecha", OffsetDateTime.now().toString())
                try {
                    runBlocking {
                        val almacen = koin.get<AlmacenSeguro>()
                        when (entrada.getString("accion")) {
                            "login" -> {
                                val sesion = koin.get<AuthRepository>().iniciarSesion(
                                    Credenciales(entrada.getString("usuario"), entrada.getString("clave")))
                                runOnMainSync { koin.get<GestorSesion>().iniciar(sesion) }
                            }
                            "token_prueba" -> {
                                val sesion = checkNotNull(almacen.leerSesion())
                                val token = entrada.getString("token")
                                almacen.guardarToken(token)
                                almacen.guardarSesion(sesion.copy(token = token))
                                cliente.authProvider<BearerAuthProvider>()?.clearToken()
                            }
                            "intento_sin_permiso" -> {
                                // Se prueba la autorización del servicio incluso si la UI oculta el formulario.
                                val remoto = koin.get<ProductoRepository>(named("repositorioRemoto"))
                                val categorias = remoto.listarCategorias().getOrThrow()
                                val intento = remoto.registrar(Producto(0, "AA13_PROHIBIDO", 2.5, 1,
                                    categoriaId = categorias.first().id))
                                check((intento.exceptionOrNull() as? ErrorApiException)?.error == ErrorApi.SinPermiso)
                                resultado.put("operacion_rechazada_403", true)
                            }
                            "estado" -> Unit
                            "rellenar" -> {
                                val etiqueta = entrada.getString("campo")
                                val root = raiz()
                                fun buscar(nodo: AccessibilityNodeInfo): AccessibilityNodeInfo? {
                                    if (nodo.className?.toString()?.endsWith("EditText") == true) {
                                        val textos = mutableListOf<String>()
                                        fun recoger(n: AccessibilityNodeInfo) {
                                            n.text?.toString()?.let(textos::add)
                                            n.hintText?.toString()?.let(textos::add)
                                            repeat(n.childCount) { i -> n.getChild(i)?.let(::recoger) }
                                        }
                                        recoger(nodo)
                                        if (etiqueta in textos) return nodo
                                    }
                                    repeat(nodo.childCount) { i ->
                                        nodo.getChild(i)?.let { buscar(it) }?.let { return it }
                                    }
                                    return null
                                }
                                val campo = checkNotNull(buscar(root))
                                check(campo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,
                                    Bundle().apply { putCharSequence(
                                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                                        entrada.getString("valor")) }))
                            }
                            "ui" -> {
                                val nodos = JSONArray()
                                fun recorrer(n: AccessibilityNodeInfo) {
                                    val bounds = Rect().also(n::getBoundsInScreen)
                                    val text = if (n.isPassword) "" else n.text?.toString().orEmpty()
                                    val desc = n.contentDescription?.toString().orEmpty()
                                    if (text.isNotEmpty() || desc.isNotEmpty() ||
                                        n.className?.toString()?.endsWith("EditText") == true) {
                                        nodos.put(JSONObject().put("text", text).put("content-desc", desc)
                                            .put("class", n.className?.toString())
                                            .put("bounds", "[${bounds.left},${bounds.top}][${bounds.right},${bounds.bottom}]")
                                            .put("hint", n.hintText?.toString().orEmpty()))
                                    }
                                    repeat(n.childCount) { i -> n.getChild(i)?.let(::recorrer) }
                                }
                                recorrer(raiz())
                                resultado.put("ui", nodos)
                            }
                            "terminar" -> continuar = false
                            else -> error("Acción no admitida")
                        }
                        val sesion = almacen.leerSesion()
                        val cuenta = entrada.optString("cuenta", sesion?.usuario ?: "almacen")
                        val local = koin.get<ProductoLocal>().paraUsuario(cuenta)
                        val productos = local.observar().first()
                        resultado.put("token_guardado", almacen.leerToken() != null)
                            .put("perfil_guardado", sesion != null)
                            .put("sesion_ui", koin.get<GestorSesion>().estado.value.sesion != null)
                            .put("roles", JSONArray(sesion?.roles.orEmpty()))
                            .put("permisos", JSONArray(sesion?.permisos.orEmpty()))
                            .put("cuenta_consultada", cuenta)
                            .put("filas_cuenta", productos.size)
                            .put("pendientes", productos.count { it.pendienteSincronizacion })
                            .put("ultima_sincronizacion", local.observarUltimaSincronizacion().first())
                            .put("productos", JSONArray(productos.map { p -> JSONObject()
                                .put("id", p.id).put("nombre", p.nombre)
                                .put("pendiente", p.pendienteSincronizacion)
                                .put("revision", p.requiereRevision).put("intentos", p.intentos)
                                .put("categoria", p.categoriaId).put("activo", p.activo) }))
                        SQLiteDatabase.openDatabase(targetContext.getDatabasePath("pharma.db").path,
                            null, SQLiteDatabase.OPEN_READONLY).use { db ->
                            db.rawQuery("SELECT COUNT(*) FROM producto", null).use { cursor ->
                                cursor.moveToFirst(); resultado.put("filas_totales_sqlite", cursor.getLong(0))
                            }
                        }
                        resultado.put("http", JSONArray(respuestas.toList()))
                            .put("aprobado", true)
                    }
                } catch (error: Throwable) {
                    resultado.put("aprobado", false).put("tipo_error", error::class.simpleName)
                }
                val temporal = File(carpeta, "respuesta.tmp")
                temporal.writeText(resultado.toString(2))
                check(temporal.renameTo(salida))
            }
            finish(Activity.RESULT_OK, Bundle())
        } catch (error: Throwable) {
            salida.writeText(JSONObject().put("aprobado", false)
                .put("tipo_error", error::class.simpleName).toString())
            finish(Activity.RESULT_CANCELED, Bundle())
        } finally { comando.delete() }
    }
}
