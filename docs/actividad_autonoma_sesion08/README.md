# Actividad Autónoma N.º 08 — preparación del informe

Proyecto: **PharmaMobile**, integrado con **PharmaSoft**  
Estudiante: **Luciana Gutierrez**  
Rama: `feature/crud-productos-Gutierrez`  
Fecha de ejecución: **5 de octubre de 2026**  
Documento final previsto: `S08_ActividadAutonoma_Gutierrez.pdf` (todavía no generado)

La ejecución móvil se realizó en Android Emulator. iOS no fue ejecutado porque
el equipo disponible usa Windows y no cuenta con macOS/Xcode.

## 1. Matriz de operaciones

El producto de evidencia fue creado con ID `43`. PharmaSoft usa borrado lógico,
por lo que después del DELETE el registro sigue apareciendo en el listado con
`estado=false`. Las respuestas literales conservadas durante la ejecución están
en [registro_api_real.txt](registro_api_real.txt).

| N.º | Operación | Petición exacta y body | Código real | Resultado observado |
| --- | --- | --- | --- | --- |
| 1 | Listar | `GET http://localhost:8080/api/v1/productos?pagina=0&tamanio=20` | 200 | Respuesta paginada; `totalElementos=7` y el ID 43 apareció en `contenido`. En PharmaMobile, la lista se representa en Activos/Inactivos/Bajo stock. |
| 2 | Obtener | `GET http://localhost:8080/api/v1/productos/43` | 200 | Se deserializó el producto `Autonoma08Gutierrez20261005051104`, precio 12.50, stock 7, categoría Analgésicos. La app no posee una pantalla de detalle independiente; el objeto de producto se usa en el flujo de edición. |
| 3 | Crear | `POST http://localhost:8080/api/v1/productos` con `{"nombre":"Autonoma08Gutierrez20261005051104","precio":12.50,"stock":7,"estado":true,"categoriaId":1}` | 201 | PharmaSoft devolvió el ID 43. El flujo equivalente de la app limpia el formulario, recarga la lista y muestra confirmación. Evidencia visual equivalente: `../evidencias_sesion08/evidencia_02_creacion_exitosa.png`. |
| 4 | Actualizar | `PUT http://localhost:8080/api/v1/productos/43` con `{"nombre":"Autonoma08Gutierrez20261005051104Editado","precio":13.75,"stock":9,"estado":true,"categoriaId":1}` | 200 | Se devolvieron nombre, precio y stock actualizados. PharmaMobile recarga sin duplicar el registro. Evidencia visual equivalente: `../evidencias_sesion08/evidencia_03_actualizacion_exitosa.png`. |
| 5 | Eliminar | `DELETE http://localhost:8080/api/v1/productos/43` | 204 | Respuesta sin cuerpo; el registro quedó inactivo. PharmaMobile lo presenta en la pestaña Inactivos. Evidencia visual equivalente: `../evidencias_sesion08/evidencia_04_producto_en_inactivos.png`. |

## 2. Bitácora de los ocho escenarios

### E01 — Validación del nombre

- **Identificador:** E01
- **Nombre:** validación del servidor para nombre corto
- **Fecha y hora:** 2026-10-05 04:08:43 (hora del host, UTC-05:00)
- **Plataforma:** Android Emulator
- **Pasos exactos:** abrir Productos; escribir `AB`, precio `10.00` y stock `2`; pulsar Registrar.
- **Petición realizada:** `POST /api/v1/productos` con `{"nombre":"AB","precio":10.0,"stock":2,"estado":true,"categoriaId":1}`.
- **Código HTTP:** 400.
- **ErrorApi producido:** `ErrorApi.Validacion`, campo `nombre`.
- **Mensaje visto:** `El nombre debe tener entre 3 y 150 caracteres` debajo del campo Nombre.
- **Resultado observado:** el formulario permaneció visible y la aplicación siguió estable.
- **Captura:** [evidencia_01_validacion_nombre.png](evidencias/evidencia_01_validacion_nombre.png).
- **Conclusión propia:** el error estructurado de PharmaSoft llega al campo correcto sin convertir toda la pantalla en error.

### E02 — Precio inválido

- **Identificador:** E02
- **Nombre:** validación del servidor para precio menor al mínimo
- **Fecha y hora:** 2026-10-05 05:09:46 (hora del host, UTC-05:00)
- **Plataforma:** Android Emulator
- **Pasos exactos:** escribir `PrecioInvalido`, precio `0.001` y stock `2`; pulsar Registrar. Se usó `0.001` porque supera la validación local `> 0`, pero PharmaSoft exige un mínimo superior y así se comprueba el 400 real.
- **Petición realizada:** `POST /api/v1/productos` con `{"nombre":"PrecioInvalido","precio":0.001,"stock":2,"estado":true,"categoriaId":1}`.
- **Código HTTP:** 400.
- **ErrorApi producido:** `ErrorApi.Validacion`, campo `precio`.
- **Mensaje visto:** `El precio debe ser mayor que cero` debajo del campo Precio.
- **Resultado observado:** la UI no se cerró ni se congeló y conservó los datos para corregirlos.
- **Captura:** [evidencia_02_validacion_precio.png](evidencias/evidencia_02_validacion_precio.png).
- **Conclusión propia:** las reglas locales evitan cero o negativos; el valor 0.001 permitió verificar además la validación real del servidor y su mapeo por campo.

### E03 — Recurso inexistente

- **Identificador:** E03
- **Nombre:** actualización de ID inexistente
- **Fecha y hora:** 2026-10-05 05:12:10 (hora del host, UTC-05:00)
- **Plataforma:** integración directa con PharmaSoft y `commonTest`
- **Pasos exactos:** enviar un PUT válido al ID `999999`.
- **Petición realizada:** `PUT /api/v1/productos/999999` con `{"nombre":"ProductoNoExistente","precio":13.75,"stock":9,"estado":true,"categoriaId":1}`.
- **Código HTTP:** 404.
- **ErrorApi producido:** `ErrorApi.NoEncontrado`; el mapeo 404 se verifica también en `ConexionRestTest.respuesta404SeConvierteEnMensajeEntendible`.
- **Mensaje correspondiente:** `No se encontró el producto solicitado.`
- **Resultado observado:** PharmaSoft devolvió `Producto no encontrado con id: 999999`; la traducción móvil centralizada produjo `NoEncontrado` en pruebas.
- **Captura:** **PENDIENTE DE EJECUCIÓN MANUAL** si se exige evidencia visual de UI, porque la pantalla solo permite editar IDs obtenidos del listado y no ofrece entrada libre de ID.
- **Conclusión propia:** API y mapeo están verificados, pero provocar este caso desde la UI normal requeriría un producto que desaparezca en el servidor entre la carga y el guardado.

### E04 — Doble eliminación

- **Identificador:** E04
- **Nombre:** segundo DELETE sobre un producto inactivo
- **Fecha y hora:** 2026-10-05 05:16:03 (hora del host, UTC-05:00)
- **Plataforma:** Android Emulator
- **Pasos exactos:** eliminar el ID 43; abrir Inactivos; pulsar nuevamente eliminar y confirmar.
- **Petición realizada:** segundo `DELETE /api/v1/productos/43`.
- **Código HTTP:** 409.
- **ErrorApi producido:** `ErrorApi.Conflicto`.
- **Mensaje visto:** `El producto Autonoma08Gutierrez20261005051104Editado ya se encuentra inactivo`.
- **Resultado observado:** se mostró Snackbar y la lista continuó utilizable.
- **Captura:** [evidencia_04_segundo_delete_conflicto.png](evidencias/evidencia_04_segundo_delete_conflicto.png).
- **Conclusión propia:** la guía sugiere `NoEncontrado`, pero PharmaSoft implementa borrado lógico y responde 409; documentar 404 habría sido incorrecto.

### E05 — Regla de negocio / conflicto

- **Identificador:** E05
- **Nombre:** nombre de producto duplicado
- **Fecha y hora:** 2026-10-05 05:13:46 aproximadamente (petición Android registrada a las 10:13:46 en el reloj UTC del emulador)
- **Plataforma:** Android Emulator
- **Pasos exactos:** intentar registrar otro producto llamado `Paracetamol 500 mg`, precio `4.5`, stock `3`.
- **Petición realizada:** `POST /api/v1/productos` con esos datos y `categoriaId=1`.
- **Código HTTP:** 409.
- **ErrorApi producido:** `ErrorApi.Conflicto`.
- **Mensaje visto:** `Ya existe un producto con el nombre Paracetamol 500 mg`.
- **Resultado observado:** el mensaje real de PharmaSoft llegó al Snackbar y la app permaneció estable.
- **Captura:** [evidencia_05_conflicto_nombre_duplicado.png](evidencias/evidencia_05_conflicto_nombre_duplicado.png).
- **Conclusión propia:** el nombre único es una regla real del backend y permite probar 409 sin modificar PharmaSoft ni fabricar datos.

### E06 — Servidor caído

- **Identificador:** E06
- **Nombre:** PharmaSoft no disponible
- **Fecha y hora:** 2026-10-05 05:26 (hora del host, UTC-05:00)
- **Plataforma:** Android Emulator
- **Pasos exactos:** detener solo el proceso local de PharmaSoft; reiniciar PharmaMobile; abrir Productos; esperar el fallo; reiniciar PharmaSoft.
- **Petición realizada:** `GET http://10.0.2.2:8080/api/v1/productos?pagina=0&tamanio=20`.
- **Código HTTP:** no existe respuesta HTTP porque no había conexión con el servidor.
- **ErrorApi producido:** `ErrorApi.SinConexion`.
- **Mensaje visto:** `No se pudo conectar con el servicio. Verifica tu conexión a Internet.`
- **Resultado observado:** la aplicación no se cerró ni se congeló; mostró una tarjeta de error controlada. PharmaSoft fue reiniciado y volvió a escuchar en 8080.
- **Captura:** [evidencia_06_servidor_caido.png](evidencias/evidencia_06_servidor_caido.png).
- **Conclusión propia:** una excepción de E/S se traduce a un estado recuperable y no expone detalles técnicos.

### E07 — Tiempo agotado

- **Identificador:** E07
- **Nombre:** timeout temporal y reversible
- **Fecha y hora:** 2026-10-05, durante la ejecución de `:shared:testAndroidHostTest`
- **Plataforma:** `commonTest` JVM en Windows
- **Pasos exactos:** `ConexionRestTest.timeoutDeUnMilisegundoSeCapturaSinDejarEseValorEnProduccion` crea un cliente aislado con `requestTimeoutMillis=1`; `MockEngine` demora 100 ms.
- **Petición realizada:** GET simulado contra `http://localhost/api/v1/`.
- **Código HTTP:** no existe respuesta HTTP; el timeout ocurre antes de recibirla.
- **ErrorApi producido:** `ErrorApi.TiempoAgotado`.
- **Mensaje correspondiente:** `La conexión tardó demasiado. Verifica tu Internet e inténtalo nuevamente.`
- **Resultado observado:** la prueba pasó. La configuración productiva continúa en 15 000 ms.
- **Captura:** **PENDIENTE DE EJECUCIÓN MANUAL** de la ventana Run de Android Studio.
- **Conclusión propia:** el timeout se verifica sin dejar una configuración peligrosa en producción y sin depender de ralentizar PharmaSoft.

### E08 — Cancelación

- **Identificador:** E08
- **Nombre:** propagación de `CancellationException`
- **Fecha y hora:** 2026-10-05, durante la ejecución de `:shared:testAndroidHostTest`
- **Plataforma:** `commonTest` JVM en Windows
- **Pasos exactos:** `EjecutarLlamadaTest.cancellationExceptionSiempreSeRelanza` lanza una cancelación dentro de `ejecutarLlamada`.
- **Petición realizada:** no llega a completarse una petición HTTP.
- **Código HTTP:** no aplica.
- **ErrorApi producido:** ninguno; la excepción se relanza.
- **Mensaje visto:** ninguno, que es el comportamiento correcto.
- **Resultado observado:** `assertFailsWith<CancellationException>` pasó. El `catch (CancellationException)` está antes de los bloques genéricos y vuelve a lanzar la excepción.
- **Captura:** **PENDIENTE DE EJECUCIÓN MANUAL** de la prueba; abandonar una pantalla no debe producir Snackbar de error.
- **Conclusión propia:** la cancelación estructurada no se confunde con un fallo de red y evita mensajes falsos al terminar el alcance de la corutina.

## 3. Pruebas automatizadas commonTest

`ProductoViewModelFlujoTest` verifica estas transiciones reales:

1. `ProductoFase.Cargando` → `ProductoFase.ConProductos`, conservando la lista esperada.
2. `ProductoFase.Cargando` → `ProductoFase.SinProductos` para una respuesta vacía.
3. `ErrorApi.Validacion` distribuye `nombre`, `precio` y `stock` en sus errores de campo; la fase general no cambia a `Error`.
4. `ProductoOperacion.Inactiva` → `EnCurso(Eliminar)` → `Inactiva`; luego se recarga la lista y queda `SinProductos`.

Comando ejecutado:

```powershell
.\gradlew.bat :shared:testAndroidHostTest --console=plain
```

Resultado real: **32 pruebas, 32 aprobadas, 0 fallos, 0 errores y 0 omitidas**;
`BUILD SUCCESSFUL in 30s`. El detalle está en
[resultado_pruebas.txt](resultado_pruebas.txt).

Para la captura en Android Studio: abrir la ventana **Gradle**, ejecutar
`shared > Tasks > verification > testAndroidHostTest`, abrir la ventana **Run**
y capturar cuando se vean el árbol verde de pruebas y `BUILD SUCCESSFUL`. No
usar `androidTest`, porque estas pruebas pertenecen a `commonTest`.

## 4. Evidencias y repositorio

### Log Ktor fallido

La evidencia real está transcrita en [log_ktor_fallido.txt](log_ktor_fallido.txt).
Para obtener la captura requerida: abrir **Logcat** en Android Studio, seleccionar
`emulator-5554` y el proceso `pe.edu.upeu.pharmamobilee`, filtrar por
`HttpClient`, ejecutar el registro con nombre `AB` y capturar juntas las líneas
`REQUEST`, URL, body, `RESPONSE: 400` y `validationErrors`.

### Git

- Rama local: `feature/crud-productos-Gutierrez`.
- Remoto: `https://github.com/LucianaGutierrez-049/PharmaMobile.git`.
- Commit de pruebas de la actividad: `0cc1113 test: verifica transiciones del flujo de productos`.
- El commit de documentación se crea localmente después de revisar estos archivos.
- **No se realizó push** de la Actividad Autónoma 08.

Enlace que corresponderá a la rama cuando se autorice un push futuro:
`https://github.com/LucianaGutierrez-049/PharmaMobile/tree/feature/crud-productos-Gutierrez`.

### Capturas pendientes y procedimiento exacto

1. **Pruebas:** Gradle > shared > verification > `testAndroidHostTest` > Run; capturar árbol verde y `BUILD SUCCESSFUL`.
2. **Ktor 400:** Logcat + filtro `HttpClient`; registrar `AB / 10.00 / 2`; capturar REQUEST, URL, RESPONSE 400 y body.
3. **404 visual, solo si el docente exige UI:** cargar un producto en la app; eliminarlo físicamente desde una herramienta autorizada del backend o usar otro ID inexistente desde una pantalla de diagnóstico; después intentar guardarlo. La UI actual no permite inventar un ID, por lo que no se fabricó esta captura.
4. **Timeout visual, solo si se exige UI:** inyectar temporalmente un cliente Koin de depuración con timeout mínimo, abrir Productos y capturar el mensaje; revertirlo inmediatamente a 15 000 ms. La prueba automática ya demuestra el mapeo sin alterar producción.
5. **Cancelación manual:** iniciar una operación bajo una red lenta controlada, salir inmediatamente de Productos y comprobar en Logcat que no aparece Snackbar ni `ErrorApi`; la prueba automática demuestra el relanzamiento.

### Checklist

| N.º | Requisito | Estado |
| --- | --- | --- |
| 1 | Contenido para `S08_ActividadAutonoma_Gutierrez.pdf` | CUMPLE |
| 2 | Datos de portada: nombre, sesión, fecha y enlace de rama | CUMPLE |
| 3 | Matriz de cinco operaciones con código real | CUMPLE |
| 4 | Bitácora de ocho escenarios | CUMPLE |
| 5 | HTTP/ErrorApi/mensaje/captura en cada escenario | PENDIENTE DE EJECUCIÓN MANUAL para capturas visuales E03, E07 y E08 |
| 6 | Cancelación documenta comportamiento real | CUMPLE mediante código y prueba automática |
| 7 | Al menos tres pruebas en `commonTest` | CUMPLE (cuatro transiciones requeridas y pruebas adicionales) |
| 8 | Todas las pruebas pasan | CUMPLE (32/32) |
| 9 | Evidencia de ejecución de pruebas | PENDIENTE DE CAPTURA MANUAL en Android Studio; salida exacta guardada |
| 10 | README incluye `Manejo de errores` | CUMPLE |
| 11 | Capturas Android | CUMPLE |
| 12 | iOS solo si se ejecutó realmente | CUMPLE: declarado no ejecutado |
| 13 | Commits de la actividad identificados | CUMPLE localmente; sin push |

### Evaluación honesta con la rúbrica

- Matriz de operaciones: **4/4**.
- Bitácora: **5.5/7** mientras falten capturas visuales E03, E07 y E08.
- Pruebas `commonTest`: **5/5**.
- Evidencias y repositorio: **1.5/2** mientras falte la captura de la ventana Run.
- Redacción y análisis: **2/2**.
- Estimación actual: **18/20**. Con las capturas manuales pendientes puede quedar preparada para **20/20**, sujeto a la evaluación docente.
