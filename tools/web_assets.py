"""
Pasa las mascotas a web/pets (SVG) desde el modelo 2,5D, el mismo que dibuja la app: de frente, contenta y dormida,
los cuadros de la vuelta de dos toques y algunas vestidas con el arte de docs/accesorios-especies.

Uso: python tools/web_assets.py
"""
import importlib.util
import os
import re
import sys

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
ART = os.path.join(ROOT, "tools", "pet_art")
OUT = os.path.join(ROOT, "web", "pets")
sys.path.insert(0, ART)
import gen as G  # noqa: E402
import model3d as M  # noqa: E402

spec = importlib.util.spec_from_file_location("gen_outfit", os.path.join(ART, "gen_outfit.py"))
outfit = importlib.util.module_from_spec(spec)
spec.loader.exec_module(outfit)

NAMES = {"DOG": "dog", "CAT": "cat", "RABBIT": "rabbit", "DUCK": "duck", "FROG": "frog", "COW": "cow", "PIG": "pig",
         "CHICK": "chick", "LION": "lion", "MONKEY": "monkey", "OWL": "owl"}
BIRDS = ("DUCK", "CHICK", "OWL")  # las aves no se acuestan de costado (PetModelData.lying)
LIE_TH = 270  # PetModel.LIE_TH
SPIN_STEP = 30
# (mascota, carpeta de Cowork, accesorios). Sin pato, vaca ni pollito: al vestirse cambian piezas de su dibujo.
DRESSED = (
    ("DOG", "perro", ("gorro_navideno", "panuelo")),
    ("PIG", "chancho", ("casco_calabaza",)),
    ("RABBIT", "conejo", ("corona_flores",)),
    ("CAT", "gato", ("gorrito_lana", "panuelo")),
)  # Solo mascotas del lanzamiento: león, mono, rana y búho llegan después, en el pack Amigos salvajes.
# Lienzo de 200×240 de los dibujos, con aire a los costados para las orejas y arriba para las del conejo.
VIEW = 'viewBox="-20 -24 240 268"'


def svg(content):
    return f'<svg xmlns="http://www.w3.org/2000/svg" {VIEW}>{G.shadow()}<g {G.W}>{content}</g></svg>\n'


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)


def accessory(folder, name):
    text = outfit.read_svg(folder, "frente", name)
    if name == "panuelo":
        for old, new in outfit.SCARF_COLORS.items():
            text = text.replace(old, new)
    return re.sub(r"^.*?<svg[^>]*>", "", text, flags=re.S).rsplit("</svg>", 1)[0]


def main():
    pets = M.all_pets()
    for enum, name in NAMES.items():
        stand = M.build(pets[enum])
        write(os.path.join(OUT, f"{name}_front.svg"), svg(M.render(stand, 0)))
        write(os.path.join(OUT, f"{name}_happy.svg"), svg(M.render(stand, 0, "happy")))
        if enum in BIRDS:
            sleep = M.render(M.build(pets[enum], sit=True), 0, "closed")
        else:
            sleep = M.render(M.build(pets[enum], lie=True), LIE_TH, "closed", head=0)
        write(os.path.join(OUT, f"{name}_sleep.svg"), svg(sleep))
        for th in range(0, 360, SPIN_STEP):
            write(os.path.join(OUT, "spin", f"{name}_{th:03d}.svg"), svg(M.render(stand, th)))
    for enum, folder, items in DRESSED:
        art = "".join(accessory(folder, item) for item in items)
        write(os.path.join(OUT, f"{NAMES[enum]}_dressed.svg"), svg(M.render(M.build(pets[enum]), 0) + art))
    print("listo:", os.path.normpath(OUT))


if __name__ == "__main__":
    main()
