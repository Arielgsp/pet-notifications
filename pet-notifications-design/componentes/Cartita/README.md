# Cartita

La mascota llega con un sobre, lo deja a sus pies y te llama con su onomatopeya hasta que lo tocás; es el estilo más tierno.

**Recibe:** especie y nombre de la mascota, título, cuerpo, 1–2 acciones, hora del evento (si tiene).

- **Llega:** entra caminando (700 ms) con el sobre en la boca (`cream`, sello `blush`) y lo deja a sus pies. Así tiene la boca libre para llamarte.
- **Te llama:** hace su Anuncio ("guau", "miau") y el sobre se mueve un poco. Se repite cada 5 min mientras siga sin abrir. Las 3 primeras veces aparece al lado la ayuda "tocá la cartita" (`notif-meta`, `ink-muted` en una pastilla `surface-raised`).
- **Al tocar:** la nota se despliega desde abajo (500 ms): fondo `paper`, borde `line`, `radius-md`, inclinada -1,5°, corazoncito `blush` arriba a la derecha. Línea superior: "Luna te trajo esto".
- **Se abre sola:** si el aviso tiene hora y faltan 15 min o menos, o después de 3 llamados sin respuesta, la cartita se abre sola como Asomado. El llamado evita que no te des cuenta, pero no te dice qué es sin tocar.

**Sí:** cumpleaños, notas para uno mismo, recordatorios con hora de nivel normal. **Importante:** mejor Asomado directo.

**Esfuerzo:** medio-alto. Poses con el sobre en la boca y a sus pies, animación de apertura y temporizador de repetición.
