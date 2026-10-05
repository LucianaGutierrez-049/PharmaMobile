package pe.edu.upeu.pharmamobilee.data.repository

import pe.edu.upeu.pharmamobilee.data.mapper.toDomain
import pe.edu.upeu.pharmamobilee.data.mapper.toRequest
import pe.edu.upeu.pharmamobilee.data.remote.ProductoApi
import pe.edu.upeu.pharmamobilee.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.domain.repository.ProductoRepository

class ProductoRepositoryImpl(
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long
) : ProductoRepository {

    override suspend fun listar(): Result<List<Producto>> = ejecutarLlamada {
        api.listar().contenido.map { it.toDomain() }
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
