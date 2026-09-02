# Evidencias y decisiones - Sesion 4 Reto 02

Fuente principal revisada: Guia Autonoma de Actividad - Sesion 4, "Proyecto de mejora: implementacion de Tabs, theming y navegacion adaptativa en PharmaMobil".

## Decisiones tecnicas

- Navegacion principal: se conserva `Screen + when` con `Scaffold` y `TopAppBar`, sin agregar Navigation Compose.
- Compact: se usa `ModalNavigationDrawer`, adecuado para telefono.
- Medium: se usa `NavigationRail`, adecuado para tablet o ancho intermedio.
- Expanded: se usa `PermanentNavigationDrawer`, adecuado para escritorio, foldable o pantallas amplias.
- Breakpoints: `maxWidth < 600.dp` para Compact, `maxWidth < 840.dp` para Medium y desde `840.dp` para Expanded.
- Productos: `ProductoScreen` se mantiene como formulario operativo y se integra debajo del inventario con Tabs.
- Inventario: los datos simulados se mantienen en memoria; los productos registrados se agregan a la lista visible y los existentes pueden editarse desde la tarjeta del inventario.
- Edicion: cada producto muestra una accion de editar para actualizar nombre, precio, stock y estado activo/inactivo sin duplicar el registro.
- Bajo stock: se aplica la regla estricta `stock <= 5`. Un producto con `stock == 0`, como Loratadina, aparece en Bajo stock aunque este inactivo porque las categorias no son excluyentes.
- Theming: la identidad visual se centraliza en `PharmaMobilTheme` con `ColorScheme`, `Typography` y `Shapes`.
- Recursos compartidos: `pharmamobil_logo.xml` vive en `composeResources/drawable` y se consume desde `InicioScreen`.

## Matriz de pruebas funcionales

| Prueba | Accion | Resultado esperado | Estado |
| --- | --- | --- | --- |
| 1 | Inicio -> Productos -> Clientes -> Pedidos | Navegacion completa sin perdida inesperada de estado | Pendiente de captura visual |
| 2 | Tabs Activos / Inactivos / Bajo stock | Filtrado correcto del mock data | Cubierto por `ProductoFiltroTest` |
| 3 | Light Theme | Textos, campos, botones, drawer y tabs legibles | Pendiente de captura visual |
| 4 | Dark Theme | Superficies y textos adaptados sin inversion simple blanco/negro | Pendiente de captura visual |
| 5 | Registrar Paracetamol / 15.50 / 100 | Registro exitoso y actualizacion de lista | Pendiente de captura visual |
| 6 | Amoxicilina / 25.00 / 5 | Clasificacion automatica en Bajo stock | Cubierto por `ProductoFiltroTest` |
| 7 | Editar un producto existente | Actualizacion de precio, stock o estado sin duplicar el producto | Pendiente de captura visual |

## Checklist final

- [x] Mantiene funcionando la navegacion del Reto 01.
- [x] Mantiene operativo el componente ProductoScreen.
- [x] Implementa Tabs funcionales en Productos: Activos, Inactivos y Bajo stock.
- [x] Utiliza datos simulados estructurados correctamente.
- [x] Aplica paleta de colores corporativa mediante Material 3.
- [x] Implementa Light Theme y Dark Theme desde un tema centralizado.
- [x] Incorpora y consume un recurso compartido.
- [x] Documenta las decisiones de diseno adaptativo para telefono y tablet.
- [x] Proyecto compila.
- [x] Aplicacion inicia en emulador sin `FATAL EXCEPTION` observado en logcat.
- [x] No existen imports rojos detectados por compilacion.
- [x] No se agregaron dependencias innecesarias.

## Comandos de verificacion ejecutados

```powershell
.\gradlew.bat :shared:compileAndroidMain
.\gradlew.bat :shared:testAndroidHostTest
.\gradlew.bat :androidApp:assembleDebug
```

Tambien se instalo y abrio la APK debug en el emulador conectado:

```powershell
adb install -r androidApp\build\outputs\apk\debug\androidApp-debug.apk
adb shell monkey -p pe.edu.upeu.pharmamobilee -c android.intent.category.LAUNCHER 1
```

## Capturas sugeridas para el informe

1. Inicio con el logo PharmaMobil visible.
2. Drawer modal abierto en telefono con Inicio, Productos, Clientes, Pedidos y switch de modo oscuro.
3. Pantalla Productos, tab Activos.
4. Pantalla Productos, tab Inactivos mostrando Loratadina.
5. Pantalla Productos, tab Bajo stock mostrando Amoxicilina, Loratadina y Diclofenaco.
6. Formulario ProductoScreen con registro exitoso.
7. Producto registrado visible en la lista despues de guardar.
8. Modo claro en Inicio o Productos.
9. Modo oscuro en Inicio o Productos.
10. Vista tablet/ancha mostrando NavigationRail o PermanentNavigationDrawer.
11. Formulario en modo editar cambiando un producto de activo a inactivo o viceversa.
