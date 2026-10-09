package pe.edu.upeu.pharmamobilee.domain.usecase

import pe.edu.upeu.pharmamobilee.domain.model.Producto
import pe.edu.upeu.pharmamobilee.platform.formatearSoles

fun Producto.comoTextoParaCompartir(): String =
    "$nombre - ${formatearSoles(precio)} - Stock: $stock"
