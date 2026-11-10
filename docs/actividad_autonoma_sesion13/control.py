"""Control local del perfil de evidencias. No guarda ni imprime credenciales."""
import argparse
import ast
import base64
import hashlib
import hmac
import json
import os
import re
import subprocess
import time
import uuid
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ADB = Path.home() / 'AppData/Local/Android/Sdk/platform-tools/adb.exe'
APP = 'pe.edu.upeu.pharmamobilee'
USER = '10'
CAPS = ROOT / 'docs/actividad_autonoma_sesion13/capturas'


def adb(*args, data=None, check=True):
    p = subprocess.run([str(ADB), '-s', 'emulator-5554', *args], input=data,
                       capture_output=True, timeout=90)
    if check and p.returncode:
        raise RuntimeError('ADB falló: ' + ' '.join(args[:3]))
    return p.stdout


def privado(*args, data=None, check=True):
    return adb('shell', 'run-as', APP, '--user', USER, *args, data=data, check=check)


def command(action, **values):
    ident = uuid.uuid4().hex
    payload = json.dumps(dict(id=ident, accion=action, **values)).encode()
    privado('sh', '-c', "'cat > files/evidencias_aa13/comando.tmp'", data=payload)
    privado('mv', 'files/evidencias_aa13/comando.tmp', 'files/evidencias_aa13/comando.json')
    until = time.monotonic() + 80
    while time.monotonic() < until:
        try:
            result = json.loads(privado('cat', 'files/evidencias_aa13/respuesta.json'))
            if result.get('id') == ident:
                if not result.get('aprobado'): raise RuntimeError(result.get('tipo_error'))
                return result
        except json.JSONDecodeError:
            pass
        time.sleep(.4)
    raise TimeoutError('No hay respuesta del ejecutor')


def xml():
    adb('shell', 'uiautomator', 'dump', '/sdcard/aa13.xml')
    return ET.fromstring(adb('shell', 'cat', '/sdcard/aa13.xml'))


def ui():
    return command('ui')['ui']


def center(node):
    x1, y1, x2, y2 = map(int, re.findall(r'\d+', node.get('bounds')))
    if x2 <= x1 or y2 <= y1: raise RuntimeError('Elemento fuera de pantalla')
    return str((x1+x2)//2), str((y1+y2)//2)


def tap(text, description=False, scroll=False):
    for attempt in range(7 if scroll else 1):
        attr = 'content-desc' if description else 'text'
        nodes = [n for n in ui() if n.get(attr) == text and n.get('bounds') != '[0,0][0,0]']
        if nodes:
            adb('shell', 'input', 'tap', *center(nodes[0])); time.sleep(1.5); return
        if scroll: adb('shell', 'input', 'swipe', '800', '1900', '800', '650', '400')
    raise RuntimeError('No se encontró: ' + text)


def fill(label, value):
    command('rellenar', campo=label, valor=value)
    time.sleep(.4)
    current = [n.get('text') for n in ui() if n.get('class', '').endswith('EditText')]
    if value not in current: raise RuntimeError('El campo no retuvo el valor completo: ' + label)


def top():
    for _ in range(5): adb('shell', 'input', 'swipe', '850', '550', '850', '1900', '150')


def capture(name, state=True, account='almacen'):
    CAPS.mkdir(parents=True, exist_ok=True)
    (CAPS / (name + '.png')).write_bytes(adb('exec-out', 'screencap', '-p'))
    if state:
        result = command('estado', cuenta=account)
        (CAPS / (name + '.json')).write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf8')
        return result


def main():
    p = argparse.ArgumentParser()
    p.add_argument('action', choices=['login', 'estado', 'captura', 'ui', 'terminar', 'expirar', 'sin_permiso'])
    p.add_argument('--usuario', default='almacen', choices=['almacen', 'vendedor'])
    p.add_argument('--nombre', default='estado')
    a = p.parse_args()
    CAPS.mkdir(parents=True, exist_ok=True)
    if a.action == 'ui':
        print(json.dumps(ui(), ensure_ascii=False, indent=2)); return
    if a.action == 'captura':
        (CAPS / (a.nombre + '.png')).write_bytes(adb('exec-out', 'screencap', '-p'))
        print(a.nombre + '.png'); return
    if a.action in ('login', 'expirar'):
        # Se consume en memoria una cuenta local ya existente; nunca se ejecuta el script fuente.
        source = ROOT / 'docs/actividad_autonoma_sesion10/probar_backend.py'
        tree = ast.parse(source.read_text(encoding='utf8'))
        keys = next(ast.literal_eval(n.value) for n in ast.walk(tree) if isinstance(n, ast.Assign)
                    and any(isinstance(t, ast.Name) and t.id == 'claves' for t in n.targets))
        if a.action == 'login':
            result = command('login', usuario=a.usuario, clave=keys[a.usuario], cuenta=a.usuario)
        else:
            # Fixture con firma local válida y exp en el pasado; no se cambia el reloj ni el backend.
            req = urllib.request.Request('http://127.0.0.1:8080/api/v1/auth/login',
                data=json.dumps({'username': a.usuario, 'password': keys[a.usuario]}).encode(),
                headers={'Content-Type': 'application/json'})
            login = json.load(urllib.request.urlopen(req, timeout=15))
            header, payload, signature = login['token'].split('.')
            alg = json.loads(base64.urlsafe_b64decode(header + '==='))['alg']
            digest = {'HS256': hashlib.sha256, 'HS384': hashlib.sha384, 'HS512': hashlib.sha512}[alg]
            def b64(v): return base64.urlsafe_b64encode(v).decode().rstrip('=')
            secret = None
            resources = ROOT.parent / 'pharmaSoft/src/main/resources'
            for config in resources.glob('application*.y*ml'):
                match = re.search(r'(?m)^\s*secret\s*:\s*(.+)', config.read_text(encoding='utf8'))
                if not match: continue
                value = match[1].strip().strip("\"'")
                if value.startswith('${'):
                    env, _, fallback = value[2:-1].partition(':')
                    value = os.environ.get(env, fallback)
                try: key = base64.b64decode(value)
                except ValueError: continue
                if hmac.compare_digest(b64(hmac.new(key, (header+'.'+payload).encode(), digest).digest()), signature):
                    secret = key; break
            if secret is None: raise RuntimeError('No se encontró la configuración de firma local para la fixture')
            claims = json.loads(base64.urlsafe_b64decode(payload + '==='))
            claims['iat'], claims['exp'] = int(time.time()) - 7200, int(time.time()) - 3600
            body = b64(json.dumps(claims, separators=(',', ':')).encode())
            unsigned = header + '.' + body
            expired = unsigned + '.' + b64(hmac.new(secret, unsigned.encode(), digest).digest())
            result = command('token_prueba', token=expired, cuenta=a.usuario)
            result['fixture_expirada_firma_local_verificada'] = True
            del secret, expired, login
        del keys
    else:
        result = command('intento_sin_permiso' if a.action == 'sin_permiso' else a.action, cuenta=a.usuario)
    (CAPS / (a.nombre + '.json')).write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf8')
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
