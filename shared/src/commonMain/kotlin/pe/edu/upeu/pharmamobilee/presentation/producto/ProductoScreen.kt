package pe.edu.upeu.pharmamobilee.presentation.producto

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
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
    puedeEscribir: Boolean = true,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmarReenvio by remember { mutableStateOf<Producto?>(null) }
    // Plegado por defecto: el inventario queda visible sin desplazarse; se abre al editar.
    var formularioExpandido by remember { mutableStateOf(false) }
    var confirmarRecuperacion by remember { mutableStateOf(false) }

    if (confirmarRecuperacion) androidx.compose.material3.AlertDialog(
        onDismissRequest = { confirmarRecuperacion = false },
        title = { Text("Recuperar pendientes anteriores") },
        text = { Text("Confirma que todos estos pendientes son de tu cuenta. Se conservarán sus intentos y errores. Deberás revisar la categoría y confirmar cada reenvío; no se enviarán ahora.") },
        confirmButton = { androidx.compose.material3.TextButton(onClick = {
            viewModel.recuperarPendientesAnteriores(); confirmarRecuperacion = false
        }) { Text("Son de mi cuenta") } },
        dismissButton = { androidx.compose.material3.TextButton(onClick = { confirmarRecuperacion = false }) { Text("Cancelar") } }
    )

    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(uiState.operacion) {
        (uiState.operacion as? ProductoOperacion.Fallida)?.let {
            snackbarHostState.showSnackbar(it.mensaje)
        }
    }
    LaunchedEffect(uiState.avisoRed) {
        uiState.avisoRed?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(uiState.productoEnEdicionId) {
        if (uiState.productoEnEdicionId != null) formularioExpandido = true
    }

    confirmarReenvio?.let { producto ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmarReenvio = null },
            title = { Text("Confirmar reenvío") },
            text = { Text("Comprueba que ${producto.nombre} no se haya creado en el servidor. Reenviarlo podría duplicarlo. ¿Deseas continuar?") },
            confirmButton = { androidx.compose.material3.TextButton(onClick = {
                viewModel.reintentar(producto.id); confirmarReenvio = null
            }) { Text("Reenviar") } },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { confirmarReenvio = null }) { Text("Cancelar") } }
        )
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

        EstadoSincronizacionCard(
            uiState = uiState,
            puedeEscribir = puedeEscribir,
            onSincronizar = viewModel::sincronizar,
            onRecuperarAnteriores = { confirmarRecuperacion = true }
        )

        uiState.productos.filter { it.producto.pendienteSincronizacion &&
            (it.producto.requiereRevision || it.producto.intentos >= 5) }.forEach { fila ->
            RevisionPendienteCard(
                nombre = fila.nombre,
                intentos = fila.producto.intentos,
                detalle = fila.producto.ultimoError ?: "Envío pendiente de revisión",
                puedeEscribir = puedeEscribir,
                habilitado = !uiState.sincronizando,
                onRevisar = { viewModel.editarProducto(fila.producto) },
                onReenviar = { confirmarReenvio = fila.producto }
            )
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val formulario: @Composable () -> Unit = {
                ProductoFormulario(
                    uiState = uiState,
                    onNombreChange = viewModel::actualizarNombre,
                    onCategoriaChange = viewModel::seleccionarCategoria,
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
                    onReintentar = viewModel::sincronizar,
                    onEditarProducto = if (puedeEscribir) viewModel::editarProducto else null,
                    onEliminarProducto = if (puedeEscribir) viewModel::eliminarProducto else null,
                    onCompartirProducto = viewModel::compartir
                )
            }

            if (maxWidth >= 600.dp) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (puedeEscribir) {
                        Column(modifier = Modifier.weight(0.9f)) { formulario() }
                    }
                    Column(modifier = Modifier.weight(1.4f)) { inventario() }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (puedeEscribir) formulario()
                    inventario()
                }
            }
        }

        // Herramientas del dispositivo: al final, para no interrumpir el trabajo con el inventario.
        Text(
            "Dispositivo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        pe.edu.upeu.pharmamobilee.presentation.permiso.PermisoNotificacionesCard()
        pe.edu.upeu.pharmamobilee.presentation.permiso.TelemetriaPruebaCard()
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
    onEditarProducto: ((Producto) -> Unit)?,
    onEliminarProducto: ((Long) -> Unit)?,
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
    onCategoriaChange: (Long) -> Unit,
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

    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onExpandidoChange(!expandido) },
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

            var categoriasAbiertas by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(onClick = { categoriasAbiertas = true }, enabled = !guardando) {
                    Text(uiState.categorias.firstOrNull { it.id == uiState.categoriaSeleccionadaId }?.nombre
                        ?: uiState.categoriaSeleccionadaId?.let { "Categoría $it" } ?: "Seleccionar categoría")
                }
                androidx.compose.material3.DropdownMenu(expanded = categoriasAbiertas, onDismissRequest = { categoriasAbiertas = false }) {
                    uiState.categorias.forEach { categoria ->
                        androidx.compose.material3.DropdownMenuItem(text = { Text(categoria.nombre) }, onClick = {
                            onCategoriaChange(categoria.id); categoriasAbiertas = false
                        })
                    }
                }
            }
            if (uiState.categorias.isEmpty()) Text("Conéctate y sincroniza para descargar las categorías.", style = MaterialTheme.typography.bodySmall)
            uiState.categoriaError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

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

/** Estado de sincronización agrupado: antigüedad, contadores y acción en una sola tarjeta. */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun EstadoSincronizacionCard(
    uiState: ProductoUiState,
    puedeEscribir: Boolean,
    onSincronizar: () -> Unit,
    onRecuperarAnteriores: () -> Unit
) {
    val sinRed = uiState.avisoRed != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (sinRed) Icons.Default.CloudOff else Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Estado de sincronización",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = textoAntiguedadSincronizacion(
                            ultimaMillis = uiState.ultimaSincronizacionMillis,
                            ahoraMillis = kotlin.time.Clock.System.now().toEpochMilliseconds()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text("${uiState.pendientes} cambios por enviar") },
                    leadingIcon = { Icon(Icons.Default.Sync, null, Modifier.size(AssistChipDefaults.IconSize)) })
                AssistChip(onClick = {}, label = { Text("${uiState.fallidas} requieren revisión") },
                    leadingIcon = { Icon(Icons.Default.WarningAmber, null, Modifier.size(AssistChipDefaults.IconSize)) })
            }
            uiState.avisoRed?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            if (uiState.pendientesAnteriores > 0) {
                Text(
                    "Hay ${uiState.pendientesAnteriores} pendientes anteriores sin cuenta identificada. Se conservan y no se enviarán desde esta sesión.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                if (puedeEscribir) OutlinedButton(onClick = onRecuperarAnteriores, enabled = !uiState.sincronizando) {
                    Text("Recuperar pendientes anteriores")
                }
            }
            Button(
                onClick = onSincronizar,
                enabled = !uiState.sincronizando,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                if (uiState.sincronizando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sincronizar inventario")
                }
            }
        }
    }
}

/** Envío que necesita la decisión del usuario: se resalta y agrupa sus acciones. */
@Composable
private fun RevisionPendienteCard(
    nombre: String,
    intentos: Int,
    detalle: String,
    puedeEscribir: Boolean,
    habilitado: Boolean,
    onRevisar: () -> Unit,
    onReenviar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WarningAmber, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.width(10.dp))
                Text(nombre, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                Text("Intentos: $intentos / 5", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer)
            }
            Text(detalle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            if (puedeEscribir) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onRevisar, enabled = habilitado) { Text("Revisar datos") }
                FilledTonalButton(onClick = onReenviar, enabled = habilitado) { Text("Reenviar con confirmación") }
            }
        }
    }
}
