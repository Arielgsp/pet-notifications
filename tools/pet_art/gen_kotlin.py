"""
Pasa los modelos 2,5D (model3d.py, model3d_pets.py) a la app:
  - app/.../overlay/PetModelData.kt: las piezas de cada mascota (parada, sentada y acostada) y sus pelajes;
  - app/src/test/resources/pet_model_golden.txt: casos de prueba con lo que dibuja el motor de Python
    (draw_ops), que PetModelTest compara con lo que dibuja el de la app (PetModel.kt).

Los colores del modelo quedan con nombre ("@main", "@light"...) y cada pelaje dice qué color es cada
nombre; las manchas de los pelajes que las tienen van aparte (se suman al final de las piezas).
Uso: python tools/pet_art/gen_kotlin.py   (después: gradlew testClosedTestDebugUnitTest)
"""
import math
import os

import model3d as M
import model3d_pets as P

HERE = os.path.dirname(os.path.abspath(__file__))
APP = os.path.join(HERE, "..", "..", "app", "src")
OUT_KT = os.path.join(APP, "main", "java", "com", "petnotifications", "overlay", "PetModelData.kt")
OUT_GOLDEN = os.path.join(APP, "test", "resources", "pet_model_golden.txt")

KINDS = {"vol": "VOL", "part": "PART", "eye": "EYE", "path": "PATH", "tail": "TAIL", "rope": "ROPE", "mane": "MANE"}


def num(v):
    v = float(v)
    if v == int(v) and abs(v) < 1e9:
        return f"{int(v)}.0"
    return repr(v)


def V(v):
    if isinstance(v, tuple):
        return f"v({num(v[0])}, {num(v[1])})" if v[0] != v[1] else f"v({num(v[0])})"
    return f"v({num(v)})"


def color(c):
    if c == "none":
        return "NONE"
    if c.startswith("@"):
        return f'k("{c[1:]}")'
    h = c.lstrip("#").upper()
    if len(h) == 3:
        h = "".join(ch * 2 for ch in h)
    return f"c(0x{h})"


def arr(xs):
    return "doubleArrayOf(" + ", ".join(num(x) for x in xs) + ")"


def kt_part(el):
    k = el["kind"]
    a = [f"MP.{KINDS[k]}"]
    if el.get("layer", M.BODY) != M.BODY:
        a.append(f"layer = {el['layer']}")
    if k in ("vol", "part", "eye"):
        for f in ("x", "y", "z"):
            if el[f] != 0:
                a.append(f"{f} = {V(el[f])}")
    if k in ("vol", "part"):
        a += [f"rx = {V(el['rx'])}", f"ry = {V(el['ry'])}", f"fill = {color(el['fill'])}"]
        if not el.get("stroke", True):
            a.append("stroke = false")
    if k == "part":
        if el.get("rot", 0) != 0:
            a.append(f"rot = {V(el['rot'])}")
        if el["behind"] != -0.35:
            a.append(f"behind = {num(el['behind'])}")
        if el.get("flat"):
            a.append("flat = true")
        if el.get("p", 0.7) != 0.7:
            a.append(f"p = {num(el['p'])}")
        if el.get("hide") is not None:
            a.append(f"hide = {num(el['hide'])}")
        if el.get("opacity", 1.0) != 1.0:
            a.append(f"opacity = {num(el['opacity'])}")
        if el.get("clip"):
            a.append(f"clip = MP.{'CLIP_BODY' if el['clip'] == 'body' else 'CLIP_HEAD'}")
        if el.get("pivot_y") is not None:
            a.append(f"pivotY = {num(el['pivot_y'])}")
        if el.get("base_rot", 0) != 0:
            a.append(f"baseRot = {V(el['base_rot'])}")
    if k == "eye":
        a += [f"rx = v({num(el['r'][0])})", f"ry = v({num(el['r'][1])})", f"hide = {num(el['hide'])}"]
        if el.get("kawaii"):
            a.append("kawaii = true")
    if k in ("path", "rope"):
        a.append("pts = " + arr([c for p in el["pts"] for c in p]))
    if k == "path":
        a += [f'd = "{el["d"]}"', f"fill = {color(el['fill'])}", f"width = {num(el['width'])}"]
        if el["color"] != M.INK:
            a.append(f"color = {color(el['color'])}")
        if el["behind"] != -0.35:
            a.append(f"behind = {num(el['behind'])}")
        if el["hide"] is not None:
            a.append(f"hide = {num(el['hide'])}")
        if el.get("bb"):
            a.append("bb = " + arr(el["bb"]))
        if el.get("pivot") is not None:
            a.append("pivot = " + arr(el["pivot"]))
    if k == "tail":
        a += ["base = " + arr(el["base"]), "tip = " + arr(el["tip"]), f"fill = {color(el['fill'])}", f"width = {num(el['width'])}",
              f"bend = {num(el['bend'])}", "ty = " + arr(el["ty"])]
    if k == "rope":
        a += [f"color = {color(el['color'])}", f"width = {num(el['width'])}"]
    if k == "mane":
        if el.get("x", 0) != 0:
            a.append(f"x = {V(el['x'])}")
        a += [f"y = {V(el['y'])}", f"z = {V(el['z'])}", f"rx = {V(el['r'])}", f"fill = {color(el['fill'])}"]
    if el.get("tag"):
        a.append(f'tag = "{el["tag"]}"')
    return "MP(" + ", ".join(a) + ")"


def symbolic(enum):
    """Paleta con nombres en lugar de colores: el modelo queda con "@main", "@light"..."""
    return {k: "@" + k for k in P.PALETTES[enum][0][1] if k not in ("spots", "lop")}


SHAPES = ("Stand", "Sit", "Lie")  # PetModel.STAND, SIT, LIE


def build(fn, shape, colors):
    return M.build(fn, sit=shape == 1, lie=shape == 2, colors=colors)


def fn_name(enum, shape, lop):
    return enum.lower() + SHAPES[shape] + ("Lop" if lop else "")


def spots_name(enum, pi, shape):
    return f"{enum.lower()}Spots{pi}{SHAPES[shape]}"


def generate():
    pets = M.all_pets()
    funcs, model_cases, spot_cases, pal_lines, lying = [], [], [], [], []
    for enum, fn in pets.items():
        sym = symbolic(enum)
        lops = (False, True) if any(pal.get("lop") for _, pal in P.PALETTES[enum]) else (False,)
        sym_len = {}
        for lop in lops:
            for shape in range(3):
                c = dict(sym, lop=True) if lop else sym
                m = build(fn, shape, c)
                if shape == 2 and m["lie"] and not lop:
                    lying.append(f'"{enum}"')
                sym_len[(lop, shape)] = len(m["parts"])
                name = fn_name(enum, shape, lop)
                body = ",\n        ".join(kt_part(el) for el in m["parts"])
                funcs.append(f"    private fun {name}() = listOf(\n        {body},\n    )\n")
                model_cases.append(f'        "{enum}/{shape}/{str(lop).lower()}" -> {name}()')
        pals = []
        for pi, (label, pal) in enumerate(P.PALETTES[enum]):
            cols = ", ".join(f'"{k}" to 0xFF{v.lstrip("#").upper()}' for k, v in pal.items() if k not in ("spots", "lop"))
            has_spots = bool(pal.get("spots"))
            pals.append(f'ModelPalette("{label}", mapOf({cols}), lop = {str(bool(pal.get("lop"))).lower()}, spots = {str(has_spots).lower()})')
            if has_spots:
                for shape in range(3):
                    real = build(fn, shape, pal)["parts"]
                    n = sym_len[(bool(pal.get("lop")), shape)]
                    extra = real[n:]
                    assert extra and all(e["kind"] == "part" and e.get("flat") for e in extra), (enum, pi)
                    name = spots_name(enum, pi, shape)
                    body = ",\n        ".join(kt_part(el) for el in extra)
                    funcs.append(f"    private fun {name}() = listOf(\n        {body},\n    )\n")
                    spot_cases.append(f'        "{enum}/{pi}/{shape}" -> {name}()')
        pal_lines.append(f'        "{enum}" to listOf(\n            ' + ",\n            ".join(pals) + ",\n        ),")
    kt = f'''// Generado por tools/pet_art/gen_kotlin.py a partir de model3d.py y model3d_pets.py: no editar a mano.
package com.petnotifications.overlay

import com.petnotifications.overlay.ModelPart as MP

/** Piezas de los modelos 2,5D (ver PetModel) y los pelajes de cada mascota (el primero es el de siempre). */
internal object PetModelData {{

    /**
     * Modelo parado, sentado o acostado ([shape]: PetModel.STAND, SIT, LIE; [lop]: orejas caídas) con los
     * colores con nombre; null si no existe.
     */
    fun parts(pet: String, shape: Int, lop: Boolean): List<MP>? = when ("$pet/$shape/$lop") {{
{chr(10).join(model_cases)}
        else -> null
    }}

    /** Manchas del pelaje [palette] (van al final de las piezas). */
    fun spots(pet: String, palette: Int, shape: Int): List<MP> = when ("$pet/$palette/$shape") {{
{chr(10).join(spot_cases)}
        else -> emptyList()
    }}

    /**
     * Mascotas que se acuestan de costado (cuerpo de perfil y cabeza de frente: model3d.LIE_TH,
     * LIE_HEAD); las demás (aves) se acuestan como sentadas.
     */
    val lying: Set<String> = setOf({", ".join(lying)})

    val palettes: Map<String, List<ModelPalette>> = mapOf(
{chr(10).join(pal_lines)}
    )

{chr(10).join(funcs)}}}
'''
    open(OUT_KT, "w", encoding="utf-8", newline="\n").write(kt)
    return pets


# ---------------------------------------------------------------- casos de prueba

def f4(v):
    return f"{float(v):.4f}"


def op_line(o):
    t = o["t"]
    if t == "ell":
        s = f"ell {f4(o['cx'])} {f4(o['cy'])} {f4(o['rx'])} {f4(o['ry'])} {o['fill']} {f4(o['rot'])} {int(bool(o['stroke']))} {f4(o['opacity'])}"
    elif t == "circ":
        s = f"circ {f4(o['cx'])} {f4(o['cy'])} {f4(o['r'])} {o['fill']}"
    elif t == "path":
        segs = " ".join(cmd + "".join(" " + f4(x) for x in xs) for cmd, xs in o["segs"])
        s = f"path {o['fill']} {o['color']} {f4(o['w'])} | {segs}"
    else:
        s = f"mane {f4(o['cx'])} {f4(o['cy'])} {f4(o['R'])} {o['n']} {f4(o['rr'])} {o['fill']}"
    if o.get("clip"):
        s += " clip " + " ".join(f4(x) for x in o["clip"])
    if o.get("turn"):
        s += " turn " + " ".join(f4(x) for x in o["turn"])
    return s


POSES = [None, {"armR": {"rot": -125}}, {"armL": {"rot": 140}, "armR": {"rot": -140}}, {"footL": {"dy": -7}, "footR": {"dy": -3}},
         {"earL": {"rot": -110}, "earR": {"rot": 110}}, {"earL": {"rot": 20, "dy": 2}, "earR": {"rot": -12}}]


def pose_str(pose):
    if not pose:
        return "-"
    return ";".join(f"{tag}:{num(v.get('rot', 0))}:{num(v.get('dy', 0))}" for tag, v in pose.items())


def golden(pets):
    lines = []

    def case(enum, pi, shape, th, face, head=None, pose=None, layer=None):
        pal = P.PALETTES[enum][pi][1]
        m = build(pets[enum], int(shape), pal)
        ops = M.draw_ops(m, th, face, pose, head, layer)
        lines.append(f"CASE {enum} {pi} {int(shape)} {num(th)} {face} {'-' if head is None else num(head)} {pose_str(pose)} "
                     f"{'-' if layer is None else layer}")
        lines.extend(op_line(o) for o in ops)

    for enum in pets:
        for sit in (False, True):
            for th in range(0, 360, 30):
                case(enum, 0, sit, th + 5 * (th % 60 == 0), ("open", "happy", "closed")[th // 30 % 3])
            for pose in POSES[1:]:
                case(enum, 0, sit, 25, "open", pose=pose)
            for h in (-50, 35):
                case(enum, 0, sit, 0, "open", head=h)
                case(enum, 0, sit, 0, "open", head=h, layer=M.HEAD)
            for pi in range(1, len(P.PALETTES[enum])):
                for th in (0, 215):
                    case(enum, pi, sit, th, "open", pose=POSES[4] if th == 215 else None)
        # acostada (como la dibuja la app: cuerpo de perfil, cabeza de frente) y el cuerpo sentado solo
        for pi in range(len(P.PALETTES[enum])):
            for face in ("open", "closed"):
                case(enum, pi, 2, M.LIE_TH, face, head=M.LIE_HEAD)
        case(enum, 0, 2, 0, "open")
        case(enum, 0, 1, 0, "open", layer=M.BODY)
    open(OUT_GOLDEN, "w", encoding="utf-8", newline="\n").write("\n".join(lines) + "\n")
    return lines


if __name__ == "__main__":
    pets = generate()
    lines = golden(pets)
    print(f"ok: {OUT_KT}\n    {OUT_GOLDEN} ({sum(l.startswith('CASE') for l in lines)} casos, {len(lines)} líneas)")
