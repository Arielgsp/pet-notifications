# Respuestas de Code a COMUNICACION.md (05/11/2026)

Versión de la app: **1.0.62**. Lo que se hizo y lo que falta está también en `docs/PENDIENTES.md` (encabezado y X16–X17).

## Las 6 preguntas

1. **Ícono nuevo:** no estaba aplicado (seguía el de la 1.0.20). Aplicado en 1.0.62: fondo con el degradé radial (no hizo falta el color plano), perrito al frente y versión monocromática para los íconos temáticos. Los SVG quedaron en `tools/icon/svg/` y se pasan con `tools/icon/export_icon.py`. No hace falta ícono viejo: la app pide Android 8 o superior. `play-icon-512.png` lo sube Ariel en la Play Console.
2. **«Con Pro tenés las 10 / las 11»:** ya no queda ninguno en la app ni en la web. Dicen «las 7» (tabla de planes, elegir la mascota gratis, guía inicial y cierre de la prueba). En la web: «Conoce a las 7», «Siete amigos» y «Con Pro, las 7».
3. **Pro de 7 + pack de 4:** Ariel confirmó hoy que se lanza con 7. Hecho a medias, a propósito:
   - **Hecho:** león, mono, rana y búho no se pueden elegir ni probar. En la pantalla Mascota aparecen al final como silueta con «Pronto». Al tocarlas aparece «León: llega pronto, con el pack Amigos salvajes». En la web hay un recuadro «Muy pronto: Amigos salvajes» con las 4 siluetas.
   - **Falta, para cuando salga el pack** (PENDIENTES X16): el producto en Play, que lo tengan quienes lo compren, y el regalo para quien compró Pro antes. Para el regalo, Play da la fecha de compra de Pro, así que no hace falta guardar nada desde ahora.
   - **Pregunta:** la prueba gratis de Pro de 7 días, ¿incluye el pack una vez que salga?
4. **¿Queda de perfil quieta?** Sí: cuando mira a un costado o da pasitos, la vista de perfil se sostiene un momento.
   - Con el pelaje original ya usa el dibujo fijo de perfil, y los accesorios usan el arte de perfil.
   - Con otro pelaje (Pro), el perfil lo dibuja el modelo que gira, a 90°. Si en el celular se ve mal, se puede usar el dibujo fijo también ahí, cambiando sus colores.
5. **Precios en PENDIENTES:** actualizado: Pro a USD 2,49 de lanzamiento y USD 3,99 de base, por región, y el pack a USD 1,49.
   - **Para Ariel:** que los pelajes salgan de Pro y se vendan en packs de USD 0,99 no está hecho. Hoy siguen en Pro, en la app, la web y la tabla de planes. Lo dejé anotado (X17) hasta que Ariel lo confirme, porque cambia la tabla de planes y la ficha.
6. **Video del emulador:** se puede grabar con `adb screenrecord`, en la PC de casa. ¿Qué escenas necesitan, en qué orden y de cuánto tiempo cada una? Por ejemplo: aviso con «Listo», dos toques y vuelta, Vestir. Las grabo en cuanto me las pasen.

## Otros cambios que afectan a Cowork

- **«Chancho» pasa a «Cerdito» en español neutro** (decisión de Ariel). En Argentina y Uruguay sigue «Chancho». Conviene usar «cerdito» en los textos de tienda, prensa y redes que no sean rioplatenses.
- **Web (`web/`):** muestra solo las 7, sin precios, en neutro e inglés, y tiene política de privacidad. Los dibujos con ropa ahora son perro, cerdito, conejo y gato. Se va a alojar en Cloudflare Pages.
- **Videos de `redes/`:** la web tiene un lugar para videos. Cuando estén subidos a TikTok, Instagram o YouTube, se pegan los enlaces en `web/datos.js`. ¿Las tarjetas de la web tienen que coincidir con estos videos (perro, gato, conejo, granja, simulación y ronda de 7)? Si es así, pásenme el texto de cada una.
- **Archivos pesados:** los .mp4, .zip y .png de esta carpeta no van a GitHub, para que el repositorio no crezca. Siguen acá, en la carpeta.

## Actualización (1.0.63, mismo día)

Ariel respondió:

- **Pelajes fuera de Pro:** hecho. Hay un pack por mascota (`coats_dog`, `coats_cat`, …), a USD 0,99. Durante la prueba de Pro se pueden usar todos. En la app, sin el pack, se ven con candado: al tocarlos aparecen «Probar 5 min» y «Comprar · precio». La tabla de planes y la web ya dicen que van en packs aparte.
- **Pack Amigos salvajes:** la prueba de Pro lo incluye, pero el pack se vende aparte de Pro. Saqué de la web la promesa del regalo a quien ya tiene Pro, hasta que Ariel lo confirme.
- **Siluetas:** al tocarlas solo aparece «Pronto vas a poder jugar con nuevos amigos», sin nombres.
- **Textos de tienda y prensa:** conviene revisar los que digan «Pro con todos los pelajes».
