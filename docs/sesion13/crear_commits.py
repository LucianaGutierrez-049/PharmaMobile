"""Confirmar únicamente los cambios de sesión 13, preservando cambios previos."""
from pathlib import Path
import subprocess,json
ROOT=Path(__file__).resolve().parents[2]
P='shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/'
def git(*args,input=None):
 p=subprocess.run(['git',*args],cwd=ROOT,input=input,capture_output=True)
 if p.returncode: raise RuntimeError(p.stderr.decode(errors='replace'))
 return p.stdout.decode().strip()
def stage(paths): git('add','--',*paths)
def staged_text(path,content):
 blob=git('hash-object','-w','--stdin','--path='+path,input=content.encode())
 git('update-index','--add','--cacheinfo','100644,'+blob+','+path)
def work(path):return (ROOT/path).read_text(encoding='utf-8')
records=[]
def commit(step,message):
 git('diff','--cached','--check')
 git('commit','-m',message)
 records.append({'paso':step,'hash':git('rev-parse','HEAD'),'mensaje':message})
 (ROOT/'docs/sesion13/commits_sesion13.json').write_text(json.dumps(records,indent=2,ensure_ascii=False),encoding='utf-8')
 print(step,records[-1]['hash'][:8],message,flush=True)

assert not git('diff','--cached','--name-only'), 'Existen cambios previos en el índice.'
assert git('branch','--show-current')=='feature/offline-first-Gutierrez'
git('switch','-c','feature/integrador-13')
stage(['docs/sesion13/brechas.md']); commit(1,'docs: registra brechas y alcance del integrador 13')
stage([P+'domain/model/Producto.kt',P+'data/local/ProductoLocal.kt',
 'shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobilee/db/Producto.sq',
 'shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobilee/db/2.sqm'])
commit(2,'feat: persiste intentos y revision de envios locales')
stage([P+'domain/model/PoliticaSincronizacion.kt',P+'domain/repository/ProductoRepository.kt'])
repo=P+'data/repository/ProductoRepositorioOffline.kt'
content='\n'.join(line for line in work(repo).splitlines() if 'Telemetria' not in line and 'telemetria.registrar' not in line)+'\n'
staged_text(repo,content)
commit(3,'feat: refina sincronizacion con limites y reenvios confirmados')
screen=P+'presentation/producto/ProductoScreen.kt'
stage([P+'App.kt',P+'presentacion/inicio/InicioScreen.kt',P+'presentation/producto/ProductoUiState.kt',P+'presentation/producto/ProductoViewModel.kt'])
staged_text(screen,work(screen).replace('        pe.edu.upeu.pharmamobilee.presentation.permiso.PermisoNotificacionesCard()\n',''))
commit(4,'feat: muestra pendientes fallos y antiguedad del inventario')
stage([screen,P+'domain/permiso',P+'presentation/permiso',
 'shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/data/permiso',
 'shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/data/permiso',
 'shared/src/androidMain/kotlin/pe/edu/upeu/pharmamobilee/di/PlatformModule.android.kt',
 'shared/src/iosMain/kotlin/pe/edu/upeu/pharmamobilee/di/PlatformModule.ios.kt',
 'androidApp/src/main/AndroidManifest.xml','androidApp/src/main/kotlin/pe/edu/upeu/pharmamobilee/MainActivity.kt'])
staged_text('shared/build.gradle.kts',work('shared/build.gradle.kts').replace('            implementation("dev.gitlive:firebase-crashlytics:2.7.0")\n',''))
commit(5,'feat: integra permiso nativo de notificaciones por plataforma')
stage(['.gitignore','build.gradle.kts','shared/build.gradle.kts','iosApp/iosApp/iOSApp.swift',P+'domain/port',P+'data/firebase',P+'di/AppModule.kt',repo])
app='androidApp/build.gradle.kts'
base=git('show','HEAD:'+app)+'\n'
start=work(app).index('// Sin configuración real')
end=work(app).index('kotlin {',start)
staged_text(app,base.replace('kotlin {',work(app)[start:end]+'kotlin {',1))
commit(6,'feat: integra Crashlytics mediante contrato de telemetria')
print('Cambios previos conservados fuera de los commits. Paso 7 pendiente de evidencias finales.',flush=True)
