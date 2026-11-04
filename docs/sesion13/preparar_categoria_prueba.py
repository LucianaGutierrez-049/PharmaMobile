"""Prepara una categoría identificada únicamente cuando el backend local está vacío.

Usa las cuentas de prueba existentes en memoria. No modifica usuarios, claves,
permisos ni categorías existentes.
"""
from pathlib import Path
import ast
import json
import urllib.request

ROOT = Path(__file__).resolve().parents[2]
BASE = 'http://localhost:8080/api/v1/'


def main():
    tree = ast.parse((ROOT / 'docs/actividad_autonoma_sesion10/probar_backend.py').read_text(encoding='utf-8'))
    claves = next(ast.literal_eval(n.value) for n in ast.walk(tree) if isinstance(n, ast.Assign)
                  and any(isinstance(t, ast.Name) and t.id == 'claves' for t in n.targets))
    def pedir(ruta, datos=None, token=None):
        headers = {'Content-Type': 'application/json'}
        if token: headers['Authorization'] = 'Bearer ' + token
        request = urllib.request.Request(BASE + ruta, data=json.dumps(datos).encode() if datos else None, headers=headers)
        with urllib.request.urlopen(request, timeout=20) as response:
            return response.status, json.load(response)
    _, sesion = pedir('auth/login', {'username': 'admin', 'password': claves['admin']})
    del claves
    token = sesion['token']
    _, categorias = pedir('categorias', token=token)
    resultado = {'fecha': '2026-11-04', 'categorias_antes': len(categorias), 'categoria_creada': False}
    if not categorias:
        estado, categoria = pedir('categorias', {'nombre': 'Pruebas Guia 13',
            'descripcion': 'Categoria identificada para verificar la integracion local de PharmaMobil', 'estado': True}, token)
        resultado.update(categoria_creada=True, estado_http=estado, id_categoria=categoria['id'])
    del token, sesion
    (ROOT / 'docs/sesion13/capturas/categoria_prueba_20261104.json').write_text(
        json.dumps(resultado, indent=2), encoding='utf-8')
    print(json.dumps(resultado))


if __name__ == '__main__':
    main()
