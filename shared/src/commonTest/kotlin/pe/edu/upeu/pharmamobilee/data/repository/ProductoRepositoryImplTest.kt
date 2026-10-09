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
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductoRepositoryImplTest {

    @Test
    fun get200DeserializaObjetoPaginadoYListaSoloProductosDelBackend() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/api/v1/productos", request.url.encodedPath)
            assertEquals("0", request.url.parameters["pagina"])
            assertEquals("20", request.url.parameters["tamanio"])
            respond(
                content = """
                    {
                      "contenido": [{
                        "id": 90,
                        "nombre": "Producto remoto",
                        "precio": 25.5,
                        "stock": 17,
                        "estado": true,
                        "categoriaId": 4,
                        "categoriaNombre": "Categoría API",
                        "fechaCreacion": "2026-09-20T09:12:44",
                        "fechaModificacion": "2026-09-20T09:12:44",
                        "campo_nuevo": "se ignora"
                      }],
                      "pagina": 0,
                      "tamanio": 20,
                      "totalElementos": 1,
                      "totalPaginas": 1,
                      "ultima": true
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        val repository = ProductoRepositoryImpl(
            api = ProductoApi(crearHttpClient(engine, "http://localhost/api/v1/")),
            categoriaPorDefecto = 1L
        )

        val productos = repository.listar().getOrThrow()

        assertEquals(1, productos.size)
        assertEquals("Producto remoto", productos.single().nombre)
        assertEquals(17, productos.single().stock)
        assertEquals(OrigenProducto.REMOTO, productos.single().origen)
    }

    @Test
    fun respuesta500SeEntregaComoResultFailure() = runTest {
        val engine = MockEngine {
            respondError(HttpStatusCode.InternalServerError)
        }
        val repository = ProductoRepositoryImpl(
            api = ProductoApi(crearHttpClient(engine, "http://localhost/api/v1/")),
            categoriaPorDefecto = 1L
        )

        assertTrue(repository.listar().isFailure)
    }
}
