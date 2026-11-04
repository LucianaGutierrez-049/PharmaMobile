package pe.edu.upeu.pharmamobilee.domain.error

import pe.edu.upeu.pharmamobilee.domain.model.Producto

/** El servidor rechazó el envío, pero el borrador ya está conservado localmente. */
class ProductoPendienteException(val producto: Producto, val fallo: Throwable) :
    Exception("El producto sigue guardado en el dispositivo.", fallo)
