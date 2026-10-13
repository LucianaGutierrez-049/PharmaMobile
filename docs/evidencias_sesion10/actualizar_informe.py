"""Actualiza el informe con capturas reales, preservando sus cinco páginas técnicas."""
from io import BytesIO
from pathlib import Path
from PIL import Image as PILImage
from pypdf import PdfReader, PdfWriter
from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Image, PageBreak

ROOT = Path(__file__).resolve().parents[2]
EVIDENCIAS = Path(__file__).resolve().parent
PDF = ROOT / 'output/pdf/Informe_Guia_Practica_10_PharmaMobile_Gutierrez.pdf'
VERDE = colors.HexColor('#00695C')
styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name='TituloE', fontName='Helvetica-Bold', fontSize=17, leading=21, textColor=VERDE, spaceAfter=12))
styles.add(ParagraphStyle(name='TextoE', fontName='Helvetica', fontSize=10, leading=14, spaceAfter=9))

CAPTURAS = [
 ('03_admin.png', 'Estado de autenticación', 'Compose muestra Autenticando y deshabilita los campos y el botón mientras espera la respuesta. La contraseña permanece oculta.'),
 ('04_resultado_login.png', 'Login ADMIN exitoso', 'Después de autenticar contra PharmaSoft, la aplicación accede a Inicio y muestra sus accesos rápidos. La respuesta de login fue HTTP 200.'),
 ('05_menu_admin.png', 'Navegación del administrador', 'La cabecera identifica al usuario de prueba y al rol ADMIN. Productos, Clientes y Pedidos están disponibles conforme a los permisos del backend.'),
 ('06_sesion_recuperada.png', 'Recuperación después del reinicio', 'Se detuvo el proceso Android y se abrió nuevamente la aplicación. Se recuperó el token guardado, /auth/me respondió HTTP 200 y apareció Inicio sin volver a ingresar credenciales.'),
 ('07_logout.png', 'Cierre de sesión', 'Al pulsar Cerrar sesión se volvió al login y apareció Sesión cerrada correctamente. El repositorio elimina el token y limpia la caché Bearer.'),
 ('08_almacen.png', 'Rechazo de credenciales incorrectas', 'Una solicitud con usuario inválido recibió HTTP 401. La interfaz mostró Usuario o contraseña incorrectos y permitió corregir los campos.'),
 ('10_menu_almacen.png', 'Menú restringido de ALMACENERO', 'El rol ALMACENERO dispone de Productos y Acerca de. Clientes y Pedidos no aparecen, porque la sesión carece de esos permisos.'),
 ('11_productos_bearer.png', 'Catálogo protegido y permiso de escritura', 'La pantalla Productos muestra tres productos remotos. Nuevo producto está habilitado porque ALMACENERO posee CATALOGO_ESCRIBIR. Las consultas protegidas usan Bearer automáticamente.'),
 ('12_postman_login_401.jpg', 'Postman: contraseña incorrecta', 'POST /api/v1/auth/login devuelve 401 Unauthorized y el mensaje Usuario o contraseña incorrectos. Es una respuesta real del backend local.'),
 ('13_postman_health_200.jpg', 'Postman: backend disponible', 'GET /api/health sin token devuelve 200 OK y Backend funcionando correctamente. La prueba de la colección también aparece aprobada.'),
 ('14_postman_sin_token_401.jpg', 'Postman: recurso protegido sin token', 'GET /api/v1/auth/me sin autenticación devuelve 401 Unauthorized: Debe iniciar sesión para acceder a este recurso.'),
 ('15_postman_admin_200.jpg', 'Postman: autenticación ADMIN', 'La respuesta es 200 OK. Las cuatro pruebas pasan: estado HTTP, token Bearer, rol ADMIN y ausencia de contraseña/hash en la respuesta. Se muestra Test Results para evitar publicar el JWT.'),
 ('16_postman_almacen_200.jpg', 'Postman: autenticación ALMACENERO', 'La respuesta es 200 OK y las cuatro pruebas pasan, incluido el rol ALMACENERO. La captura no expone el token ni las credenciales.'),
]
if (EVIDENCIAS/'17_postman_clientes_403.jpg').exists():
    CAPTURAS.append(('17_postman_clientes_403.jpg', 'Postman: autorización insuficiente', 'GET /api/v1/clientes con la sesión ALMACENERO devuelve 403 Forbidden. El usuario está autenticado, pero no posee permiso de lectura de clientes.'))

def pie(canvas, doc):
    canvas.setFillColor(VERDE)
    canvas.setFont('Helvetica-Bold', 9)
    canvas.drawString(18*mm, A4[1]-13*mm, 'PharmaMobile - Guía Práctica N.º 10')
    canvas.setFont('Helvetica', 8)
    canvas.drawString(18*mm, 11*mm, 'Luciana Gutierrez - Verificación: 13 de octubre de 2026')
    canvas.drawRightString(A4[0]-18*mm, 11*mm, f'Página {doc.page+5}')

def p(text):
    return Paragraph(text, styles['TextoE'])

buffer = BytesIO()
story = [Paragraph('5. Verificación y evidencias completadas', styles['TituloE'])]
story += [p('La compilación final terminó con BUILD SUCCESSFUL. Se generó el APK Android, se compiló Kotlin para iosSimulatorArm64 y pasaron las 39 pruebas Android host, con cero fallos, errores u omitidas.'),
          p('En Android se ejecutaron realmente login ADMIN, recuperación de sesión tras reiniciar el proceso, logout, error por credenciales inválidas y acceso ALMACENERO. Logcat confirmó Authorization: *** y HTTP 200 para productos y /auth/me.'),
          p('En Postman se ejecutó la colección de seguridad contra el backend local. Las capturas muestran health 200, credenciales inválidas 401, ausencia de token 401 y login de ambos roles con sus cuatro pruebas aprobadas.'),
          p('Las páginas siguientes incluyen las capturas originales y una descripción de lo que comprueba cada una. Los JWT y las contraseñas válidas no se incluyen en las imágenes del informe.'),
          p('El primer bloqueo del emulador quedó superado. Las evidencias Android ya están realizadas. iOS cuenta con implementación y compilación, pero sigue sin ejecución real por falta de Mac/Xcode.'),
          p('Alcance: estas capturas no sustituyen pruebas de expiración temporal del JWT ni una ejecución iOS. Los estados 401/403 fueron además verificados mediante pruebas de integración del backend y pruebas automáticas móviles.'),
          p('Git: rama feature/auth-jwt-Gutierrez. La implementación se guardó en el commit 1fc3834. Las evidencias se incorporan como documentación adicional. No se realiza push.'),
          PageBreak()]
for index, (archivo, titulo, descripcion) in enumerate(CAPTURAS, 1):
    story.append(Paragraph(f'Evidencia {index:02d}. {titulo}', styles['TituloE']))
    story.append(p(descripcion))
    story.append(Spacer(1, 4*mm))
    path = EVIDENCIAS/archivo
    with PILImage.open(path) as im:
        width, height = im.size
    factor = min(174*mm/width, 204*mm/height)
    story.append(Image(str(path), width=width*factor, height=height*factor))
    story.append(Spacer(1, 3*mm))
    story.append(p(f'Fuente: captura real de Android o Postman. Archivo: {archivo}'))
    if index < len(CAPTURAS):
        story.append(PageBreak())
SimpleDocTemplate(buffer, pagesize=A4, leftMargin=18*mm, rightMargin=18*mm, topMargin=22*mm, bottomMargin=20*mm).build(story, onFirstPage=pie, onLaterPages=pie)
original = PdfReader(PDF)
writer = PdfWriter()
for page in original.pages[:5]:
    writer.add_page(page)
for page in PdfReader(buffer).pages:
    writer.add_page(page)
writer.add_metadata({'/Title':'Guía Práctica 10 - PharmaMobile con evidencias', '/Author':'Luciana Gutierrez'})
with PDF.open('wb') as output:
    writer.write(output)
print(f'Informe actualizado: {len(writer.pages)} páginas, {len(CAPTURAS)} capturas')
