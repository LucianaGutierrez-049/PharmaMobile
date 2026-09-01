package pe.edu.upeu.pharmamobilee.navigation

sealed class Screen {
    data object Inicio : Screen()

    data object Productos : Screen()

    data object Clientes : Screen()

    data object Pedidos : Screen()
}