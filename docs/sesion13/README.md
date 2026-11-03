# Sesión 13: integración en PharmaMobil

## Cambios implementados

- SQLDelight: migración `2.sqm` (versión 2 a 3), intentos por producto, último error, revisión necesaria y próxima fecha de envío.
- Sincronización serializada con `Mutex`, confirmación local transaccional y conservación de pendientes al descargar.
- Clasificación de errores de dominio: red, timeout y servidor son transitorios; validación, permisos, sesión, conflicto y recurso inexistente requieren intervención.
- Las descargas GET reintentan hasta cinco veces con pausas de 1, 2, 4 y 8 segundos. No se repite automáticamente un POST: no consta soporte de idempotencia en PharmaSoft. Una creación ambigua requiere revisión y confirmación del usuario; cada reenvío respeta la espera persistida. Tras agotar cinco intentos, la confirmación explícita inicia un nuevo ciclo.
- Una marca se persiste antes del POST: una interrupción del proceso no provoca un reenvío automático al abrir nuevamente la app.
- La pantalla Productos muestra pendientes, filas que requieren revisión, intentos, último error y antigüedad de la descarga. Permite corregir datos o confirmar un reenvío; el aviso explica el riesgo de duplicados.
- Un fallo al borrar en el servidor conserva la copia local.
- Interfaz común `GestorPermisos`, implementación Android con tres estados, puente de Activity y refresco al volver de Ajustes. Implementación iOS mediante UserNotifications. La tarjeta permite solicitar notificaciones y enviar una notificación local de prueba.
- `Telemetria` es una interfaz de dominio; GitLive Crashlytics solo aparece en datos. Se registran eventos técnicos de sincronización, sin mensajes originales de excepciones, tokens ni datos de formularios.
- Pruebas de política de reintentos, permisos y repositorio offline.

## Activar Firebase Android

1. En Firebase, registrar una aplicación Android con el paquete `pe.edu.upeu.pharmamobilee`.
2. Descargar la configuración real a `androidApp/google-services.json`. No usar archivos de otro proyecto.
3. Volver a sincronizar Gradle: Google Services y Crashlytics se aplican cuando existe ese archivo.
4. Habilitar Crashlytics en la consola y comprobar allí un error no fatal tras ejecutar una sincronización fallida y reiniciar la app.

La aplicación puede compilar sin configuración Firebase; en ese caso no envía telemetría. El archivo está excluido de Git. No se creó un proyecto remoto ni se validó recepción en consola. No se implementó mensajería push: la capacidad nativa demostrada es una notificación local y el módulo Firebase integrado es Crashlytics.

## iOS

Agregar FirebaseCore y FirebaseCrashlytics al proyecto Xcode según la instalación de GitLive/Firebase, incorporar `GoogleService-Info.plist` al target y configurar la fase de carga de símbolos de Crashlytics. El arranque Swift configura Firebase cuando el SDK y el plist están disponibles. Notificaciones se solicitan con UserNotifications; este permiso no requiere una clave de descripción en Info.plist. No se solicitan cámara ni ubicación.

La compilación y las pruebas iOS requieren macOS/Xcode y no se verifican desde este equipo Windows. La notificación local de prueba iOS se programa para dos segundos después: enviar la app a segundo plano para verla.

## Tabla de huecos identificados por inspección

Esta tabla es una revisión de código; no sustituye el recorrido en un dispositivo real.

| Hallazgo | Gravedad | Decisión |
|---|---|---|
| Creaciones pendientes repetidas sin conocer el resultado previo | Alta | Resuelto mediante bloqueo persistido y confirmación explícita; para automatizarlas se necesita idempotencia en el backend. |
| No se conservaban intentos ni causa del error | Alta | Resuelto con migración y campos observables. |
| Un fallo detenía el resto de la cola | Media | Resuelto: los siguientes registros siguen procesándose. |
| Borrado local antes de confirmar el remoto | Alta | Resuelto: no se elimina localmente si falla el servidor. |
| No existía gestión de permisos | Media | Implementada para notificaciones Android/iOS; faltan evidencias manuales por plataforma. |
| Falta configuración de Firebase real | Media | Integración preparada; completar con configuración de la cuenta y comprobar recepción. |
| Formularios se descartan cuando cambia la sesión | Media | Sesión 14: diseñar borrador asociado al usuario; no almacenar datos del formulario en telemetría. |
| Caché local no está particionada por usuario | Alta | Sesión 14: separar bases o filas por usuario y definir conservación de pendientes. No limpiar pendientes silenciosamente ni probar cuentas distintas en el mismo dispositivo hasta resolverlo. |
| No hay cola offline de modificaciones y eliminaciones remotas | Media | Sesión 14: definir operaciones locales PUT/DELETE y conflictos. Las creaciones existentes sí funcionan offline. |

## Verificación manual y evidencias pendientes

1. Instalar una versión previa con datos y actualizar a esta compilación: verificar la migración sin reinstalar.
2. Iniciar sesión, crear un producto sin red y comprobar que conserva sus datos y muestra el error e intentos.
3. Restablecer conexión y sincronizar: el pendiente ambiguo no se reenvía solo. Revisar el servidor, confirmar el reenvío y comprobar la conciliación.
4. Provocar validación/conflicto: verificar que queda visible y que las otras filas avanzan.
5. Capturar Productos con pendientes, errores y fecha de actualización.
6. Con una instalación nueva, capturar permiso pendiente/denegado, concedido y bloqueo permanente. Volver de Ajustes y comprobar el refresco. Las opciones exactas de denegación dependen del sistema.
7. Pulsar Probar notificación y comprobar el aviso nativo. En iOS, pasar la aplicación a segundo plano.
8. Comprobar errores de credenciales, token vencido, estado vacío y cierre de sesión. Registrar evidencias reales; no están generadas por las pruebas unitarias.

## Comandos

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug --console=plain
```

Los tests `commonTest` existentes usan SQLite JDBC, por lo que su ejecución validada corresponde al target Android host. No representan una ejecución nativa iOS.

## Resultado de verificación, 3 de noviembre de 2026

- `:shared:testAndroidHostTest`: 67 pruebas ejecutadas, 0 fallos, 0 errores, 0 omitidas.
- `:androidApp:assembleDebug`: BUILD SUCCESSFUL. APK generado en `androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- Incluye 12 pruebas del repositorio offline, 2 de permisos y 2 de la política de sincronización.
- `git diff --check`: sin errores de espacios.
- Para evitar que Java agote la memoria de este equipo, la ejecución utiliza `--no-daemon --max-workers=1`, `-Dorg.gradle.jvmargs=-Xmx1536m -XX:ActiveProcessorCount=2 -Dfile.encoding=UTF-8` y `-Pkotlin.compiler.execution.strategy=in-process`.
- Pendientes de verificación externa: instalación y capturas Android reales, recepción en Firebase e iOS con macOS/Xcode.

Referencias de implementación: [GitLive SDK](https://github.com/GitLiveApp/firebase-kotlin-sdk), [Crashlytics Android](https://firebase.google.com/docs/crashlytics/android/get-started).

## Cierre de evidencias y control de versiones

Se obtuvieron capturas reales del estado sincronizado, registro conservado sin servicio, fila en revisión, tres estados del permiso, notificación local, copia SQLite, código real, reporte Gradle e historial Git. Los originales están en `docs/sesion13/capturas` y las condiciones exactas de cada prueba en `validacion_android.md`.

La rama `feature/integrador-13` contiene siete commits por paso y el ajuste adicional `72acc38` para habilitar opcionalmente el túnel ADB en depuración. Los hashes por paso están en `commits_sesion13.json`. No se hizo push.

El POST de prueba respondió 404; el producto se conservó localmente. No se acredita un alta remota exitosa. Firebase real e iOS siguen pendientes. La suite de 67 pruebas fue previa al ajuste de URL; el ajuste posterior compiló y se verificó con REST real.

El informe completado desde la plantilla de Luciana está en `output/docx/Informe_Guia_Practica_13_Luciana_Gutierrez.docx`. Los extractos de código y Git se capturaron desde un visor local real; no se presentan como capturas de Android Studio o de terminal.
