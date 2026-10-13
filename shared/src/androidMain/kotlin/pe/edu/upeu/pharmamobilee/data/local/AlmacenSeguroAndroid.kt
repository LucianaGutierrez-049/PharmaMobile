package pe.edu.upeu.pharmamobilee.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AlmacenSeguroAndroid(contexto: Context) : AlmacenSeguro {
    private val preferencias = contexto.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    override fun guardarToken(token: String) {
        val cifrador = Cipher.getInstance(TRANSFORMACION)
        cifrador.init(Cipher.ENCRYPT_MODE, obtenerOCrearClave())
        val cifrado = cifrador.doFinal(token.encodeToByteArray())
        preferencias.edit()
            .putString(CLAVE_TOKEN, Base64.encodeToString(cifrado, Base64.NO_WRAP))
            .putString(CLAVE_IV, Base64.encodeToString(cifrador.iv, Base64.NO_WRAP))
            .apply()
    }

    override fun leerToken(): String? {
        val tokenCifrado = preferencias.getString(CLAVE_TOKEN, null) ?: return null
        val iv = preferencias.getString(CLAVE_IV, null) ?: return null
        return runCatching {
            val cifrador = Cipher.getInstance(TRANSFORMACION)
            cifrador.init(
                Cipher.DECRYPT_MODE,
                obtenerOCrearClave(),
                GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
            )
            cifrador.doFinal(Base64.decode(tokenCifrado, Base64.NO_WRAP)).decodeToString()
        }.getOrElse {
            borrarToken()
            null
        }
    }

    override fun borrarToken() {
        preferencias.edit().remove(CLAVE_TOKEN).remove(CLAVE_IV).apply()
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
        const val ALIAS = "pharmamobile.jwt"
        const val TRANSFORMACION = "AES/GCM/NoPadding"
    }
}
