# Vuelve a dejar pendiente el festejo del nivel 3 y toca "Mostrame" en la tarjeta flotante, grabando.
export MSYS_NO_PATHCONV=1
ADB=$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe
S="C:/Users/ARIEL~1.SPA/AppData/Local/Temp/claude/C--Users-ariel-spagnuolo-Desktop-Pet-Notifications/62551c4f-1bc8-4e58-8582-9fb54cb497b0/scratchpad"
timeout 20 $ADB shell am force-stop com.petnotifications
timeout 30 $ADB exec-out run-as com.petnotifications cat shared_prefs/pet_prefs.xml > "$S/pp.xml"
python - <<EOF
import re
p = r"$S/pp.xml"; s = open(p, encoding="utf-8").read()
s = re.sub(r'<int name="bond_celebrated" value="\d+" />', '<int name="bond_celebrated" value="2" />', s)
open(r"$S/pp2.xml", "w", encoding="utf-8").write(s)
EOF
timeout 30 $ADB push "$S/pp2.xml" /data/local/tmp/pet_prefs.xml >/dev/null; timeout 10 $ADB shell chmod 644 /data/local/tmp/pet_prefs.xml; timeout 20 $ADB shell run-as com.petnotifications sh /data/local/tmp/swapprefs.sh
timeout 20 $ADB shell am start -n com.petnotifications/.MainActivity >/dev/null; sleep 12
# Con la app abierta sale el diálogo: se sale sin tocarlo y la mascota festeja sola (al minuto, como mucho).
timeout 20 $ADB shell input keyevent HOME
python "$S/waitpixel.py" 414 554 FBE7E3 150
timeout 20 $ADB shell rm -f /sdcard/bond.mp4
timeout 30 $ADB shell screenrecord --time-limit 14 /sdcard/bond.mp4 &
sleep 1
timeout 20 $ADB shell input tap 414 554
wait
timeout 60 $ADB pull /sdcard/bond.mp4 "$S/bond.mp4" >/dev/null
timeout 30 $ADB exec-out run-as com.petnotifications cat shared_prefs/pet_prefs.xml | grep -o '<[^>]*bond_celebrated[^>]*>'
