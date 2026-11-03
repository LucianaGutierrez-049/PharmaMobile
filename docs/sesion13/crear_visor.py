from pathlib import Path
import html,subprocess,shutil,hashlib
ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'tmp/s13_visor'; OUT.mkdir(exist_ok=True)
P='shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobilee/'
sections={
 'migracion':('Esquema y migración SQLDelight',['shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobilee/db/2.sqm','shared/src/commonMain/sqldelight/pe/edu/upeu/pharmamobilee/db/Producto.sq']),
 'sincronizacion':('Sincronización y política de reintentos',[P+'domain/model/PoliticaSincronizacion.kt',P+'data/repository/ProductoRepositorioOffline.kt']),
 'permisos':('Contrato común y permiso Android',[P+'domain/permiso/GestorPermisos.kt','androidApp/src/main/AndroidManifest.xml']),
 'firebase':('Firebase detrás del contrato de dominio',[P+'domain/port/Telemetria.kt',P+'data/firebase/TelemetriaFirebase.kt',P+'di/AppModule.kt']),
}
def save(name,title,blocks):
 body=''.join('<section><h2>'+html.escape(label)+'</h2><pre>'+html.escape(code)+'</pre></section>' for label,code in blocks)
 (OUT/(name+'.html')).write_text('<!doctype html><html lang="es"><meta charset="utf-8"><title>'+title+'</title><style>body{margin:32px;font:16px Arial;color:#172536;background:#f4f7fa}h1{font-size:27px}h2{font-size:16px;overflow-wrap:anywhere}p{color:#46596c}section{background:white;border:1px solid #ced8e1;border-radius:8px;padding:18px;margin:18px 0}pre{font:14px/1.5 Consolas,monospace;white-space:pre-wrap;overflow-wrap:anywhere}</style><h1>'+title+'</h1><p>PharmaMobil · Sesión 13 · Visor local de evidencia real del repositorio</p>'+body+'</html>',encoding='utf-8')
for name,(title,paths) in sections.items():
 save(name,title,[(p+' · SHA256 '+hashlib.sha256((ROOT/p).read_bytes()).hexdigest()[:12],(ROOT/p).read_text(encoding='utf-8')) for p in paths])
history=subprocess.check_output(['git','log','--oneline','--graph','--decorate','-9'],cwd=ROOT,text=True,encoding='utf-8')
status=subprocess.check_output(['git','status','--short'],cwd=ROOT,text=True,encoding='utf-8')
save('git','Historial Git real',[('git log --oneline --graph --decorate -9',history),('git status --short',status)])
db=ROOT/'docs/sesion13/capturas/base_local.json'
if db.exists(): save('base_local','Consulta real de la base Android',[('Copia SQLite leída con run-as sin modificar los datos del dispositivo',db.read_text(encoding='utf-8'))])
shutil.copytree(ROOT/'shared/build/reports/tests/testAndroidHostTest',OUT/'pruebas',dirs_exist_ok=True)
print(OUT)
