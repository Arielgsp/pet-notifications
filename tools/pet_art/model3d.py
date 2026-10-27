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


def eye(x, y, z, r=(10, 12), hide=0.22, kawaii=False):
    """Ojo. kawaii: brillo grande arriba y uno chico abajo, más notorios (mirada "tierna")."""
    return dict(kind="eye", layer=HEAD, x=x, y=y, z=z, r=r, hide=hide, kawaii=kawaii)


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


# Operaciones de dibujo: draw_ops() arma la lista (ya ordenada) y render() la pasa a SVG. La app dibuja
# con el mismo motor (overlay/PetModel.kt) y su test compara sus operaciones con estas (gen_kotlin.py
# escribe los casos de prueba), así la vista previa y la app no se separan.
#   ell:  óvalo (cx, cy, rx, ry, fill, rot, stroke, opacity)
#   circ: círculo sin borde (cx, cy, r, fill)
#   path: trazo; segs = [(comando, [x, y, ...])] con comandos M Q L C Z; fill "none" = sin relleno;
#         color = color del borde; w = ancho (0 = sin borde)
#   mane: melena (anillo de n festones de radio rr alrededor de (cx, cy), a distancia R)
# Cualquiera puede llevar clip=(cx, cy, rx, ry) (recortada con ese óvalo) y turn=(grados, px, py)
# (girada alrededor de ese punto, con recorte y todo).

def op_ell(cx, cy, rx, ry, fill, rot=0.0, stroke=True, opacity=1.0):
    return dict(t="ell", cx=cx, cy=cy, rx=rx, ry=ry, fill=fill, rot=rot, stroke=stroke, opacity=opacity)


def op_circ(cx, cy, r, fill):
    return dict(t="circ", cx=cx, cy=cy, r=r, fill=fill)


def op_path(segs, fill="none", color=INK, w=4, w_attr=None):
    return dict(t="path", segs=segs, fill=fill, color=color, w=w, w_attr=w_attr)


def template_segs(d, pts):
    """Plantilla de path ("M{0} Q{1} {2} Z") y sus puntos (x, y) → segmentos [(comando, [x, y, ...])]."""
    segs = []
    for tok in d.split():
        if tok[0].isalpha():
            segs.append((tok[0], []))
            tok = tok[1:]
        if tok:
            segs[-1][1].extend(pts[int(tok.strip("{}"))])
    return segs


def d_svg(segs):
    out = []
    for cmd, xs in segs:
        out.append(cmd + " ".join(f"{f1(xs[i])} {f1(xs[i + 1])}" for i in range(0, len(xs), 2)))
    return " ".join(out)


def op_svg(o):
    t = o["t"]
    if t == "ell":
        s = ellipse_svg(o["cx"], o["cy"], o["rx"], o["ry"], o["fill"], o["rot"], o["stroke"], o["opacity"])
    elif t == "circ":
        s = f'<circle cx="{f1(o["cx"])}" cy="{f1(o["cy"])}" r="{f1(o["r"])}" fill="{o["fill"]}" stroke="none"></circle>'
    elif t == "path":
        stroke = "" if o["color"] == INK else f' stroke="{o["color"]}"'
        if o["w"] == 0:
            stroke = ' stroke="none"'
        show_w = o["w_attr"] if o["w_attr"] is not None else o["w"] not in (0, 4)
        w = f' stroke-width="{o["w"]}"' if show_w else ""
        s = f'<path d="{d_svg(o["segs"])}" fill="{o["fill"]}"{stroke}{w}></path>'
    else:  # mane
        cx, cy, R, n, rr = o["cx"], o["cy"], o["R"], o["n"], o["rr"]
        pts = [(cx + R * math.cos(2 * math.pi * i / n - math.pi / 2), cy + R * math.sin(2 * math.pi * i / n - math.pi / 2)) for i in range(n)]
        d = f"M{f1(pts[0][0])} {f1(pts[0][1])} " + " ".join(
            f"A{f1(rr)} {f1(rr)} 0 0 1 {f1(pts[(i + 1) % n][0])} {f1(pts[(i + 1) % n][1])}" for i in range(n)) + " Z"
        s = f'<path d="{d}" fill="{o["fill"]}"></path>'
    if o.get("clip"):
        cid = f"c{id(o)}"
        s = (f'<clipPath id="{cid}">{ellipse_svg(*o["clip"], "#000", stroke=False)}</clipPath>'
             f'<g clip-path="url(#{cid})">{s}</g>')
    if o.get("turn"):
        a, px, py = o["turn"]
        s = f'<g transform="rotate({f1(a)} {f1(px)} {f1(py)})">{s}</g>'
    return s


def draw_ops(model, th, face="open", pose=None, head=None, layer=None):
    """
    Operaciones de dibujo (en orden, en el lienzo 200×240) de la mascota girada [th] grados. [head]:
    ángulo de la cabeza si gira distinto que el cuerpo. [pose]: {tag: {"rot": grados desde el hombro, "dy": px}}.
    [layer]: solo las piezas de esa capa (HEAD: la cabeza sola, para dibujarla sobre el cuerpo sentado fijo).
    """
    FB = Frame(th)
    FH = Frame(th if head is None else head)
    pose = pose or {}
    items = []  # (clave de orden, profundidad, [operaciones])
    clips = {}
    for el in model["parts"]:
        if el["kind"] == "vol":
            F = FH if el["layer"] == HEAD else FB
            sa = F.sa
            clips["body" if el["layer"] == BODY else "head"] = (
                F.proj(vary(el["x"], sa), vary(el["z"], sa))[0], vary(el["y"], sa), vary(el["rx"], sa) - 2, vary(el["ry"], sa) - 2)
    for el in model["parts"]:
        k = el["kind"]
        if layer is not None and el.get("layer") != layer:
            continue
        F = FH if el.get("layer") == HEAD else FB
        sa = F.sa
        dy_pose = pose.get(el.get("tag"), {}).get("dy", 0) if el.get("tag") else 0
        drot_pose = pose.get(el.get("tag"), {}).get("rot", 0) if el.get("tag") else 0
        if k == "vol":
            x, z = vary(el["x"], sa), vary(el["z"], sa)
            px, pz = F.proj(x, z)
            items.append((el["layer"], 0, [op_ell(px, vary(el["y"], sa), vary(el["rx"], sa), vary(el["ry"], sa), el["fill"],
                                                  stroke=el.get("stroke", True))]))
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
                if face == "open" and el.get("kawaii"):
                    ops = [op_ell(px, y, r[0] * kk, r[1], INK, stroke=False),
                           op_circ(px - r[0] * 0.3 * kk, y - r[1] * 0.38, r[0] * 0.48 * kk, WHITE),
                           op_circ(px + r[0] * 0.42 * kk, y + r[1] * 0.4, r[0] * 0.24 * kk, WHITE)]
                elif face == "open":
                    ops = [op_ell(px, y, r[0] * kk, r[1], INK, stroke=False),
                           op_circ(px - 3 * kk, y - 5, r[0] * 0.4 * kk, WHITE),
                           op_circ(px + 4 * kk, y + 4, 1.8 * kk, WHITE)]
                elif face == "happy":
                    ops = [op_path([("M", [px - r[0] * kk, y + 3]), ("Q", [px, y - 10, px + r[0] * kk, y + 3])], w=4.5)]
                else:
                    ops = [op_path([("M", [px - r[0] * kk, y - 2]), ("Q", [px, y + 7, px + r[0] * kk, y - 2])], w=4.5)]
                items.append((HEAD + 0.5, pz, ops))
                continue
            rx = vary(el["rx"], sa)
            if el.get("flat"):
                rx *= max(fz, 0) ** el.get("p", 0.7)
            rot0 = vary(el["rot"], sa)
            rot = rot0 * (px - 100) / x if abs(x) > 1 else rot0 * (1 - sa)
            key = el["layer"] + (-0.5 if fz < el["behind"] else 0.5)
            o = op_ell(px, y, rx, vary(el["ry"], sa), el["fill"], rot, stroke=el.get("stroke", True), opacity=el.get("opacity", 1.0))
            if el.get("clip") in clips:
                o["clip"] = clips[el["clip"]]
                key = el["layer"] + 0.1  # justo encima de su volumen
            base_rot = vary(el.get("base_rot", 0), sa)
            if base_rot and abs(x) > 1 and (px - 100) * x < 0:
                base_rot = -base_rot  # de espaldas la oreja quedó del otro lado: cae hacia afuera igual
            drot_pose += base_rot
            if drot_pose:
                ry_ = vary(el["ry"], sa)
                pvy = el["pivot_y"] + dy_pose if el.get("pivot_y") is not None else y - ry_ * 0.8
                o["turn"] = (drot_pose, px, pvy)
                # brazo levantado: pasa por delante de la cabeza (si no, la cabeza lo tapa)
                if abs(drot_pose) > 60 and fz >= el["behind"]:
                    key = HEAD + 0.6
            items.append((key, pz, [o]))
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
            o = op_path(template_segs(el["d"], [(px, y + dy_pose) for px, _, y in pts]), el["fill"], el["color"], el["width"])
            if drot_pose and el.get("pivot"):
                pvx, _ = F.proj(el["pivot"][0], el["pivot"][2])
                o["turn"] = (drot_pose, pvx, el["pivot"][1] + dy_pose)
            key = el["layer"] + (-0.5 if fz < el["behind"] else 0.5)
            items.append((key, sum(p[1] for p in pts) / len(pts), [o]))
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
            segs = [("M", [bx + nx * w, by + ny * w]), ("Q", [c1[0], c1[1], tx, ty]), ("Q", [c2[0], c2[1], bx - nx * w, by - ny * w]), ("Z", [])]
            zz = (bz + tz) / 2
            items.append((el["layer"] + (0.5 if zz >= 0 else -0.5), zz, [op_path(segs, el["fill"])]))
        elif k == "rope":
            pts = [F.proj(p[0], p[2]) + (p[1],) for p in el["pts"]]
            xy = [(px, y) for px, _, y in pts]
            segs = template_segs("M{0} C{1} {2} {3}" if len(pts) > 3 else "M{0} Q{1} {2}", xy)
            zz = sum(p[1] for p in pts) / len(pts)
            w = el["width"]
            items.append((el["layer"] + (0.5 if zz >= 0 else -0.5), zz,
                          [op_path(segs, w=w, w_attr=True), op_path(segs, color=el["color"], w=w - 6, w_attr=True)]))
        elif k == "mane":
            # Melena: anillo de festones alrededor de la cabeza, detrás de ella (de espaldas, delante).
            R, n = el["r"], 14
            items.append((HEAD - 0.5, -999, [dict(t="mane", cx=F.proj(el.get("x", 0), el["z"])[0], cy=el["y"], R=R, n=n,
                                                  rr=R * math.sin(math.pi / n) * 1.15, fill=el["fill"])]))
    items.sort(key=lambda t: (t[0], t[1]))
    return [o for _, _, ops in items for o in ops]


def render(model, th, face="open", pose=None, head=None):
    """SVG (solo el contenido, en el lienzo 200×240) de draw_ops()."""
    return "".join(op_svg(o) for o in draw_ops(model, th, face, pose, head))


def svg(model, th, size=150, face="open", pose=None):
    h = int(size * 1.2)
    return (f'<svg viewBox="0 0 200 240" width="{size}" height="{h}" xmlns="http://www.w3.org/2000/svg" style="overflow:visible">'
            f'<ellipse cx="100" cy="228" rx="54" ry="7" fill="{INK}" opacity=".12" stroke="none"></ellipse>'
            f'<g stroke="{INK}" stroke-width="4" stroke-linejoin="round" stroke-linecap="round">{render(model, th, face, pose)}</g></svg>')


# ---------------------------------------------------------------- piezas comunes

SIT = False  # build(sit=True): las piezas comunes salen en la pose sentada "peluche"
LIE = False  # build(lie=True): acostada de costado (cuerpo de perfil, cabeza de frente; ver lie_view)
PAD = "#FFC9D2"
SIT_HEAD_DY = 8
_LEGS_USED = [False]


def legs(main, feet, arms=None, feet_far=None, belly=None, arm_rot=22):
    """Cuerpo de cuatro patas parado: patas en un anillo en el piso, brazos al costado y panza."""
    arms = arms or main
    feet_far = feet_far or feet
    _LEGS_USED[0] = True
    if LIE:
        return lie_legs(feet)
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
    if LIE:
        return dict(vol(BODY, 0, LIE_Y, LIE_Z, (44, 62), (30, 28), main), lie=True)
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


# Acostada: se dibuja con el cuerpo de perfil (mirando a la izquierda, θ = 270) y la cabeza de frente
# (head = 0) al lado, como los dibujos fijos echados. El cuerpo se estira y se apoya en el piso; las patas
# de adelante quedan estiradas bajo la cabeza y la de atrás plegada. Lo demás del cuerpo (cola, manchas)
# se lleva del cuerpo parado al acostado (lie_point).
LIE_Y, LIE_Z = 200, -26        # centro del cuerpo acostado (de perfil: x = 100 - z en el lienzo)
LIE_HX, LIE_HY = -26, 54       # cuánto se corre la cabeza (de frente) respecto de la mascota parada
LIE_TH, LIE_HEAD = 270, 0      # ángulos para dibujarla (draw_ops(th=LIE_TH, head=LIE_HEAD))


def lie_legs(feet):
    out = []
    for tag, x, z in (("footL", -14, 24), ("footR", 14, 36)):  # de adelante, estiradas bajo la cabeza
        out.append(dict(part(BODY, x, 218, z, (12, 20), 9, feet, behind=-2, tag=tag), lie=True))
    out.append(dict(part(BODY, 22, 202, LIE_Z - 34, (14, 20), 19, "@haunch", behind=-0.35, tag="backR"), lie=True))
    out.append(dict(part(BODY, 24, 220, LIE_Z - 12, (10, 17), 8, feet, behind=-0.35, tag="backR"), lie=True))
    return out


def lie_point(y, z):
    """Punto del cuerpo parado (y, z) en el cuerpo acostado: más bajo, más largo y más corto de alto."""
    return LIE_Y + (y - 176) * 0.8, LIE_Z + z * 1.3


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
            if SIT and not LIE:  # acostada: _to_lie las lleva desde el cuerpo parado
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


def build(fn, sit=False, colors=None, lie=False):
    """
    Modelo de una mascota parada, sentada (la cabeza baja SIT_HEAD_DY) o acostada (lie: ver LIE_Y; las
    que no tienen patas de cuatro, como las aves, se acuestan como sentadas), con su pelaje.
    """
    global SIT, LIE
    SIT = sit or lie
    LIE = lie
    _LEGS_USED[0] = False
    try:
        m = fn(colors) if colors else fn()
    finally:
        SIT = LIE = False
    if lie and _LEGS_USED[0]:
        _to_lie(m["parts"], colors)
        m["lie"] = True
    elif (sit or lie) and _LEGS_USED[0]:
        for el in m["parts"]:
            if el.get("layer") != HEAD:
                continue
            if "y" in el:
                y = el["y"]
                el["y"] = (y[0] + SIT_HEAD_DY, y[1] + SIT_HEAD_DY) if isinstance(y, tuple) else y + SIT_HEAD_DY
            if el.get("pivot_y") is not None:
                el["pivot_y"] += SIT_HEAD_DY
            if el.get("pivot") is not None:
                px_, py_, pz_ = el["pivot"]
                el["pivot"] = (px_, py_ + SIT_HEAD_DY, pz_)
            if "pts" in el:
                el["pts"] = [(a, b + SIT_HEAD_DY, c) for a, b, c in el["pts"]]
    m["sit"] = sit or lie
    m.setdefault("lie", False)
    return m


def _vy(v, f):
    return (f(v[0]), f(v[1])) if isinstance(v, tuple) else f(v)


def _to_lie(parts, colors):
    """Cabeza al costado del cuerpo acostado; cola, manchas y demás piezas del cuerpo, al cuerpo acostado."""
    main = next(el["fill"] for el in parts if el.get("lie") and el["kind"] == "vol")
    tip = None
    for el in parts:
        if el.get("lie"):
            el.pop("lie")
            if el.get("fill") == "@haunch":
                el["fill"] = main
            continue
        if el.get("layer") == HEAD:
            dx, dy = LIE_HX, LIE_HY
            if "x" in el:
                el["x"] = _vy(el["x"], lambda a: a + dx)
            elif el["kind"] == "mane":
                el["x"] = dx
            if "y" in el:
                el["y"] = _vy(el["y"], lambda a: a + dy)
            if el.get("pivot_y") is not None:
                el["pivot_y"] += dy
            if el.get("pivot") is not None:
                px_, py_, pz_ = el["pivot"]
                el["pivot"] = (px_ + dx, py_ + dy, pz_)
            if el.get("bb"):
                el["bb"] = (el["bb"][0] + dx, el["bb"][1])
            if "pts" in el:
                el["pts"] = [(a + dx, b + dy, c) for a, b, c in el["pts"]]
            continue
        k = el["kind"]
        if el.get("tag") == "tailTip":  # pompón de la cola de cuerda (va después de ella)
            el["y"], el["z"] = tip[1] - 6, tip[2] - 2
        elif k in ("vol", "part"):
            if isinstance(el["y"], tuple) or isinstance(el["z"], tuple):
                ys = el["y"] if isinstance(el["y"], tuple) else (el["y"],) * 2
                zs = el["z"] if isinstance(el["z"], tuple) else (el["z"],) * 2
                a, b = lie_point(ys[0], zs[0]), lie_point(ys[1], zs[1])
                el["y"], el["z"] = (a[0], b[0]), (a[1], b[1])
            else:
                el["y"], el["z"] = lie_point(el["y"], el["z"])
        elif k == "rope":  # cola de cuerda: apoyada en el piso detrás del cuerpo, con la punta para arriba
            x0 = el["pts"][0][0]
            pts = [(x0, 208, LIE_Z - 56), (x0 + 4, 224, LIE_Z - 76), (x0 + 6, 204, LIE_Z - 94)]
            if len(el["pts"]) > 3:  # la del mono termina enrulada
                pts = [(x0, 208, LIE_Z - 56), (x0 + 4, 226, LIE_Z - 80), (x0 + 6, 186, LIE_Z - 100), (x0 + 4, 186, LIE_Z - 82)]
            el["pts"] = pts
            tip = pts[2] if len(pts) == 3 else pts[3]
        elif k == "path":
            el["pts"] = [(x,) + lie_point(y, z) for x, y, z in el["pts"]]
            if el.get("bb"):
                el["bb"] = (el["bb"][0], lie_point(176, el["bb"][1])[1])
        elif k == "tail":
            (bx, bz), (tx, tz) = el["base"], el["tip"]
            (by, bz2), (ty, tz2) = lie_point(el["ty"][0], bz), lie_point(el["ty"][1], tz)
            el["base"], el["tip"], el["ty"] = (bx, bz2), (tx, tz2), (by, ty)


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
        p.append(part(HEAD, x, 97, z + 6, 15, 31, dark, rot=-sx * 18 * (63 / 62), tag=("earL" if sx < 0 else "earR"), pivot_y=70))
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
    """Las mascotas, en el orden de PetType."""
    from model3d_pets import MORE
    m = dict(PETS)
    m.update(MORE)
    return {k: m[k] for k in ("DOG", "CAT", "DUCK", "FROG", "COW", "PIG", "CHICK", "LION", "MONKEY", "OWL", "RABBIT")}


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
