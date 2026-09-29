# Evidencias de la Guía Práctica N.º 07

Evidencias obtenidas el 29 de setiembre de 2026 mediante ejecución real de la
APK debug en el AVD `Medium_Phone`.

## Evidencia 1 — Ktor Client

- `evidencia_01_ktor_logcat.png`: captura legible del Logcat real con la
  petición `GET products?limit=10` y la respuesta HTTP `200`.
- `evidencia_01_ktor_logcat.txt`: registro original extraído mediante ADB.

## Evidencia 2 — Android

- `evidencia_02_android_lista.png`: lista de productos remotos renderizada en
  Compose e identificada mediante la etiqueta `Catálogo API`.

## Evidencia 3 — iOS

Pendiente de ejecución manual en macOS con Xcode. El host utilizado para esta
verificación es Windows y no puede ejecutar el simulador iOS. No se fabricó una
captura sustituta.

## Evidencia 4 — Error de conexión

- `evidencia_04_error_conexion.png`: aplicación ejecutada con modo avión,
  mostrando un error legible y el botón `Reintentar` sin cerrarse.

Después de restaurar la conectividad se pulsó `Reintentar`; Logcat volvió a
registrar una petición al endpoint y una respuesta HTTP `200`.
