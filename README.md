# PharmaMobile

Aplicación académica multiplataforma de gestión farmacéutica construida con
Kotlin Multiplatform y Compose Multiplatform.

## Alcance actual

- Modelos de dominio para productos, clientes y pedidos.
- CRUD remoto de productos con validaciones locales y del backend.
- Inventario con pestañas de activos, inactivos y bajo stock (`stock <= 5`).
- Navegación adaptable para teléfono, tableta y pantallas amplias.
- Clean Architecture, MVVM y Koin compartidos entre Android e iOS.
- Catálogo real de PharmaSoft gestionado con Ktor Client mediante GET, POST,
  PUT y DELETE.

## Conectividad REST

PharmaMobile se comunica con la API REST v1 de PharmaSoft. La URL base se
proporciona desde el módulo Koin de cada plataforma:

- Android Emulator: `http://10.0.2.2:8080/api/v1/`
- iOS Simulator: `http://localhost:8080/api/v1/`

El contrato CRUD implementado utiliza estas rutas:

| Método | Ruta | Estado de implementación móvil |
| --- | --- | --- |
| GET | `/api/v1/productos` | Implementado |
| GET | `/api/v1/productos/{id}` | Implementado |
| POST | `/api/v1/productos` | Implementado |
| PUT | `/api/v1/productos/{id}` | Implementado |
| DELETE | `/api/v1/productos/{id}` | Implementado |

El listado envía `pagina=0` y `tamanio=20`. PharmaSoft admite además los
parámetros opcionales `ordenarPor` (`id`, `nombre`, `precio` o `stock`) y
`direccion` (`asc` o `desc`). Si PharmaMobile no los envía, el servidor usa
`id` y `asc`.

PharmaSoft devuelve un objeto paginado. `PaginaResponseDto<ProductoResponseDto>`
deserializa `contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas` y
`ultima`. `ProductoResponseDto` refleja `id`, `nombre`, `precio`, `stock`,
`estado`, `categoriaId`, `categoriaNombre`, `fechaCreacion` y
`fechaModificacion`.

`ProductoRequestDto` contiene `nombre`, `precio`, `stock`, `estado` y
`categoriaId`. Los mappers convierten entre DTO y dominio sin introducir Ktor ni
`kotlinx.serialization` en la capa de presentación. El repositorio enlazado por
Koin es la implementación REST; el repositorio en memoria se conserva solamente
como alternativa no inyectada.

El cliente usa `ContentNegotiation` con `ignoreUnknownKeys = true`, timeout de
solicitud de 15 segundos y `Logging` en nivel `ALL` para registrar la petición,
cabeceras, estado y cuerpo durante las pruebas académicas. Los errores 404,
timeout, deserialización y conexión se convierten en mensajes controlados antes
de llegar a la interfaz.

Los errores HTTP se traducen en un único punto a `Validacion`, `NoEncontrado`,
`Conflicto`, `Servidor`, `SinConexion` o `TiempoAgotado`. La cancelación de una
corutina siempre se relanza. El `ViewModel` mantiene por separado la fase del
listado y la operación CRUD, expone errores por campo y recarga el inventario
después de crear, actualizar o eliminar.

## Manejo de errores

La capa de datos convierte los fallos técnicos en `ErrorApi` antes de que
lleguen al `ViewModel`:

| Situación | Error de dominio | Presentación |
| --- | --- | --- |
| HTTP 400 | `Validacion` | Mensajes debajo de `nombre`, `precio` o `stock` |
| HTTP 404 | `NoEncontrado` | Mensaje controlado de recurso no encontrado |
| HTTP 409 | `Conflicto` | Mensaje de la regla de negocio enviado por PharmaSoft |
| HTTP 5xx o respuesta inválida | `Servidor` | Mensaje controlado, sin mostrar excepciones técnicas |
| Backend inaccesible | `SinConexion` | Mensaje de conexión y opción de reintentar |
| Tiempo de espera agotado | `TiempoAgotado` | Mensaje para intentar nuevamente |

PharmaSoft realiza borrado lógico. Por ese motivo, un segundo `DELETE` sobre el
mismo producto devuelve actualmente HTTP 409 y se representa como
`ErrorApi.Conflicto`; no se documenta como 404 porque ese no es el comportamiento
real del backend. `CancellationException` no se convierte en un error visible:
se relanza para respetar la cancelación estructurada cuando termina el alcance
de la corutina.

## Seguridad de red local

- Android declara `INTERNET` y usa `network_security_config.xml` para autorizar
  HTTP únicamente hacia `10.0.2.2` y `localhost`.
- iOS declara `NSAllowsLocalNetworking` para el entorno local.
- La excepción de tráfico en claro es solo para desarrollo; producción debe usar
  HTTPS.

## Código específico de plataforma

La sesión 09 incorpora capacidades específicas de Android e iOS sin introducir
dependencias nativas en `commonMain`.

### formatearSoles

- Contrato (`commonMain`):
  `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/platform/Formato.kt`.
- Android (`androidMain`):
  `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/platform/Formato.android.kt`,
  con `NumberFormat` y `Locale("es", "PE")` de la JVM.
- iOS (`iosMain`):
  `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/platform/Formato.ios.kt`,
  con `NSNumberFormatter`, `NSNumber` y `NSLocale("es_PE")` de Foundation.
- `Producto.toUi()` aplica el formato monetario en la capa de presentación. El
  modelo de dominio conserva `precio` como `Double` y los composables reciben el
  texto ya formateado.

### Compartidor

- Contrato (`commonMain`):
  `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/domain/platform/Compartidor.kt`.
- Android (`androidMain`):
  `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/platform/CompartidorAndroid.kt`,
  con `Context`, `Intent.ACTION_SEND`, `Intent.EXTRA_TEXT`,
  `Intent.createChooser` y `FLAG_ACTIVITY_NEW_TASK`.
- iOS (`iosMain`):
  `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/platform/CompartidorIos.kt`,
  con `UIActivityViewController` y `UIApplication` de UIKit.
- La extensión
  `Producto.comoTextoParaCompartir()` arma una sola vez el mensaje con nombre,
  precio formateado y stock.
- Cada tarjeta del inventario muestra el precio nativo e incluye el botón
  **Compartir**. Compose solo invoca una acción común del ViewModel; no conoce
  `Context`, `Intent` ni UIKit.

### InfoDispositivo

- Contrato `expect` (`commonMain`):
  `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/platform/InfoDispositivo.kt`.
- Android (`androidMain`):
  `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/platform/InfoDispositivo.android.kt`,
  con `Build.VERSION.RELEASE`.
- iOS (`iosMain`):
  `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/platform/InfoDispositivo.ios.kt`,
  con `UIDevice.currentDevice.systemName()` y `systemVersion` de UIKit.
- La pantalla compartida
  `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/presentacion/acerca/AcercaDeScreen.kt`
  muestra el sistema y la versión sin importar APIs nativas.

### PlatformModule

- Contrato `expect` (`commonMain`):
  `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/di/AppModule.kt`.
- Android (`androidMain`):
  `shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/di/PlatformModule.android.kt`;
  registra el motor OkHttp, la URL del emulador y
  `CompartidorAndroid(androidContext())`.
- iOS (`iosMain`):
  `shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/di/PlatformModule.ios.kt`;
  registra el motor Darwin, la URL local y `CompartidorIos()`.
- `ProductoViewModel` recibe el contrato `Compartidor` por constructor mediante
  Koin.

El módulo Kotlin se expone a Swift como el framework `Shared`. `iOSApp.swift`
inicializa Koin mediante `KoinIosKt.initKoinIos()` y `ContentView.swift` obtiene
la interfaz Compose con `MainViewControllerKt.MainViewController()`.

Android se compila y prueba desde Windows. `:shared:assemble` también valida la
compilación Kotlin de `iosArm64` e `iosSimulatorArm64`, pero en Windows omite el
enlace de los frameworks y no ejecuta la aplicación. El formato monetario iOS,
la hoja de compartir y la pantalla de información del dispositivo permanecen
pendientes de ejecución real en una máquina macOS con Xcode.

## Verificación

```powershell
.\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug
```

Resultado de la verificación automática actual: `BUILD SUCCESSFUL` para
`:shared:assemble`, las pruebas compartidas y la compilación del APK Android.
Se ejecutaron 34 pruebas, con 0 fallos, 0 errores y 0 omitidas. Las pruebas cubren los
cinco métodos HTTP, el DELETE 204 sin cuerpo, el mapeo de errores 400 y 409, la
propagación de `CancellationException`, los mappers y los flujos de creación,
actualización y eliminación del `ViewModel`.

La ejecución del simulador iOS sigue pendiente porque requiere macOS y Xcode.
En macOS, iniciar PharmaSoft en el puerto 8080, abrir
`iosApp/iosApp.xcodeproj`, ejecutar un simulador y comprobar el mismo flujo.

Las evidencias están documentadas en `docs/evidencias_sesion07/README.md`.
La preparación de la Actividad Autónoma N.º 07, el catálogo completo, el
diccionario de DTO y la bitácora pendiente de ejecución están en
`docs/actividad_autonoma_sesion07/README.md`.
