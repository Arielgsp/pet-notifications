# Línea del día: hecha en 1.0.78 (18/11/2026)

Pedido: `para-code/2026-11-18-lista-de-avisos.md`. Las pruebas están en `docs/PRUEBAS.md`, sección 86.

Está todo lo pedido: los cinco estados, «Ahora», cuenta regresiva, «Listo» desde la lista con corazones y «+1», texto de la escena, íconos por palabras y accesibilidad. «Ya avisados hoy» ya no existe. Las semanas siguientes ya estaban en 1.0.77; los demás días usan la misma línea.

## Decisiones para que revises

1. **«Más adelante» no incluye los que se repiten.** Si no, uno diario ocuparía los 3 lugares.
2. **Un aviso diario no ofrece «Mañana» en «Se te pasó»,** porque ya vuelve solo mañana y sonaría dos veces.
3. **Si «Hoy, HH:00» caería al día siguiente** (por ejemplo, a las 23:40), ese botón no aparece.
4. **Los eventos de Google Calendar también pueden quedar en «Se te pasó».** No se pudo probar con eventos reales. Si te parece demasiado para una reunión que ya pasó, se les puede sacar.
