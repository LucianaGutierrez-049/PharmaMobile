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
- Catálogo remoto consultado con Ktor Client mediante una petición GET.

## Consumo REST con Ktor

- URL base: `https://api.escuelajs.co/api/v1/`
- Método y endpoint: `GET products`
- Parámetro de consulta: `limit` (valor predeterminado: `10`)
- Respuesta esperada: arreglo JSON de productos.

`ProductoDto` representa el contrato de red y utiliza los campos `id`, `title`,
`price`, `description`, `images` y `category`. `CategoriaDto` utiliza `id` y
`name`. El mapper convierte esos DTO al modelo de dominio; ni el modelo ni el
repositorio de dominio dependen de Ktor o de `kotlinx.serialization`.

El inventario creado en la Unidad 1 se mantiene para registro y edición local.
Los elementos obtenidos de la API se identifican como catálogo remoto, no
inventan un valor de stock y son de solo lectura mientras la aplicación todavía
no implemente POST o PUT.

Para verificar la conexión, abre Productos con Internet disponible. El log de
Ktor debe mostrar una petición a `products?limit=10` y una respuesta `200 OK`.
Sin conexión, la pantalla debe mostrar un mensaje de error y permitir reintentar.

## Verificación

```powershell
.\gradlew.bat :shared:testAndroidHostTest
.\gradlew.bat :androidApp:assembleDebug
```

La ejecución de pruebas iOS requiere macOS y Xcode.
