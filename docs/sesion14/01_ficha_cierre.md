# Sesión 14 · Paso 1. Cierre de la ficha de integración y congelamiento del alcance

Fecha: 10 de noviembre de 2026. Rama: `feature/builds-14` (creada desde `develop`, que se
adelantó hasta `feature/integrador-13`, commit `680ae2d`).

## Clasificación de los huecos de la sesión 13

| N.º | Hueco (ficha de la sesión 13) | Clasificación | Motivo / acción |
|---|---|---|---|
| 1 | Faltaban evidencias del recorrido de diez pasos | Cerrado | A01–A10 aprobadas y publicadas (commit `680ae2d`). |
| 2 | Faltaba probar 400 por campo, 403 y retorno por 401 | Cerrado | Comprobado contra PharmaSoft local el 10/11 (A07, A08, A09). |
| 3 | Bitácora con los tres estados del permiso | Cerrado | C00–C05 con diálogo nativo y recuperación desde Ajustes. |
| 4 | iOS sin compilación ni recorrido acreditados | Se declara | El entorno es Windows; Xcode exige macOS. Se documenta en el paso 6. |
| 5 | Edición y baja de productos sin cola offline | Se declara | Solo el alta tiene cola persistente. Cerrarlo exige cambiar repositorio y esquema, lo que está prohibido tras congelar. |
| 6 | Clientes registra un resultado académico y Pedidos es una vista pendiente | Se declara | No están integrados con la API; la integración implica nueva funcionalidad. |
| 7 | Borradores de formulario no guardados se pierden al cerrar sesión | Se descarta | Nunca estuvo en el alcance comprometido; queda como trabajo futuro. |
| 8 | Reenvío automático de un POST ambiguo | Se descarta | Decisión de diseño: PharmaSoft no garantiza idempotencia y la app pide confirmación. |
| 9 | Registros antiguos sin cuenta identificada | Cerrado | La app los asigna con revisión obligatoria (`recuperarPendientesAnteriores`), cubierto por pruebas host. |
| 10 | El emulador no alcanzó 10.0.2.2:8080 el 10/11 | Cerrado | Volvió a responder `HTTP 200` desde el emulador por 10.0.2.2 y por la IP de la red local; fue un estado transitorio del arranque. |

## Alcance congelado

Desde este paso no entra funcionalidad nueva, ni cambios de arquitectura, librerías, diseño de
pantallas o esquema de SQLDelight. Solo se permiten cambios de empaquetado:

- configuración de firma y versión en `androidApp/build.gradle.kts`;
- reglas de R8 en `androidApp/proguard-rules.pro`;
- `.gitignore`, README y documentación de la sesión.

El producto entregado es: inicio de sesión JWT con roles, CRUD de productos contra PharmaSoft,
inventario offline-first con cola de altas, categorías en caché, permiso de notificaciones con sus
tres estados y telemetría con Firebase Crashlytics.
