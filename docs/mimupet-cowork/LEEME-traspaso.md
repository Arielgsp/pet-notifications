# MimuPet — estado del proyecto (traspaso de sesión)

Este resumen viene de la sesión de Cowork del 31/10 al 5/11/2026, que estaba vinculada a la PC del trabajo. Adjuntalo al empezar la sesión nueva en la PC de tu casa, donde está el proyecto real (`C:\Users\ariel\Desktop\pet-notifications`).

## Decisiones tomadas

- **Nombre:** MimuPet. App Android en la que una mascota flota sobre el celular y entrega los recordatorios y eventos con su onomatopeya («guau», «miau», etc.).
- **Modelo:** gratis + **Pro de pago único**, no suscripción, con prueba de 7 días.
  - **Precio:** base USD 3,99, de lanzamiento USD 2,49, con precios por región.
  - **Lanzamiento:** Pro incluye **7 mascotas**: perro, gato, conejo, pato, chancho, pollito y vaca.
  - **Pack «Amigos salvajes»:** león, mono, rana y búho, a USD 1,49, unas 4 a 6 semanas después. Quien compró Pro antes de que salga el pack lo recibe de regalo.
  - **Pelajes:** salen de Pro y se venden en packs, a USD 0,99.
  - **Accesorios:** los del vínculo se ganan y nunca se venden. Los de temporada se venden en packs.
- **Vínculo:** 8 niveles. Cada «Listo» suma un corazón, con un tope de 10 por día. La mascota aprende trucos y gana accesorios.
- **Pantalla Mascota:** una sola pantalla, sin pestañas internas.
  - Cabeza, Cuello y Camita van en una columna al costado de la mascota.
  - El nivel va en una línea con los corazones.
  - Abajo, una tira con las 11 mascotas.
  - Vestir es una hoja que sube desde abajo.
  - Tocar una mascota Pro la muestra grande con *Probar 5 min*.
  - Tamaño, transparencia, modo fantasma y *Probar un aviso* pasan a Ajustes.
- **Accesorios:**
  - Cabeza: gorro navideño, gorrito de lana, calabacita, corona de flores y **casco de calabaza** (disfraz de Halloween, aparte de la calabacita).
  - Cuello: pañuelo, que reemplaza la campana de la vaca y el moñito del pato.
  - Camita.
  - En el pollito, los accesorios de cabeza reemplazan la cáscara.
- **Idioma:** el español por defecto es tuteo neutro. El voseo queda para Argentina y Uruguay (`values-es-rAR` y `values-es-rUY`). Los videos ya están en tuteo.
- **Ícono:** el perrito contento, con los ojos cerrados. Para la app sin corazones; para la foto de perfil de las redes, con dos corazones.

## Archivos que le pasé a Code (están en el chat de la sesión anterior; descargalos)

| Archivo | Qué es |
| --- | --- |
| `accesorios-11-especies.zip` | Accesorios para las 11 especies, de frente y de perfil, con el casco de calabaza, las medidas de ubicación y un LEEME |
| `mascota-una-pantalla-para-code.md` | Rediseño de la pantalla Mascota y la hoja Vestir |
| `espanol-neutro-y-voseo-para-code.md` | Cómo separar tuteo y voseo, con la tabla de conversión |
| `mimupet-icono-app.zip` | Ícono de Google Play (512 px) y del celular (fondo, dibujo y versión monocromática) |
| `mimupet-avisos-vestidor.zip` | Rediseño anterior de Avisos y Vestidor (ya reemplazado en parte por el de una sola pantalla) |

## Material para redes (en el mismo chat)

- **Videos de lanzamiento (12,5 s):** perro («compañero perruno»), gato («gatuno»), conejo («orejudo») y granja. Cada mascota trae un aviso y se toca *Listo*.
- **`mimupet-video1-simulacion.mp4`:** simulación del celular con un aviso encima de una app de mensajes inventada.
- **`mimupet-video2-ronda-7.mp4`:** ronda de las 7 mascotas, con las siluetas de las que llegan después.
- **`mimupet-teaser-leon.mp4`:** adelanto del León en la selva, para cuando salga el pack.
- **Foto de perfil:** `mimupet-icono-redes.png`.

## Documentos y páginas en claude.ai (se abren desde cualquier sesión)

- **Design System de MimuPet:** https://claude.ai/artifact/E13qkSQK22UezSXtJgWZ6o
- **Informe de Code con las respuestas de Cowork:** https://claude.ai/artifact/MYz4g22hYn33Aj9Z2AdMmc
- **Textos para la tienda y frases:** https://claude.ai/artifact/XhKHMSp3EqBsNwnSRy3GdB
- **Plan de videos cortos:** https://claude.ai/artifact/A5ZHnUtxiV83JfepmqT8RN
- **Prensa y creadores para el lanzamiento:** https://claude.ai/artifact/8wQJouXX67tVRJBbiJnfMG

## Pendientes

- [ ] **Code:** aplicar los accesorios, la pantalla Mascota, el ícono y la separación de tuteo y voseo.
- [ ] **Code:** revisar si la mascota se queda de costado en algún momento. Si es así, usar el dibujo fijo de perfil, porque el modelo que gira se ve mal a 90°.
- [ ] **Code:** corregir el texto viejo «Con Pro tenés las 10».
- [ ] **Code:** grabar un video real del emulador para las capturas de la tienda.
- [ ] **Cowork:** pasar a tuteo los mensajes para medios de fuera de Argentina, en el documento de prensa.
- [ ] **Cowork:** si se decide hacer el sistema de códigos para creadores (un producto de Play por creador, con 20–30 % de descuento y un enlace que lo aplica solo), escribir las instrucciones para Code y un modelo de acuerdo con el creador.
- [ ] **Tuyo:**
  - clave de subida y política de privacidad;
  - mail para comentarios;
  - prueba cerrada con 12 testers;
  - capturas reales de la app;
  - registrar MimuPet en el INPI (clase 9);
  - revisión del inglés por un nativo.
