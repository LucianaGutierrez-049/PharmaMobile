# Evidencias de la Guía Práctica N.º 07

Evidencias vigentes obtenidas el 4 de octubre de 2026 mediante ejecución real de
la APK debug en el AVD `Medium_Phone`, con PharmaSoft disponible en el puerto
8080.

## Evidencia 0 — Swagger UI y datos de prueba

- `evidencia_00_swagger_productos.png`: ejecución real de
  `GET /api/v1/productos?pagina=0&tamanio=20&ordenarPor=id&direccion=asc` en
  Swagger UI. La captura muestra el código HTTP `200` y los productos
  Paracetamol 500 mg, Ibuprofeno 400 mg y Naproxeno 550 mg almacenados en
  PharmaSoft.

## Evidencia 1 — Ktor Client contra PharmaSoft

- `evidencia_01_ktor_pharmasoft.txt`: extracto original de Logcat obtenido con
  ADB. Registra la petición
  `GET http://10.0.2.2:8080/api/v1/productos?pagina=0&tamanio=20` y
  `RESPONSE: 200`.
- `evidencia_01_ktor_pharmasoft.png`: visualización legible creada únicamente a
  partir de ese registro ADB auténtico; no es una captura fabricada de Android
  Studio.

## Evidencia 2 — Lista Android

- `evidencia_02_android_lista_pharmasoft.png`: muestra los tres registros reales
  de PharmaSoft: Paracetamol 500 mg (stock 120), Ibuprofeno 400 mg (stock 80) y
  Naproxeno 550 mg (stock 60).

## Evidencia 3 — iOS

Pendiente de ejecución manual en macOS con Xcode. Windows no puede ejecutar el
simulador iOS y no se generó una captura sustituta.

Pasos pendientes:

1. Iniciar PharmaSoft en `http://localhost:8080`.
2. Abrir `iosApp/iosApp.xcodeproj` en Xcode.
3. Ejecutar `iosApp` en un simulador de iPhone.
4. Abrir Productos, verificar los tres registros y tomar la captura.

## Evidencia 4 — Error y recuperación

- `evidencia_04_error_conexion_pharmasoft.png`: con modo avión activo, la app
  muestra un mensaje legible y el botón `Reintentar` sin cerrarse.
- Se restauró el estado original del AVD (`airplane=0`, `wifi=1`,
  `mobile_data=1`) y `Reintentar` recuperó los tres productos.

## Evidencias históricas

Los archivos sin el sufijo `_pharmasoft` pertenecen a la integración anterior
con `api.escuelajs.co`. Se conservan como historial del proyecto, pero no deben
presentarse como evidencia de la integración actual con PharmaSoft.
