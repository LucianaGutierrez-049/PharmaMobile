package pe.edu.upeu.pharmamobilee.presentacion.cliente


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ClienteScreen() {

    var nombre by remember {
        mutableStateOf("")
    }

    var correo by remember {
        mutableStateOf("")
    }

    var telefono by remember {
        mutableStateOf("")
    }

    var nombreError by remember {
        mutableStateOf<String?>(null)
    }

    var correoError by remember {
        mutableStateOf<String?>(null)
    }

    var telefonoError by remember {
        mutableStateOf<String?>(null)
    }

    var mensajeExito by remember {
        mutableStateOf<String?>(null)
    }

    fun validar(): Boolean {
        nombreError = ClienteValidator.validarNombre(nombre)
        correoError = ClienteValidator.validarCorreo(correo)
        telefonoError = ClienteValidator.validarTelefono(telefono)

        return nombreError == null && correoError == null && telefonoError == null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("PharmaMobil")
        Text("Registro de Cliente")

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = {
                Text("Nombre")
            },
            isError = nombreError != null,
            supportingText = {
                nombreError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = {
                Text("Correo")
            },
            isError = correoError != null,
            supportingText = {
                correoError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = {
                Text("Teléfono (opcional)")
            },
            isError = telefonoError != null,
            supportingText = {
                telefonoError?.let { Text(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                mensajeExito = null
                if (validar()) {
                    mensajeExito = "Cliente \"$nombre\" registrado correctamente"
                    nombre = ""
                    correo = ""
                    telefono = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        mensajeExito?.let {
            Text(it)
        }
    }
}