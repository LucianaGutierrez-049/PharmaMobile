package pe.edu.upeu.pharmamobilee.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobilee.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobilee.domain.model.Credenciales
import pe.edu.upeu.pharmamobilee.domain.model.Sesion
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import io.ktor.client.plugins.ClientRequestException

class AuthRepositorioRestTest {
    @Test
    fun logoutBorraAlmacenYCacheDelMismoCliente() = runTest {
        val almacen = AlmacenPrueba("token.prueba.firma")
        val cabeceras = mutableListOf<String?>()
        val cliente = crearHttpClient(MockEngine { request ->
            val cabecera = request.headers[HttpHeaders.Authorization]
            cabeceras.add(cabecera)
            respond("{}", if (cabecera == null) HttpStatusCode.Unauthorized else HttpStatusCode.OK,
                headersOf(HttpHeaders.ContentType, "application/json"))
        }, "http://localhost/api/v1/", almacen)
        try {
            cliente.get("productos") // Carga deliberadamente la caché Bearer.
            AuthRepositorioRest(cliente, almacen).cerrarSesion()
            assertNull(almacen.leerToken())
            assertFailsWith<ClientRequestException> { cliente.get("productos") }
            assertEquals(listOf("Bearer token.prueba.firma", null), cabeceras)
        } finally { cliente.close() }
    }

    @Test
    fun restaurarSesionRechazadaEliminaTokenYCache() = runTest {
        val almacen = AlmacenPrueba("token.expirado.firma")
        val cabeceras = mutableListOf<String?>()
        val cliente = crearHttpClient(MockEngine { request ->
            cabeceras.add(request.headers[HttpHeaders.Authorization])
            respond("{}", HttpStatusCode.Unauthorized,
                headersOf(HttpHeaders.ContentType, "application/json"))
        }, "http://localhost/api/v1/", almacen)
        try {
            assertNull(AuthRepositorioRest(cliente, almacen).sesionGuardada())
            assertNull(almacen.leerToken())
            assertFailsWith<ClientRequestException> { cliente.get("productos") }
            assertNull(cabeceras.last())
        } finally { cliente.close() }
    }

    @Test
    fun loginGuardaTokenSinEnviarAuthorization() = runTest {
        val almacen = AlmacenPrueba()
        var authorizationLogin: String? = "no inspeccionado"
        val engine = MockEngine { request ->
            authorizationLogin = request.headers[HttpHeaders.Authorization]
            respond(
                content = LOGIN_ADMIN,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val cliente = crearHttpClient(engine, "http://localhost/api/v1/", almacen)
        val repositorio = AuthRepositorioRest(cliente, almacen)

        val sesion = repositorio.iniciarSesion(Credenciales("admin", "Admin123"))

        assertNull(authorizationLogin)
        assertEquals("token.prueba.firma", almacen.leerToken())
        assertEquals("admin", sesion.usuario)
        assertTrue(sesion.puede("CATALOGO_ESCRIBIR"))
    }

    @Test
    fun pluginAuthAdjuntaBearerAutomaticamente() = runTest {
        val almacen = AlmacenPrueba("token.prueba.firma")
        var authorization: String? = null
        val engine = MockEngine { request ->
            authorization = request.headers[HttpHeaders.Authorization]
            respond("{}", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }
        val cliente = crearHttpClient(engine, "http://localhost/api/v1/", almacen)

        cliente.get("productos")

        assertEquals("Bearer token.prueba.firma", authorization)
    }

    @Test
    fun sesionGuardadaConsultaAuthMe() = runTest {
        val almacen = AlmacenPrueba("token.prueba.firma")
        val engine = MockEngine { request ->
            assertEquals("/api/v1/auth/me", request.url.encodedPath)
            assertEquals("Bearer token.prueba.firma", request.headers[HttpHeaders.Authorization])
            respond(
                content = USUARIO_ADMIN,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val repositorio = AuthRepositorioRest(
            crearHttpClient(engine, "http://localhost/api/v1/", almacen),
            almacen
        )

        val sesion = repositorio.sesionGuardada()

        assertEquals("admin", sesion?.usuario)
        assertEquals(setOf("ADMIN"), sesion?.roles)
        assertTrue(sesion?.puede("USUARIOS_ADMINISTRAR") == true)
    }

    @Test
    fun sesionGuardadaSinRedUsaPerfilLocal() = runTest {
        val local = Sesion(
            token = "token.prueba.firma",
            usuario = "admin",
            nombreCompleto = "Carlos Ramirez Torres",
            roles = setOf("ADMIN"),
            permisos = setOf("CATALOGO_LEER")
        )
        val almacen = AlmacenPrueba(local.token, local)
        val cliente = crearHttpClient(
            MockEngine { throw kotlinx.io.IOException("sin red") },
            "http://localhost/api/v1/",
            almacen
        )

        val sesion = AuthRepositorioRest(cliente, almacen).sesionGuardada()

        assertEquals(local, sesion)
        assertEquals(local.token, almacen.leerToken())
    }

    private class AlmacenPrueba(
        private var token: String? = null,
        private var sesion: Sesion? = null
    ) : AlmacenSeguro {
        override fun guardarToken(token: String) { this.token = token }
        override fun leerToken(): String? = token
        override fun guardarSesion(sesion: Sesion) { this.sesion = sesion }
        override fun leerSesion(): Sesion? = sesion
        override fun borrarToken() {
            token = null
            sesion = null
        }
    }

    private companion object {
        const val LOGIN_ADMIN = """
            {
              "token":"token.prueba.firma",
              "tipo":"Bearer",
              "expiraEnSegundos":3600,
              "username":"admin",
              "nombreCompleto":"Carlos Ramirez Torres",
              "roles":["ADMIN"],
              "permisos":["CATALOGO_LEER","CATALOGO_ESCRIBIR"]
            }
        """
        const val USUARIO_ADMIN = """
            {
              "username":"admin",
              "empleadoNombre":"Carlos Ramirez Torres",
              "roles":["ADMIN"],
              "permisos":["CATALOGO_LEER","USUARIOS_ADMINISTRAR"]
            }
        """
    }
}
