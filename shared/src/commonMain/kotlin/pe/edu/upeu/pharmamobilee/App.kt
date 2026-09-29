package pe.edu.upeu.pharmamobilee

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobilee.domain.model.OrigenProducto
import pe.edu.upeu.pharmamobilee.navigation.Screen
import pe.edu.upeu.pharmamobilee.presentacion.cliente.ClienteScreen
import pe.edu.upeu.pharmamobilee.presentacion.inicio.InicioScreen
import pe.edu.upeu.pharmamobilee.presentacion.pedido.PedidoScreen
import pe.edu.upeu.pharmamobilee.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobilee.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobilee.theme.PharmaMobilTheme
import org.koin.compose.viewmodel.koinViewModel

private enum class TipoNavegacion {
    Compact,
    Medium,
    Expanded
}

private data class OpcionNavegacion(
    val screen: Screen,
    val titulo: String,
    val icono: ImageVector
)

private val opcionesNavegacion = listOf(
    OpcionNavegacion(Screen.Inicio, "Inicio", Icons.Default.Home),
    OpcionNavegacion(Screen.Productos, "Productos", Icons.Default.Medication),
    OpcionNavegacion(Screen.Clientes, "Clientes", Icons.Default.Person),
    OpcionNavegacion(Screen.Pedidos, "Pedidos", Icons.Default.ShoppingCart)
)

@Composable
fun App() {
    var pantallaActual by remember {
        mutableStateOf<Screen>(Screen.Inicio)
    }

    var darkTheme by remember {
        mutableStateOf(false)
    }

    PharmaMobilTheme(
        darkTheme = darkTheme
    ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                val tipoNavegacion = when {
                    maxWidth < 600.dp -> TipoNavegacion.Compact
                    maxWidth < 840.dp -> TipoNavegacion.Medium
                    else -> TipoNavegacion.Expanded
                }

                PharmaMobilLayout(
                    pantallaActual = pantallaActual,
                    onSeleccionarPantalla = {
                        pantallaActual = it
                    },
                    darkTheme = darkTheme,
                    onDarkThemeChange = {
                        darkTheme = it
                    },
                    tipoNavegacion = tipoNavegacion
                )
            }
    }
}

@Composable
private fun PharmaMobilLayout(
    pantallaActual: Screen,
    onSeleccionarPantalla: (Screen) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    tipoNavegacion: TipoNavegacion
) {
    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()

    when (tipoNavegacion) {
        TipoNavegacion.Compact -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        DrawerContent(
                            pantallaActual = pantallaActual,
                            onSeleccionarPantalla = { screen ->
                                onSeleccionarPantalla(screen)
                                scope.launch {
                                    drawerState.close()
                                }
                            },
                            darkTheme = darkTheme,
                            onDarkThemeChange = onDarkThemeChange
                        )
                    }
                }
            ) {
                PharmaMobilScaffold(
                    pantallaActual = pantallaActual,
                    onSeleccionarPantalla = onSeleccionarPantalla,
                    darkTheme = darkTheme,
                    onDarkThemeChange = onDarkThemeChange,
                    mostrarMenu = true,
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        }

        TipoNavegacion.Medium -> {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight()
                ) {
                    opcionesNavegacion.forEach { opcion ->
                        NavigationRailItem(
                            selected = pantallaActual::class == opcion.screen::class,
                            onClick = {
                                onSeleccionarPantalla(opcion.screen)
                            },
                            icon = {
                                Icon(
                                    imageVector = opcion.icono,
                                    contentDescription = opcion.titulo
                                )
                            },
                            label = {
                                Text(opcion.titulo)
                            }
                        )
                    }

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )

                    ThemeSwitch(
                        darkTheme = darkTheme,
                        onDarkThemeChange = onDarkThemeChange,
                        compacto = true
                    )
                }

                PharmaMobilScaffold(
                    modifier = Modifier.weight(1f),
                    pantallaActual = pantallaActual,
                    onSeleccionarPantalla = onSeleccionarPantalla,
                    darkTheme = darkTheme,
                    onDarkThemeChange = onDarkThemeChange,
                    mostrarMenu = false,
                    onMenuClick = {}
                )
            }
        }

        TipoNavegacion.Expanded -> {
            PermanentNavigationDrawer(
                drawerContent = {
                    PermanentDrawerSheet {
                        DrawerContent(
                            pantallaActual = pantallaActual,
                            onSeleccionarPantalla = onSeleccionarPantalla,
                            darkTheme = darkTheme,
                            onDarkThemeChange = onDarkThemeChange
                        )
                    }
                }
            ) {
                PharmaMobilScaffold(
                    pantallaActual = pantallaActual,
                    onSeleccionarPantalla = onSeleccionarPantalla,
                    darkTheme = darkTheme,
                    onDarkThemeChange = onDarkThemeChange,
                    mostrarMenu = false,
                    onMenuClick = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PharmaMobilScaffold(
    modifier: Modifier = Modifier,
    pantallaActual: Screen,
    onSeleccionarPantalla: (Screen) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    mostrarMenu: Boolean,
    onMenuClick: () -> Unit
) {
    val productoViewModel = koinViewModel<ProductoViewModel>()
    val productoUiState by productoViewModel.uiState.collectAsState()
    val productosLocales = productoUiState.productos.filter {
        it.origen == OrigenProducto.LOCAL
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(text = tituloPantalla(pantallaActual), style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "PharmaMobil",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    if (mostrarMenu) {
                        IconButton(
                            onClick = onMenuClick
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Abrir menú"
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onDarkThemeChange(!darkTheme) }) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (darkTheme) "Activar modo claro" else "Activar modo oscuro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (pantallaActual) {
                Screen.Inicio -> {
                    InicioScreen(
                        totalProductos = productosLocales.size,
                        productosActivos = productosLocales.count { it.activo },
                        productosBajoStock = productosLocales.count { it.requiereReposicion },
                        onProductosClick = { onSeleccionarPantalla(Screen.Productos) },
                        onClientesClick = { onSeleccionarPantalla(Screen.Clientes) },
                        onPedidosClick = { onSeleccionarPantalla(Screen.Pedidos) }
                    )
                }

                Screen.Productos -> {
                    ProductoScreen(
                        viewModel = productoViewModel
                    )
                }

                Screen.Clientes -> {
                    ClienteScreen()
                }

                Screen.Pedidos -> {
                    PedidoScreen()
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    pantallaActual: Screen,
    onSeleccionarPantalla: (Screen) -> Unit,
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxHeight()) {
        DrawerHeader()
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        Text(
            text = "NAVEGACIÓN",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 28.dp, top = 20.dp, bottom = 8.dp)
        )

        opcionesNavegacion.forEach { opcion ->
            NavigationDrawerItem(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                label = { Text(opcion.titulo) },
                selected = pantallaActual::class == opcion.screen::class,
                onClick = { onSeleccionarPantalla(opcion.screen) },
                icon = {
                    Icon(imageVector = opcion.icono, contentDescription = opcion.titulo)
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        ThemeSwitch(
            darkTheme = darkTheme,
            onDarkThemeChange = onDarkThemeChange
        )
    }
}

@Composable
private fun DrawerHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Icon(
                imageVector = Icons.Default.LocalPharmacy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(12.dp).size(28.dp)
            )
        }

        Column {
            Text(text = "PharmaMobil", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Gestión farmacéutica",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemeSwitch(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    compacto: Boolean = false
) {
    if (compacto) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Switch(
                checked = darkTheme,
                onCheckedChange = onDarkThemeChange
            )
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Modo oscuro"
        )

        Switch(
            checked = darkTheme,
            onCheckedChange = onDarkThemeChange
        )
    }
}

private fun tituloPantalla(
    screen: Screen
): String {
    return when (screen) {
        Screen.Inicio -> "Inicio"
        Screen.Productos -> "Productos"
        Screen.Clientes -> "Clientes"
        Screen.Pedidos -> "Pedidos"
    }
}
