from pathlib import Path
import json,subprocess
ROOT=Path(__file__).resolve().parents[2]; D=ROOT/'docs/sesion13'; C=D/'capturas'
manifest={'inicio':'E06_inicio.png','pendientes':'E04_offline.png','fallidas':'E05_fallida.png','concedido':'E07_concedido.png','denegado':'E08_denegado.png','definitivo':'E09_definitivo.png'}
(C/'evidencias_actuales.json').write_text(json.dumps(manifest,indent=2),encoding='utf-8')
(D/'validacion_android.md').write_text('''# Validación Android de sesión 13

Se recuperó la sesión y GET productos respondió 200 mediante túnel ADB. Se capturaron denegación, bloqueo definitivo y permiso concedido desde Ajustes.

El registro S13_EVIDENCIA_20261103 (precio 2.50, stock 10) recibió 404 en el POST. Se conservó con intentos 1/5, motivo y confirmación de reenvío. Ese POST aprovechó una conexión persistente todavía abierta al retirar el túnel: NO se atribuye el 404 a ausencia de red.

Para validar offline se cerró el proceso, se deshabilitaron Wi-Fi y datos, y se abrió sin túnel. Se conservaron sesión y producto. SQLite real mostró versión 3 y una fila pendiente con id -1. La UI cuenta ese pendiente como una fila que requiere revisión, no como un envío automático elegible.

Se restauraron conectividad y túnel. No se confirmó el reenvío ni se eliminó el registro de prueba. Firebase real e iOS siguen pendientes. Los visores muestran código real, Git real y el HTML original Gradle.

Las 67 pruebas corresponden a la ejecución previa al ajuste de URL; el ajuste posterior compiló y se validó mediante REST real en Android.
''',encoding='utf-8')
def git(*args): return subprocess.check_output(['git',*args],cwd=ROOT,text=True,encoding='utf-8').strip()
paths=['shared/src/commonTest/kotlin/pe/edu/upeu/pharmamobilee/data/repository/ProductoRepositorioOfflineTest.kt','shared/src/commonTest/kotlin/pe/edu/upeu/pharmamobilee/domain/usecase/GestorPermisosTest.kt','shared/src/commonTest/kotlin/pe/edu/upeu/pharmamobilee/domain/usecase/PoliticaSincronizacionTest.kt','docs/sesion13/capturar_android.py','docs/sesion13/crear_visor.py','docs/sesion13/capturar_visor.cjs','docs/sesion13/validacion_android.md']
paths += [str(p.relative_to(ROOT)).replace('\\','/') for p in C.glob('E*.png') if p.name!='E01_acceso_actual.png']
paths += ['docs/sesion13/capturas/base_local.json','docs/sesion13/capturas/evidencias_actuales.json']
git('add','--',*paths); git('diff','--cached','--check')
message='test: documenta pruebas y evidencias reales del integrador 13'
git('commit','-m',message)
records=json.loads((D/'commits_sesion13.json').read_text(encoding='utf-8'))
records.append({'paso':7,'hash':git('rev-parse','HEAD'),'mensaje':message})
(D/'commits_sesion13.json').write_text(json.dumps(records,ensure_ascii=False,indent=2),encoding='utf-8')
print(git('log','-9','--format=%h %s'))
