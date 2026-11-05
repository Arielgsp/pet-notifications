"""
Pasa el ícono de Cowork (SVG de 108 x 108 dp) a VectorDrawable para el ícono adaptable de Android.

  python tools/icon/export_icon.py <carpeta adaptive/ del zip de Cowork>

Escribe drawable/ic_launcher_foreground.xml, ic_launcher_background.xml e ic_launcher_monochrome.xml.
Entiende lo que usan esos SVG: g, path, ellipse, rect, translate/scale/rotate, clipPath y un radialGradient.
"""
import os, re, sys
import xml.etree.ElementTree as ET

SVG = "{http://www.w3.org/2000/svg}"
OUT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "app", "src", "main", "res", "drawable")


def num(v):
    v = float(v)
    return str(int(v)) if v == int(v) else f"{v:.3f}".rstrip("0").rstrip(".")


def ellipse_d(cx, cy, rx, ry):
    cx, cy, rx, ry = map(float, (cx, cy, rx, ry))
    return (f"M{num(cx - rx)},{num(cy)} a{num(rx)},{num(ry)} 0 1,0 {num(2 * rx)},0 "
            f"a{num(rx)},{num(ry)} 0 1,0 {num(-2 * rx)},0 Z")


def shape_d(el):
    a, tag = el.attrib, el.tag.replace(SVG, "")
    if tag == "ellipse":
        return ellipse_d(a["cx"], a["cy"], a["rx"], a["ry"])
    if tag == "circle":
        return ellipse_d(a["cx"], a["cy"], a["r"], a["r"])
    if tag == "rect":
        x, y = float(a.get("x", 0)), float(a.get("y", 0))
        w, h = float(a["width"]), float(a["height"])
        return f"M{num(x)},{num(y)} h{num(w)} v{num(h)} h{num(-w)} Z"
    if tag == "path":
        return a["d"]
    raise ValueError(tag)


def groups_for(transform):
    """Una <group> por operación, en el mismo orden que el SVG (la de más a la izquierda, afuera)."""
    out = []
    for op, args in re.findall(r"(rotate|translate|scale)\(([^)]*)\)", transform or ""):
        a = [float(x) for x in re.split(r"[ ,]+", args.strip())]
        if op == "rotate":
            px, py = (a[1], a[2]) if len(a) == 3 else (0, 0)
            out.append(f'<group android:rotation="{num(a[0])}" android:pivotX="{num(px)}" android:pivotY="{num(py)}">')
        elif op == "translate":
            out.append(f'<group android:translateX="{num(a[0])}" android:translateY="{num(a[1] if len(a) > 1 else 0)}">')
        else:
            out.append(f'<group android:scaleX="{num(a[0])}" android:scaleY="{num(a[1] if len(a) > 1 else a[0])}">')
    return out


def walk(el, style, clips, grads, out):
    tag = el.tag.replace(SVG, "")
    if tag in ("clipPath", "defs", "radialGradient", "linearGradient"):
        return
    st = dict(style)
    for k in ("fill", "stroke", "stroke-width", "opacity"):
        if k in el.attrib:
            st[k] = el.attrib[k]
    opens = groups_for(el.attrib.get("transform"))
    clip = re.match(r"url\(#(.+)\)", el.attrib.get("clip-path", ""))
    if clip:
        opens.append("<group>")
    out.extend(opens)
    if clip:
        for c in clips[clip.group(1)]:
            out.append(f'<clip-path android:pathData="{shape_d(c)}" />')
    if tag in ("g", "svg"):
        for ch in el:
            walk(ch, st, clips, grads, out)
    else:
        alpha = float(st.get("opacity", 1))
        fill = st.get("fill", "#000000")
        stroke = st.get("stroke", "none")
        sw = float(st.get("stroke-width", 1))
        has_stroke = stroke != "none" and sw > 0
        attrs = [f'android:pathData="{shape_d(el)}"']
        inner = ""
        g = re.match(r"url\(#(.+)\)", fill)
        if g:
            inner = grads[g.group(1)]
        elif fill != "none":
            attrs.append(f'android:fillColor="{fill.upper()}"')
            if alpha < 1:
                attrs.append(f'android:fillAlpha="{num(alpha)}"')
        if has_stroke:
            attrs += [f'android:strokeColor="{stroke.upper()}"', f'android:strokeWidth="{num(sw)}"',
                      'android:strokeLineCap="round"', 'android:strokeLineJoin="round"']
            if alpha < 1:
                attrs.append(f'android:strokeAlpha="{num(alpha)}"')
        if fill != "none" or has_stroke:
            out.append("<path " + " ".join(attrs) + (f">{inner}</path>" if inner else " />"))
    out.extend("</group>" for _ in opens)


def radial(el, w, h):
    cx = float(el.get("cx", "50%").rstrip("%")) / 100 * w
    cy = float(el.get("cy", "50%").rstrip("%")) / 100 * h
    r = float(el.get("r", "50%").rstrip("%")) / 100 * max(w, h)
    items = "".join(
        f'<item android:offset="{num(s.get("offset"))}" android:color="#FF{s.get("stop-color").lstrip("#").upper()}" />'
        for s in el.iter(SVG + "stop"))
    return (f'<aapt:attr name="android:fillColor"><gradient android:type="radial" android:centerX="{num(cx)}" '
            f'android:centerY="{num(cy)}" android:gradientRadius="{num(r)}">{items}</gradient></aapt:attr>')


def convert(src, dst):
    root = ET.parse(src).getroot()
    w, h = (float(x) for x in root.get("viewBox").split()[2:])
    clips = {c.get("id"): list(c) for c in root.iter(SVG + "clipPath")}
    grads = {g.get("id"): radial(g, w, h) for g in root.iter(SVG + "radialGradient")}
    body = []
    walk(root, {}, clips, grads, body)
    aapt = ' xmlns:aapt="http://schemas.android.com/aapt"' if grads else ""
    text = "\n".join([
        '<?xml version="1.0" encoding="utf-8"?>',
        f"<!-- Generado desde {os.path.basename(src)} (ícono de Cowork) con tools/icon/export_icon.py; no editar a mano. -->",
        f'<vector xmlns:android="http://schemas.android.com/apk/res/android"{aapt} android:width="{num(w)}dp" '
        f'android:height="{num(h)}dp" android:viewportWidth="{num(w)}" android:viewportHeight="{num(h)}">',
        *body, "</vector>", ""])
    open(dst, "w", encoding="utf-8", newline="\n").write(text)
    print("escrito", os.path.relpath(dst))


if __name__ == "__main__":
    src = sys.argv[1]
    for name in ("foreground", "background", "monochrome"):
        convert(os.path.join(src, f"ic_launcher_{name}.svg"), os.path.join(OUT, f"ic_launcher_{name}.xml"))
