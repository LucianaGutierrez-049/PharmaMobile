package pe.edu.upeu.pharmamobilee.domain.repository

import pe.edu.upeu.pharmamobilee.domain.model.Cliente
import pe.edu.upeu.pharmamobilee.domain.model.LineaVenta
import pe.edu.upeu.pharmamobilee.domain.model.Venta

interface ClienteRepository {
    suspend fun listar(): Result<List<Cliente>>
    suspend fun registrar(cliente: Cliente): Result<Cliente>
    suspend fun actualizar(cliente: Cliente): Result<Cliente>
    /** PharmaSoft hace baja lógica: el cliente queda inactivo. */
    suspend fun darDeBaja(id: Long): Result<Unit>
}

interface VentaRepository {
    suspend fun listar(): Result<List<Venta>>
    suspend fun registrar(clienteId: Long, lineas: List<LineaVenta>): Result<Venta>
}
