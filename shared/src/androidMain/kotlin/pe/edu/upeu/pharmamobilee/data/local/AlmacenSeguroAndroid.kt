package pe.edu.upeu.pharmamobilee.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobilee.domain.model.Sesion
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {
    private val preferencias = contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override fun guardarToken(token: String) {
        // El almacén es exclusivo de la sesión. Nombre y valor usan nonces
        // independientes; la clave AES nunca sale de Android Keystore.
        check(preferencias.edit().clear()
            .putString(cifrar(CLAVE_TOKEN), cifrar(token)).commit()) {
            "No se pudo guardar la sesión de forma segura."
        }
    }

    override fun leerToken(): String? {
        return runCatching {
            val anterior = preferencias.getString(CLAVE_TOKEN, null)
            if (anterior != null) {
                // Migración del formato anterior sin cerrar sesiones existentes.
                val iv = preferencias.getString(CLAVE_IV, null)
                    ?: error("Falta el vector de inicialización")
                val token = descifrar(Base64.decode(iv, Base64.NO_WRAP) +
                    Base64.decode(anterior, Base64.NO_WRAP))
                guardarToken(token)
                token
            } else {
                preferencias.all.entries.firstOrNull {
                    runCatching { descifrar(Base64.decode(it.key, Base64.NO_WRAP)) == CLAVE_TOKEN }.getOrDefault(false)
                }?.value?.let { descifrar(Base64.decode(it as String, Base64.NO_WRAP)) }
            }
        }.getOrElse {
            borrarToken()
            null
        }
    }

    override fun guardarSesion(sesion: Sesion) {
        val editor = preferencias.edit()
        preferencias.all.keys.filter { clave ->
            clave == CLAVE_PERFIL || runCatching {
                descifrar(Base64.decode(clave, Base64.NO_WRAP)) == CLAVE_SESION
            }.getOrDefault(false)
        }.forEach { editor.remove(it) }
        check(editor.putString(CLAVE_PERFIL, cifrar(Json.encodeToString(sesion))).commit()) {
            "No se pudo guardar el perfil de sesión de forma segura."
        }
    }

    override fun leerSesion(): Sesion? = runCatching {
        val vigente = preferencias.getString(CLAVE_PERFIL, null)
        val valor = vigente ?: preferencias.all.entries.firstOrNull {
            runCatching { descifrar(Base64.decode(it.key, Base64.NO_WRAP)) == CLAVE_SESION }.getOrDefault(false)
        }?.value as? String
        valor?.let {
            Json.decodeFromString<Sesion>(descifrar(Base64.decode(it as String, Base64.NO_WRAP)))
        }
    }.getOrNull()

    override fun borrarToken() {
        check(preferencias.edit().clear().commit()) {
            "No se pudo eliminar la sesión guardada."
        }
    }

    private fun cifrar(texto: String): String {
        val cifrador = Cipher.getInstance(TRANSFORMACION)
        cifrador.init(Cipher.ENCRYPT_MODE, obtenerOCrearClave())
        return Base64.encodeToString(
            cifrador.iv + cifrador.doFinal(texto.encodeToByteArray()), Base64.NO_WRAP
        )
    }

    private fun descifrar(datos: ByteArray): String {
        require(datos.size >= 12 + 16) { "Entrada cifrada incompleta" }
        val cifrador = Cipher.getInstance(TRANSFORMACION)
        cifrador.init(Cipher.DECRYPT_MODE, obtenerOCrearClave(),
            GCMParameterSpec(128, datos.copyOfRange(0, 12)))
        return cifrador.doFinal(datos.copyOfRange(12, datos.size)).decodeToString()
    }

    private fun obtenerOCrearClave(): SecretKey {
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val PREFERENCIAS = "sesion_segura"
        const val CLAVE_TOKEN = "jwt_cifrado"
        const val CLAVE_IV = "jwt_iv"
        const val CLAVE_SESION = "sesion_cifrada"
        const val CLAVE_PERFIL = "perfil_cifrado_v2"
        const val ALIAS = "pharmamobile.jwt"
        const val TRANSFORMACION = "AES/GCM/NoPadding"
    }
}
