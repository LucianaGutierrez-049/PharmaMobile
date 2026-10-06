package pe.edu.upeu.pharmamobilee.presentation.producto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.presentacion.components.EmptyState
import pe.edu.upeu.pharmamobilee.presentacion.components.ErrorState
import pe.edu.upeu.pharmamobilee.presentacion.components.LoadingState
import pe.edu.upeu.pharmamobilee.presentacion.components.SectionHeader
import pe.edu.upeu.pharmamobilee.presentacion.components.ValidatedTextField
import pe.edu.upeu.pharmamobilee.presentacion.producto.InventarioTabs

@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var formularioExpandido by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(uiState.operacion) {
        (uiState.operacion as? ProductoOperacion.Fallida)?.let {
            snackbarHostState.showSnackbar(it.mensaje)
        }
    }
    LaunchedEffect(uiState.productoEnEdicionId) {
        if (uiState.productoEnEdicionId != null) formularioExpandido = true
    }

    Box(modifier = modifier.fillMaxSize()) {
      Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SectionHeader(
            title = "Gestiona tu inventario",
            description = "Controla precios, disponibilidad y reposición desde una sola vista.",
            icon = Icons.Default.Medication
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val formulario: @Composable () -> Unit = {
                ProductoFormulario(
                    uiState = uiState,
                    onNombreChange = viewModel::actualizarNombre,
                    onPrecioChange = viewModel::actualizarPrecio,
                    onStockChange = viewModel::actualizarStock,
                    onActivoChange = viewModel::actualizarActivo,
                    onRegistrar = viewModel::guardarProducto,
                    onCancelarEdicion = viewModel::cancelarEdicion,
                    expandido = formularioExpandido,
                    onExpandidoChange = { formularioExpandido = it }
                )
            }
            val inventario: @Composable () -> Unit = {
                ProductoContenido(
                    uiState = uiState,
                    onReintentar = viewModel::cargarProductos,
                    onEditarProducto = viewModel::editarProducto,
                    onEliminarProducto = viewModel::eliminarProducto,
                    onCompartirProducto = viewModel::compartir
                )
            }

            if (maxWidth >= 600.dp) {
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
      SnackbarHost(
          hostState = snackbarHostState,
          modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
      )
    }
}

@Composable
private fun ProductoContenido(
    uiState: ProductoUiState,
    onReintentar: () -> Unit,
    onEditarProducto: (Producto) -> Unit,
    onEliminarProducto: (Long) -> Unit,
    onCompartirProducto: (Producto) -> Unit
) {
    when (val fase = uiState.fase) {
            ProductoFase.Cargando -> LoadingState(title = "Cargando productos")

            ProductoFase.SinProductos -> {
                EmptyState(
                    icon = Icons.Default.Inventory2,
                    title = "Tu inventario está listo para comenzar",
                    description = "Registra el primer producto en el formulario o actualiza para consultar cambios.",
                    actionLabel = "Actualizar inventario",
                    onAction = onReintentar
                )
            }

            ProductoFase.ConProductos -> {
                InventarioTabs(
                    productos = uiState.productos,
                    onEditarProducto = onEditarProducto,
                    onEliminarProducto = onEliminarProducto,
                    onCompartirProducto = onCompartirProducto,
                    eliminando = (uiState.operacion as? ProductoOperacion.EnCurso)
                        ?.tipo == ProductoOperacion.Tipo.Eliminar
                )
            }

            is ProductoFase.Error -> {
                ErrorState(message = fase.mensaje, onRetry = onReintentar)
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
    onCancelarEdicion: () -> Unit,
    expandido: Boolean,
    onExpandidoChange: (Boolean) -> Unit
) {
    val operacionEnCurso = uiState.operacion as? ProductoOperacion.EnCurso
    val guardando = operacionEnCurso?.tipo == ProductoOperacion.Tipo.Crear ||
        operacionEnCurso?.tipo == ProductoOperacion.Tipo.Actualizar

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer) {
                    Icon(
                        imageVector = Icons.Default.Medication,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(10.dp).size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.productoEnEdicionId == null) "Nuevo producto" else "Editar producto",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Completa los datos comerciales y de inventario.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onExpandidoChange(!expandido) }) {
                    Icon(
                        imageVector = if (expandido) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expandido) "Contraer formulario" else "Expandir formulario"
                    )
                }
            }

            AnimatedVisibility(visible = expandido) {
              Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ValidatedTextField(
                value = uiState.nombre,
                onValueChange = onNombreChange,
                label = "Nombre",
                error = uiState.nombreError,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Medication, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
            )

            ValidatedTextField(
                value = uiState.precio,
                onValueChange = onPrecioChange,
                label = "Precio",
                error = uiState.precioError,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                },
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth()
            )

            ValidatedTextField(
                value = uiState.stock,
                onValueChange = onStockChange,
                label = "Stock",
                error = uiState.stockError,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Inventory2, contentDescription = null)
                },
                keyboardType = KeyboardType.Number,
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
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        if (uiState.productoEnEdicionId == null) {
                            "Registrar"
                        } else {
                            "Guardar cambios"
                        }
                    )
                }
            }

            if (uiState.productoEnEdicionId != null) {
                OutlinedButton(
                    onClick = onCancelarEdicion,
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text("Cancelar edición")
                }
            }
              }
            }
        }
    }
}
