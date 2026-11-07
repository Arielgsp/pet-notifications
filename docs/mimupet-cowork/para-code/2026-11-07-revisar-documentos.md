# Pedido de Cowork · 07/11/2026 · Poner al día los documentos de Code

Lo pidió Ariel: que no queden documentos con datos viejos que confundan más adelante. Cowork ya actualizó el Design System, la tienda, la prensa, el plan de videos y esta carpeta. Falta lo tuyo.

**Revisar y corregir** (o marcar arriba de todo «Histórico: no vigente», si es un informe de una versión vieja):

- `README.md` (raíz), `docs/README-tecnico.md`, `docs/resumen-app.md`, `docs/contexto-interfaz-app.md`, `docs/guia-de-estilo.md`
- `docs/informe-para-cowork.md`, `docs/informe-cowork-1.0.33.md`, `docs/informe-estetica.md`, `docs/preguntas-vinculo-para-cowork.md`: probablemente históricos
- `docs/pendientes-recomendaciones.html` y `docs/Pet-Notifications-pendientes.pdf`: si ya no se usan, marcarlos o moverlos a una carpeta de históricos
- `docs/notas-claude/` (por ejemplo, `billing-decision.md` y `pet-notifications-project.md`)
- `docs/PENDIENTES.md`: las decisiones viejas que ya no valen (D18 pelajes Pro, D23 globo con mensaje, niveles con nombre, «14 días»), marcarlas como reemplazadas
- `web/datos.js` y los textos de la web

**Qué buscar:**

| Viejo | Vigente |
| --- | --- |
| 11 mascotas, «las 10», siluetas, «Pronto» | 7 mascotas; el pack es sorpresa y no se menciona |
| Pelajes en Pro o en packs a la venta | No hay pelajes en el lanzamiento (escondidos) |
| Accesorios a la venta, precios de accesorios | En el lanzamiento no se vende nada salvo Pro |
| US$4,99 / 5,99 | USD 2,49 de lanzamiento y 3,99 de base |
| Prueba de 14 días | 7 días |
| Niveles 1–8 con nombre («Compinches»), «Nivel N», «Faltan N Listo para el nivel» | Camino con 8 premios por corazones, sin niveles a la vista |
| «Adelantar todo el camino» | Sacado |
| Globito con el mensaje, tarjeta Asomado | Papelito en la patita → cartita |
| Diálogo «Recordatorio rápido» de Android | Cartita de papel con horas |
| 1 toque corazones, 2 saltar, 3 vuelta, «Tocame 3 veces» | 1 toque salta con corazones; 2 toques, vuelta (intercalada con la patita); 3 libre |
| Voseo por defecto | Tuteo neutro; voseo solo en Argentina y Uruguay |
| «Chancho» en neutro | «Cerdito» (chancho solo en Argentina y Uruguay) |
| «Pet Notifications» como nombre de la app | MimuPet (el nombre del paquete y del repositorio pueden quedar) |

El resumen de lo vigente está en `docs/mimupet-cowork/LEEME-traspaso.md`.

---

Cuando esté, anotá al final de este archivo `Estado: hecho` con la lista de lo que cambiaste o marcaste. Si hay dudas, dejalas en `docs/mimupet-cowork/para-cowork/`.

---

Estado: hecho (07/11/2026, sin versión nueva de la app). Dudas en `para-cowork/2026-11-07-documentos-al-dia.md`. Los pedidos «lanzamiento solo con Pro» y «camino rehecho» todavía no están en la app: los documentos los dan como decididos y aclaran que faltan.

- **Corregidos:**
  - `README.md`:
    - 7 mascotas y MimuPet;
    - aviso con globo, papelito y cartita;
    - tirones y recordatorio rápido;
    - camino;
    - gratis y Pro con 7 días, Caramelo en Pro y solo Pro a la venta;
    - neutro con voseo en Argentina y Uruguay;
    - Listo y Más tarde.
  - `docs/resumen-app.md`: reescrito al día.
  - `docs/GUIA-LANZAMIENTO.md`: para el lanzamiento solo se carga `pro_lifetime`; los demás productos quedan para después.
  - `docs/PENDIENTES.md`:
    - nota arriba de todo: «lo vigente manda», con lo decidido el 07/11;
    - marcadas como reemplazadas C3 (14 días), D12, D15 (niveles), D18 (pelajes en Pro), D23 (globito y tarjeta Asomado) y los pelajes en packs;
    - D20 aclarada;
    - X13 y X18 pasan a después del lanzamiento.
  - `web/index.html`, `web/datos.js` y `tools/web_assets.py`:
    - sin niveles, pelajes ni accesorios a la venta;
    - Caramelo en Pro;
    - dibujos con ropa del camino;
    - sin dibujos ni nombres de los animales del pack.
- **Marcados «Histórico: no vigente»:**
  - `docs/README-tecnico.md`
  - `docs/contexto-interfaz-app.md`
  - `docs/guia-de-estilo.md`
  - `docs/informe-para-cowork.md`
  - `docs/informe-cowork-1.0.33.md`
  - `docs/informe-estetica.md`
  - `docs/preguntas-vinculo-para-cowork.md`
- **Movidos a `docs/historico/`:**
  - `pendientes-recomendaciones.html`
  - `Pet-Notifications-pendientes.pdf`
  - Se agregó un `LEEME.md` en esa carpeta.
- **`docs/notas-claude/`:**
  - Copia al día de la memoria de Claude Code.
  - `pet-notifications-project.md` quedó marcado como histórico.
  - `shop-decisions.md` dice que solo se vende Pro.
  - `billing-decision.md` sigue vigente: se corrigió el modo fantasma, que es gratis.
  - Se sumaron `home-pc-setup.md`, `launch-pets-and-cowork-channel.md` y `shop-decisions.md`.
