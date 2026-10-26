"""
Vista previa animada de los modelos 2,5D (docs/prototipos/mascotas-3d.html): las 10 mascotas girando,
girando la cabeza y moviendo las patas, para aprobarlas antes de pasarlas a la app.
Uso: python tools/pet_art/model_preview.py
"""
import json
import math
import os

import model3d as M

OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "docs", "prototipos", "mascotas-3d.html")
BIRDS = {"DUCK", "CHICK", "OWL"}


def ease(t):
    return 2 * t * t if t < 0.5 else 1 - (-2 * t + 2) ** 2 / 2


def ramp(t, a, b):
    """0 antes de a, 1 después de b, suave en el medio."""
    if t <= a:
        return 0.0
    if t >= b:
        return 1.0
    return ease((t - a) / (b - a))


def anims(enum, fn):
    stand, sit = M.build(fn), M.build(fn, sit=True)
    out = {}
    # vuelta de 360° (parada), con saltito suave
    fr = []
    for i in range(24):
        th = ease(i / 23) * 360
        hop = -abs(math.sin(math.radians(th * 2))) * 5
        fr.append(f'<g transform="translate(0 {hop:.1f})">{M.render(stand, th)}</g>')
    out["spin"] = fr
    # girar solo la cabeza (sentada)
    seq = [0, 20, 40, 55, 55, 55, 55, 40, 10, -20, -45, -50, -50, -50, -30, -10, 0, 0]
    out["head"] = [M.render(sit, 0, head=a) for a in seq]
    # saludar con la patita
    fr = []
    for i in range(14):
        t = i / 13
        up = ramp(t, 0, 0.2) * (1 - ramp(t, 0.85, 1))
        wave = math.sin(t * 2 * math.pi * 2.5) * 15 * up
        fr.append(M.render(sit, 0, face="happy", pose={"armR": {"rot": -125 * up + wave}}))
    out["wave"] = fr
    # patas: amasar (o aletear, las aves)
    fr = []
    for i in range(12):
        ph = math.sin(i / 12 * 2 * math.pi)
        if enum in BIRDS:
            pose = {"armL": {"rot": 35 * max(0, ph)}, "armR": {"rot": -35 * max(0, ph)}}
        else:
            pose = {"footL": {"dy": -7 * max(0, ph)}, "footR": {"dy": -7 * max(0, -ph)}}
        fr.append(M.render(sit, 0, pose=pose))
    out["paws"] = fr
    # ¡hurra! (los dos bracitos arriba)
    fr = []
    for i in range(12):
        t = i / 11
        up = ramp(t, 0, 0.3) * (1 - ramp(t, 0.75, 1))
        fr.append(f'<g transform="translate(0 {-6 * up:.1f})">' +
                  M.render(sit, 0, face="happy", pose={"armL": {"rot": 140 * up}, "armR": {"rot": -140 * up}}) + "</g>")
    out["hurray"] = fr
    # orejas (las mascotas que las mueven): cada una a su manera
    fr = []
    for i in range(20):
        t = i / 19
        if enum == "RABBIT":  # bajar, subir y mover una sola, como si escuchara algo
            down = ramp(t, 0.0, 0.2) * (1 - ramp(t, 0.45, 0.6))
            twitch = math.sin(max(0.0, t - 0.65) / 0.35 * math.pi * 3) * 25 if t > 0.65 else 0
            pose = {"earL": {"rot": -110 * down}, "earR": {"rot": 110 * down + twitch}}
        elif enum == "DOG":  # se balancean y después se levantan un poquito (presta atención)
            swing = math.sin(t * math.pi * 4) * 12 * (1 - ramp(t, 0.5, 0.6))
            perk = ramp(t, 0.6, 0.7) * (1 - ramp(t, 0.9, 1.0)) * 28
            pose = {"earL": {"rot": swing - perk}, "earR": {"rot": swing + perk}}
        elif enum == "CAT":  # tic rápido de una oreja y después de la otra
            a = 14 * math.sin(min(1.0, max(0.0, (t - 0.1) / 0.2)) * math.pi * 2)
            b = 14 * math.sin(min(1.0, max(0.0, (t - 0.55) / 0.2)) * math.pi * 2)
            pose = {"earL": {"rot": -a}, "earR": {"rot": b}}
        else:
            pose = {}
        fr.append(M.render(sit, 0, pose=pose))
    out["ears"] = fr
    # en reposo: sentada, abre y cierra los ojos
    out["rest"] = [M.render(sit, 0)] * 6 + [M.render(sit, 0, face="blink")]
    return out


def main():
    pets = M.all_pets()
    data = {}
    names = {}
    for enum, fn in pets.items():
        data[enum] = anims(enum, fn)
        names[enum] = fn()["name"]
    buttons = "".join(f'<button class="pet" data-p="{k}">{names[k]}</button>' for k in pets)
    acts = [("rest", "En reposo"), ("spin", "Vuelta"), ("head", "Girar la cabeza"), ("wave", "Saludar"),
            ("paws", "Patitas"), ("hurray", "¡Hurra!"), ("ears", "Orejas (conejo, perro, gato)")]
    act_buttons = "".join(f'<button class="act" data-a="{k}">{t}</button>' for k, t in acts)
    html = f"""<!doctype html><html lang="es"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1"><title>Mascotas en 3D</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Nunito:wght@600;800&display=swap">
<style>
:root {{ --bg:#FBF6EE; --card:#FFFDF9; --line:#E8DCCD; --ink:#3A2E27; --muted:#6E5E52; --cocoa:#8A5536; --soft:#FBE7E3; }}
body {{ margin:0; background:var(--bg); color:var(--ink); font-family:Nunito,sans-serif; }}
main {{ max-width:760px; margin:0 auto; padding:16px; }}
h1 {{ font-size:22px; margin:0 0 4px; }} p {{ color:var(--muted); font-weight:600; font-size:14px; margin:0 0 12px; }}
.row {{ display:flex; flex-wrap:wrap; gap:6px; margin:8px 0; }}
button {{ font:800 13px Nunito,sans-serif; color:var(--cocoa); background:var(--card); border:1px solid var(--line); border-radius:999px; padding:8px 12px; }}
button.on {{ background:var(--soft); border-color:var(--soft); }}
.stage {{ background:var(--card); border:1px solid var(--line); border-radius:24px; height:300px; display:flex; align-items:flex-end; justify-content:center; }}
.stage svg {{ width:220px; height:264px; overflow:visible; }}
.grid {{ display:grid; grid-template-columns:repeat(5,1fr); gap:6px; margin-top:12px; }}
.mini {{ background:var(--card); border:1px solid var(--line); border-radius:16px; display:flex; align-items:flex-end; justify-content:center; height:120px; }}
.mini svg {{ width:90px; height:108px; overflow:visible; }}
</style></head><body><main>
<h1>Mascotas en 3D: giro, cabeza y patas</h1>
<p>Elegí una mascota y una acción. Abajo, las 10 a la vez haciendo lo mismo.</p>
<div class="row">{buttons}</div>
<div class="row">{act_buttons}</div>
<div class="stage"><svg id="big" viewBox="0 0 200 240"></svg></div>
<div class="grid" id="grid"></div>
</main>
<script>
const DATA = {json.dumps(data, ensure_ascii=False)};
const WRAP = s => '<ellipse cx="100" cy="228" rx="54" ry="7" fill="#4A3228" opacity=".12" stroke="none"></ellipse>' +
  '<g stroke="#4A3228" stroke-width="4" stroke-linejoin="round" stroke-linecap="round">' + s + '</g>';
let pet = 'DOG', act = 'spin', i = 0;
const big = document.getElementById('big'), grid = document.getElementById('grid');
for (const k of Object.keys(DATA)) grid.insertAdjacentHTML('beforeend', `<div class="mini"><svg viewBox="0 0 200 240" data-p="${{k}}"></svg></div>`);
const minis = [...grid.querySelectorAll('svg')];
function mark() {{
  document.querySelectorAll('.pet').forEach(b => b.classList.toggle('on', b.dataset.p === pet));
  document.querySelectorAll('.act').forEach(b => b.classList.toggle('on', b.dataset.a === act));
}}
document.querySelectorAll('.pet').forEach(b => b.onclick = () => {{ pet = b.dataset.p; mark(); }});
document.querySelectorAll('.act').forEach(b => b.onclick = () => {{ act = b.dataset.a; i = 0; mark(); }});
mark();
setInterval(() => {{
  const fr = DATA[pet][act];
  big.innerHTML = WRAP(fr[i % fr.length]);
  minis.forEach(m => {{ const f = DATA[m.dataset.p][act]; m.innerHTML = WRAP(f[i % f.length]); }});
  i++;
}}, 80);
</script></body></html>"""
    with open(OUT, "w", encoding="utf-8") as f:
        f.write(html)
    print("ok", round(len(html) / 1024), "KB")


if __name__ == "__main__":
    main()
