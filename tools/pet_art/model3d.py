"""
Modelos "2,5D" de las mascotas: cada parte (cabeza, cuerpo, ojos, orejas, hocico, patas, cola...) es un
óvalo o un trazo con su posición en 3D alrededor del eje vertical de la mascota. Así se puede dibujar a
la mascota en cualquier ángulo (giro continuo, girar solo la cabeza) y mover las patas por separado.

Una sola descripción por mascota (PETS, más abajo) sirve para:
  - la vista previa (SVG), para mirar y aprobar los modelos sin compilar la app;
  - el código de la app (PetModels.kt, generado por gen_kotlin()): la app dibuja con el mismo motor.

Coordenadas: el lienzo de los dibujos (200×240). x y z se miden desde el eje de la mascota (x = 100 en
el lienzo); z es la profundidad (positivo = hacia quien mira, de frente). y es la del lienzo.
Ángulo: 0 de frente, 90 mirando a la derecha, 180 de espaldas.
"""
import math

INK = "#4A3228"
BLUSH = "#FF9EAE"
WHITE = "#FFFFFF"

BODY, HEAD = 10, 20  # capas: lo que pertenece al cuerpo y lo que pertenece a la cabeza


def vary(v, sa):
    """Valor que cambia de frente (a) a perfil (b): (a, b) → a + (b - a)·|sen θ|."""
    if isinstance(v, tuple):
        return v[0] + (v[1] - v[0]) * sa
    return v


# ---------------------------------------------------------------- piezas

def vol(group, x, y, z, rx, ry, fill, **kw):
    """Volumen (cabeza, cuerpo): óvalo que siempre se ve; su ancho pasa de rx (frente) a rx de perfil."""
    return dict(kind="vol", layer=group, x=x, y=y, z=z, rx=rx, ry=ry, fill=fill, **kw)


def part(group, x, y, z, rx, ry, fill, rot=0, behind=-0.35, **kw):
    """
    Óvalo con volumen propio (orejas, brazos, patas, hocico): se mueve con el giro y pasa detrás del
    volumen de su grupo cuando su orientación baja de [behind]. La rotación se achica al pasar al centro.
    flat=True: pegado a la superficie (ojos, mejillas, manchas): se angosta hacia el borde y [hide]
    lo esconde cuando mira demasiado hacia atrás.
    """
    return dict(kind="part", layer=group, x=x, y=y, z=z, rx=rx, ry=ry, fill=fill, rot=rot, behind=behind, **kw)


def eye(x, y, z, r=(10, 12), hide=0.22):
    return dict(kind="eye", layer=HEAD, x=x, y=y, z=z, r=r, hide=hide)


def path(group, pts, d, fill="none", width=4, color=INK, behind=-0.35, hide=None, **kw):
    """
    Trazo con puntos en 3D: [d] es una plantilla de path SVG con {0}, {1}... en lugar de "x y".
    hide: se esconde cuando la orientación promedio de sus puntos baja de ese valor.
    """
    return dict(kind="path", layer=group, pts=pts, d=d, fill=fill, width=width, color=color, behind=behind, hide=hide, **kw)


def tail(base, tip, fill, width=9, bend=8, y=(190, 142)):
    """Cola con forma de hoja de la base a la punta (en 3D: base y tip son (x, z))."""
    return dict(kind="tail", layer=BODY, base=base, tip=tip, fill=fill, width=width, bend=bend, ty=y)


def rope_tail(pts, color, width=10):
    """Cola de cuerda (león, mono, vaca): un trazo grueso con borde."""
    return dict(kind="rope", layer=BODY, pts=pts, color=color, width=width)


def round_pts(pts, R=70.0, zoff=-30.0):
    """
    Puntos del lienzo (X, Y) de orejas y cuernos: les da volumen (z según su distancia al eje), así de
    perfil no quedan como una rayita.
    """
    return [(X - 100, Y, math.sqrt(max(0.0, R * R - (X - 100) ** 2)) + zoff) for X, Y in pts]


def on_head(x, y, cy=98, rx=64, ry=60, cz=0, r=64):
    """z de un punto de la cara apoyado sobre la cabeza (un elipsoide)."""
    u = (x / rx) ** 2 + ((y - cy) / ry) ** 2
    return cz + r * math.sqrt(max(0.0, 1 - u))


# ---------------------------------------------------------------- motor (SVG)

def f1(v):
    return f"{v:.1f}"


class Frame:
    def __init__(self, th):
        self.th = th
        self.s = math.sin(math.radians(th))
        self.c = math.cos(math.radians(th))
        self.sa = abs(self.s)

    def proj(self, x, z):
        """Gira (x, z) alrededor del eje vertical: devuelve x en el lienzo y la profundidad."""
        return 100 + x * self.c + z * self.s, -x * self.s + z * self.c

    def facing(self, x, z):
        """Cuánto mira hacia quien observa la superficie en (x, z): 1 de frente, −1 de espaldas."""
        n = math.hypot(x, z)
        if n < 1e-6:
            return self.c
        return (-x * self.s + z * self.c) / n


def ellipse_svg(cx, cy, rx, ry, fill, rot=0.0, stroke=True, opacity=1.0):
    t = f' transform="rotate({f1(rot)} {f1(cx)} {f1(cy)})"' if abs(rot) > 0.05 else ""
    st = "" if stroke else ' stroke="none"'
    op = f' opacity="{opacity}"' if opacity < 1 else ""
    return f'<ellipse cx="{f1(cx)}" cy="{f1(cy)}" rx="{f1(max(rx, 0.01))}" ry="{f1(ry)}" fill="{fill}"{t}{st}{op}></ellipse>'


def render(model, th, face="open", pose=None, head=None):
    """
    SVG (solo el contenido, en el lienzo 200×240) de la mascota girada [th] grados. [head]: ángulo de la
    cabeza si gira distinto que el cuerpo. [pose]: {tag: {"rot": grados desde el hombro, "dy": px}}.
    """
    FB = Frame(th)
    FH = Frame(th if head is None else head)
    pose = pose or {}
    items = []  # (clave de orden, profundidad, svg)
    clips = {}
    for el in model["parts"]:
        if el["kind"] == "vol":
            F = FH if el["layer"] == HEAD else FB
            sa = F.sa
            clips["body" if el["layer"] == BODY else "head"] = (
                F.proj(vary(el["x"], sa), vary(el["z"], sa))[0], vary(el["y"], sa), vary(el["rx"], sa) - 2, vary(el["ry"], sa) - 2)
    for el in model["parts"]:
        k = el["kind"]
        F = FH if el.get("layer") == HEAD else FB
        sa = F.sa
        dy_pose = pose.get(el.get("tag"), {}).get("dy", 0) if el.get("tag") else 0
        drot_pose = pose.get(el.get("tag"), {}).get("rot", 0) if el.get("tag") else 0
        if k == "vol":
            x, z = vary(el["x"], sa), vary(el["z"], sa)
            px, pz = F.proj(x, z)
            items.append((el["layer"], 0, ellipse_svg(px, vary(el["y"], sa), vary(el["rx"], sa), vary(el["ry"], sa), el["fill"],
                                                    stroke=el.get("stroke", True))))
        elif k in ("part", "eye"):
            x, z = vary(el["x"], sa), vary(el["z"], sa)
            px, pz = F.proj(x, z)
            fz = F.facing(x, z)
            if el.get("hide") is not None and fz < el["hide"]:
                continue
            y = vary(el["y"], sa) + dy_pose
            if k == "eye":
                kk = max(fz, 0) ** 0.6
                r = el["r"]
                if face == "open":
                    svg = (ellipse_svg(px, y, r[0] * kk, r[1], INK, stroke=False) +
                           f'<circle cx="{f1(px - 3 * kk)}" cy="{f1(y - 5)}" r="{f1(r[0] * 0.4 * kk)}" fill="{WHITE}" stroke="none"></circle>'
                           f'<circle cx="{f1(px + 4 * kk)}" cy="{f1(y + 4)}" r="{f1(1.8 * kk)}" fill="{WHITE}" stroke="none"></circle>')
                elif face == "happy":
                    svg = f'<path d="M{f1(px - r[0] * kk)} {f1(y + 3)} Q{f1(px)} {f1(y - 10)} {f1(px + r[0] * kk)} {f1(y + 3)}" fill="none" stroke-width="4.5"></path>'
                else:
                    svg = f'<path d="M{f1(px - r[0] * kk)} {f1(y - 2)} Q{f1(px)} {f1(y + 7)} {f1(px + r[0] * kk)} {f1(y - 2)}" fill="none" stroke-width="4.5"></path>'
                items.append((HEAD + 0.5, pz, svg))
                continue
            rx = vary(el["rx"], sa)
            if el.get("flat"):
                rx *= max(fz, 0) ** el.get("p", 0.7)
            rot0 = vary(el["rot"], sa)
            rot = rot0 * (px - 100) / x if abs(x) > 1 else rot0 * (1 - sa)
            key = el["layer"] + (-0.5 if fz < el["behind"] else 0.5)
            e_svg = ellipse_svg(px, y, rx, vary(el["ry"], sa), el["fill"], rot, stroke=el.get("stroke", True), opacity=el.get("opacity", 1.0))
            if el.get("clip") in clips:
                cid = f"c{id(el)}{int(th * 10)}"
                e_svg = (f'<clipPath id="{cid}">{ellipse_svg(*clips[el["clip"]], "#000", stroke=False)}</clipPath>'
                         f'<g clip-path="url(#{cid})">{e_svg}</g>')
                key = el["layer"] + 0.1  # justo encima de su volumen
            if drot_pose:
                ry_ = vary(el["ry"], sa)
                e_svg = f'<g transform="rotate({f1(drot_pose)} {f1(px)} {f1(y - ry_ * 0.8)})">{e_svg}</g>'
                # brazo levantado: pasa por delante de la cabeza (si no, la cabeza lo tapa)
                if abs(drot_pose) > 60 and fz >= el["behind"]:
                    key = HEAD + 0.6
            items.append((key, pz, e_svg))
        elif k == "path":
            if el.get("bb"):
                ax, az = el["bb"]
                apx, apz = F.proj(ax, az)
                pts = [(apx + (p[0] - ax), apz, p[1]) for p in el["pts"]]
                fz = F.facing(ax, az) if (ax or az) else 1.0
            else:
                pts = [F.proj(p[0], p[2]) + (p[1],) for p in el["pts"]]
                fz = sum(F.facing(p[0], p[2]) for p in el["pts"]) / len(el["pts"])
            if el["hide"] is not None and fz < el["hide"]:
                continue
            d = el["d"].format(*[f"{f1(px)} {f1(y + dy_pose)}" for px, _, y in pts])
            stroke = "" if el["color"] == INK else f' stroke="{el["color"]}"'
            if el["width"] == 0:
                stroke = ' stroke="none"'
            w = f' stroke-width="{el["width"]}"' if el["width"] not in (0, 4) else ""
            key = el["layer"] + (-0.5 if fz < el["behind"] else 0.5)
            items.append((key, sum(p[1] for p in pts) / len(pts), f'<path d="{d}" fill="{el["fill"]}"{stroke}{w}></path>'))
        elif k == "tail":
            (bx3, bz3), (tx3, tz3) = el["base"], el["tip"]
            bx, bz = F.proj(bx3, bz3)
            tx, tz = F.proj(tx3, tz3)
            by, ty = el["ty"]
            dx, dy = tx - bx, ty - by
            ln = math.hypot(dx, dy) or 1
            nx, ny = -dy / ln, dx / ln
            w = el["width"]
            side = 1 if dx >= 0 else -1
            c1 = (bx + dx * .5 + nx * (w + 14 * side * .2) + el["bend"] * side, by + dy * .5 + ny * w + 6)
            c2 = (bx + dx * .5 - nx * w + el["bend"] * side, by + dy * .5 - ny * w + 6)
            d = (f"M{f1(bx + nx * w)} {f1(by + ny * w)} Q{f1(c1[0])} {f1(c1[1])} {f1(tx)} {f1(ty)} "
                 f"Q{f1(c2[0])} {f1(c2[1])} {f1(bx - nx * w)} {f1(by - ny * w)} Z")
            zz = (bz + tz) / 2
            items.append((el["layer"] + (0.5 if zz >= 0 else -0.5), zz, f'<path d="{d}" fill="{el["fill"]}"></path>'))
        elif k == "rope":
            pts = [F.proj(p[0], p[2]) + (p[1],) for p in el["pts"]]
            d = "M{0} Q{1} {2}".format(*[f"{f1(px)} {f1(y)}" for px, _, y in pts[:3]])
            if len(pts) > 3:
                d = "M{0} C{1} {2} {3}".format(*[f"{f1(px)} {f1(y)}" for px, _, y in pts[:4]])
            zz = sum(p[1] for p in pts) / len(pts)
            w = el["width"]
            items.append((el["layer"] + (0.5 if zz >= 0 else -0.5), zz,
                          f'<path d="{d}" fill="none" stroke-width="{w}"></path><path d="{d}" fill="none" stroke="{el["color"]}" stroke-width="{w - 6}"></path>'))
        elif k == "mane":
            # Melena: anillo de festones alrededor de la cabeza, detrás de ella (de espaldas, delante).
            cx, cy, R, n = F.proj(0, el["z"])[0], el["y"], el["r"], 14
            pts = [(cx + R * math.cos(2 * math.pi * i / n - math.pi / 2), cy + R * math.sin(2 * math.pi * i / n - math.pi / 2)) for i in range(n)]
            rr = R * math.sin(math.pi / n) * 1.15
            d = f"M{f1(pts[0][0])} {f1(pts[0][1])} " + " ".join(
                f"A{f1(rr)} {f1(rr)} 0 0 1 {f1(pts[(i + 1) % n][0])} {f1(pts[(i + 1) % n][1])}" for i in range(n)) + " Z"
            items.append((HEAD - 0.5, -999, f'<path d="{d}" fill="{el["fill"]}"></path>'))
    items.sort(key=lambda t: (t[0], t[1]))
    return "".join(svg for _, _, svg in items)


def svg(model, th, size=150, face="open", pose=None):
    h = int(size * 1.2)
    return (f'<svg viewBox="0 0 200 240" width="{size}" height="{h}" xmlns="http://www.w3.org/2000/svg" style="overflow:visible">'
            f'<ellipse cx="100" cy="228" rx="54" ry="7" fill="{INK}" opacity=".12" stroke="none"></ellipse>'
            f'<g stroke="{INK}" stroke-width="4" stroke-linejoin="round" stroke-linecap="round">{render(model, th, face, pose)}</g></svg>')


# ---------------------------------------------------------------- piezas comunes

SIT = False  # build(sit=True): las piezas comunes salen en la pose sentada "peluche"
PAD = "#FFC9D2"
SIT_HEAD_DY = 8
_LEGS_USED = [False]


def legs(main, feet, arms=None, feet_far=None, belly=None, arm_rot=22):
    """Cuerpo de cuatro patas parado: patas en un anillo en el piso, brazos al costado y panza."""
    arms = arms or main
    feet_far = feet_far or feet
    _LEGS_USED[0] = True
    if SIT:
        return sit_legs(main, feet, arms, belly)
    out = []
    for i, phi in enumerate((-35, 35, -145, 145)):
        x, z = 30 * math.sin(math.radians(phi)), 30 * math.cos(math.radians(phi))
        tag = ("footL", "footR", "backL", "backR")[i]
        # siempre detrás del cuerpo; la de atrás se ve de perfil y de espaldas
        out.append(part(BODY, x, 213 if z > 0 else 212, z, 15 if z > 0 else 14, 10 if z > 0 else 9, feet if z > 0 else feet_far,
                        behind=2, hide=-0.7, tag=tag))
    for side, tag in ((-1, "armL"), (1, "armR")):
        x, z = side * 38 * math.sin(math.radians(58)), 38 * math.cos(math.radians(58))
        out.append(part(BODY, x, 181, z, 10, 15, arms, rot=-side * arm_rot, behind=-0.1, tag=tag))
    if belly:
        out.append(part(BODY, 10 * 0, (186, 196), 38, (25, 30), (22, 11), belly, flat=True, p=0.5, hide=-0.3, stroke=False,
                        clip="body", behind=-2))
    return out


def body(main, z=(0, -4), rx=(42, 48), ry=(38, 34), y=(176, 178)):
    if SIT:
        return vol(BODY, 0, (186, 188), z, (42, 46), (34, 32), main)
    return vol(BODY, 0, y, z, rx, ry, main)


def sit_legs(main, feet, arms, belly):
    """Sentada como peluche: patas estiradas hacia adelante (plantitas rosadas) y bracitos al costado."""
    out = []
    if belly:
        out.append(part(BODY, 0, (192, 196), 36, (24, 26), (20, 12), belly, flat=True, p=0.5, hide=-0.3, stroke=False, clip="body", behind=-2))
    for side, tag in ((-1, "armL"), (1, "armR")):
        x, z = side * 38 * math.sin(math.radians(62)), 38 * math.cos(math.radians(62))
        out.append(part(BODY, x, 186, z, 10, 15, arms, rot=-side * 22, behind=-0.1, tag=tag))
    for side, tag in ((-1, "footL"), (1, "footR")):
        x = side * 28
        out.append(part(BODY, x, 212, 30, 18, 13, feet, behind=-0.2, tag=tag))
        out.append(part(BODY, x, 214, 44, 8, 6, PAD, flat=True, p=0.5, hide=0.1, stroke=False, behind=-0.2, tag=tag))
        for dx, yy in ((-9, 207), (0, 205), (9, 207)):
            out.append(part(BODY, x + dx, yy, 43, 2.6, 2.6, PAD, flat=True, p=0.5, hide=0.1, stroke=False, behind=-0.2, tag=tag))
    return out


def spot_parts(c, head=(98, 64, 60, 6), body_geo=(176, 42, 38)):
    """
    Manchas del pelaje (c["spots"]): ("head" | "body", X, Y, rx, ry, color, atrás?) en coordenadas del
    lienzo de frente. Van pegadas a la superficie y recortadas con la cabeza o el cuerpo; las de atrás
    se ven al girar. Sentada, las del cuerpo siguen al cuerpo (más bajo y más corto).
    """
    out = []
    hcy, hrx, hry, hcz = head
    bcy, brx, bry = body_geo
    for group, X, Y, rx, ry, color, back in c.get("spots", ()):
        x = X - 100
        if group == "body":
            if SIT:
                Y = 186 + (Y - bcy) * 34 / bry
                cy_, ry_ = 186, 34
            else:
                cy_, ry_ = bcy, bry
            z = on_head(x, Y, cy_, brx, ry_, 0, 40)
            layer, clip = BODY, "body"
        else:
            z = on_head(x, Y, hcy, hrx, hry, hcz, 64)
            layer, clip = HEAD, "head"
        if back:
            z = -z
        out.append(part(layer, x, Y, z, rx, ry, color, flat=True, p=0.6, hide=0.0, stroke=False, clip=clip))
    return out


def build(fn, sit=False, colors=None):
    """Modelo de una mascota parada o sentada (la cabeza baja SIT_HEAD_DY al sentarse), con su pelaje."""
    global SIT
    SIT = sit
    _LEGS_USED[0] = False
    try:
        m = fn(colors) if colors else fn()
    finally:
        SIT = False
    if sit and _LEGS_USED[0]:
        for el in m["parts"]:
            if el.get("layer") != HEAD:
                continue
            if "y" in el:
                y = el["y"]
                el["y"] = (y[0] + SIT_HEAD_DY, y[1] + SIT_HEAD_DY) if isinstance(y, tuple) else y + SIT_HEAD_DY
            if "pts" in el:
                el["pts"] = [(a, b + SIT_HEAD_DY, c) for a, b, c in el["pts"]]
    m["sit"] = sit
    return m


def blush2(x, y, z_head=0, r=(11, 6.5)):
    return [part(HEAD, sx * x, y, on_head(sx * x, y, cz=z_head), r[0], r[1], BLUSH, flat=True, hide=0.05, stroke=False, opacity=0.65)
            for sx in (-1, 1)]


def eyes2(x, y, z_head=0, r=(10, 12)):
    return [eye(sx * x, y, on_head(sx * x, y, cz=z_head), r) for sx in (-1, 1)]


# ---------------------------------------------------------------- mascotas

def dog(c=None):
    c = c or dict(main="#F2C38B", light="#FFF4E2", dark="#C98A55")
    main, light, dark = c["main"], c["light"], c["dark"]
    hz = (0, 6)  # la cabeza se adelanta un poco de perfil
    p = [tail((0, -34), (56, -56), main)]
    p += legs(main, light, belly=light)
    p.append(body(main))
    p.append(vol(HEAD, 0, 98, hz, (68, 62), (60, 58), main))
    for sx in (-1, 1):  # orejas caídas a los costados
        x, z = sx * 64 * math.sin(math.radians(100)), 64 * math.cos(math.radians(100))
        p.append(part(HEAD, x, 97, z + 6, 15, 31, dark, rot=-sx * 18 * (63 / 62)))
    p.append(path(HEAD, [(0, 40, 44), (8, 52, 44), (0, 60, 46), (-8, 52, 44)], "M{0} Q{1} {2} Q{3} {0} Z",
                  fill=light, width=0, hide=0.05))
    p.append(path(HEAD, [(-6, 50, -46), (0, 40, -48), (6, 50, -46)], "M{0} Q{1} {2}", color=dark, hide=0.05))
    p += blush2(44, 124, 6)
    p += eyes2(28, 100, 6)
    p.append(part(HEAD, 0, (126, 121), 60, (25, 24), (18, 17), light, behind=-0.1))   # hocico
    p.append(part(HEAD, 0, (116, 112), 77, (8, 6.5), 6, INK, behind=-0.1, stroke=False))  # nariz
    # boca y lengüita (se ven de frente y de tres cuartos)
    p.append(path(HEAD, [(0, 122, 74), (0, 127, 74)], "M{0} L{1}", width=3.5, hide=0.15))
    p.append(path(HEAD, [(-3, 130, 74), (0, 138, 74), (3, 130, 74)], "M{0} Q{1} {2}", fill="#FF8FA3", width=2.5, hide=0.15))
    p.append(path(HEAD, [(-9, 127, 74), (-4.5, 133, 74), (0, 127, 74), (4.5, 133, 74), (9, 127, 74)], "M{0} Q{1} {2} Q{3} {4}",
                  width=3.5, hide=0.15))
    p += spot_parts(c)
    return dict(name="Perro", parts=p)


PETS = {"DOG": dog}


def all_pets():
    """Las 10 mascotas, en el orden de PetType."""
    from model3d_pets import MORE
    m = dict(PETS)
    m.update(MORE)
    return {k: m[k] for k in ("DOG", "CAT", "DUCK", "FROG", "COW", "PIG", "CHICK", "LION", "MONKEY", "OWL")}


if __name__ == "__main__":
    import sys
    out = sys.argv[1]
    rows = ""
    for enum, fn in all_pets().items():
        m = fn()
        cells = "".join(f'<div style="text-align:center;font:700 11px sans-serif">{svg(m, a, 110)}<div>{a}°</div></div>'
                        for a in range(0, 360, 30))
        rows += f'<div style="display:flex;gap:2px;align-items:flex-end">{cells}</div>'
    open(out, "w", encoding="utf-8").write(f'<html><body style="background:#FFF8EF;margin:8px">{rows}</body></html>')
    print("ok")
