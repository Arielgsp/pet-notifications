# Deja pendiente el festejo del nivel 7 (bailar), abre la app y toca "Mostrame" en el diálogo, grabando.
export MSYS_NO_PATHCONV=1
ADB=$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe
S="C:/Users/ARIEL~1.SPA/AppData/Local/Temp/claude/C--Users-ariel-spagnuolo-Desktop-Pet-Notifications/62551c4f-1bc8-4e58-8582-9fb54cb497b0/scratchpad"
timeout 20 $ADB shell am force-stop com.petnotifications
timeout 30 $ADB exec-out run-as com.petnotifications cat shared_prefs/pet_prefs.xml > "$S/pp.xml"
python - <<EOF
import re
p = r"$S/pp.xml"; s = open(p, encoding="utf-8").read()
s = re.sub(r'<int name="bond_hearts" value="\d+" />', '<int name="bond_hearts" value="225" />', s)
s = re.sub(r'<int name="bond_celebrated" value="\d+" />', '<int name="bond_celebrated" value="6" />', s)
open(r"$S/pp2.xml", "w", encoding="utf-8").write(s)
EOF
timeout 30 $ADB push "$S/pp2.xml" /data/local/tmp/pet_prefs.xml >/dev/null; timeout 10 $ADB shell chmod 644 /data/local/tmp/pet_prefs.xml; timeout 20 $ADB shell run-as com.petnotifications sh /data/local/tmp/swapprefs.sh
timeout 20 $ADB shell am start -n com.petnotifications/.MainActivity >/dev/null; sleep 14
timeout 30 $ADB shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
B=$(timeout 20 $ADB exec-out cat /sdcard/ui.xml | grep -o 'text="Mostrame"[^>]*bounds="\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]"' | grep -o '\[[0-9,]*\]\[[0-9,]*\]')
echo "Mostrame: $B"
X=$(echo "$B" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\1 \3/' | awk '{print int(($1+$2)/2)}')
Y=$(echo "$B" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\2 \4/' | awk '{print int(($1+$2)/2)}')
timeout 20 $ADB shell rm -f /sdcard/bond.mp4
timeout 30 $ADB shell screenrecord --time-limit 10 /sdcard/bond.mp4 &
sleep 1
timeout 20 $ADB shell input tap $X $Y
wait
timeout 60 $ADB pull /sdcard/bond.mp4 "$S/bond.mp4" >/dev/null
python - <<EOF
import cv2, numpy as np
cap = cv2.VideoCapture(r"$S/bond.mp4"); fr = []
while True:
    ok, f = cap.read()
    if not ok: break
    fr.append(f)
print(len(fr), "cuadros")
idx = np.linspace(0, len(fr) - 1, 24).astype(int)
tiles = []
for i in idx:
    f = fr[i]; h, w = f.shape[:2]
    t = f[int(h*0.55):int(h*0.66), int(w*0.62):w].copy()
    cv2.putText(t, str(i), (5, 20), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0,0,255), 2)
    tiles.append(t)
rows = [np.hstack(tiles[r:r+12]) for r in range(0, 24, 12)]
cv2.imwrite(r"$S/dance_frames.png", np.vstack(rows))
EOF
