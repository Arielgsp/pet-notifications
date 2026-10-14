# Saca N cuadros parejos de un video y los arma en una grilla (para revisar animaciones cortas).
import sys, cv2, numpy as np
src, out = sys.argv[1], sys.argv[2]
n = int(sys.argv[3]) if len(sys.argv) > 3 else 12
crop = sys.argv[4] if len(sys.argv) > 4 else None  # "y0:y1" en proporción, ej. "0.4:0.8"
cap = cv2.VideoCapture(src)
frames = []
while True:
    ok, f = cap.read()
    if not ok:
        break
    frames.append(f)
if not frames:
    sys.exit("sin cuadros")
idx = np.linspace(0, len(frames) - 1, n).astype(int)
tiles = []
for i in idx:
    f = frames[i]
    if crop:
        a, b = (float(x) for x in crop.split(":"))
        h = f.shape[0]
        f = f[int(h * a):int(h * b)]
    f = cv2.resize(f, (f.shape[1] // 3, f.shape[0] // 3))
    cv2.putText(f, str(i), (8, 24), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 2)
    tiles.append(f)
cols = 6
rows = [np.hstack(tiles[r:r + cols] + [np.zeros_like(tiles[0])] * (cols - len(tiles[r:r + cols]))) for r in range(0, len(tiles), cols)]
cv2.imwrite(out, np.vstack(rows))
print(len(frames), "cuadros")
