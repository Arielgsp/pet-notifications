---
name: billing-decision
description: "Task 11 decision on Play Billing pulling INTERNET/ACCESS_NETWORK_STATE (via datatransport) — try stripping, fall back to accepting"
metadata:
  node_type: memory
  type: project
  originSessionId: 62551c4f-1bc8-4e58-8582-9fb54cb497b0
  modified: 2026-09-27T17:17:09.976Z
---

Play Billing Library (all versions 6.x–9.1.0) depends on com.google.android.datatransport (transport-backend-cct / transport-runtime), whose manifests add INTERNET and ACCESS_NETWORK_STATE. Decision (2026-09-27): option A — use the library but strip both permissions with `tools:node="remove"`, and test hard (Android 14+ JobScheduler network constraints may throw SecurityException without ACCESS_NETWORK_STATE).

**Why:** the Cowork rule "tus datos no salen del celular" is a selling point, but the user's top priority is that the app works: "ante el primer problema complejo, vamos por la B" (B = accept the permissions, drop the no-internet claim, declare Google's library diagnostics in Data safety).
**How to apply:** at the first complex problem with A, switch to B without re-asking, and tell the user. Other task 11 answers: "Enviar comentarios" email = a new address the user will create (keep a single placeholder constant); Pro-only settings under "tamaño/comportamiento" = size + opacity + ghost mode (bubble duration and tap action stay free). Related: [[pet-notifications-project]], [[pending-list]].
