package pe.edu.upeu.pharmamobilee.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import pe.edu.upeu.pharmamobilee.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.ProductoResponseDto

class ProductoApi(
    private val client: HttpClient
) {
    suspend fun listar(
        pagina: Int = 0,
        tamanio: Int = 20
    ): PaginaResponseDto<ProductoResponseDto> =
        client.get("productos") {
            parameter("pagina", pagina)
            parameter("tamanio", tamanio)
        }.body()
}
