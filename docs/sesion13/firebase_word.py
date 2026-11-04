import json
from docx.shared import Inches

def aplicar(d,ps,root):
 path=root/'docs/sesion13/capturas/firebase_validacion.json'
 if not path.exists(): return
 facts=json.loads(path.read_text(encoding='utf-8'))
 build=facts.get('compilacion','Pendiente de verificación')
 project=facts['proyecto']; package=facts['paquete']
 ps[87].text=('Firebase Crashlytics está integrado mediante Telemetria en el dominio y TelemetriaFirebase en datos, registrado con Koin. '
  f'La configuración Android real ya está incorporada: proyecto {project}, paquete {package}. '
  'El archivo se encuentra en androidApp/google-services.json y coincide con el applicationId. '
  'Esta comprobación acredita la configuración local; la recepción de un error en la consola Crashlytics sigue pendiente. '
  'registrarEvento escribe logs de Crashlytics. La notificación local Android no demuestra mensajería push ni recepción en Firebase.')
 d.tables[11].cell(1,2).text=f'Configuración real verificada en androidApp/google-services.json. Proyecto {project}; paquete {package}. Archivo excluido de Git.'
 ps[91].text=''
 ps[91].add_run().add_picture(str(root/'docs/sesion13/capturas/E10_configuracion_android.png'),width=Inches(6.5))
 ps[91].add_run('\nFigura 1. Captura aportada por la estudiante: google-services.json ubicado dentro del módulo androidApp. Se conserva solo el panel del proyecto para omitir valores de configuración y claves.')
 ps[91].add_run(f'\nVerificación de contenido: proyecto {project}; paquete {package}. Compilación con configuración real: {build}. '
  'Las dependencias y los complementos ya estaban declarados; al existir el archivo se aplican Google Services y Crashlytics. '
  'No se duplicó la configuración de Gradle mostrada por el asistente de Firebase.')
 ps[91].paragraph_format.keep_together=True
 ps[117].text='2. Enviar un error de prueba, reiniciar la app si corresponde y verificar su recepción en la consola Crashlytics. Capturar el resultado real y completar la configuración y pruebas iOS en macOS/Xcode.'
 ps[110].text=ps[110].text.replace('Firebase real e iOS siguen pendientes.', 'La configuración Firebase Android real ya está incorporada; quedan pendientes la recepción del error en Crashlytics y la validación iOS.')
 d.tables[15].cell(12,3).text='SDK aislado en datos y configuración Android real verificada. Recepción en Crashlytics aún no acreditada.'
 d.tables[13].cell(10,2).text='E10_firebase.png y E10_configuracion_android.png'
 d.tables[13].cell(10,4).text='Código y configuración Android verificados; falta evidencia de recepción en consola.'
 d.tables[1].cell(0,0).text='ESTADO DE VALIDACIÓN\n67 pruebas automatizadas aprobadas y APK Android compilado. Configuración Firebase Android real incorporada. Quedan pendientes recepción en Crashlytics, validación iOS y cierre del alta remota que respondió 404.'
 ps[134].text=('Anexo D Firebase\nContrato Telemetria y adaptador de datos; configuración Android real verificada con paquete coincidente. '
  f'Compilación con esa configuración: {build}. No se adjuntan el archivo JSON completo, claves o credenciales. La recepción en consola y la integración nativa iOS requieren evidencia adicional.')
 ps[97].text += f' Verificación posterior tras incorporar google-services.json: {build}. Esta compilación no sustituye la comprobación de recepción de eventos en Firebase.'
