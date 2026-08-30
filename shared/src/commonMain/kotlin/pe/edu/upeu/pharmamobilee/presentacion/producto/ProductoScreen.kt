package pe.edu.upeu.pharmamobilee.presentacion.producto

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
import pe.edu.upeu.pharmamobilee.domain.model.Producto

@Composable
fun ProductoScreen() {

    // --- Paso 1 y 2: estados de entrada (siempre String para poder validar antes de convertir) ---
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    // --- Paso 3: estado de retroalimentación (un único mensaje, éxito o error) ---
    var mensaje by remember { mutableStateOf("") }

    // --- Paso 4: bandera que indica si el usuario ya intentó registrar ---
    // Evita mostrar errores en rojo antes de que el usuario pulse "Registrar".
    var intentoRegistrar by remember { mutableStateOf(false) }

    // --- Paso 5 y 6: conversión segura (nunca lanza excepción, retorna null si falla) ---
    val precioValor = precio.toDoubleOrNull()
    val stockValor = stock.toIntOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("PharmaMobil")
        Text("Registro de Producto")

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            // Paso 9: solo se marca error si ya se intentó registrar
            isError = intentoRegistrar && nombre.isBlank(),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio") },
            isError = intentoRegistrar && (precioValor == null || precioValor <= 0.0),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = stock,
            onValueChange = { stock = it },
            label = { Text("Stock") },
            isError = intentoRegistrar && (stockValor == null || stockValor < 0),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                intentoRegistrar = true

                // Paso 7: secuencia estricta de validación con "when"
                when {
                    nombre.isBlank() ->
                        mensaje = "El nombre es obligatorio."

                    precioValor == null ->
                        mensaje = "Ingrese un precio numérico."

                    precioValor <= 0.0 ->
                        mensaje = "El precio debe ser mayor que cero."

                    stockValor == null ->
                        mensaje = "Ingrese un stock entero."

                    stockValor < 0 ->
                        mensaje = "El stock no puede ser negativo."

                    else -> {
                        // Solo se construye Producto cuando TODAS las condiciones son válidas
                        val producto = Producto(
                            id = 0L,
                            nombre = nombre,
                            precio = precioValor,
                            stock = stockValor
                        )

                        mensaje = "Producto \"${producto.nombre}\" registrado correctamente."

                        // Paso 8: limpieza del formulario tras un registro exitoso
                        nombre = ""
                        precio = ""
                        stock = ""
                        intentoRegistrar = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar")
        }

        if (mensaje.isNotBlank()) {
            Text(mensaje)
        }
    }
}