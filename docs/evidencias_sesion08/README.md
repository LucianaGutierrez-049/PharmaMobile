# Evidencias de la Guía Práctica N.º 08

## Verificación realizada

- `evidencia_android_listado.png`: inicio de PharmaMobile ejecutándose en el
  emulador Android.
- `evidencia_android_crud.png`: formulario para crear productos y listado
  conectado a PharmaSoft.
- `evidencia_android_inventario.png`: inventario remoto con los productos de
  PharmaSoft y acciones Editar y Eliminar.
- `evidencia_02_creacion_exitosa.png`: formulario limpiado durante la operación
  de creación real.
- `evidencia_02_producto_creado_en_lista.png`: producto `Guia08Gutierrez`
  creado con precio S/ 21.50 y stock 9.
- `evidencia_03_actualizacion_exitosa.png`: mensaje de actualización correcta.
- `evidencia_03_producto_actualizado_en_lista.png`: producto actualizado a
  `Guia08GutierrezEditado`, precio S/ 23.75 y stock 5.
- `evidencia_04_eliminacion_exitosa.png`: mensaje de eliminación correcta.
- `evidencia_04_producto_en_inactivos.png`: producto id 42 dado de baja y
  mostrado en la pestaña Inactivos.
- `evidencia_05_error_400_nombre.png`: respuesta 400 mostrada debajo del campo
  Nombre para el valor `AB`.
- `evidencia_06_log_ktor_exitoso.txt`: Logcat auténtico con POST 201, PUT 200 y
  DELETE 204.
- `evidencia_07_log_ktor_fallido.txt`: Logcat auténtico con POST 400.
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

## Evidencias completadas en Android

Se ejecutó el siguiente flujo completo desde PharmaMobile contra PharmaSoft:

1. Creación mediante POST 201.
2. Recarga y aparición del producto en Activos.
3. Actualización mediante PUT 200.
4. Recarga con nombre, precio y stock actualizados.
5. Eliminación mediante DELETE 204.
6. Recarga y aparición del producto en Inactivos.
7. Validación del nombre `AB` mediante POST 400 y mensaje debajo del campo.
8. Extracción del Logcat Ktor de operaciones exitosas y fallidas.

El control de escritorio disponible no expuso la ventana de Android Studio. Por
esa razón, los logs se conservaron directamente como texto auténtico extraído de
Logcat; no se generó una imagen artificial del registro.

La evidencia iOS queda pendiente porque la compilación y ejecución requieren
macOS, Xcode y un simulador iOS.
