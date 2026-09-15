# PharmaMobile

Aplicación académica multiplataforma de gestión farmacéutica construida con Kotlin
Multiplatform y Compose Multiplatform.

## Alcance actual

- Modelos de dominio para productos, clientes y pedidos.
- Ejercicios de corrutinas y Flow conservados como evidencia de la Sesión 2.
- Registro y edición de productos con validaciones de negocio.
- Inventario con Tabs de activos, inactivos y bajo stock (`stock <= 5`).
- Navegación adaptable para teléfono, tablet y pantallas amplias.
- Temas Material 3 claro y oscuro.
- Clean Architecture, MVVM y Koin compartidos entre Android e iOS.

## Verificación

```powershell
.\gradlew.bat :shared:testAndroidHostTest
.\gradlew.bat :androidApp:assembleDebug
```

La ejecución de pruebas iOS requiere macOS y Xcode.
