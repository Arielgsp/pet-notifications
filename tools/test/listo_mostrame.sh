# Toca "Listo" en el globo abierto, espera la tarjeta del vínculo, toca "Mostrame" y graba.
export MSYS_NO_PATHCONV=1
ADB=$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe
S="C:/Users/ARIEL~1.SPA/AppData/Local/Temp/claude/C--Users-ariel-spagnuolo-Desktop-Pet-Notifications/62551c4f-1bc8-4e58-8582-9fb54cb497b0/scratchpad"
timeout 20 $ADB shell rm -f /sdcard/bond.mp4
timeout 45 $ADB shell screenrecord --time-limit 30 /sdcard/bond.mp4 &
sleep 2
timeout 20 $ADB shell input tap 893 1549
B=""
for i in $(seq 1 12); do
  sleep 1
  timeout 30 $ADB shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
  B=$(timeout 20 $ADB exec-out cat /sdcard/ui.xml | grep -o 'text="Mostrame"[^>]*bounds="\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]"' | grep -o '\[[0-9,]*\]\[[0-9,]*\]')
  [ -n "$B" ] && break
done
echo "Mostrame: $B"
if [ -n "$B" ]; then
  X=$(echo "$B" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\1 \3/' | awk '{print int(($1+$2)/2)}')
  Y=$(echo "$B" | sed -E 's/\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]/\2 \4/' | awk '{print int(($1+$2)/2)}')
  timeout 20 $ADB shell input tap $X $Y
  echo "tap $X $Y"
fi
wait
timeout 60 $ADB pull /sdcard/bond.mp4 "$S/bond.mp4" >/dev/null
timeout 30 $ADB exec-out run-as com.petnotifications cat shared_prefs/pet_prefs.xml | grep -o '<[^>]*bond_hearts[^>]*>\|<[^>]*bond_celebrated[^>]*>'
