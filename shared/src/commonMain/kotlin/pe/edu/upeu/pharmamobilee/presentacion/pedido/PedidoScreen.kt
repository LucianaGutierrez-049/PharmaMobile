package pe.edu.upeu.pharmamobilee.presentacion.pedido

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobilee.presentacion.components.EmptyState
import pe.edu.upeu.pharmamobilee.presentacion.components.SectionHeader

@Composable
fun PedidoScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        SectionHeader(
            title = "Gestión de pedidos",
            description = "Consulta el estado general de las ventas.",
            icon = Icons.AutoMirrored.Filled.ReceiptLong
        )

        EmptyState(
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            title = "Aún no hay pedidos para mostrar",
            description = "Este módulo se ampliará en sesiones posteriores sin adelantar funcionalidades del curso."
        )
    }
}
