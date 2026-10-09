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

## Estado de evidencias

- Android, moneda: disponible en `docs/evidencias_sesion09/evidencia_01_listado_android.png`.
- Android, compartir: disponible en `docs/evidencias_sesion09/evidencia_02_selector_compartir_android.png`.
- Android, información del dispositivo: **PENDIENTE DE EJECUCIÓN REAL**.
- iOS, tres capacidades: **PENDIENTE DE MAC/XCODE**.
- Error por `actual` ausente: **PENDIENTE DE EJECUCIÓN REAL**.
