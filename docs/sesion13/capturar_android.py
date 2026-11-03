"""Evidencias reales de la app Android en el emulador de prueba."""
import json, re, subprocess, sys, time
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent
OUT = ROOT / 'capturas'
OUT.mkdir(exist_ok=True)
ADB = r'C:\Users\lucia\AppData\Local\Android\Sdk\platform-tools\adb.exe'
PKG = 'pe.edu.upeu.pharmamobilee'

def adb(*args):
    p = subprocess.run([ADB, '-s', 'emulator-5554', *args], capture_output=True, timeout=90)
    if p.returncode: raise RuntimeError(p.stderr.decode(errors='replace')[:500])
    return p.stdout

def ui():
    adb('shell', 'uiautomator', 'dump', '/sdcard/s13.xml')
    return ET.fromstring(adb('exec-out', 'cat', '/sdcard/s13.xml'))

def tap(node):
    x1,y1,x2,y2 = map(int,re.findall(r'\d+', node.attrib['bounds']))
    adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))

def texts(tree):
    return [{'texto': n.get('text') or n.get('content-desc'), 'limites':n.get('bounds'), 'clase':n.get('class')}
            for n in tree.iter('node') if (n.get('text') or n.get('content-desc')) and n.get('password') != 'true']

action = sys.argv[1]
if action == 'tunnel':
    adb('reverse','tcp:8080','tcp:8080')
    adb('shell','run-as',PKG,'mkdir','-p','shared_prefs')
    config=b'<?xml version="1.0" encoding="utf-8"?><map><boolean name="usar_tunel_adb" value="true" /></map>'
    p=subprocess.run([ADB,'-s','emulator-5554','shell','run-as',PKG,'tee','shared_prefs/desarrollo.xml'], input=config,capture_output=True,timeout=30)
    assert p.returncode==0,p.stderr.decode(errors='replace')
    print('Tunel ADB preparado. Reiniciar app para leer configuracion de depuracion.')
elif action == 'state':
    print(json.dumps(texts(ui()), ensure_ascii=False))
elif action == 'tap':
    tree = ui()
    candidates = [n for n in tree.iter('node') if n.get('text') == sys.argv[2] or n.get('content-desc') == sys.argv[2]]
    if not candidates: raise RuntimeError('Elemento no encontrado: '+sys.argv[2])
    tap(candidates[0]); time.sleep(1)
    print(json.dumps(texts(ui()), ensure_ascii=False))
elif action == 'login':
    tree = ui()
    fields = [n for n in tree.iter('node') if n.get('class') == 'android.widget.EditText']
    tap(fields[0]); adb('shell','input','keyevent','123',*(['67']*25)); adb('shell','input','text','admin')
    tree = ui()
    fields = [n for n in tree.iter('node') if n.get('class') == 'android.widget.EditText']
    tap(fields[1]); adb('shell','input','keyevent','123',*(['67']*25)); adb('shell','input','text','Admin123')
    adb('shell','input','keyevent','4')
    tree = ui()
    tap(next(n for n in tree.iter('node') if n.get('text') == 'Iniciar sesión'))
    time.sleep(3)
    print(json.dumps(texts(ui()), ensure_ascii=False))
elif action == 'capture':
    label = sys.argv[2]
    assert re.fullmatch(r'[a-zA-Z0-9_]+', label)
    time.sleep(0.8)
    (OUT/(label+'.png')).write_bytes(adb('exec-out','screencap','-p'))
    (OUT/(label+'.json')).write_text(json.dumps({'fecha':time.strftime('%Y-%m-%d %H:%M:%S'),
        'plataforma':'Android emulador Medium Phone'},ensure_ascii=False,indent=2),encoding='utf-8')
    print(str(OUT/(label+'.png')))
elif action == 'field':
    tree = ui()
    fields = [n for n in tree.iter('node') if n.get('class') == 'android.widget.EditText']
    tap(fields[int(sys.argv[2])]); adb('shell','input','keyevent','123',*(['67']*25)); adb('shell','input','text',sys.argv[3])
    adb('shell','input','keyevent','4')
    print(json.dumps(texts(ui()), ensure_ascii=False))
elif action == 'swipe':
    adb('shell','input','swipe','920','2070','920','650','450')
    print(json.dumps(texts(ui()), ensure_ascii=False))
elif action == 'databases':
    import io, tarfile, sqlite3, tempfile
    raw = adb('exec-out','run-as',PKG,'tar','-cf','-','databases')
    with tempfile.TemporaryDirectory(dir=ROOT.parents[1]/'tmp') as temp:
        with tarfile.open(fileobj=io.BytesIO(raw)) as archive:
            for item in archive.getmembers():
                if item.isfile() and Path(item.name).name in ('pharma.db','pharma.db-wal','pharma.db-shm'):
                    Path(temp,Path(item.name).name).write_bytes(archive.extractfile(item).read())
        with sqlite3.connect(str(Path(temp,'pharma.db'))) as db:
            datos={'version':db.execute('pragma user_version').fetchone()[0],
                'productos':db.execute('select count(*) from producto').fetchone()[0],
                'pendientes':db.execute('select count(*) from producto where pendiente=1').fetchone()[0],
                'columnas':[r[1] for r in db.execute('pragma table_info(producto)')],
                'filas_de_prueba':list(db.execute("select id,nombre,intentos,ultimoError,requiereRevision from producto where nombre like 'S13_EVIDENCIA%'"))}
            (OUT/'base_local.json').write_text(json.dumps(datos,ensure_ascii=False,indent=2),encoding='utf-8')
            print(json.dumps(datos,ensure_ascii=False))
        db.close()
