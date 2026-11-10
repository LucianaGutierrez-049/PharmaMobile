package pe.edu.upeu.pharmamobilee.presentacion.cliente

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import pe.edu.upeu.pharmamobilee.domain.model.Cliente
import pe.edu.upeu.pharmamobilee.presentacion.components.EmptyState
import pe.edu.upeu.pharmamobilee.presentacion.components.ErrorState
import pe.edu.upeu.pharmamobilee.presentacion.components.LoadingState
import pe.edu.upeu.pharmamobilee.presentacion.components.SectionHeader
import pe.edu.upeu.pharmamobilee.presentacion.components.ValidatedTextField

@Composable
fun ClienteScreen(viewModel: ClienteViewModel, puedeEscribir: Boolean) {
    val estado by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var porDarDeBaja by remember { mutableStateOf<Cliente?>(null) }

    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let { snackbar.showSnackbar(it); viewModel.limpiarMensaje() }
    }
    porDarDeBaja?.let { c ->
        AlertDialog(
            onDismissRequest = { porDarDeBaja = null },
            title = { Text("Dar de baja cliente") },
            text = { Text("¿Deseas dar de baja a ${c.nombreCompleto}? Quedará inactivo y no podrá recibir nuevas ventas.") },
            confirmButton = { TextButton(onClick = { viewModel.darDeBaja(c); porDarDeBaja = null }) { Text("Dar de baja") } },
            dismissButton = { TextButton(onClick = { porDarDeBaja = null }) { Text("Cancelar") } }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(
                title = "Clientes",
                description = "Registra y mantén actualizados los clientes de la farmacia.",
                icon = Icons.Default.Person
            )
            if (puedeEscribir && !estado.formularioVisible) {
                Button(onClick = viewModel::nuevo, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Icon(Icons.Default.PersonAdd, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Nuevo cliente")
                }
            }
            if (puedeEscribir && estado.formularioVisible) ClienteFormularioCard(estado, viewModel)

            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::buscar,
                label = { Text("Buscar por nombre, DNI o correo") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            when {
                estado.cargando && estado.clientes.isEmpty() -> LoadingState(title = "Cargando clientes")
                estado.error != null && estado.clientes.isEmpty() -> ErrorState(
                    message = estado.error!!, onRetry = viewModel::cargar, titulo = "No pudimos cargar los clientes"
                )
                estado.filtrados.isEmpty() -> EmptyState(
                    icon = Icons.Default.Person,
                    title = if (estado.clientes.isEmpty()) "Aún no hay clientes" else "Sin coincidencias",
                    description = if (estado.clientes.isEmpty()) "Registra el primer cliente con el botón Nuevo cliente."
                    else "Prueba con otro nombre, DNI o correo.",
                    actionLabel = "Actualizar",
                    onAction = viewModel::cargar
                )
                else -> {
                    Text(
                        "${estado.filtrados.size} clientes",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    estado.filtrados.forEach { c ->
                        ClienteCard(
                            cliente = c,
                            puedeEscribir = puedeEscribir,
                            onEditar = { viewModel.editar(c) },
                            onBaja = { porDarDeBaja = c }
                        )
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(16.dp))
    }
}

@Composable
private fun ClienteFormularioCard(estado: ClienteUiState, vm: ClienteViewModel) {
    val f = estado.formulario
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                if (estado.editandoId == null) "Nuevo cliente" else "Editar cliente",
                style = MaterialTheme.typography.titleLarge
            )
            ValidatedTextField(f.dni, { v -> vm.cambiar { it.copy(dni = v.filter(Char::isDigit).take(8)) } }, "DNI",
                f.errores["dni"], { Icon(Icons.Default.Badge, null) }, KeyboardType.Number)
            ValidatedTextField(f.nombres, { v -> vm.cambiar { it.copy(nombres = v) } }, "Nombres",
                f.errores["nombres"], { Icon(Icons.Default.Person, null) })
            ValidatedTextField(f.apellidos, { v -> vm.cambiar { it.copy(apellidos = v) } }, "Apellidos",
                f.errores["apellidos"], { Icon(Icons.Default.Person, null) })
            ValidatedTextField(f.email, { v -> vm.cambiar { it.copy(email = v) } }, "Correo",
                f.errores["email"], { Icon(Icons.Default.Email, null) }, KeyboardType.Email)
            ValidatedTextField(f.telefono, { v -> vm.cambiar { it.copy(telefono = v.filter(Char::isDigit).take(9)) } },
                "Teléfono (opcional)", f.errores["telefono"], { Icon(Icons.Default.Phone, null) }, KeyboardType.Phone)
            ValidatedTextField(f.direccion, { v -> vm.cambiar { it.copy(direccion = v) } }, "Dirección (opcional)",
                f.errores["direccion"], { Icon(Icons.Default.Home, null) })
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Cliente activo", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (f.activo) "Puede recibir ventas" else "No podrá recibir ventas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = f.activo, onCheckedChange = { v -> vm.cambiar { it.copy(activo = v) } })
            }
            Button(onClick = vm::guardar, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                if (estado.guardando) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text(if (estado.editandoId == null) "Registrar cliente" else "Guardar cambios")
            }
            OutlinedButton(onClick = vm::cerrarFormulario, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth()) {
                Text("Cancelar")
            }
        }
    }
}

@Composable
private fun ClienteCard(cliente: Cliente, puedeEscribir: Boolean, onEditar: () -> Unit, onBaja: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Text(cliente.nombres.take(1).uppercase(), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(cliente.nombreCompleto, style = MaterialTheme.typography.titleMedium)
                Text("DNI ${cliente.dni} · ${cliente.obtenerTelefono()}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(cliente.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (cliente.activo) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (cliente.activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            if (puedeEscribir) {
                IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, "Editar ${cliente.nombreCompleto}") }
                if (cliente.activo) IconButton(onClick = onBaja) {
                    Icon(Icons.Default.Delete, "Dar de baja ${cliente.nombreCompleto}", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
