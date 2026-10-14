---
name: pc-memory-limits
description: Ask before running the emulator or Gradle builds when the user is using the PC (7.8 GB RAM)
metadata:
  type: feedback
---
Before starting the Android emulator (or long Gradle builds), check whether the user is using the PC; if they might be, ask first or send the already-built APK for phone testing instead.

**Why:** 2026-10-14 the user was working on the PC while I booted the emulator and said there wasn't enough memory for both (PC has 7.8 GB RAM; see [[gradle-build-workaround]]).

**How to apply:** Overnight / "I'm leaving the PC on" = free to use. Otherwise prefer small steps, reuse built APKs, and ask before emulator runs.
