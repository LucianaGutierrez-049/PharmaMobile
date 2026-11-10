package pe.edu.upeu.pharmamobilee.presentacion.cliente

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val DNI_REGEX = Regex("^\\d{8}$")
private val TELEFONO_REGEX = Regex("^\\d{9}$")

/** Mismas reglas que ClienteRequestDTO de PharmaSoft, para avisar antes de enviar. */
object ClienteValidator {

    fun validarDni(dni: String): String? = when {
        dni.isBlank() -> "El DNI es obligatorio"
        !DNI_REGEX.matches(dni.trim()) -> "El DNI debe contener exactamente 8 dígitos"
        else -> null
    }

    fun validarNombre(nombre: String): String? = validarTexto(nombre, "Los nombres")

    fun validarApellidos(apellidos: String): String? = validarTexto(apellidos, "Los apellidos")

    fun validarCorreo(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        correo.trim().length > 150 -> "El correo no debe superar 150 caracteres"
        !EMAIL_REGEX.matches(correo.trim()) -> "El correo no tiene un formato válido"
        else -> null
    }

    fun validarTelefono(telefono: String): String? =
        if (telefono.isNotBlank() && !TELEFONO_REGEX.matches(telefono.trim())) "El teléfono debe contener 9 dígitos" else null

    fun validarDireccion(direccion: String): String? =
        if (direccion.trim().length > 250) "La dirección no debe superar los 250 caracteres" else null

    private fun validarTexto(valor: String, campo: String): String? = when {
        valor.isBlank() -> "$campo son obligatorios"
        valor.trim().length !in 2..100 -> "$campo deben tener entre 2 y 100 caracteres"
        else -> null
    }
}
