"""Convierte los dibujos del lienzo (SVG) en VectorDrawables de Android + el mapa Kotlin PetArt.kt."""
import os, re, sys, contextlib, io
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
os.chdir(HERE)
with contextlib.redirect_stdout(io.StringIO()):
    import gen as G

APP = os.path.join(HERE, "..", "..", "app", "src", "main")
DRAW = os.path.join(APP, "res", "drawable")

VW, VH = 256, 240  # viewport común: parada (200x240) centrada; acostada (256x200) apoyada abajo


def num(v):
    v = float(v)
    return str(int(v)) if v == int(v) else f"{v:.2f}".rstrip("0").rstrip(".")


def ellipse_d(cx, cy, rx, ry):
    cx, cy, rx, ry = map(float, (cx, cy, rx, ry))
    return (f"M{num(cx-rx)},{num(cy)} a{num(rx)},{num(ry)} 0 1,0 {num(2*rx)},0 "
            f"a{num(rx)},{num(ry)} 0 1,0 {num(-2*rx)},0 Z")


def argb(color, alpha=1.0):
    c = color.lstrip("#")
    if len(c) == 3:
        c = "".join(ch * 2 for ch in c)
    return "#" + c.upper()


def groups_for(transform):
    out = []
    for op, args in re.findall(r"(rotate|translate)\(([^)]*)\)", transform or ""):
        a = [float(x) for x in re.split(r"[ ,]+", args.strip())]
        if op == "rotate":
            px, py = (a[1], a[2]) if len(a) == 3 else (0, 0)
            out.append(f'<group android:rotation="{num(a[0])}" android:pivotX="{num(px)}" android:pivotY="{num(py)}">')
        else:
            ty = a[1] if len(a) > 1 else 0
            out.append(f'<group android:translateX="{num(a[0])}" android:translateY="{num(ty)}">')
    return out


def walk(el, style, out):
    st = dict(style)
    for k in ("fill", "stroke", "stroke-width", "opacity"):
        if k in el.attrib:
            st[k] = el.attrib[k]
    opens = groups_for(el.attrib.get("transform"))
    out.extend(opens)
    tag = el.tag
    if tag == "g":
        for ch in el:
            walk(ch, st, out)
    else:
        a = el.attrib
        if tag == "ellipse":
            d = ellipse_d(a["cx"], a["cy"], a["rx"], a["ry"])
        elif tag == "circle":
            d = ellipse_d(a["cx"], a["cy"], a["r"], a["r"])
        elif tag == "rect":
            x, y, w, h = (float(a[k]) for k in ("x", "y", "width", "height"))
            d = f"M{num(x)},{num(y)} h{num(w)} v{num(h)} h{num(-w)} Z"
        elif tag == "path":
            d = a["d"]
        else:
            raise ValueError(tag)
        attrs = [f'android:pathData="{d}"']
        alpha = float(st.get("opacity", 1))
        fill = st.get("fill", "#000000")
        if fill != "none":
            attrs.append(f'android:fillColor="{argb(fill)}"')
            if alpha < 1:
                attrs.append(f'android:fillAlpha="{num(alpha)}"')
        stroke = st.get("stroke", "none")
        sw = float(st.get("stroke-width", 1))
        if stroke != "none" and sw > 0:
            attrs += [f'android:strokeColor="{argb(stroke)}"', f'android:strokeWidth="{num(sw)}"',
                      'android:strokeLineCap="round"', 'android:strokeLineJoin="round"']
            if alpha < 1:
                attrs.append(f'android:strokeAlpha="{num(alpha)}"')
        out.append("<path " + " ".join(attrs) + " />")
    out.extend("</group>" for _ in opens)


def vector(markup, tx, ty):
    root = ET.fromstring(f'<g stroke="{G.INK}" stroke-width="4">{markup}</g>')
    body = []
    walk(root, {}, body)
    lines = ['<?xml version="1.0" encoding="utf-8"?>',
             '<!-- Generado desde el lienzo "Mascotas tiernas"; no editar a mano. -->',
             f'<vector xmlns:android="http://schemas.android.com/apk/res/android" android:width="{VW}dp" android:height="{VH}dp" '
             f'android:viewportWidth="{VW}" android:viewportHeight="{VH}">',
             f'<group android:translateX="{num(tx)}" android:translateY="{num(ty)}">']
    lines += body
    lines += ["</group>", "</vector>", ""]
    return "\n".join(lines)


STAND = (28, 0)
LIE = (16, 40)

BLINK_RE = re.compile(r'<path d="M(\S+) (\S+) Q(\S+) (\S+) (\S+) (\S+)" fill="none" stroke-width="4.5"></path>')


def open_eyes(markup):
    """Pasa ojos cerrados (parpadeo) a ojos abiertos, para la pose acostada despierta."""
    return BLINK_RE.sub(lambda m: eye_at(float(m.group(3)), float(m.group(2)) + 2), markup)


def eye_at(x, y):
    return (f'<ellipse cx="{num(x)}" cy="{num(y)}" rx="10" ry="12" fill="{G.INK}" stroke="none"></ellipse>'
            f'<circle cx="{num(x-3)}" cy="{num(y-5)}" r="4" fill="#FFFFFF" stroke="none"></circle>'
            f'<circle cx="{num(x+4)}" cy="{num(y+4)}" r="1.8" fill="#FFFFFF" stroke="none"></circle>')


ARM_RE = re.compile(r'<ellipse cx="([\d.]+)" cy="([\d.]+)" rx="([\d.]+)" ry="([\d.]+)" fill="([^"]+)" '
                    r'transform="rotate\((-[\d.]+) [\d.]+ [\d.]+\)"></ellipse>')


def paw_up(markup, angle=62):
    """"Dar la patita": el brazo (o ala) derecho levantado hacia afuera, con la almohadilla rosada."""
    import math
    m = [m for m in ARM_RE.finditer(markup) if float(m.group(1)) >= 130 and float(m.group(2)) >= 140][-1]
    cx, cy, rx, ry = (float(m.group(i)) for i in range(1, 5))
    fill = m.group(5)
    sx, sy = cx - 4, cy - ry + 2  # hombro
    if ry < 20:  # brazos cortos (cuadrúpedos): más largos para que se note la patita
        rx, ry = rx + 1, ry * 1.45
    a = math.radians(angle)
    ccx, ccy = sx + math.sin(a) * ry, sy - math.cos(a) * ry
    tx, ty = sx + math.sin(a) * (2 * ry - 4), sy - math.cos(a) * (2 * ry - 4)
    arm = (f'<ellipse cx="{ccx:.1f}" cy="{ccy:.1f}" rx="{num(rx)}" ry="{ry + 2:.1f}" fill="{fill}" '
           f'transform="rotate({angle} {ccx:.1f} {ccy:.1f})"></ellipse>'
           f'<ellipse cx="{tx:.1f}" cy="{ty:.1f}" rx="{rx * 0.55:.1f}" ry="{rx * 0.45:.1f}" fill="{G.BLUSH}" opacity=".7" '
           f'stroke="none" transform="rotate({angle} {tx:.1f} {ty:.1f})"></ellipse>')
    # El brazo levantado va al final: por delante del cuerpo y de la cabeza.
    return markup[:m.start()] + markup[m.end():] + arm


def sit_body(front, bird):
    """Sentada sin cabeza: para girar solo la cabeza con el modelo 2,5D (ui: PetModel.drawHead)."""
    m = sitting(front, bird, "open")
    cut = f'<g transform="translate(0 {G.SIT_HEAD_DY})">'
    return m.split(cut)[0] if cut in m else m


def sitting(front, bird, e):
    """Sentada (pose de reposo): las aves se esponjan; las demás pliegan las patas (gen.SITTING)."""
    if bird:
        return bird(e, feet=False)
    G.SITTING = True
    try:
        m = front(e)
    finally:
        G.SITTING = False
    # base_body dejó abierto el grupo que baja la cabeza (la rana no usa base_body: queda igual)
    return m + ("</g>" if f'translate(0 {G.SIT_HEAD_DY})' in m else "")


PETS = {
    # enum:    (nombre en el lienzo, frente(e), perfil, espalda, ave?)
    "DOG": ("Perro", lambda e: G.dog(e), G.dog_side, G.dog_back, None),
    "CAT": ("Gato", G.cat, G.cat_side, G.cat_back, None),
    "DUCK": ("Pato", lambda e: G.duck_v(e), G.duck_side, G.duck_back, G.duck_v),
    "FROG": ("Rana", G.frog, G.frog_side, G.frog_back, None),
    "COW": ("Vaca", G.cow, G.cow_side, G.cow_back, None),
    "PIG": ("Chancho", G.pig, G.pig_side, G.pig_back, None),
    "CHICK": ("Pollito", lambda e: G.chick_v(e), G.chick_side, G.chick_back, G.chick_v),
    "LION": ("León", G.lion, G.lion_side, G.lion_back, None),
    "MONKEY": ("Mono", G.monkey, G.monkey_side, G.monkey_back, None),
    "OWL": ("Búho", lambda e: G.owl_v(e), G.owl_side, G.owl_back, G.owl_v),
}
VIEWS = ["FRONT", "BLINK", "HAPPY", "SIDE", "BACK", "LIE", "SLEEP", "PAW", "SIT", "SIT_BLINK", "SIT_HAPPY", "SIT_BODY"]

written = []
for enum, (name, front, side, back, bird) in PETS.items():
    sleep = G.SLEEP[name][1]()
    art = {
        "FRONT": (front("open"), STAND),
        "BLINK": (front("blink"), STAND),
        "HAPPY": (front("happy"), STAND),
        "SIDE": (side(), STAND),
        "BACK": (back(), STAND),
        "PAW": (paw_up(front("happy")), STAND),
        "SIT": (sitting(front, bird, "open"), STAND),
        "SIT_BLINK": (sitting(front, bird, "blink"), STAND),
        "SIT_HAPPY": (sitting(front, bird, "happy"), STAND),
        "SIT_BODY": (sit_body(front, bird), STAND),
    }
    if bird:
        art["LIE"] = (bird("open", feet=False), STAND)
        art["SLEEP"] = (bird("blink", feet=False), STAND)
    else:
        art["LIE"] = (open_eyes(sleep), LIE)
        art["SLEEP"] = (sleep, LIE)
    for view, (markup, (tx, ty)) in art.items():
        fname = f"pet_{enum.lower()}_{view.lower()}.xml"
        with open(os.path.join(DRAW, fname), "w", encoding="utf-8", newline="\n") as f:
            f.write(vector(markup, tx, ty))
        written.append(fname)

kt = ['package com.petnotifications', '',
      '// Generado desde el lienzo "Mascotas tiernas"; no editar a mano.', '',
      '/** Dibujos de cada mascota: de frente (con sus caras), de perfil, de espaldas, echada, dando la patita y sentada. */',
      'enum class PetView { ' + ", ".join(VIEWS) + ' }', '',
      '/** Las aves no se echan: se esponjan en el lugar, así que su pose dormida no cambia el encuadre. */',
      'val PetType.liesDown: Boolean',
      '    get() = this != PetType.DUCK && this != PetType.CHICK && this != PetType.OWL', '',
      '/** Dibujo fijo (los de siempre, en el pelaje original); null si la mascota no tiene (ver petPainter). */',
      'fun PetType.art(view: PetView): Int? = when (this) {']
for enum in PETS:
    kt.append(f'    PetType.{enum} -> when (view) {{')
    for v in VIEWS:
        kt.append(f'        PetView.{v} -> R.drawable.pet_{enum.lower()}_{v.lower()}')
    kt.append('    }')
kt += ['    else -> null', '}', '']
with open(os.path.join(APP, "java", "com", "petnotifications", "PetArt.kt"), "w", encoding="utf-8", newline="\n") as f:
    f.write("\n".join(kt))
print(len(written), "drawables")
