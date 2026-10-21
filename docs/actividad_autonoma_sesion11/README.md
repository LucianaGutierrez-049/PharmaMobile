# Actividad Autónoma N.º 11 - preparación de evidencias

## Verificación automatizada

Ejecutar la suite completa desde Android Studio o PowerShell:

```powershell
.\gradlew.bat :shared:testAndroidHostTest
```

El reporte HTML queda en
`shared/build/reports/tests/testAndroidHostTest/index.html`. La clase
`ProductoRepositorioOfflineTest` contiene las seis pruebas obligatorias con
SQLite en memoria y `MockEngine` de Ktor. `AntiguedadSincronizacionTest`
verifica el caso sin sincronización y una antigüedad superior a un día.

## Bitácora Android que debe capturarse

Cada evidencia debe registrar fecha y hora, plataforma Android, pasos, estado
visible, estado local y una conclusión propia.

1. Sincronizar con red, cerrar, activar modo avión y volver a abrir: la lista
   local permanece visible y aparece el aviso de falta de conexión.
2. En modo avión registrar un producto: aparece inmediatamente con id negativo
   y la etiqueta `Pendiente de sincronizar`.
3. Desactivar modo avión y sincronizar: desaparece la etiqueta, se asigna un id
   positivo y el mismo producto debe verificarse en Swagger.
4. Borrar los datos de la aplicación, activar modo avión y abrir: se muestra el
   estado vacío informativo y `Nunca sincronizado`.
5. Con token caducado y modo avión abrir: se conservan los datos locales; la
   sesión se resuelve al reconectar porque expulsar al usuario sin red impediría
   consultar información ya almacenada.

No se incluyen capturas de iOS porque no se dispone de macOS/Xcode.

## Guion de sustentación de ocho minutos

- 0:00-1:00 - Autenticación JWT: login correcto y credenciales incorrectas.
- 1:00-3:00 - REST y CRUD: listar, registrar y actualizar con PharmaSoft activo.
- 3:00-5:00 - Estados y errores: provocar 403 con usuario sin permiso y mostrar
  validación bajo el campo correspondiente.
- 5:00-7:00 - Offline-first: activar modo avión, mostrar lista local, registrar
  pendiente, reconectar y sincronizar.
- 7:00-8:00 - Evidencia de plataforma: ejecutar Android y explicar que iOS está
  implementado, pero no se ejecutó por falta de macOS/Xcode.

Preparación previa: backend PharmaSoft en el puerto 8080, base con productos,
Swagger abierto, emulador Android iniciado y sesión cerrada. Usar una cuenta
administradora para CRUD y una cuenta de solo lectura para demostrar el 403.
