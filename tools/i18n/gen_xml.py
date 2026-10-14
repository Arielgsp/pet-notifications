"""
Genera los textos de la app (values/strings.xml en inglés y values-es/strings.xml en español) desde
strings.py, la lista de (clave, español, inglés). No editar los strings.xml a mano: editar strings.py y
correr `python tools/i18n/gen_xml.py --write`. Usar comillas tipográficas (“ ”), no rectas.
"""
import os, re, sys
sys.path.insert(0, os.path.dirname(__file__))
from strings import S

RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "app", "src", "main", "res")
old = open(os.path.join(RES, "values-es", "strings.xml"), encoding="utf-8").read()
old_map = dict(re.findall(r'<string name="([^"]+)">(.*?)</string>', old, re.S))

def esc(v):
    v = v.replace("&", "&amp;").replace("<", "&lt;").replace("'", "\\'").replace("\n", "\\n")
    return v

keys = [k for k, _, _ in S if k != "#"]
dup = {k for k in keys if keys.count(k) > 1}
missing = [k for k in old_map if k not in keys]
print("dup:", dup, "missing old keys:", missing)
for k, es, en in S:
    if k in old_map and k != "#":
        o = old_map[k].replace("\n", "\\n")
        if o != esc(es):
            print("DIFF", k, "\n  old:", o[:120], "\n  new:", esc(es)[:120])

def write(path, idx, header):
    lines = ['<?xml version="1.0" encoding="utf-8"?>', header, "<resources>"]
    for k, es, en in S:
        if k == "#":
            lines.append(f"\n    <!-- {es} -->")
            continue
        if k == "app_name" and idx == 0:
            continue  # el nombre visible sale de un solo lugar: values/strings.xml
        v = (es, en)[idx]
        attr = ' translatable="false"' if k == "app_name" else ""
        lines.append(f'    <string name="{k}"{attr}>{esc(v)}</string>')
    lines.append("</resources>\n")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "w", encoding="utf-8", newline="\n").write("\n".join(lines))

if "--write" in sys.argv:
    write(os.path.join(RES, "values", "strings.xml"), 1,
          "<!-- Idioma por defecto: inglés. Traducciones en values-es/ (y values-pt/ en el futuro). -->")
    write(os.path.join(RES, "values-es", "strings.xml"), 0, "<!-- Español -->")
    print("written", len(keys))
