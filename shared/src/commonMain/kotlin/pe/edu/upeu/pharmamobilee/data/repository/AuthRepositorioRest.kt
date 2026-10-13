package pe.edu.upeu.pharmamobilee.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobilee.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobilee.data.mapper.toDomain
import pe.edu.upeu.pharmamobilee.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobilee.data.remote.dto.UsuarioActualDto
import pe.edu.upeu.pharmamobilee.domain.error.ErrorSesion
import pe.edu.upeu.pharmamobilee.domain.error.ErrorSesionException
import pe.edu.upeu.pharmamobilee.domain.model.Credenciales
import pe.edu.upeu.pharmamobilee.domain.model.Sesion
import pe.edu.upeu.pharmamobilee.domain.repository.AuthRepository

class AuthRepositorioRest(
    private val cliente: HttpClient,
    private val almacen: AlmacenSeguro
) : AuthRepository {

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion = proteger {
        val dto: LoginResponseDto = cliente.post("auth/login") {
            setBody(LoginRequestDto(credenciales.usuario, credenciales.clave))
        }.body()
        almacen.guardarToken(dto.token)
        limpiarCacheBearer()
        dto.toDomain()
    }

    override suspend fun sesionGuardada(): Sesion? {
        val token = almacen.leerToken() ?: return null
        return try {
            val usuario: UsuarioActualDto = cliente.get("auth/me").body()
            usuario.toDomain(token)
        } catch (error: ClientRequestException) {
            if (error.response.status.value == 401) {
                cerrarSesion()
                null
            } else {
                throw traducir(error)
            }
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (error: Throwable) {
            throw traducir(error)
        }
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        limpiarCacheBearer()
    }

    private fun limpiarCacheBearer() {
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
    }

    private suspend fun <T> proteger(bloque: suspend () -> T): T = try {
        bloque()
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (error: Throwable) {
        throw traducir(error)
    }

    private fun traducir(error: Throwable): ErrorSesionException = when (error) {
        is ErrorSesionException -> error
        is ClientRequestException -> when (error.response.status.value) {
            401 -> ErrorSesionException(ErrorSesion.CredencialesInvalidas, error)
            403 -> ErrorSesionException(ErrorSesion.SinPermiso, error)
            else -> ErrorSesionException(ErrorSesion.SinConexion, error)
        }
        is ServerResponseException,
        is HttpRequestTimeoutException,
        is IOException -> ErrorSesionException(ErrorSesion.SinConexion, error)
        else -> ErrorSesionException(ErrorSesion.SinConexion, error)
    }
}
