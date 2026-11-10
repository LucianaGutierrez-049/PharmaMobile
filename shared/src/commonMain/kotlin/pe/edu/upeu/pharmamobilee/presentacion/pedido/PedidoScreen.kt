package pe.edu.upeu.pharmamobilee.presentacion.pedido

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobilee.domain.model.Venta
import pe.edu.upeu.pharmamobilee.presentacion.components.EmptyState
import pe.edu.upeu.pharmamobilee.presentacion.components.ErrorState
import pe.edu.upeu.pharmamobilee.presentacion.components.LoadingState
import pe.edu.upeu.pharmamobilee.presentacion.components.SectionHeader

@Composable
fun PedidoScreen(viewModel: VentaViewModel, puedeRegistrar: Boolean) {
    val s by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(s.mensaje) { s.mensaje?.let { snackbar.showSnackbar(it); viewModel.limpiarMensaje() } }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(
                title = "Ventas",
                description = "Registra ventas y consulta el historial de PharmaSoft.",
                icon = Icons.AutoMirrored.Filled.ReceiptLong
            )
            if (puedeRegistrar) NuevaVentaCard(s, viewModel)

            Text("Historial de ventas", style = MaterialTheme.typography.titleLarge)
            when {
                s.cargando && s.ventas.isEmpty() -> LoadingState(title = "Cargando ventas")
                s.error != null && s.ventas.isEmpty() ->
                    ErrorState(message = s.error!!, onRetry = viewModel::cargar, titulo = "No pudimos cargar las ventas")
                s.ventas.isEmpty() -> EmptyState(
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    title = "Aún no hay ventas",
                    description = if (puedeRegistrar) "Registra la primera venta con el formulario de arriba."
                    else "Cuando se registren ventas aparecerán aquí.",
                    actionLabel = "Actualizar", onAction = viewModel::cargar
                )
                else -> s.ventas.forEach { VentaCard(it) }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}

@Composable
private fun NuevaVentaCard(s: VentaUiState, vm: VentaViewModel) {
    var clientesAbierto by remember { mutableStateOf(false) }
    var productosAbierto by remember { mutableStateOf(false) }
    Card(
        Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingCart, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text("Nueva venta", style = MaterialTheme.typography.titleLarge)
            }

            Box {
                OutlinedButton(onClick = { clientesAbierto = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Person, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(s.clienteSeleccionado?.let { "${it.nombreCompleto} · DNI ${it.dni}" } ?: "Seleccionar cliente")
                }
                DropdownMenu(expanded = clientesAbierto, onDismissRequest = { clientesAbierto = false }) {
                    if (s.clientes.isEmpty()) DropdownMenuItem(text = { Text("No hay clientes activos. Regístralos en Clientes.") }, onClick = { clientesAbierto = false })
                    s.clientes.forEach { c ->
                        DropdownMenuItem(text = { Text("${c.nombreCompleto} · DNI ${c.dni}") },
                            onClick = { vm.seleccionarCliente(c.id); clientesAbierto = false })
                    }
                }
            }

            Box {
                OutlinedButton(onClick = { productosAbierto = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(s.productoSeleccionado?.let { "${it.nombre} · ${formatoMonto(it.precio)} · stock ${it.stock}" } ?: "Seleccionar producto")
                }
                DropdownMenu(expanded = productosAbierto, onDismissRequest = { productosAbierto = false }) {
                    if (s.productos.isEmpty()) DropdownMenuItem(text = { Text("No hay productos activos con stock.") }, onClick = { productosAbierto = false })
                    s.productos.forEach { p ->
                        DropdownMenuItem(text = { Text("${p.nombre} · ${formatoMonto(p.precio)} · stock ${p.stock}") },
                            onClick = { vm.seleccionarProducto(p.id); productosAbierto = false })
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = s.cantidad, onValueChange = vm::cambiarCantidad, label = { Text("Cantidad") },
                    singleLine = true, modifier = Modifier.weight(1f)
                )
                FilledTonalButton(onClick = vm::agregar, modifier = Modifier.heightIn(min = 52.dp)) {
                    Icon(Icons.Default.AddShoppingCart, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Agregar")
                }
            }

            if (s.carrito.isNotEmpty()) {
                HorizontalDivider()
                s.carrito.forEach { l ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(l.producto.nombre, style = MaterialTheme.typography.titleSmall)
                            Text("${l.cantidad} × ${formatoMonto(l.producto.precio)}", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatoMonto(l.subtotal), style = MaterialTheme.typography.titleSmall)
                        IconButton(onClick = { vm.quitar(l) }) { Icon(Icons.Default.Close, "Quitar ${l.producto.nombre}") }
                    }
                }
                HorizontalDivider()
                Row {
                    Text("Total estimado", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Text(formatoMonto(s.totalEstimado), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            s.errorCarrito?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            Button(onClick = vm::registrar, enabled = !s.registrando, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                if (s.registrando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp) else Text("Registrar venta")
            }
        }
    }
}

@Composable
private fun VentaCard(v: Venta) {
    var abierto by remember { mutableStateOf(false) }
    Card(
        Modifier.fillMaxWidth().clickable { abierto = !abierto }, shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Venta #${v.id}", style = MaterialTheme.typography.titleMedium)
                    Text(v.clienteNombre, style = MaterialTheme.typography.bodyMedium)
                    Text(v.fecha.replace('T', ' ').take(16), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatoMonto(v.total), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(v.estado, style = MaterialTheme.typography.labelMedium,
                        color = if (v.estado == "ANULADA") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(if (abierto) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    if (abierto) "Ocultar detalle" else "Ver detalle")
            }
            AnimatedVisibility(abierto) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HorizontalDivider()
                    v.detalles.forEach { d ->
                        Row {
                            Text("${d.cantidad} × ${d.productoNombre}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(formatoMonto(d.subtotal), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
