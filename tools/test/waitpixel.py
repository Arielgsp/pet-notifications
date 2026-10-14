# Espera hasta que el pixel (x, y) de la pantalla del emulador tenga un color parecido al pedido.
# Uso: waitpixel.py X Y RRGGBB TIMEOUT_S
import os, subprocess, sys, time
x, y, color, limit = int(sys.argv[1]), int(sys.argv[2]), sys.argv[3], float(sys.argv[4])
want = tuple(int(color[i:i + 2], 16) for i in (0, 2, 4))
adb = os.path.join(os.environ["LOCALAPPDATA"], "Android", "Sdk", "platform-tools", "adb.exe")
end = time.time() + limit
while time.time() < end:
    raw = subprocess.run([adb, "exec-out", "screencap"], capture_output=True, timeout=30).stdout
    if len(raw) < 16:
        continue
    w = int.from_bytes(raw[0:4], "little")
    h = int.from_bytes(raw[4:8], "little")
    header = len(raw) - w * h * 4
    off = header + (y * w + x) * 4
    px = tuple(raw[off:off + 3])
    if all(abs(a - b) <= 12 for a, b in zip(px, want)):
        print("ok", px)
        sys.exit(0)
print("timeout")
sys.exit(1)
