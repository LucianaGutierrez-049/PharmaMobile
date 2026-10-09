package pe.edu.upeu.pharmamobilee.presentacion.acerca

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobilee.platform.InfoDispositivo
import pe.edu.upeu.pharmamobilee.presentacion.components.SectionHeader

@Composable
fun AcercaDeScreen(
    modifier: Modifier = Modifier
) {
    val dispositivo = remember { InfoDispositivo() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SectionHeader(
            title = "Información del dispositivo",
            description = "Datos obtenidos con la API nativa de esta plataforma.",
            icon = Icons.Default.Info
        )

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                DatoDispositivo(
                    etiqueta = "Sistema",
                    valor = dispositivo.sistema,
                    icono = {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null
                        )
                    }
                )
                DatoDispositivo(
                    etiqueta = "Versión",
                    valor = dispositivo.version,
                    icono = {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        Text(
            text = "PharmaMobile usa un contrato común y una implementación distinta para Android e iOS.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DatoDispositivo(
    etiqueta: String,
    valor: String,
    icono: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icono()
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
