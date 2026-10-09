package pe.edu.upeu.pharmamobilee.presentacion.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import pharmamobilee.shared.generated.resources.Res
import pharmamobilee.shared.generated.resources.pharmamobil_logo

@Composable
fun InicioScreen(
    totalProductos: Int,
    productosActivos: Int,
    productosBajoStock: Int,
    onProductosClick: () -> Unit,
    onClientesClick: () -> Unit,
    onPedidosClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        InicioHeader()
        ResumenInventario(
            totalProductos = totalProductos,
            productosActivos = productosActivos,
            productosBajoStock = productosBajoStock,
            onClick = onProductosClick
        )
        ModulosResumen(
            onProductosClick = onProductosClick,
            onClientesClick = onClientesClick,
            onPedidosClick = onPedidosClick
        )
        EstadoOperacion(onProductosClick = onProductosClick)
    }
}

@Composable
private fun ResumenInventario(
    totalProductos: Int,
    productosActivos: Int,
    productosBajoStock: Int,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Resumen del inventario", style = MaterialTheme.typography.titleLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResumenCard(
                valor = totalProductos.toString(),
                etiqueta = "Productos",
                icono = Icons.Default.Inventory2,
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
            ResumenCard(
                valor = productosActivos.toString(),
                etiqueta = "Activos",
                icono = Icons.Default.LocalPharmacy,
                onClick = onClick,
                modifier = Modifier.weight(1f)
            )
            ResumenCard(
                valor = productosBajoStock.toString(),
                etiqueta = "Bajo stock",
                icono = Icons.Default.WarningAmber,
                onClick = onClick,
                modifier = Modifier.weight(1f),
                esAlerta = productosBajoStock > 0
            )
        }
    }
}

@Composable
private fun ResumenCard(
    valor: String,
    etiqueta: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    esAlerta: Boolean = false
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = if (esAlerta) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = if (esAlerta) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.primary
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.headlineSmall,
                color = if (esAlerta) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = if (esAlerta) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InicioHeader() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.pharmamobil_logo),
                    contentDescription = "Logo PharmaMobil",
                    modifier = Modifier.size(58.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "PharmaMobil",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "Sistema de gestión farmacéutica",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                AssistChip(
                    onClick = {},
                    label = {
                        Text("Gestión segura y centralizada")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ModulosResumen(
    onProductosClick: () -> Unit,
    onClientesClick: () -> Unit,
    onPedidosClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Accesos rápidos", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Todo lo que necesitas para la operación diaria.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tarjetas = listOf<@Composable (Modifier) -> Unit>(
                { cardModifier -> DashboardCard(
                titulo = "Productos",
                detalle = "Registro, stock y reposición",
                icono = Icons.Default.Inventory2,
                onClick = onProductosClick,
                modifier = cardModifier
            ) },
                { cardModifier -> DashboardCard(
                titulo = "Clientes",
                detalle = "Datos de contacto",
                icono = Icons.Default.Groups,
                onClick = onClientesClick,
                modifier = cardModifier
            ) },
                { cardModifier -> DashboardCard(
                titulo = "Pedidos",
                detalle = "Venta y detalle",
                icono = Icons.Default.ShoppingCartCheckout,
                onClick = onPedidosClick,
                modifier = cardModifier
            ) }
            )

            if (maxWidth >= 600.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        tarjetas.take(2).forEach { tarjeta -> tarjeta(Modifier.weight(1f)) }
                    }
                    tarjetas.last()(Modifier.fillMaxWidth())
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    tarjetas.forEach { tarjeta -> tarjeta(Modifier.fillMaxWidth()) }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(
    titulo: String,
    detalle: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = detalle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Abrir $titulo",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EstadoOperacion(onProductosClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onProductosClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(36.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Inventario listo para operar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = "Revisa existencias y detecta productos con bajo stock en un solo lugar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
