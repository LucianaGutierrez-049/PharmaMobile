# Actividad Autónoma N.º 07 de PharmaMobile

Este documento reúne la información técnica verificable que servirá para crear
posteriormente `S07_ActividadAutonoma_Gutierrez.pdf`. No sustituye las capturas
de ejecución ni registra como ejecutada una prueba pendiente.

## 1. Catálogo real de endpoints de Producto

### Identificación del servicio

- API: PharmaSoft.
- Versión contractual: `v1`, expresada en la ruta `/api/v1/`.
- URL del backend y Swagger en Windows: `http://localhost:8080`.
- URL base desde Android Emulator: `http://10.0.2.2:8080/api/v1/`.
- URL base desde iOS Simulator: `http://localhost:8080/api/v1/`.
- Swagger: `http://localhost:8080/swagger-ui.html`.
- OpenAPI: `http://localhost:8080/v3/api-docs`.

La metadata generada por Springdoc muestra `info.version = v0`, pero el contrato
real expuesto por `ProductoController` está versionado como `/api/v1`. Esta
diferencia de metadata no cambia las rutas consumidas por PharmaMobile.

### GET listado de productos

| Dato | Valor verificado |
| --- | --- |
| Método y ruta | `GET /api/v1/productos` |
| Parámetros de ruta | Ninguno |
| Query | `pagina: Int = 0`, `tamanio: Int = 20`, `ordenarPor: String = "id"`, `direccion: String = "asc"` |
| Ordenamientos admitidos | `id`, `nombre`, `precio`, `stock` |
| Respuesta exitosa | `200 OK` con `PaginaResponseDTO<ProductoResponseDTO>` |
| Errores verificados en código | `400` por tipo de parámetro inválido; `409` por página, tamaño, campo o dirección no permitidos; `500` por error no controlado |
| Estado en PharmaMobile | Implementado; la app envía `pagina` y `tamanio`, y usa los valores predeterminados del servidor para ordenamiento |

La ejecución real contra PharmaSoft devolvió `200`, tres productos, página `0`,
tamaño `20`, una página total y `ultima = true`.

### GET producto por identificador

| Dato | Valor verificado |
| --- | --- |
| Método y ruta | `GET /api/v1/productos/{id}` |
| Parámetro de ruta | `id: Long`, obligatorio |
| Query | Ninguno |
| Respuesta exitosa | `200 OK` con `ProductoResponseDTO` |
| Errores | `400` si el identificador no es numérico; `404` si no existe; `500` por error no controlado |
| Estado en PharmaMobile | Documentado, no implementado aún en la aplicación |

La ejecución controlada con `id = 999999999` devolvió realmente `404` y el
mensaje del backend `Producto no encontrado con id: 999999999`.

### POST crear producto

| Dato | Valor verificado |
| --- | --- |
| Método y ruta | `POST /api/v1/productos` |
| Parámetros de ruta o query | Ninguno |
| Body | `ProductoRequestDTO` completo |
| Respuesta exitosa del controlador | `201 Created` con `ProductoResponseDTO` |
| Errores | `400` por validación o JSON inválido; `404` si la categoría no existe; `409` por nombre duplicado o restricción de integridad; `500` por error no controlado |
| Estado en PharmaMobile | Documentado, no implementado aún en la aplicación |

```json
{
  "nombre": "Producto evidencia S07",
  "precio": 10.50,
  "stock": 15,
  "estado": true,
  "categoriaId": 1
}
```

### PUT actualizar producto

| Dato | Valor verificado |
| --- | --- |
| Método y ruta | `PUT /api/v1/productos/{id}` |
| Parámetro de ruta | `id: Long`, obligatorio |
| Query | Ninguno |
| Body | `ProductoRequestDTO` completo; PUT reemplaza los cinco valores editables |
| Respuesta exitosa | `200 OK` con `ProductoResponseDTO` actualizado |
| Errores | `400` por validación, tipo o JSON inválido; `404` por producto o categoría inexistente; `409` por nombre duplicado o integridad; `500` por error no controlado |
| Estado en PharmaMobile | Documentado, no implementado aún en la aplicación |

### DELETE dar de baja un producto

| Dato | Valor verificado |
| --- | --- |
| Método y ruta | `DELETE /api/v1/productos/{id}` |
| Parámetro de ruta | `id: Long`, obligatorio |
| Query y body | Ninguno |
| Respuesta exitosa del controlador | `204 No Content` |
| Efecto | Baja lógica: cambia `estado` a `false`; no elimina físicamente el registro |
| Errores | `400` si el identificador no es numérico; `404` si no existe; `409` si ya está inactivo; `500` por error no controlado |
| Estado en PharmaMobile | Documentado, no implementado aún en la aplicación |

Springdoc no contiene anotaciones explícitas de respuestas y muestra `200` como
valor genérico para POST y DELETE. El código del controlador devuelve `201` y
`204`, respectivamente. Para cumplir la orientación de ejecutar cada endpoint,
el 4 de octubre de 2026 se ejecutaron las cinco solicitudes contra PharmaSoft
local usando un único producto de evidencia autorizado. Los resultados fueron:

| Solicitud | Resultado observado |
| --- | --- |
| `GET /productos?pagina=0&tamanio=20&ordenarPor=id&direccion=asc` | `200 OK` |
| `GET /productos/999999999` | `404 Not Found` |
| `POST /productos` | `201 Created`, producto de evidencia con ID `21` |
| `PUT /productos/21` | `200 OK`, precio actualizado a `11.25` y stock a `18` |
| `DELETE /productos/21` | `204 No Content`; comprobación posterior: `estado = false` |

La captura visual real de Swagger está disponible en
`../evidencias_sesion07/evidencia_00_swagger_productos.png`; muestra la URL de
solicitud, el código `200` y el JSON devuelto por PharmaSoft.

## 2. Diccionario real de DTO

Solo existen dos DTO de red en PharmaMobile relacionados con esta actividad:
`ProductoResponseDto` y `PaginaResponseDto<T>`.

### ProductoResponseDto

| Campo JSON | Tipo Kotlin exacto | Obligatorio al deserializar | Valor por defecto | Nulable | Correspondencia en el dominio |
| --- | --- | --- | --- | --- | --- |
| `id` | `Long` | Sí | Sin valor | No | `Producto.id` |
| `nombre` | `String` | Sí | Sin valor | No | `Producto.nombre` |
| `precio` | `Double` | Sí | Sin valor | No | `Producto.precio` |
| `stock` | `Int` | Sí | Sin valor | No | `Producto.stock` |
| `estado` | `Boolean` | No | `true` | No | `Producto.activo` |
| `categoriaId` | `Long?` | No | `null` | Sí | No se transfiere al dominio |
| `categoriaNombre` | `String?` | No | `null` | Sí | `Producto.categoria`; usa `"Sin categoría"` si es nulo |
| `fechaCreacion` | `String?` | No | `null` | Sí | No se transfiere al dominio |
| `fechaModificacion` | `String?` | No | `null` | Sí | No se transfiere al dominio |

Código real resumido:

```kotlin
@Serializable
data class ProductoResponseDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long? = null,
    val categoriaNombre: String? = null,
    val fechaCreacion: String? = null,
    val fechaModificacion: String? = null
)
```

### PaginaResponseDto de ProductoResponseDto

| Campo JSON | Tipo Kotlin exacto | Obligatorio al deserializar | Valor por defecto | Nulable | Correspondencia en el dominio |
| --- | --- | --- | --- | --- | --- |
| `contenido` | `List<ProductoResponseDto>` en este GET | Sí | Sin valor | No | Cada elemento se transforma en `Producto`; el repositorio devuelve `List<Producto>` |
| `pagina` | `Int` | Sí | Sin valor | No | Metadato no transferido al modelo `Producto` |
| `tamanio` | `Int` | Sí | Sin valor | No | Metadato no transferido al modelo `Producto` |
| `totalElementos` | `Long` | Sí | Sin valor | No | Metadato no transferido al modelo `Producto` |
| `totalPaginas` | `Int` | Sí | Sin valor | No | Metadato no transferido al modelo `Producto` |
| `ultima` | `Boolean` | Sí | Sin valor | No | Metadato no transferido al modelo `Producto` |

Código real:

```kotlin
@Serializable
data class PaginaResponseDto<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean
)
```

### JSON real de PharmaSoft para contrastar con los DTO

```json
{
  "contenido": [
    {
      "id": 1,
      "nombre": "Paracetamol 500 mg",
      "precio": 4.5,
      "stock": 120,
      "estado": true,
      "categoriaId": 1,
      "categoriaNombre": "Analgésicos",
      "fechaCreacion": "2026-10-04T00:50:56.249957",
      "fechaModificacion": null
    }
  ],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 3,
  "totalPaginas": 1,
  "ultima": true
}
```

Para la evidencia final, abrir Swagger, ejecutar el GET de productos y capturar
en una sola imagen la URL de la solicitud, el código `200` y el JSON que muestre
al menos un producto junto con los metadatos paginados.

### Mapper DTO a dominio

`ProductoResponseDto.toDomain()` utiliza `id`, `nombre`, `precio`, `stock`,
`estado` y `categoriaNombre`. No transfiere `categoriaId`, `fechaCreacion` ni
`fechaModificacion`. Además, establece `stockDisponible = true` y
`origen = OrigenProducto.REMOTO`; `descripcion` e `imagen` conservan los valores
vacíos predeterminados del dominio.

## 3. Bitácora de pruebas de conexión

Las pruebas combinan ejecución real contra PharmaSoft, evidencia visual Android
y pruebas automatizadas con `MockEngine`. iOS se considera no aplicable en este
equipo Windows, porque su ejecución requiere macOS y Xcode.

### Prueba 1 - Respuesta exitosa

- Identificador: `P1-GET-200`.
- Fecha y hora: disponible en la captura real anterior; repetir si se requiere
  una bitácora nueva.
- Plataforma: Android comprobado; iOS no aplicable en Windows.
- Pasos: iniciar Oracle y PharmaSoft; abrir PharmaMobile; entrar a Productos;
  pulsar Reintentar si fuera necesario.
- Esperado: Logcat muestra GET y `RESPONSE: 200`; la interfaz muestra productos.
- Observado: Android mostró Paracetamol 500 mg, Ibuprofeno 400 mg y Naproxeno
  550 mg. El endpoint también devolvió `200` en la verificación técnica.
- Capturas disponibles: `../evidencias_sesion07/evidencia_01_ktor_pharmasoft.png`
  y `../evidencias_sesion07/evidencia_02_android_lista_pharmasoft.png`.
- Conclusión: el GET paginado y el renderizado Android funcionaron con datos de
  PharmaSoft.

### Prueba 2 - Recurso inexistente

- Identificador: `P2-GET-404`.
- Fecha: 4 de octubre de 2026.
- Plataforma: backend real y prueba automatizada Android Host superada; iOS no
  aplicable en Windows.
- Pasos controlados para la UI: cambiar temporalmente en `ProductoApi.listar()`
  `client.get("productos")` por `client.get("productos/999999999")`, ejecutar la
  app y luego restaurar inmediatamente `client.get("productos")`.
- Esperado: `ClientRequestException`, código `404`, UI estable y mensaje
  `No se encontró el producto solicitado.`.
- Observado: el backend real devolvió `404`; la prueba automatizada confirmó el
  mensaje controlado y el tipo `ClientRequestException`.
- Capturas: `evidencia_07_swagger_404.png` y
  `evidencia_05_pruebas_conexion.png`.
- Conclusión: escenario verificado correctamente.

### Prueba 3 - Sin conexión

- Identificador: `P3-SIN-RED`.
- Fecha y hora: disponible en la captura real anterior; repetir si se requiere
  una bitácora nueva.
- Plataforma: Android comprobado; iOS no aplicable en Windows.
- Pasos: cargar la app una vez; activar modo avión o detener PharmaSoft; pulsar
  Reintentar.
- Esperado: excepción de entrada/salida capturada, UI estable y mensaje
  `No se pudo conectar con el servicio. Verifica tu conexión a Internet.`.
- Observado: Android mostró un estado de error recuperable y el botón
  Reintentar; la aplicación no se cerró.
- Captura disponible: `../evidencias_sesion07/evidencia_04_error_conexion_pharmasoft.png`.
- Conclusión: el fallo de conectividad se presenta al usuario de forma
  controlada.

### Prueba 4 - Timeout

- Identificador: `P4-TIMEOUT`.
- Fecha: 4 de octubre de 2026.
- Plataforma: prueba automatizada Android Host superada; iOS no aplicable en
  Windows.
- Cambio temporal: en `AppModule.kt`, sustituir temporalmente
  `crearHttpClient(get(), get(named("urlBase")))` por
  `crearHttpClient(get(), get(named("urlBase")), tiempoEsperaSolicitudMillis = 1)`.
- Restauración obligatoria: volver a
  `crearHttpClient(get(), get(named("urlBase")))`; el valor final predeterminado
  permanece en `15_000 ms`.
- Esperado: `HttpRequestTimeoutException` capturada, UI estable y mensaje
  `La conexión tardó demasiado. Verifica tu Internet e inténtalo nuevamente.`.
- Observado: la prueba automatizada con `MockEngine` superó este escenario y
  confirmó `HttpRequestTimeoutException` junto con el mensaje controlado.
- Captura: `evidencia_05_pruebas_conexion.png`.
- Conclusión: escenario verificado correctamente.

### Prueba 5 - Campo desconocido en JSON

- Identificador: `P5-JSON-DESCONOCIDO`.
- Fecha: 4 de octubre de 2026.
- Plataforma: pruebas automatizadas Android Host superadas; iOS no aplicable en
  Windows.
- Configuración final: `ignorarCamposDesconocidos = true`.
- Verificación segura: ejecutar `ConexionRestTest`; la respuesta simulada agrega
  `campoDesconocido` sin modificar PharmaSoft.
- Verificación estricta: la misma prueba crea un cliente temporal con
  `ignorarCamposDesconocidos = false` y confirma el error controlado
  `No se pudo interpretar la respuesta del servidor.`.
- Restauración: ninguna; el cliente usado por la aplicación conserva `true`.
- Captura: `evidencia_05_pruebas_conexion.png` muestra las cuatro pruebas
  aprobadas y `BUILD SUCCESSFUL`.
- Conclusión: el cliente tolera campos nuevos y el modo estricto detecta el
  contrato incompatible.

Comando para ejecutar las cuatro pruebas automatizadas de conectividad:

```powershell
.\gradlew.bat :shared:testAndroidHostTest --tests "pe.edu.upeu.pharmamobilee.data.remote.ConexionRestTest"
```

## 4. Evidencias y repositorio

### Capturas exactas y ordenadas

1. `CAPTURA 01 - Swagger GET 200 y JSON`: abrir
   `http://localhost:8080/swagger-ui.html`, ejecutar GET `/api/v1/productos` y
   mostrar Request URL, código 200 y el JSON con `contenido` y paginación.
2. `CAPTURA 02 - Ktor completo`: en Logcat filtrar `HttpClient`, recargar la
   pantalla y mostrar la línea REQUEST, URL, cabeceras/cuerpo y RESPONSE 200. El
   nivel final `LogLevel.ALL` permite obtenerla.
3. `CAPTURA 03 - Android éxito`: mostrar el emulador completo con los tres
   productos reales visibles y sin superponer otras ventanas.
4. `CAPTURA 04 - Android 404`: aplicar solo el cambio temporal de P2, ejecutar,
   mostrar el mensaje `No se encontró el producto solicitado.` y mantener visible
   Logcat con `404`; restaurar la ruta al terminar.
5. `CAPTURA 05 - Android sin conexión`: activar modo avión o detener el backend,
   pulsar Reintentar y mostrar el mensaje de conexión y el botón de recuperación.
6. `CAPTURA 06 - Android timeout`: aplicar el timeout temporal de P4, ejecutar y
   mostrar el mensaje de demora junto con el Logcat; restaurar `15_000 ms`.
7. `CAPTURA 07 - Campo desconocido tolerado`: ejecutar en Android Studio
   `campoDesconocidoSeIgnoraConLaConfiguracionFinal` y mostrar la prueba aprobada.
8. `CAPTURA 08 - Campo desconocido estricto`: ejecutar
   `campoDesconocidoFallaAlDesactivarTemporalmenteLaOpcion` y mostrar la prueba
   aprobada que verifica el error de deserialización controlado.
9. `CAPTURA 09 - iOS`: no aplicable en este equipo Windows; para obtenerla se
   requiere una Mac con Xcode y el simulador iOS.
11. `CAPTURA 11 - Git`: después de la autorización futura para commit y push,
    mostrar la rama, el hash del commit y el enlace accesible del repositorio.

### Estado de repositorio

- Rama actual: `feature/ktor-client-Gutierrez`.
- Repositorio: `https://github.com/LucianaGutierrez-049/PharmaMobile`.
- Commit técnico: `55c54c2` (`test: validar errores y compatibilidad JSON de Ktor`).
- Commit de documentación y evidencias: `b900072`
  (`docs: agregar evidencias de la actividad autonoma 07`).
- El README raíz contiene la sección `Conectividad REST` actualizada.
- Ejecución REST real del 4 de octubre de 2026: se validaron GET, GET 404, POST,
  PUT y DELETE. El producto de evidencia ID `21` quedó inactivo tras la baja
  lógica y no alteró los productos base.

### Checklist de la actividad

| N.º | Criterio observable | Estado |
| --- | --- | --- |
| 1 | PDF `S07_ActividadAutonoma_Gutierrez.pdf` | CUMPLE; generado con la evidencia verificable disponible |
| 2 | Portada con nombre, sesión, fecha y repositorio | CUMPLE |
| 3 | Cinco endpoints con todos sus datos | CUMPLE; GET, GET 404, POST, PUT y DELETE fueron ejecutados contra PharmaSoft |
| 4 | Todos los campos de cada DTO del proyecto | CUMPLE |
| 5 | JSON real junto al código de DTO | CUMPLE en esta preparación |
| 6 | Cinco escenarios documentados | CUMPLE mediante ejecución real, UI Android y pruebas automatizadas |
| 7 | Mensaje visible en cada error | CUMPLE en código; evidencia manual incompleta |
| 8 | Capturas Android e iOS | CUMPLE Android; iOS no aplicable en Windows |
| 9 | README con Conectividad REST | CUMPLE |
| 10 | Commits accesibles en la rama feature | CUMPLE: `55c54c2` y `b900072` |

## Fuentes de verificación

- Guía Práctica N.º 07 ya aplicada en PharmaMobile.
- Actividad Autónoma N.º 07, cinco páginas.
- Código actual de PharmaMobile.
- `ProductoController`, DTO, servicio, paginación y manejador global de errores
  del backend PharmaSoft.
- Swagger/OpenAPI local y ejecuciones reales GET 200 y GET 404.
