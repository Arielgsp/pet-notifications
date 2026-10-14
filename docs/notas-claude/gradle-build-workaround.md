---
name: gradle-build-workaround
description: "How to build Pet_Notifications here — Gradle fails with \"Unable to establish loopback connection\" unless JAVA_TOOL_OPTIONS points unix-domain sockets to a short dir"
metadata:
  node_type: memory
  type: reference
  originSessionId: 62551c4f-1bc8-4e58-8582-9fb54cb497b0
  modified: 2026-09-23T19:50:34.242Z
---

In this Windows environment `gradlew` fails with `java.io.IOException: Unable to establish loopback connection` (even outside the sandbox). Fix, in PowerShell with sandbox disabled:

```
$env:JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=C:\Users\ariel.spagnuolo\.gradle\uds"
.\gradlew.bat assembleDebug testDebugUnitTest --console=plain -q
```
(the `uds` folder must exist). Filter noise lines `RemoteException|Picked up JAVA_TOOL|SDK XML`.

Emulator: AVD `medium_phone` (API 36) at `%LOCALAPPDATA%\Android\Sdk`. It is very slow/overloaded right after boot (ANRs in launcher, Play Store, etc. are emulator noise, not app bugs). Related: [[pet-notifications-project]].


Since 2026-09-28 the app has product flavors: use assembleClosedTestDebug / assembleClosedTestRelease (plain assembleDebug builds both flavors and takes twice as long). Stop the emulator before long builds (7.8 GB RAM PC). Emulator-granted appops (SYSTEM_ALERT_WINDOW) are lost on reboot unless you run `adb shell cmd appops write-settings` first. bmgr backups skip force-stopped apps ("Backup is not allowed").
