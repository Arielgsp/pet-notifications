---
name: pending-list
description: Where the running list of open decisions, missing tasks, untested items and known limitations for Pet Notifications lives, and the rule to keep it updated
metadata:
  type: feedback
---

The user asked (2026-09-27) for an internal list of ALL pending items since the Cowork plan started, so that whenever they ask "¿qué queda pendiente?" everything is listed. It lives in the repo at `docs/PENDIENTES.md` (sections: decisions awaiting the user, remaining Cowork tasks, open A–L problems, things only testable on a real phone, known limitations, tech debt).

**Why:** the user works across many turns and chats and doesn't want open items to get lost.
**How to apply:** update `docs/PENDIENTES.md` at the end of every task (add/remove items, bump the date) and answer "pendientes" questions from it. Related: [[pet-notifications-project]].
