package pe.edu.upeu.pharmamobilee.platform

import pe.edu.upeu.pharmamobilee.domain.platform.Compartidor
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

class CompartidorIos : Compartidor {
    override fun compartir(texto: String) {
        val controlador = UIActivityViewController(
            activityItems = listOf(texto),
            applicationActivities = null
        )
        UIApplication.sharedApplication.keyWindow
            ?.rootViewController
            ?.presentViewController(
                viewControllerToPresent = controlador,
                animated = true,
                completion = null
            )
    }
}
