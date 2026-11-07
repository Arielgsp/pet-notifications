# Pedido de Cowork · 07/11/2026 · Camino de la mascota, rehecho

Lo aprobó Ariel. Maqueta (se puede tocar), en la tercera fila del lienzo, «Camino de la mascota»: https://claude.ai/artifact/6AadjKE3sDNdirdK4QVHo7

**Problema:** hoy el vínculo no se entiende ni da ganas de avanzar:
- se ve como un juego con puntaje (niveles, nombres, 10 corazones);
- hay premios que no se notan (la cola, los buenos días) o que dependen de un gesto que nadie conoce;
- casi todo depende de tocar «Listo»;
- el primer premio tarda varios días.

**Idea:** mostrar lo que viene, no el puntaje. Que todos los premios se vean y se festejen.

Lo que no cambia: el vínculo nunca resta, es del usuario y no de cada mascota, es gratis y viaja con la copia de seguridad.

## 1. Premios nuevos

| Corazones | Premio | Tipo |
| --- | --- | --- |
| 3 | Pañuelo | Cuello (ya existe) |
| 10 | Dar la patita | Truco (ya existe: 3 toques) |
| 22 | Camita | Ya existe |
| 40 | **Pelotita** | Nuevo: un juguete al lado de la mascota. A veces juega sola con ella (empujarla o perseguirla un poco) |
| 65 | Bailar | Truco (ya existe) |
| 95 | Gorrito de lana | Cabeza (ya existe) |
| 135 | **Chapita con su nombre** | Nuevo: va en el cuello y es una opción en lugar del pañuelo (un lugar, un accesorio) |
| 180 | Corona de flores | Cabeza (ya existe) |

- **«Te saluda con la cola» y «Saludo de buenos días» dejan de ser premios.** Si ya existen, quedan como comportamiento normal para todos desde el primer día.
- **Después de la corona:** hoy no hay más premios. La tarjeta dice «¡Mimu aprendió todo!». Las sorpresas (cada 50 corazones) se suman cuando haya contenido, en una actualización.
- **Quien ya tiene corazones** los conserva. Se recalcula con la tabla nueva y recibe de una vez lo que le corresponda, con **un solo** festejo que lo resuma (no uno por premio).
- Las cifras quedan en un solo lugar del código, para ajustarlas fácil.

## 2. Cómo se ganan los corazones

| Acción | Corazones | Tope |
| --- | --- | --- |
| Tocar «Listo» en un aviso | +1 | 10 por día (como hoy) |
| El primer «Listo» del día | +1 extra | 1 por día |
| Darle un mimo (tocarla) | +1 | 1 por día (como hoy) |
| Pasar a saludarla (abrir la app) | +1 | 1 por día, **nuevo** |

El día empieza a la medianoche local, como hoy.

## 3. Tarjeta en la pantalla Mascota (tablero «Camino · Tarjeta en Mascota»)

- **Sin números de nivel ni nombres de nivel** («Compinches», etc.). Se pueden mantener por dentro del código, pero no se muestran.
- **Si el próximo premio es un accesorio de cabeza o cuello**, se ve puesto sobre la mascota, transparente (40 %), con un cartelito «¿para mí?». Se va al ganarlo.
- **Título:**
  - si faltan 10 o menos: «Un poco más y (nombre) estrena (premio)», o «…aprende a (truco)»;
  - si faltan más: «Lo próximo: (premio)».
- **Barra de avance** en `blush` sobre `#f1e6d8`, con un corazón al final, más «Faltan N corazones» y el enlace «¿Cómo gano corazones?».
- **Tira con los 8 premios** (fichas de 34 dp): ganados sobre `sage-soft`, el próximo sobre `blush-soft` con borde `blush`, y los demás apagados (grises, al 30 %). Más «Ver todo».

## 4. Pantalla del camino (tablero «Camino · Todo lo que aprende»)

- Título «Lo que (nombre) aprende contigo» y debajo «Llevan N corazones juntos. Nunca se pierden.».
- **Grilla de 2 columnas** con los 8 premios, cada uno con dibujo, nombre y estado: «Ya lo tiene», «Lo próximo · faltan N» o «A los N corazones».
- **«Cómo gano corazones»**: las 4 formas de la tabla 2, cada una con su «+1».
- Al final: «Aunque pases días sin usar la app, (nombre) no se olvida de nada.»

## 5. Festejos

- **Accesorio o juguete** (tablero «Camino · Tarjeta en Mascota», al llegar a 95 con «Simular un Listo»):
  - una tarjeta con la mascota ya vestida, unos corazones alrededor, «¡Estreno gorrito!» y «Me lo gané con tus «Listo». Ya está en Vestir.»;
  - botones «Ahora no» y «Ponérselo».
  - Si la app está cerrada, sale la próxima vez que se abre.
- **Truco** (tablero «Camino · Aprendió un truco»):
  - en el momento de ganarlo, sobre la mascota flotante, un **papelito** (el mismo del aviso, con la esquina doblada) con «¡Aprendí a dar la patita!» y «Tócame 3 veces y te la doy»;
  - botones «¡A ver!» (hace el truco en el momento) y «Después».
  - Para bailar: «¡Aprendí a bailar!» y «A veces bailo cuando tocas Listo».

## 6. Dibujos

- En `docs/mimupet-cowork/para-code/camino/`:
  - `pelotita.svg` y `chapita.svg` (nuevos, 100 × 100);
  - las fichas recortadas `t-*.svg` de los 8 premios (los trucos usan íconos de patita y de nota musical, como en la maqueta).
- **Chapita en la mascota:** se ancla al cuello como el pañuelo (`anclajes.json`), centrada debajo del mentón y a un 45 % del ancho del pañuelo. Si en alguna especie queda mal, avisen en `para-cowork/` y la dibujo por especie.
- **Pelotita:** va al lado de la mascota, en el piso. No depende de la especie.

## Para revisar en la app

- «Adelantar todo el camino» ya se saca en el pedido del 07/11 (lanzamiento solo con Pro). Que no vuelva acá.
- Textos en español neutro, con voseo en Argentina y Uruguay, como siempre.

---

Cuando esté, anotá al final de este archivo `Estado: hecho en 1.0.xx`. Si hay dudas, dejalas en `docs/mimupet-cowork/para-cowork/`.
