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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.navigation.Screen
import pe.edu.upeu.pharmamobilee.presentacion.cliente.ClienteScreen
import pe.edu.upeu.pharmamobilee.presentacion.inicio.InicioScreen
import pe.edu.upeu.pharmamobilee.presentacion.pedido.PedidoScreen
import pe.edu.upeu.pharmamobilee.presentacion.producto.InventarioTabs
import pe.edu.upeu.pharmamobilee.presentacion.producto.ProductoScreen
import pe.edu.upeu.pharmamobilee.presentacion.producto.productosInventarioInicial
import pe.edu.upeu.pharmamobilee.theme.PharmaMobilTheme

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

    val productos = remember {
        mutableStateListOf<Producto>().also {
            it.addAll(productosInventarioInicial)
        }
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
                productos = productos,
                onRegistrarProducto = { producto ->
                    productos.add(
                        producto.copy(
                            id = (productos.maxOfOrNull { it.id } ?: 0L) + 1L
                        )
                    )
                },
                onActualizarProducto = { productoActualizado ->
                    val index = productos.indexOfFirst { it.id == productoActualizado.id }
                    if (index >= 0) {
                        productos[index] = productoActualizado
                    }
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
    productos: List<Producto>,
    onRegistrarProducto: (Producto) -> Unit,
    onActualizarProducto: (Producto) -> Unit,
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
                    mostrarMenu = true,
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    productos = productos,
                    onRegistrarProducto = onRegistrarProducto,
                    onActualizarProducto = onActualizarProducto
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
                    pantallaActual = pantallaActual,
                    mostrarMenu = false,
                    onMenuClick = {},
                    productos = productos,
                    onRegistrarProducto = onRegistrarProducto,
                    onActualizarProducto = onActualizarProducto
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
                    mostrarMenu = false,
                    onMenuClick = {},
                    productos = productos,
                    onRegistrarProducto = onRegistrarProducto,
                    onActualizarProducto = onActualizarProducto
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PharmaMobilScaffold(
    pantallaActual: Screen,
    mostrarMenu: Boolean,
    onMenuClick: () -> Unit,
    productos: List<Producto>,
    onRegistrarProducto: (Producto) -> Unit,
    onActualizarProducto: (Producto) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = tituloPantalla(pantallaActual)
                    )
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
                }
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
                    InicioScreen()
                }

                Screen.Productos -> {
                    ProductosContenido(
                        productos = productos,
                        onRegistrarProducto = onRegistrarProducto,
                        onActualizarProducto = onActualizarProducto
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
private fun ProductosContenido(
    productos: List<Producto>,
    onRegistrarProducto: (Producto) -> Unit,
    onActualizarProducto: (Producto) -> Unit
) {
    var seccionSeleccionada by remember {
        mutableStateOf(0)
    }

    var productoEnEdicion by remember {
        mutableStateOf<Producto?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrimaryTabRow(
            selectedTabIndex = seccionSeleccionada
        ) {
            Tab(
                selected = seccionSeleccionada == 0,
                onClick = {
                    seccionSeleccionada = 0
                },
                text = {
                    Text("Inventario")
                }
            )

            Tab(
                selected = seccionSeleccionada == 1,
                onClick = {
                    productoEnEdicion = null
                    seccionSeleccionada = 1
                },
                text = {
                    Text(if (productoEnEdicion == null) "Registrar" else "Editar")
                }
            )
        }

        if (seccionSeleccionada == 0) {
            InventarioTabs(
                productos = productos,
                onEditarProducto = { producto ->
                    productoEnEdicion = producto
                    seccionSeleccionada = 1
                }
            )
        } else {
            ProductoScreen(
                onRegistrar = { producto ->
                    onRegistrarProducto(producto)
                    productoEnEdicion = null
                    seccionSeleccionada = 0
                },
                productoEnEdicion = productoEnEdicion,
                onActualizar = { producto ->
                    onActualizarProducto(producto)
                    productoEnEdicion = null
                    seccionSeleccionada = 0
                },
                onCancelarEdicion = {
                    productoEnEdicion = null
                    seccionSeleccionada = 0
                },
                modifier = Modifier.fillMaxWidth()
            )
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
    DrawerHeader()

    opcionesNavegacion.forEach { opcion ->
        NavigationDrawerItem(
            label = {
                Text(opcion.titulo)
            },
            selected = pantallaActual::class == opcion.screen::class,
            onClick = {
                onSeleccionarPantalla(opcion.screen)
            },
            icon = {
                Icon(
                    imageVector = opcion.icono,
                    contentDescription = opcion.titulo
                )
            }
        )
    }

    Spacer(
        modifier = Modifier.padding(8.dp)
    )

    ThemeSwitch(
        darkTheme = darkTheme,
        onDarkThemeChange = onDarkThemeChange
    )
}

@Composable
private fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "PharmaMobil",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Gestión farmacéutica",
            style = MaterialTheme.typography.bodyMedium
        )
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
