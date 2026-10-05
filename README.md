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

## Seguridad de red local

- Android declara `INTERNET` y usa `network_security_config.xml` para autorizar
  HTTP únicamente hacia `10.0.2.2` y `localhost`.
- iOS declara `NSAllowsLocalNetworking` para el entorno local.
- La excepción de tráfico en claro es solo para desarrollo; producción debe usar
  HTTPS.

## Verificación

```powershell
.\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug
```

Resultado de la verificación automática de la Guía 08: `BUILD SUCCESSFUL` para
las pruebas compartidas y la compilación del APK Android. Las pruebas cubren los
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
