package pe.edu.upeu.pharmamobilee.data.remote

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import pe.edu.upeu.pharmamobilee.data.repository.ProductoRepositoryImpl
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConexionRestTest {

    @Test
    fun respuesta404SeConvierteEnMensajeEntendible() = runTest {
        val repository = crearRepositorio(
            engine = MockEngine {
                respondError(HttpStatusCode.NotFound)
            }
        )

        val error = repository.listar().exceptionOrNull()

        assertEquals(ErrorApi.NoEncontrado, (error as ErrorApiException).error)
        assertTrue(error.contieneCausa<ClientRequestException>())
    }

    @Test
    fun campoDesconocidoSeIgnoraConLaConfiguracionFinal() = runTest {
        val repository = crearRepositorio(
            engine = MockEngine {
                respond(
                    content = respuestaConCampoDesconocido,
                    status = HttpStatusCode.OK,
                    headers = cabecerasJson
                )
            }
        )

        val resultado = repository.listar()

        assertTrue(resultado.isSuccess)
        assertEquals("Producto de prueba", resultado.getOrThrow().single().nombre)
    }

    @Test
    fun campoDesconocidoFallaAlDesactivarTemporalmenteLaOpcion() = runTest {
        val repository = crearRepositorio(
            engine = MockEngine {
                respond(
                    content = respuestaConCampoDesconocido,
                    status = HttpStatusCode.OK,
                    headers = cabecerasJson
                )
            },
            ignorarCamposDesconocidos = false
        )

        val error = repository.listar().exceptionOrNull()

        assertEquals(ErrorApi.Servidor, (error as ErrorApiException).error)
    }

    @Test
    fun timeoutDeUnMilisegundoSeCapturaSinDejarEseValorEnProduccion() = runTest {
        val repository = crearRepositorio(
            engine = MockEngine {
                delay(100)
                respond(
                    content = respuestaConCampoDesconocido,
                    status = HttpStatusCode.OK,
                    headers = cabecerasJson
                )
            },
            tiempoEsperaSolicitudMillis = 1
        )

        val error = repository.listar().exceptionOrNull()

        assertEquals(ErrorApi.TiempoAgotado, (error as ErrorApiException).error)
    }

    private fun crearRepositorio(
        engine: MockEngine,
        tiempoEsperaSolicitudMillis: Long = 15_000,
        ignorarCamposDesconocidos: Boolean = true
    ): ProductoRepositoryImpl = ProductoRepositoryImpl(
        api = ProductoApi(
            crearHttpClient(
                engine = engine,
                urlBase = "http://localhost/api/v1/",
                tiempoEsperaSolicitudMillis = tiempoEsperaSolicitudMillis,
                ignorarCamposDesconocidos = ignorarCamposDesconocidos
            )
        ),
        categoriaPorDefecto = 1L
    )

    private companion object {
        val cabecerasJson = headersOf(
            HttpHeaders.ContentType,
            ContentType.Application.Json.toString()
        )

        val respuestaConCampoDesconocido = """
            {
              "contenido": [{
                "id": 1,
                "nombre": "Producto de prueba",
                "precio": 4.5,
                "stock": 10,
                "estado": true,
                "categoriaId": 1,
                "categoriaNombre": "Pruebas",
                "fechaCreacion": "2026-09-29T10:00:00",
                "fechaModificacion": null,
                "campoDesconocido": "valor adicional"
              }],
              "pagina": 0,
              "tamanio": 20,
              "totalElementos": 1,
              "totalPaginas": 1,
              "ultima": true
            }
        """.trimIndent()
    }
}

private inline fun <reified T : Throwable> Throwable?.contieneCausa(): Boolean {
    var actual = this
    while (actual != null) {
        if (actual is T) return true
        actual = actual.cause
    }
    return false
}
