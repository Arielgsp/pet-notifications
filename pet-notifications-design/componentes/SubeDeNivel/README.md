# SubeDeNivel

Los dos momentos del vínculo que se celebran sobre la pantalla: el "+1" al tocar "Listo" y la subida de nivel.

**+1 al tocar "Listo":** una pastilla `surface-raised` con corazón `blush` y "+1" sube 14px desde la mascota y se desvanece (≈1,5 s). Sin sonido. Si se llegó al tope del día, no aparece.

**Sube de nivel:**
- La mascota se pone el accesorio nuevo (rebote mínimo) y le salen 5 corazoncitos en abanico.
- Tarjeta centrada arriba (`surface-raised`, `radius-lg`, `shadow-soft`): chip "Nivel 3 · Amigos", título "Luna aprendió a dar la patita" (`notif-title`), cómo usarlo ("Tocala 3 veces y te la da") y acciones *Mostrame* / *Después*.
- *Mostrame* hace el truco en el momento. Se cierra sola a los 10 s.
- Nunca interrumpe un aviso: si hay uno abierto, la celebración espera a que se resuelva. Con No molestar o en silencio, espera al siguiente desbloqueo.

**Voz:** en tercera persona con el nombre ("Luna aprendió…") o en primera ("¡Aprendí a dar la patita!"); elegir una y usarla siempre. Sin "¡Felicitaciones!" ni puntos o números grandes: se celebra lo que la mascota aprendió, no un puntaje.
