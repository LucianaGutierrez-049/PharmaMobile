from pathlib import Path
from copy import deepcopy
import hashlib, json, subprocess, zipfile
from docx import Document
from docx.shared import Inches, Pt
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from PIL import Image
import re

ROOT=Path(__file__).resolve().parents[2]
SRC=Path(r'C:\Users\lucia\Downloads\Estructura_Informe_Guia_Practica_13_PharmaMobil.docx')
OUT=ROOT/'output/docx/Informe_Guia_Practica_13_Luciana_Gutierrez.docx'
TMP=ROOT/'tmp/s13_plantilla'; TMP.mkdir(exist_ok=True)
d=Document(SRC); ps=list(d.paragraphs)
def text(i,value): ps[i].text=value
def cell(t,r,c,value): d.tables[t].cell(r,c).text=value
def row(t,values):
 table=d.tables[t]; new=deepcopy(table.rows[-1]._tr); table._tbl.append(new)
 for c,v in zip(table.rows[-1].cells,values): c.text=v
def block(i,title,value):
 text(i,title+'\n'+value)
 for r in ps[i].runs: r.font.size=Pt(9)
def image(i,name,caption):
 path=ROOT/'docs/sesion13/capturas'/name
 cropped=TMP/('detalle_'+name)
 with Image.open(path) as original: original.crop((55,530,1025,1870)).save(cropped)
 text(i,'')
 ps[i].add_run().add_picture(str(cropped),height=Inches(4.7))
 ps[i].add_run('\n'+caption+' Recorte central para facilitar la lectura; la captura original se conserva en los anexos digitales.')
 ps[i].paragraph_format.keep_together=True

sha=hashlib.sha256(SRC.read_bytes()).hexdigest()
contract=f'''# Contrato de edición de la plantilla
Referencia: {SRC}
SHA256: {sha}
Una sección Letter vertical, márgenes 0.7875 pulgadas superior e inferior y 0.86597 laterales.
Se conserva la plantilla como documento base, estilos Aptos 10 pt y títulos Aptos Display 16 y 12 pt, orden de 14 secciones, portada, tablas, listas y numeración.
Slots: word/document.xml, párrafos 7 a 15 son instrucciones convertidas en alcance; párrafos con indicaciones y corchetes son editables; todas las celdas de resultado de las 16 tablas son editables; el campo estudiante se completa con el nombre proporcionado.
Las capturas se insertan únicamente en los espacios correspondientes. Los espacios sin una captura verificada se completan con un estado explícito y una explicación. Se permiten filas clonadas en tablas para los campos locales adicionales.
Preservar estilos, numbering, tema, cabeceras, pies y geometría. Puede actualizarse document.xml y relaciones de imágenes. La referencia permanece intacta.
Validación: exportación PDF mediante Word, inspección de todas las páginas, ausencia de marcadores de plantilla y comparación de partes preservadas.
'''
(TMP/'artifact.md').write_text(contract,encoding='utf-8')
with zipfile.ZipFile(SRC) as z:
 (TMP/'inventario_referencia.json').write_text(json.dumps({n:hashlib.sha256(z.read(n)).hexdigest() for n in z.namelist()},indent=2),encoding='utf-8')

cell(0,4,1,'Luciana Cristina Gutierrez Quispe')
text(7,'Informe completado con código, resultados de pruebas y evidencias reales del proyecto.')
text(9,'Alcance y criterios de la evidencia')
for i,v in {11:'Se conserva el orden de las catorce secciones y los siete pasos de la guía.',12:'Las figuras identifican el estado observado y su alcance de validación.',13:'Las evidencias omiten contraseñas visibles, tokens JWT y claves privadas.',14:'Los requisitos no verificados en ejecución se señalan como pendientes.',15:'El control de versiones utiliza la rama y el historial reales del repositorio.'}.items(): text(i,v)
cell(1,0,0,'ESTADO DE VALIDACIÓN\n67 pruebas automatizadas aprobadas y APK Android compilado. La verificación con Firebase real, iOS y las capturas de escenarios pendientes aún requiere cierre. El estado de cada requisito se detalla en las secciones 9, 10 y 13.')
text(45,'PharmaMobil es una aplicación Kotlin Multiplatform con inventario de productos y navegación por Inicio, Productos, Clientes y Pedidos. El código compartido separa presentación, dominio y datos. Ktor consume PharmaSoft, SQLDelight conserva el inventario y Koin registra las implementaciones específicas de Android e iOS. Antes de esta sesión existían repositorios REST, autenticación, persistencia local y envío básico de creaciones pendientes; faltaba controlar intentos, registrar errores por fila, resolver permisos e integrar telemetría.')
block(47,'Estructura real del código','shared/src/commonMain/kotlin/.../{presentation,domain,data,di}\nshared/src/commonMain/sqldelight/.../db/{Producto.sq,1.sqm,2.sqm}\nshared/src/androidMain/kotlin/.../{data/permiso,di}\nshared/src/iosMain/kotlin/.../{data/permiso,di}\nandroidApp/src/main/{AndroidManifest.xml,kotlin/.../MainActivity.kt}\nNo se adjunta una captura de Android Studio: esta es una relación de rutas verificadas.')
initial=['Implementado; repositorios y cliente Ktor previos.','Implementado; DTO en data/remote/dto y modelos en domain/model.','Código y pruebas existentes; el acceso real depende de la API ejecutada.','Implementado; producto local y metadatos de sincronización.','Básica: registrar pendientes, subir y después descargar.','Implementado; módulos comunes y platformModule.']
for r,v in enumerate(initial,1): cell(2,r,2,v)
text(49,'Se revisó el flujo de acceso y el código de los ocho momentos propuestos por la guía. La tabla distingue una observación en el emulador de un resultado automatizado o una verificación pendiente. La presencia de una pantalla en el código no equivale a una prueba funcional completa.')
brechas=[
['Formulario de acceso visible en el emulador.','La sesión guardada no se recuperó en la primera ejecución.','Media','Revalidar con la API actual.'],
['Mensajes tipados cubiertos por AuthRepositorioRestTest.','Falta captura de credencial incorrecta en esta sesión.','Media','Pendiente de evidencia manual.'],
['Existe estado SinProductos en la presentación.','Falta reproducir base vacía en el emulador actual.','Baja','Pendiente de captura.'],
['Prueba registroSinRedQuedaPendienteYElFlowLoEmite aprobada.','Falta captura del producto creado sin red.','Alta','Código y test cerrados; evidencia pendiente.'],
['Prueba envioAlReconectarQuitaMarcaPendiente aprobada.','Creaciones ambiguas no deben reenviarse solas.','Alta','Confirmación explícita y revisión del servidor.'],
['Prueba de fallo permanente aprobada.','Falta captura de validación real por campo.','Media','Mantener motivo local; completar recorrido.'],
['El manejo de sesión expirada está implementado.','El formulario se descarta al cambiar la sesión.','Alta','Sesión 14: borrador asociado al usuario.'],
['El repositorio borra token y limpia caché Bearer.','La base local no está particionada por usuario.','Alta','Sesión 14: aislar datos sin perder pendientes.']]
for r,vs in enumerate(brechas,1):
 for c,v in enumerate(vs,2): cell(3,r,c,v)
text(51,'Las brechas prioritarias son la repetición de creaciones ambiguas y el aislamiento de datos entre usuarios. La primera se mitiga bloqueando el reenvío automático y solicitando confirmación; la segunda permanece pendiente. La autenticación del servicio local cambió durante la revisión: inicialmente el login devolvió 404 y una comprobación posterior ya encontró las rutas auth/login y auth/me. El error anterior no se presenta como diagnóstico vigente.')
image(52,'01_estado_inicial.png','Figura 1. Primera ejecución registrada: formulario de acceso y aviso de sesión guardada no recuperada. Evidencia del recorrido inicial; no acredita un login correcto.')
text(54,'La tabla producto incorpora intentos y ultimoError, además de requiereRevision y proximoIntento. La migración 2.sqm corresponde al avance del esquema 2 al 3. Una prueba crea una base anterior, aplica la migración y comprueba que conserva la fila y los valores iniciales. La actualización real de un dispositivo con datos previos sigue pendiente de evidencia.')
cell(4,1,2,'Contabiliza fallos de envío registrados; el éxito confirma y retira la fila pendiente.')
row(4,['requiereRevision','INTEGER NOT NULL DEFAULT 0','Bloquea creaciones ambiguas o errores que necesitan intervención.'])
row(4,['proximoIntento','INTEGER NOT NULL DEFAULT 0','Conserva el instante mínimo para volver a enviar.'])
locations=['No existe con ese nombre. listarPendientes en Producto.sq y PoliticaSincronizacion.puedeEnviar filtran las filas elegibles.','No hay consulta SQL específica: ProductoViewModel cuenta las filas observadas que no requieren revisión y tienen menos de cinco fallos.','ProductoViewModel cuenta las pendientes con requiereRevision o intentos >= 5.','Equivalente real registrarFallo, en Producto.sq y ProductoLocal: suma intentos y guarda el motivo.']
for r,v in enumerate(locations,1): cell(5,r,2,v)
block(57,'Evidencia de código de la migración 2.sqm',(ROOT/'shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobilee/db/2.sqm').read_text(encoding='utf-8'))
text(58,'Verificación de base local: la prueba migracionConservaFilasExistentesYAgregaEstadoDeEnvio aprobó la conservación de datos y los valores iniciales. No se dispone de una captura de Database Inspector; no se sustituye por una imagen simulada.')
text(60,'La lógica está implementada en ProductoRepositorioOffline y PoliticaSincronizacion. Se utilizan ErrorApi y ErrorApiException del dominio. No se añadió la clase ResultadoEnvio ni un SincronizarUseCase separado como en el ejemplo de la guía; la documentación registra los nombres reales. Sin conexión, timeout y servidor son transitorios; validación, conflicto, inexistencia o falta de permiso requieren intervención.')
cell(6,2,2,'Guardar intento y motivo. GET reintenta automáticamente; una creación POST ambigua exige confirmación manual y respeta la espera.')
text(63,'MAX_INTENTOS es 5. esperaMillis calcula 1 000 ms desplazados según el contador y limita el exponente a 4. Para cinco intentos GET se ejecutan cuatro pausas: 1, 2, 4 y 8 s. La función puede devolver hasta 16 s. Los POST ambiguos no se repiten automáticamente.')
for r,v in enumerate(['5 por ciclo; un reenvío confirmado tras agotarlo inicia otro ciclo.','1, 2, 4, 8 y hasta 16 s; GET usa cuatro pausas entre cinco intentos.','GET con límite; POST ambiguo queda en revisión para confirmación.','Conservar fila, motivo y marca requiereRevision; no reintentar automáticamente.'],1): cell(7,r,2,v)
text(66,'El repositorio serializa la sincronización mediante Mutex. Primero recorre los pendientes elegibles; guarda una marca de revisión antes del POST. Al confirmar éxito, registra el producto remoto y elimina el pendiente dentro de una transacción. Luego descarga y concilia, preservando los pendientes locales. Si se interrumpe el proceso, la marca impide repetir automáticamente una creación cuyo resultado se desconoce. No se ha acreditado soporte de claves de idempotencia en el backend.')
block(67,'Secuencia real y política de sincronización','local.pendientes().filter { PoliticaSincronizacion.puedeEnviar(it, ahoraMillis()) }\nlocal.prepararEnvio(pendiente.id)\nremoto.registrar(pendiente)\nlocal.confirmarEnvio(pendiente.id, productoRemoto)\n// Después de recorrer los envíos:\ndescargarConReintentos()\nlocal.reemplazarSincronizados(productosRemotos)\nFuente: data/repository/ProductoRepositorioOffline.kt. Fragmento abreviado; no es captura de ejecución.')
text(69,'ProductoViewModel observa ProductoRepository.observar(), cuyo origen es SQLDelight. Calcula pendientes y filas en revisión con cada emisión. La fecha proviene de observarUltimaSincronizacion. Inicio y Productos muestran esos valores y acciones de revisión. El aviso de error procede del resultado de sincronizar; no se implementó un monitor continuo de conectividad.')
for r,v in enumerate(['Campo real sincronizando: indicador de progreso y bloqueo temporal del botón.','Cuenta filas pendientes sin revisión y con intentos < 5.','Cuenta pendientes en revisión o con intentos >= 5; permite revisar datos y confirmar reenvío.','Campo real ultimaSincronizacionMillis; se muestra como antigüedad relativa de la última descarga correcta.'],1): cell(8,r,2,v)
text(71,'Los mensajes implementados incluyen «cambios por enviar», «requieren revisión», «Nunca sincronizado» y «Actualizado hace…». Una fila fallida conserva el motivo. No se acredita una detección de red en tiempo real: un error de autenticación o del servidor no debe interpretarse automáticamente como falta de conexión.')
for i,desc in [(72,'Pendientes visibles'),(73,'Filas fallidas y acciones'),(74,'Antigüedad de la sincronización')]: text(i,'Evidencia funcional pendiente: '+desc+'. El componente está implementado y se cubren partes del flujo mediante pruebas; falta una captura actual verificable de este escenario.')
text(76,'El contrato común GestorPermisos declara el permiso utilizado por esta sesión y sus operaciones. GestorPermisosAndroid usa Context y una referencia a ComponentActivity conectada desde MainActivity. La implementación iOS utiliza UserNotifications y requiere validación en macOS.')
cell(9,1,1,'NOTIFICACIONES, único valor implementado en este proyecto.')
cell(9,3,1,'estado(), solicitar(), abrirAjustes() y mostrarNotificacionPrueba()')
text(81,'GestorPermisosAndroid registra ActivityResultContracts.RequestPermission al conectar la Activity y espera el resultado mediante CompletableDeferred. SharedPreferences recuerda si POST_NOTIFICATIONS ya se pidió; shouldShowRequestPermissionRationale distingue una nueva denegación de un bloqueo definitivo. Se desconecta la Activity al destruirla. No existe una clase PuentePermisos separada: ese puente está integrado en el gestor. AndroidManifest.xml declara INTERNET y POST_NOTIFICATIONS. Para las notificaciones de iOS no aplica una clave descriptiva de cámara o ubicación en Info.plist.')
for i,state in [(82,'Concedido y notificación local'),(83,'Denegado'),(84,'DenegadoParaSiempre y acceso a Ajustes')]: text(i,'Captura pendiente del estado '+state+'. La lógica se verifica en GestorPermisosTest; una prueba simulada no acredita la aparición del diálogo nativo.')
block(85,'Declaraciones Android verificadas','<uses-permission android:name="android.permission.INTERNET" />\n<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />\nFuente: androidApp/src/main/AndroidManifest.xml.')
text(87,'Telemetria define registrarError y registrarEvento en el dominio. TelemetriaFirebase adapta ese contrato a GitLive Crashlytics y se registra como single<Telemetria> en Koin. La configuración externa no está finalizada: falta el archivo real de Firebase y la recepción verificada en consola. registrarEvento utiliza logs de Crashlytics, no Google Analytics. La notificación de prueba es local, no mensajería push.')
firebase=['Pendiente de cuenta/proyecto real y androidApp/google-services.json. No se generaron credenciales ficticias.','shared/build.gradle.kts: dev.gitlive:firebase-crashlytics:2.7.0. Raíz: Google Services 4.4.4 y Crashlytics 3.0.8. Aplicación condicional al existir configuración.','commonMain/.../domain/port/Telemetria.kt: registrarError y registrarEvento.','commonMain/.../data/firebase/TelemetriaFirebase.kt: acceso al SDK y filtrado de contexto técnico.','commonMain/.../di/AppModule.kt: single<Telemetria> { TelemetriaFirebase() }.']
for r,v in enumerate(firebase,1): cell(11,r,2,v)
block(90,'Interfaces y adaptación verificadas','domain/port/Telemetria.kt → interface Telemetria\ndata/firebase/TelemetriaFirebase.kt → class TelemetriaFirebase : Telemetria\nEl SDK dev.gitlive.firebase.crashlytics se importa únicamente en el adaptador de datos.')
block(91,'Configuración e inyección verificadas','implementation("dev.gitlive:firebase-crashlytics:2.7.0")\nsingle<Telemetria> { TelemetriaFirebase() }\nSin google-services.json real no se acredita envío a Firebase. La configuración iOS y su carga de símbolos también siguen pendientes.')
text(93,'commonTest usa SQLite JDBC en memoria y MockEngine para los escenarios del repositorio. GestorPermisosTest comprueba la política de estados y un gestor falso. Estas pruebas se ejecutaron mediante el target Android host; no constituyen pruebas nativas iOS ni sustituyen capturas del dispositivo.')
results=[['Aprobado: conserva fila, suma intento y bloquea reenvío ambiguo.','ProductoRepositorioOfflineTest, falloTransitorioRecuerdaIntentoYNoRepiteCreacionAmbigua'],['Aprobado: queda en revisión y permite procesar otras filas.','ProductoRepositorioOfflineTest, falloPermanenteNoSeReintentaYNoImpideEnviarOtrasFilas'],['Aprobado: confirma producto remoto y retira pendiente.','ProductoRepositorioOfflineTest, envioAlReconectarQuitaMarcaPendiente'],['Aprobado en lógica de estados. Captura nativa pendiente.','GestorPermisosTest, distinguePrimeraSolicitudDeBloqueoDefinitivo'],['Aprobado en lógica de estados. Captura nativa pendiente.','GestorPermisosTest, distinguePrimeraSolicitudDeBloqueoDefinitivo'],['Aprobado el estado lógico; acción Ajustes implementada, ejecución pendiente.','GestorPermisosTest y PermisoNotificacionesCard.kt']]
for r,vs in enumerate(results,1):
 for c,v in enumerate(vs,3): cell(12,r,c,v)
text(96,'Comando real: .\\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug --console=plain --no-daemon --max-workers=1 -Dorg.gradle.jvmargs="-Xmx1536m -XX:ActiveProcessorCount=2 -Dfile.encoding=UTF-8" -Pkotlin.compiler.execution.strategy=in-process. Tras completar las pruebas se repitió :androidApp:assembleDebug de forma independiente para liberar memoria.')
text(97,'Resultado: :shared:testAndroidHostTest completó las 67 pruebas; :androidApp:assembleDebug terminó con BUILD SUCCESSFUL. El primer intento agotó memoria de Java; la ejecución con memoria limitada y el empaquetado independiente concluyeron correctamente.')
text(98,'Total: 67 pruebas. Correctas: 67. Fallidas: 0. Errores: 0. Omitidas: 0. Suites: 18. Incluye 12 pruebas del repositorio offline, 2 de permisos y 2 de política de reintentos.')
text(99,'Evidencia verificable: shared/build/test-results/testAndroidHostTest/TEST-*.xml y shared/build/reports/tests/testAndroidHostTest/index.html. No se adjunta una captura de terminal simulada. APK generado: androidApp/build/outputs/apk/debug/androidApp-debug.apk, build de depuración del 3 de noviembre de 2026.')
for r in range(1,13):
 cell(13,r,2,'No disponible como captura actual')
 cell(13,r,4,'Pendiente de captura')
cell(13,1,2,'01_estado_inicial.png'); cell(13,1,4,'OK: observación inicial, no recorrido completo')
for r,desc in [(2,'Código 2.sqm y prueba de migración'),(3,'Repositorio y política verificados'),(10,'Código y Koin verificados; servicio pendiente'),(11,'67 pruebas acreditadas mediante XML')]: cell(13,r,4,desc+'; captura pendiente')
text(103,'La guía solicita feature/integrador-13 desde develop y un commit por paso. El estado real del trabajo consultado corresponde a otra rama; los cambios de esta sesión siguen sin commits propios. La tabla registra esa diferencia sin asignar a la sesión 13 commits anteriores.')
text(105,'Rama utilizada: feature/offline-first-Gutierrez. Rama base solicitada: develop; no se acredita la creación de una rama integrador-13 desde esa base. Remoto: https://github.com/LucianaGutierrez-049/PharmaMobile.git.')
for r in range(1,8):
 cell(14,r,2,'Pendiente de commit propio de sesión 13'); cell(14,r,3,'No asignado'); cell(14,r,4,'No acreditado')
history=subprocess.check_output(['git','log','-6','--format=%h %s'],cwd=ROOT,text=True,encoding='utf-8')
block(106,'Historial real de referencia anterior a los commits de sesión 13',history)
text(107,'Estado final revisado: existen archivos modificados y archivos nuevos sin seguimiento. Incluye trabajo previo de autenticación y documentación, además de los cambios de sesión 13. No se realizó commit ni push de esos cambios como parte de la elaboración del informe.')
text(110,'Se ampliaron los metadatos locales; se serializó la sincronización; se controlaron fallos y reenvíos; se añadió estado observable en Inicio y Productos; se implementó el permiso de notificaciones y su prueba local; se creó el adaptador de Crashlytics. La evidencia automatizada suma 67 pruebas aprobadas y la aplicación Android compila. La activación Firebase, el recorrido completo y las evidencias de plataforma no están cerrados.')
text(112,'1. La memoria del envío y la marca anterior al POST permiten conservar la fila y bloquear repeticiones ambiguas; las pruebas cubren ese comportamiento.')
text(113,'2. La separación de contratos del dominio y adaptadores permite probar permisos y sincronización sin depender del proveedor o del dispositivo.')
text(114,'3. La compilación y las pruebas demuestran propiedades concretas del código. El cumplimiento completo exige además evidencias actuales del backend, de los tres estados del permiso y de Firebase e iOS.')
text(116,'1. Completar el recorrido con PharmaSoft actual y recoger capturas de pendientes, fallos, última sincronización y los tres estados del permiso.')
text(117,'2. Incorporar la configuración real Firebase, comprobar recepción sin datos sensibles y validar iOS desde macOS/Xcode.')
text(118,'3. Aislar la caché por usuario, conservar borradores al expirar la sesión, ampliar las operaciones offline y cerrar commits por paso antes de la entrega definitiva.')
checks=[('Parcial','Recorrido inicial observado y tabla de brechas; falta el flujo completo.'),('Sí','Producto.sq, 2.sqm y prueba de migración.'),('Sí','Clasificación de ErrorApi en ProductoRepositorioOffline.'),('Sí','PoliticaSincronizacion y pruebas específicas.'),('Parcial','Presentación implementada; falta captura funcional actual.'),('Sí','Orden en repositorio y pruebas con red simulada.'),('Parcial','Estado observable implementado; evidencia actual pendiente.'),('Sí','domain/permiso/GestorPermisos.kt.'),('Parcial','Tres estados probados en lógica; faltan los tres en ejecución.'),('Sí Android','POST_NOTIFICATIONS en manifiesto. iOS aún no validado.'),('Parcial','Explicación y Ajustes implementados; captura pendiente.'),('Sí código','SDK en datos; recepción Firebase no acreditada.'),('Sí Android host','67 pruebas aprobadas; no es ejecución nativa iOS.'),('No','Sin commits nuevos por paso ni push acreditado.')]
for r,(state,ev) in enumerate(checks,1): cell(15,r,2,state); cell(15,r,3,ev)
annex=[('Anexo A Esquema y migración','Producto.sq define producto y sincronizacion_metadata. La migración 2.sqm agrega intentos, ultimoError, requiereRevision y proximoIntento. La versión anterior se conserva mediante migración, no reinstalación obligatoria.'),('Anexo B Sincronización','ProductoRepositorioOffline aplica Mutex, marca previa al POST, confirmación transaccional y descarga posterior. PoliticaSincronizacion define MAX_INTENTOS y esperaMillis. La guía usa otros nombres de ejemplo; el informe identifica las clases reales.'),('Anexo C Permisos','GestorPermisos es el contrato común; GestorPermisosAndroid implementa el puente con Activity dentro de la propia clase. GestorPermisosIos usa UserNotifications. Solo se implementó NOTIFICACIONES.'),('Anexo D Firebase','Telemetria, TelemetriaFirebase, shared/build.gradle.kts, AppModule.kt y aplicación condicional de los complementos Android. No se adjuntan claves ni un archivo de configuración ficticio.'),('Anexo E Pruebas y capturas','Reportes XML y HTML en shared/build. Imágenes originales en docs/sesion13/capturas. El archivo verificacion_api.json registra que, en una consulta posterior al primer fallo, ya se publicaban auth/login y auth/me.'),('Anexo F Repositorio','https://github.com/LucianaGutierrez-049/PharmaMobile\nRama revisada: feature/offline-first-Gutierrez. Historial de referencia en la sección 11; commits propios de sesión 13 pendientes.')]
for i,(title,body) in enumerate(annex,131): text(i,title+'\n'+body)
with Image.open(ROOT/'docs/sesion13/capturas/02_despues_login.png') as original: original.crop((55,530,1025,1870)).save(TMP/'detalle_login.png')
ps[135].add_run('\n').add_picture(str(TMP/'detalle_login.png'),height=Inches(4.2))
ps[135].add_run('\nFigura 2. Captura histórica del primer intento de acceso: mensaje genérico de error. En aquel momento el login respondió 404; una comprobación posterior encontró publicadas las rutas de autenticación. Esta imagen no acredita una falla vigente de la API ni un login exitoso.')
ps[135].paragraph_format.keep_together=True
from evidencias_word import aplicar
aplicar(d,ps,ROOT)
from firebase_word import aplicar as aplicar_firebase
aplicar_firebase(d,ps,ROOT)

# Mantener geometría, estilos y elementos de la plantilla. Evitar cortes de filas.
for table in d.tables:
 for ri,r in enumerate(table.rows):
  pr=r._tr.get_or_add_trPr(); pr.append(OxmlElement('w:cantSplit'))
  if ri==0:
   pr.append(OxmlElement('w:tblHeader'))
   for c in r.cells:
    for p in c.paragraphs: p.paragraph_format.keep_with_next=True
d.core_properties.author='Luciana Cristina Gutierrez Quispe'
d.core_properties.title='Informe técnico Guía Práctica 13 PharmaMobil'
numero=0
for par in d.paragraphs:
 for run in par.runs:
  if re.search(r'Figura \d+\.',run.text):
   numero+=1
   run.text=re.sub(r'Figura \d+\.',f'Figura {numero}.',run.text)
OUT.parent.mkdir(parents=True,exist_ok=True); d.save(OUT)
assert hashlib.sha256(SRC.read_bytes()).hexdigest()==sha
with zipfile.ZipFile(SRC) as a,zipfile.ZipFile(OUT) as b:
 preserved=[n for n in a.namelist() if n in b.namelist() and n.startswith(('word/styles','word/numbering','word/theme/','word/header','word/footer'))]
 differences=[n for n in preserved if a.read(n)!=b.read(n)]
 (TMP/'fidelidad.json').write_text(json.dumps({'referencia_intacta':True,'partes_preservadas':preserved,'diferencias':differences},indent=2),encoding='utf-8')
print(OUT)
