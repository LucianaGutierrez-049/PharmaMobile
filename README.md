# PharmaMobile

Aplicación académica multiplataforma de gestión farmacéutica construida con
Kotlin Multiplatform y Compose Multiplatform.

## Alcance actual

- Modelos de dominio para productos, clientes y pedidos.
- Registro y edición local de productos con validaciones de negocio.
- Inventario con pestañas de activos, inactivos y bajo stock (`stock <= 5`).
- Navegación adaptable para teléfono, tableta y pantallas amplias.
- Clean Architecture, MVVM y Koin compartidos entre Android e iOS.
- Catálogo real de PharmaSoft consultado con Ktor Client mediante GET.

## Conectividad REST

PharmaMobile se comunica con la API REST v1 de PharmaSoft. La URL base se
proporciona desde el módulo Koin de cada plataforma:

- Android Emulator: `http://10.0.2.2:8080/api/v1/`
- iOS Simulator: `http://localhost:8080/api/v1/`

Petición implementada actualmente en la aplicación móvil:

```text
GET productos?pagina=0&tamanio=20
```

PharmaSoft admite además los parámetros opcionales `ordenarPor` (`id`,
`nombre`, `precio` o `stock`) y `direccion` (`asc` o `desc`). Si PharmaMobile
no los envía, el servidor usa `id` y `asc`.

El contrato CRUD documentado para la Unidad 2 utiliza estas rutas:

| Método | Ruta | Estado de implementación móvil |
| --- | --- | --- |
| GET | `/api/v1/productos` | Implementado |
| GET | `/api/v1/productos/{id}` | Documentado; pendiente de la sesión 8 |
| POST | `/api/v1/productos` | Documentado; pendiente de la sesión 8 |
| PUT | `/api/v1/productos/{id}` | Documentado; pendiente de la sesión 8 |
| DELETE | `/api/v1/productos/{id}` | Documentado; pendiente de la sesión 8 |

PharmaSoft devuelve un objeto paginado. `PaginaResponseDto<ProductoResponseDto>`
deserializa `contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas` y
`ultima`. `ProductoResponseDto` refleja `id`, `nombre`, `precio`, `stock`,
`estado`, `categoriaId`, `categoriaNombre`, `fechaCreacion` y
`fechaModificacion`.

El mapper convierte el DTO al modelo `Producto` utilizando el stock y el estado
reales. El dominio no depende de Ktor ni de `kotlinx.serialization`. El listado
muestra únicamente `contenido` del backend; no mezcla productos simulados.

El cliente usa `ContentNegotiation` con `ignoreUnknownKeys = true`, timeout de
solicitud de 15 segundos y `Logging` en nivel `ALL` para registrar la petición,
cabeceras, estado y cuerpo durante las pruebas académicas. Los errores 404,
timeout, deserialización y conexión se convierten en mensajes controlados antes
de llegar a la interfaz.

Las operaciones de registro y actualización siguen en memoria porque POST y PUT
corresponden a la sesión siguiente. Por ese motivo, un producto registrado desde
la aplicación no aparece en el listado remoto.

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

Resultado verificado el 4 de octubre de 2026: `BUILD SUCCESSFUL`. En el AVD
`Medium_Phone`, Ktor ejecutó el GET contra PharmaSoft y recibió HTTP 200. La UI
mostró Paracetamol 500 mg, Ibuprofeno 400 mg y Naproxeno 550 mg. En modo avión se
mostró un error recuperable y, al restaurar la conexión, `Reintentar` volvió a
cargar los tres productos.

La ejecución del simulador iOS sigue pendiente porque requiere macOS y Xcode.
En macOS, iniciar PharmaSoft en el puerto 8080, abrir
`iosApp/iosApp.xcodeproj`, ejecutar un simulador y comprobar el mismo flujo.

Las evidencias están documentadas en `docs/evidencias_sesion07/README.md`.
La preparación de la Actividad Autónoma N.º 07, el catálogo completo, el
diccionario de DTO y la bitácora pendiente de ejecución están en
`docs/actividad_autonoma_sesion07/README.md`.
