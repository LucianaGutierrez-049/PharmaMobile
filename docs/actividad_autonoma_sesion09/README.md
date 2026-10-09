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

## Estado de evidencias

- Android, moneda: disponible en `docs/evidencias_sesion09/evidencia_01_listado_android.png`.
- Android, compartir: disponible en `docs/evidencias_sesion09/evidencia_02_selector_compartir_android.png`.
- Android, información del dispositivo: **PENDIENTE DE EJECUCIÓN REAL**.
- iOS, tres capacidades: **PENDIENTE DE MAC/XCODE**.
- Error por `actual` ausente: **PENDIENTE DE EJECUCIÓN REAL**.
