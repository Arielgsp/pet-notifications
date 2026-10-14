# Prueba del vínculo sobre la pantalla: prepara corazones, hace sonar un recordatorio, toca "Listo",
# espera la tarjeta, toca "Mostrame" y graba todo en video.
# Uso: bondtest.sh HEARTS CELEBRATED
export MSYS_NO_PATHCONV=1
ADB=$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe
S="C:/Users/ARIEL~1.SPA/AppData/Local/Temp/claude/C--Users-ariel-spagnuolo-Desktop-Pet-Notifications/62551c4f-1bc8-4e58-8582-9fb54cb497b0/scratchpad"
HEARTS=$1; CEL=$2
timeout 120 $ADB install -r "C:/Users/ariel.spagnuolo/Desktop/Pet_Notifications/app/build/outputs/apk/closedTest/debug/app-closedTest-debug.apk" | tail -1
timeout 20 $ADB shell am force-stop com.petnotifications
timeout 30 $ADB exec-out run-as com.petnotifications cat shared_prefs/pet_prefs.xml > "$S/pp.xml"
python - <<EOF
import re
p = r"$S/pp.xml"; s = open(p, encoding="utf-8").read()
s = re.sub(r'<int name="bond_hearts" value="\d+" />', '<int name="bond_hearts" value="$HEARTS" />', s)
s = re.sub(r'<int name="bond_celebrated" value="\d+" />', '<int name="bond_celebrated" value="$CEL" />', s)
open(r"$S/pp2.xml", "w", encoding="utf-8").write(s)
EOF
timeout 30 $ADB push "$S/pp2.xml" /data/local/tmp/pet_prefs.xml >/dev/null; timeout 10 $ADB shell chmod 644 /data/local/tmp/pet_prefs.xml; timeout 20 $ADB shell run-as com.petnotifications sh /data/local/tmp/swapprefs.sh
timeout 30 $ADB exec-out run-as com.petnotifications cat databases/pets.db > "$S/lk.db"; rm -f "$S/lk.db-wal"; timeout 30 $ADB exec-out run-as com.petnotifications cat databases/pets.db-wal > "$S/lk.db-wal"
NOW=$(timeout 10 $ADB shell date +%s | tr -d '\r'); FIRE=$((NOW + 80))
python - <<EOF
import sqlite3
c = sqlite3.connect(r"$S/lk.db")
c.execute("insert into reminders(title,note,timeMillis,repeat,fired) values('Prueba $HEARTS','',?, 'NONE',0)", ($FIRE*1000,)); c.commit()
c.execute("pragma wal_checkpoint(TRUNCATE)"); c.commit(); c.close()
EOF
rm -f "$S/lk.db-wal"; timeout 60 $ADB push "$S/lk.db" /data/local/tmp/pets.db >/dev/null; timeout 10 $ADB shell chmod 644 /data/local/tmp/pets.db; timeout 20 $ADB shell run-as com.petnotifications sh /data/local/tmp/swapdbonly.sh
timeout 20 $ADB shell am start -n com.petnotifications/.MainActivity >/dev/null; sleep 15
timeout 20 $ADB shell input keyevent HOME
for i in $(seq 1 250); do if timeout 10 $ADB shell dumpsys notification --noredact 2>/dev/null | grep -q "Prueba $HEARTS"; then echo "llegó"; break; fi; sleep 3; done
sleep 5
timeout 20 $ADB shell rm -f /sdcard/bond.mp4
timeout 40 $ADB shell screenrecord --time-limit 25 /sdcard/bond.mp4 &
sleep 2
timeout 20 $ADB shell input tap 893 1549
# Espera la tarjeta y busca "Mostrame"
for i in $(seq 1 10); do
  sleep 1.5
  timeout 30 $ADB shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  B=$(timeout 20 $ADB exec-out cat /sdcard/ui.xml | grep -o 'text="Mostrame"[^>]*bounds="\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]"' | grep -o '\[[0-9,]*\]\[[0-9,]*\]')
  [ -n "$B" ] && break
done
echo "Mostrame: $B"
if [ -n "$B" ]; then
  X=$(echo "$B" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\1 \3/' | awk '{print int(($1+$2)/2)}')
  Y=$(echo "$B" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\2 \4/' | awk '{print int(($1+$2)/2)}')
  timeout 20 $ADB shell input tap $X $Y
fi
wait
timeout 60 $ADB pull /sdcard/bond.mp4 "$S/bond.mp4" >/dev/null
timeout 30 $ADB exec-out run-as com.petnotifications cat shared_prefs/pet_prefs.xml | grep -o '<[^>]*bond_hearts[^>]*>\|<[^>]*bond_celebrated[^>]*>'
