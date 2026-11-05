"""
Genera los textos de la app desde strings.py (voseo e inglés) y tuteo.py (lo que cambia en español neutro):

  values/           inglés, y cualquier idioma sin traducción
  values-es/        español neutro (tuteo), para todos los países hispanohablantes
  values-es-rAR/    voseo: solo los textos distintos de values-es (Android toma el resto de values-es)
  values-es-rUY/    igual que values-es-rAR

No editar los strings.xml a mano: editar strings.py y tuteo.py y correr `python tools/i18n/gen_xml.py --write`.
Si queda voseo en el neutro, un marcador distinto o una clave desconocida, avisa y no escribe nada.
Usar comillas tipográficas (“ ”), no rectas.
"""
import os, re, sys
sys.path.insert(0, os.path.dirname(__file__))
from strings import S, A
from tuteo import T, TA

RES = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "app", "src", "main", "res")
HEADER_RIO = "<!-- Voseo para %s: solo lo que cambia respecto de values-es. Generado por tools/i18n/gen_xml.py. -->"

def esc(v):
    v = v.replace("&", "&amp;").replace("<", "&lt;").replace("'", "\\'").replace('"', '\\"').replace("\n", "\\n")
    return v

keys = [k for k, _, _ in S if k != "#"]
dup = {k for k in keys if keys.count(k) > 1}
if dup:
    sys.exit(f"Claves repetidas: {dup}")

# --- controles del español neutro ---
# Palabras que terminan con vocal o -s con tilde y no son voseo.
OK_ACCENT = {w.lower() for w in """
    aquí ahí así sí mí está están qué más después además también días día esté estés será sé aún dé
    atrás jamás quizás país inglés según ningún algún tenía tenías demás estás
    gané aprendí guardé
""".split()}
VOSEO_WORD = re.compile(r"(?<![\w])(\w+(?:á|é|í|ás|és|ís))(?![\w])", re.I)
VOSEO_LIST = re.compile(
    r"(?<![\w])(vos|sos|acá|ponele|tocame|tocala|tocalo|elegila|elegilas|arrastrala|arrastrame|buscame|llevame|contame|"
    r"mostrame|haceme|probalo|agregala|abrilo|acordate|tomate|mandale|saltear|apretad[ao]|prendid[ao]|prender|prendé|"
    r"cortina|mail|dale)(?![\w])", re.I)
PLACEHOLDER = re.compile(r"%\d*\$?[sd%]")

def neutral_problem(text):
    hits = [m.group(1) for m in VOSEO_WORD.finditer(text) if m.group(1).lower() not in OK_ACCENT]
    hits += [m.group(1) for m in VOSEO_LIST.finditer(text)]
    return hits

es_map = {k: es for k, es, _ in S if k != "#"}
problems = []
for k in T:
    if k not in es_map:
        problems.append(f"tuteo.py: clave desconocida {k}")
    elif sorted(PLACEHOLDER.findall(T[k])) != sorted(PLACEHOLDER.findall(es_map[k])):
        problems.append(f"tuteo.py: marcadores distintos en {k}")
arrays = dict(A)
for name, items in TA.items():
    if name not in arrays:
        problems.append(f"tuteo.py: lista desconocida {name}")
    elif any(i >= len(arrays[name]) for i in items):
        problems.append(f"tuteo.py: posición fuera de la lista {name}")
if not problems:
    for k, es in es_map.items():
        if k in ("tts_language", "home_date_pattern", "home_day_pattern"):
            continue
        for w in neutral_problem(T.get(k, es)):
            problems.append(f"voseo en el neutro: {k}: {w}")
    for name, items in A:
        for i, (es, _) in enumerate(items):
            for w in neutral_problem(TA.get(name, {}).get(i, es)):
                problems.append(f"voseo en el neutro: {name}[{i}]: {w}")
if problems:
    print("\n".join(problems))
    sys.exit("Corregí tuteo.py: no se escribió nada.")

# --- escritura ---
def save(path, lines, header):
    out = ['<?xml version="1.0" encoding="utf-8"?>', header, "<resources>"] + lines + ["</resources>\n"]
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, "w", encoding="utf-8", newline="\n").write("\n".join(out))

def english():
    lines = []
    for k, es, en in S:
        if k == "#":
            lines.append(f"\n    <!-- {es} -->")
            continue
        attr = ' translatable="false"' if k == "app_name" else ""
        lines.append(f'    <string name="{k}"{attr}>{esc(en)}</string>')
    for name, items in A:
        lines.append(f'    <string-array name="{name}">')
        lines.extend(f"        <item>{esc(en)}</item>" for _, en in items)
        lines.append("    </string-array>")
    return lines

def neutral():
    lines = []
    for k, es, _ in S:
        if k == "#":
            lines.append(f"\n    <!-- {es} -->")
            continue
        if k == "app_name":
            continue  # el nombre visible sale de un solo lugar: values/strings.xml
        lines.append(f'    <string name="{k}">{esc(T.get(k, es))}</string>')
    for name, items in A:
        lines.append(f'    <string-array name="{name}">')
        lines.extend(f"        <item>{esc(TA.get(name, {}).get(i, es))}</item>" for i, (es, _) in enumerate(items))
        lines.append("    </string-array>")
    return lines

def rioplatense():
    # Solo lo que cambia. Una lista de frases va entera: Android reemplaza el recurso completo.
    lines = [f'    <string name="{k}">{esc(es)}</string>' for k, es, _ in S if k != "#" and k in T and T[k] != es]
    for name, items in A:
        if any(TA.get(name, {}).get(i, es) != es for i, (es, _) in enumerate(items)):
            lines.append(f'    <string-array name="{name}">')
            lines.extend(f"        <item>{esc(es)}</item>" for es, _ in items)
            lines.append("    </string-array>")
    return lines

same = [k for k in T if T[k] == es_map[k]]
if same:
    print("En tuteo.py pero igual al voseo (se puede sacar):", same)

if "--write" in sys.argv:
    save(os.path.join(RES, "values", "strings.xml"), english(),
         "<!-- Idioma por defecto: inglés. Traducciones en values-es/ (y values-pt/ en el futuro). -->")
    save(os.path.join(RES, "values-es", "strings.xml"), neutral(), "<!-- Español neutro (tuteo) -->")
    rio = rioplatense()
    save(os.path.join(RES, "values-es-rAR", "strings.xml"), rio, HEADER_RIO % "Argentina")
    save(os.path.join(RES, "values-es-rUY", "strings.xml"), rio, HEADER_RIO % "Uruguay")
    print("escritos", len(keys), "textos; distintos en voseo:", sum(1 for l in rio if "<string name" in l))
else:
    print("Todo en orden. Para escribir: --write")
