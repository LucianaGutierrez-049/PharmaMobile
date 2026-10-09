package pe.edu.upeu.pharmamobilee.presentacion.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.presentacion.components.SectionHeader
import pe.edu.upeu.pharmamobilee.presentation.producto.ProductoUi

private val tabs = listOf(
    ProductoTab("Activos", Icons.Default.CheckCircle),
    ProductoTab("Inactivos", Icons.Default.Block),
    ProductoTab("Bajo stock", Icons.Default.WarningAmber)
)

private data class ProductoTab(
    val titulo: String,
    val icono: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun InventarioTabs(
    productos: List<ProductoUi>,
    onEditarProducto: ((Producto) -> Unit)?,
    onEliminarProducto: ((Long) -> Unit)?,
    onCompartirProducto: ((Producto) -> Unit)?,
    eliminando: Boolean,
    modifier: Modifier = Modifier
) {
    var tabSeleccionada by remember {
        mutableStateOf(0)
    }
    var busqueda by remember { mutableStateOf("") }
    var productoAEliminar by remember { mutableStateOf<ProductoUi?>(null) }

    val productosPorCategoria = when (tabSeleccionada) {
        0 -> filtrarProductosInventario(productos, ProductoFiltro.Activos)
        1 -> filtrarProductosInventario(productos, ProductoFiltro.Inactivos)
        else -> filtrarProductosInventario(productos, ProductoFiltro.BajoStock)
    }
    val productosFiltrados = productosPorCategoria.filter {
        it.nombre.contains(busqueda.trim(), ignoreCase = true)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(
            title = "Inventario",
            description = "${productosFiltrados.size} productos en esta categoría",
            icon = Icons.Default.Inventory2
        )

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            PrimaryTabRow(
                selectedTabIndex = tabSeleccionada,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = tabSeleccionada == index,
                        onClick = { tabSeleccionada = index },
                        text = { Text(tab.titulo) },
                        icon = {
                            Icon(
                                imageVector = tab.icono,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Buscar producto") },
            placeholder = { Text("Ej. Paracetamol") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (busqueda.isNotEmpty()) {
                    IconButton(onClick = { busqueda = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )

        ListaProductos(
            productos = productosFiltrados,
            onEditarProducto = onEditarProducto,
            onCompartirProducto = onCompartirProducto,
            onSolicitarEliminar = {
                if (!eliminando) productoAEliminar = it
            }
        )
    }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar producto") },
            text = {
                Text("¿Deseas eliminar ${producto.nombre}? Esta acción dará de baja el producto en PharmaSoft.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        productoAEliminar = null
                        onEliminarProducto?.invoke(producto.id)
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ListaProductos(
    productos: List<ProductoUi>,
    onEditarProducto: ((Producto) -> Unit)?,
    onCompartirProducto: ((Producto) -> Unit)?,
    onSolicitarEliminar: (ProductoUi) -> Unit
) {
    if (productos.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = "No hay productos en esta categoría.")
            }
        }
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        productos.forEach { producto ->
            ProductCard(
                producto = producto,
                onEditar = onEditarProducto?.let { { it(producto.producto) } },
                onCompartir = onCompartirProducto?.let { { it(producto.producto) } },
                onEliminar = { onSolicitarEliminar(producto) }
            )
        }
    }
}

@Composable
fun ProductCard(
    producto: ProductoUi,
    onEditar: (() -> Unit)?,
    onCompartir: (() -> Unit)?,
    onEliminar: (() -> Unit)?
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = producto.nombre.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text("Código #${producto.id}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (producto.origen == OrigenProducto.REMOTO) {
                        Text(
                            text = producto.categoria,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Row {
                    if (onEditar != null) {
                        IconButton(onClick = onEditar) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar ${producto.nombre}"
                            )
                        }
                    }
                    if (onEliminar != null) {
                        IconButton(onClick = onEliminar) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar ${producto.nombre}",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = producto.precio,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Precio unitario", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoProductoBadge(activo = producto.activo)
                    StockBadge(
                        stock = producto.stock,
                        stockDisponible = producto.stockDisponible,
                        bajoStock = producto.requiereReposicion
                    )
                }
            }

            if (onCompartir != null) {
                OutlinedButton(
                    onClick = onCompartir,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir")
                }
            }
        }
    }
}

@Composable
private fun EstadoProductoBadge(activo: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (activo) MaterialTheme.colorScheme.tertiaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (activo) Icons.Default.CheckCircle else Icons.Default.Block,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Text(text = if (activo) "Activo" else "Inactivo", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StockBadge(stock: Int, stockDisponible: Boolean, bajoStock: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (bajoStock) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (bajoStock) Icons.Default.WarningAmber else Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (bajoStock) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = when {
                    !stockDisponible -> "Catálogo API"
                    bajoStock -> "Bajo · $stock"
                    else -> "Stock $stock"
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (bajoStock) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
                )
        }
    }
}
