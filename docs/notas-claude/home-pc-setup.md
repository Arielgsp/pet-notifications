---
name: home-pc-setup
description: Home PC toolchain locations (all on E:, C: is nearly full), Gradle builds without the loopback workaround, emulator works with WHPX since 2026-10-29
metadata:
  type: reference
---
Home PC (user `ariel`, Ryzen 5 1600, ~32 GB RAM), set up 2026-10-15. C: has only ~5 GB free → everything heavy lives on E:
- JDK 17 Temurin: `E:\Java\jdk-17`; Android SDK: `E:\Android\Sdk` (platform-tools, platforms;android-36, build-tools;36.0.0, emulator, system-images;android-36;google_apis_playstore;x86_64); AVDs: `E:\Android\avd` (AVD `medium_phone`, API 36); Gradle cache: `E:\gradle-home`.
- User env vars set: JAVA_HOME, ANDROID_HOME, ANDROID_SDK_ROOT, ANDROID_AVD_HOME, GRADLE_USER_HOME, plus PATH entries. A session started before 2026-10-15 may not see them → set them inline. `local.properties` has `sdk.dir=E\:/Android/Sdk`.
- `gradlew assembleClosedTestDebug testClosedTestDebugUnitTest` works here WITHOUT the JAVA_TOOL_OPTIONS workaround from [[gradle-build-workaround]] (first build 5.4 min incl. downloads).
- Emulator works since 2026-10-29 (board ASUS PRIME A320M-K: SVM Mode in BIOS Advanced → CPU Configuration; Windows Hypervisor Platform on; `emulator -accel-check` → WHPX usable). Launch `E:\Android\Sdk\emulator\emulator.exe -avd medium_phone -no-snapshot-save -no-boot-anim` in background, wait for `sys.boot_completed`, `adb install -r`, grant `appops set com.petnotifications SYSTEM_ALERT_WINDOW allow` / SCHEDULE_EXACT_ALARM. Emulator locale is English. In Git Bash use MSYS_NO_PATHCONV=1 for `adb shell` paths like /sdcard. `uiautomator dump` + parse bounds to find buttons; `cmd statusbar expand-notifications` for notification actions. No PIL installed (crop screenshots via an HTML page in the browser pane).
- Git updated to 2.56 on 2026-10-28 (2.28's credential manager could not log in to GitHub); push now works with the stored login.
Related: [[pc-memory-limits]], [[pet-notifications-project]].
- Signing (29/10/2026): APKs are signed with the debug key; this PC now uses the WORK PC's `~/.android/debug.keystore` (SHA-256 bb:ae:8b…) so APKs install over the phone's app. Old home key saved as `debug.keystore.casa-backup` (don't use it). Different keys → "No se instaló la app debido a un conflicto con un paquete".
