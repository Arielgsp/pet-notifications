"""Modelos 2,5D de las mascotas (salvo el perro, en model3d.py). Ver model3d.py para las piezas."""
import math

import model3d as M

from model3d import (BLUSH, BODY, HEAD, INK, body, eye, eyes2, blush2, legs, on_head, part, path, rope_tail, round_pts, spot_parts, tail, vol)


def flat_pts(pts, cy=98, rx=64, ry=60, cz=0, r=64, lift=0.0):
    """Puntos del lienzo (X, Y) apoyados sobre un elipsoide: devuelve (x, y, z) para path()."""
    return [(X - 100, Y, on_head(X - 100, Y, cy, rx, ry, cz, r) + lift) for X, Y in pts]


def fixed_pts(pts, z):
    """Puntos del lienzo (X, Y) a una misma profundidad z (orejas, cuernos, copete)."""
    return [(X - 100, Y, z) for X, Y in pts]

# Pelajes (Pro): el primero de cada lista es el de siempre. Cada paleta define los colores con nombre
# que usa el modelo de la mascota.
PALETTES = {
    "DOG": [("Dorado", dict(main="#F2C38B", light="#FFF4E2", dark="#C98A55")),
            ("Chocolate", dict(main="#B07A55", light="#F6E6D4", dark="#7A4B32")),
            ("Gris", dict(main="#BDBAC2", light="#F5F4F7", dark="#85818C")),
            ("Negro", dict(main="#5E5352", light="#F1E7E0", dark="#3B3333")),
            ("Blanco", dict(main="#FFFAF3", light="#FFFFFF", dark="#E3CDB6")),
            ("Dálmata", dict(main="#FFFFFF", light="#FFFFFF", dark="#3B3536", spots=[('head', 74, 62, 9, 8, '#3B3536', 0), ('head', 128, 66, 6, 5.5, '#3B3536', 0), ('head', 106, 48, 4, 4, '#3B3536', 0), ('head', 60, 112, 4.5, 4, '#3B3536', 0), ('head', 88, 66, 9, 8, '#3B3536', 1), ('head', 116, 84, 7, 6, '#3B3536', 1), ('head', 100, 120, 5, 4, '#3B3536', 1), ('body', 74, 168, 6, 5, '#3B3536', 0), ('body', 126, 176, 7, 6, '#3B3536', 0), ('body', 112, 158, 4, 4, '#3B3536', 0), ('body', 82, 200, 5, 4, '#3B3536', 0), ('body', 95, 180, 5, 4.5, '#3B3536', 0), ('body', 108, 191, 4, 3.5, '#3B3536', 0), ('body', 93, 200, 3.5, 3, '#3B3536', 0), ('body', 104, 168, 3, 3, '#3B3536', 0), ('body', 88, 166, 7, 6, '#3B3536', 1), ('body', 118, 184, 6, 5, '#3B3536', 1), ('body', 100, 202, 5, 4, '#3B3536', 1), ('body', 76, 190, 4, 4, '#3B3536', 1)]))],
    "CAT": [("Naranja", dict(main="#F7B77A", light="#FFF3E4", soft="#F0A066")),
            ("Gris", dict(main="#B9BDC8", light="#F4F5F8", soft="#9CA1AE")),
            ("Negro", dict(main="#5A5352", light="#EFE6E0", soft="#726A68")),
            ("Blanco", dict(main="#FFFDF9", light="#FFFFFF", soft="#EADFD3")),
            ("Siamés", dict(main="#F2E2CB", light="#FFF8EE", soft="#CDB194")),
            ("Tricolor", dict(main="#FFFDF9", light="#FFFFFF", soft="#E8D6C4", spots=[('head', 66, 66, 24, 20, '#F2A65E', 0), ('head', 138, 64, 16, 14, '#4A403E', 0), ('head', 100, 46, 8, 6, '#F2A65E', 0), ('head', 76, 70, 22, 18, '#F2A65E', 1), ('head', 126, 86, 18, 16, '#4A403E', 1), ('body', 74, 176, 16, 14, '#F2A65E', 0), ('body', 128, 190, 12, 10, '#4A403E', 0), ('body', 86, 172, 18, 16, '#4A403E', 1), ('body', 118, 192, 16, 13, '#F2A65E', 1)]))],
    "DUCK": [("Amarillo", dict(main="#FFEFA8", wing="#FFE07A", tuft="#FFD95E")),
             ("Blanco", dict(main="#FFFFFF", wing="#F0F0F0", tuft="#F7F7F7")),
             ("Marrón", dict(main="#D8B48E", wing="#C49A70", tuft="#C9A27E")),
             ("Rosa", dict(main="#FFD3DC", wing="#F9BCC9", tuft="#FFC2CF"))],
    "FROG": [("Verde", dict(main="#A6D98A", light="#E6F5D0")),
             ("Celeste", dict(main="#9FD4EA", light="#E3F4FA")),
             ("Amarilla", dict(main="#F5DC7A", light="#FBF3D2")),
             ("Rosa", dict(main="#F5B7C8", light="#FCE6EC")),
             ("Con lunares", dict(main="#A6D98A", light="#E6F5D0", spots=[('head', 50, 104, 7.5, 6.5, '#6AA653', 0), ('head', 150, 98, 6.5, 6, '#6AA653', 0), ('head', 118, 90, 5.5, 5, '#6AA653', 0), ('head', 84, 92, 9.5, 8.5, '#6AA653', 1), ('head', 118, 112, 7.5, 6.5, '#6AA653', 1), ('head', 100, 80, 6.5, 5.5, '#6AA653', 1), ('body', 76, 176, 6.5, 6, '#6AA653', 0), ('body', 128, 180, 6, 5.5, '#6AA653', 0), ('body', 88, 172, 8.5, 7.5, '#6AA653', 1), ('body', 116, 188, 7.5, 6.5, '#6AA653', 1)]))],
    "COW": [("Manchas marrones", dict(main="#FFFCF7", spot="#7A5646")),
            ("Manchas negras", dict(main="#FFFCF7", spot="#3D3536")),
            ("Marrón", dict(main="#D9A57A", spot="#9A6644")),
            ("Gris", dict(main="#D6D3D0", spot="#8E8784"))],
    "PIG": [("Rosa", dict(main="#FFC6D1", dark="#F29BB0", snout="#FFB0C0", belly="#FFDDE4")),
            ("Marrón", dict(main="#C9A086", dark="#9E7461", snout="#D9B39C", belly="#E7CDBE")),
            ("Gris", dict(main="#C7C1C3", dark="#9C9497", snout="#D8D1D3", belly="#E6E1E3")),
            ("Blanco", dict(main="#FFF6F4", dark="#F0CFCF", snout="#FFE0E3", belly="#FFFFFF")),
            ("Manchado", dict(main="#FFC6D1", dark="#F29BB0", snout="#FFB0C0", belly="#FFDDE4", spots=[('head', 128, 62, 12, 10, '#5E4A4A', 0), ('head', 70, 86, 7, 6, '#5E4A4A', 0), ('head', 88, 72, 14, 12, '#5E4A4A', 1), ('head', 120, 100, 10, 8, '#5E4A4A', 1), ('body', 76, 172, 10, 9, '#5E4A4A', 0), ('body', 126, 196, 7, 6, '#5E4A4A', 0), ('body', 90, 170, 13, 11, '#5E4A4A', 1), ('body', 116, 196, 9, 8, '#5E4A4A', 1)]))],
    "CHICK": [("Amarillo", dict(main="#FFE27A", dark="#F7C948")),
              ("Blanco", dict(main="#FFFFFF", dark="#E9E9E9")),
              ("Naranja", dict(main="#FFC68A", dark="#F5A65B")),
              ("Gris", dict(main="#E2E0DC", dark="#C4C0BA"))],
    "LION": [("Dorado", dict(main="#F7C873", mane="#E0873C", light="#FFF1D6", mane_dark="#D27A34", mane_line="#B9652A")),
             ("Blanco", dict(main="#F6EDDF", mane="#E5D6BF", light="#FFFFFF", mane_dark="#D9C8AE", mane_line="#C4B191")),
             ("Pardo", dict(main="#E2B377", mane="#A9632F", light="#FFF1D6", mane_dark="#965428", mane_line="#7E4520")),
             ("Melena negra", dict(main="#F7C873", mane="#5E4636", light="#FFF1D6", mane_dark="#523C2E", mane_line="#43312A"))],
    "MONKEY": [("Marrón", dict(main="#A9744F", face="#F5D2AE")),
               ("Gris", dict(main="#8E8A8C", face="#EADCD3")),
               ("Dorado", dict(main="#D69A50", face="#F8E0BC")),
               ("Chocolate", dict(main="#6E4A36", face="#EBC9A9"))],
    "OWL": [("Marrón", dict(main="#A88470", dark="#7E5E4E", light="#F3E3CF", disc="#EBD5BD")),
            ("Nieve", dict(main="#F4F1EC", dark="#CFC6BA", light="#FFFFFF", disc="#FFFFFF")),
            ("Gris", dict(main="#9D9A9C", dark="#6F6B6E", light="#E9E6E6", disc="#DCD8D8")),
            ("Rojizo", dict(main="#C27A4E", dark="#8F5534", light="#F6E1CC", disc="#EDCBB0"))],
    "RABBIT": [("Crema", dict(main="#FFF1E6", light="#FFFFFF", inner="#FFB9C8")),
               ("Blanco", dict(main="#FFFFFF", light="#FFFFFF", inner="#FFC9D4")),
               ("Gris", dict(main="#C9C6CC", light="#F4F3F6", inner="#F3BFCB")),
               ("Marrón", dict(main="#C9A07E", light="#F6E9DC", inner="#F2B9B9")),
               ("Negro", dict(main="#5C5352", light="#EFE7E2", inner="#E7A9B4")),
               ("Orejas caídas", dict(main="#F4DCC4", light="#FFF8F0", inner="#F7B3BE", lop=True)),
               ("Manchado", dict(main="#FFFFFF", light="#FFFFFF", inner="#FFC9D4", spots=[('head', 68, 74, 20, 17, '#8C6A55', 0), ('head', 130, 60, 9, 8, '#8C6A55', 0), ('head', 84, 70, 20, 17, '#8C6A55', 1), ('head', 120, 90, 12, 10, '#8C6A55', 1), ('body', 128, 176, 12, 10, '#8C6A55', 0), ('body', 84, 172, 15, 13, '#8C6A55', 1), ('body', 118, 196, 10, 8, '#8C6A55', 1)]))],
}


def colors(enum, c):
    """La paleta pedida (o la de siempre)."""
    return c or PALETTES[enum][0][1]


def cat(c=None):
    c = colors("CAT", c)
    main, light, soft = c["main"], c["light"], c["soft"]
    hz = (0, 6)
    p = [tail((0, -34), (62, -50), main, y=(190, 130), bend=10)]
    p += legs(main, light, belly=light)
    p.append(body(main))
    for sx in (-1, 1):  # orejas redondeadas, siempre detrás de la cabeza
        ear = [(100 + sx * 58, 76), (100 + sx * 58, 30), (100 + sx * 48, 24), (100 + sx * 34, 30), (100 + sx * 10, 48)]
        inner = [(100 + sx * 46, 58), (100 + sx * 46, 40), (100 + sx * 42, 36), (100 + sx * 34, 40), (100 + sx * 24, 49)]
        tag, piv = ("earL" if sx < 0 else "earR"), (sx * 34, 62, 30)
        p.append(path(HEAD, round_pts(ear), "M{0} Q{1} {2} Q{3} {4} Z", fill=main, behind=2, tag=tag, pivot=piv))
        p.append(path(HEAD, round_pts(inner, zoff=-29), "M{0} Q{1} {2} Q{3} {4} Z", fill="#FFB3BF", width=0, behind=2, tag=tag, pivot=piv))
    p.append(vol(HEAD, 0, 98, hz, (68, 62), (60, 58), main))
    p.append(path(HEAD, flat_pts([(92, 42), (94, 48), (93, 53)], cz=6), "M{0} Q{1} {2}", color=soft, hide=0.05))
    p.append(path(HEAD, flat_pts([(100, 39), (100, 50)], cz=6), "M{0} L{1}", color=soft, hide=0.05))
    p.append(path(HEAD, flat_pts([(108, 42), (106, 48), (107, 53)], cz=6), "M{0} Q{1} {2}", color=soft, hide=0.05))
    p.append(path(HEAD, [(-14, 48, -50), (-12, 57, -54), (-13, 64, -54)], "M{0} Q{1} {2}", color=soft, hide=0.05))
    p.append(path(HEAD, [(0, 42, -50), (0, 62, -56)], "M{0} L{1}", color=soft, hide=0.05))
    p.append(path(HEAD, [(14, 48, -50), (12, 57, -54), (13, 64, -54)], "M{0} Q{1} {2}", color=soft, hide=0.05))
    p.append(part(HEAD, 0, 123, on_head(0, 123, cz=6) + 4, 23, 15, light, flat=True, p=0.5, hide=0.0, stroke=False))
    p += eyes2(28, 100, 6, (11, 13))
    p += blush2(48, 122, 6)
    p += [part(HEAD, sx * 48, 122, on_head(sx * 48, 122, cz=6), 13, 7.5, BLUSH, flat=True, hide=0.05, stroke=False, opacity=0.5)
          for sx in (-1, 1)]
    nz = on_head(0, 115, cz=6) + 6
    p.append(path(HEAD, fixed_pts([(95, 113), (105, 113), (100, 119)], nz), "M{0} L{1} L{2} Z", fill="#FF8FA3", width=2.5, hide=0.1))
    p.append(path(HEAD, fixed_pts([(100, 119), (100, 122)], nz), "M{0} L{1}", width=3, hide=0.15))
    p.append(path(HEAD, fixed_pts([(89, 122), (100, 138), (111, 122), (100, 127)], nz - 2), "M{0} Q{1} {2} Q{3} {0} Z",
                  fill="#B8475A", width=3, hide=0.15))
    p.append(path(HEAD, fixed_pts([(95, 129), (100, 126), (105, 129), (100, 133)], nz - 1), "M{0} Q{1} {2} Q{3} {0} Z",
                  fill="#FF8FA3", width=0, hide=0.15))
    for sx in (-1, 1):
        for y0, y1, y2 in ((110, 112, 116), (124, 122, 121)):
            w = flat_pts([(100 + sx * 60, y0), (100 + sx * 52, y1), (100 + sx * 44, y2)], cz=6, lift=4)
            p.append(path(HEAD, w, "M{0} Q{1} {2}", width=3, hide=-0.1))
    p += spot_parts(c)
    return dict(name="Gato", parts=p)


def pig(c=None):
    c = colors("PIG", c)
    main, dark, snout, belly_c = c["main"], c["dark"], c["snout"], c["belly"]
    hz = (0, 4)
    p = [path(BODY, [(40, 182, -30), (50, 176, -36), (55, 186, -38), (48, 190, -36), (42, 184, -34), (52, 178, -38), (60, 174, -40)],
              "M{0} Q{1} {2} Q{3} {4} Q{5} {6}", behind=0)]
    p += legs(main, dark, belly=belly_c)
    p.append(body(main))
    for sx in (-1, 1):
        ear = [(100 + sx * 56, 70), (100 + sx * 64, 30), (100 + sx * 42, 22), (100 + sx * 24, 34), (100 + sx * 14, 50)]
        inner = [(100 + sx * 46, 54), (100 + sx * 48, 38), (100 + sx * 40, 32), (100 + sx * 30, 40), (100 + sx * 24, 50)]
        p.append(path(HEAD, round_pts(ear), "M{0} Q{1} {2} Q{3} {4} Z", fill=main, behind=2))
        p.append(path(HEAD, round_pts(inner, zoff=-29), "M{0} Q{1} {2} Q{3} {4} Z", fill=dark, width=0, behind=2))
    p.append(vol(HEAD, 0, 98, hz, (68, 62), (60, 58), main))
    p += eyes2(28, 96, 4)
    p += blush2(48, 120, 4)
    p.append(part(HEAD, 0, 122, 62, 22, 15, snout, behind=-0.1))
    p += [part(HEAD, sx * 8, 122, 76, 3.5, 5, "#C9677E", behind=-0.1, stroke=False, hide=-0.1) for sx in (-1, 1)]
    p.append(path(HEAD, flat_pts([(94, 142), (100, 147), (106, 142)], cz=4, lift=2), "M{0} Q{1} {2}", width=3, hide=0.15))
    p += spot_parts(c, head=(98, 64, 60, 4))
    return dict(name="Chancho", parts=p)


def cow(c=None):
    c = colors("COW", c)
    main, spot, muzzle, horn = c["main"], c["spot"], "#FFC4CE", "#F3DFB5"
    hz = (0, 6)
    p = [rope_tail([(40, 190, -30), (64, 196, -40), (66, 170, -44)], main, width=10),
         part(BODY, 63, 166, -46, 6, 6, spot, behind=0, tag="tailTip")]
    p += legs(main, spot, arms=main)
    p.append(body(main))
    p.append(path(BODY, flat_pts([(64, 168), (72, 158), (90, 162), (88, 176), (86, 188), (70, 190), (64, 180)], cy=176, rx=42, ry=38, r=40),
                  "M{0} C{1} {2} {3} C{4} {5} {6} Z", fill=spot, width=0, hide=0.0, behind=-2))
    for sx in (-1, 1):
        h = [(100 + sx * 34, 50), (100 + sx * 42, 26), (100 + sx * 28, 24), (100 + sx * 28, 38), (100 + sx * 20, 44)]
        p.append(path(HEAD, round_pts(h, zoff=-34), "M{0} Q{1} {2} Q{3} {4} Z", fill=horn, behind=2))
        p.append(part(HEAD, sx * 68, 84, 0, 22, 11, main, rot=sx * 18, behind=2))
        p.append(part(HEAD, sx * 70, 85, 1, 12, 5, muzzle, rot=sx * 18, behind=2, stroke=False))
    p.append(vol(HEAD, 0, 98, hz, (68, 62), (60, 58), main))
    p.append(path(HEAD, flat_pts([(114, 54), (128, 48), (142, 58), (140, 70), (136, 82), (120, 78), (116, 70), (112, 64), (110, 58)], cz=6),
                  "M{0} C{1} {2} {3} C{4} {5} {6} C{7} {8} {0} Z", fill=spot, width=0, hide=0.0))
    p.append(path(HEAD, flat_pts([(72, 144), (100, 158), (128, 144)], cz=6, lift=6), "M{0} Q{1} {2}", color="#E86A6A", width=7, hide=-0.2))
    p.append(path(HEAD, flat_pts([(90, 152), (100, 146), (110, 152), (112, 166), (100, 172), (88, 166)], cz=6, lift=10),
                  "M{0} Q{1} {2} L{3} Q{4} {5} Z", fill="#FFD166", width=4, hide=-0.1))
    p += eyes2(28, 94, 6)
    p += blush2(50, 110, 6)
    p.append(part(HEAD, 0, 126, 56, (34, 30), 20, muzzle, behind=-0.1))
    p += [part(HEAD, sx * 12, 124, 74, 4, 5.5, "#C97884", behind=-0.1, stroke=False, hide=-0.1) for sx in (-1, 1)]
    p.append(path(HEAD, fixed_pts([(93, 135), (100, 140), (107, 135)], 74), "M{0} Q{1} {2}", width=3, hide=0.15))
    return dict(name="Vaca", parts=p)


def lion(c=None):
    c = colors("LION", c)
    main, mane, light = c["main"], c["mane"], c["light"]
    hz = (0, 4)
    p = [rope_tail([(36, 192, -30), (72, 198, -40), (70, 166, -44)], main, width=10),
         part(BODY, 66, 160, -46, 9, 9, mane, behind=0, tag="tailTip")]
    p += legs(main, light, belly=light)
    p.append(body(main))
    p.append(dict(kind="mane", layer=HEAD, y=94, z=0, r=72, fill=mane))
    for sx in (-1, 1):
        p.append(part(HEAD, sx * 44, 52, 10, 14, 14, main, behind=2))
        p.append(part(HEAD, sx * 44, 52, 11, 7, 7, light, behind=2, stroke=False))
    p.append(vol(HEAD, 0, 100, hz, (58, 54), (52, 50), main))
    p.append(part(HEAD, 0, 98, -50, 54, 50, c["mane_dark"], flat=True, p=0.4, hide=0.05, stroke=False, clip="head"))
    p.append(path(HEAD, [(-16, 70, -48), (-8, 90, -50), (-16, 112, -46)], "M{0} Q{1} {2}", color=c["mane_line"], width=3.5, hide=0.1))
    p.append(path(HEAD, [(0, 64, -50), (8, 90, -52), (0, 120, -48)], "M{0} Q{1} {2}", color=c["mane_line"], width=3.5, hide=0.1))
    p.append(path(HEAD, [(16, 70, -48), (24, 90, -50), (16, 112, -46)], "M{0} Q{1} {2}", color=c["mane_line"], width=3.5, hide=0.1))
    p += eyes2(24, 98, 4)
    p += blush2(42, 120, 4)
    p.append(part(HEAD, 0, 124, 52, 22, 15, light, behind=-0.1))
    p.append(path(HEAD, fixed_pts([(92, 113), (100, 109), (108, 113), (104, 121), (100, 121), (96, 121)], 64),
                  "M{0} Q{1} {2} Q{3} {4} Q{5} {0} Z", fill="#7A4A2E", width=2.5, hide=-0.1))
    p.append(path(HEAD, fixed_pts([(91, 125), (95.5, 131), (100, 125), (104.5, 131), (109, 125)], 66), "M{0} Q{1} {2} Q{3} {4}",
                  width=3.5, hide=0.15))
    return dict(name="León", parts=p)


def monkey(c=None):
    c = colors("MONKEY", c)
    main, face = c["main"], c["face"]
    hz = (0, 4)
    p = [rope_tail([(36, 196, -30), (76, 200, -40), (88, 154, -44), (66, 144, -40)], main, width=11)]
    p += legs(main, main, belly=face)
    p.append(body(main))
    for sx in (-1, 1):
        p.append(part(HEAD, sx * 68, 102, 0, 20, 20, main, behind=2))
        p.append(part(HEAD, sx * 68, 102, 1, 11, 11, face, behind=2, stroke=False))
    p.append(vol(HEAD, 0, 98, hz, (64, 58), (58, 56), main))
    p.append(path(HEAD, flat_pts([(96, 44), (100, 30), (111, 36)], cz=4, lift=-6), "M{0} Q{1} {2}", width=3.5, behind=2, bb=(2, 4)))
    fp = [(100, 72), (82, 54), (46, 62), (50, 100), (52, 134), (76, 148), (100, 148), (124, 148), (148, 134), (150, 100), (154, 62), (118, 54)]
    p.append(path(HEAD, flat_pts(fp, cz=4, lift=1), "M{0} C{1} {2} {3} C{4} {5} {6} C{7} {8} {9} C{10} {11} {0} Z", fill=face, hide=0.05))
    p += eyes2(22, 100, 4)
    p += blush2(38, 124, 4)
    p += [part(HEAD, sx * 5, 118, on_head(sx * 5, 118, cz=4) + 2, 2.3, 2.3, INK, stroke=False, flat=True, hide=0.2) for sx in (-1, 1)]
    p.append(path(HEAD, flat_pts([(84, 128), (100, 142), (116, 128)], cz=4, lift=2), "M{0} Q{1} {2}", width=3.5, hide=0.15))
    return dict(name="Mono", parts=p)


def frog(c=None):
    c = colors("FROG", c)
    main, light = c["main"], c["light"]
    p = []
    if M.LIE:  # acostada como las de cuatro patas (model3d.lie_legs)
        p += legs(main, main)
        p.append(body(main))
    else:
        for i, phi in enumerate((-40, 40, -140, 140)):
            x, z = 26 * math.sin(math.radians(phi)), 26 * math.cos(math.radians(phi))
            p.append(part(BODY, x, 214, z, 19, 9, main, behind=2, hide=-0.7, tag=("footL", "footR", "backL", "backR")[i]))
        p.append(vol(BODY, 0, (178, 180), (0, -4), (42, 48), (36, 32), main))
        p.append(part(BODY, 0, (188, 196), 38, (26, 30), (20, 11), light, flat=True, p=0.5, hide=-0.3, stroke=False, clip="body", behind=-2))
        for side, tag in ((-1, "armL"), (1, "armR")):
            x, z = side * 38 * math.sin(math.radians(58)), 38 * math.cos(math.radians(58))
            p.append(part(BODY, x, 182, z, 10, 15, main, rot=-side * 22, behind=-0.1, tag=tag))
    for sx in (-1, 1):  # ojos saltones arriba de la cabeza
        p.append(part(HEAD, sx * 34, 62, 14, 27, 27, main, behind=2))
    p.append(vol(HEAD, 0, 108, (0, 4), (74, 64), (50, 48), main))
    for sx in (-1, 1):  # tapan la unión de los ojos con la cabeza
        p.append(part(HEAD, sx * 34, 62, 16, 25, 25, main, stroke=False, behind=-2))
    p.append(path(HEAD, fixed_pts([(50, 62), (150, 62), (150, 92), (50, 92)], 30), "M{0} L{1} L{2} L{3} Z", fill=main, width=0, behind=-2,
                  hide=-0.5))
    p += [eye(sx * 34, 64, 38) for sx in (-1, 1)]
    p += [part(HEAD, sx * 54, 112, on_head(sx * 54, 112, cy=108, rx=74, ry=50, r=60), 11, 6.5, BLUSH, flat=True, hide=0.05, stroke=False,
               opacity=0.65) for sx in (-1, 1)]
    p.append(path(HEAD, flat_pts([(70, 116), (100, 138), (130, 116)], cy=108, rx=74, ry=50, r=60, lift=2), "M{0} Q{1} {2}", hide=0.15))
    p += [part(HEAD, sx * 8, 100, on_head(sx * 8, 100, cy=108, rx=74, ry=50, r=60) + 2, 2, 2, INK, stroke=False, flat=True, hide=0.2)
          for sx in (-1, 1)]
    p += spot_parts(c, head=(108, 74, 50, 4), body_geo=(178, 42, 36))
    return dict(name="Rana", parts=p)


def duck(c=None):
    c = colors("DUCK", c)
    main, bill, scarf, wing, tuft = c["main"], "#FFA94D", "#7FB8E8", c["wing"], c["tuft"]
    p = [path(BODY, [(40, 176, -30), (60, 166, -38), (52, 184, -36)], "M{0} L{1} L{2} Z", fill=main, behind=0)]
    for i, sx in enumerate((-1, 1) if not M.SIT else ()):  # sentado: esconde las patas
        x0 = sx * 20
        foot = [(x0 - 14, 206, 22), (x0 + 14, 206, 22), (x0 + 14, 222, 26), (x0 + 8, 214, 24), (x0, 222, 26)]
        if sx < 0:
            foot = [(x0 - 14, 206, 22), (x0 + 14, 206, 22), (x0, 222, 26), (x0 - 8, 214, 24), (x0 - 14, 222, 26)]
        p.append(path(BODY, foot, "M{0} L{1} L{2} L{3} L{4} Z", fill=bill, behind=2, tag=("footL", "footR")[i]))
    if M.SIT:  # esponjado, apoyado en el piso
        p.append(vol(BODY, 0, 190, (0, -4), (54, 56), (32, 30), main))
    else:
        p.append(vol(BODY, 0, (176, 178), (0, -4), (42, 48), (38, 34), main))
    for side, tag in ((-1, "armL"), (1, "armR")):
        p.append(part(BODY, side * 38, 180, 18, 11, 20, wing, rot=-side * 28, behind=-0.1, tag=tag))
    p.append(part(BODY, 0, (186, 194), 38, (24, 28), (20, 12), "#FFF8DC", flat=True, p=0.5, hide=-0.3, stroke=False, clip="body", behind=-2))
    tp = [(98, 42), (90, 26), (98, 16), (106, 22), (104, 28), (110, 30), (114, 26), (116, 36), (108, 42), (104, 44)]
    p.append(path(HEAD, fixed_pts(tp, 6), "M{0} C{1} {2} {3} C{4} {5} {6} C{7} {8} {9} Z", fill=tuft, behind=2, bb=(4, 6)))
    p.append(vol(HEAD, 0, 98, (0, 6), (66, 62), (60, 58), main))
    p.append(path(HEAD, flat_pts([(100, 156), (78, 146), (80, 168)], cz=6, lift=14), "M{0} L{1} L{2} Z", fill=scarf, hide=-0.2))
    p.append(path(HEAD, flat_pts([(100, 156), (122, 146), (120, 168)], cz=6, lift=14), "M{0} L{1} L{2} Z", fill=scarf, hide=-0.2))
    p.append(part(HEAD, 0, 156, 42, 6, 6, scarf, behind=-0.2, hide=-0.2))
    p += eyes2(28, 98, 6)
    p += blush2(46, 120, 6)
    p.append(part(HEAD, 0, 124, 64, (24, 22), 11, bill, behind=-0.1))
    p.append(path(HEAD, fixed_pts([(82, 124), (100, 130), (118, 124)], 70), "M{0} Q{1} {2}", width=3, hide=0.15))
    return dict(name="Pato", parts=p)


def chick(c=None):
    c = colors("CHICK", c)
    main, dark, beak = c["main"], c["dark"], "#FF9F43"
    p = []
    for i, sx in enumerate((-1, 1) if not M.SIT else ()):
        cx = sx * 18
        for dx, ex in ((-7, -10), (0, 0), (7, 10)):
            p.append(path(BODY, [(cx + dx, 208, 30), (cx + ex, 222, 34)], "M{0} L{1}", color=beak, width=5, behind=2,
                          tag=("footL", "footR")[i]))
    if M.SIT:
        p.append(vol(HEAD, 0, 140, 0, (74, 72), (74, 74), main))
    else:
        p.append(vol(HEAD, 0, 132, 0, (68, 66), (80, 80), main))
    for side, tag in ((-1, "armL"), (1, "armR")):
        p.append(part(HEAD, side * 64, 146, 0, 12, 22, dark, rot=-side * 24, behind=-0.35, tag=tag))
    shell = [(64, 64), (62, 24), (100, 20), (138, 24), (136, 64), (127, 55), (118, 65), (109, 55), (100, 65), (91, 55), (82, 65), (73, 55)]
    a = math.radians(-10)
    shell = [(100 + (X - 100) * math.cos(a) - (Y - 44) * math.sin(a), 44 + (X - 100) * math.sin(a) + (Y - 44) * math.cos(a)) for X, Y in shell]
    p.append(path(HEAD, fixed_pts(shell, 18), "M{0} Q{1} {2} Q{3} {4} L{5} L{6} L{7} L{8} L{9} L{10} L{11} Z", fill="#FFFDF5", behind=-2, bb=(0, 4)))
    p += [eye(sx * 24, 110, on_head(sx * 24, 110, cy=132, rx=68, ry=80, r=66)) for sx in (-1, 1)]
    p += [part(HEAD, sx * 44, 132, on_head(sx * 44, 132, cy=132, rx=68, ry=80, r=66), 11, 6.5, BLUSH, flat=True, hide=0.05, stroke=False,
               opacity=0.65) for sx in (-1, 1)]
    p.append(path(HEAD, fixed_pts([(90, 124), (110, 124), (100, 137)], 66), "M{0} L{1} L{2} Z", fill=beak, width=3, hide=-0.1))
    return dict(name="Pollito", parts=p)


def owl(c=None):
    c = colors("OWL", c)
    main, dark, light, disc = c["main"], c["dark"], c["light"], c["disc"]
    p = []
    for sx in (-1, 1):
        p.append(path(HEAD, round_pts([(100 + sx * 58, 78), (100 + sx * 60, 34), (100 + sx * 24, 56)], R=74), "M{0} L{1} L{2} Z", fill=main, behind=2))
    for i, sx in enumerate((-1, 1) if not M.SIT else ()):
        cx = sx * 18
        for dx, ex in ((-7, -10), (0, 0), (7, 10)):
            p.append(path(BODY, [(cx + dx, 208, 30), (cx + ex, 222, 34)], "M{0} L{1}", color="#FFA94D", width=5, behind=2,
                          tag=("footL", "footR")[i]))
    if M.SIT:
        p.append(vol(HEAD, 0, 136, 0, (76, 74), (76, 76), main))
    else:
        p.append(vol(HEAD, 0, 130, 0, (70, 68), (82, 82), main))
    p.append(part(HEAD, 0, 174, 52, 38, 32, light, flat=True, p=0.5, hide=-0.1, stroke=False))
    for (X, Y) in ((86, 160), (100, 156), (114, 160), (93, 174), (107, 174), (100, 188)):
        p.append(path(HEAD, flat_pts([(X - 5, Y), (X, Y + 5), (X + 5, Y)], cy=130, rx=70, ry=82, r=68, lift=1), "M{0} Q{1} {2}", color=dark,
                      width=3, hide=0.1))
    for side, tag in ((-1, "armL"), (1, "armR")):
        p.append(part(HEAD, side * 64, 150, 0, 14, 32, dark, rot=-side * 14, behind=-0.35, tag=tag))
    for sx in (-1, 1):
        p.append(part(HEAD, sx * 26, 100, on_head(sx * 26, 100, cy=130, rx=70, ry=82, r=68), 27, 27, disc, flat=True, p=0.6, hide=0.05,
                      stroke=False))
    p += [eye(sx * 26, 100, on_head(sx * 26, 100, cy=130, rx=70, ry=82, r=68) + 1, (12, 14)) for sx in (-1, 1)]
    p += [part(HEAD, sx * 50, 126, on_head(sx * 50, 126, cy=130, rx=70, ry=82, r=68), 11, 6.5, BLUSH, flat=True, hide=0.05, stroke=False,
               opacity=0.65) for sx in (-1, 1)]
    p.append(path(HEAD, fixed_pts([(93, 118), (107, 118), (100, 130)], 70), "M{0} L{1} L{2} Z", fill="#FFB347", width=3, hide=-0.1))
    return dict(name="Búho", parts=p)


def rabbit(c=None):
    """Conejo kawaii: ojos grandes con dos brillos, cachetes rosados, boquita ω y mechoncito."""
    c = colors("RABBIT", c)
    main, light, inner = c["main"], c["light"], c["inner"]
    hz = (0, 6)
    p = [part(BODY, 0, 196, -42, 15, 15, light, behind=0)]  # pompón (se ve de costado y de espaldas)
    p += legs(main, light, belly=light)
    p.append(body(main))
    # orejas largas, anchas y redondeadas; giran desde la base (pose earL/earR: subir y bajar)
    lop = c.get("lop", False)  # orejas caídas: siempre bajas a los costados, por delante de la cabeza
    for sx, tag in ((-1, "earL"), (1, "earR")):
        base = (sx * 128, sx * 172) if lop else 0  # de perfil cuelgan derecho hacia abajo
        behind = -0.35 if lop else 2               # de espaldas pasan detrás de la cabeza
        p.append(part(HEAD, sx * 26, 30, 4, 18, 44, main, rot=sx * 12, behind=behind, tag=tag, pivot_y=66, base_rot=base))
        p.append(part(HEAD, sx * 26, 36, 5, 9.5, 33, inner, rot=sx * 12, flat=True, p=0.6, hide=0.05, stroke=False, behind=behind,
                      tag=tag, pivot_y=66, base_rot=base))
    p.append(vol(HEAD, 0, 104, hz, (70, 64), (60, 58), main))
    # mechoncito arriba de la cabeza
    p.append(path(HEAD, fixed_pts([(90, 48), (93, 38), (99, 45), (102, 35), (108, 45), (112, 40), (112, 50)], 40),
                  "M{0} Q{1} {2} Q{3} {4} Q{5} {6}", fill=main, width=3.5, behind=-2, bb=(0, 40)))
    p += [eye(sx * 30, 106, on_head(sx * 30, 106, cy=104, cz=6), (12, 14), kawaii=True) for sx in (-1, 1)]
    p += [part(HEAD, sx * 46, 128, on_head(sx * 46, 128, cy=104, cz=6), 14, 9, BLUSH, flat=True, hide=0.05, stroke=False, opacity=0.55)
          for sx in (-1, 1)]
    nz = on_head(0, 120, cy=104, cz=6) + 4
    p.append(path(HEAD, fixed_pts([(95, 117), (105, 117), (100, 122)], nz), "M{0} Q{2} {1} Z", fill="#FF9FB2", width=2.5, hide=0.1))
    p.append(path(HEAD, fixed_pts([(93, 124), (96.5, 129), (100, 124), (103.5, 129), (107, 124)], nz), "M{0} Q{1} {2} Q{3} {4}",
                  width=3, hide=0.15))
    for sx in (-1, 1):  # bigotes cortitos y suaves
        w = flat_pts([(100 + sx * 60, 118), (100 + sx * 54, 119), (100 + sx * 48, 121)], cy=104, cz=6, lift=4)
        p.append(path(HEAD, w, "M{0} Q{1} {2}", width=2.5, color="#C9B8AA", hide=-0.1))
    p += spot_parts(c, head=(104, 64, 60, 6))
    return dict(name="Conejo", parts=p)

MORE = {"CAT": cat, "DUCK": duck, "FROG": frog, "COW": cow, "PIG": pig, "CHICK": chick, "LION": lion, "MONKEY": monkey, "OWL": owl, "RABBIT": rabbit}
