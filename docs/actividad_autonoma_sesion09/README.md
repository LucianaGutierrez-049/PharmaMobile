# Actividad Autónoma N.º 09

## Producto 1. Inventario de capacidades nativas

Inventario construido a partir del código real de PharmaMobile en la rama
`feature/expect-actual-Gutierrez`.

| Capacidad | Firma común | commonMain | Android y source set | API Android | iOS y source set | API iOS |
| --- | --- | --- | --- | --- | --- | --- |
| Formato de moneda | `expect fun formatearSoles(valor: Double): String` | `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/platform/Formato.kt` | `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/platform/Formato.android.kt` (`androidMain`) | `java.text.NumberFormat` y `java.util.Locale("es", "PE")` | `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/platform/Formato.ios.kt` (`iosMain`) | `NSNumberFormatter`, `NSLocale("es_PE")` y `NSNumber` de Foundation |
| Compartir producto | `interface Compartidor { fun compartir(texto: String) }` | `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/domain/platform/Compartidor.kt` | `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/platform/CompartidorAndroid.kt` (`androidMain`) | `Context`, `Intent.ACTION_SEND`, `Intent.EXTRA_TEXT`, `Intent.createChooser` y `FLAG_ACTIVITY_NEW_TASK` | `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/platform/CompartidorIos.kt` (`iosMain`) | `UIActivityViewController` y `UIApplication` de UIKit |
| Módulo de inyección | `expect val platformModule: Module` | `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/di/AppModule.kt` | `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/di/PlatformModule.android.kt` (`androidMain`) | Koin `module`, `androidContext()`, motor OkHttp y URL `10.0.2.2` | `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/di/PlatformModule.ios.kt` (`iosMain`) | Koin `module`, motor Darwin y URL `localhost` |
| Plataforma base | `expect fun getPlatform(): Platform` | `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/Platform.kt` | `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/Platform.android.kt` (`androidMain`) | `Build.VERSION.SDK_INT` | `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/Platform.ios.kt` (`iosMain`) | `UIDevice.currentDevice` de UIKit |
| Información del dispositivo | `expect class InfoDispositivo() { val sistema: String; val version: String }` | `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/platform/InfoDispositivo.kt` | `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/platform/InfoDispositivo.android.kt` (`androidMain`) | `Build.VERSION.RELEASE` | `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/platform/InfoDispositivo.ios.kt` (`iosMain`) | `UIDevice.currentDevice.systemName` y `systemVersion` de UIKit |

### Observación de arquitectura

`formatearSoles`, `platformModule`, `getPlatform` e `InfoDispositivo` emplean
`expect/actual`. `Compartidor` usa una interfaz común con inyección porque es una
capacidad con efecto sobre la interfaz nativa y necesita dependencias diferentes
por plataforma. La UI compartida solo consume contratos de `commonMain`.

## Producto 2. Informe comparativo

### 1. Formato de moneda con el mismo locale

Las dos implementaciones reciben el mismo `Double` y buscan representar soles
con configuración peruana, pero se ejecutan sobre bibliotecas diferentes. En
Android, `Formato.android.kt` llama a
`NumberFormat.getCurrencyInstance(Locale("es", "PE"))`; es la API de
internacionalización de la JVM y aplica las reglas disponibles en el runtime de
Java del dispositivo. En iOS, `Formato.ios.kt` crea un `NSNumberFormatter`, le
asigna `NSNumberFormatterCurrencyStyle` y `NSLocale("es_PE")`, y convierte el
`Double` a `NSNumber`; esas clases pertenecen a Foundation. El locale expresa la
intención cultural, pero no obliga a JVM y Foundation a producir exactamente la
misma secuencia de espacios o símbolos porque cada framework aporta sus propios
datos y convenciones de renderizado. En la ejecución Android real, el producto
Paracetamol 500 mg se mostró como `S/ 4.50`. El código iOS está escrito, pero no
se ejecutó desde Windows; por ello no se afirma todavía cuál será su resultado
literal y esa comparación queda pendiente de Mac/Xcode.

### 2. Context en Android y presentación en iOS

`CompartidorAndroid` recibe un `Context` porque iniciar una actividad es una
operación del modelo de componentes Android. La clase construye un
`Intent.ACTION_SEND`, configura `text/plain`, coloca el mensaje en
`Intent.EXTRA_TEXT` y crea el selector del sistema. Como Koin entrega el contexto
de aplicación mediante `androidContext()`, no existe una `Activity` como
receptora directa; por eso el selector incorpora `FLAG_ACTIVITY_NEW_TASK` antes
de llamar a `contexto.startActivity`. `CompartidorIos` no recibe un objeto
equivalente por constructor. UIKit expone el estado global de la aplicación por
`UIApplication.sharedApplication`; desde su ventana y controlador raíz presenta
un `UIActivityViewController` con el texto. Esta diferencia también se refleja
en la inyección: `PlatformModule.android.kt` crea
`CompartidorAndroid(androidContext())`, mientras que
`PlatformModule.ios.kt` registra `CompartidorIos()` sin argumentos. En ambos
casos `ProductoViewModel` depende únicamente de la interfaz `Compartidor`.

### 3. expect actual frente a interfaz e inyección

Si `formatearSoles` se hubiese modelado como interfaz, habría que definir un
contrato, crear implementaciones, registrarlas en los dos módulos Koin e
inyectar una instancia en cada consumidor. Esa estrategia mejora la sustitución
por dobles de prueba y es apropiada cuando la capacidad tiene estado,
dependencias o efectos. Sin embargo, para una función pura y pequeña aumenta el
código ceremonial y el acoplamiento de la presentación al contenedor. Con
`expect/actual`, el compilador selecciona la implementación del source set y
garantiza que firma, paquete y disponibilidad coincidan; la llamada común sigue
siendo directa y legible. La desventaja es que sustituirla en una prueba común no
es tan flexible como inyectar una interfaz y que la falta de un `actual` bloquea
la compilación del target. PharmaMobile usa ambas estrategias con intención:
`formatearSoles` es determinista, sin estado y adecuado para `expect/actual`;
`Compartidor` produce un efecto nativo, necesita `Context` en Android y se
beneficia de interfaz más inyección. Así, las pruebas del ViewModel pueden usar
un compartidor falso sin abrir UI nativa.

### 4. Ausencia de una implementación actual

La prueba se ejecutó de forma controlada comentando temporalmente la función de
`Formato.android.kt` y lanzando
`.\gradlew.bat :shared:compileAndroidMain --console=plain`. Kotlin detuvo la
compilación antes de generar el módulo Android. El mensaje literal obtenido fue:

```text
e: file:///C:/Users/lucia/AndroidStudioProjects/PHARMAMOBILEE/shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/platform/Formato.kt:3:1 Expected formatearSoles has no actual declaration in module <commonMain> for JVM
```

Después de registrar el resultado se restauró inmediatamente el `actual`. Esto
demuestra que `expect` no es una llamada opcional: cada target que compila el
código común debe suministrar una declaración compatible en paquete, nombre,
parámetros y retorno. El registro completo está en
`docs/actividad_autonoma_sesion09/error_actual_faltante.txt`.

### 5. Código que debe permanecer en commonMain

`ProductoViewModel` no debería bajar a `androidMain` ni a `iosMain`. Coordina
casos de uso, actualiza `ProductoUiState`, procesa `Result`, conserva el listado
durante las mutaciones, distribuye errores de validación por campo y vuelve a
cargar los productos después del CRUD. Ninguna de esas reglas depende de
`Context`, `Intent`, UIKit ni Foundation; son comportamiento de presentación y
negocio compartido. Duplicar el ViewModel por plataforma introduciría dos flujos
susceptibles de divergir, obligaría a repetir pruebas y podría generar mensajes
o transiciones diferentes para la misma respuesta de PharmaSoft. El límite
correcto es el que ya usa el proyecto: el ViewModel permanece en `commonMain` y
recibe contratos como `Compartidor`; solo la acción final que abre la interfaz
nativa vive en el source set específico. El mismo criterio mantiene en común el
repositorio REST, los casos de uso y las reglas de validación.

## Producto 3. Información del dispositivo

La tercera capacidad se implementó mediante el contrato común
`InfoDispositivo`, con las propiedades `sistema` y `version`. El `actual` de
Android devuelve `Android` y `Build.VERSION.RELEASE`; el de iOS obtiene
`UIDevice.currentDevice.systemName()` y
`UIDevice.currentDevice.systemVersion` desde UIKit.

La capacidad está conectada a la pantalla Compose
`presentacion/acerca/AcercaDeScreen.kt`. La navegación incorpora la opción
**Acerca de**, y la pantalla muestra dos valores obtenidos únicamente a través
del contrato común:

- `Sistema: Android` o el nombre real informado por iOS.
- `Versión: <versión informada por la plataforma>`.

La búsqueda global ejecutada después de la implementación fue:

```powershell
rg -n '^import\s+(android\.|platform\.)' shared/src/commonMain
```

Resultado real: **0 coincidencias**. La compilación
`:shared:compileAndroidMain` finalizó con `BUILD SUCCESSFUL`. Además,
`:shared:assemble` compiló `compileKotlinIosArm64` y
`compileKotlinIosSimulatorArm64` correctamente desde Windows. Las tareas de
enlace de los frameworks se omitieron y la ejecución iOS permanece pendiente de
macOS/Xcode.

## Estado de evidencias

- Android, moneda: disponible en `docs/evidencias_sesion09/evidencia_01_listado_android.png`.
- Android, compartir: disponible en `docs/evidencias_sesion09/evidencia_02_selector_compartir_android.png`.
- Android, información del dispositivo: disponible en
  `docs/actividad_autonoma_sesion09/evidencias/evidencia_03_info_dispositivo_android.png`.
- iOS, tres capacidades: **PENDIENTE DE MAC/XCODE**.
- Error por `actual` ausente: **CUMPLE**; mensaje literal guardado en
  `docs/actividad_autonoma_sesion09/error_actual_faltante.txt`.

## Producto 4. Plan exacto de evidencias

### Android 1. Formato de moneda

**Qué abrir:** PharmaMobile en el emulador Android y la sección Productos.

**Qué hacer:** esperar a que cargue el inventario de PharmaSoft.

**Qué debe verse:** la tarjeta de Paracetamol 500 mg con el precio literal
`S/ 4.50`.

**Cuándo tomar la captura:** cuando el nombre y el precio estén visibles en la
misma tarjeta. Evidencia ya disponible en
`docs/evidencias_sesion09/evidencia_01_listado_android.png`.

### Android 2. Compartir producto

**Qué abrir:** Productos y la tarjeta de Paracetamol 500 mg.

**Qué hacer:** pulsar el botón Compartir.

**Qué debe verse:** el selector nativo Android y el texto
`Paracetamol 500 mg - S/ 4.50 - Stock: 120`.

**Cuándo tomar la captura:** con el selector abierto y el texto visible.
Evidencia ya disponible en
`docs/evidencias_sesion09/evidencia_02_selector_compartir_android.png`.

### Android 3. Información del dispositivo

**Qué abrir:** menú lateral de PharmaMobile y opción **Acerca de**.

**Qué hacer:** entrar a la nueva pantalla.

**Qué debe verse:** título `Información del dispositivo`, `Sistema: Android` y
la versión real informada por `Build.VERSION.RELEASE`.

**Cuándo tomar la captura:** cuando los dos valores estén completamente
visibles. Evidencia real obtenida en el emulador `Medium_Phone`: sistema
`Android`, versión `17`. Archivo:
`docs/actividad_autonoma_sesion09/evidencias/evidencia_03_info_dispositivo_android.png`.

### Compilador. actual faltante

**Qué abrir:**
`shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/platform/Formato.android.kt`
y una terminal en la raíz del proyecto.

**Qué hacer:** comentar temporalmente las dos líneas de la función
`actual fun formatearSoles`, ejecutar
`.\gradlew.bat :shared:compileAndroidMain --console=plain` y restaurar el
archivo inmediatamente.

**Qué debe verse:**
`Expected formatearSoles has no actual declaration in module <commonMain> for JVM`.

**Cuándo tomar la captura:** con la ruta de `Formato.kt`, el mensaje literal y
la tarea `:shared:compileAndroidMain FAILED` visibles. La prueba ya fue ejecutada
y restaurada; falta únicamente la captura de pantalla de la consola si se exige
como imagen.

## Instrucciones para obtener las evidencias iOS

Estas tareas deben ejecutarse en una Mac con Xcode. No se consideran ejecutadas
hasta recibir las capturas reales.

1. Clonar o actualizar la rama `feature/expect-actual-Gutierrez`.
2. Levantar PharmaSoft en el puerto 8080.
3. Abrir `iosApp/iosApp.xcodeproj` en Xcode y seleccionar un simulador iPhone.
4. Ejecutar la app y esperar que el inventario cargue desde
   `http://localhost:8080/api/v1/`.
5. **Precio:** abrir Productos, localizar Paracetamol 500 mg y capturar la tarjeta
   con el precio. Registrar el texto literal producido por `NSNumberFormatter`;
   no asumir que será idéntico al de Android.
6. **Compartir:** pulsar Compartir en esa tarjeta y capturar
   `UIActivityViewController` con el texto del producto visible.
7. **Información del dispositivo:** abrir el menú, entrar a Acerca de y capturar
   el nombre y la versión reales suministrados por `UIDevice`.
8. Anotar nombre de la persona que ejecutó, modelo de simulador, versión de iOS,
   fecha y commit probado.

Estado de las tres capturas: **PENDIENTE DE MAC/XCODE**.

## Verificación automática real

- `:shared:assemble`: **BUILD SUCCESSFUL** en 2 min 10 s. Compiló el código
  Kotlin de `iosArm64` e `iosSimulatorArm64`, pero omitió el enlace de frameworks
  y no ejecutó iOS.
- `:shared:testAndroidHostTest`: **34 pruebas, 0 fallos, 0 errores y 0 omitidas**.
- `:androidApp:assembleDebug`: **BUILD SUCCESSFUL**.
- APK generado en
  `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- Ejecución Android: APK instalado y pantalla Acerca de verificada en el
  emulador `Medium_Phone`; mostró `Sistema: Android` y `Versión: 17`.

## Checklist de once puntos

| N.º | Verificación | Estado |
| --- | --- | --- |
| 1 | PDF preparado para llamarse `S09_ActividadAutonoma_Gutierrez.pdf` | PENDIENTE DE EJECUCIÓN REAL |
| 2 | Portada con nombre, sesión, fecha y enlace a la rama | PENDIENTE DE EJECUCIÓN REAL |
| 3 | Inventario con firma, archivos, source sets y APIs | CUMPLE |
| 4 | Cinco preguntas respondidas con código propio | CUMPLE |
| 5 | Pregunta 4 con mensaje literal del compilador | CUMPLE |
| 6 | `InfoDispositivo` tiene expect y dos actual diferentes | CUMPLE |
| 7 | Tercera capacidad conectada a la interfaz | CUMPLE |
| 8 | Sin imports `android.*` ni `platform.*` en commonMain | CUMPLE |
| 9 | Evidencias de las tres capacidades en Android e iOS | PENDIENTE DE MAC/XCODE |
| 10 | README contiene `Código específico de plataforma` | CUMPLE |
| 11 | Commits identificados en la rama propia | CUMPLE |

## Commits de la actividad

- `e0d3a08` - `docs: registra inventario de capacidades nativas`.
- `a1bfc75` - `docs: prepara comparacion de implementaciones por plataforma`.
- `2599ce6` - `feat: agrega informacion del dispositivo con expect actual`.

Rama de entrega:
`https://github.com/LucianaGutierrez-049/PharmaMobile/tree/feature/expect-actual-Gutierrez`.
Los commits permanecen locales hasta recibir autorización expresa para hacer
push.
