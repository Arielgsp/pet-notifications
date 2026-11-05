"""
Pasa el arte de los accesorios (docs/accesorios-especies, SVG de Cowork por especie y vista, en el lienzo
de 200×240) a la app: app/.../overlay/OutfitArtData.kt, con cada figura como un trazado ya transformado. El
borde no se escala: queda siempre del grosor del contorno de la mascota.

Uso: python tools/pet_art/gen_outfit.py
"""
import json
import math
import os
import re
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.join(HERE, "..", "..")
SRC = os.path.join(ROOT, "docs", "accesorios-especies")
OUT_KT = os.path.join(ROOT, "app", "src", "main", "java", "com", "petnotifications", "overlay", "OutfitArtData.kt")
ARTS = ("panuelo", "gorro_navideno", "gorrito_lana", "corona_flores", "calabacita", "casco_calabaza")
# El casco tiene agujeros para la cara (clipPath). Con la cara hacia atrás va entero: sin recorte, sin el brillo
# amarillo de los agujeros y sin sus bordes.
HOLED = "casco_calabaza"
HOLE_GLOW = "#FFD36E"
# Ancho del casco respecto del dibujo de Cowork, que quedaba excesivo. Perro y león van más anchos: si no, asoman
# los costados de la cabeza y la melena. Se achican los gajos, sus rayas y el brillo; los agujeros no se mueven.
HOLED_WIDTH = 0.86
HOLED_WIDTHS = {"perro": 0.90, "leon": 0.90}
# De perfil se achica desde atrás: el conejo tiene la cabeza más larga y quedaría a la vista.
HOLED_SIDE_WIDTHS = {"conejo": 0.97}
STEM_MARKS = ("#8A6A3B", "#8FC1A0", "#74A886")
PETS = {"DOG": "perro", "CAT": "gato", "DUCK": "pato", "FROG": "rana", "COW": "vaca", "PIG": "chancho",
        "CHICK": "pollito", "LION": "leon", "MONKEY": "mono", "OWL": "buho", "RABBIT": "conejo"}
# De espaldas solo se ve el nudo del pañuelo (LEEME de Cowork): el del perfil, en el medio de la espalda,
# a la altura del cuello de cada especie (anclajes.json).
KNOT = ('<svg xmlns="http://www.w3.org/2000/svg"><g transform="translate({dx} {dy})" stroke="#4A3228" stroke-width="4" stroke-linejoin="round" stroke-linecap="round">'
        '<path d="M96 155 L88 168 M104 155 L112 168" fill="none" stroke-width="3.5"/>'
        '<ellipse cx="100" cy="150" rx="7" ry="6" fill="#74A886"/></g></svg>')
# Color del pañuelo: Cowork lo dibujó en verde salvia; va en coral, que combina con todas las especies.
# Solo en el pañuelo: las hojas de la calabacita y de la corona usan el mismo verde.
SCARF_COLORS = {"#8FC1A0": "#E8836F", "#74A886": "#CF6A58"}
K = 0.5522847498


def mul(a, b):
    return (a[0] * b[0] + a[2] * b[1], a[1] * b[0] + a[3] * b[1],
            a[0] * b[2] + a[2] * b[3], a[1] * b[2] + a[3] * b[3],
            a[0] * b[4] + a[2] * b[5] + a[4], a[1] * b[4] + a[3] * b[5] + a[5])


def rotation(deg, cx=0.0, cy=0.0):
    c, s = math.cos(math.radians(deg)), math.sin(math.radians(deg))
    return mul(mul((1, 0, 0, 1, cx, cy), (c, s, -s, c, 0, 0)), (1, 0, 0, 1, -cx, -cy))


def transform(text):
    m = (1, 0, 0, 1, 0, 0)
    for name, args in re.findall(r"(\w+)\(([^)]*)\)", text or ""):
        v = [float(x) for x in re.split(r"[ ,]+", args.strip())]
        if name == "translate":
            m = mul(m, (1, 0, 0, 1, v[0], v[1] if len(v) > 1 else 0))
        elif name == "scale":
            m = mul(m, (v[0], 0, 0, v[1] if len(v) > 1 else v[0], 0, 0))
        elif name == "rotate":
            m = mul(m, rotation(*v))
        else:
            raise ValueError(name)
    return m


def fmt(v):
    return f"{v:.2f}".rstrip("0").rstrip(".").replace("-0", "0") if abs(v) < 0.005 else f"{v:.2f}".rstrip("0").rstrip(".")


def point(m, x, y):
    return f"{fmt(m[0] * x + m[2] * y + m[4])} {fmt(m[1] * x + m[3] * y + m[5])}"


def path_d(d, m):
    out = []
    for cmd, args in re.findall(r"([MLQCZ])([^MLQCZ]*)", d):
        v = [float(x) for x in re.findall(r"-?\d*\.?\d+", args)]
        if len(v) % 2:
            raise ValueError(d)
        out.append(cmd + " ".join(point(m, v[i], v[i + 1]) for i in range(0, len(v), 2)))
    return " ".join(out)


def ellipse_d(cx, cy, rx, ry, m):
    kx, ky = rx * K, ry * K
    p = lambda x, y: point(m, cx + x, cy + y)
    return (f"M{p(rx, 0)} C{p(rx, ky)} {p(kx, ry)} {p(0, ry)} C{p(-kx, ry)} {p(-rx, ky)} {p(-rx, 0)} "
            f"C{p(-rx, -ky)} {p(-kx, -ry)} {p(0, -ry)} C{p(kx, -ry)} {p(rx, -ky)} {p(rx, 0)} Z")


def walk(el, m, style, out):
    tag = el.tag.split("}")[-1]
    m = mul(m, transform(el.get("transform")))
    style = dict(style)
    for key in ("fill", "stroke", "stroke-width", "opacity", "clip-path"):
        if el.get(key) is not None:
            style[key] = el.get(key)
    if el.get("clip-path") is not None and m != (1, 0, 0, 1, 0, 0):
        raise ValueError("clip-path con transformación")
    if tag == "defs":
        return
    if tag == "path":
        d = path_d(el.get("d"), m)
    elif tag in ("ellipse", "circle"):
        r = float(el.get("r", 0))
        d = ellipse_d(float(el.get("cx")), float(el.get("cy")), float(el.get("rx", r)), float(el.get("ry", r)), m)
    elif tag in ("g", "svg"):
        for child in el:
            walk(child, m, style, out)
        return
    else:
        raise ValueError(tag)
    fill = style.get("fill")
    stroke = style.get("stroke")
    out.append(dict(
        d=d,
        fill=None if fill == "none" else fill,
        stroke=None if stroke in (None, "none") else stroke,
        w=float(style.get("stroke-width", 0)),
        alpha=float(style.get("opacity", 1)),
        clipped="clip-path" in style,
    ))


def read_ops(text):
    out = []
    walk(ET.fromstring(text), (1, 0, 0, 1, 0, 0), {}, out)
    return out


def clip_of(text):
    """El trazado del clipPath, tal cual: ya está en el lienzo y puede usar V y H."""
    found = ET.fromstring(text).findall(".//{http://www.w3.org/2000/svg}clipPath/{http://www.w3.org/2000/svg}path")
    return found[0].get("d") if found else None


def recolor_scarf(ops):
    return [{**o, "fill": SCARF_COLORS.get(o["fill"], o["fill"])} for o in ops]


def narrow_holed(text, folder, view):
    """El casco más angosto. De frente se achica hacia el centro; de perfil hacia adelante, donde está el agujero del ojo."""
    f = HOLED_WIDTHS.get(folder, HOLED_WIDTH)
    if view == "perfil":
        f = HOLED_SIDE_WIDTHS.get(folder, f)
    ellipses = re.findall(r'<ellipse cx="([^"]+)" cy="[^"]+" rx="([^"]+)"', text)
    pivot = 100.0 if view == "frente" else max(float(cx) + float(rx) for cx, rx in ellipses)
    x_at = lambda x: pivot + (x - pivot) * f
    # El tallo y la hoja se corren enteros con el centro de la calabaza, sin deformarse.
    shift = x_at(100.0) - 100.0

    def ellipse(m):
        tag = re.sub(r'cx="([^"]+)"', lambda c: f'cx="{x_at(float(c.group(1))):.2f}"', m.group(0))
        return re.sub(r'rx="([^"]+)"', lambda r: f'rx="{float(r.group(1)) * f:.2f}"', tag)

    def path(m):
        tag = m.group(0)
        if 'opacity=".45"' in tag or "#FFF4E2" in tag:
            move = x_at
        elif any(mark in tag for mark in STEM_MARKS):
            move = lambda x: x + shift
        else:
            return tag
        pairs = lambda d: re.sub(r"(-?\d+\.?\d*)[ ,]+(-?\d+\.?\d*)", lambda p: f"{move(float(p.group(1))):.2f} {p.group(2)}", d.group(1))
        return re.sub(r'\bd="([^"]+)"', lambda d: f'd="{pairs(d)}"', tag)

    return re.sub(r"<path[^>]*>", path, re.sub(r"<ellipse[^>]*>", ellipse, text))


def read_svg(folder, view, name):
    with open(os.path.join(SRC, folder, view, name + ".svg"), encoding="utf-8") as fh:
        text = fh.read()
    return narrow_holed(text, folder, view) if name == HOLED else text


def read_art(folder, view, name):
    ops = read_ops(read_svg(folder, view, name))
    return recolor_scarf(ops) if name == "panuelo" else ops


def read_clip(folder, view, name):
    with open(os.path.join(SRC, folder, view, name + ".svg"), encoding="utf-8") as fh:
        return clip_of(fh.read())


def whole(ops):
    """El casco sin agujeros, para verlo de espaldas."""
    holes = {o["d"] for o in ops if o["stroke"] == HOLE_GLOW}
    return [{**o, "clipped": False} for o in ops if o["d"] not in holes]


def color(c):
    return "null" if c is None else "0xFF" + c.lstrip("#").upper()


def kt_art(key, ops, clip=None):
    rows = "\n".join(f'            ArtOp("{o["d"]}", {color(o["fill"])}, {color(o["stroke"])}, {fmt(o["w"])}f, {fmt(o["alpha"])}f),' for o in ops)
    extra = ""
    if clip:
        # Las recortadas van primero: Art las pinta juntas.
        clipped = sum(o["clipped"] for o in ops)
        if any(o["clipped"] for o in ops[clipped:]):
            raise ValueError(key)
        extra = f', clip = "{clip}", clipped = {clipped}'
    return f'        "{key}" to lazy {{\n            Art(listOf(\n{rows}\n            ){extra})\n        }},'


def main():
    with open(os.path.join(SRC, "anclajes.json"), encoding="utf-8") as fh:
        anchors = json.load(fh)
    blocks = []
    for pet, folder in PETS.items():
        for name in ARTS:
            for view in ("frente", "perfil"):
                blocks.append(kt_art(f"{pet}/{name}/{view}", read_art(folder, view, name), read_clip(folder, view, name)))
        blocks.append(kt_art(f"{pet}/{HOLED}/espalda", whole(read_art(folder, "frente", HOLED))))
        blocks.append(kt_art(f"{pet}/{HOLED}/perfil_atras", whole(read_art(folder, "perfil", HOLED))))
        nx, ny = anchors[folder]["frente"]["cuello"]["translate"]
        blocks.append(kt_art(f"{pet}/panuelo/espalda", recolor_scarf(read_ops(KNOT.format(dx=nx - 100, dy=ny - 150)))))
    body = "\n".join(blocks)
    with open(OUT_KT, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(f"""package com.petnotifications.overlay

/** Generado por tools/pet_art/gen_outfit.py desde docs/accesorios-especies: no editar a mano. */
internal object OutfitArtData {{
    private val arts: Map<String, Lazy<Art>> = mapOf(
{body}
    )

    /** [pet]: nombre de PetType; [name]: el archivo (panuelo, gorrito_lana...); [view]: frente, perfil o espalda. */
    fun get(pet: String, name: String, view: String): Art? = arts["$pet/$name/$view"]?.value
}}
""")
    print(os.path.normpath(OUT_KT), len(blocks))


if __name__ == "__main__":
    main()
