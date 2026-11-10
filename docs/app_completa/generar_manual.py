"""Manual de uso de PharmaMobil con capturas reales del 10/11/2026 (versión firmada 1.0.0)."""
from pathlib import Path
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

ROOT = Path(__file__).resolve().parents[2]
CAP = ROOT / 'docs/app_completa/capturas'
OUT = ROOT / 'output/docx/Gutierrez_Luciana_PharmaMobil_Manual_de_Uso.docx'

d = Document()
s = d.sections[0]
s.page_width, s.page_height = Inches(8.5), Inches(11)
for m in ('top_margin', 'bottom_margin', 'left_margin', 'right_margin'):
    setattr(s, m, Inches(.7))
for name in ['Normal', 'Title', 'Subtitle', 'Heading 1', 'Heading 2', 'Caption']:
    st = d.styles[name]; st.font.name = 'Calibri'; st.font.color.rgb = RGBColor(0, 0, 0)
    st.font.size = Pt({'Title': 24, 'Subtitle': 15, 'Heading 1': 16, 'Heading 2': 13, 'Caption': 9.5}.get(name, 10.5))
f = s.footer.paragraphs[0]; f.alignment = WD_ALIGN_PARAGRAPH.RIGHT
f.add_run('PharmaMobil  |  Manual de uso  |  ')
fld = OxmlElement('w:fldSimple'); fld.set(qn('w:instr'), 'PAGE'); f._p.append(fld)


def p(t='', style=None): return d.add_paragraph(t, style)


def table(headers, rows, widths):
    t = d.add_table(rows=1, cols=len(headers)); t.style = 'Table Grid'
    for i, x in enumerate(headers):
        c = t.rows[0].cells[i]; c.text = x; c.paragraphs[0].runs[0].bold = True
        sh = OxmlElement('w:shd'); sh.set(qn('w:fill'), 'D9E2F3'); c._tc.get_or_add_tcPr().append(sh)
    for r in rows:
        cells = t.add_row().cells
        for i, x in enumerate(r): cells[i].text = str(x)
    for row in t.rows:
        for i, c in enumerate(row.cells):
            c.width = Inches(widths[i])
            for para in c.paragraphs:
                for run in para.runs: run.font.size = Pt(9.5)
    p()


def pair(a, b, ca, cb, w=2.6):
    t = d.add_table(rows=2, cols=2)
    for i, (img, cap) in enumerate(((a, ca), (b, cb))):
        t.cell(0, i).paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
        t.cell(0, i).paragraphs[0].add_run().add_picture(str(CAP / img), width=Inches(w))
        t.cell(1, i).text = cap
        for run in t.cell(1, i).paragraphs[0].runs: run.font.size = Pt(9)
    p()


for t in ['Universidad Peruana Unión', 'Desarrollo de Aplicaciones Móviles']:
    p(t).alignment = WD_ALIGN_PARAGRAPH.CENTER
x = p('PharmaMobil', 'Title'); x.alignment = WD_ALIGN_PARAGRAPH.CENTER
x = p('Manual de uso y verificación funcional', 'Subtitle'); x.alignment = WD_ALIGN_PARAGRAPH.CENTER
table(['Dato', 'Detalle'], [
    ['Autora', 'Luciana Cristina Gutierrez Quispe (código 202320154, grupo 2)'],
    ['Versión', '1.0.0 (versionCode 1), APK y AAB firmados'],
    ['Servicio', 'PharmaSoft (Spring Boot + Oracle), API REST v1 con JWT'],
    ['Fecha de verificación', '10 de noviembre de 2026'],
    ['Rama', 'feature/app-completa']], [1.8, 5.0])

d.add_heading('1. Qué hace la aplicación', 1)
p('PharmaMobil es la aplicación móvil de la farmacia. Cada pantalla trabaja con datos reales de PharmaSoft; no hay '
  'pantallas de demostración. Lo que cada persona ve depende del rol de su cuenta.')
table(['Módulo', 'Funciones', 'Roles'], [
    ['Inicio de sesión', 'JWT guardado cifrado; sesión que se recupera al reabrir; cierre automático si el token vence', 'Todos'],
    ['Productos', 'Inventario con pestañas, búsqueda, alta, edición, baja lógica; alta sin conexión con reenvío confirmado', 'Ver: todos. Escribir: ADMIN, ALMACENERO'],
    ['Clientes', 'Listado, búsqueda por nombre/DNI/correo, alta, edición y baja lógica con las reglas de PharmaSoft', 'ADMIN, VENDEDOR'],
    ['Ventas', 'Carrito con control de stock, registro de la venta y historial con detalle', 'Ver: ADMIN, VENDEDOR. Registrar: ADMIN, VENDEDOR'],
    ['Dispositivo', 'Permiso de notificaciones con sus tres estados y telemetría con Crashlytics', 'Todos'],
], [1.3, 3.8, 1.7])

d.add_heading('2. Instalación', 1)
p('1. Copiar Gutierrez_ProyectoFinal_Android_dispositivo.apk al teléfono e instalarlo desde el administrador de archivos '
  '(aceptar "instalar apps desconocidas"). En el emulador se usa Gutierrez_ProyectoFinal_Android.apk.')
p('2. El teléfono debe alcanzar a PharmaSoft: conectarlo a la zona con cobertura de la laptop donde corre el servicio '
  '(http://192.168.137.1:8080). En el emulador se usa http://10.0.2.2:8080.')
p('3. Iniciar sesión con una cuenta de PharmaSoft. Cuentas de prueba del backend local: admin, vendedor y almacen.')

d.add_heading('3. Clientes', 1)
p('Tocar "Nuevo cliente", completar DNI (8 dígitos), nombres, apellidos y correo; teléfono (9 dígitos) y dirección son '
  'opcionales. La app valida antes de enviar y además muestra el mensaje de PharmaSoft si el DNI o el correo ya existen. '
  'El lápiz edita y la papelera da de baja (el cliente queda inactivo y ya no puede recibir ventas).')
pair('A04_cliente_validacion.png', 'A06_cliente_registrado.png', 'Validación antes de enviar.', 'Cliente registrado en PharmaSoft.')
pair('A08_cliente_dni_duplicado.png', 'A09_cliente_baja.png', 'Rechazo 409: DNI duplicado.', 'Baja lógica: cliente inactivo.')

d.add_heading('4. Ventas', 1)
p('Elegir un cliente activo, elegir un producto (solo activos, sincronizados y con stock), indicar la cantidad y tocar '
  '"Agregar". El carrito muestra el total estimado; la app no deja superar el stock. "Registrar venta" la envía a '
  'PharmaSoft, que calcula el total definitivo y descuenta el stock; el inventario se actualiza solo.')
pair('A12_venta_stock_insuficiente.png', 'A13_venta_carrito.png', 'Control de stock en el carrito.', 'Carrito con total estimado.')
pair('A14_venta_registrada.png', 'A15_venta_detalle.png', 'Venta #1 registrada (S/ 13.00).', 'Detalle de la venta.')

d.add_heading('5. Productos', 1)
p('El formulario de alta está plegado; se abre al tocar "Nuevo producto". La categoría es obligatoria y se pide antes '
  'de enviar (defecto D-06 corregido). Sin conexión, el alta queda guardada en el teléfono y se envía al recuperar la '
  'señal, con confirmación para evitar duplicados.')
pair('A16_producto_sin_categoria.png', 'A01_inicio_admin.png', 'Categoría obligatoria (D-06).', 'Inicio con el resumen del inventario.')

d.add_heading('6. Roles verificados', 1)
pair('A17_menu_vendedor.png', 'A19_menu_almacen.png', 'VENDEDOR: Productos, Clientes y Ventas.', 'ALMACENERO: solo Productos.')
pair('A02_menu_admin.png', 'A18_venta_vendedor.png', 'ADMIN: todos los módulos.', 'Venta #2 registrada por el VENDEDOR.')

d.add_heading('7. Verificación', 1)
table(['Comprobación', 'Resultado'], [
    ['Pruebas automáticas', '86 pruebas host (incluye validaciones de cliente, agrupación de líneas, paginación REST y carrito); 0 fallos'],
    ['Clientes', 'Alta, edición, DNI duplicado (409) y baja lógica contra PharmaSoft local'],
    ['Ventas', 'Venta #1 (ADMIN, S/ 13.00) y #2 (VENDEDOR, S/ 25.50); stock 12→10 y 9→8 reflejado en la app'],
    ['Productos', 'Alta sin categoría bloqueada; alta con categoría registrada'],
    ['Roles', 'ADMIN, VENDEDOR y ALMACENERO ven solo sus módulos'],
    ['Build', 'AAB y APK firmados con el almacén del equipo, R8 activo'],
], [1.7, 5.1])
d.add_heading('Límites conocidos', 2)
p('Clientes y ventas requieren conexión (no tienen cola sin conexión, a diferencia del alta de productos). PharmaSoft no '
  'expone la anulación de ventas, por lo que la app solo muestra su estado. iOS queda fuera de esta entrega.')

OUT.parent.mkdir(parents=True, exist_ok=True)
d.core_properties.author = 'Luciana Cristina Gutierrez Quispe'
d.save(OUT)
print(OUT)
