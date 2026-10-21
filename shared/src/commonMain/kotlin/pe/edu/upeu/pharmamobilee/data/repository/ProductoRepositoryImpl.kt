package pe.edu.upeu.pharmamobilee.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobilee.data.mapper.toDomain
import pe.edu.upeu.pharmamobilee.data.mapper.toRequest
import pe.edu.upeu.pharmamobilee.data.remote.ProductoApi
import pe.edu.upeu.pharmamobilee.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApi
import pe.edu.upeu.pharmamobilee.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

class ProductoRepositoryImpl(
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = flow {
        emit(listar().getOrDefault(emptyList()))
    }

    override suspend fun sincronizar(): Result<Unit> = listar().map { Unit }

    override suspend fun listar(): Result<List<Producto>> = ejecutarLlamada {
        // No confundir la primera página (20 registros) con todo el inventario.
        buildList {
            var pagina = 0
            while (true) {
                val respuesta = api.listar(pagina = pagina)
                if (respuesta.pagina != pagina) throw ErrorApiException(ErrorApi.Servidor)
                addAll(respuesta.contenido.map { it.toDomain() })
                if (respuesta.ultima) break
                if (respuesta.contenido.isEmpty() || pagina + 1 >= respuesta.totalPaginas)
                    throw ErrorApiException(ErrorApi.Servidor)
                pagina++
            }
        }
    }

    override suspend fun obtener(id: Long): Result<Producto> = ejecutarLlamada {
        api.obtener(id).toDomain()
    }

    override suspend fun registrar(producto: Producto): Result<Producto> =
        ejecutarLlamada {
            api.crear(producto.toRequest(categoriaPorDefecto)).toDomain()
        }

    override suspend fun actualizar(producto: Producto): Result<Producto> =
        ejecutarLlamada {
            api.actualizar(
                id = producto.id,
                request = producto.toRequest(categoriaPorDefecto)
            ).toDomain()
        }

    override suspend fun eliminar(id: Long): Result<Unit> = ejecutarLlamada {
        api.eliminar(id)
    }
}
