package pe.edu.upeu.pharmamobilee.domain.usecase

import kotlin.test.Test
import kotlin.test.assertContains
import pe.edu.upeu.pharmamobilee.domain.model.Producto

class TextoParaCompartirTest {
    @Test
    fun textoIncluyeNombrePrecioFormateadoYStock() {
        val producto = Producto(
            id = 1L,
            nombre = "Paracetamol 500 mg",
            precio = 4.5,
            stock = 120
        )

        val texto = producto.comoTextoParaCompartir()

        assertContains(texto, "Paracetamol 500 mg")
        assertContains(texto, "S/")
        assertContains(texto, "Stock: 120")
    }
}
