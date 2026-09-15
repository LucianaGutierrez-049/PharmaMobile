package pe.edu.upeu.pharmamobilee.presentacion.cliente


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobilee.presentacion.components.SectionHeader
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        SectionHeader(
            title = "Registro de clientes",
            description = "Mantén actualizados sus datos de contacto.",
            icon = Icons.Default.Person
        )

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Información personal", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Los campos marcados se validan antes del registro.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ValidatedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre",
                    error = nombreError,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )

                ValidatedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = "Correo",
                    error = correoError,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardType = KeyboardType.Email
                )

                ValidatedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = "Teléfono (opcional)",
                    error = telefonoError,
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardType = KeyboardType.Phone
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
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                ) {
                    Text("Registrar")
                }
            }
        }

        mensajeExito?.let {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null)
                    Text(it, color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
            }
        }
    }
}
