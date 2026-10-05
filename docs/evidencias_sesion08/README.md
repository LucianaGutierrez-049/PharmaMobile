# Evidencias de la Guía Práctica N.º 08

## Verificación realizada

- `evidencia_android_listado.png`: inicio de PharmaMobile ejecutándose en el
  emulador Android.
- `evidencia_android_crud.png`: formulario para crear productos y listado
  conectado a PharmaSoft.
- `evidencia_android_inventario.png`: inventario remoto con los productos de
  PharmaSoft y acciones Editar y Eliminar.
- Pruebas automatizadas: 32 pruebas, 0 fallos y 0 errores.
- Compilación Android: `:androidApp:assembleDebug` finalizó correctamente.
- Prueba de integración contra PharmaSoft:
  - POST creó el producto de prueba con id 41.
  - GET recuperó el producto creado.
  - PUT actualizó nombre, precio y stock.
  - DELETE respondió HTTP 204.
  - GET posterior confirmó el borrado lógico con `estado=false`.
- Un POST inválido respondió HTTP 400 con mensajes independientes para
  `nombre`, `precio` y `stock`, compatibles con los errores por campo de la UI.

El registro de prueba quedó inactivo con el nombre
`Evidencia CRUD Gutierrez Editado`; no se alteraron los productos iniciales.

## Evidencias manuales que faltan para la entrega

Con PharmaSoft activo en el puerto 8080, ejecutar la aplicación en Android y
capturar estas acciones completas:

1. Registrar un producto válido y mostrar el mensaje de éxito junto con el
   producto recargado en el inventario.
2. Editar ese producto y mostrar los valores actualizados después de la recarga.
3. Eliminarlo, confirmar la operación y mostrarlo en la pestaña Inactivos.
4. Enviar un nombre inválido para mostrar el mensaje de validación junto al
   campo correspondiente.
5. Detener temporalmente PharmaSoft, pulsar Reintentar y capturar el mensaje de
   conexión controlado; volver a iniciar el backend al terminar.

La evidencia iOS queda pendiente porque la compilación y ejecución requieren
macOS, Xcode y un simulador iOS.
