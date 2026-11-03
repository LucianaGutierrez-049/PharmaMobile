from pathlib import Path
import json,subprocess
from docx.shared import Inches

def aplicar(d,ps,root):
 base=root/'docs/sesion13'; caps=base/'capturas'
 manifest=base/'capturas/evidencias_actuales.json'
 native=json.loads(manifest.read_text(encoding='utf-8')) if manifest.exists() else {}
 def text(i,value): ps[i].text=value
 def fig(i,name,caption,n):
  path=caps/name
  if not path.exists(): return False
  from PIL import Image
  crops={'E07_concedido.png':(40,880,1040,1545),'E08_denegado.png':(40,880,1040,1545),'E09_definitivo.png':(40,880,1040,1545),'E05_fallida.png':(40,715,1040,1420),'E06_inicio.png':(40,820,1040,1320),'E04_offline.png':(0,0,1080,1750),'E07_notificacion.png':(30,450,1045,785)}
  with Image.open(path) as im:
   if name in crops:
    detalle=root/'tmp/s13_plantilla'/('detalle_final_'+name); im.crop(crops[name]).save(detalle); path=detalle
   w,h=Image.open(path).size
  text(i,''); run=ps[i].add_run()
  if h>w: run.add_picture(str(path),height=Inches(5.6))
  else: run.add_picture(str(path),width=Inches(6.5))
  ps[i].add_run('\nFigura '+str(n)+'. '+caption+(' Detalle recortado; original conservado.' if name in crops else ''))
  ps[i].paragraph_format.keep_together=True
  return True
 figures=[(57,'E02_migracion.png','Captura del visor local del código real: migración 2.sqm y esquema Producto.sq, con hash del archivo.',2),
 (67,'E03_sincronizacion.png','Captura del código real de política y repositorio de sincronización en el visor local.',3),
 (85,'E09_manifest.png','Contrato común de permisos y declaración real de POST_NOTIFICATIONS en el manifiesto.',9),
 (90,'E10_firebase.png','Interfaz de dominio y adaptador GitLive Crashlytics, capturados desde el visor local del código.',10),
 (99,'E11_pruebas.png','Reporte HTML original de Gradle: 67 pruebas y 100 % satisfactorias. No es una consola simulada.',11),
 (106,'E12_historial.png','Historial real de Git y estado del árbol de trabajo capturados desde el visor local.',12)]
 for args in figures: fig(*args)
 fig(58,'E02_base_local.png','Consulta real de la copia SQLite Android: esquema versión 3 y fila de prueba pendiente con id negativo.',13)
 text(51,'Se verificó recuperación de sesión y lectura REST real mediante túnel ADB. El producto de prueba recibió 404 al crear y permaneció local con motivo e intentos. Tras cerrar la app y reabrir sin red ni túnel, se conservaron sesión y producto. El primer POST todavía usó una conexión persistente: no se atribuye su 404 al modo avión. El aislamiento por usuario y los borradores siguen pendientes.')
 mappings={'inicio':(74,6,'Inicio con antigüedad y estado de sincronización'),
 'pendientes':(72,4,'Producto pendiente conservado después de cerrar y reabrir sin servicio ni túnel ADB; aparece como una fila que requiere revisión'),
 'fallidas':(73,5,'Fila que requiere revisión y acción de reenvío'),
 'concedido':(82,7,'Notificaciones con permiso concedido'),
 'denegado':(83,8,'Notificaciones con permiso denegado'),
 'definitivo':(84,9,'Denegación permanente y acceso a Ajustes')}
 for key,(i,n,caption) in mappings.items():
  if key in native: fig(i,native[key],caption+'. Captura real Android.',n)
 if 'fallidas' in native:
  d.tables[15].cell(5,2).text='Sí Android'; d.tables[15].cell(5,3).text='Fila, intentos, motivo y revisión capturados.'
 text(110,'Se amplió la base local y se validó su versión 3 en Android; la app recuperó sesión y descargó datos reales. Se capturaron los tres estados del permiso, revisión de un envío fallido y conservación de un producto al reiniciar sin servicio. El POST de prueba devolvió 404: se documenta esa limitación y no se declara un alta remota exitosa. Las 67 pruebas automatizadas previas aprobaron y el ajuste posterior de conexión compiló y se validó en ejecución. Firebase real e iOS siguen pendientes.')
 text(116,'1. Investigar el 404 del alta de productos y verificar sus referencias, como la categoría utilizada por defecto. Después confirmar un alta y conciliación correctas; mantener el registro de prueba identificado.')
 text(118,'3. Aislar la caché por usuario, conservar borradores al expirar la sesión y ampliar las operaciones offline. Los commits por paso ya están realizados localmente; revisar el historial antes de solicitar push.')
 d.tables[3].cell(4,2).text='El producto local se conserva al cerrar y reabrir sin servicio; copia SQLite con id -1.'
 d.tables[3].cell(4,3).text='La creación previa recibió 404; no se atribuye ese error a ausencia de red.'
 d.tables[3].cell(4,5).text='Conservación offline verificada; revisar el alta remota.'
 d.tables[3].cell(5,2).text='Fila en revisión con motivo e intento 1/5; confirmación de reenvío capturada.'
 d.tables[3].cell(5,3).text='Alta remota exitosa todavía no acreditada por el 404 observado.'
 text(103,'Se creó feature/integrador-13 conservando la Unidad 2 ya implementada. Su punto inicial fue 47a4351, descendiente de develop con 39 commits de avance. Se efectuaron commits separados por cada paso y un ajuste adicional para el acceso al backend mediante túnel ADB de depuración. No se reescribió el historial previo ni se incluyeron los cambios de autenticación que ya estaban sin confirmar.')
 text(105,'Rama utilizada: feature/integrador-13. Ancestro develop: eaa16c2. Base funcional previa: 47a4351. Remoto: https://github.com/LucianaGutierrez-049/PharmaMobile.git. Los commits son locales; no se solicitó push.')
 commits=json.loads((base/'commits_sesion13.json').read_text(encoding='utf-8'))
 for entry in commits:
  step=entry.get('paso')
  if isinstance(step,int) and 1<=step<=7:
   cells=d.tables[14].rows[step].cells
   cells[2].text=entry['mensaje']; cells[3].text=entry['hash'][:8]; cells[4].text='Local; push no solicitado'
 text(107,'El árbol conserva cambios previos del usuario en autenticación y otros entregables. No se declara un estado global limpio, porque esos cambios se excluyeron deliberadamente de los commits de esta sesión. Los hashes de la tabla corresponden al historial real; el cierre editorial del Word puede constituir un commit adicional.')
 d.tables[15].cell(14,2).text='Sí local'
 d.tables[15].cell(14,3).text='Commits por paso en feature/integrador-13; sin push solicitado.'
 for r,name in [(2,'E02_migracion.png'),(3,'E03_sincronizacion.png'),(10,'E10_firebase.png'),(11,'E11_pruebas.png'),(12,'E12_historial.png')]:
  if (caps/name).exists():
   d.tables[13].cell(r,2).text=name; d.tables[13].cell(r,4).text='OK captura técnica real; ver alcance en pie de figura.'
 for key,row in [('inicio',6),('pendientes',4),('fallidas',5),('concedido',7),('denegado',8),('definitivo',9)]:
  if key in native:
   d.tables[13].cell(row,2).text=native[key]; d.tables[13].cell(row,4).text='OK captura Android actual.'
 if 'inicio' in native:
  d.tables[15].cell(7,2).text='Sí Android'; d.tables[15].cell(7,3).text='Captura actual de Inicio y estado observable.'
 if all(k in native for k in ('concedido','denegado','definitivo')):
  d.tables[15].cell(9,2).text='Sí Android'; d.tables[15].cell(9,3).text='Tres estados capturados en el emulador.'
  d.tables[15].cell(11,2).text='Sí Android'; d.tables[15].cell(11,3).text='Explicación previa y acceso a Ajustes capturados.'
  for i in [82,83,84]: ps[i].paragraph_format.keep_together=True
  for row in [4,5,6]:
   d.tables[12].cell(row,3).text='Aprobado en lógica y observado en Android; ver captura del estado correspondiente.'
 # La captura histórica ya no se usa como evidencia de la versión actual.
 text(135,'Anexo E Reportes y evidencias actuales\nCapturas originales PNG en docs/sesion13/capturas. Reporte original Gradle y XML en shared/build. Las figuras técnicas provienen de un visor local con los archivos reales; las figuras funcionales provienen del emulador Android. La validación con Firebase real y la plataforma iOS siguen pendientes.')
 fig(135,'E07_notificacion.png','Notificación local de prueba visible en el panel nativo Android. No es un mensaje push de Firebase.',15)
 ps[135].insert_paragraph_before('Anexo E Reportes y evidencias actuales',style='List Bullet')
 ps[135].style='Normal'
 text(136,'Anexo F Historial de la rama integradora\nRepositorio: https://github.com/LucianaGutierrez-049/PharmaMobile\nRama: feature/integrador-13. Hashes reales por paso en la sección 11. El ajuste 72acc38 habilita opcionalmente localhost:8080 mediante ADB solo en builds de depuración; la configuración por defecto sigue siendo 10.0.2.2.')
