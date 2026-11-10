package pe.edu.upeu.pharmamobilee.data.repository

import pe.edu.upeu.pharmamobilee.data.remote.ComercialApi
import pe.edu.upeu.pharmamobilee.data.remote.dto.ClienteRequestDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.ClienteResponseDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.DetalleVentaRequestDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.VentaRequestDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.VentaResponseDto
import pe.edu.upeu.pharmamobilee.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobilee.domain.model.Cliente
import pe.edu.upeu.pharmamobilee.domain.model.DetalleVenta
import pe.edu.upeu.pharmamobilee.domain.model.LineaVenta
import pe.edu.upeu.pharmamobilee.domain.model.Venta
import pe.edu.upeu.pharmamobilee.domain.repository.ClienteRepository
import pe.edu.upeu.pharmamobilee.domain.repository.VentaRepository

/** Recorre todas las páginas: no confunde la primera página con el total. */
internal suspend fun <T> todasLasPaginas(pedir: suspend (Int) -> PaginaResponseDto<T>): List<T> = buildList {
    var pagina = 0
    while (true) {
        val respuesta = pedir(pagina)
        addAll(respuesta.contenido)
        if (respuesta.ultima || respuesta.contenido.isEmpty()) break
        pagina++
        if (pagina >= respuesta.totalPaginas) throw ErrorApiException(ErrorApi.Servidor)
    }
}

fun ClienteResponseDto.toDomain() = Cliente(
    id = id, dni = dni, nombres = nombres, apellidos = apellidos, email = email,
    telefono = telefono, direccion = direccion, activo = estado
)

fun Cliente.toRequest() = ClienteRequestDto(
    dni = dni.trim(),
    nombres = nombres.trim(),
    apellidos = apellidos.trim(),
    email = email.trim(),
    telefono = telefono?.trim()?.takeIf { it.isNotEmpty() },
    direccion = direccion?.trim()?.takeIf { it.isNotEmpty() },
    estado = activo
)

fun VentaResponseDto.toDomain() = Venta(
    id = id,
    fecha = fecha,
    clienteId = clienteId,
    clienteNombre = clienteNombre ?: "Cliente #$clienteId",
    estado = estado,
    total = total,
    detalles = detalles.map {
        DetalleVenta(it.productoId, it.productoNombre ?: "Producto #${it.productoId}", it.cantidad, it.precio, it.subtotal)
    }
)

/** Agrupa productos repetidos en una sola línea, como espera el descuento de stock del servidor. */
fun agruparLineas(lineas: List<LineaVenta>): List<DetalleVentaRequestDto> =
    lineas.groupBy { it.producto.id }.map { (id, grupo) -> DetalleVentaRequestDto(id, grupo.sumOf { it.cantidad }) }

class ClienteRepositorioRest(private val api: ComercialApi) : ClienteRepository {
    override suspend fun listar() = ejecutarLlamada { todasLasPaginas(api::clientes).map { it.toDomain() } }
    override suspend fun registrar(cliente: Cliente) = ejecutarLlamada { api.crearCliente(cliente.toRequest()).toDomain() }
    override suspend fun actualizar(cliente: Cliente) =
        ejecutarLlamada { api.actualizarCliente(cliente.id, cliente.toRequest()).toDomain() }
    override suspend fun darDeBaja(id: Long) = ejecutarLlamada { api.eliminarCliente(id) }
}

class VentaRepositorioRest(private val api: ComercialApi) : VentaRepository {
    override suspend fun listar() = ejecutarLlamada { todasLasPaginas(api::ventas).map { it.toDomain() } }
    override suspend fun registrar(clienteId: Long, lineas: List<LineaVenta>) = ejecutarLlamada {
        api.registrarVenta(VentaRequestDto(clienteId, agruparLineas(lineas))).toDomain()
    }
}
