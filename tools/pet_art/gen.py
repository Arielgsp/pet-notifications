import math, os, json, datetime

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "canvas")
os.makedirs(os.path.join(ROOT, "project"), exist_ok=True)

INK = "#4A3228"
BLUSH = "#FF9EAE"
W = 'stroke="%s" stroke-width="4" stroke-linejoin="round" stroke-linecap="round"' % INK


def eyes(lx, rx, y, kind="open", r=(10, 12)):
    out = []
    for x in (lx, rx):
        if kind == "open":
            out.append(f'<ellipse cx="{x}" cy="{y}" rx="{r[0]}" ry="{r[1]}" fill="{INK}" stroke="none"></ellipse>')
            out.append(f'<circle cx="{x-3}" cy="{y-5}" r="4" fill="#FFFFFF" stroke="none"></circle>')
            out.append(f'<circle cx="{x+4}" cy="{y+4}" r="1.8" fill="#FFFFFF" stroke="none"></circle>')
        elif kind == "happy":
            out.append(f'<path d="M{x-10} {y+3} Q{x} {y-10} {x+10} {y+3}" fill="none" stroke-width="4.5"></path>')
        elif kind == "blink":
            out.append(f'<path d="M{x-10} {y-2} Q{x} {y+7} {x+10} {y-2}" fill="none" stroke-width="4.5"></path>')
    return "".join(out)


def blush(lx, rx, y):
    return "".join(
        f'<ellipse cx="{x}" cy="{y}" rx="11" ry="6.5" fill="{BLUSH}" opacity=".65" stroke="none"></ellipse>' for x in (lx, rx)
    )


def wmouth(x, y, kind="smile", tongue=False):
    if kind == "open":
        return (f'<path d="M{x-9} {y-1} Q{x} {y+18} {x+9} {y-1} Q{x} {y+3} {x-9} {y-1} Z" fill="#B8475A" stroke-width="3.5"></path>'
                f'<path d="M{x-5} {y+8} Q{x} {y+4} {x+5} {y+8} Q{x} {y+13} {x-5} {y+8} Z" fill="#FF8FA3" stroke="none"></path>')
    t = f'<path d="M{x-3} {y+3} Q{x} {y+11} {x+3} {y+3}" fill="#FF8FA3" stroke-width="2.5"></path>' if tongue else ""
    return t + f'<path d="M{x-9} {y} Q{x-4.5} {y+6} {x} {y} Q{x+4.5} {y+6} {x+9} {y}" fill="none" stroke-width="3.5"></path>'


def shadow():
    return f'<ellipse cx="100" cy="228" rx="54" ry="7" fill="{INK}" opacity=".12" stroke="none"></ellipse>'


# Pose sentada (vistas SIT del exportador): como un peluche, con las patas estiradas hacia adelante y
# la cabeza un poco más abajo. base_body deja abierto un grupo que baja todo lo que sigue
# (la cabeza); quien la usa lo cierra con "</g>".
SITTING = False
SIT_HEAD_DY = 8
PAW_PAD = "#FFC9D2"  # almohadillas de las plantitas


def base_body(main, belly=None, feet=None, arms=None):
    feet = feet or main
    arms = arms or main
    if SITTING:
        # Sentada "peluche": patas estiradas hacia adelante (se ven las plantitas) y bracitos al costado.
        s = f'<ellipse cx="100" cy="186" rx="42" ry="34" fill="{main}"></ellipse>'
        if belly:
            s += f'<ellipse cx="100" cy="192" rx="24" ry="20" fill="{belly}" stroke="none"></ellipse>'
        s += (f'<ellipse cx="64" cy="186" rx="10" ry="15" fill="{arms}" transform="rotate(22 64 186)"></ellipse>'
              f'<ellipse cx="136" cy="186" rx="10" ry="15" fill="{arms}" transform="rotate(-22 136 186)"></ellipse>')
        for cx in (72, 128):
            s += f'<ellipse cx="{cx}" cy="212" rx="18" ry="13" fill="{feet}"></ellipse>'
            s += f'<ellipse cx="{cx}" cy="214" rx="8" ry="6" fill="{PAW_PAD}" stroke="none"></ellipse>'
            for dx, dy in ((-9, 207), (0, 205), (9, 207)):
                s += f'<circle cx="{cx + dx}" cy="{dy}" r="2.6" fill="{PAW_PAD}" stroke="none"></circle>'
        return s + f'<g transform="translate(0 {SIT_HEAD_DY})">'
    s = (f'<ellipse cx="80" cy="214" rx="15" ry="10" fill="{feet}"></ellipse>'
         f'<ellipse cx="120" cy="214" rx="15" ry="10" fill="{feet}"></ellipse>'
         f'<ellipse cx="100" cy="176" rx="42" ry="38" fill="{main}"></ellipse>')
    if belly:
        s += f'<ellipse cx="100" cy="186" rx="25" ry="22" fill="{belly}" stroke="none"></ellipse>'
    s += (f'<ellipse cx="66" cy="180" rx="10" ry="15" fill="{arms}" transform="rotate(22 66 180)"></ellipse>'
          f'<ellipse cx="134" cy="180" rx="10" ry="15" fill="{arms}" transform="rotate(-22 134 180)"></ellipse>')
    return s


def toes(color):
    s = ""
    for cx in (82, 118):
        for dx, ex in ((-7, -10), (0, 0), (7, 10)):
            s += f'<path d="M{cx+dx} 208 L{cx+ex} 222" fill="none" stroke="{color}" stroke-width="5"></path>'
    return s


def svg(inner, size=200):
    h = int(size * 1.2)
    return (f'<svg viewBox="0 0 200 240" width="{size}" height="{h}" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">'
            f'{shadow()}<g {W}>{inner}</g></svg>')


# ---------- pets ----------

def dog(expr="open", mouth="smile"):
    main, light, dark = "#F2C38B", "#FFF4E2", "#C98A55"
    s = f'<path d="M134 188 C162 186 174 160 164 140 C160 156 150 168 132 172 Z" fill="{main}"></path>'
    s += base_body(main, light, light)
    return s + dog_head(expr, mouth)


DOG_MAIN, DOG_LIGHT, DOG_DARK = "#F2C38B", "#FFF4E2", "#C98A55"


def dog_head(expr="open", mouth="smile"):
    main, light, dark = DOG_MAIN, DOG_LIGHT, DOG_DARK
    s = f'<ellipse cx="100" cy="98" rx="68" ry="60" fill="{main}"></ellipse>'
    s += f'<path d="M100 40 Q108 52 100 60 Q92 52 100 40 Z" fill="{light}" stroke="none"></path>'
    s += f'<ellipse cx="100" cy="126" rx="25" ry="18" fill="{light}"></ellipse>'
    s += f'<ellipse cx="40" cy="96" rx="15" ry="31" fill="{dark}" transform="rotate(18 40 96)"></ellipse>'
    s += f'<ellipse cx="160" cy="96" rx="15" ry="31" fill="{dark}" transform="rotate(-18 160 96)"></ellipse>'
    s += eyes(72, 128, 100, expr) + blush(54, 146, 124)
    s += f'<ellipse cx="100" cy="116" rx="8" ry="6" fill="{INK}" stroke="none"></ellipse>'
    s += f'<path d="M100 121 L100 126" fill="none" stroke-width="3.5"></path>'
    s += wmouth(100, 126, mouth, tongue=True)
    return s


CAT_MAIN, CAT_LIGHT, CAT_SOFT = "#F7B77A", "#FFF3E4", "#F0A066"


def cat_ears():
    """Orejas redondeadas (más amigables que las puntiagudas de antes)."""
    m = CAT_MAIN
    return (f'<path d="M42 76 Q42 30 52 24 Q66 30 90 48 Z" fill="{m}"></path><path d="M158 76 Q158 30 148 24 Q134 30 110 48 Z" fill="{m}"></path>'
            f'<path d="M54 58 Q54 40 58 36 Q66 40 76 49 Z" fill="#FFB3BF" stroke="none"></path><path d="M146 58 Q146 40 142 36 Q134 40 124 49 Z" fill="#FFB3BF" stroke="none"></path>')


def cat_face(e):
    """Cara del gato de frente ("Contenta"): rayitas suaves, hocico claro, ojos grandes, sonrisa abierta."""
    s = f'<ellipse cx="100" cy="98" rx="68" ry="60" fill="{CAT_MAIN}"></ellipse>'
    s += f'<path d="M92 42 Q94 48 93 53 M100 39 L100 50 M108 42 Q106 48 107 53" fill="none" stroke="{CAT_SOFT}" stroke-width="4"></path>'
    s += f'<ellipse cx="100" cy="123" rx="23" ry="15" fill="{CAT_LIGHT}" stroke="none"></ellipse>'
    s += eyes(72, 128, 100, e, r=(11, 13)) + blush(52, 148, 122)
    s += (f'<ellipse cx="52" cy="122" rx="13" ry="7.5" fill="{BLUSH}" opacity=".5" stroke="none"></ellipse>'
          f'<ellipse cx="148" cy="122" rx="13" ry="7.5" fill="{BLUSH}" opacity=".5" stroke="none"></ellipse>')
    s += f'<path d="M95 113 L105 113 L100 119 Z" fill="#FF8FA3" stroke-width="2.5"></path>'
    s += (f'<path d="M100 119 L100 122" fill="none" stroke-width="3"></path>'
          f'<path d="M89 122 Q100 138 111 122 Q100 127 89 122 Z" fill="#B8475A" stroke-width="3"></path>'
          f'<path d="M95 129 Q100 126 105 129 Q100 133 95 129 Z" fill="#FF8FA3" stroke="none"></path>')
    s += f'<path d="M40 110 Q48 112 56 116 M40 124 Q48 122 56 121 M160 110 Q152 112 144 116 M160 124 Q152 122 144 121" fill="none" stroke-width="3"></path>'
    return s


def cat(e="open"):
    main, light = CAT_MAIN, CAT_LIGHT
    s = f'<path d="M136 198 C176 198 186 150 164 130 C156 124 148 134 156 140 C168 154 162 184 134 184 Z" fill="{main}"></path>'
    s += base_body(main, light, light)
    # las orejas van después del cuerpo: así bajan con la cabeza cuando está sentada
    return s + cat_ears() + cat_face(e)


def duck():
    main, bill, scarf = "#FFEFA8", "#FFA94D", "#7FB8E8"
    wing, tuft = "#FFE07A", "#FFD95E"
    s = f'<path d="M140 176 L160 166 L152 184 Z" fill="{main}"></path>'
    s += (f'<path d="M66 206 L94 206 L80 222 L72 214 L66 222 Z" fill="{bill}"></path>'
          f'<path d="M106 206 L134 206 L134 222 L128 214 L120 222 Z" fill="{bill}"></path>')
    s += f'<ellipse cx="100" cy="176" rx="42" ry="38" fill="{main}"></ellipse>'
    s += (f'<ellipse cx="62" cy="178" rx="11" ry="20" fill="{wing}" transform="rotate(28 62 178)"></ellipse>'
          f'<ellipse cx="138" cy="178" rx="11" ry="20" fill="{wing}" transform="rotate(-28 138 178)"></ellipse>')
    s += f'<ellipse cx="100" cy="186" rx="24" ry="20" fill="#FFF8DC" stroke="none"></ellipse>'
    s += f'<path d="M98 42 C90 26 98 16 106 22 C104 28 110 30 114 26 C116 36 108 42 104 44 Z" fill="{tuft}"></path>'
    s += f'<ellipse cx="100" cy="98" rx="66" ry="60" fill="{main}"></ellipse>'
    s += (f'<path d="M100 156 L78 146 L80 168 Z" fill="{scarf}"></path><path d="M100 156 L122 146 L120 168 Z" fill="{scarf}"></path>'
          f'<circle cx="100" cy="156" r="6" fill="{scarf}"></circle>')
    s += eyes(72, 128, 98) + blush(54, 146, 120)
    s += f'<ellipse cx="100" cy="124" rx="24" ry="11" fill="{bill}"></ellipse>'
    s += f'<path d="M82 124 Q100 130 118 124" fill="none" stroke-width="3"></path>'
    return s


def frog(e="open"):
    main, light = "#A6D98A", "#E6F5D0"
    s = (f'<ellipse cx="78" cy="214" rx="19" ry="9" fill="{main}"></ellipse>'
         f'<ellipse cx="122" cy="214" rx="19" ry="9" fill="{main}"></ellipse>'
         f'<ellipse cx="100" cy="178" rx="42" ry="36" fill="{main}"></ellipse>'
         f'<ellipse cx="100" cy="188" rx="26" ry="20" fill="{light}" stroke="none"></ellipse>'
         f'<ellipse cx="66" cy="182" rx="10" ry="15" fill="{main}" transform="rotate(22 66 182)"></ellipse>'
         f'<ellipse cx="134" cy="182" rx="10" ry="15" fill="{main}" transform="rotate(-22 134 182)"></ellipse>')
    s += f'<circle cx="66" cy="62" r="27" fill="{main}"></circle><circle cx="134" cy="62" r="27" fill="{main}"></circle>'
    s += f'<ellipse cx="100" cy="108" rx="74" ry="50" fill="{main}"></ellipse>'
    s += f'<circle cx="66" cy="62" r="25" fill="{main}" stroke="none"></circle><circle cx="134" cy="62" r="25" fill="{main}" stroke="none"></circle>'
    s += f'<rect x="50" y="62" width="100" height="30" fill="{main}" stroke="none"></rect>'
    s += eyes(66, 134, 64, e) + blush(46, 154, 112)
    s += f'<path d="M70 116 Q100 138 130 116" fill="none" stroke-width="4"></path>'
    s += f'<circle cx="92" cy="100" r="2" fill="{INK}" stroke="none"></circle><circle cx="108" cy="100" r="2" fill="{INK}" stroke="none"></circle>'
    return s


def cow(e="open"):
    main, spot, muzzle, horn = "#FFFCF7", "#7A5646", "#FFC4CE", "#F3DFB5"
    s = f'<path d="M140 190 Q164 196 166 170" fill="none" stroke-width="4"></path><circle cx="166" cy="166" r="6" fill="{spot}"></circle>'
    s += base_body(main, None, spot, main)
    s += f'<path d="M64 168 C72 158 90 162 88 176 C86 188 70 190 64 180 Z" fill="{spot}" stroke="none"></path>'
    s += (f'<path d="M66 50 Q58 26 72 24 Q72 38 80 44 Z" fill="{horn}"></path>'
          f'<path d="M134 50 Q142 26 128 24 Q128 38 120 44 Z" fill="{horn}"></path>')
    s += (f'<ellipse cx="32" cy="84" rx="22" ry="11" fill="{main}" transform="rotate(-18 32 84)"></ellipse>'
          f'<ellipse cx="168" cy="84" rx="22" ry="11" fill="{main}" transform="rotate(18 168 84)"></ellipse>'
          f'<ellipse cx="30" cy="85" rx="12" ry="5" fill="{muzzle}" stroke="none" transform="rotate(-18 30 85)"></ellipse>'
          f'<ellipse cx="170" cy="85" rx="12" ry="5" fill="{muzzle}" stroke="none" transform="rotate(18 170 85)"></ellipse>')
    s += f'<ellipse cx="100" cy="98" rx="68" ry="60" fill="{main}"></ellipse>'
    s += f'<path d="M114 54 C128 48 142 58 140 70 C136 82 120 78 116 70 C112 64 110 58 114 54 Z" fill="{spot}" stroke="none"></path>'
    s += f'<path d="M72 144 Q100 158 128 144" fill="none" stroke="#E86A6A" stroke-width="7"></path>'
    s += f'<path d="M90 152 Q100 146 110 152 L112 166 Q100 172 88 166 Z" fill="#FFD166"></path>'
    s += eyes(72, 128, 94, e) + blush(50, 150, 110)
    s += f'<ellipse cx="100" cy="126" rx="34" ry="20" fill="{muzzle}"></ellipse>'
    s += f'<ellipse cx="88" cy="124" rx="4" ry="5.5" fill="#C97884" stroke="none"></ellipse><ellipse cx="112" cy="124" rx="4" ry="5.5" fill="#C97884" stroke="none"></ellipse>'
    s += f'<path d="M93 135 Q100 140 107 135" fill="none" stroke-width="3"></path>'
    return s


def pig(e="open"):
    main, dark, snout = "#FFC6D1", "#F29BB0", "#FFB0C0"
    s = f'<path d="M140 182 c10 -4 15 5 9 10 c-6 4 -11 -3 -5 -8 c6 -4 14 -2 16 -9" fill="none" stroke-width="4"></path>'
    s += (f'<path d="M44 70 Q36 30 58 22 Q76 34 86 50 Z" fill="{main}"></path>'
          f'<path d="M156 70 Q164 30 142 22 Q124 34 114 50 Z" fill="{main}"></path>'
          f'<path d="M54 54 Q52 38 60 32 Q70 40 76 50 Z" fill="{dark}" stroke="none"></path>'
          f'<path d="M146 54 Q148 38 140 32 Q130 40 124 50 Z" fill="{dark}" stroke="none"></path>')
    s += base_body(main, "#FFDDE4", dark)
    s += f'<ellipse cx="100" cy="98" rx="68" ry="60" fill="{main}"></ellipse>'
    s += eyes(72, 128, 96, e) + blush(52, 148, 120)
    s += f'<ellipse cx="100" cy="122" rx="22" ry="15" fill="{snout}"></ellipse>'
    s += f'<ellipse cx="92" cy="122" rx="3.5" ry="5" fill="#C9677E" stroke="none"></ellipse><ellipse cx="108" cy="122" rx="3.5" ry="5" fill="#C9677E" stroke="none"></ellipse>'
    s += f'<path d="M94 142 Q100 147 106 142" fill="none" stroke-width="3"></path>'
    return s


def chick():
    main, dark, beak = "#FFE27A", "#F7C948", "#FF9F43"
    s = f'<ellipse cx="100" cy="132" rx="68" ry="80" fill="{main}"></ellipse>'
    s += toes(beak)
    s += (f'<ellipse cx="36" cy="146" rx="12" ry="22" fill="{dark}" transform="rotate(24 36 146)"></ellipse>'
          f'<ellipse cx="164" cy="146" rx="12" ry="22" fill="{dark}" transform="rotate(-24 164 146)"></ellipse>')
    s += (f'<g transform="rotate(-10 100 44)"><path d="M64 64 Q62 24 100 20 Q138 24 136 64 L127 55 L118 65 L109 55 L100 65 L91 55 L82 65 L73 55 Z" fill="#FFFDF5"></path></g>')
    s += eyes(76, 124, 110) + blush(56, 144, 132)
    s += f'<path d="M90 124 L110 124 L100 137 Z" fill="{beak}" stroke-width="3"></path>'
    return s


def lion(e="open"):
    main, mane, light = "#F7C873", "#E0873C", "#FFF1D6"
    cx, cy, R, n = 100, 94, 72, 14
    pts = [(cx + R * math.cos(2 * math.pi * i / n - math.pi / 2), cy + R * math.sin(2 * math.pi * i / n - math.pi / 2)) for i in range(n)]
    r = R * math.sin(math.pi / n) * 1.15
    d = f"M{pts[0][0]:.1f} {pts[0][1]:.1f} " + " ".join(
        f"A{r:.1f} {r:.1f} 0 0 1 {pts[(i+1)%n][0]:.1f} {pts[(i+1)%n][1]:.1f}" for i in range(n)) + " Z"
    s = (f'<path d="M136 192 Q172 198 170 166" fill="none" stroke-width="10"></path>'
         f'<path d="M136 192 Q172 198 170 166" fill="none" stroke="{main}" stroke-width="4"></path>'
         f'<circle cx="170" cy="160" r="9" fill="{mane}"></circle>')
    s += base_body(main, light, light)
    s += f'<path d="{d}" fill="{mane}"></path>'
    s += (f'<circle cx="56" cy="52" r="14" fill="{main}"></circle><circle cx="144" cy="52" r="14" fill="{main}"></circle>'
          f'<circle cx="56" cy="52" r="7" fill="{light}" stroke="none"></circle><circle cx="144" cy="52" r="7" fill="{light}" stroke="none"></circle>')
    s += f'<ellipse cx="100" cy="100" rx="58" ry="52" fill="{main}"></ellipse>'
    s += eyes(76, 124, 98, e) + blush(58, 142, 120)
    s += f'<ellipse cx="100" cy="124" rx="22" ry="15" fill="{light}"></ellipse>'
    s += f'<path d="M92 113 Q100 109 108 113 Q104 121 100 121 Q96 121 92 113 Z" fill="#7A4A2E" stroke-width="2.5"></path>'
    s += wmouth(100, 125)
    return s


def monkey(e="open"):
    main, face = "#A9744F", "#F5D2AE"
    s = (f'<path d="M136 196 C176 200 188 154 166 144 C154 138 146 154 158 158" fill="none" stroke-width="11"></path>'
         f'<path d="M136 196 C176 200 188 154 166 144 C154 138 146 154 158 158" fill="none" stroke="{main}" stroke-width="5"></path>')
    s += base_body(main, face, main)
    s += (f'<circle cx="32" cy="102" r="20" fill="{main}"></circle><circle cx="168" cy="102" r="20" fill="{main}"></circle>'
          f'<circle cx="32" cy="102" r="11" fill="{face}" stroke="none"></circle><circle cx="168" cy="102" r="11" fill="{face}" stroke="none"></circle>')
    s += f'<ellipse cx="100" cy="98" rx="64" ry="58" fill="{main}"></ellipse>'
    s += f'<path d="M96 44 Q100 30 111 36" fill="none" stroke-width="3.5"></path>'
    s += f'<path d="M100 72 C82 54 46 62 50 100 C52 134 76 148 100 148 C124 148 148 134 150 100 C154 62 118 54 100 72 Z" fill="{face}"></path>'
    s += eyes(78, 122, 100, e) + blush(62, 138, 124)
    s += f'<circle cx="95" cy="118" r="2.3" fill="{INK}" stroke="none"></circle><circle cx="105" cy="118" r="2.3" fill="{INK}" stroke="none"></circle>'
    s += f'<path d="M84 128 Q100 142 116 128" fill="none" stroke-width="3.5"></path>'
    return s


def owl():
    main, dark, light, disc = "#A88470", "#7E5E4E", "#F3E3CF", "#EBD5BD"
    s = f'<path d="M42 78 L40 34 L76 56 Z" fill="{main}"></path><path d="M158 78 L160 34 L124 56 Z" fill="{main}"></path>'
    s += f'<ellipse cx="100" cy="130" rx="70" ry="82" fill="{main}"></ellipse>'
    s += toes("#FFA94D")
    s += f'<ellipse cx="100" cy="174" rx="38" ry="32" fill="{light}" stroke="none"></ellipse>'
    for (x, y) in ((86, 160), (100, 156), (114, 160), (93, 174), (107, 174), (100, 188)):
        s += f'<path d="M{x-5} {y} Q{x} {y+5} {x+5} {y}" fill="none" stroke="{dark}" stroke-width="3"></path>'
    s += (f'<ellipse cx="36" cy="150" rx="14" ry="32" fill="{dark}" transform="rotate(14 36 150)"></ellipse>'
          f'<ellipse cx="164" cy="150" rx="14" ry="32" fill="{dark}" transform="rotate(-14 164 150)"></ellipse>')
    s += f'<circle cx="74" cy="100" r="27" fill="{disc}" stroke="none"></circle><circle cx="126" cy="100" r="27" fill="{disc}" stroke="none"></circle>'
    s += eyes(74, 126, 100, r=(12, 14)) + blush(50, 150, 126)
    s += f'<path d="M93 118 L107 118 L100 130 Z" fill="#FFB347" stroke-width="3"></path>'
    return s


PETS = [
    ("Perro", "¡Guau!", "#FFF1DF", dog),
    ("Gato", "¡Miau!", "#FFEBDD", cat),
    ("Pato", "¡Cuac!", "#E6F1FA", duck),
    ("Rana", "¡Croac!", "#EAF6DF", frog),
    ("Vaca", "¡Muuu!", "#F7EEF0", cow),
    ("Chancho", "¡Oink!", "#FFE9EE", pig),
    ("Pollito", "¡Pío pío!", "#FFF6D6", chick),
    ("León", "¡Roaar!", "#FFEFD9", lion),
    ("Mono", "¡Uh uh ah ah!", "#F5EADF", monkey),
    ("Búho", "¡Uhuu!", "#F1E9E2", owl),
]

FONTS = ('<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Fredoka:wght@500;600;700&amp;family=Nunito:wght@600;700;800&amp;display=swap">')


def page(title, body, w, h):
    return f'''<!doctype html>
<html lang="es">
<head>
<meta charset="utf-8">
<title>{title}</title>
<script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
{FONTS}
<style>
body{{margin:0;font-family:Nunito,system-ui,sans-serif;color:{INK};background:#FFF8EF}}
a{{color:#B8475A}}a:hover{{color:#8E2F42}}
</style>
</helmet>
{body}
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{{"$preview":{{"width":{w},"height":{h}}}}}'>
class Component extends DCLogic {{
renderVals() {{
return {{}};
}}
}}
</script>
</body>
</html>
'''


# ---------- Main: lineup ----------
cards = []
for name, sound, tint, fn in PETS:
    cards.append(f'''<div style="display: flex; flex-direction: column; align-items: center; gap: 14px; padding: 28px 16px 26px; background: {tint}; border-radius: 32px;">
{svg(fn(), 200)}
<div style="display: flex; flex-direction: column; align-items: center; gap: 8px;">
<div style="font-family: Fredoka, Nunito, sans-serif; font-size: 28px; font-weight: 600; line-height: 1;">{name}</div>
<div style="font-size: 14px; font-weight: 800; padding: 5px 14px; border-radius: 999px; background: #FFFFFF; color: #8E2F42;">{sound}</div>
</div>
</div>''')

main_w, main_h = 1440, 1120
main = f'''<div style="width: {main_w}px; height: {main_h}px; box-sizing: border-box; padding: 64px 56px; display: flex; flex-direction: column; gap: 44px; background: #FFF8EF;">
<div style="display: flex; justify-content: space-between; align-items: flex-end; gap: 40px;">
<div style="display: flex; flex-direction: column; gap: 10px;">
<div style="font-size: 15px; font-weight: 800; letter-spacing: 0.12em; text-transform: uppercase; color: #8E5A44;">Pet Notifications · personajes</div>
<h1 style="margin: 0; font-family: Fredoka, Nunito, sans-serif; font-size: 60px; font-weight: 600; line-height: 1.02;">Mascotas de cuerpo entero</h1>
</div>
<p style="margin: 0; max-width: 460px; font-size: 18px; line-height: 1.5; color: #75594B;">Cabeza grande, cuerpo chiquito, ojos con brillo y cachetes rosados. Mismo trazo cálido en las diez para que se sientan de la misma familia.</p>
</div>
<div style="display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); gap: 28px;">
{"".join(cards)}
</div>
</div>'''

# ---------- Estados ----------
HEART = '<svg viewBox="0 0 24 24" width="{s}" height="{s}" aria-hidden="true" style="position: absolute; left: {x}px; top: {y}px; transform: rotate({r}deg);"><path d="M12 21 C5 15 2 11.5 2 8 C2 5 4.5 3 7 3 C9 3 11 4.5 12 6.5 C13 4.5 15 3 17 3 C19.5 3 22 5 22 8 C22 11.5 19 15 12 21 Z" fill="#FF7A93" stroke="#4A3228" stroke-width="1.8" stroke-linejoin="round"></path></svg>'

BURST = '''<div style="position: absolute; left: 150px; top: 6px; padding: 10px 18px; background: #FFD54F; border: 3px solid #4A3228; border-radius: 999px; font-family: Fredoka, Nunito, sans-serif; font-size: 24px; font-weight: 700; font-style: italic; color: #D9541E; transform: rotate(-8deg);">¡Guau!</div>'''

states = [
    ("Reposo", "Quieta, respira cada tanto. Parpadea sola.", svg(dog("open", "smile"), 220), ""),
    ("Parpadeo", "Ojitos cerrados unos 150 ms, cada 4 a 7 segundos.", svg(dog("blink", "smile"), 220), ""),
    ("Hablando", "Boca abierta y se balancea mientras suena.", f'<div style="transform: rotate(-6deg);">{svg(dog("open", "open"), 220)}</div>', BURST),
    ("Mimos", "Al tocarla: ojos felices y suben corazones.", svg(dog("happy", "smile"), 220),
     HEART.format(s=34, x=40, y=30, r=-14) + HEART.format(s=26, x=236, y=60, r=12) + HEART.format(s=20, x=210, y=14, r=-6)),
]
st_cards = []
for label, desc, art, extra in states:
    st_cards.append(f'''<div style="display: flex; flex-direction: column; gap: 18px; padding: 24px; background: #FFF1DF; border-radius: 32px;">
<div style="position: relative; height: 290px; display: flex; align-items: flex-end; justify-content: center;">
{art}
{extra}
</div>
<div style="display: flex; flex-direction: column; gap: 6px;">
<div style="font-family: Fredoka, Nunito, sans-serif; font-size: 26px; font-weight: 600;">{label}</div>
<div style="font-size: 16px; line-height: 1.45; color: #75594B;">{desc}</div>
</div>
</div>''')

est_w, est_h = 1440, 680
estados = f'''<div style="width: {est_w}px; height: {est_h}px; box-sizing: border-box; padding: 56px; display: flex; flex-direction: column; gap: 32px; background: #FFF8EF;">
<div style="display: flex; flex-direction: column; gap: 8px;">
<h2 style="margin: 0; font-family: Fredoka, Nunito, sans-serif; font-size: 40px; font-weight: 600;">Estados y expresiones</h2>
<p style="margin: 0; font-size: 17px; color: #75594B;">Las mismas cuatro caras para las diez mascotas; solo cambian ojos y boca.</p>
</div>
<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 28px;">
{"".join(st_cards)}
</div>
</div>'''

# ---------- En pantalla ----------
clock = '<svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="#6D6D6D" stroke-width="2.4" stroke-linecap="round" aria-hidden="true"><circle cx="12" cy="13" r="8"></circle><path d="M12 9 L12 13 L15 15"></path></svg>'
ph_w, ph_h = 390, 844
pantalla = f'''<div style="width: {ph_w}px; height: {ph_h}px; box-sizing: border-box; position: relative; overflow: hidden; background: #D9E4EC; font-family: Nunito, system-ui, sans-serif;">
<div style="position: absolute; left: 24px; right: 24px; top: 72px; display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 26px 22px;">
<div style="height: 64px; border-radius: 18px; background: #C4D2DD;"></div><div style="height: 64px; border-radius: 18px; background: #C4D2DD;"></div><div style="height: 64px; border-radius: 18px; background: #C4D2DD;"></div><div style="height: 64px; border-radius: 18px; background: #C4D2DD;"></div>
<div style="height: 64px; border-radius: 18px; background: #C4D2DD;"></div><div style="height: 64px; border-radius: 18px; background: #C4D2DD;"></div><div style="height: 64px; border-radius: 18px; background: #C4D2DD;"></div><div style="height: 64px; border-radius: 18px; background: #C4D2DD;"></div>
</div>
<div style="position: absolute; left: 20px; right: 20px; bottom: 228px; display: flex; flex-direction: column;">
<div style="display: flex; flex-direction: column; gap: 2px; padding: 12px 16px; background: #FFFDF5; border: 3px solid #1B1B1B; border-radius: 20px;">
<div style="display: flex; align-items: center; gap: 5px; font-size: 11px; font-weight: 800; color: #6D6D6D;">{clock}<span>Recordatorio</span></div>
<div style="font-size: 18px; font-weight: 800; color: #1B1B1B;">Llamar al veterinario</div>
<div style="font-size: 14px; color: #333333;">Hoy 17:30 · turno de vacunas</div>
<div style="padding-top: 6px; font-size: 11px; font-style: italic; color: #6D6D6D; text-align: right;">Tocá para abrir · mantené para posponer</div>
</div>
<svg width="350" height="18" viewBox="0 0 350 18" aria-hidden="true" style="margin-top: -3px;"><path d="M248 0 L258 17 L270 0 Z" fill="#FFFDF5"></path><path d="M248 0 L258 17 L270 0" fill="none" stroke="#1B1B1B" stroke-width="3" stroke-linejoin="round"></path></svg>
</div>
<div style="position: absolute; right: 38px; bottom: 44px;">{svg(dog("open", "open"), 150)}</div>
</div>'''

# ---------- Poses ----------
def dog_lying(expr="open", mouth="smile"):
    main, light = DOG_MAIN, DOG_LIGHT
    s = f'<path d="M186 150 C214 146 226 120 216 104 C212 118 202 130 182 134 Z" fill="{main}"></path>'
    s += f'<ellipse cx="180" cy="166" rx="22" ry="16" fill="{main}"></ellipse>'
    s += f'<ellipse cx="138" cy="148" rx="54" ry="34" fill="{main}"></ellipse>'
    s += f'<ellipse cx="196" cy="178" rx="14" ry="8" fill="{light}"></ellipse>'
    s += f'<g transform="translate(-14 14)">{dog_head(expr, mouth)}</g>'
    s += f'<ellipse cx="62" cy="178" rx="17" ry="9" fill="{light}"></ellipse>'
    s += f'<ellipse cx="104" cy="180" rx="17" ry="9" fill="{light}"></ellipse>'
    return (f'<svg viewBox="0 0 240 200" width="{{w}}" height="{{h}}" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">'
            f'<ellipse cx="124" cy="188" rx="98" ry="7" fill="{INK}" opacity=".12" stroke="none"></ellipse>'
            f'<g {W}>{s}</g></svg>')


def dog_side():
    main, light, dark = DOG_MAIN, DOG_LIGHT, DOG_DARK
    s = f'<path d="M52 170 C30 164 24 140 34 126 C38 140 44 154 58 158 Z" fill="{main}"></path>'
    s += f'<ellipse cx="88" cy="212" rx="14" ry="9" fill="{dark}"></ellipse><ellipse cx="142" cy="212" rx="14" ry="9" fill="{dark}"></ellipse>'
    s += f'<ellipse cx="72" cy="214" rx="15" ry="10" fill="{light}"></ellipse><ellipse cx="126" cy="214" rx="15" ry="10" fill="{light}"></ellipse>'
    s += f'<ellipse cx="96" cy="178" rx="48" ry="34" fill="{main}"></ellipse>'
    s += f'<ellipse cx="104" cy="196" rx="30" ry="11" fill="{light}" stroke="none"></ellipse>'
    s += f'<ellipse cx="128" cy="190" rx="10" ry="15" fill="{main}" transform="rotate(-10 128 190)"></ellipse>'
    s += f'<ellipse cx="112" cy="98" rx="62" ry="58" fill="{main}"></ellipse>'
    s += f'<ellipse cx="92" cy="100" rx="16" ry="32" fill="{dark}" transform="rotate(12 92 100)"></ellipse>'
    s += f'<ellipse cx="164" cy="120" rx="24" ry="17" fill="{light}"></ellipse>'
    s += (f'<ellipse cx="142" cy="98" rx="10" ry="12" fill="{INK}" stroke="none"></ellipse>'
          f'<circle cx="139" cy="93" r="4" fill="#FFFFFF" stroke="none"></circle><circle cx="146" cy="102" r="1.8" fill="#FFFFFF" stroke="none"></circle>')
    s += f'<ellipse cx="132" cy="128" rx="10" ry="6" fill="{BLUSH}" opacity=".65" stroke="none"></ellipse>'
    s += f'<ellipse cx="185" cy="112" rx="7" ry="6" fill="{INK}" stroke="none"></ellipse>'
    s += f'<path d="M160 130 Q168 136 176 129" fill="none" stroke-width="3.5"></path>'
    return s


def dog_back():
    main, light, dark = DOG_MAIN, DOG_LIGHT, DOG_DARK
    s = base_body(main, None, light)
    s += f'<ellipse cx="100" cy="98" rx="68" ry="60" fill="{main}"></ellipse>'
    s += f'<path d="M94 50 Q100 40 106 50" fill="none" stroke="{dark}" stroke-width="4"></path>'
    s += f'<ellipse cx="40" cy="96" rx="15" ry="31" fill="{dark}" transform="rotate(18 40 96)"></ellipse>'
    s += f'<ellipse cx="160" cy="96" rx="15" ry="31" fill="{dark}" transform="rotate(-18 160 96)"></ellipse>'
    s += f'<path d="M98 200 C94 180 106 162 126 150 C120 166 112 182 110 200 Z" fill="{main}"></path>'
    return s


# Giro como persona
frames = [
    ("Frente", "0 s", svg(dog(), 190), "rotate(-4deg)"),
    ("Perfil", "0,6 s", svg(dog_side(), 190), "rotate(5deg) translateY(-8px)"),
    ("Espalda", "1,2 s", svg(dog_back(), 190), "rotate(-5deg)"),
    ("Perfil espejado", "1,8 s", svg(dog_side(), 190), "scaleX(-1) rotate(5deg) translateY(-8px)"),
    ("Frente", "2,4 s", svg(dog(expr="happy"), 190), "rotate(0deg)"),
]
arrow = '<svg viewBox="0 0 48 24" width="48" height="24" aria-hidden="true"><path d="M4 12 H40 M32 5 L41 12 L32 19" fill="none" stroke="#C9A58C" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"></path></svg>'
fr = []
for i, (label, t, art, tf) in enumerate(frames):
    if i:
        fr.append(f'<div style="display: flex; flex-direction: column; align-items: center; gap: 6px; padding-bottom: 96px;">{arrow}<div style="font-size: 12px; font-weight: 800; color: #8E6A52;">se afina</div></div>')
    fr.append(f'''<div style="display: flex; flex-direction: column; align-items: center; gap: 12px;">
<div style="height: 240px; display: flex; align-items: flex-end;"><div style="transform: {tf}; transform-origin: 50% 90%;">{art}</div></div>
<div style="display: flex; flex-direction: column; align-items: center; gap: 2px;">
<div style="font-family: Fredoka, Nunito, sans-serif; font-size: 22px; font-weight: 600;">{label}</div>
<div style="font-size: 14px; font-weight: 800; color: #8E5A44;">{t}</div>
</div>
</div>''')

specs = [
    ("Lento", "2,4 s la vuelta entera. Cada vista queda unos 0,5 s en pantalla."),
    ("Meneo", "A cada paso se inclina ±5° hacia un lado y el otro, con un saltito de 4 dp, como si diera pasitos en el lugar."),
    ("Cambio de vista", "Entre dibujo y dibujo se afina de costado (ancho 100 % → 15 % → 100 %) en 120 ms; en el punto más fino se cambia el dibujo y el ojo no nota el corte."),
    ("Qué hace falta", "Dos dibujos nuevos por mascota: perfil y espalda. El otro perfil es el mismo dado vuelta."),
]
sp = "".join(f'''<div style="display: flex; flex-direction: column; gap: 6px; padding: 20px 22px; background: #FFFFFF; border-radius: 20px;">
<div style="font-family: Fredoka, Nunito, sans-serif; font-size: 20px; font-weight: 600;">{a}</div>
<div style="font-size: 15px; line-height: 1.45; color: #75594B;">{b}</div>
</div>''' for a, b in specs)

giro_w, giro_h = 1440, 840
giro = f'''<div style="width: {giro_w}px; height: {giro_h}px; box-sizing: border-box; padding: 56px; display: flex; flex-direction: column; gap: 32px; background: #FFF8EF;">
<div style="display: flex; flex-direction: column; gap: 8px;">
<h2 style="margin: 0; font-family: Fredoka, Nunito, sans-serif; font-size: 40px; font-weight: 600;">Giro: se da la vuelta despacito</h2>
<p style="margin: 0; font-size: 17px; color: #75594B;">En vez de rotar como una rueda, gira sobre sí misma mostrando el perfil y la espalda, meneándose a cada paso.</p>
</div>
<div style="display: flex; align-items: flex-end; justify-content: space-between; padding: 28px 36px; background: #FFF1DF; border-radius: 32px;">
{"".join(fr)}
</div>
<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 20px;">
{sp}
</div>
</div>'''

zzz = '<div style="position: absolute; right: 36px; top: 20px; display: flex; align-items: flex-end; gap: 4px; font-family: Fredoka, Nunito, sans-serif; font-weight: 700; color: #7FA7D6;"><span style="font-size: 22px;">z</span><span style="font-size: 30px; padding-bottom: 12px;">z</span><span style="font-size: 40px; padding-bottom: 26px;">Z</span></div>'

FIELD = "font: inherit; font-size: 16px; font-weight: 700; color: #4A3228; padding: 8px 12px; background: #FFFFFF; border: 2px solid #E6D3BF; border-radius: 12px;"
CHECK = "width: 22px; height: 22px; margin: 0; accent-color: #C8642F;"
settings = f'''<div style="width: 100%; height: 100%; display: flex; flex-direction: column; justify-content: center; gap: 14px;">
<div style="display: flex; flex-direction: column; gap: 10px; padding: 16px 18px; background: #FFFFFF; border-radius: 20px;">
<label style="display: flex; align-items: center; gap: 12px; font-size: 16px; font-weight: 800;"><input type="checkbox" checked style="{CHECK}"><span>Sin avisos por un rato</span></label>
<label style="display: flex; align-items: center; gap: 10px; padding-left: 34px; font-size: 15px; color: #75594B;"><span>Después de</span><select style="{FIELD}"><option>1 hora</option><option selected>2 horas</option><option>4 horas</option></select></label>
</div>
<div style="display: flex; flex-direction: column; gap: 10px; padding: 16px 18px; background: #FFFFFF; border-radius: 20px;">
<label style="display: flex; align-items: center; gap: 12px; font-size: 16px; font-weight: 800;"><input type="checkbox" checked style="{CHECK}"><span>En mi horario de sueño</span></label>
<div style="display: flex; align-items: center; gap: 10px; padding-left: 34px; font-size: 15px; color: #75594B;">
<label style="display: flex; align-items: center; gap: 8px;"><span>De</span><input type="time" value="22:00" style="{FIELD}"></label>
<label style="display: flex; align-items: center; gap: 8px;"><span>a</span><input type="time" value="07:00" style="{FIELD}"></label>
</div>
</div>
</div>'''

pose_cards = [
    ("Acostada", "Primer paso hacia dormir: se echa con la cabeza sobre las patitas y respira más lento.",
     dog_lying("open", "smile").format(w=290, h=242), ""),
    ("Dormida", "Cierra los ojos y flotan las zetas. Un aviso nuevo la despierta: se estira, se para y habla.",
     dog_lying("blink", "smile").format(w=290, h=242), zzz),
    ("Cuándo se duerme", "Las dos opciones pueden estar activas; se duerme con la primera que se cumpla.", settings, ""),
]
pc = []
for label, desc, art, extra in pose_cards:
    pc.append(f'''<div style="display: flex; flex-direction: column; gap: 18px; padding: 24px; background: #FFF1DF; border-radius: 32px;">
<div style="position: relative; height: 290px; display: flex; align-items: flex-end; justify-content: center;">
{art}
{extra}
</div>
<div style="display: flex; flex-direction: column; gap: 6px;">
<div style="font-family: Fredoka, Nunito, sans-serif; font-size: 26px; font-weight: 600;">{label}</div>
<div style="font-size: 16px; line-height: 1.45; color: #75594B;">{desc}</div>
</div>
</div>''')
pos_w, pos_h = 1440, 700
poses = f'''<div style="width: {pos_w}px; height: {pos_h}px; box-sizing: border-box; padding: 56px; display: flex; flex-direction: column; gap: 32px; background: #FFF8EF;">
<div style="display: flex; flex-direction: column; gap: 8px;">
<h2 style="margin: 0; font-family: Fredoka, Nunito, sans-serif; font-size: 40px; font-weight: 600;">Hora de dormir</h2>
<p style="margin: 0; font-size: 17px; color: #75594B;">Propuesta con el perro; si te convence, se replica en las otras nueve.</p>
</div>
<div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 28px;">
{"".join(pc)}
</div>
</div>'''


# ---------- Todas las vistas: perfil, espalda, dormida ----------

def eye1(x, y, r=(10, 12)):
    return (f'<ellipse cx="{x}" cy="{y}" rx="{r[0]}" ry="{r[1]}" fill="{INK}" stroke="none"></ellipse>'
            f'<circle cx="{x-3}" cy="{y-5}" r="4" fill="#FFFFFF" stroke="none"></circle>'
            f'<circle cx="{x+4}" cy="{y+4}" r="1.8" fill="#FFFFFF" stroke="none"></circle>')


def blush1(x, y):
    return f'<ellipse cx="{x}" cy="{y}" rx="10" ry="6" fill="{BLUSH}" opacity=".65" stroke="none"></ellipse>'


def side_body(main, belly=None, feet=None, far=None):
    feet = feet or main
    far = far or feet
    s = (f'<ellipse cx="88" cy="212" rx="14" ry="9" fill="{far}"></ellipse><ellipse cx="142" cy="212" rx="14" ry="9" fill="{far}"></ellipse>'
         f'<ellipse cx="72" cy="214" rx="15" ry="10" fill="{feet}"></ellipse><ellipse cx="126" cy="214" rx="15" ry="10" fill="{feet}"></ellipse>'
         f'<ellipse cx="96" cy="178" rx="48" ry="34" fill="{main}"></ellipse>')
    if belly:
        s += f'<ellipse cx="104" cy="196" rx="30" ry="11" fill="{belly}" stroke="none"></ellipse>'
    s += f'<ellipse cx="128" cy="190" rx="10" ry="15" fill="{main}" transform="rotate(-10 128 190)"></ellipse>'
    return s


def back_body(main, feet=None):
    feet = feet or main
    return (f'<ellipse cx="80" cy="214" rx="15" ry="10" fill="{feet}"></ellipse>'
            f'<ellipse cx="120" cy="214" rx="15" ry="10" fill="{feet}"></ellipse>'
            f'<ellipse cx="100" cy="176" rx="42" ry="38" fill="{main}"></ellipse>'
            f'<ellipse cx="66" cy="180" rx="10" ry="15" fill="{main}" transform="rotate(22 66 180)"></ellipse>'
            f'<ellipse cx="134" cy="180" rx="10" ry="15" fill="{main}" transform="rotate(-22 134 180)"></ellipse>')


def rope(d, main, w=10):
    return (f'<path d="{d}" fill="none" stroke-width="{w}"></path>'
            f'<path d="{d}" fill="none" stroke="{main}" stroke-width="{w-6}"></path>')


def mane_path(cx, cy, R, n=14):
    pts = [(cx + R * math.cos(2 * math.pi * i / n - math.pi / 2), cy + R * math.sin(2 * math.pi * i / n - math.pi / 2)) for i in range(n)]
    r = R * math.sin(math.pi / n) * 1.15
    return f"M{pts[0][0]:.1f} {pts[0][1]:.1f} " + " ".join(
        f"A{r:.1f} {r:.1f} 0 0 1 {pts[(i+1)%n][0]:.1f} {pts[(i+1)%n][1]:.1f}" for i in range(n)) + " Z"


def lying(main, feet, tail, head, back_leg=None, body_extra=""):
    s = tail
    s += back_leg if back_leg is not None else f'<ellipse cx="180" cy="166" rx="22" ry="16" fill="{main}"></ellipse>'
    s += f'<ellipse cx="138" cy="148" rx="54" ry="34" fill="{main}"></ellipse>' + body_extra
    s += f'<ellipse cx="196" cy="178" rx="14" ry="8" fill="{feet}"></ellipse>'
    s += f'<g transform="translate(-14 14)">{head}</g>'
    s += f'<ellipse cx="62" cy="178" rx="17" ry="9" fill="{feet}"></ellipse>'
    s += f'<ellipse cx="104" cy="180" rx="17" ry="9" fill="{feet}"></ellipse>'
    return s


def svg_lying(inner, width=180):
    h = int(width * 200 / 256)
    return (f'<svg viewBox="-16 0 256 200" width="{width}" height="{h}" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">'
            f'<ellipse cx="124" cy="188" rx="98" ry="7" fill="{INK}" opacity=".12" stroke="none"></ellipse>'
            f'<g {W}>{inner}</g></svg>')


# --- cabezas de frente con ojos variables (para acostados) ---

def head_cat(e):
    return cat_ears() + cat_face(e)

def head_frog(e):
    main = "#A6D98A"
    s = f'<circle cx="66" cy="62" r="27" fill="{main}"></circle><circle cx="134" cy="62" r="27" fill="{main}"></circle>'
    s += f'<ellipse cx="100" cy="108" rx="74" ry="50" fill="{main}"></ellipse>'
    s += f'<circle cx="66" cy="62" r="25" fill="{main}" stroke="none"></circle><circle cx="134" cy="62" r="25" fill="{main}" stroke="none"></circle>'
    s += f'<rect x="50" y="62" width="100" height="30" fill="{main}" stroke="none"></rect>'
    s += eyes(66, 134, 64, e) + blush(46, 154, 112)
    s += f'<path d="M70 116 Q100 138 130 116" fill="none" stroke-width="4"></path>'
    s += f'<circle cx="92" cy="100" r="2" fill="{INK}" stroke="none"></circle><circle cx="108" cy="100" r="2" fill="{INK}" stroke="none"></circle>'
    return s


def head_cow(e):
    main, spot, muzzle, horn = "#FFFCF7", "#7A5646", "#FFC4CE", "#F3DFB5"
    s = (f'<path d="M66 50 Q58 26 72 24 Q72 38 80 44 Z" fill="{horn}"></path>'
         f'<path d="M134 50 Q142 26 128 24 Q128 38 120 44 Z" fill="{horn}"></path>')
    s += (f'<ellipse cx="32" cy="84" rx="22" ry="11" fill="{main}" transform="rotate(-18 32 84)"></ellipse>'
          f'<ellipse cx="168" cy="84" rx="22" ry="11" fill="{main}" transform="rotate(18 168 84)"></ellipse>'
          f'<ellipse cx="30" cy="85" rx="12" ry="5" fill="{muzzle}" stroke="none" transform="rotate(-18 30 85)"></ellipse>'
          f'<ellipse cx="170" cy="85" rx="12" ry="5" fill="{muzzle}" stroke="none" transform="rotate(18 170 85)"></ellipse>')
    s += f'<ellipse cx="100" cy="98" rx="68" ry="60" fill="{main}"></ellipse>'
    s += f'<path d="M114 54 C128 48 142 58 140 70 C136 82 120 78 116 70 C112 64 110 58 114 54 Z" fill="{spot}" stroke="none"></path>'
    s += eyes(72, 128, 94, e) + blush(50, 150, 110)
    s += f'<ellipse cx="100" cy="126" rx="34" ry="20" fill="{muzzle}"></ellipse>'
    s += f'<ellipse cx="88" cy="124" rx="4" ry="5.5" fill="#C97884" stroke="none"></ellipse><ellipse cx="112" cy="124" rx="4" ry="5.5" fill="#C97884" stroke="none"></ellipse>'
    s += f'<path d="M93 135 Q100 140 107 135" fill="none" stroke-width="3"></path>'
    return s


def head_pig(e):
    main, dark, snout = "#FFC6D1", "#F29BB0", "#FFB0C0"
    s = (f'<path d="M44 70 Q36 30 58 22 Q76 34 86 50 Z" fill="{main}"></path>'
         f'<path d="M156 70 Q164 30 142 22 Q124 34 114 50 Z" fill="{main}"></path>'
         f'<path d="M54 54 Q52 38 60 32 Q70 40 76 50 Z" fill="{dark}" stroke="none"></path>'
         f'<path d="M146 54 Q148 38 140 32 Q130 40 124 50 Z" fill="{dark}" stroke="none"></path>')
    s += f'<ellipse cx="100" cy="98" rx="68" ry="60" fill="{main}"></ellipse>'
    s += eyes(72, 128, 96, e) + blush(52, 148, 120)
    s += f'<ellipse cx="100" cy="122" rx="22" ry="15" fill="{snout}"></ellipse>'
    s += f'<ellipse cx="92" cy="122" rx="3.5" ry="5" fill="#C9677E" stroke="none"></ellipse><ellipse cx="108" cy="122" rx="3.5" ry="5" fill="#C9677E" stroke="none"></ellipse>'
    s += f'<path d="M94 142 Q100 147 106 142" fill="none" stroke-width="3"></path>'
    return s


def head_lion(e):
    main, mane, light = "#F7C873", "#E0873C", "#FFF1D6"
    s = f'<path d="{mane_path(100, 94, 72)}" fill="{mane}"></path>'
    s += (f'<circle cx="56" cy="52" r="14" fill="{main}"></circle><circle cx="144" cy="52" r="14" fill="{main}"></circle>'
          f'<circle cx="56" cy="52" r="7" fill="{light}" stroke="none"></circle><circle cx="144" cy="52" r="7" fill="{light}" stroke="none"></circle>')
    s += f'<ellipse cx="100" cy="100" rx="58" ry="52" fill="{main}"></ellipse>'
    s += eyes(76, 124, 98, e) + blush(58, 142, 120)
    s += f'<ellipse cx="100" cy="124" rx="22" ry="15" fill="{light}"></ellipse>'
    s += f'<path d="M92 113 Q100 109 108 113 Q104 121 100 121 Q96 121 92 113 Z" fill="#7A4A2E" stroke-width="2.5"></path>'
    s += wmouth(100, 125)
    return s


def head_monkey(e):
    main, face = "#A9744F", "#F5D2AE"
    s = (f'<circle cx="32" cy="102" r="20" fill="{main}"></circle><circle cx="168" cy="102" r="20" fill="{main}"></circle>'
         f'<circle cx="32" cy="102" r="11" fill="{face}" stroke="none"></circle><circle cx="168" cy="102" r="11" fill="{face}" stroke="none"></circle>')
    s += f'<ellipse cx="100" cy="98" rx="64" ry="58" fill="{main}"></ellipse>'
    s += f'<path d="M96 44 Q100 30 111 36" fill="none" stroke-width="3.5"></path>'
    s += f'<path d="M100 72 C82 54 46 62 50 100 C52 134 76 148 100 148 C124 148 148 134 150 100 C154 62 118 54 100 72 Z" fill="{face}"></path>'
    s += eyes(78, 122, 100, e) + blush(62, 138, 124)
    s += f'<circle cx="95" cy="118" r="2.3" fill="{INK}" stroke="none"></circle><circle cx="105" cy="118" r="2.3" fill="{INK}" stroke="none"></circle>'
    s += f'<path d="M84 128 Q100 142 116 128" fill="none" stroke-width="3.5"></path>'
    return s


# --- aves con ojos variables (dormidas = esponjadas, patas escondidas) ---

def duck_v(e="open", feet=True):
    main, bill, scarf = "#FFEFA8", "#FFA94D", "#7FB8E8"
    wing, tuft = "#FFE07A", "#FFD95E"
    s = f'<path d="M140 176 L160 166 L152 184 Z" fill="{main}"></path>'
    if feet:
        s += (f'<path d="M66 206 L94 206 L80 222 L72 214 L66 222 Z" fill="{bill}"></path>'
              f'<path d="M106 206 L134 206 L134 222 L128 214 L120 222 Z" fill="{bill}"></path>')
        s += f'<ellipse cx="100" cy="176" rx="42" ry="38" fill="{main}"></ellipse>'
    else:
        s += f'<ellipse cx="100" cy="190" rx="54" ry="32" fill="{main}"></ellipse>'
    s += (f'<ellipse cx="62" cy="180" rx="11" ry="20" fill="{wing}" transform="rotate(28 62 180)"></ellipse>'
          f'<ellipse cx="138" cy="180" rx="11" ry="20" fill="{wing}" transform="rotate(-28 138 180)"></ellipse>')
    s += f'<ellipse cx="100" cy="190" rx="24" ry="18" fill="#FFF8DC" stroke="none"></ellipse>'
    s += f'<path d="M98 42 C90 26 98 16 106 22 C104 28 110 30 114 26 C116 36 108 42 104 44 Z" fill="{tuft}"></path>'
    s += f'<ellipse cx="100" cy="98" rx="66" ry="60" fill="{main}"></ellipse>'
    s += (f'<path d="M100 156 L78 146 L80 168 Z" fill="{scarf}"></path><path d="M100 156 L122 146 L120 168 Z" fill="{scarf}"></path>'
          f'<circle cx="100" cy="156" r="6" fill="{scarf}"></circle>')
    s += eyes(72, 128, 98, e) + blush(54, 146, 120)
    s += f'<ellipse cx="100" cy="124" rx="24" ry="11" fill="{bill}"></ellipse>'
    s += f'<path d="M82 124 Q100 130 118 124" fill="none" stroke-width="3"></path>'
    return s


def chick_v(e="open", feet=True):
    main, dark, beak = "#FFE27A", "#F7C948", "#FF9F43"
    s = f'<ellipse cx="100" cy="{132 if feet else 140}" rx="{68 if feet else 74}" ry="{80 if feet else 74}" fill="{main}"></ellipse>'
    if feet:
        s += toes(beak)
    s += (f'<ellipse cx="36" cy="150" rx="12" ry="22" fill="{dark}" transform="rotate(24 36 150)"></ellipse>'
          f'<ellipse cx="164" cy="150" rx="12" ry="22" fill="{dark}" transform="rotate(-24 164 150)"></ellipse>')
    s += (f'<g transform="rotate(-10 100 44) translate(0 {0 if feet else 12})"><path d="M64 64 Q62 24 100 20 Q138 24 136 64 L127 55 L118 65 L109 55 L100 65 L91 55 L82 65 L73 55 Z" fill="#FFFDF5"></path></g>')
    s += eyes(76, 124, 112, e) + blush(56, 144, 134)
    s += f'<path d="M90 126 L110 126 L100 139 Z" fill="{beak}" stroke-width="3"></path>'
    return s


def owl_v(e="open", feet=True):
    main, dark, light, disc = "#A88470", "#7E5E4E", "#F3E3CF", "#EBD5BD"
    s = f'<path d="M42 78 L40 34 L76 56 Z" fill="{main}"></path><path d="M158 78 L160 34 L124 56 Z" fill="{main}"></path>'
    s += f'<ellipse cx="100" cy="{130 if feet else 136}" rx="{70 if feet else 76}" ry="{82 if feet else 76}" fill="{main}"></ellipse>'
    if feet:
        s += toes("#FFA94D")
    s += f'<ellipse cx="100" cy="174" rx="38" ry="32" fill="{light}" stroke="none"></ellipse>'
    for (x, y) in ((86, 160), (100, 156), (114, 160), (93, 174), (107, 174), (100, 188)):
        s += f'<path d="M{x-5} {y} Q{x} {y+5} {x+5} {y}" fill="none" stroke="{dark}" stroke-width="3"></path>'
    s += (f'<ellipse cx="36" cy="150" rx="14" ry="32" fill="{dark}" transform="rotate(14 36 150)"></ellipse>'
          f'<ellipse cx="164" cy="150" rx="14" ry="32" fill="{dark}" transform="rotate(-14 164 150)"></ellipse>')
    s += f'<circle cx="74" cy="100" r="27" fill="{disc}" stroke="none"></circle><circle cx="126" cy="100" r="27" fill="{disc}" stroke="none"></circle>'
    s += eyes(74, 126, 100, e, r=(12, 14)) + blush(50, 150, 126)
    s += f'<path d="M93 118 L107 118 L100 130 Z" fill="#FFB347" stroke-width="3"></path>'
    return s


# --- perfiles ---

def cat_side():
    main, light, dark = "#F7B77A", "#FFF3E4", "#E08E4B"
    s = f'<path d="M52 176 C24 170 22 130 36 116 C42 112 48 118 44 124 C36 138 40 160 58 164 Z" fill="{main}"></path>'
    s += side_body(main, light, light, "#F2DCC6")
    s += f'<path d="M118 50 L140 20 L152 60 Z" fill="{main}"></path>'
    s += f'<path d="M80 66 L88 22 L122 48 Z" fill="{main}"></path><path d="M90 54 L94 34 L110 48 Z" fill="#FFB3BF" stroke="none"></path>'
    s += f'<ellipse cx="112" cy="98" rx="62" ry="58" fill="{main}"></ellipse>'
    s += f'<path d="M104 44 Q106 50 105 55 M114 42 L114 52" fill="none" stroke="{CAT_SOFT}" stroke-width="4"></path>'
    s += f'<ellipse cx="164" cy="118" rx="18" ry="14" fill="{light}"></ellipse>'
    s += eye1(142, 98) + blush1(134, 124)
    s += f'<path d="M176 108 L184 110 L178 116 Z" fill="#FF8FA3" stroke-width="2.5"></path>'
    s += f'<path d="M168 126 Q173 131 179 127" fill="none" stroke-width="3"></path>'
    s += f'<path d="M158 116 L190 110 M158 122 L190 124" fill="none" stroke-width="3"></path>'
    return s


def duck_side():
    main, bill, scarf, wing, tuft = "#FFEFA8", "#FFA94D", "#7FB8E8", "#FFE07A", "#FFD95E"
    s = f'<path d="M84 206 L110 206 L100 222 L92 214 L84 222 Z" fill="{bill}"></path>'
    s += f'<path d="M56 168 L32 152 L46 184 Z" fill="{main}"></path>'
    s += f'<ellipse cx="100" cy="178" rx="50" ry="36" fill="{main}"></ellipse>'
    s += f'<ellipse cx="108" cy="198" rx="30" ry="10" fill="#FFF8DC" stroke="none"></ellipse>'
    s += f'<ellipse cx="90" cy="174" rx="28" ry="17" fill="{wing}" transform="rotate(-8 90 174)"></ellipse>'
    s += f'<path d="M112 46 C106 30 114 22 122 28 C120 34 126 36 130 32 C132 42 124 48 120 48 Z" fill="{tuft}"></path>'
    s += f'<ellipse cx="116" cy="100" rx="58" ry="56" fill="{main}"></ellipse>'
    s += (f'<path d="M110 154 L94 146 L96 164 Z" fill="{scarf}"></path><path d="M110 154 L126 146 L124 164 Z" fill="{scarf}"></path>'
          f'<circle cx="110" cy="154" r="6" fill="{scarf}"></circle>')
    s += eye1(144, 98) + blush1(138, 122)
    s += f'<path d="M160 112 Q194 108 198 121 Q194 132 160 130 Z" fill="{bill}"></path>'
    s += f'<path d="M166 121 L192 121" fill="none" stroke-width="3"></path>'
    return s


def frog_side():
    main, light = "#A6D98A", "#E6F5D0"
    s = f'<ellipse cx="136" cy="214" rx="14" ry="7" fill="{main}"></ellipse>'
    s += f'<ellipse cx="96" cy="178" rx="46" ry="34" fill="{main}"></ellipse>'
    s += f'<ellipse cx="104" cy="196" rx="26" ry="11" fill="{light}" stroke="none"></ellipse>'
    s += f'<ellipse cx="68" cy="214" rx="24" ry="8" fill="{main}"></ellipse>'
    s += f'<ellipse cx="78" cy="192" rx="30" ry="19" fill="{main}"></ellipse>'
    s += f'<ellipse cx="130" cy="196" rx="8" ry="16" fill="{main}"></ellipse>'
    s += f'<circle cx="128" cy="68" r="26" fill="{main}"></circle>'
    s += f'<ellipse cx="116" cy="112" rx="60" ry="46" fill="{main}"></ellipse>'
    s += f'<circle cx="128" cy="68" r="24" fill="{main}" stroke="none"></circle>'
    s += eye1(134, 66) + blush1(148, 112)
    s += f'<circle cx="170" cy="98" r="2" fill="{INK}" stroke="none"></circle>'
    s += f'<path d="M124 122 Q156 132 174 114" fill="none" stroke-width="4"></path>'
    return s


def cow_side():
    main, spot, muzzle, horn = "#FFFCF7", "#7A5646", "#FFC4CE", "#F3DFB5"
    s = f'<path d="M50 170 Q30 176 30 196" fill="none" stroke-width="4"></path><circle cx="30" cy="200" r="6" fill="{spot}"></circle>'
    s += side_body(main, None, spot, spot)
    s += f'<path d="M62 168 C70 158 88 162 86 176 C84 188 68 190 62 180 Z" fill="{spot}" stroke="none"></path>'
    s += f'<path d="M108 50 Q102 26 116 24 Q116 38 124 44 Z" fill="{horn}"></path>'
    s += f'<ellipse cx="112" cy="98" rx="60" ry="56" fill="{main}"></ellipse>'
    s += f'<path d="M72 64 C84 54 102 60 100 74 C96 86 80 84 74 76 C70 72 68 68 72 64 Z" fill="{spot}" stroke="none"></path>'
    s += (f'<ellipse cx="80" cy="92" rx="24" ry="11" fill="{main}" transform="rotate(-25 80 92)"></ellipse>'
          f'<ellipse cx="78" cy="93" rx="13" ry="5" fill="{muzzle}" stroke="none" transform="rotate(-25 78 93)"></ellipse>')
    s += f'<path d="M88 146 Q110 158 134 148" fill="none" stroke="#E86A6A" stroke-width="7"></path>'
    s += f'<path d="M100 154 Q110 148 120 154 L122 168 Q110 174 98 168 Z" fill="#FFD166"></path>'
    s += eye1(136, 92) + blush1(132, 114)
    s += f'<ellipse cx="164" cy="122" rx="28" ry="21" fill="{muzzle}"></ellipse>'
    s += f'<ellipse cx="182" cy="118" rx="4" ry="5.5" fill="#C97884" stroke="none"></ellipse>'
    s += f'<path d="M160 136 Q166 141 173 136" fill="none" stroke-width="3"></path>'
    return s


def pig_side():
    main, dark, snout = "#FFC6D1", "#F29BB0", "#FFB0C0"
    s = f'<path d="M50 170 c-10 -4 -15 5 -9 10 c6 4 11 -3 5 -8 c-6 -4 -14 -2 -16 -9" fill="none" stroke-width="4"></path>'
    s += side_body(main, "#FFDDE4", dark, "#E88AA0")
    s += f'<ellipse cx="112" cy="98" rx="62" ry="58" fill="{main}"></ellipse>'
    s += f'<path d="M86 66 Q82 26 104 20 Q120 34 124 52 Z" fill="{main}"></path><path d="M96 56 Q94 38 104 32 Q114 42 116 52 Z" fill="{dark}" stroke="none"></path>'
    s += eye1(142, 96) + blush1(134, 122)
    s += f'<path d="M158 104 L176 104 Q188 118 176 132 L158 132 Z" fill="{snout}"></path>'
    s += f'<ellipse cx="178" cy="118" rx="8" ry="14" fill="{snout}"></ellipse>'
    s += f'<ellipse cx="180" cy="114" rx="2.5" ry="4" fill="#C9677E" stroke="none"></ellipse>'
    s += f'<path d="M154 140 Q160 144 166 140" fill="none" stroke-width="3"></path>'
    return s


def chick_side():
    main, dark, beak = "#FFE27A", "#F7C948", "#FF9F43"
    s = f'<path d="M40 150 L20 140 L32 168 Z" fill="{main}"></path>'
    s += f'<ellipse cx="100" cy="132" rx="66" ry="80" fill="{main}"></ellipse>'
    for dx, ex in ((-6, -2), (0, 8), (6, 16)):
        s += f'<path d="M{100+dx} 208 L{100+ex} 222" fill="none" stroke="{beak}" stroke-width="5"></path>'
    s += f'<ellipse cx="90" cy="150" rx="15" ry="26" fill="{dark}" transform="rotate(-18 90 150)"></ellipse>'
    s += (f'<g transform="rotate(-14 100 44)"><path d="M64 64 Q62 24 100 20 Q138 24 136 64 L127 55 L118 65 L109 55 L100 65 L91 55 L82 65 L73 55 Z" fill="#FFFDF5"></path></g>')
    s += eye1(138, 110) + blush1(132, 132)
    s += f'<path d="M162 116 L186 124 L162 132 Z" fill="{beak}" stroke-width="3"></path>'
    return s


def lion_side():
    main, mane, light = "#F7C873", "#E0873C", "#FFF1D6"
    s = rope("M52 176 Q22 180 26 150", main) + f'<circle cx="26" cy="146" r="9" fill="{mane}"></circle>'
    s += side_body(main, light, light, "#F2DCB8")
    s += f'<path d="{mane_path(108, 96, 66)}" fill="{mane}"></path>'
    s += f'<circle cx="112" cy="50" r="13" fill="{main}"></circle><circle cx="112" cy="50" r="6" fill="{light}" stroke="none"></circle>'
    s += f'<ellipse cx="120" cy="100" rx="52" ry="48" fill="{main}"></ellipse>'
    s += f'<ellipse cx="160" cy="122" rx="20" ry="15" fill="{light}"></ellipse>'
    s += eye1(142, 98) + blush1(136, 122)
    s += f'<path d="M170 110 Q178 108 181 114 Q178 120 172 119 Z" fill="#7A4A2E" stroke-width="2.5"></path>'
    s += f'<path d="M154 132 Q160 136 167 131" fill="none" stroke-width="3"></path>'
    return s


def monkey_side():
    main, face = "#A9744F", "#F5D2AE"
    s = rope("M52 180 C14 184 10 138 32 130 C44 126 50 140 40 144", main, 11)
    s += side_body(main, face, main, "#8A5A3A")
    s += f'<ellipse cx="112" cy="98" rx="60" ry="56" fill="{main}"></ellipse>'
    s += f'<path d="M110 44 Q116 30 126 38" fill="none" stroke-width="3.5"></path>'
    s += f'<circle cx="94" cy="104" r="18" fill="{main}"></circle><circle cx="94" cy="104" r="10" fill="{face}" stroke="none"></circle>'
    s += f'<path d="M118 70 C146 58 172 80 172 106 C172 132 150 146 126 144 C116 130 114 96 118 70 Z" fill="{face}"></path>'
    s += f'<ellipse cx="164" cy="122" rx="20" ry="16" fill="{face}"></ellipse>'
    s += eye1(146, 98) + blush1(132, 122)
    s += f'<circle cx="178" cy="116" r="2.2" fill="{INK}" stroke="none"></circle>'
    s += f'<path d="M156 132 Q166 138 174 130" fill="none" stroke-width="3.5"></path>'
    return s


def owl_side():
    main, dark, light, disc = "#A88470", "#7E5E4E", "#F3E3CF", "#EBD5BD"
    s = f'<path d="M124 58 L140 32 L152 70 Z" fill="{main}"></path><path d="M92 62 L100 28 L126 54 Z" fill="{main}"></path>'
    s += f'<ellipse cx="100" cy="130" rx="64" ry="82" fill="{main}"></ellipse>'
    for dx, ex in ((-6, -2), (0, 8), (6, 16)):
        s += f'<path d="M{102+dx} 208 L{102+ex} 222" fill="none" stroke="#FFA94D" stroke-width="5"></path>'
    s += f'<ellipse cx="128" cy="172" rx="24" ry="32" fill="{light}" stroke="none"></ellipse>'
    s += f'<ellipse cx="84" cy="150" rx="26" ry="42" fill="{dark}" transform="rotate(-8 84 150)"></ellipse>'
    s += f'<circle cx="140" cy="100" r="25" fill="{disc}" stroke="none"></circle>'
    s += eye1(144, 100, (12, 14)) + blush1(150, 128)
    s += f'<path d="M164 110 L180 118 L164 126 Z" fill="#FFB347" stroke-width="3"></path>'
    return s


# --- espaldas ---

def cat_back():
    main, dark = "#F7B77A", "#E08E4B"
    s = f'<path d="M42 76 Q42 30 52 24 Q66 30 90 48 Z" fill="{main}"></path><path d="M158 76 Q158 30 148 24 Q134 30 110 48 Z" fill="{main}"></path>'
    s += back_body(main, "#FFF3E4")
    s += f'<ellipse cx="100" cy="98" rx="68" ry="60" fill="{main}"></ellipse>'
    s += f'<path d="M86 48 Q88 57 87 64 M100 42 L100 62 M114 48 Q112 57 113 64 M100 150 L100 166" fill="none" stroke="{CAT_SOFT}" stroke-width="4"></path>'
    s += f'<path d="M100 206 C102 180 128 170 140 150 C142 144 150 146 148 154 C136 174 118 186 114 206 Z" fill="{main}"></path>'
    return s


def duck_back():
    main, scarf, wing, tuft = "#FFEFA8", "#7FB8E8", "#FFE07A", "#FFD95E"
    s = f'<path d="M66 206 L94 206 L80 218 Z" fill="#FFA94D"></path><path d="M106 206 L134 206 L120 218 Z" fill="#FFA94D"></path>'
    s += f'<ellipse cx="100" cy="176" rx="42" ry="38" fill="{main}"></ellipse>'
    s += (f'<ellipse cx="76" cy="176" rx="16" ry="26" fill="{wing}" transform="rotate(14 76 176)"></ellipse>'
          f'<ellipse cx="124" cy="176" rx="16" ry="26" fill="{wing}" transform="rotate(-14 124 176)"></ellipse>')
    s += f'<path d="M88 200 Q100 218 112 200 Q100 206 88 200 Z" fill="{main}"></path>'
    s += f'<path d="M98 42 C90 26 98 16 106 22 C104 28 110 30 114 26 C116 36 108 42 104 44 Z" fill="{tuft}"></path>'
    s += f'<ellipse cx="100" cy="98" rx="66" ry="60" fill="{main}"></ellipse>'
    s += f'<path d="M100 152 L90 176 L100 170 L110 176 Z" fill="{scarf}"></path><circle cx="100" cy="152" r="6" fill="{scarf}"></circle>'
    return s


def frog_back():
    main, dot = "#A6D98A", "#86C06A"
    s = (f'<ellipse cx="78" cy="214" rx="19" ry="9" fill="{main}"></ellipse>'
         f'<ellipse cx="122" cy="214" rx="19" ry="9" fill="{main}"></ellipse>'
         f'<ellipse cx="100" cy="178" rx="42" ry="36" fill="{main}"></ellipse>'
         f'<ellipse cx="66" cy="182" rx="10" ry="15" fill="{main}" transform="rotate(22 66 182)"></ellipse>'
         f'<ellipse cx="134" cy="182" rx="10" ry="15" fill="{main}" transform="rotate(-22 134 182)"></ellipse>')
    s += f'<circle cx="84" cy="170" r="7" fill="{dot}" stroke="none"></circle><circle cx="116" cy="182" r="9" fill="{dot}" stroke="none"></circle><circle cx="96" cy="196" r="5" fill="{dot}" stroke="none"></circle>'
    s += f'<circle cx="66" cy="62" r="27" fill="{main}"></circle><circle cx="134" cy="62" r="27" fill="{main}"></circle>'
    s += f'<ellipse cx="100" cy="108" rx="74" ry="50" fill="{main}"></ellipse>'
    s += f'<circle cx="66" cy="62" r="25" fill="{main}" stroke="none"></circle><circle cx="134" cy="62" r="25" fill="{main}" stroke="none"></circle>'
    s += f'<rect x="50" y="62" width="100" height="30" fill="{main}" stroke="none"></rect>'
    s += f'<circle cx="84" cy="106" r="8" fill="{dot}" stroke="none"></circle><circle cx="118" cy="98" r="6" fill="{dot}" stroke="none"></circle><circle cx="104" cy="126" r="5" fill="{dot}" stroke="none"></circle>'
    return s


def cow_back():
    main, spot, muzzle, horn = "#FFFCF7", "#7A5646", "#FFC4CE", "#F3DFB5"
    s = (f'<path d="M66 50 Q58 26 72 24 Q72 38 80 44 Z" fill="{horn}"></path>'
         f'<path d="M134 50 Q142 26 128 24 Q128 38 120 44 Z" fill="{horn}"></path>')
    s += (f'<ellipse cx="32" cy="84" rx="22" ry="11" fill="{main}" transform="rotate(-18 32 84)"></ellipse>'
          f'<ellipse cx="168" cy="84" rx="22" ry="11" fill="{main}" transform="rotate(18 168 84)"></ellipse>')
    s += back_body(main, spot)
    s += f'<path d="M104 160 C118 150 136 160 134 176 C132 190 112 192 106 182 C100 174 98 166 104 160 Z" fill="{spot}" stroke="none"></path>'
    s += f'<ellipse cx="100" cy="98" rx="68" ry="60" fill="{main}"></ellipse>'
    s += f'<path d="M70 70 C84 60 104 68 102 84 C98 98 80 96 74 88 C68 82 66 76 70 70 Z" fill="{spot}" stroke="none"></path>'
    s += f'<path d="M72 144 Q100 158 128 144" fill="none" stroke="#E86A6A" stroke-width="7"></path>'
    s += f'<path d="M100 186 Q106 202 98 212" fill="none" stroke-width="4"></path><circle cx="97" cy="215" r="6" fill="{spot}"></circle>'
    return s


def pig_back():
    main, dark = "#FFC6D1", "#F29BB0"
    s = (f'<path d="M44 70 Q36 30 58 22 Q76 34 86 50 Z" fill="{main}"></path>'
         f'<path d="M156 70 Q164 30 142 22 Q124 34 114 50 Z" fill="{main}"></path>')
    s += back_body(main, dark)
    s += f'<ellipse cx="100" cy="98" rx="68" ry="60" fill="{main}"></ellipse>'
    s += f'<path d="M98 190 c10 -4 15 5 9 10 c-6 4 -11 -3 -5 -8 c6 -4 14 -2 16 -9" fill="none" stroke-width="4"></path>'
    return s


def chick_back():
    main, dark, beak = "#FFE27A", "#F7C948", "#FF9F43"
    s = f'<ellipse cx="100" cy="132" rx="68" ry="80" fill="{main}"></ellipse>'
    s += toes(beak)
    s += (f'<ellipse cx="40" cy="146" rx="13" ry="24" fill="{dark}" transform="rotate(18 40 146)"></ellipse>'
          f'<ellipse cx="160" cy="146" rx="13" ry="24" fill="{dark}" transform="rotate(-18 160 146)"></ellipse>')
    s += f'<path d="M90 196 L100 212 L110 196" fill="{main}"></path>'
    s += (f'<g transform="rotate(10 100 44)"><path d="M64 64 Q62 24 100 20 Q138 24 136 64 L127 55 L118 65 L109 55 L100 65 L91 55 L82 65 L73 55 Z" fill="#FFFDF5"></path></g>')
    return s


def lion_back():
    main, mane, light = "#F7C873", "#E0873C", "#FFF1D6"
    s = back_body(main, light)
    s += f'<path d="{mane_path(100, 94, 72)}" fill="{mane}"></path>'
    s += f'<circle cx="56" cy="52" r="14" fill="{main}"></circle><circle cx="144" cy="52" r="14" fill="{main}"></circle>'
    s += f'<ellipse cx="100" cy="98" rx="54" ry="50" fill="#D27A34" stroke="none"></ellipse>'
    s += f'<path d="M84 70 Q92 90 84 112 M100 64 Q108 90 100 120 M116 70 Q124 90 116 112" fill="none" stroke="#B9652A" stroke-width="3.5"></path>'
    s += rope("M104 196 Q140 200 146 170", main) + f'<circle cx="146" cy="164" r="9" fill="{mane}"></circle>'
    return s


def monkey_back():
    main = "#A9744F"
    s = (f'<circle cx="32" cy="102" r="20" fill="{main}"></circle><circle cx="168" cy="102" r="20" fill="{main}"></circle>')
    s += back_body(main, "#F5D2AE")
    s += f'<ellipse cx="100" cy="98" rx="64" ry="58" fill="{main}"></ellipse>'
    s += f'<path d="M96 44 Q100 30 111 36" fill="none" stroke-width="3.5"></path>'
    s += rope("M100 200 C132 206 152 174 134 160 C124 152 112 164 122 170", main, 11)
    return s


def owl_back():
    main, dark = "#A88470", "#7E5E4E"
    s = f'<path d="M42 78 L40 34 L76 56 Z" fill="{main}"></path><path d="M158 78 L160 34 L124 56 Z" fill="{main}"></path>'
    s += f'<ellipse cx="100" cy="130" rx="70" ry="82" fill="{main}"></ellipse>'
    s += toes("#FFA94D")
    s += (f'<ellipse cx="68" cy="152" rx="28" ry="46" fill="{dark}" transform="rotate(8 68 152)"></ellipse>'
          f'<ellipse cx="132" cy="152" rx="28" ry="46" fill="{dark}" transform="rotate(-8 132 152)"></ellipse>')
    for (x, y) in ((88, 80), (100, 74), (112, 80), (94, 94), (106, 94)):
        s += f'<path d="M{x-5} {y} Q{x} {y+5} {x+5} {y}" fill="none" stroke="{dark}" stroke-width="3"></path>'
    s += f'<path d="M90 200 L100 214 L110 200 Z" fill="{dark}"></path>'
    return s


# --- dormidas ---
def lying_tail_cat():
    return f'<path d="M190 170 C230 176 236 140 222 126 C216 122 210 128 214 134 C222 150 214 166 188 158 Z" fill="#F7B77A"></path>'


SLEEP = {
    "Perro": ("lie", lambda: lying(DOG_MAIN, DOG_LIGHT,
              f'<path d="M186 150 C214 146 226 120 216 104 C212 118 202 130 182 134 Z" fill="{DOG_MAIN}"></path>', dog_head("blink"))),
    "Gato": ("lie", lambda: lying("#F7B77A", "#FFF3E4", lying_tail_cat(), head_cat("blink"))),
    "Pato": ("bird", lambda: duck_v("blink", feet=False)),
    "Rana": ("lie", lambda: lying("#A6D98A", "#A6D98A", "", head_frog("blink"),
              back_leg='<ellipse cx="182" cy="164" rx="30" ry="18" fill="#A6D98A"></ellipse>')),
    "Vaca": ("lie", lambda: lying("#FFFCF7", "#7A5646",
              '<path d="M190 150 Q222 150 224 176" fill="none" stroke-width="4"></path><circle cx="224" cy="180" r="6" fill="#7A5646"></circle>',
              head_cow("blink"),
              body_extra='<path d="M140 124 C152 118 172 124 170 138 C168 150 150 152 142 144 Z" fill="#7A5646" stroke="none"></path>')),
    "Chancho": ("lie", lambda: lying("#FFC6D1", "#F29BB0",
              '<path d="M190 140 c10 -4 15 5 9 10 c-6 4 -11 -3 -5 -8 c6 -4 14 -2 16 -9" fill="none" stroke-width="4"></path>', head_pig("blink"))),
    "Pollito": ("bird", lambda: chick_v("blink", feet=False)),
    "León": ("lie", lambda: lying("#F7C873", "#FFF1D6",
              rope("M190 156 Q226 160 224 128", "#F7C873") + '<circle cx="224" cy="122" r="9" fill="#E0873C"></circle>', head_lion("blink"))),
    "Mono": ("lie", lambda: lying("#A9744F", "#A9744F",
              rope("M190 160 C230 166 236 124 214 118 C204 116 200 128 210 132", "#A9744F", 11), head_monkey("blink"))),
    "Búho": ("bird", lambda: owl_v("blink", feet=False)),
}

VIEWS = [
    ("Perro", "#FFF1DF", dog, dog_side, dog_back),
    ("Gato", "#FFEBDD", cat, cat_side, cat_back),
    ("Pato", "#E6F1FA", duck, duck_side, duck_back),
    ("Rana", "#EAF6DF", frog, frog_side, frog_back),
    ("Vaca", "#F7EEF0", cow, cow_side, cow_back),
    ("Chancho", "#FFE9EE", pig, pig_side, pig_back),
    ("Pollito", "#FFF6D6", chick, chick_side, chick_back),
    ("León", "#FFEFD9", lion, lion_side, lion_back),
    ("Mono", "#F5EADF", monkey, monkey_side, monkey_back),
    ("Búho", "#F1E9E2", owl, owl_side, owl_back),
]

zzz_s = '<div style="position: absolute; right: 8px; top: 4px; display: flex; align-items: flex-end; gap: 2px; font-family: Fredoka, Nunito, sans-serif; font-weight: 700; color: #6F97C6;"><span style="font-size: 14px;">z</span><span style="font-size: 19px; padding-bottom: 8px;">z</span><span style="font-size: 25px; padding-bottom: 16px;">Z</span></div>'

rows = []
for name, tint, front, side, back in VIEWS:
    kind, sleep_fn = SLEEP[name]
    sleep_art = svg_lying(sleep_fn(), 200) if kind == "lie" else svg(sleep_fn(), 130)
    cells = [svg(front(), 130), svg(side(), 130), svg(back(), 130)]
    cell_html = "".join(f'<div style="height: 170px; display: flex; align-items: flex-end; justify-content: center;">{c}</div>' for c in cells)
    rows.append(f'''<div style="display: grid; grid-template-columns: 180px repeat(4, minmax(0, 1fr)); align-items: end; gap: 16px; padding: 16px 28px; background: {tint}; border-radius: 28px;">
<div style="align-self: center; font-family: Fredoka, Nunito, sans-serif; font-size: 28px; font-weight: 600;">{name}</div>
{cell_html}
<div style="position: relative; height: 170px; display: flex; align-items: flex-end; justify-content: center;">{sleep_art}{zzz_s}</div>
</div>''')

hdr = "".join(f'<div style="text-align: center; font-size: 14px; font-weight: 800; letter-spacing: 0.1em; text-transform: uppercase; color: #8E5A44;">{h}</div>'
              for h in ("Frente", "Perfil", "Espalda", "Dormida"))
vis_w, vis_h = 1440, 2460
vistas = f'''<div style="width: {vis_w}px; height: {vis_h}px; box-sizing: border-box; padding: 56px; display: flex; flex-direction: column; gap: 16px; background: #FFF8EF;">
<div style="display: flex; flex-direction: column; gap: 8px; padding-bottom: 16px;">
<h2 style="margin: 0; font-family: Fredoka, Nunito, sans-serif; font-size: 40px; font-weight: 600;">Todas las vistas</h2>
<p style="margin: 0; font-size: 17px; color: #75594B;">Frente, perfil y espalda para el giro; dormida para la hora de dormir. Las aves no se acuestan: se esponjan, esconden las patas y cierran los ojos.</p>
</div>
<div style="display: grid; grid-template-columns: 180px repeat(4, minmax(0, 1fr)); gap: 16px; padding: 0 28px;">
<div></div>{hdr}
</div>
{"".join(rows)}
</div>'''


files = {
    "Main.dc.html": page("Mascotas de cuerpo entero", main, main_w, main_h),
    "Estados.dc.html": page("Estados de la mascota", estados, est_w, est_h),
    "EnPantalla.dc.html": page("Mascota en pantalla", pantalla, ph_w, ph_h),
    "Poses.dc.html": page("Hora de dormir", poses, pos_w, pos_h),
    "Giro.dc.html": page("Giro de la mascota", giro, giro_w, giro_h),
    "Vistas.dc.html": page("Todas las vistas", vistas, vis_w, vis_h),
}
for n, c in files.items():
    with open(os.path.join(ROOT, "project", n), "w", encoding="utf-8") as f:
        f.write(c)

canvas = {
    "v": 3,
    "createdOnFiles": {"v": 1, "at": "2026-09-22T23:09:34Z"},
    "title": "Mascotas tiernas",
    "launch": {"view": "canvas"},
    "pages": [],
    "boards": {
        "Main.dc.html": {"x": 0, "y": 0, "w": main_w, "h": main_h, "title": "Las 10 mascotas"},
        "EnPantalla.dc.html": {"x": main_w + 80, "y": 0, "w": ph_w, "h": ph_h, "title": "En pantalla"},
        "Estados.dc.html": {"x": 0, "y": main_h + 120, "w": est_w, "h": est_h, "title": "Estados"},
        "Poses.dc.html": {"x": 0, "y": main_h + 120 + est_h + 120, "w": pos_w, "h": pos_h, "title": "Hora de dormir"},
        "Giro.dc.html": {"x": 0, "y": main_h + 120 + est_h + 120 + pos_h + 120, "w": giro_w, "h": giro_h, "title": "Giro"},
        "Vistas.dc.html": {"x": 1520, "y": 964, "w": vis_w, "h": vis_h, "title": "Todas las vistas"},
    },
    "order": ["Main.dc.html", "EnPantalla.dc.html", "Estados.dc.html", "Poses.dc.html", "Giro.dc.html", "Vistas.dc.html"],
    "notes": {},
    "designSystems": [],
}
with open(os.path.join(ROOT, "project", "canvas.json"), "w", encoding="utf-8") as f:
    json.dump(canvas, f, ensure_ascii=False, indent=1)
print("ok", ROOT)
