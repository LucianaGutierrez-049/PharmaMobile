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

## 6. Matriz de los diez criterios reales de la guía

| N.º | Criterio | Estado | Evidencia técnica |
| ---: | --- | --- | --- |
| 1 | Login obtiene token real | CUMPLE | Los tres usuarios oficiales autenticaron con 200 |
| 2 | Almacenamiento seguro | CUMPLE EN ANDROID | Keystore AES/GCM y sesión recuperada; implementación Keychain conservada |
| 3 | Auth adjunta Authorization automáticamente | CUMPLE | Prueba MockEngine y CRUD real autenticado |
| 4 | sendWithoutRequest evita solicitud previa sin token | CUMPLE | Rutas protegidas con envío anticipado; login excluido |
| 5 | Logging sanea Authorization | CUMPLE | `22_logcat_saneado.jpg`, Authorization: *** |
| 6 | Sesión sobrevive al cierre/reapertura | CUMPLE | Proceso detenido y app reabierta, `/auth/me` 200 |
| 7 | 401 borra token y regresa al login | CUMPLE | `28_android_401_login.png`; jwt_cifrado ausente tras 401 |
| 8 | 403 mantiene sesión y muestra mensaje | CUMPLE | `27_android_403.png`; pantalla Productos permanece abierta |
| 9 | UI oculta acciones según permisos | CUMPLE | ADMIN, VENDEDOR sin escritura y ALMACENERO sin Clientes/Pedidos |
| 10 | Funcionamiento multiplataforma | IOS OMITIDO POR SOLICITUD | Android ejecutado; iOS no probado por falta de Mac/Xcode |

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
| `19_crud_creado.png` | POST autenticado: producto EvidenciaJWT_S10, id 61, HTTP 201. |
| `20_crud_listado.png` | GET autenticado: producto de prueba visible en el catálogo. |
| `21_crud_actualizado.png` | PUT autenticado: precio 14 y stock 18, HTTP 200. |
| `22_logcat_saneado.jpg` | Captura Android Studio del registro Authorization: ***. |
| `23_crud_eliminado.png` | Confirmación de baja lógica del producto de prueba. |
| `24_crud_delete_ok.png` | DELETE autenticado: mensaje de éxito, HTTP 204, estado FALSE en Oracle. |
| `25_menu_vendedor.png` | Login y menú del tercer usuario oficial VENDEDOR. |
| `26_vendedor_sin_escritura.png` | Catálogo de VENDEDOR sin formulario, edición ni eliminación. |
| `27_android_403.png` | 403 real: mensaje de falta de permiso y sesión conservada. |
| `28_android_401_login.png` | 401 real: token eliminado y regreso al login. |

Logcat real confirmó `Authorization: ***` en GET productos y `/auth/me`, ambos
con HTTP 200. La prueba de recuperación se realizó deteniendo el proceso y
reabriendo la aplicación; no se ingresaron nuevamente las credenciales.

### Procedimiento controlado de 401/403

Para el 403 se abrió una sesión ALMACENERO y se preparó un formulario válido.
Se cambió temporalmente la asignación local de la cuenta `almacen` a VENDEDOR.
PharmaSoft vuelve a consultar permisos en Oracle en cada solicitud: el POST
recibió 403 y Android mostró “No tienes permisos para realizar esta operación”,
sin cerrar sesión. Se restauró el rol ALMACENERO en un bloque finally.

Para el 401 se deshabilitó temporalmente la misma cuenta local de prueba. La
siguiente solicitud recibió 401 y la app eliminó el JWT y volvió al login con
“Tu sesión expiró. Inicia sesión nuevamente”. Se restauró estado TRUE en finally.
Esta prueba verifica revocación y manejo de 401; no se presenta como espera real
de la expiración temporal del JWT.

Comprobaciones finales: `almacen` conserva ALMACENERO y estado TRUE; los tres
usuarios vuelven a autenticar con HTTP 200; Prueba403_S10 no existe; el producto
EvidenciaJWT_S10 queda de baja lógica (FALSE). No se alteraron código ni
migraciones de PharmaSoft ni productos anteriores.

Las pruebas y capturas iOS se omiten por solicitud expresa y falta de Mac/Xcode.
No se afirma cumplimiento de ejecución iOS.

### Entregables de la práctica

- Rama: `feature/auth-jwt-Gutierrez`, publicada en GitHub.
- Login real con ADMIN, VENDEDOR y ALMACENERO: verificado en Android.
- CRUD autenticado Android: GET/POST/PUT/DELETE verificados con capturas.
- Registro Authorization saneado: captura real de Android Studio.
- 403 deliberado y mensaje de la app: verificado y capturado.
- README con Autenticación y usuarios oficiales: actualizado.
- Commits reales: `1fc3834` (implementación, 16:13) y `8ed2928`
  (documentación inicial, 16:28), el 13 de octubre de 2026. La verificación
  adicional se incorpora como trabajo posterior; no se inventan fechas ni
  commits de etapas de implementación que no se registraron en su momento.

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
