package pe.edu.upeu.pharmamobilee.presentacion.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.presentacion.components.ValidatedTextField

@Composable
fun ProductoScreen(
    onRegistrar: (Producto) -> Unit = {},
    productoEnEdicion: Producto? = null,
    onActualizar: (Producto) -> Unit = {},
    onCancelarEdicion: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    var nombre by remember {
        mutableStateOf("")
    }

    var precio by remember {
        mutableStateOf("")
    }

    var stock by remember {
        mutableStateOf("")
    }

    var activo by remember {
        mutableStateOf(true)
    }

    var nombreError by remember {
        mutableStateOf<String?>(null)
    }

    var precioError by remember {
        mutableStateOf<String?>(null)
    }

    var stockError by remember {
        mutableStateOf<String?>(null)
    }

    var mensajeExito by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(productoEnEdicion?.id) {
        nombre = productoEnEdicion?.nombre.orEmpty()
        precio = productoEnEdicion?.precio?.formatearParaCampo().orEmpty()
        stock = productoEnEdicion?.stock?.toString().orEmpty()
        activo = productoEnEdicion?.activo ?: true
        nombreError = null
        precioError = null
        stockError = null
        mensajeExito = null
    }

    fun validar(): Producto? {
        nombreError = ProductoValidator.validarNombre(nombre)
        precioError = ProductoValidator.validarPrecio(precio)
        stockError = ProductoValidator.validarStock(stock)

        if (nombreError != null || precioError != null || stockError != null) return null

        return Producto(
            id = productoEnEdicion?.id ?: 0L,
            nombre = nombre.trim(),
            precio = precio.toDouble(),
            stock = stock.toInt(),
            activo = activo
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (productoEnEdicion == null) {
                "Nuevo producto"
            } else {
                "Editar producto"
            },
            style = MaterialTheme.typography.titleLarge
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
                    error = nombreError,
                    modifier = Modifier.fillMaxWidth()
                )

                ValidatedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = "Precio",
                    error = precioError,
                    modifier = Modifier.fillMaxWidth()
                )

                ValidatedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = "Stock",
                    error = stockError,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Producto activo",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Text(
                            text = if (activo) "Aparece en Activos" else "Aparece en Inactivos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = activo,
                        onCheckedChange = {
                            activo = it
                        }
                    )
                }

                Button(
                    onClick = {
                        mensajeExito = null
                        val producto = validar()
                        if (producto != null) {
                            if (productoEnEdicion == null) {
                                onRegistrar(producto)
                                mensajeExito = "Producto \"${producto.nombre}\" registrado correctamente"
                                nombre = ""
                                precio = ""
                                stock = ""
                                activo = true
                            } else {
                                onActualizar(producto)
                                mensajeExito = "Producto \"${producto.nombre}\" actualizado correctamente"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (productoEnEdicion == null) "Registrar" else "Guardar cambios"
                    )
                }

                if (productoEnEdicion != null) {
                    OutlinedButton(
                        onClick = onCancelarEdicion,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancelar edición")
                    }
                }
            }
        }

        mensajeExito?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun Double.formatearParaCampo(): String {
    val texto = toString()
    return if (texto.endsWith(".0")) {
        texto.dropLast(2)
    } else {
        texto
    }
}
