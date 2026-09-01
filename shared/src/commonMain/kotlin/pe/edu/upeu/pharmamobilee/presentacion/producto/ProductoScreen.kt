package pe.edu.upeu.pharmamobilee.presentacion.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.presentacion.components.ValidatedTextField

@Composable
fun ProductoScreen(
    onRegistrar: (Producto) -> Unit = {}
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

    fun validar(): Producto? {
        nombreError = ProductoValidator.validarNombre(nombre)
        precioError = ProductoValidator.validarPrecio(precio)
        stockError = ProductoValidator.validarStock(stock)

        if (nombreError != null || precioError != null || stockError != null) return null

        return Producto(
            id = 0L,
            nombre = nombre.trim(),
            precio = precio.toDouble(),
            stock = stock.toInt()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text("PharmaMobil")
        Text("Registro de Producto")

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

        Button(
            onClick = {
                mensajeExito = null
                val producto = validar()
                if (producto != null) {
                    onRegistrar(producto)
                    mensajeExito = "Producto \"${producto.nombre}\" registrado correctamente"
                    nombre = ""
                    precio = ""
                    stock = ""
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