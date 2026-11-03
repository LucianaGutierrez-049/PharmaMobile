# Recorrido y brechas de la sesión 13

Base de trabajo: la Unidad 2 en `feature/offline-first-Gutierrez`, descendiente de `develop` (39 commits de avance). La rama integradora conserva estos antecedentes para no perder persistencia, autenticación y sincronización existentes.

| Momento | Hallazgo | Decisión |
|---|---|---|
| Primera apertura | La sesión anterior puede no recuperarse. | Verificar recuperación y nuevo login con el backend actual. |
| Credenciales incorrectas | Los errores se traducen mediante tipos del dominio. | Completar evidencia de ejecución. |
| Base vacía | Existe estado vacío informativo. | Capturar el caso con datos de prueba. |
| Creación sin red | No se guardaban intentos ni el motivo de fallo. | Añadir migración y metadatos por fila. |
| Reconexión | Un POST repetido puede duplicar productos. | Bloquear creaciones ambiguas y confirmar reenvíos. |
| Validación | Un error detenía la cola. | Conservar causa y continuar otras filas. |
| Token vencido | El cambio de sesión descarta el formulario. | Diseñar borrador por usuario en sesión 14. |
| Cierre de sesión | La caché no está particionada por usuario. | Aislar datos sin eliminar silenciosamente pendientes. |

Las evidencias y resultados finales se añaden al informe. No se atribuyen capturas de sesiones anteriores a la implementación actual.
