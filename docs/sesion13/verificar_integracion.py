"""Prueba Android opt-in sin exportar contraseñas ni tokens.

Por defecto utiliza la sesión ya guardada. --usar-cuenta-local consume en memoria
la cuenta de pruebas existente en el entorno de sesión 10; no ejecuta ese script.
"""
from pathlib import Path
import argparse
import ast
import json
import subprocess

ROOT = Path(__file__).resolve().parents[2]
ADB = Path.home() / 'AppData/Local/Android/Sdk/platform-tools/adb.exe'
APP = 'pe.edu.upeu.pharmamobilee'


def adb(*args, entrada=None):
    result = subprocess.run([str(ADB), '-s', 'emulator-5554', *args], input=entrada,
                            stdout=subprocess.PIPE, stderr=subprocess.PIPE, timeout=180)
    if result.returncode:
        raise RuntimeError('ADB no pudo completar la operación solicitada')
    return result.stdout


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--usar-cuenta-local', action='store_true')
    args = parser.parse_args()
    adb('install', '-r', str(ROOT / 'androidApp/build/outputs/apk/androidTest/debug/androidApp-debug-androidTest.apk'))
    adb('reverse', 'tcp:8080', 'tcp:8080')
    try:
        if args.usar_cuenta_local:
            source = ROOT / 'docs/actividad_autonoma_sesion10/probar_backend.py'
            tree = ast.parse(source.read_text(encoding='utf-8'))
            datos = next(ast.literal_eval(n.value) for n in ast.walk(tree) if isinstance(n, ast.Assign)
                         and any(isinstance(t, ast.Name) and t.id == 'claves' for t in n.targets))
            usuario = 'almacen'
            entrada = json.dumps({'usuario': usuario, 'clave': datos[usuario]}).encode('utf-8')
            adb('shell', 'run-as', APP, 'sh', '-c', "'cat > files/s13_acceso_temporal.json'", entrada=entrada)
            del entrada, datos
        adb('shell', 'run-as', APP, 'rm', '-f', 'files/evidencias_s13/validacion_integracion.json')
        adb('shell', 'am', 'instrument', '-w', APP + '.test/' + APP + '.Integrador13Instrumentation')
        # La prueba solo publica códigos, indicadores técnicos e ID del producto de prueba.
        result = json.loads(adb('shell', 'run-as', APP, 'cat', 'files/evidencias_s13/validacion_integracion.json'))
        (ROOT / 'docs/sesion13/capturas/validacion_integracion_20261104.json').write_text(
            json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
        print(json.dumps(result, ensure_ascii=False, indent=2))
    finally:
        # Limpieza también si la instrumentación no llega a consumir el archivo.
        adb('shell', 'run-as', APP, 'rm', '-f', 'files/s13_acceso_temporal.json')


if __name__ == '__main__':
    main()
