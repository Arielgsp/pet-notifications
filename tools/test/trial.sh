# Uso: trial.sh <días atrás>  — mueve la "primera apertura" N días al pasado y borra las mascotas gratis.
export MSYS_NO_PATHCONV=1
ADB=$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe
DAYS=${1:-8}
TS=$(( ($(date +%s) - DAYS*86400) * 1000 ))
$ADB shell am force-stop com.petnotifications
$ADB shell "run-as com.petnotifications sh -c 'f=shared_prefs/pet_prefs.xml; sed -i \"/name=\\\"first_open\\\"/d; /name=\\\"free_pets\\\"/,/<\\/set>/d\" \$f; sed -i \"s|</map>|    <long name=\\\"first_open\\\" value=\\\"$TS\\\" />\\n</map>|\" \$f; cat \$f'" | grep -E 'first_open|free_pets|pro_purchased|name="pet"'
