package pe.edu.upeu.pharmamobilee.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import pe.edu.upeu.pharmamobilee.data.remote.dto.ClienteRequestDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.ClienteResponseDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.VentaRequestDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.VentaResponseDto

/** Clientes y ventas de PharmaSoft (/api/v1/clientes y /api/v1/ventas). */
class ComercialApi(private val client: HttpClient) {
    suspend fun clientes(pagina: Int): PaginaResponseDto<ClienteResponseDto> =
        client.get("clientes") {
            parameter("pagina", pagina)
            parameter("tamanio", 100)
            parameter("ordenarPor", "apellidos")
            parameter("direccion", "asc")
        }.body()

    suspend fun crearCliente(request: ClienteRequestDto): ClienteResponseDto =
        client.post("clientes") { setBody(request) }.body()

    suspend fun actualizarCliente(id: Long, request: ClienteRequestDto): ClienteResponseDto =
        client.put("clientes/$id") { setBody(request) }.body()

    suspend fun eliminarCliente(id: Long) {
        client.delete("clientes/$id")
    }

    suspend fun ventas(pagina: Int): PaginaResponseDto<VentaResponseDto> =
        client.get("ventas") {
            parameter("pagina", pagina)
            parameter("tamanio", 50)
            parameter("ordenarPor", "fecha")
            parameter("direccion", "desc")
        }.body()

    suspend fun registrarVenta(request: VentaRequestDto): VentaResponseDto =
        client.post("ventas") { setBody(request) }.body()
}
