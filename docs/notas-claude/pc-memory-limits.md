---
name: pc-memory-limits
description: Ask before running the emulator or Gradle builds while the user is on the WORK PC (7.8 GB RAM); the home PC (32 GB) has no such limit
metadata:
  type: feedback
---
On the work PC (7.8 GB RAM, user `ariel.spagnuolo`): before starting the Android emulator (or long Gradle builds), check whether the user is using the PC; if they might be, ask first or send the already-built APK for phone testing instead.

**Why:** 2026-10-14 the user was working on the work PC while I booted the emulator and said there wasn't enough memory for both (see [[gradle-build-workaround]]).

**How to apply:** Only on the work PC. Since 2026-10-15 development is on the home PC (user `ariel`, ~32 GB RAM, Desktop\pet-notifications), where emulator + Gradle together are fine. On the work PC: overnight / "I'm leaving the PC on" = free to use; otherwise small steps, reuse built APKs, ask before emulator runs.
