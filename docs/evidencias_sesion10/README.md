# Evidencias de la Guía Práctica N.º 10

## Autenticación JWT entre PharmaMobile y PharmaSoft

Estudiante: Luciana Gutierrez  
Proyecto móvil: PharmaMobile  
Backend: PharmaSoft  
Fecha de verificación: 13 de octubre de 2026

## 1. Recursos revisados

- Guía `sesion10_guia_practica_autenticacion_jwt_anaqufq03e.pdf`.
- Colección `pharmabackend - pruebas de seguridad.postman_collection_xt0bliniew.json`.
- Scripts V2 y V3 contenidos en `sql_smt01pxo64.rar`.
- Código oficial actualizado de PharmaSoft, commit `fff44f9` (`seguridad`).
- Proyecto Kotlin Multiplatform PharmaMobile.

La colección Postman se utilizó como especificación de escenarios y respuestas.
No se añadió Postman al proyecto móvil ni se incluyeron tokens o secretos en el
repositorio.

## 2. Oracle y PharmaSoft

Se reutilizó la instancia local existente, sin crear una configuración Docker
nueva para PharmaMobile:

| Elemento | Configuración comprobada |
| --- | --- |
| Contenedor | `pharmasoft-oracle` |
| Imagen | `gvenzl/oracle-free:23.26.1-slim-faststart` |
| Puerto host | `127.0.0.1:1522` |
| Servicio Oracle | `FREEPDB1` |
| Esquema de aplicación | `PHARMADB` |
| Volumen persistente | `pharmasoft-oracle-data` |

Se verificó una conexión SQL real con el esquema de aplicación. PharmaSoft se
compiló y arrancó con Spring Boot en el puerto 8080. Flyway validó tres
migraciones y aplicó correctamente `V2__seguridad_empleados_usuarios.sql` y
`V3__datos_iniciales_seguridad.sql`, dejando el esquema en la versión 3.

El archivo V2 del RAR contiene una línea suelta `BD SEGURIDAD` que no es SQL
válido. No se copió de manera ciega. La versión oficial de PharmaSoft conserva
el mismo modelo y corrige esa línea como comentario, por lo que esa fue la
migración ejecutada.

## 3. Pruebas reales del backend

Las siguientes pruebas se ejecutaron contra `http://localhost:8080`:

| Escenario | Resultado |
| --- | --- |
| `GET /api/health` sin token | HTTP 200 |
| `GET /api/v1/productos` sin token | HTTP 401 |
| Login de `admin` | HTTP 200, Bearer, JWT de tres segmentos, rol ADMIN |
| `GET /api/v1/productos` con token ADMIN | HTTP 200 |
| `GET /api/v1/auth/me` con token ADMIN | HTTP 200 |
| Login de `almacen` | HTTP 200, rol ALMACENERO |
| Productos con ALMACENERO | Permitido por `CATALOGO_LEER` |
| Clientes con ALMACENERO | HTTP 403 |

Los tokens reales no se copiaron en esta documentación. La respuesta 401 fue
“Debe iniciar sesión para acceder a este recurso” y la respuesta 403 fue “No
tiene permisos para realizar esta operación”.

## 4. Implementación móvil

### Cliente y DTO

- `HttpClientFactory` instala `ContentNegotiation`, timeout, `Logging` seguro y
  `Auth` con proveedor Bearer.
- `/auth/login` nunca recibe una cabecera Bearer previa.
- La cabecera `Authorization` se censura en depuración y el logging se desactiva
  en producción.
- `LoginRequestDto`, `LoginResponseDto` y `UsuarioActualDto` reflejan el contrato
  real de PharmaSoft.
- `SesionMapper` evita exponer DTO o Ktor en la presentación.

### Repositorio y sesión

- `AuthRepository` define login, recuperación y cierre de sesión.
- `AuthRepositorioRest` llama a `/auth/login` y `/auth/me`, guarda o elimina el
  token y limpia la caché Bearer cuando cambia la sesión.
- Android cifra el token mediante Android Keystore con AES/GCM.
- iOS usa Keychain Services. El código iOS fue compilado, pero no se ejecutó por
  no disponer de macOS y Xcode.

### MVVM, Koin y Compose

- `SesionViewModel` valida credenciales, expone estados de carga/error y restaura
  una sesión al abrir la aplicación.
- Koin inyecta el almacenamiento seguro, repositorio, gestor de sesión y
  ViewModels.
- Compose muestra login, progreso, errores, usuario, rol y cierre de sesión.
- Las opciones de navegación se filtran por permisos.
- Los controles de crear, editar y eliminar productos solo aparecen con
  `CATALOGO_ESCRIBIR`.
- Un 401 borra el token y regresa al login; un 403 conserva la sesión y muestra
  un mensaje controlado.

## 5. Verificación automática

Comando ejecutado:

```powershell
.\gradlew.bat :shared:testAndroidHostTest :shared:compileKotlinIosSimulatorArm64 :androidApp:assembleDebug
```

Resultado comprobado antes del ajuste final de documentación:

- APK Android generado correctamente.
- Kotlin para iOS Simulator ARM64 compilado correctamente.
- 39 pruebas Android host: 39 aprobadas, 0 fallos, 0 errores y 0 omitidas.
- Las pruebas nuevas validan que login no envíe `Authorization`, que el token se
  guarde, que Bearer se adjunte automáticamente y que `/auth/me` recupere la
  sesión.

## 6. Matriz de evaluación

| N.º | Criterio | Estado | Evidencia técnica |
| ---: | --- | --- | --- |
| 1 | Dependencias y configuración Ktor Auth | CUMPLE | Catálogo Gradle y `shared/build.gradle.kts` |
| 2 | DTO de autenticación acordes al backend | CUMPLE | `LoginDto.kt` |
| 3 | Mapper DTO a dominio | CUMPLE | `SesionMapper.kt` |
| 4 | Repositorio de autenticación | CUMPLE | `AuthRepository.kt` y `AuthRepositorioRest.kt` |
| 5 | Bearer automático y login excluido | CUMPLE | `HttpClientFactory.kt` y pruebas MockEngine |
| 6 | Persistencia segura del token | CUMPLE EN CÓDIGO | Keystore Android y Keychain iOS |
| 7 | Koin y ViewModel de sesión | CUMPLE | módulos DI y `SesionViewModel.kt` |
| 8 | Login, logout y recuperación de sesión | CUMPLE EN CÓDIGO | Compose, `/auth/me` y limpieza del token |
| 9 | Manejo de 401/403 y permisos | CUMPLE | `EjecutarLlamada.kt`, ViewModel y navegación filtrada |
| 10 | Verificación multiplataforma | PARCIAL | Android compila y tiene APK; iOS compila, pero su ejecución requiere Mac/Xcode |

## 7. Capturas reales y descripción

Se ejecutó PharmaMobile en Android y la colección de seguridad en Postman contra
el backend local. Las imágenes están incluidas en el informe PDF, con título,
descripción y resultado. Las respuestas de login se capturaron en Test Results
para no publicar los JWT ni las contraseñas de prueba.

| Imagen | Descripción y resultado comprobado |
| --- | --- |
| `03_admin.png` | Estado de carga: Autenticando, botón y campos deshabilitados. |
| `04_resultado_login.png` | Login ADMIN exitoso: acceso a Inicio. |
| `05_menu_admin.png` | Usuario y rol ADMIN; Productos, Clientes y Pedidos disponibles. |
| `06_sesion_recuperada.png` | Inicio después de detener y reabrir la app; `/auth/me` respondió 200. |
| `07_logout.png` | Regreso al login y mensaje Sesión cerrada correctamente. |
| `08_almacen.png` | Credenciales inválidas: mensaje Usuario o contraseña incorrectos. |
| `10_menu_almacen.png` | ALMACENERO con Productos; Clientes y Pedidos ocultos. |
| `11_productos_bearer.png` | Inventario remoto con tres productos; escritura habilitada para ALMACENERO. |
| `12_postman_login_401.jpg` | Contraseña incorrecta: HTTP 401 y mensaje controlado. |
| `13_postman_health_200.jpg` | `/api/health` sin token: HTTP 200 y backend funcionando. |
| `14_postman_sin_token_401.jpg` | `/auth/me` sin token: HTTP 401. |
| `15_postman_admin_200.jpg` | Login ADMIN: HTTP 200 y 4/4 tests aprobados. |
| `16_postman_almacen_200.jpg` | Login ALMACENERO: HTTP 200 y 4/4 tests aprobados. |

Logcat real confirmó `Authorization: ***` en GET productos y `/auth/me`, ambos
con HTTP 200. La prueba de recuperación se realizó deteniendo el proceso y
reabriendo la aplicación; no se ingresaron nuevamente las credenciales.

Las pruebas iOS siguen declaradas como no ejecutadas por falta de Mac/Xcode.

## 8. Archivos principales modificados o creados

- `gradle/libs.versions.toml`
- `shared/build.gradle.kts`
- `shared/src/commonMain/.../data/remote/HttpClientFactory.kt`
- `shared/src/commonMain/.../data/remote/dto/LoginDto.kt`
- `shared/src/commonMain/.../data/mapper/SesionMapper.kt`
- `shared/src/commonMain/.../data/repository/AuthRepositorioRest.kt`
- `shared/src/commonMain/.../domain/model/Sesion.kt`
- `shared/src/commonMain/.../domain/repository/AuthRepository.kt`
- `shared/src/commonMain/.../presentation/sesion/`
- `shared/src/androidMain/.../data/local/AlmacenSeguroAndroid.kt`
- `shared/src/iosMain/.../data/local/AlmacenSeguroIos.kt`
- `shared/src/commonMain/.../di/AppModule.kt`
- `shared/src/commonMain/.../App.kt`
- `README.md`

## 9. Estado de Git

El trabajo de la sesión se realiza en `feature/auth-jwt-Gutierrez`. Los cambios
de la Sesión 08 que ya existían en el árbol de trabajo no forman parte de esta
implementación y deben mantenerse fuera de los commits de autenticación. No se
realiza push en esta entrega.
