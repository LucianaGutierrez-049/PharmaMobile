package pe.edu.upeu.pharmamobilee.presentation.producto

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        ProductoFormulario(
            uiState = uiState,
            onNombreChange = viewModel::actualizarNombre,
            onPrecioChange = viewModel::actualizarPrecio,
            onStockChange = viewModel::actualizarStock,
            onActivoChange = viewModel::actualizarActivo,
            onRegistrar = viewModel::registrarProducto
        )

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
                Text(
                    text = "No hay productos registrados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            is ProductoFase.ConProductos -> {
                InventarioTabs(
                    productos = fase.productos,
                    onEditarProducto = null
                )
            }

            is ProductoFase.Error -> {
                Text(
                    text = fase.mensaje,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        uiState.mensajeExito?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary
            )
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
    onRegistrar: () -> Unit
) {
    Text(
        text = "Nuevo producto",
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
                Text("Registrar")
            }
        }
    }
}
