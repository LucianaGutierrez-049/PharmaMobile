package pe.edu.upeu.pharmamobilee.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.presentacion.components.ValidatedTextField
import pe.edu.upeu.pharmamobilee.presentacion.producto.InventarioTabs

@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Gestiona tu inventario",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Registra productos y controla su disponibilidad desde una sola vista.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        uiState.mensajeExito?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val formulario: @Composable () -> Unit = {
                ProductoFormulario(
                    uiState = uiState,
                    onNombreChange = viewModel::actualizarNombre,
                    onPrecioChange = viewModel::actualizarPrecio,
                    onStockChange = viewModel::actualizarStock,
                    onActivoChange = viewModel::actualizarActivo,
                    onRegistrar = viewModel::registrarProducto,
                    onCancelarEdicion = viewModel::cancelarEdicion
                )
            }
            val inventario: @Composable () -> Unit = {
                ProductoContenido(
                    uiState = uiState,
                    onReintentar = viewModel::cargarProductos,
                    onEditarProducto = viewModel::editarProducto
                )
            }

            if (maxWidth >= 840.dp) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(0.9f)) { formulario() }
                    Column(modifier = Modifier.weight(1.4f)) { inventario() }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    formulario()
                    inventario()
                }
            }
        }

    }
}

@Composable
private fun ProductoContenido(
    uiState: ProductoUiState,
    onReintentar: () -> Unit,
    onEditarProducto: (Producto) -> Unit
) {
    when (val fase = uiState.fase) {
            ProductoFase.Cargando -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Cargando productos...")
                }
            }

            ProductoFase.SinProductos -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Tu inventario está vacío", style = MaterialTheme.typography.titleMedium)
                        Text("Registra el primer producto con el formulario.")
                    }
                }
            }

            ProductoFase.ConProductos -> {
                InventarioTabs(
                    productos = uiState.productos,
                    onEditarProducto = onEditarProducto
                )
            }

            is ProductoFase.Error -> {
                Text(
                    text = fase.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = onReintentar) {
                    Text("Reintentar")
                }
            }
        }
}

@Composable
private fun ProductoFormulario(
    uiState: ProductoUiState,
    onNombreChange: (String) -> Unit,
    onPrecioChange: (String) -> Unit,
    onStockChange: (String) -> Unit,
    onActivoChange: (Boolean) -> Unit,
    onRegistrar: () -> Unit,
    onCancelarEdicion: () -> Unit
) {
    Text(
        text = if (uiState.productoEnEdicionId == null) "Nuevo producto" else "Editar producto",
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
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = "Nombre",
                error = uiState.nombreError,
                modifier = Modifier.fillMaxWidth()
            )

            ValidatedTextField(
                value = uiState.precio,
                onValueChange = onPrecioChange,
                label = "Precio",
                error = uiState.precioError,
                modifier = Modifier.fillMaxWidth()
            )

            ValidatedTextField(
                value = uiState.stock,
                onValueChange = onStockChange,
                label = "Stock",
                error = uiState.stockError,
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
                        text = if (uiState.activo) "Aparece en Activos" else "Aparece en Inactivos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = uiState.activo,
                    onCheckedChange = onActivoChange
                )
            }

            Button(
                onClick = onRegistrar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.productoEnEdicionId == null) "Registrar" else "Guardar cambios")
            }

            if (uiState.productoEnEdicionId != null) {
                OutlinedButton(
                    onClick = onCancelarEdicion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancelar edición")
                }
            }
        }
    }
}
