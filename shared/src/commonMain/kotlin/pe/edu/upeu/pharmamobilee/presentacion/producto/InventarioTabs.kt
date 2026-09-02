package pe.edu.upeu.pharmamobilee.presentacion.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import pe.edu.upeu.pharmamobilee.domain.model.Producto

private val titulosTabs = listOf(
    "Activos",
    "Inactivos",
    "Bajo stock"
)

@Composable
fun InventarioTabs(
    productos: List<Producto>,
    onEditarProducto: (Producto) -> Unit,
    modifier: Modifier = Modifier
) {
    var tabSeleccionada by remember {
        mutableStateOf(0)
    }

    val productosFiltrados = when (tabSeleccionada) {
        0 -> filtrarProductosInventario(productos, ProductoFiltro.Activos)
        1 -> filtrarProductosInventario(productos, ProductoFiltro.Inactivos)
        else -> filtrarProductosInventario(productos, ProductoFiltro.BajoStock)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Inventario",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "${productosFiltrados.size} productos",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PrimaryTabRow(
            selectedTabIndex = tabSeleccionada
        ) {
            titulosTabs.forEachIndexed { index, titulo ->
                Tab(
                    selected = tabSeleccionada == index,
                    onClick = {
                        tabSeleccionada = index
                    },
                    text = {
                        Text(titulo)
                    }
                )
            }
        }

        ListaProductos(
            productos = productosFiltrados,
            onEditarProducto = onEditarProducto
        )
    }
}

@Composable
private fun ListaProductos(
    productos: List<Producto>,
    onEditarProducto: (Producto) -> Unit
) {
    if (productos.isEmpty()) {
        Text(
            text = "No hay productos en esta categoria.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 12.dp)
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        productos.forEach { producto ->
            ProductoInventarioCard(
                producto = producto,
                onEditar = {
                    onEditarProducto(producto)
                }
            )
        }
    }
}

@Composable
private fun ProductoInventarioCard(
    producto: Producto,
    onEditar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (producto.activo) "Activo" else "Inactivo",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (producto.activo) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }

                Text(
                    text = "S/ ${producto.precio.formatearPrecio()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = onEditar
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar ${producto.nombre}"
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock: ${producto.stock}",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (producto.stock <= 5) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text("Bajo stock")
                        }
                    )
                }
            }
        }
    }
}

private fun Double.formatearPrecio(): String {
    val centavosTotales = (this * 100).roundToInt()
    val entero = centavosTotales / 100
    val centimos = centavosTotales % 100
    return "$entero.${centimos.toString().padStart(2, '0')}"
}
