# Actividad Autónoma 13

Autora: Luciana Cristina Gutierrez Quispe. Código 202320154. Grupo 2.
Fecha del recorrido: 6 de noviembre de 2026.

La actividad pide documentar diez pasos del flujo, las plataformas, los tres
estados de cada permiso y una ficha de integración con decisiones y fechas.
El permiso de notificaciones, la sincronización local, el manejo de errores y
la integración de Crashlytics ya estaban implementados en la rama.

## Método

El perfil Android `Evidencias13` (usuario 10) separa los datos del recorrido de
los del propietario del emulador. No se borra la base del perfil principal.
`Evidencias13Instrumentation` pertenece al APK de pruebas, no al producto de
producción. Registra únicamente códigos HTTP, roles, permisos, estados, conteos
e identificadores. Las credenciales se consumen en memoria desde la configuración
local previa y el archivo de comando del dispositivo se elimina antes de operar.

`control.py` permite consultar los controles actuales mediante accesibilidad,
rellenar el formulario real y capturar la pantalla del emulador. Los registros
JSON son evidencia técnica complementaria; no sustituyen a las capturas.

La prueba de expiración utiliza una fixture firmada con la configuración del
backend local y una fecha pasada, verificando primero la firma del token vigente.
No cambia el reloj, la configuración de producción ni los usuarios del backend.
No se exportan tokens o claves de firma.

## Validación

El 6 de noviembre se volvió a ejecutar `:shared:testAndroidHostTest --rerun`:
82 pruebas, 21 suites, cero fallos, errores u omisiones. La compilación del APK
de pruebas fue correcta. Los resultados del recorrido se conservan en `capturas`.
iOS permanece sin ejecución: Gradle indica que el simulador requiere macOS.

El informe final se construye con las evidencias aprobadas y se revisa mediante
renderizado antes de entregar Word y PDF. El PDF de la actividad pide entre ocho
y catorce páginas en tamaño carta, con portada, índice y enlaces finales a la rama
y al commit evaluado.
