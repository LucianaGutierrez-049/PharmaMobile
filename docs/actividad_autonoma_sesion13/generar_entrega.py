"""Genera el informe a partir de capturas y estados comprobados del recorrido."""
import json
from datetime import datetime, timezone, timedelta
from pathlib import Path
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_CELL_VERTICAL_ALIGNMENT
from docx.oxml import OxmlElement
from docx.oxml.ns import qn

ROOT = Path(__file__).resolve().parents[2]
CAPS = ROOT / 'docs/actividad_autonoma_sesion13/capturas'
OUT = ROOT / 'output/docx/Gutierrez_Luciana_Sesion13_Evidencias.docx'
names = ['A01_primera_apertura','A02_login','A03_primera_sincronizacion','A04_alta_con_red',
         'A05_sin_red','A06_recuperacion','A07_validacion','A08_sin_privilegio','A09_expirada','A10_logout']
facts = {}
for name in names:
    facts[name] = json.loads((CAPS / (name+'.json')).read_text(encoding='utf8'))
    assert facts[name]['aprobado'] and (CAPS / (name+'.png')).is_file(), name
summary = json.loads((CAPS/'evaluacion.json').read_text(encoding='utf8'))
tests = json.loads((CAPS/'pruebas_20261106.json').read_text(encoding='utf8'))
assert tests['pruebas'] == 82 and tests['fallos'] == tests['errores'] == 0

d = Document()
s = d.sections[0]
s.page_width, s.page_height = Inches(8.5), Inches(11)
s.top_margin = s.bottom_margin = Inches(.6)
s.left_margin = s.right_margin = Inches(.6)
s.footer_distance = Inches(.22)
for name in ['Normal','Title','Subtitle','Heading 1','Heading 2','Caption']:
    st = d.styles[name]
    st.font.name = 'Calibri'
    st.font.color.rgb = RGBColor(0,0,0)
    st.font.size = Pt({'Title':25,'Subtitle':16,'Heading 1':17,'Heading 2':13,'Caption':10}.get(name,11))
    st.paragraph_format.space_after = Pt(7)
d.styles['Normal'].paragraph_format.line_spacing = 1.05
for name in ['Heading 1','Heading 2']:
    d.styles[name].paragraph_format.keep_with_next = True
footer=s.footer.paragraphs[0]
footer.alignment=WD_ALIGN_PARAGRAPH.RIGHT
footer.add_run('PharmaMobil  |  Actividad Autónoma 13  |  ')
field=OxmlElement('w:fldSimple'); field.set(qn('w:instr'),'PAGE'); footer._p.append(field)
for r in footer.runs:r.font.size=Pt(9)

def p(text='',style=None): return d.add_paragraph(text,style)
def h(text): return d.add_heading(text,1)
def page(): d.add_page_break()
def table(headers, rows, widths=None):
    t=d.add_table(rows=1,cols=len(headers)); t.alignment=WD_TABLE_ALIGNMENT.CENTER
    t.autofit=False
    if widths:
        for col,width in zip(t.columns,widths): col.width=Inches(width)
    for cell,value in zip(t.rows[0].cells,headers): cell.text=value
    for row in rows:
        for cell,value in zip(t.add_row().cells,row): cell.text=str(value)
    for i,row in enumerate(t.rows):
        trPr=row._tr.get_or_add_trPr()
        if i==0:trPr.append(OxmlElement('w:tblHeader'))
        trPr.append(OxmlElement('w:cantSplit'))
        for cell in row.cells:
            cell.vertical_alignment=WD_CELL_VERTICAL_ALIGNMENT.CENTER
            tcPr=cell._tc.get_or_add_tcPr()
            shade=OxmlElement('w:shd'); shade.set(qn('w:fill'),'203C57' if i==0 else ('F1F4F7' if i%2==0 else 'FFFFFF')); tcPr.append(shade)
            margins=OxmlElement('w:tcMar')
            for side in ['top','left','bottom','right']:
                e=OxmlElement('w:'+side);e.set(qn('w:w'),'80');e.set(qn('w:type'),'dxa');margins.append(e)
            tcPr.append(margins)
            borders=OxmlElement('w:tcBorders')
            for side in ['top','left','bottom','right']:
                e=OxmlElement('w:'+side);e.set(qn('w:val'),'single');e.set(qn('w:sz'),'4');e.set(qn('w:color'),'D9D9D9');borders.append(e)
            tcPr.append(borders)
            for para in cell.paragraphs:
                para.paragraph_format.space_after=Pt(4)
                for run in para.runs:
                    run.font.size=Pt(10.5)
                    if i==0:run.bold=True;run.font.color.rgb=RGBColor(255,255,255)
    return t

def picture(name,width=6.7,caption=''):
    para=p();para.alignment=WD_ALIGN_PARAGRAPH.CENTER
    para.add_run().add_picture(str(CAPS/(name+'.png')),width=Inches(width))
    if caption:p(caption,'Caption')

def pair(title, a, b, ca, cb):
    h(title)
    t=table([ca[0],cb[0]],[['',''],[ca[1],cb[1]]],[3.65,3.65])
    for cell,name in zip(t.rows[1].cells,[a,b]):
        cell.paragraphs[0].alignment=WD_ALIGN_PARAGRAPH.CENTER
        cell.paragraphs[0].add_run().add_picture(str(CAPS/(name+'.png')),width=Inches(3.08))

def time_lima(millis):
    return datetime.fromtimestamp(millis/1000,timezone(timedelta(hours=-5))).strftime('%H:%M:%S')

p('Universidad Peruana Unión')
p('Facultad de Ingeniería y Arquitectura')
p('Escuela Profesional de Ingeniería de Sistemas')
p('Desarrollo de Aplicaciones Móviles')
p('\n')
p('Evidencias del flujo de PharmaMobil','Title')
p('Actividad Autónoma 13','Subtitle')
p('Proyecto Kotlin Multiplatform integrado con PharmaSoft')
p('\n')
table(['Estudiante','Datos'],[
    ['Nombre','Luciana Cristina Gutierrez Quispe'],['Código','202320154'],['Grupo','2'],
    ['Docente','Benjamin David Reyna Barreto'],['Ciclo y semestre','VI  |  2026-2'],
    ['Fecha de verificación','6 y 10 de noviembre de 2026']], [1.6,5.7])
p('\nEste informe reúne el recorrido Android, la comparación de plataformas, la bitácora de notificaciones y la ficha de integración. Las capturas corresponden al emulador del proyecto; los códigos HTTP y los estados de SQLite se contrastan con registros técnicos sin credenciales.')
page()
h('Índice')
table(['Contenido','Página'],[
    ['Alcance y entorno',2],['Producto A Primera apertura e inicio de sesión',3],
    ['Producto A Primera sincronización y alta con conexión',4],['Producto A Modo avión y recuperación',5],
    ['Producto A Validación y autorización',6],['Producto A Expiración y cierre de sesión',7],
    ['Producto B Evidencias por plataforma',8],['Producto C Bitácora de permisos',9],
    ['Producto C Diálogo y recuperación desde Ajustes',10],['Producto D Ficha de integración',11],
    ['Verificación técnica y Firebase',12],['Lista de cotejo y repositorio',13]],[6.4,.9])
h('Alcance y entorno')
p('Los pasos 1 a 4 y la bitácora de permisos se ejecutaron el 6 de noviembre; los pasos 5 a 10, el 10 de noviembre. PharmaSoft respondió en el puerto 8080 con Oracle activo en su contenedor. ' + summary['conectividad_texto'] + ' Se utilizó el perfil Android Evidencias13 para mantener intactos los datos del propietario del emulador. Las cuentas empleadas son cuentas locales de prueba del backend.')
p('Cada paso muestra una pantalla real y un estado contrastado con SQLite, almacenamiento seguro o la respuesta HTTP. Los inicios de sesión se ejecutan con el repositorio de autenticación de la app; el resto del recorrido opera la interfaz real. El APK de pruebas captura los indicadores técnicos y no se incorpora al APK de producción.')
p('Las capturas muestran el reloj del emulador en UTC; las horas de sincronización del texto se expresan en Lima. La consulta de permisos conserva los diálogos del sistema en inglés. El alcance iOS se declara en su estado real, sin capturas de ejecución.')
page()
pair('Producto A Primera apertura e inicio de sesión',names[0],names[1],
     ('Paso 1 Primera apertura','Sin sesión en el perfil nuevo. Token y perfil guardados: no. SQLite: 0 filas y 0 pendientes. La aplicación presenta el formulario de acceso.'),
     ('Paso 2 Inicio de sesión','HTTP 200 en auth/login. Token y perfil cifrados guardados. Rol ALMACENERO; permisos CATALOGO_LEER y CATALOGO_ESCRIBIR. El menú muestra la sesión vigente.'))
page()
a3=facts[names[2]]
id_red=summary['id_con_red']; id_off=summary['id_sin_red']; id_local=summary['id_local']
pair('Producto A Sincronización y creación con conexión',names[2],names[3],
     ('Paso 3 Primera sincronización',f"GET productos y categorías: HTTP 200. Se descargó {a3['filas_cuenta']} fila inactiva ya existente; pendientes: 0. Última sincronización: {time_lima(a3['ultima_sincronizacion'])} de Lima. La base nueva se pobló desde PharmaSoft."),
     ('Paso 4 Creación con conexión',f"POST productos: HTTP 201. El servicio asignó el ID {id_red} al registro de prueba AA13_. Categoría Pruebas Guia 13, precio S/ 3.50 y stock 12. La copia local quedó sincronizada."))
page()
pair('Producto A Modo avión y recuperación',names[4],names[5],
     ('Paso 5 Creación sin conexión',f"Modo avión activo y túnel ADB retirado, sin acceso al servicio. El registro aa13offline se conservó en SQLite con ID provisional {id_local}, pendiente de sincronizar. Pendientes antes de recuperar la red: 1. La app mantiene el inventario local."),
     ('Paso 6 Recuperación de la señal',f"Tras recuperar la red se revisó el registro y se confirmó Reenviar. POST: HTTP 201; ID definitivo {id_off}. Los pendientes pasaron de 1 a 0. El ID local se sustituyó de forma transaccional. La confirmación evita repetir automáticamente un POST ambiguo."))
page()
pair('Producto A Validación y privilegios',names[6],names[7],
     ('Paso 7 Validación del servicio','Se envió el nombre ab, de dos caracteres. PharmaSoft devolvió HTTP 400 y validationErrors.nombre. La interfaz mostró el mensaje del servicio debajo del campo Nombre. El borrador rechazado se conserva para su revisión; no es un alta remota correcta.'),
     ('Paso 8 Privilegio insuficiente','Con la cuenta VENDEDOR, la interfaz permite consultar el catálogo y oculta el formulario y las acciones de escritura. Una solicitud de auditoría desde el repositorio remoto de la app confirmó HTTP 403 al intentar crear. No se creó el registro prohibido.'))
page()
pair('Producto A Expiración y cierre de sesión',names[8],names[9],
     ('Paso 9 Token vencido','Se utilizó una fixture de prueba con firma local válida y expiración pasada, sin cambiar el reloj ni el backend. La solicitud devolvió HTTP 401. La app borró la sesión segura y volvió al acceso, con el aviso de sesión expirada.'),
     ('Paso 10 Cierre voluntario',f"Se inició sesión nuevamente como ALMACENERO y se pulsó Cerrar sesión. Token y perfil quedaron borrados. SQLite conservó {facts[names[9]]['filas_cuenta']} filas de esa cuenta, incluido el borrador de validación. El acceso queda protegido y los datos no se reasignan a otro usuario."))
page()
h('Producto B Evidencias por plataforma')
p('Android fue ejecutado de principio a fin. iOS tiene código compartido y adaptadores nativos, pero no se presenta como una aplicación compilada o probada en este entorno.')
table(['Aspecto','Android comprobado','iOS al 6 de noviembre'],[
    ['Token','AES GCM con clave en Android Keystore; perfil cifrado. El cierre y el 401 eliminan la sesión segura.','Adaptador Keychain preparado. Falta compilar y ejecutar para acreditar lectura, actualización y cierre.'],
    ['Reinstalación',summary['reinstalacion_texto'],'Comportamiento de Keychain después de reinstalar: sin verificar en dispositivo iOS.'],
    ['Permisos','Diálogo del sistema en inglés. Se observaron dos solicitudes antes del bloqueo definitivo. Recuperación por Ajustes.','UserNotifications implementado en el adaptador. No se acreditan diálogos ni número de solicitudes.'],
    ['Sin red','Alta local con ID negativo, marca pendiente y posterior reenvío confirmado.','Se comparte la lógica, pero no hay ejecución iOS que confirme la experiencia.'],
    ['Notificaciones','Notificación local visible con permiso concedido; denegarlo no impide utilizar el catálogo.','Recepción y presentación sin verificar. No hay evidencia de push en esta actividad.'],
    ['Interfaz','Pantalla compacta con menú lateral, barras Android y navegación gestual; contenido observado sin invadirlas.','Tipografías y áreas seguras pendientes de revisión en simulador o dispositivo.']],[1.05,3.1,3.15])
h('Condición que impide validar iOS')
p("Gradle informó: Task 'iosSimulatorArm64Test' cannot run on the current host (windows-x86_64). Reason: simulator tests require macOS. Este es un límite del entorno comprobado, no un resultado de pruebas iOS.")
p('Para la sesión 14 se propone abrir iosApp.xcodeproj en macOS con Xcode, resolver y enlazar FirebaseCore y FirebaseCrashlytics, incorporar GoogleService-Info.plist, configurar símbolos y ejecutar compilación, permisos, persistencia de sesión y el mismo recorrido. La URL localhost del adaptador iOS debe revisarse antes de usar un iPhone físico.')
page()
h('Producto C Bitácora de permisos')
p('El único permiso de ejecución solicitado por la aplicación es POST_NOTIFICATIONS. INTERNET es un permiso normal declarado en el manifiesto y no muestra diálogo de concesión. No se solicitan cámara, ubicación, contactos ni almacenamiento.')
table(['Estado','Lo que ve el usuario','Recuperación'],[
    ['Concedido','Permiso concedido y botón Probar notificación. El aviso local aparece en el panel nativo.','Puede seguir usando el botón de prueba; puede gestionar el permiso desde Ajustes.'],
    ['Denegado','Permiso pendiente o denegado y botón Permitir notificaciones. El catálogo continúa funcionando.','Volver a solicitar y aceptar el diálogo.'],
    ['Denegado definitivamente','Permiso bloqueado y botón Abrir Ajustes. La app no insiste con un diálogo que Android ya no muestra.','Abrir Ajustes > Notifications > All PHARMAMOBILEE notifications y activar; volver a la app.']],[1.35,3,2.95])
for name,caption in [('C04_concedido','Concedido'),('C01_denegado','Denegado después de la primera solicitud'),('C02_bloqueado','Bloqueado después de la segunda denegación')]:
    picture(name+'_detalle',6.2,caption+'. Fragmento de la tarjeta real; se conserva la captura original completa.')
page()
pair('Producto C Diálogo y acceso a Ajustes','C00_dialogo','C03_ajustes',
     ('Solicitud nativa','Texto observado: Allow PHARMAMOBILEE to send you notifications? Opciones Allow y Don’t allow. Se documentaron dos denegaciones; el sistema no presentó una tercera solicitud desde la aplicación.'),
     ('Recuperación del permiso','El botón Abrir Ajustes llevó a App info de PHARMAMOBILEE. Desde Notifications se activaron las notificaciones y se volvió a la app. El observador de ciclo de vida actualizó la tarjeta a Permiso concedido.'))
page()
h('Producto D Ficha de integración')
p('Decisiones a partir del recorrido y de la revisión del proyecto. Las fechas de la sesión 14 son objetivos de cierre; no indican que esas tareas ya estén ejecutadas.')
table(['N','Hueco detectado','Gravedad','Decisión','Fecha'],[
    ['1','Faltaban evidencias del recorrido de diez pasos.','Alta','Cerrado con capturas y estados técnicos.','10/11/2026'],
    ['2','Faltaba probar 400 por campo, 403 y retorno por 401.','Alta','Cerrado contra PharmaSoft local.','10/11/2026'],
    ['3','Faltaba una bitácora con los tres estados del permiso y recuperación.','Media','Cerrado hoy con diálogo y Ajustes reales.','06/11/2026'],
    ['4','iOS sin compilación ni recorrido acreditados.','Alta','Sesión 14 en macOS y Xcode.','10/11/2026'],
    ['5','Edición y baja de productos no tienen cola offline.','Media','Sesión 14 para definir alcance e implementar si se exige.','10/11/2026'],
    ['6','Clientes registra un resultado académico y Pedidos es una vista pendiente.','Alta','Sesión 14 para integrar los módulos; fuera del recorrido de Productos.','10/11/2026'],
    ['7','Borradores de formulario aún no guardados se pierden al salir de la sesión.','Media','Sesión 14 para decidir persistencia del formulario. Los registros ya guardados sí se conservan.','10/11/2026'],
    ['8','Reenvío automático de un POST ambiguo puede duplicar registros.','Alta','Descartado: el servicio no acredita idempotencia y la app exige confirmación.','06/11/2026'],
    ['9','Los registros antiguos sin cuenta identificada requieren revisión de pertenencia.','Alta','Sesión 14: confirmar cuenta y categoría antes de recuperar o enviar.','10/11/2026'],
    ['10','El emulador no alcanzó 10.0.2.2:8080 el 10/11; se usó el túnel ADB de depuración.','Media','Sesión 14: revisar firewall del host o URL de pruebas en dispositivo real.','10/11/2026']],[.3,2.6,.65,2.7,1.05])
p('La actividad es una documentación del estado real del producto. Los pendientes de la ficha no se declaran implementados ni se ocultan en la lista de cotejo. No fue necesario cambiar las reglas de autorización o validación de PharmaSoft ni la configuración ya acreditada de Firebase Android.')
page()
h('Verificación técnica y Firebase')
table(['Comprobación','Resultado'],[
    ['Pruebas Android host','82 pruebas en 21 suites, ejecutadas de nuevo el 06/11/2026; 0 fallos, 0 errores y 0 omisiones.'],
    ['Compilación','APK Android y APK de instrumentación generados correctamente. El controlador se distribuye solo en el APK de pruebas.'],
    ['Persistencia','SQLite SQLDelight versión 4. Productos, categorías y fecha de descarga separados por cuenta. IDs locales negativos y confirmación transaccional del ID remoto.'],
    ['REST observado','200 en acceso y descargas; 201 en altas; 400 en validación; 403 sin privilegio; 401 con la fixture expirada.'],
    ['Datos del recorrido',summary['limpieza_texto']]], [1.6,5.7])
h('Telemetría Android acreditada')
p('La captura siguiente fue aportada el 4 de noviembre y se conserva como evidencia previa de recepción de una prueba no fatal en Crashlytics. Identifica TelemetriaFirebase.registrarError y las claves codigo=inesperado, operacion=prueba_sesion13, pantalla=productos y tipo=prueba. No es una notificación push ni una prueba iOS.')
prior=ROOT/'docs/sesion13/capturas/E18_crashlytics_claves_20261104.png'
para=p();para.add_run().add_picture(str(prior),width=Inches(6.7))
p('Consola Firebase del proyecto PharmaMobile. Evento controlado del 4 de noviembre de 2026.','Caption')
p('La captura de notificación local del recorrido se conserva con el código C05. La entrega no requiere reemplazar Crashlytics, crear otro proyecto Firebase ni introducir Firestore para la persistencia local de productos.')
page()
h('Lista de cotejo y repositorio')
table(['Criterio','Estado'],[
    ['Portada con nombre, código y grupo','Sí'],['Diez pasos con captura y estado del sistema','Sí'],
    ['Capturas del emulador con explicación','Sí'],['Recorrido Android completo','Sí'],
    ['Estado de iOS fundamentado y plan explícito','Sí; iOS sin ejecutar'],['Tres estados de POST_NOTIFICATIONS y recuperación','Sí'],
    ['Ficha con gravedad, decisión y fecha','Sí'],['Descarte justificado del reenvío automático ambiguo','Sí'],
    ['Tamaño carta, portada, índice y 8 a 14 páginas','Verificado en la revisión final'],
    ['Nombre pedido y archivo menor de 20 MB','Verificado en la revisión final']],[5.9,1.4])
h('Identificación de la versión evaluada')
branch='https://github.com/LucianaGutierrez-049/PharmaMobile/tree/feature/integrador-13'
commit='https://github.com/LucianaGutierrez-049/PharmaMobile/commit/'+summary['commit_evaluado']
p('Rama feature/integrador-13')
p(branch)
p('Commit de implementación y evidencias evaluado')
p(commit)
p('Los enlaces identifican el código comprobado; la documentación puede incorporarse en un commit posterior de la misma rama. Los registros JSON y las capturas originales se conservan en docs/actividad_autonoma_sesion13/capturas.')
h('Referencias')
p('Universidad Peruana Unión. Actividad Autónoma 13 Documentar el flujo completo y las evidencias por plataforma. Ficha de la sesión 13, semestre 2026-2.')
p('Código de PharmaMobil y PharmaSoft revisado y ejecutado el 6 de noviembre de 2026. Evidencias técnicas propias del recorrido y captura previa de Firebase identificada en el informe.')
OUT.parent.mkdir(parents=True,exist_ok=True)
d.core_properties.author='Luciana Cristina Gutierrez Quispe'
d.core_properties.title='Evidencias del flujo de PharmaMobil'
d.save(OUT)
print(OUT)
