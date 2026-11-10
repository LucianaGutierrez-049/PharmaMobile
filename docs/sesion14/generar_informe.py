"""Informe de la Guía Práctica 14 a partir de evidencias reales del 10/11/2026."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

ROOT = Path(__file__).resolve().parents[2]
S14 = ROOT / 'docs/sesion14'
CAP, EV = S14 / 'capturas', S14 / 'evidencias'
OUT = ROOT / 'output/docx/Gutierrez_Luciana_Guia14_Builds_Firmados.docx'


def terminal(txt_name, png_name, max_lines=24):
    if (CAP / png_name).exists(): return CAP / png_name
    lines = (EV / txt_name).read_text(encoding='utf8').splitlines()[:max_lines]
    font = ImageFont.truetype('C:/Windows/Fonts/consola.ttf', 22)
    w = max(font.getlength(l) for l in lines) + 40
    img = Image.new('RGB', (int(w), 30 * len(lines) + 30), (12, 12, 12))
    d = ImageDraw.Draw(img)
    for i, l in enumerate(lines):
        d.text((20, 15 + 30 * i), l, font=font, fill=(90, 220, 120) if l.startswith('$') else (230, 230, 230))
    img.save(CAP / png_name)
    return CAP / png_name


d = Document()
s = d.sections[0]
s.page_width, s.page_height = Inches(8.5), Inches(11)
for m in ('top_margin', 'bottom_margin', 'left_margin', 'right_margin'):
    setattr(s, m, Inches(.7))
for name in ['Normal', 'Title', 'Subtitle', 'Heading 1', 'Heading 2', 'Caption']:
    st = d.styles[name]; st.font.name = 'Calibri'; st.font.color.rgb = RGBColor(0, 0, 0)
    st.font.size = Pt({'Title': 24, 'Subtitle': 15, 'Heading 1': 16, 'Heading 2': 13, 'Caption': 9.5}.get(name, 10.5))
f = s.footer.paragraphs[0]; f.alignment = WD_ALIGN_PARAGRAPH.RIGHT
f.add_run('PharmaMobil  |  Guía Práctica 14  |  ')
fld = OxmlElement('w:fldSimple'); fld.set(qn('w:instr'), 'PAGE'); f._p.append(fld)


def p(t='', style=None): return d.add_paragraph(t, style)
def h(t, lvl=1): return d.add_heading(t, lvl)


def table(headers, rows, widths=None):
    t = d.add_table(rows=1, cols=len(headers)); t.style = 'Table Grid'
    for i, x in enumerate(headers):
        c = t.rows[0].cells[i]; c.text = x; c.paragraphs[0].runs[0].bold = True
        sh = OxmlElement('w:shd'); sh.set(qn('w:fill'), 'D9E2F3'); c._tc.get_or_add_tcPr().append(sh)
    for r in rows:
        cells = t.add_row().cells
        for i, x in enumerate(r): cells[i].text = str(x)
    for row in t.rows:
        for i, c in enumerate(row.cells):
            for para in c.paragraphs:
                for run in para.runs: run.font.size = Pt(9.5)
            if widths: c.width = Inches(widths[i])
    p()


def pic(path, width, caption):
    para = p(); para.alignment = WD_ALIGN_PARAGRAPH.CENTER
    para.add_run().add_picture(str(path), width=Inches(width))
    c = p(caption, 'Caption'); c.alignment = WD_ALIGN_PARAGRAPH.CENTER


def pair(a, b, ca, cb, w=2.6):
    t = d.add_table(rows=2, cols=2)
    for i, (img, cap) in enumerate(((a, ca), (b, cb))):
        t.cell(0, i).paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
        t.cell(0, i).paragraphs[0].add_run().add_picture(str(CAP / img), width=Inches(w))
        t.cell(1, i).text = cap
        for run in t.cell(1, i).paragraphs[0].runs: run.font.size = Pt(9)
    p()


sha = (EV / 'sha256_artefactos.txt').read_text(encoding='utf8').split()
apks = (EV / 'apksigner_verify.txt').read_text(encoding='utf8')
huella = next(l.split(': ')[1] for l in apks.splitlines() if 'certificate SHA-256' in l)
titular = next(l.split(': ', 1)[1] for l in apks.splitlines() if 'certificate DN' in l)
disp = (EV / 'dispositivo1.txt').read_text(encoding='utf8').strip()

# Portada
for t in ['Universidad Peruana Unión', 'Facultad de Ingeniería y Arquitectura', 'EP Ingeniería de Sistemas',
          'Desarrollo de Aplicaciones Móviles']:
    p(t).alignment = WD_ALIGN_PARAGRAPH.CENTER
p('\n')
x = p('Guía Práctica N.º 14', 'Title'); x.alignment = WD_ALIGN_PARAGRAPH.CENTER
x = p('Mejoras finales, firma y generación de builds AAB e IPA', 'Subtitle'); x.alignment = WD_ALIGN_PARAGRAPH.CENTER
p('\n')
table(['Dato', 'Detalle'], [
    ['Estudiante', 'Luciana Cristina Gutierrez Quispe'], ['Código', '202320154'], ['Grupo', '2'],
    ['Proyecto', 'PharmaMobil (Kotlin Multiplatform) sobre PharmaSoft'],
    ['Docente', 'Benjamin David Reyna Barreto'], ['Fecha de ejecución', '10 de noviembre de 2026'],
    ['Rama', 'feature/builds-14 (desde develop)']], [1.8, 5.0])
d.add_page_break()

h('Índice')
for i, t in enumerate(['Resumen de la entrega', 'Paso 1. Ficha de integración cerrada y alcance congelado',
                       'Pasos 2 y 3. Almacén de claves y firma sin secretos',
                       'Paso 4. Artefactos y verificación de la firma', 'Paso 5. Versión de entrega con R8',
                       'Paso 6. iOS: límite documentado', 'Paso 7. Matriz de pruebas y defectos',
                       'Versionado y nomenclatura', 'Lista de cotejo', 'Repositorio'], 1):
    p(f'{i}. {t}')

h('1. Resumen de la entrega')
table(['Entregable', 'Estado'], [
    ['Ficha de integración cerrada', 'Sí: docs/sesion14/01_ficha_cierre.md'],
    ['keystore.properties documentado sin contraseñas y .gitignore', 'Sí: README y tres líneas en .gitignore'],
    ['AAB firmado e instalable de revisión', 'Sí: entrega/Gutierrez_ProyectoFinal_Android.aab y .apk'],
    ['Captura de la verificación de la firma', 'Sí: apksigner con titular y huella'],
    ['Archivo de iOS', 'No generado: límite documentado (Windows sin Xcode)'],
    ['Matriz de pruebas', 'Emulador (recorrido completo) + OPPO A5 5G y Samsung S26 Ultra físicos'],
], [3.2, 3.6])

h('2. Paso 1. Ficha de integración cerrada y alcance congelado')
p('Se revisaron los diez huecos de la ficha de la sesión 13 y se clasificaron en cerrado, se declara o se descarta. '
  'Desde este punto no entró funcionalidad nueva: solo cambios de empaquetado (firma, versión, reglas R8) y el nombre '
  'visible de la aplicación, que la guía clasifica como texto que se cierra hoy.')
table(['N.º', 'Hueco', 'Clasificación', 'Motivo'], [
    ['1', 'Evidencias del recorrido de diez pasos', 'Cerrado', 'A01–A10 publicadas (680ae2d).'],
    ['2', 'Probar 400, 403 y 401', 'Cerrado', 'Comprobado el 10/11 contra PharmaSoft local.'],
    ['3', 'Bitácora de los tres estados del permiso', 'Cerrado', 'C00–C05 y de nuevo en la versión de entrega.'],
    ['4', 'iOS sin compilación ni recorrido', 'Se declara', 'Requiere macOS y Xcode (paso 6).'],
    ['5', 'Edición y baja sin cola offline', 'Se declara', 'Exige cambiar repositorio y esquema: fuera del congelamiento.'],
    ['6', 'Clientes y Pedidos sin integrar', 'Se declara', 'Integrarlos es funcionalidad nueva.'],
    ['7', 'Borradores no guardados se pierden', 'Se descarta', 'Nunca estuvo en el alcance; trabajo futuro.'],
    ['8', 'Reenvío automático de POST ambiguo', 'Se descarta', 'PharmaSoft no garantiza idempotencia; se confirma.'],
    ['9', 'Registros antiguos sin cuenta', 'Cerrado', 'Asignación con revisión, cubierta por pruebas host.'],
    ['10', 'Emulador sin acceso a 10.0.2.2', 'Cerrado', 'Responde HTTP 200 por 10.0.2.2 y por la IP local.'],
], [.4, 2.3, 1.0, 3.1])

h('3. Pasos 2 y 3. Almacén de claves y firma sin secretos')
p('El almacén se creó una sola vez con keytool, fuera de la carpeta del repositorio (C:/Users/lucia/claves_pharmamobil), '
  'con una copia de respaldo en la misma carpeta. Las contraseñas se generaron aleatoriamente y viven solo en '
  'keystore.properties, que Git ignora; no se transcriben en este informe.')
table(['Dato del almacén', 'Valor'], [
    ['Tipo y algoritmo', 'JKS · RSA 2048 bits · validez 10000 días (hasta 28/03/2054)'],
    ['Alias', 'pharmamobil'], ['Titular', titular], ['Huella SHA-256 del certificado', huella]], [2.0, 4.8])
p('Configuración aplicada en androidApp/build.gradle.kts: lee keystore.properties con if (f.exists()); solo crea la '
  'configuración de firma release si el archivo existe, de modo que quien clone el repositorio compila la versión de '
  'depuración sin credenciales. La versión release usa esa firma, isMinifyEnabled = true e isShrinkResources = true. '
  'El .gitignore incluye keystore.properties, *.jks y *.keystore (git check-ignore lo confirma).')

h('4. Paso 4. Artefactos y verificación de la firma')
table(['Artefacto', 'Tarea', 'SHA-256'], [
    ['Gutierrez_ProyectoFinal_Android.aab', ':androidApp:bundleRelease', sha[0]],
    ['Gutierrez_ProyectoFinal_Android.apk', ':androidApp:assembleRelease', sha[2]]], [2.3, 1.7, 2.8])
pic(terminal('apksigner_verify.txt', 'F01_apksigner.png', 13), 6.6,
    'Figura 1. apksigner verify --print-certs sobre el APK de entrega: firma v2 válida, titular y huella SHA-256.')
pic(terminal('jarsigner_aab.txt', 'F02_jarsigner_aab.png'), 6.6,
    'Figura 2. jarsigner -verify sobre el AAB: firmado por el mismo titular (SHA384withRSA, 2048 bits).')
p('bundletool: el JAR disponible en la caché de Gradle es solo la librería, sin dependencias, y descargar el ejecutable '
  'oficial no fue autorizado en esta sesión. La guía marca ese paso como opcional; el instalable de revisión es el APK '
  'firmado de assembleRelease.')

h('5. Paso 5. Versión de entrega con R8')
p('Se agregaron reglas mínimas en proguard-rules.pro: conservar los serializadores generados de kotlinx.serialization, '
  'los atributos de anotación y la firma genérica, y silenciar advertencias de clases opcionales de Ktor/OkHttp. Koin se '
  'usa con definiciones explícitas (sin reflexión), por lo que no necesitó reglas. El APK minificado se instaló limpio y '
  'se recorrió: el inicio de sesión, la descarga de productos y categorías y el CRUD funcionaron, lo que confirma que la '
  'serialización y la inyección sobrevivieron a R8.')
p('Observación: R8 emitió avisos de metadatos de Kotlin (Kotlin 2.4.10 es más nuevo que el R8 de AGP 9.0.1). No '
  'provocaron fallos en el recorrido; se registran como defecto D-03.')

h('6. Paso 6. iOS: límite documentado')
p('El equipo de trabajo es Windows. El iPhone de la estudiante se conectó por USB-C y Windows lo reconoce '
  '(Get-PnpDevice: "Apple iPhone" y "Apple Mobile Device USB Composite Device", estado OK), pero Xcode solo existe en '
  'macOS: no es posible configurar Signing & Capabilities, archivar ni exportar el .ipa. Gradle confirma el límite:')
pic(terminal('ios_gradle.txt', 'F03_ios_gradle.png', 6), 6.6,
    'Figura 3. Gradle omite las tareas iOS en windows-x86_64: el simulador y el enlace requieren macOS.')
p('Plan: abrir iosApp.xcodeproj en una Mac con Xcode, seleccionar el equipo (cuenta gratuita), destino Any iOS Device, '
  'Product > Archive e instalar en el iPhone propio; con cuenta gratuita el perfil caduca a los siete días y no hay '
  'exportación de .ipa, por lo que el entregable sería el archivo de Xcode, la captura del organizador y una foto de la '
  'app corriendo.')

h('7. Paso 7. Matriz de pruebas y defectos')
p(f'Se probó en tres equipos: el emulador {disp} (perfil Medium Phone) con el recorrido completo, y dos teléfonos '
  'físicos de marcas distintas, un OPPO A5 5G y un Samsung S26 Ultra. En ambos teléfonos la depuración USB estaba '
  'desactivada, así que el APK firmado (variante con -Ppharma.apiUrl=http://192.168.137.1:8080/api/v1/) se copió por '
  'transferencia de archivos y se instaló desde el administrador de archivos, sin el entorno de desarrollo conectado. '
  'La red del campus aísla a los dispositivos; el teléfono alcanzó PharmaSoft a través de la zona con cobertura de la '
  'laptop. La matriz siguiente corresponde al emulador; los teléfonos se documentan en la sección 7.1.')
table(['N.º', 'Paso', 'Resultado observado', 'Cumple'], [
    ['1', 'Instalación desde el archivo firmado', 'Instala limpio; versión 1.0.0; nombre PharmaMobil. Ícono genérico (D-02).', 'Parcial'],
    ['2', 'Login correcto e incorrecto', '"Usuario o contraseña incorrectos" vs "No se pudo conectar con PharmaSoft"; login correcto descarga 3 productos.', 'Sí'],
    ['3', 'CRUD con rechazo de validación', 'Alta #42, edición stock 10→3, baja lógica a Inactivos; "ab" → 400 bajo Nombre.', 'Sí'],
    ['4', 'Operación sin privilegio', 'Vendedor ve el catálogo sin formulario ni editar/eliminar; la sesión sigue abierta.', 'Sí'],
    ['5', 'Modo avión: leer y crear', 'Lista visible; S14Offline creado con código #-2 "Pendiente de sincronizar".', 'Sí'],
    ['6', 'Recuperar conexión y sincronizar', 'Sube como #43; una sola coincidencia (sin duplicado); "0 cambios por enviar".', 'Sí'],
    ['7', 'Permiso: conceder, denegar, para siempre', 'Diálogo nativo, denegado, bloqueado con "Abrir Ajustes", activado en Ajustes y notificación recibida.', 'Sí'],
    ['8', 'Cerrar sesión y reabrir', '"Sesión cerrada correctamente"; tras forzar cierre y reabrir vuelve al login.', 'Sí'],
], [.4, 1.6, 3.9, .7])
pair('R01_instalada_login.png', 'R01b_lanzador.png', 'Paso 1. Primera apertura del APK firmado.',
     'Paso 1. Lanzador: nombre PharmaMobil con ícono genérico.')
pair('R02_login_incorrecto.png', 'R03_login_sin_red.png', 'Paso 2. Credencial inválida.',
     'Paso 2. Sin conexión: mensaje distinto.')
pair('R04_login_correcto.png', 'R05b_listado_id42.png', 'Paso 2. Login correcto con R8 activo.',
     'Paso 3. Alta con código #42 asignado por PharmaSoft.')
pair('R06_editar.png', 'R07a_confirmar_baja.png', 'Paso 3. Edición: stock 3, marcado Bajo.',
     'Paso 3. Confirmación de baja.')
pair('R07_eliminar.png', 'R08_validacion_400.png', 'Paso 3. Baja lógica: aparece en Inactivos.',
     'Paso 3. HTTP 400: mensaje bajo Nombre.')
pair('R09a_menu_vendedor.png', 'R09_sin_privilegio.png', 'Paso 4. Sesión VENDEDOR.',
     'Paso 4. Catálogo sin acciones de escritura.')
pair('R10_modo_avion_pendiente.png', 'R11_sincronizado.png', 'Paso 5. Modo avión: #-2 pendiente.',
     'Paso 6. Sincronizado como #43, sin duplicado.')
pair('R12a_dialogo.png', 'R12c_bloqueado.png', 'Paso 7. Diálogo nativo del permiso.',
     'Paso 7. Bloqueado: ofrece Abrir Ajustes.')
pair('R12e_activar.png', 'R12g_notificacion.png', 'Paso 7. Activado en Ajustes.',
     'Paso 7. Notificación de prueba recibida.')
pair('R13a_logout.png', 'R13_reabrir.png', 'Paso 8. Sesión cerrada.', 'Paso 8. Reapertura: vuelve al login.')

h('7.1 Dispositivos reales', 2)
table(['Paso', 'OPPO A5 5G', 'Samsung S26 Ultra'], [
    ['1 Instalación desde el archivo', 'Sí: instalado desde Descargas; nombre PharmaMobil', 'Sí: instalado desde Descargas'],
    ['2 Login', 'Correcto como ALMACENERO (Jorge Mendoza Huaman)', 'Muestra "No se pudo conectar con PharmaSoft" (sin acceso a la red de la laptop)'],
    ['3 CRUD / lectura', 'Descarga los productos reales #21, #41 y #43 de PharmaSoft', 'No ejecutado'],
    ['5 Alta con rechazo', 'Paracetamol sin categoría: queda #-1 pendiente y en revisión (D-06)', 'No ejecutado'],
    ['7 Permiso', 'Concedido desde la tarjeta; queda "Permiso concedido"', 'No ejecutado'],
    ['8 Cerrar sesión', '"Sesión cerrada correctamente"', 'No ejecutado'],
], [1.6, 2.7, 2.5])
TEL = CAP / 'telefono'
def pair_t(a, b, ca, cb, w=2.2):
    t = d.add_table(rows=2, cols=2)
    for i, (img, cap) in enumerate(((a, ca), (b, cb))):
        t.cell(0, i).paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
        t.cell(0, i).paragraphs[0].add_run().add_picture(str(TEL / img), width=Inches(w))
        t.cell(1, i).text = cap
        for run in t.cell(1, i).paragraphs[0].runs: run.font.size = Pt(9)
    p()
pair_t('T01_oppo_inicio.png', 'T03_oppo_menu_almacenero.png', 'OPPO A5 5G. Inicio con datos sincronizados de PharmaSoft.',
       'OPPO A5 5G. Sesión ALMACENERO con el APK firmado.')
pair_t('T05_oppo_listado.png', 'T02_oppo_formulario.png', 'OPPO A5 5G. Productos reales #43, #41 y #21.',
       'OPPO A5 5G. Formulario de alta en tema oscuro.')
pair_t('T04_oppo_pendiente.png', 'T07_oppo_guardado_local.png', 'OPPO A5 5G. Paracetamol sin categoría queda #-1 pendiente.',
       'OPPO A5 5G. Guardado en el dispositivo y en revisión (D-06).')
pair_t('T08_oppo_permiso_concedido.png', 'T09_oppo_sesion_cerrada.png', 'OPPO A5 5G. Permiso de notificaciones concedido.',
       'OPPO A5 5G. Sesión cerrada correctamente.')
pair_t('T00_samsung_login_sin_red.png', 'T06_oppo_revision_categoria.png', 'Samsung S26 Ultra. APK instalado; sin acceso al servicio.',
       'OPPO A5 5G. Mensaje de revisión: categoriaId.')

h('Registro de defectos', 2)
table(['ID', 'Dispositivo', 'Pasos', 'Esperado', 'Obtenido', 'Sev.', 'Estado'], [
    ['D-01', disp, '1. Instalar APK. 2. Ver nombre en el lanzador.', 'Nombre del producto', 'Mostraba PHARMAMOBILEE',
     'Baja', 'Corregido (app_name = PharmaMobil) antes del build final'],
    ['D-02', disp, '1. Instalar APK. 2. Ver ícono.', 'Ícono propio', 'Ícono genérico de Android', 'Baja',
     'Pendiente: cambio de recursos tras congelar'],
    ['D-03', 'Compilación', '1. ./gradlew bundleRelease.', 'Sin avisos', 'R8 avisa metadatos de Kotlin 2.4.10', 'Baja',
     'Pendiente: sin efecto observado; actualizar AGP'],
    ['D-04', 'OPPO A5 5G, Samsung S26 Ultra', '1. Activar depuración USB.', 'ADB disponible', 'Depuración USB desactivada',
     'Baja', 'Descartado: se instaló por transferencia de archivos'],
    ['D-05', 'Dispositivo físico', '1. Usar la app en un teléfono real.', 'Acceso al servicio',
     'La URL release apunta a 10.0.2.2 (solo emulador)', 'Alta', 'Corregido (35ba115): -Ppharma.apiUrl y cleartext para 192.168.137.1'],
    ['D-06', 'OPPO A5 5G', '1. Productos. 2. Nuevo producto sin elegir categoría. 3. Registrar.', 'Error en el formulario antes de guardar',
     'Se guarda local como #-1 pendiente y en revisión (categoriaId)', 'Media', 'Pendiente: validar categoría en el formulario (tras congelar)'],
], [.45, 1.0, 1.3, .9, 1.2, .45, 1.3])

h('8. Versionado y nomenclatura')
table(['Dato', 'Valor'], [['versionCode', '1'], ['versionName', '1.0.0'],
                          ['applicationId', 'pe.edu.upeu.pharmamobilee'],
                          ['Etiqueta de Git', 'v1.0-final (sobre el commit compilado)'],
                          ['Archivo', 'Gutierrez_ProyectoFinal_Android.aab']], [2.0, 4.8])
p('El applicationId se conserva en pe.edu.upeu.pharmamobilee en lugar de pe.upeu.<proyecto>: cambiarlo rompería la '
  'configuración de Firebase (google-services.json está registrado para ese paquete) después del congelamiento. '
  'aapt2 confirma el paquete, versionCode 1 y versionName 1.0.0 en el APK entregado.')

h('9. Lista de cotejo')
table(['N.º', 'Criterio', 'Cumple'], [
    ['1', 'Ficha cerrada y alcance congelado', 'Sí'],
    ['2', 'Almacén existe y copia fuera del equipo de un integrante', 'Parcial: respaldo local; falta copia externa'],
    ['3', 'Almacén y contraseñas fuera del repositorio', 'Sí'],
    ['4', 'Compila sin el archivo de propiedades', 'Sí (configuración condicional)'],
    ['5', 'AAB firmado generado', 'Sí'],
    ['6', 'APK de entrega instalado en dispositivo real', 'Sí: OPPO A5 5G y Samsung S26 Ultra'],
    ['7', 'Verificación con titular y huella', 'Sí'],
    ['8', 'Versión de entrega recorrida; serialización e inyección', 'Sí'],
    ['9', 'Decisión sobre minificación', 'Minificación activa'],
    ['10', 'iOS firmado o límite documentado', 'Límite documentado'],
    ['11', 'Dos dispositivos reales', 'Sí: OPPO (recorrido) y Samsung (instalación, sin red)'],
    ['12', 'Defectos con ID, pasos, severidad y estado', 'Sí'],
    ['13', 'Versión e identificador declarados y coinciden', 'Sí'],
    ['14', 'Commits por paso', 'Sí'],
], [.4, 3.8, 2.6])

h('10. Repositorio')
p('https://github.com/LucianaGutierrez-049/PharmaMobile/tree/feature/builds-14')
p('Los artefactos firmados están en entrega/ y las evidencias en docs/sesion14/. Ni el almacén ni sus contraseñas forman '
  'parte del repositorio.')

OUT.parent.mkdir(parents=True, exist_ok=True)
d.core_properties.author = 'Luciana Cristina Gutierrez Quispe'
d.save(OUT)
print(OUT)
