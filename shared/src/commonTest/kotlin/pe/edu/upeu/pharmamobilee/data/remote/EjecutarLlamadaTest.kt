package pe.edu.upeu.pharmamobilee.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobilee.data.repository.ProductoRepositoryImpl
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EjecutarLlamadaTest {

    @Test
    fun respuesta400RecuperaErroresPorCampo() = runTest {
        val repository = repositoryConRespuesta(
            status = HttpStatusCode.BadRequest,
            content = """
                {
                  "status": 400,
                  "message": "Existen errores de validación",
                  "validationErrors": {
                    "nombre": "El nombre debe tener entre 3 y 150 caracteres"
                  }
                }
            """.trimIndent()
        )

        val exception = repository.registrar(productoPrueba)
            .exceptionOrNull() as ErrorApiException
        val error = exception.error as ErrorApi.Validacion

        assertEquals(
            "El nombre debe tener entre 3 y 150 caracteres",
            error.porCampo["nombre"]
        )
    }

    @Test
    fun respuesta409ConservaMensajeDelServidor() = runTest {
        val repository = repositoryConRespuesta(
            status = HttpStatusCode.Conflict,
            content = """{"status":409,"message":"El producto ya existe"}"""
        )

        val exception = repository.registrar(productoPrueba)
            .exceptionOrNull() as ErrorApiException

        assertEquals(
            ErrorApi.Conflicto("El producto ya existe"),
            exception.error
        )
    }

    @Test
    fun cancellationExceptionSiempreSeRelanza() = runTest {
        assertFailsWith<CancellationException> {
            ejecutarLlamada<Unit> { throw CancellationException("cancelada") }
        }
    }

    private fun repositoryConRespuesta(
        status: HttpStatusCode,
        content: String
    ): ProductoRepositoryImpl {
        val engine = MockEngine {
            respond(
                content = content,
                status = status,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    ContentType.Application.Json.toString()
                )
            )
        }
        return ProductoRepositoryImpl(
            api = ProductoApi(crearHttpClient(engine, "http://localhost/api/v1/")),
            categoriaPorDefecto = 1L
        )
    }

    private companion object {
        val productoPrueba = pe.edu.upeu.pharmamobilee.domain.model.Producto(
            id = 0,
            nombre = "AB",
            precio = 2.0,
            stock = 1
        )
    }
}
