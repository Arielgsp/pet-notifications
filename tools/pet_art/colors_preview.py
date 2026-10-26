"""Página con los pelajes de cada mascota (docs/prototipos/pelajes.html). Uso: python tools/pet_art/colors_preview.py"""
import os

import model3d as M
from model3d_pets import PALETTES

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "docs", "prototipos", "pelajes.html")
rows = ""
for enum, fn in M.all_pets().items():
    name = fn()["name"]
    cells = ""
    for label, pal in PALETTES[enum]:
        sit, stand = M.build(fn, True, pal), M.build(fn, False, pal)
        cells += (f'<div class="c"><div class="p">{M.svg(sit, 0, 84)}{M.svg(stand, 35, 84)}</div>'
                  f'<span>{label}</span></div>')
    rows += f'<h2>{name}</h2><div class="r">{cells}</div>'
html = f"""<!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Pelajes</title><link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Nunito:wght@600;800&display=swap">
<style>
body {{ margin:0; background:#FBF6EE; color:#3A2E27; font-family:Nunito,sans-serif; }}
main {{ max-width:760px; margin:0 auto; padding:16px; }}
h1 {{ font-size:22px; margin:0 0 4px; }} p {{ color:#6E5E52; font-weight:600; font-size:14px; margin:0 0 8px; }}
h2 {{ font-size:16px; margin:16px 0 6px; }}
.r {{ display:flex; gap:6px; overflow-x:auto; padding-bottom:4px; }}
.c {{ flex:0 0 auto; background:#FFFDF9; border:1px solid #E8DCCD; border-radius:18px; padding:6px 6px 8px; text-align:center; }}
.c .p {{ display:flex; align-items:flex-end; }} .c span {{ font-weight:800; font-size:13px; }}
</style></head><body><main>
<h1>Pelajes (Pro)</h1>
<p>Cada mascota con sus colores: sentada de frente y parada de tres cuartos. El primero es el de siempre (gratis). Deslizá de costado para ver todos.</p>
{rows}</main></body></html>"""
open(OUT, "w", encoding="utf-8").write(html)
print("ok", round(len(html) / 1024), "KB")
