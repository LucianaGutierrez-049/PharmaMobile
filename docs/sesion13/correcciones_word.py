"""Actualiza los slots de la plantilla conservando las evidencias históricas fechadas."""
import json
from docx.shared import Inches


def aplicar(d, ps, root):
    caps = root / 'docs/sesion13/capturas'
    facts = json.loads((caps / 'validacion_correcciones_20261104.json').read_text(encoding='utf-8'))
    native_path = caps / 'validacion_integracion_20261104.json'
    native = json.loads(native_path.read_text(encoding='utf-8')) if native_path.exists() else {}
    aprobado = native.get('aprobado', False)
    cierre = ('Alta, edición, categoría local y baja lógica verificadas contra PharmaSoft.' if aprobado else
              'La integración REST actual requiere completar la prueba con una sesión válida del emulador.')
    d.tables[1].cell(0, 0).text = (
        f'ESTADO AL 4 DE NOVIEMBRE DE 2026\n{facts["tests"]} pruebas Android host aprobadas y compilación correcta. '
        'Migración a versión 4, categorías y separación por cuenta implementadas. ' + cierre +
        ' Recepción en Crashlytics e iOS pendientes de validación externa.')
    ps[51].text = (
        'Las capturas del 3 de noviembre documentan el estado inicial y sus limitaciones, incluido un POST 404. '
        'La revisión del 4 de noviembre conserva la categoría en SQLite, elimina la categoría 1 impuesta en producción '
        'y permite elegir categorías descargadas. La caché, categorías y fecha de descarga se separan por cuenta. '
        'Los pendientes antiguos se preservan sin propietario; solo una confirmación explícita permite recuperarlos, '
        'con su historial y revisión obligatoria. ' + cierre)
    ps[54].text = (
        'La migración 2.sqm incorporó los metadatos de envío en la versión 3. La corrección 3.sqm lleva '
        'el esquema a versión 4 e incorpora propietario, categoría y catálogo local por cuenta. '
        'Las pruebas migran una base anterior hasta versión 4 y comprueban la conservación de sus pendientes. '
        'Los registros antiguos sin propietario no se asignan automáticamente a la primera cuenta que accede.')
    ps[93].text = (
        'Las pruebas JDBC de SQLite están en androidHostTest; commonTest conserva las pruebas portables '
        'con MockEngine, contratos falsos y lógica de dominio. FormularioOfflineTest conecta el ViewModel '
        'con SQLite y el repositorio offline real para comprobar 400 y 401. La ejecución Android host '
        'no constituye validación nativa de iOS ni sustituye las pruebas del emulador.')
    from copy import deepcopy
    for values in [('propietario', 'TEXT NOT NULL', 'Separa el inventario y los pendientes por cuenta.'),
                   ('categoriaId / categoria', 'INTEGER NULL / TEXT', 'Conserva la referencia y el nombre descargados.')]:
        d.tables[4]._tbl.append(deepcopy(d.tables[4].rows[-1]._tr))
        for cell, value in zip(d.tables[4].rows[-1].cells, values): cell.text = value
    d.tables[3].cell(8, 2).text = 'Token y caché Bearer se eliminan al salir; SQLite conserva los datos separados por cuenta.'
    d.tables[3].cell(8, 3).text = 'Aislamiento corregido el 4 de noviembre; pendientes antiguos requieren confirmación de propiedad.'
    d.tables[3].cell(8, 5).text = 'Pruebas de aislamiento aprobadas; no se borran pendientes de otra cuenta.'
    referencias = [
        'ProductoRepositorioOfflineTest\nFallo transitorio y bloqueo del reenvío ambiguo.',
        'ProductoRepositorioOfflineTest\nFallo permanente y procesamiento de otras filas.',
        'ProductoRepositorioOfflineTest\nConfirmación del envío al recuperar conexión.',
        'GestorPermisosTest\nEstado concedido y evidencia nativa histórica.',
        'GestorPermisosTest\nDenegación inicial y evidencia nativa histórica.',
        'GestorPermisosTest\nBloqueo definitivo y acceso a Ajustes.'
    ]
    for index, referencia in enumerate(referencias, 1):
        d.tables[12].cell(index, 4).text = referencia
    if aprobado:
        d.tables[3].cell(5, 3).text = 'Alta real confirmada el 4 de noviembre con categoría válida; el 404 inicial queda como antecedente.'
        d.tables[3].cell(5, 5).text = 'Alta, lectura, edición y baja lógica comprobadas; pendiente anterior conservado.'
    ps[110].text = (
        f'La verificación actual ejecutó {facts["tests"]} pruebas en {facts["suites"]} suites, sin fallos, errores ni omisiones. '
        'Se generaron los APK de aplicación y pruebas. El esquema versión 4 conserva categorías y propietario, '
        'y la migración mantiene los datos anteriores. Se probaron el aislamiento entre cuentas, recuperación explícita, '
        'validación por campo con repositorio offline, cierre de sesión por 401, cancelación y concurrencia. ' + cierre +
        ' La configuración Firebase Android está incorporada; su recepción en consola y la ejecución iOS siguen pendientes.')
    ps[97].text = (
        f'El 4 de noviembre de 2026, :shared:testAndroidHostTest ejecutó {facts["tests"]} pruebas, con cero fallos, '
        'errores y omisiones. :androidApp:assembleDebug y :androidApp:assembleDebugAndroidTest terminaron con BUILD SUCCESSFUL. '
        'Se ejecutó con memoria limitada y dependencias locales. AndroidX Core se alineó en 1.17.0: 1.19.0 exige '
        'compileSdk 37 y AGP superior, mientras este proyecto mantiene compileSdk 36 y AGP 9.0.1. '
        'Los resultados anteriores de 67 pruebas corresponden al 3 de noviembre y se conservan como evidencia histórica.')
    ps[98].text = (f'Total actual: {facts["tests"]} pruebas. Correctas: {facts["tests"]}. Fallidas: 0. Errores: 0. '
                   f'Omitidas: 0. Suites: {facts["suites"]}. Incluye 10 pruebas nuevas de correcciones, '
                   '2 del formulario con repositorio offline real y 3 del adaptador de telemetría.')
    ps[116].text = ('1. ' + ('Conservar el producto de prueba identificado y revisar los pendientes antiguos antes de reasignarlos o reenviarlos.'
                              if aprobado else 'Completar la prueba REST actual con sesión válida: alta, edición, persistencia de categoría y baja lógica.'))
    ps[118].text = (
        '3. Validar en macOS/Xcode el perfil iOS preparado en Keychain y completar la vinculación nativa Firebase. '
        'Los formularios aún no guardados y la cola offline de PUT/DELETE permanecen fuera del cierre actual; '
        'los productos ya guardados sí se conservan. No se ha hecho push.')
    ps[134].text = (
        'Anexo D Firebase\nContrato Telemetria y adaptador de datos con códigos técnicos seguros. '
        'Las pruebas verifican que no se envían mensaje ni causa originales, tokens o campos personales. '
        'Los fallos de descarga también se registran. La acción de diagnóstico solo aparece en depuración. '
        'La recepción en consola no se considera aprobada por compilar ni por solicitar un registro.')
    ps[131].text = (
        'Anexo A Esquema y migración\n3.sqm actualiza versión 3 a 4 conservando las filas bajo un propietario legado. '
        'Producto.sq incorpora categoría y clave compuesta por cuenta e identificador. La recuperación explícita '
        'evita colisiones de ID y conserva intentos, error y próxima fecha. No se desinstaló la aplicación para aplicar cambios.')
    ps[107].text = (
        'El cierre conserva los cambios previos de autenticación que se revisaron y probaron junto a las correcciones. '
        'Otros entregables y archivos temporales permanecen fuera del alcance. El historial de la figura es del 3 de noviembre; '
        'los commits posteriores de corrección se consultan en Git. No se ha publicado en GitHub.')
    for p in d.paragraphs:
        if 'Figura ' in p.text and '4 de noviembre' not in p.text:
            p.add_run('\nEvidencia del 3 de noviembre de 2026; describe la versión anterior a las correcciones finales.')
    from docx.shape import InlineShape
    for index in (82, 83, 84):
        for inline in ps[index]._p.xpath('.//wp:inline'):
            shape = InlineShape(inline)
            ratio = shape.height / shape.width
            shape.width = Inches(5.3)
            shape.height = int(shape.width * ratio)
    for row in d.tables[13].rows[1:]:
        row.cells[4].text = 'Evidencia histórica del 3 de noviembre; ver actualización y resultados del 4 de noviembre.'
    d.tables[15].cell(2, 3).text = 'Esquema versión 4 y migración conservadora probados en Android host.'
    d.tables[15].cell(6, 2).text = 'Parcial'
    d.tables[15].cell(6, 3).text = 'Se conservan pendientes y categorías. Falta validación completa de todas las operaciones offline.'
    d.tables[15].cell(13, 3).text = f'{facts["tests"]} pruebas aprobadas en Android host; no equivalen a ejecución iOS.'
    d.tables[15].cell(14, 2).text = 'Parcial'
    d.tables[15].cell(14, 3).text = 'Commits reales presentes; no se acredita distribución temporal de todos los pasos. Sin push.'
    d.add_page_break()
    d.add_heading('Actualización y verificación del 4 de noviembre de 2026', level=1)
    d.add_paragraph('Esta actualización sustituye los estados iniciales de las capturas históricas. '
                    'Las evidencias del 3 de noviembre se mantienen fechadas para conservar el recorrido de la práctica.')
    d.add_heading('Resultados actuales', level=2)
    d.add_paragraph(f'{facts["tests"]} pruebas aprobadas en {facts["suites"]} suites. '
                    'Se validaron 15 pruebas nuevas de correcciones y telemetría. Compilación Android y APK de pruebas correctos.')
    d.add_paragraph(cierre)
    if aprobado:
        d.add_paragraph('El backend local no tenía categorías. Se creó la categoría identificada Pruebas Guia 13 '
                        'mediante la cuenta local de pruebas. Con ella se confirmó el alta; una petición con una categoría '
                        'inexistente devolvió 404. El producto de prueba quedó inactivo mediante la baja lógica, '
                        'y el pendiente anterior permanece conservado sin reasignación ni reenvío.')
    d.add_paragraph('La prueba de integración usa la sesión guardada o, con opción explícita, una cuenta local de pruebas. '
                    'Las credenciales temporales se eliminan antes de las solicitudes y no se exportan en los resultados. '
                    'Cuando se completa, crea un producto identificado y le aplica baja lógica; conserva los pendientes anteriores.')
    for name, caption in [('E13_pruebas_20261104.png', 'Reporte original Gradle del 4 de noviembre con 82 pruebas aprobadas.'),
                          ('E14_correcciones_20261104.png', 'Reporte local de instrumentación real y consulta de SQLite versión 4, generado antes de la confirmación posterior de recepción en consola.'),
                          ('E15_productos_20261104.png', 'Pantalla real Android de la versión corregida.'),
                          ('E16_categorias_20261104.png', 'Selector de categoría en el formulario de la aplicación.')]:
        path = caps / name
        if path.exists():
            title = {'E13_pruebas_20261104.png': 'Pruebas automatizadas actuales',
                     'E14_correcciones_20261104.png': 'Integración real y persistencia',
                     'E15_productos_20261104.png': 'Pantalla actual de Productos',
                     'E16_categorias_20261104.png': 'Selección de categoría'}[name]
            d.add_heading(title, level=2)
            p = d.add_paragraph()
            from PIL import Image
            if name == 'E15_productos_20261104.png':
                crop = root / 'tmp/s13_plantilla/productos_actual_detalle.png'
                with Image.open(path) as source: source.crop((30, 240, 1050, 2010)).save(crop)
                path = crop
            w, h = Image.open(path).size
            p.add_run().add_picture(str(path), **({'height': Inches(5.5)} if h > w else {'width': Inches(6.5)}))
            p.add_run('\nFigura 0. ' + caption)
            p.paragraph_format.keep_together = True
    d.add_heading('Comprobaciones que requieren intervención de la estudiante', level=2)
    d.add_paragraph('Firebase: abrir el proyecto en Crashlytics y comprobar la incidencia de operación prueba_sesion13 '
                    'después de reiniciar la app con Internet. Adjuntar la captura de recepción solo cuando aparezca realmente.')
    if not aprobado:
        d.add_paragraph('Android: iniciar sesión y repetir la prueba de integración del emulador. '
                        'No se acredita un alta remota exitosa de la versión actual hasta completar esa prueba.')
    d.add_paragraph('iOS: en macOS/Xcode, vincular FirebaseCore y FirebaseCrashlytics, incorporar la configuración real, '
                    'configurar símbolos y ejecutar compilación, permisos y persistencia de sesión. Windows no valida esa plataforma.')
    d.add_paragraph('Revisar los pendientes anteriores y confirmar su pertenencia antes de recuperarlos; '
                    'elegir una categoría válida y comprobar el servidor antes de reenviar. No se asignan ni eliminan automáticamente.')
    d.add_paragraph('Commit local de correcciones verificadas: 9ae4ab9. La documentación y sus capturas '
                    'se conservan en un commit editorial posterior, sin publicación en GitHub.')
    from firebase_cierre_word import aplicar as cerrar_firebase
    cerrar_firebase(d, ps, root)
