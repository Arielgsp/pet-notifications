"""
Láminas de referencia para Cowork/Design: cada mascota de frente y de perfil en SVG, con el grosor del
contorno, el centro de la cabeza y el cuello marcados. Uso:

    python tools/pet_art/export_reference.py [carpeta]

Deja <mascota>-frente.svg y <mascota>-perfil.svg (512 px de ancho) en docs/referencia-mascotas/ o en
[carpeta]. Los dibujos salen de gen.py; el conejo, que no está ahí, del modelo 2,5D (model3d.py). Las
medidas están en unidades del lienzo de gen.py (200 × 240).
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import gen as G  # noqa: E402
import model3d as M  # noqa: E402

OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(
    os.path.dirname(os.path.abspath(__file__)), "..", "..", "docs", "referencia-mascotas")
STROKE = 4  # el contorno de gen.py (W) y de PetModel (outline)
MARK = "#1E88E5"
NECK = "#D81B60"
PX = 512
VIEW_Y, VIEW_W, VIEW_H = -20, 200, 340  # lienzo (200 × 240) con lugar para las orejas del conejo, y leyenda
BODY_FRONT = (100, 176, 42, 38)  # gen.base_body
BODY_SIDE = (96, 178, 48, 34)  # gen.side_body


def ellipse_meet(a, b):
    """Puntos donde se cruzan los contornos de dos elipses (cx, cy, rx, ry), buscando sobre la primera."""
    out = []
    n = 3600
    def f(t):
        x = a[0] + a[2] * math.cos(t); y = a[1] + a[3] * math.sin(t)
        return ((x - b[0]) / b[2]) ** 2 + ((y - b[1]) / b[3]) ** 2 - 1, x, y
    prev = f(0)
    for i in range(1, n + 1):
        cur = f(2 * math.pi * i / n)
        if (prev[0] < 0) != (cur[0] < 0):
            out.append(((prev[1] + cur[1]) / 2, (prev[2] + cur[2]) / 2))
        prev = cur
    return out


def fmt(v):
    return f"{v:.1f}".rstrip("0").rstrip(".")


def neck_points(head, body):
    """Extremos del cuello: donde la cabeza se apoya en el cuerpo o, sin cuello, la línea del pañuelo de la app."""
    hx, hy, hrx, hry = head
    if body is None:
        y = hy + hry * 0.42
        half = hrx * math.sqrt(1 - 0.42 * 0.42)
        return (hx - half, y), (hx + half, y)
    meets = sorted(p for p in ellipse_meet(head, body) if p[1] > hy)  # los de abajo de la cabeza
    return meets[0], meets[-1]


def sheet(title, art, head, body, fur, note=None):
    hx, hy, hrx, hry = head
    (x1, y1), (x2, y2) = neck_points(head, body)
    nx, ny = (x1 + x2) / 2, (y1 + y2) / 2
    scale = PX / 200
    f = 'font-family="Nunito, Segoe UI, sans-serif"'
    m = []
    # centro de la cabeza: cruz y elipse punteada
    m.append(f'<ellipse cx="{hx}" cy="{hy}" rx="{hrx}" ry="{hry}" fill="none" stroke="{MARK}" stroke-width="0.8" stroke-dasharray="3 2"/>')
    m.append(f'<path d="M{hx-9} {hy} H{hx+9} M{hx} {hy-9} V{hy+9}" stroke="{MARK}" stroke-width="1.4"/>')
    m.append(f'<circle cx="{hx}" cy="{hy}" r="2.2" fill="{MARK}"/>')
    # cuello: línea donde la cabeza se apoya en el cuerpo y su punto medio
    m.append(f'<path d="M{x1:.1f} {y1:.1f} L{x2:.1f} {y2:.1f}" stroke="{NECK}" stroke-width="1.4" stroke-dasharray="3 2"/>')
    for x, y in ((x1, y1), (x2, y2)):
        m.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="1.6" fill="{NECK}"/>')
    m.append(f'<circle cx="{nx:.1f}" cy="{ny:.1f}" r="2.6" fill="{NECK}" stroke="#fff" stroke-width="0.8"/>')
    # dónde se midió el contorno
    ex, ey = hx - hrx, hy  # borde izquierdo de la cabeza
    m.append(f'<circle cx="{ex}" cy="{ey}" r="5" fill="none" stroke="{MARK}" stroke-width="0.8"/>')
    # leyenda
    neck_name = "Cuello" if body is not None else "Sin cuello, línea del pañuelo"
    lines = [
        (G.INK, f"Contorno: {STROKE} u ≈ {fmt(STROKE * scale)} px acá · #4A3228, punta y unión redondas"),
        (MARK, f"Centro de la cabeza: ({fmt(hx)}, {fmt(hy)}) · elipse {fmt(hrx * 2)} × {fmt(hry * 2)} u"),
        (NECK, f"{neck_name}: ({fmt(nx)}, {fmt(ny)}) · x {fmt(x1)} a {fmt(x2)}"),
        ("#6D6D6D", "Lienzo de gen.py 200 × 240 u, origen arriba a la izq. · 1 u = 2,56 px"),
    ]
    leg = [f'<text x="6" y="251" {f} font-size="7" font-weight="900" fill="{G.INK}">{title}</text>']
    for i, (col, t) in enumerate(lines):
        y = 261 + i * 9
        leg.append(f'<circle cx="9" cy="{y - 2}" r="2.2" fill="{col}"/>')
        leg.append(f'<text x="14" y="{y}" {f} font-size="5" font-weight="700" fill="{G.INK}">{t}</text>')
    # contorno agrandado ×4: afuera | contorno | adentro (pelaje)
    k = 4
    bx, by = 8, 297
    w = STROKE * k
    leg.append(f'<rect x="{bx + 14}" y="{by}" width="{w + 14}" height="14" fill="{fur}"/>')
    leg.append(f'<rect x="{bx + 14}" y="{by}" width="{w}" height="14" fill="{G.INK}"/>')
    leg.append(f'<rect x="{bx}" y="{by}" width="{w + 28}" height="14" rx="2" fill="none" stroke="{MARK}" stroke-width="0.7"/>')
    leg.append(f'<text x="{bx + w + 34}" y="{by + 6}" {f} font-size="5" font-weight="800" fill="{MARK}">Contorno ×4 (el círculo azul marca dónde)</text>')
    if note:
        leg.append(f'<text x="{bx + w + 34}" y="{by + 13}" {f} font-size="5" font-weight="700" fill="#6D6D6D">{note}</text>')
    h = int(PX * VIEW_H / VIEW_W)
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 {VIEW_Y} {VIEW_W} {VIEW_H}" width="{PX}" height="{h}">'
            f'<rect y="{VIEW_Y}" width="{VIEW_W}" height="{VIEW_H}" fill="#FFF8EF"/>'
            f'{G.shadow()}<g {G.W}>{art}</g>{"".join(m)}{"".join(leg)}</svg>')


def model_art(enum, th):
    return M.render(M.build(M.all_pets()[enum]), th)


def fur_color(enum):
    model = M.build(M.all_pets()[enum])
    return next(e["fill"] for e in model["parts"] if e["kind"] == "vol" and e["layer"] == M.HEAD)


# Archivo, título, enum, dibujos y elipses de cabeza y cuerpo (None: una sola pieza, sin cuello).
LION_NOTE = "La cabeza es el óvalo interior; la melena va por fuera"
BIRD_NOTE = "Cabeza y cuerpo son una sola pieza"
PETS = [
    ("perro", "Perro", "DOG", G.dog, G.dog_side, (100, 98, 68, 60), BODY_FRONT, (112, 98, 62, 58), BODY_SIDE, None),
    ("gato", "Gato", "CAT", G.cat, G.cat_side, (100, 98, 68, 60), BODY_FRONT, (112, 98, 62, 58), BODY_SIDE, None),
    ("pato", "Pato", "DUCK", G.duck, G.duck_side, (100, 98, 66, 60), BODY_FRONT, (116, 100, 58, 56), (100, 178, 50, 36), None),
    ("rana", "Rana", "FROG", G.frog, G.frog_side, (100, 108, 74, 50), (100, 178, 42, 36), (116, 112, 60, 46), (96, 178, 46, 34), None),
    ("vaca", "Vaca", "COW", G.cow, G.cow_side, (100, 98, 68, 60), BODY_FRONT, (112, 98, 60, 56), BODY_SIDE, None),
    ("chancho", "Chancho", "PIG", G.pig, G.pig_side, (100, 98, 68, 60), BODY_FRONT, (112, 98, 62, 58), BODY_SIDE, None),
    ("pollito", "Pollito", "CHICK", G.chick, G.chick_side, (100, 132, 68, 80), None, (100, 132, 66, 80), None, BIRD_NOTE),
    ("leon", "León", "LION", G.lion, G.lion_side, (100, 100, 58, 52), BODY_FRONT, (120, 100, 52, 48), BODY_SIDE, LION_NOTE),
    ("mono", "Mono", "MONKEY", G.monkey, G.monkey_side, (100, 98, 64, 58), BODY_FRONT, (112, 98, 60, 56), BODY_SIDE, None),
    ("buho", "Búho", "OWL", G.owl, G.owl_side, (100, 130, 70, 82), None, (100, 130, 64, 82), None, BIRD_NOTE),
    ("conejo", "Conejo", "RABBIT", lambda: model_art("RABBIT", 0), lambda: model_art("RABBIT", 90),
     (100, 104, 70, 60), BODY_FRONT, (106, 104, 64, 58), BODY_SIDE, "Dibujo del modelo 2,5D (no está en gen.py)"),
]

os.makedirs(OUT, exist_ok=True)
for name, title, enum, front, side, head_f, body_f, head_s, body_s, note in PETS:
    fur = fur_color(enum)
    for view, label, art, head, body in (("frente", "de frente", front, head_f, body_f), ("perfil", "de perfil", side, head_s, body_s)):
        path = os.path.join(OUT, f"{name}-{view}.svg")
        with open(path, "w", encoding="utf-8") as fh:
            fh.write(sheet(f"{title} {label}", art(), head, body, fur, note))
        print(os.path.normpath(path))
