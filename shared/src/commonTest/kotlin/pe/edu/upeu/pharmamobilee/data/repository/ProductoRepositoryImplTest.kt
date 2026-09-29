package pe.edu.upeu.pharmamobilee.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobilee.data.remote.ProductoApi
import pe.edu.upeu.pharmamobilee.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoRepositoryImplTest {

    @Test
    fun get200DeserializaMapeaYConservaProductosLocales() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/api/v1/products", request.url.encodedPath)
            assertEquals("10", request.url.parameters["limit"])
            respond(
                content = """
                    [{
                      "id": 90,
                      "title": "Producto remoto",
                      "price": 25.5,
                      "description": "Desde la API",
                      "images": ["https://example.com/90.png"],
                      "category": {"id": 4, "name": "Categoría API"},
                      "campo_nuevo": "se ignora"
                    }]
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val repository = ProductoRepositoryImpl(
            api = ProductoApi(crearHttpClient(engine)),
            repositorioLocal = ProductoRepositorioEnMemoria(emptyList())
        )

        val productos = repository.listar().getOrThrow()

        assertEquals(1, productos.size)
        assertEquals("Producto remoto", productos.single().nombre)
        assertEquals(OrigenProducto.REMOTO, productos.single().origen)
    }

    @Test
    fun respuesta500SeEntregaComoResultFailure() = runTest {
        val engine = MockEngine {
            respondError(HttpStatusCode.InternalServerError)
        }
        val repository = ProductoRepositoryImpl(
            api = ProductoApi(crearHttpClient(engine)),
            repositorioLocal = ProductoRepositorioEnMemoria(emptyList())
        )

        assertTrue(repository.listar().isFailure)
    }
}
