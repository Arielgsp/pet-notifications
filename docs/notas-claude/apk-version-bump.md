---
name: apk-version-bump
description: Bump appVersionCode in gradle.properties before every APK sent to the user (Xiaomi won't update same versionCode)
metadata:
  type: feedback
---

Antes de cada APK que se le manda al usuario, subir `appVersionCode` en `gradle.properties` (versionName queda "1.0.<n>") y nombrar el archivo con esa versión (p. ej. PetNotifications-1.0.3.apk).

**Why:** hasta el 27/09 todas las APK tenían versionCode 1; en el Xiaomi del usuario, al abrir la APK nueva no aparecía el instalador sino que se abría la app ya instalada.
**How to apply:** subir el número, compilar, verificar con aapt2 dump badging (scratchpad/aapt/aapt2.exe) y recién ahí mandar. La versión se ve al final de la pantalla principal. Related: [[pet-notifications-project]].
