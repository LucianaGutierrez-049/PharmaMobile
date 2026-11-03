# Validación Android de sesión 13

Se recuperó la sesión y GET productos respondió 200 mediante túnel ADB. Se capturaron denegación, bloqueo definitivo y permiso concedido desde Ajustes.

El registro S13_EVIDENCIA_20261103 (precio 2.50, stock 10) recibió 404 en el POST. Se conservó con intentos 1/5, motivo y confirmación de reenvío. Ese POST aprovechó una conexión persistente todavía abierta al retirar el túnel: NO se atribuye el 404 a ausencia de red.

Para validar offline se cerró el proceso, se deshabilitaron Wi-Fi y datos, y se abrió sin túnel. Se conservaron sesión y producto. SQLite real mostró versión 3 y una fila pendiente con id -1. La UI cuenta ese pendiente como una fila que requiere revisión, no como un envío automático elegible.

Se restauraron conectividad y túnel. No se confirmó el reenvío ni se eliminó el registro de prueba. Firebase real e iOS siguen pendientes. Los visores muestran código real, Git real y el HTML original Gradle.

Las 67 pruebas corresponden a la ejecución previa al ajuste de URL; el ajuste posterior compiló y se validó mediante REST real en Android.
