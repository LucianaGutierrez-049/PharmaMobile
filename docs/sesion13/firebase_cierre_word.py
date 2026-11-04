"""Cierre de recepción con evidencia aportada por la estudiante en Firebase."""
import json
from docx.shared import Inches


def aplicar(d, ps, root):
    caps = root / 'docs/sesion13/capturas'
    path = caps / 'firebase_recepcion_20261104.json'
    if not path.exists():
        return
    facts = json.loads(path.read_text(encoding='utf-8'))
    for name in facts['capturas']:
        if not (caps / name).exists():
            raise FileNotFoundError(name)
    d.tables[1].cell(0, 0).text = (
        'ESTADO AL 4 DE NOVIEMBRE DE 2026\n82 pruebas Android host aprobadas y compilación correcta. '
        'Categorías, separación por cuenta y flujo REST verificados. Recepción de la prueba no fatal '
        'confirmada en Firebase Crashlytics. iOS y los demás límites indicados en el informe siguen pendientes.')
    ps[87].text = (
        'Firebase Crashlytics está integrado mediante Telemetria en el dominio y TelemetriaFirebase en datos, '
        'registrado con Koin. La configuración Android real corresponde al proyecto pharmamobile-33661 y al '
        'paquete pe.edu.upeu.pharmamobilee. El archivo androidApp/google-services.json está excluido de Git. '
        'La recepción se confirmó en la consola el 4 de noviembre: error no fatal originado en '
        'TelemetriaPruebaCard y registrado por TelemetriaFirebase, con operacion=prueba_sesion13. '
        'El evento de la consola y sus claves se adjuntan al cierre. registrarEvento escribe logs de '
        'Crashlytics; la notificación local Android es una capacidad nativa independiente.')
    ps[110].text = ps[110].text.replace(
        'La configuración Firebase Android está incorporada; su recepción en consola y la ejecución iOS siguen pendientes.',
        'La prueba controlada no fatal Android ya fue recibida en Firebase; la ejecución iOS sigue pendiente.')
    ps[114].text = (
        '3. Las pruebas, la integración REST y la recepción de la prueba controlada en Crashlytics '
        'acreditan el alcance Android descrito. El cierre completo conserva pendientes explícitos: '
        'validación iOS, recorrido integral y ampliaciones offline indicadas en la lista de cotejo.')
    ps[117].text = (
        '2. Conservar las capturas del evento y sus claves como evidencia de recepción en Crashlytics. '
        'La acción de diagnóstico permanece restringida a depuración. Completar la configuración nativa '
        'y las pruebas iOS en macOS/Xcode.')
    ps[134].text = (
        'Anexo D Firebase\nContrato Telemetria y adaptador de datos con códigos técnicos seguros. '
        'Se verificó la recepción no fatal Android en consola: operacion=prueba_sesion13, '
        'pantalla=productos, tipo=prueba y codigo=inesperado. La pila identifica '
        'TelemetriaPruebaCard.kt:19 y TelemetriaFirebase.kt:49. Las capturas aportadas por '
        'la estudiante no contienen contraseñas ni tokens; esta evidencia no valida la plataforma iOS.')
    d.tables[11].cell(1, 2).text = (
        'Configuración Android real verificada en androidApp/google-services.json, excluida de Git. '
        'Proyecto pharmamobile-33661 y paquete coincidente. Recepción de prueba no fatal confirmada en consola.')
    d.tables[13].cell(10, 4).text = (
        'Código y configuración del 3 de noviembre. Recepción Android confirmada el 4 de noviembre; '
        'capturas E17 y E18 en el cierre Firebase.')
    d.tables[15].cell(12, 2).text = 'Sí Android'
    d.tables[15].cell(12, 3).text = (
        'SDK aislado en datos. Configuración y recepción no fatal Android confirmadas; '
        'evento y claves prueba_sesion13 visibles en la consola.')
    for paragraph in d.paragraphs:
        if paragraph.text.startswith('Firebase: abrir el proyecto en Crashlytics'):
            paragraph.text = (
                'Firebase Android: recepción ya confirmada mediante las capturas aportadas de la consola. '
                'El cierre siguiente muestra la pila y las claves del mismo evento controlado.')

    d.add_page_break()
    d.add_heading('Recepción confirmada en Firebase Crashlytics', level=1)
    d.add_paragraph(
        'El evento mostrado en la consola corresponde al 4 de noviembre de 2026 a las 8:37:04 a. m., '
        'versión 1.0 (1), en el emulador Android. La pila identifica la acción de diagnóstico '
        'y las claves confirman operacion=prueba_sesion13, pantalla=productos y tipo=prueba. '
        'codigo=inesperado clasifica esta excepción sintética controlada.')
    for name, caption in [
        (facts['capturas'][0], 'Consola Firebase: evento no fatal y pila desde TelemetriaPruebaCard hasta TelemetriaFirebase.'),
        (facts['capturas'][1], 'Claves del mismo evento: operacion=prueba_sesion13, pantalla=productos, tipo=prueba y codigo=inesperado.')
    ]:
        p = d.add_paragraph()
        p.add_run().add_picture(str(caps / name), width=Inches(6.25))
        p.add_run('\nFigura 0. ' + caption + ' Captura original aportada por la estudiante.')
        p.paragraph_format.keep_together = True
