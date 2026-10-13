@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package pe.edu.upeu.pharmamobilee.data.local

import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

class AlmacenSeguroIos : AlmacenSeguro {
    override fun guardarToken(token: String) {
        borrarToken()
        val consulta = consultaBase()
        val bytes = token.encodeToByteArray()
        val datos = bytes.usePinned { pinned ->
            CFDataCreate(
                kCFAllocatorDefault,
                pinned.addressOf(0).reinterpret(),
                bytes.size.toLong()
            )
        } ?: error("No se pudo codificar el token")
        CFDictionarySetValue(consulta, kSecValueData, datos)
        val estado = SecItemAdd(consulta, null)
        CFRelease(datos)
        CFRelease(consulta)
        check(estado == errSecSuccess) { "Keychain no pudo guardar la sesión ($estado)" }
    }

    override fun leerToken(): String? {
        val consulta = consultaBase()
        CFDictionarySetValue(consulta, kSecReturnData, kCFBooleanTrue)
        CFDictionarySetValue(consulta, kSecMatchLimit, kSecMatchLimitOne)
        return memScoped {
            val resultado = alloc<COpaquePointerVar>()
            val estado = SecItemCopyMatching(consulta, resultado.ptr)
            CFRelease(consulta)
            if (estado == errSecItemNotFound) return@memScoped null
            check(estado == errSecSuccess) { "Keychain no pudo leer la sesión ($estado)" }
            val datos: CFDataRef = resultado.value?.reinterpret() ?: return@memScoped null
            val longitud = CFDataGetLength(datos).toInt()
            val bytes = CFDataGetBytePtr(datos)?.readBytes(longitud) ?: return@memScoped null
            CFRelease(datos)
            bytes.decodeToString()
        }
    }

    override fun borrarToken() {
        val consulta = consultaBase()
        val estado = SecItemDelete(consulta)
        CFRelease(consulta)
        check(estado == errSecSuccess || estado == errSecItemNotFound) {
            "Keychain no pudo borrar la sesión ($estado)"
        }
    }

    private fun consultaBase() = CFDictionaryCreateMutable(
        kCFAllocatorDefault,
        0,
        kCFTypeDictionaryKeyCallBacks.ptr,
        kCFTypeDictionaryValueCallBacks.ptr
    )!!.also { consulta ->
        val servicio = CFStringCreateWithCString(
            kCFAllocatorDefault,
            SERVICIO,
            kCFStringEncodingUTF8
        )!!
        val cuenta = CFStringCreateWithCString(
            kCFAllocatorDefault,
            CUENTA,
            kCFStringEncodingUTF8
        )!!
        CFDictionarySetValue(consulta, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(consulta, kSecAttrService, servicio)
        CFDictionarySetValue(consulta, kSecAttrAccount, cuenta)
        CFRelease(servicio)
        CFRelease(cuenta)
    }

    private companion object {
        const val SERVICIO = "pe.edu.upeu.pharmamobilee"
        const val CUENTA = "jwt"
    }
}
