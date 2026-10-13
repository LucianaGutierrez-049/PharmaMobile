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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositorioRestTest {
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

    private class AlmacenPrueba(private var token: String? = null) : AlmacenSeguro {
        override fun guardarToken(token: String) { this.token = token }
        override fun leerToken(): String? = token
        override fun borrarToken() { token = null }
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
