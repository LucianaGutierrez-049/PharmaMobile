package pe.edu.upeu.pharmamobilee.presentacion.cliente


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobilee.presentacion.components.ValidatedTextField

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

        Text(
            text = "Registro de clientes",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Mantén actualizados los datos de contacto de tus clientes.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ValidatedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre",
                    error = nombreError
                )

                ValidatedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = "Correo",
                    error = correoError
                )

                ValidatedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = "Teléfono (opcional)",
                    error = telefonoError
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
            }
        }

        mensajeExito?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
        }
    }
}
