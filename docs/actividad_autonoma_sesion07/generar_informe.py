from pathlib import Path
from xml.sax.saxutils import escape

from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_JUSTIFY, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle, getSampleStyleSheet
from reportlab.lib.units import cm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    Image,
    KeepTogether,
    PageBreak,
    Paragraph,
    SimpleDocTemplate,
    Spacer,
    Table,
    TableStyle,
)


ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "docs" / "S07_ActividadAutonoma_Gutierrez.pdf"
EVIDENCIAS = ROOT / "docs" / "evidencias_sesion07"
ACTIVIDAD = ROOT / "docs" / "actividad_autonoma_sesion07"

FONT_DIR = Path("C:/Windows/Fonts")
pdfmetrics.registerFont(TTFont("Arial", str(FONT_DIR / "arial.ttf")))
pdfmetrics.registerFont(TTFont("Arial-Bold", str(FONT_DIR / "arialbd.ttf")))
pdfmetrics.registerFont(TTFont("Consolas", str(FONT_DIR / "consola.ttf")))

GREEN = colors.HexColor("#087A67")
DARK = colors.HexColor("#17212B")
MINT = colors.HexColor("#DDF7F0")
LILAC = colors.HexColor("#F2ECF7")
BLUE = colors.HexColor("#DCE8FF")
RED = colors.HexColor("#B42318")
GRAY = colors.HexColor("#5D6670")
LIGHT = colors.HexColor("#F7F9FA")


def p(text, style="Body"):
    return Paragraph(text, STYLES[style])


def cell(text, style="TableBody"):
    return Paragraph(escape(str(text)), STYLES[style])


def table(rows, widths, header=True, font_size=8):
    processed = []
    for row_index, row in enumerate(rows):
        style = "TableHead" if header and row_index == 0 else "TableBody"
        processed.append([cell(value, style) for value in row])
    result = Table(processed, colWidths=widths, repeatRows=1 if header else 0, hAlign="LEFT")
    rules = [
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#B7C0C7")),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
        ("FONTNAME", (0, 0), (-1, -1), "Arial"),
        ("FONTSIZE", (0, 0), (-1, -1), font_size),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, LIGHT]),
    ]
    if header:
        rules.extend([
            ("BACKGROUND", (0, 0), (-1, 0), GREEN),
            ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
            ("FONTNAME", (0, 0), (-1, 0), "Arial-Bold"),
        ])
    result.setStyle(TableStyle(rules))
    return result


def evidence_image(path, max_width=16.6 * cm, max_height=19.7 * cm):
    img = Image(str(path))
    ratio = min(max_width / img.imageWidth, max_height / img.imageHeight)
    img.drawWidth = img.imageWidth * ratio
    img.drawHeight = img.imageHeight * ratio
    img.hAlign = "CENTER"
    return img


def callout(title, body, tone="info"):
    bg = {"info": BLUE, "ok": MINT, "warn": colors.HexColor("#FFF1D6")}[tone]
    return Table(
        [[p(title, "CalloutTitle"), p(body, "CalloutBody")]],
        colWidths=[4.0 * cm, 12.6 * cm],
        style=TableStyle([
            ("BACKGROUND", (0, 0), (-1, -1), bg),
            ("BOX", (0, 0), (-1, -1), 0.8, GREEN),
            ("VALIGN", (0, 0), (-1, -1), "TOP"),
            ("LEFTPADDING", (0, 0), (-1, -1), 8),
            ("RIGHTPADDING", (0, 0), (-1, -1), 8),
            ("TOPPADDING", (0, 0), (-1, -1), 8),
            ("BOTTOMPADDING", (0, 0), (-1, -1), 8),
        ]),
    )


def section(title, subtitle=None):
    items = [Spacer(1, 0.15 * cm), p(title, "H1")]
    if subtitle:
        items.append(p(subtitle, "Lead"))
    items.append(Spacer(1, 0.15 * cm))
    return items


def endpoint_block(method, route, purpose, request, success, errors, mobile_state):
    return KeepTogether([
        p(f"{method} {route}", "H2"),
        table([
            ["Aspecto", "Detalle verificado"],
            ["Finalidad", purpose],
            ["Entrada", request],
            ["Éxito", success],
            ["Errores", errors],
            ["PharmaMobile", mobile_state],
        ], [3.3 * cm, 13.3 * cm]),
        Spacer(1, 0.22 * cm),
    ])


def footer(canvas, doc):
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor("#D4DBDF"))
    canvas.line(2 * cm, 1.4 * cm, A4[0] - 2 * cm, 1.4 * cm)
    canvas.setFont("Arial", 8)
    canvas.setFillColor(GRAY)
    canvas.drawString(2 * cm, 1.0 * cm, "Actividad Autónoma N.º 07 · PharmaMobile")
    canvas.drawRightString(A4[0] - 2 * cm, 1.0 * cm, f"Página {doc.page}")
    canvas.restoreState()


sample = getSampleStyleSheet()
STYLES = {
    "Title": ParagraphStyle("Title", fontName="Arial-Bold", fontSize=28, leading=33, textColor=DARK, alignment=TA_CENTER),
    "Subtitle": ParagraphStyle("Subtitle", fontName="Arial", fontSize=15, leading=20, textColor=GREEN, alignment=TA_CENTER),
    "Cover": ParagraphStyle("Cover", fontName="Arial", fontSize=12, leading=18, textColor=DARK, alignment=TA_CENTER),
    "H1": ParagraphStyle("H1", fontName="Arial-Bold", fontSize=18, leading=22, textColor=GREEN, spaceAfter=7),
    "H2": ParagraphStyle("H2", fontName="Arial-Bold", fontSize=12.5, leading=16, textColor=DARK, spaceBefore=8, spaceAfter=5),
    "H3": ParagraphStyle("H3", fontName="Arial-Bold", fontSize=10.5, leading=14, textColor=GREEN, spaceBefore=5, spaceAfter=3),
    "Body": ParagraphStyle("Body", fontName="Arial", fontSize=9.5, leading=14, textColor=DARK, alignment=TA_JUSTIFY, spaceAfter=6),
    "Lead": ParagraphStyle("Lead", fontName="Arial", fontSize=10.5, leading=15, textColor=GRAY, spaceAfter=8),
    "Small": ParagraphStyle("Small", fontName="Arial", fontSize=8, leading=11, textColor=GRAY),
    "Caption": ParagraphStyle("Caption", fontName="Arial", fontSize=8, leading=11, textColor=GRAY, alignment=TA_CENTER, spaceBefore=5, spaceAfter=8),
    "TableHead": ParagraphStyle("TableHead", fontName="Arial-Bold", fontSize=8, leading=10, textColor=colors.white),
    "TableBody": ParagraphStyle("TableBody", fontName="Arial", fontSize=8, leading=10.5, textColor=DARK),
    "CalloutTitle": ParagraphStyle("CalloutTitle", fontName="Arial-Bold", fontSize=9, leading=12, textColor=GREEN),
    "CalloutBody": ParagraphStyle("CalloutBody", fontName="Arial", fontSize=8.7, leading=12, textColor=DARK),
    "Code": ParagraphStyle("Code", fontName="Consolas", fontSize=7.6, leading=10.5, textColor=DARK, backColor=LIGHT, borderColor=colors.HexColor("#CCD4D9"), borderWidth=0.5, borderPadding=7, spaceAfter=7),
}


doc = SimpleDocTemplate(
    str(OUT),
    pagesize=A4,
    rightMargin=2 * cm,
    leftMargin=2 * cm,
    topMargin=1.8 * cm,
    bottomMargin=1.8 * cm,
    title="Actividad Autónoma N.º 07 - PharmaMobile",
    author="Luciana Gutierrez",
    subject="Endpoints REST, DTO y pruebas de conectividad con Ktor Client",
)

story = []

# Portada
story.extend([
    Spacer(1, 2.1 * cm),
    p("UNIVERSIDAD PERUANA UNIÓN", "Subtitle"),
    Spacer(1, 0.7 * cm),
    p("ACTIVIDAD AUTÓNOMA N.º 07", "Title"),
    Spacer(1, 0.3 * cm),
    p("Endpoints, DTO y pruebas de conectividad", "Subtitle"),
    Spacer(1, 1.2 * cm),
    Table([
        [cell("Proyecto", "TableHead"), cell("PharmaMobile integrado con PharmaSoft", "TableBody")],
        [cell("Estudiante", "TableHead"), cell("Luciana Gutierrez", "TableBody")],
        [cell("Asignatura", "TableHead"), cell("Desarrollo de Aplicaciones Móviles", "TableBody")],
        [cell("Ciclo y semestre", "TableHead"), cell("VI · 2026-2", "TableBody")],
        [cell("Fecha", "TableHead"), cell("29 de septiembre de 2026", "TableBody")],
        [cell("Rama", "TableHead"), cell("feature/ktor-client-Gutierrez", "TableBody")],
        [cell("Repositorio", "TableHead"), cell("github.com/LucianaGutierrez-049/PharmaMobile", "TableBody")],
    ], colWidths=[4.5 * cm, 11.5 * cm], style=TableStyle([
        ("BACKGROUND", (0, 0), (0, -1), GREEN),
        ("TEXTCOLOR", (0, 0), (0, -1), colors.white),
        ("GRID", (0, 0), (-1, -1), 0.6, colors.HexColor("#AFC0C6")),
        ("VALIGN", (0, 0), (-1, -1), "MIDDLE"),
        ("LEFTPADDING", (0, 0), (-1, -1), 9),
        ("RIGHTPADDING", (0, 0), (-1, -1), 9),
        ("TOPPADDING", (0, 0), (-1, -1), 9),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 9),
    ])),
    Spacer(1, 1.2 * cm),
    callout("Alcance de la entrega", "El informe documenta los cinco endpoints de Producto, los DTO reales de PharmaMobile, el mapper, cinco escenarios de prueba y las evidencias verificables obtenidas en Windows/Android. La ejecución en iOS se declara no realizada porque requiere macOS y Xcode.", "info"),
    Spacer(1, 0.8 * cm),
    p("Documento elaborado a partir del código del proyecto, del backend PharmaSoft, de ejecuciones HTTP reales, de pruebas automatizadas y de capturas tomadas durante la verificación.", "Small"),
    PageBreak(),
])

# Resumen
story.extend(section("1. Resumen de verificación", "Resultado consolidado del trabajo técnico y documental."))
story.append(table([
    ["Elemento", "Resultado"],
    ["Backend", "PharmaSoft local operativo en http://localhost:8080"],
    ["Android Emulator", "Consume http://10.0.2.2:8080/api/v1/ mediante Ktor Client"],
    ["GET listado", "200 OK; datos deserializados, mapeados y mostrados en Compose"],
    ["CRUD REST", "GET, GET 404, POST, PUT y DELETE ejecutados contra PharmaSoft"],
    ["Pruebas automáticas", "22 pruebas Android Host aprobadas; 4 corresponden a conectividad y manejo de errores"],
    ["Compilación", ":androidApp:assembleDebug completó correctamente"],
    ["iOS", "No ejecutado: el equipo Windows no dispone de macOS/Xcode"],
], [4.2 * cm, 12.4 * cm]))
story.append(Spacer(1, 0.3 * cm))
story.append(callout("Conclusión", "La integración Ktor GET funciona de extremo a extremo en Android. Los escenarios 404, timeout y JSON desconocido están cubiertos por pruebas automatizadas; la desconexión también cuenta con evidencia visual. Las capturas iOS quedan fuera de esta ejecución por limitación de plataforma.", "ok"))

story.extend(section("2. Arquitectura implementada"))
story.append(table([
    ["Capa", "Componente", "Responsabilidad"],
    ["Red", "HttpClientFactory", "ContentNegotiation JSON, Logging ALL, timeout de 15 000 ms y configuración ignoreUnknownKeys."],
    ["Datos", "ProductoApi", "Ejecuta GET productos con pagina y tamanio."],
    ["DTO", "ProductoResponseDto / PaginaResponseDto", "Representan el contrato JSON de PharmaSoft."],
    ["Mapper", "ProductoResponseDto.toDomain()", "Convierte el DTO al modelo Producto y marca el origen remoto."],
    ["Repositorio", "ProductoRepositoryImpl", "Expone productos de dominio y desacopla la UI de la red."],
    ["DI", "Koin / AppModule", "Proporciona URL base, motor, HttpClient, API, repositorio y ViewModel."],
    ["Presentación", "ProductoViewModel / UiState / Compose", "Gestiona carga, éxito, error, reintento y renderizado de la lista."],
], [2.1 * cm, 4.6 * cm, 9.9 * cm]))
story.append(PageBreak())

# Endpoints
story.extend(section("3. Catálogo de endpoints", "Contrato real comprobado en ProductoController y mediante solicitudes al backend."))
story.append(callout("Versionado", "La ruta contractual es /api/v1/productos. Aunque la metadata general de Springdoc puede mostrar v0, la aplicación consume correctamente la versión v1 declarada por el controlador.", "info"))
story.append(Spacer(1, 0.15 * cm))
story.extend([
    endpoint_block("GET", "/api/v1/productos", "Listar productos de forma paginada.", "Query: pagina:Int=0, tamanio:Int=20, ordenarPor:String=id, direccion:String=asc. Orden válido: id, nombre, precio o stock.", "200 OK con PaginaResponseDTO<ProductoResponseDTO>.", "400 por tipos inválidos; 409 por paginación u orden no permitido; 500 no controlado.", "Implementado y consumido por la aplicación."),
    endpoint_block("GET", "/api/v1/productos/{id}", "Consultar un producto.", "Path id:Long obligatorio; sin body ni query.", "200 OK con ProductoResponseDTO.", "400 si id no es numérico; 404 si no existe; 500 no controlado.", "Documentado; no forma parte del flujo de listado actual."),
    endpoint_block("POST", "/api/v1/productos", "Crear un producto.", "Body ProductoRequestDTO: nombre, precio, stock, estado y categoriaId.", "201 Created con ProductoResponseDTO.", "400 por validación/JSON; 404 categoría; 409 nombre duplicado o integridad; 500.", "Endpoint verificado; creación aún no implementada en la UI móvil."),
    endpoint_block("PUT", "/api/v1/productos/{id}", "Actualizar un producto completo.", "Path id:Long y body ProductoRequestDTO completo.", "200 OK con ProductoResponseDTO actualizado.", "400 por validación; 404 producto/categoría; 409 duplicidad o integridad; 500.", "Endpoint verificado; edición aún no implementada en la UI móvil."),
    endpoint_block("DELETE", "/api/v1/productos/{id}", "Dar de baja lógicamente un producto.", "Path id:Long; sin body.", "204 No Content; estado pasa a false.", "400 id inválido; 404 inexistente; 409 ya inactivo; 500.", "Endpoint verificado; baja aún no implementada en la UI móvil."),
])

story.append(p("Ejecución real del CRUD", "H2"))
story.append(table([
    ["Solicitud", "Resultado observado el 04/10/2026"],
    ["GET /productos?pagina=0&tamanio=20&ordenarPor=id&direccion=asc", "200 OK"],
    ["GET /productos/999999999", "404 Not Found"],
    ["POST /productos", "201 Created · producto de evidencia ID 21"],
    ["PUT /productos/21", "200 OK · precio 11.25 y stock 18"],
    ["DELETE /productos/21", "204 No Content · comprobación posterior estado=false"],
], [10.0 * cm, 6.6 * cm]))
story.append(PageBreak())

# DTO
story.extend(section("4. Diccionario de DTO y mapper", "Campos, nulabilidad, valores predeterminados y correspondencia con el dominio."))
story.append(p("ProductoResponseDto", "H2"))
story.append(table([
    ["Campo JSON", "Tipo Kotlin", "Obligatorio", "Default", "Nulable", "Destino"],
    ["id", "Long", "Sí", "—", "No", "Producto.id"],
    ["nombre", "String", "Sí", "—", "No", "Producto.nombre"],
    ["precio", "Double", "Sí", "—", "No", "Producto.precio"],
    ["stock", "Int", "Sí", "—", "No", "Producto.stock"],
    ["estado", "Boolean", "No", "true", "No", "Producto.activo"],
    ["categoriaId", "Long?", "No", "null", "Sí", "No transferido"],
    ["categoriaNombre", "String?", "No", "null", "Sí", "Producto.categoria"],
    ["fechaCreacion", "String?", "No", "null", "Sí", "No transferido"],
    ["fechaModificacion", "String?", "No", "null", "Sí", "No transferido"],
], [2.7 * cm, 2.3 * cm, 2.1 * cm, 1.7 * cm, 1.7 * cm, 6.1 * cm]))
story.append(p("PaginaResponseDto&lt;ProductoResponseDto&gt;", "H2"))
story.append(table([
    ["Campo JSON", "Tipo Kotlin", "Obligatorio", "Nulable", "Uso"],
    ["contenido", "List<ProductoResponseDto>", "Sí", "No", "Cada elemento se transforma en Producto"],
    ["pagina", "Int", "Sí", "No", "Metadato de paginación"],
    ["tamanio", "Int", "Sí", "No", "Metadato de paginación"],
    ["totalElementos", "Long", "Sí", "No", "Metadato de paginación"],
    ["totalPaginas", "Int", "Sí", "No", "Metadato de paginación"],
    ["ultima", "Boolean", "Sí", "No", "Indica última página"],
], [3.0 * cm, 4.4 * cm, 2.2 * cm, 1.8 * cm, 5.2 * cm]))
story.append(p("Reglas del mapper", "H2"))
story.append(p("ProductoResponseDto.toDomain() transfiere id, nombre, precio, stock, estado y categoriaNombre. Si categoriaNombre es nulo usa «Sin categoría». También fija stockDisponible=true y origen=REMOTO. categoriaId, fechaCreacion y fechaModificacion no forman parte del modelo de dominio actual."))
story.append(p("Configuración JSON final", "H2"))
story.append(p("JSON { ignoreUnknownKeys = true; isLenient = true }", "Code"))
story.append(p("La configuración permite evolucionar el backend agregando campos sin romper clientes anteriores. Los campos obligatorios sin default siguen protegiendo la estructura mínima del contrato."))
story.append(PageBreak())

# Tests
story.extend(section("5. Bitácora de pruebas de conexión", "Cinco escenarios solicitados, con resultado y evidencia disponible."))
story.append(table([
    ["ID", "Escenario", "Esperado", "Observado", "Estado"],
    ["P1", "GET exitoso", "200, lista visible", "Backend 200, Logcat y lista Android", "COMPLETO"],
    ["P2", "Recurso inexistente", "404 y mensaje controlado", "Backend real 404 y test automatizado aprobado", "VERIFICADO"],
    ["P3", "Sin conexión", "UI estable y Reintentar", "Mensaje de conexión visible; no se cerró", "COMPLETO"],
    ["P4", "Timeout de 1 ms", "HttpRequestTimeoutException controlada", "MockEngine confirmó mensaje de timeout", "VERIFICADO"],
    ["P5", "Campo JSON desconocido", "Se ignora con configuración final", "Modo tolerante y modo estricto aprobados", "VERIFICADO"],
], [1.0 * cm, 3.1 * cm, 4.3 * cm, 6.0 * cm, 2.2 * cm]))

details = [
    ("P1 · Respuesta exitosa", "Se inició PharmaSoft, se abrió Productos en Android y se recargó la pantalla. Ktor registró REQUEST y RESPONSE 200. La UI mostró los registros del backend. Conclusión: integración de extremo a extremo aprobada."),
    ("P2 · 404", "La solicitud real GET /productos/999999999 devolvió 404. ConexionRestTest comprobó ClientRequestException y el mensaje «No se encontró el producto solicitado.». La aplicación mantiene un UiState de error controlado."),
    ("P3 · Sin conexión", "Con el servicio no accesible, Android mostró «No pudimos cargar el inventario» y «No se pudo conectar con el servicio. Verifica tu conexión a Internet.», además del botón Reintentar."),
    ("P4 · Timeout", "Un cliente de prueba con requestTimeoutMillis=1 y MockEngine retardado produjo HttpRequestTimeoutException. El mapper devolvió «La conexión tardó demasiado. Verifica tu Internet e inténtalo nuevamente.». La configuración productiva quedó restaurada en 15 000 ms."),
    ("P5 · JSON desconocido", "Con ignoreUnknownKeys=true, un campo adicional fue tolerado. Con un cliente estricto temporal, la deserialización falló y fue convertida a «No se pudo interpretar la respuesta del servidor.». No se alteró la configuración final de la aplicación."),
]
for title, body in details:
    story.append(p(title, "H3"))
    story.append(p(body))

story.append(callout("Resultado automatizado", ":shared:testAndroidHostTest ejecutó 22 pruebas sin fallos. ConexionRestTest contiene cuatro pruebas específicas: 404, timeout y las dos variantes de campo desconocido. :androidApp:assembleDebug también terminó correctamente.", "ok"))
story.append(PageBreak())

# Evidence 1
story.extend(section("6. Evidencias de ejecución", "Las imágenes siguientes corresponden a ejecuciones reales conservadas dentro del repositorio."))
story.append(p("Evidencia 1 · Swagger GET 200 y respuesta JSON", "H2"))
story.append(evidence_image(EVIDENCIAS / "evidencia_00_swagger_productos.png", max_height=18.5 * cm))
story.append(p("Swagger ejecutado contra http://localhost:8080/api/v1/productos. Se observan Request URL, código 200 y productos devueltos por PharmaSoft.", "Caption"))
story.append(PageBreak())

story.extend(section("6. Evidencias de ejecución (continuación)"))
story.append(p("Evidencia 2 · Ktor Client en Logcat", "H2"))
story.append(evidence_image(EVIDENCIAS / "evidencia_01_ktor_pharmasoft.png", max_height=11.5 * cm))
story.append(p("Registro extraído del emulador mediante ADB: GET a 10.0.2.2:8080 y RESPONSE 200.", "Caption"))
story.append(Spacer(1, 0.25 * cm))
story.append(p("Trazabilidad de red", "H2"))
story.append(table([
    ["Dato", "Valor"],
    ["Origen", "Android Emulator Medium_Phone"],
    ["Cliente", "Ktor HttpClient"],
    ["URL", "http://10.0.2.2:8080/api/v1/productos?pagina=0&tamanio=20"],
    ["Respuesta", "HTTP 200"],
    ["Configuración final", "Logging ALL · timeout 15 000 ms · ignoreUnknownKeys=true"],
], [4.0 * cm, 12.6 * cm]))
story.append(PageBreak())

story.append(KeepTogether([
    p("6. Evidencias de ejecución (continuación)", "H1"),
    p("Evidencia 3 · Lista de productos en Android", "H2"),
    evidence_image(ACTIVIDAD / "evidencia_03_android_lista_20261004.png", max_width=7.2 * cm, max_height=18.0 * cm),
    p("La interfaz Compose muestra los productos recibidos de PharmaSoft, incluido el registro de evidencia utilizado para comprobar POST y PUT.", "Caption"),
]))
story.append(PageBreak())

story.append(KeepTogether([
    p("6. Evidencias de ejecución (continuación)", "H1"),
    p("Evidencia 4 · Error sin conexión y recuperación", "H2"),
    evidence_image(EVIDENCIAS / "evidencia_04_error_conexion_pharmasoft.png", max_width=6.2 * cm, max_height=15.5 * cm),
    p("La UI conserva estabilidad, explica el problema y ofrece el botón Reintentar.", "Caption"),
]))
story.append(PageBreak())

story.append(KeepTogether([
    p("6. Evidencias de ejecución (continuación)", "H1"),
    p("Evidencia 5 · Pruebas automatizadas de conectividad", "H2"),
    p("Ejecución de ConexionRestTest desde Android Studio. La suite finalizó correctamente con 4 tests passed y BUILD SUCCESSFUL. Esta evidencia respalda los escenarios de recurso inexistente (404), timeout y campos JSON desconocidos."),
    evidence_image(ACTIVIDAD / "evidencia_05_pruebas_conexion.png", max_width=16.2 * cm, max_height=12.0 * cm),
    p("Resultado de ConexionRestTest: cuatro pruebas aprobadas y compilación exitosa.", "Caption"),
]))
story.append(PageBreak())

story.append(KeepTogether([
    p("6. Evidencias de ejecución (continuación)", "H1"),
    p("Evidencia 6 · Código real de ProductoResponseDto", "H2"),
    p("El DTO usado para deserializar productos está declarado con @Serializable. La captura permite contrastar el contrato JSON con los tipos Kotlin, la nulabilidad y los valores predeterminados reales."),
    evidence_image(ACTIVIDAD / "evidencia_06_codigo_dto.png", max_width=13.8 * cm, max_height=15.0 * cm),
    p("ProductoResponseDto.kt con los nueve campos documentados en el diccionario de DTO.", "Caption"),
]))
story.append(PageBreak())

story.append(KeepTogether([
    p("6. Evidencias de ejecución (continuación)", "H1"),
    p("Evidencia 7 · Recurso inexistente: respuesta 404 real", "H2"),
    p("Swagger ejecutó GET /api/v1/productos/{id} con el identificador 999999999. PharmaSoft respondió 404 Not Found y devolvió el mensaje de producto no encontrado, confirmando el escenario P2 con el backend real."),
    evidence_image(ACTIVIDAD / "evidencia_07_swagger_404.png", max_width=16.0 * cm, max_height=17.5 * cm),
    p("Swagger: GET de producto inexistente con código HTTP 404 y respuesta JSON del backend.", "Caption"),
]))
story.append(PageBreak())

# Deliverables / limitations
story.extend(section("7. Entregables y trazabilidad Git"))
story.append(table([
    ["Entregable", "Ubicación / estado"],
    ["Informe PDF", "docs/S07_ActividadAutonoma_Gutierrez.pdf"],
    ["Documentación técnica", "docs/actividad_autonoma_sesion07/README.md"],
    ["Capturas", "docs/evidencias_sesion07 y docs/actividad_autonoma_sesion07"],
    ["Pruebas", "shared/src/commonTest/.../ConexionRestTest.kt"],
    ["README del proyecto", "Sección Conectividad REST actualizada"],
    ["Rama", "feature/ktor-client-Gutierrez"],
    ["Repositorio", "https://github.com/LucianaGutierrez-049/PharmaMobile"],
    ["Commit técnico", "55c54c2 · test: validar errores y compatibilidad JSON de Ktor"],
    ["Commit documental", "b900072 · docs: agregar evidencias de la actividad autonoma 07"],
    ["Publicación", "origin/feature/ktor-client-Gutierrez"],
], [5.0 * cm, 11.6 * cm]))

story.extend(section("8. Consideración de plataforma"))
story.append(callout("iOS", "No se ejecutaron pruebas ni se incluyeron capturas iOS porque el equipo disponible utiliza Windows y el simulador iOS requiere macOS con Xcode. Esta limitación se declara expresamente para mantener la autenticidad de la evidencia.", "warn"))
story.append(Spacer(1, 0.25 * cm))
story.append(p("La cobertura funcional solicitada sí fue verificada en Android y mediante pruebas comunes de Kotlin Multiplatform. Para agregar evidencia iOS auténtica será necesario abrir iosApp/iosApp.xcodeproj en una Mac, ejecutar PharmaSoft y capturar el estado exitoso y un estado de error."))

story.extend(section("9. Checklist final"))
story.append(table([
    ["Criterio", "Resultado"],
    ["Cinco endpoints documentados con entradas, salidas y errores", "CUMPLE"],
    ["DTO completos, nulabilidad, defaults y mapper", "CUMPLE"],
    ["Cinco escenarios documentados y verificados", "CUMPLE"],
    ["Captura Swagger 200 + JSON", "CUMPLE"],
    ["Captura Ktor REQUEST/RESPONSE", "CUMPLE"],
    ["Captura Android de éxito", "CUMPLE"],
    ["Captura Android sin conexión", "CUMPLE"],
    ["Pruebas 404, timeout y JSON desconocido", "CUMPLE mediante tests automatizados"],
    ["README Conectividad REST", "CUMPLE"],
    ["Capturas iOS", "NO APLICA EN ESTE EQUIPO WINDOWS"],
    ["Commits y rama feature", "CUMPLE · 55c54c2 y b900072"],
], [11.2 * cm, 5.4 * cm]))
doc.build(story, onFirstPage=footer, onLaterPages=footer)
print(OUT)
