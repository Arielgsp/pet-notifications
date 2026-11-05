# Español neutro y voseo en la app — para Claude Code

Decisión de Ariel: la app es internacional, así que el español por defecto pasa a ser **tuteo neutro** («¿Qué quieres borrar?»). El **voseo** actual («¿Qué querés borrar?») se mantiene solo para Argentina y Uruguay. Android elige la carpeta según el país del celular, así que el usuario no tiene que configurar nada.

## 1. Estructura de recursos

| Carpeta | Para quién | Contenido |
| --- | --- | --- |
| `values/` | Inglés y cualquier idioma sin traducción | Sin cambios |
| `values-es/` | Todos los países hispanohablantes | **Tuteo neutro** (hay que convertirlo) |
| `values-es-rAR/` | Celulares con región Argentina | Voseo: **solo los strings que cambian** respecto de `values-es` |
| `values-es-rUY/` | Uruguay | Igual que `values-es-rAR` |

- Android busca cada string por separado: si no lo encuentra en `values-es-rAR`, lo toma de `values-es`. Por eso las carpetas regionales llevan solo las diferencias, y el resto se mantiene en un solo lugar.
- Paraguay usa voseo y tuteo según la zona. Que use `values-es` hasta que haya usuarios que pidan otra cosa.
- Lo práctico: copiar el `values-es/strings.xml` actual (en voseo) como base de `values-es-rAR`, convertir `values-es` a tuteo y, al final, borrar de `values-es-rAR` los strings que quedaron iguales en las dos versiones. Conviene un script chico en `tools/i18n/` que haga esa comparación, para que no se desincronicen cuando se agreguen textos.

## 2. Reglas de conversión

**Imperativos.** Ojo con las tildes cuando lleva pronombre pegado.

| Voseo | Tuteo |
| --- | --- |
| Tocá | Toca |
| Tocame | Tócame |
| Elegí | Elige |
| Elegilas | Elígelas |
| Mantené apretado | Mantén presionado |
| Probá | Prueba |
| Activá | Activa |
| Apagá | Apaga |
| Revisá | Revisa |
| Sacá | Saca |
| Agregala | Agrégala |
| Seguí | Sigue |
| Escribí | Escribe |
| Arrastrala | Arrástrala |
| Arrastrame | Arrástrame |
| Buscame | Búscame |
| Ponele | Ponle |
| Cambiá | Cambia |

**Presente.**

| Voseo | Tuteo |
| --- | --- |
| tenés | tienes |
| querés | quieres |
| podés | puedes |
| decís | dices |
| seguís | sigues |
| mirás | miras |
| jugás | juegas |
| tocás | tocas |
| respondés | respondes |
| cerrás | cierras |
| desbloqueás | desbloqueas |
| prendés | enciendes |
| creás | creas |
| mantenés | mantienes |
| tirás | tiras |
| anotás | anotas |
| abrís | abres |
| sos | eres |

**Otras palabras.**

| Voseo / regional | Neutro |
| --- | --- |
| con vos | contigo |
| para vos | para ti |
| acá | aquí |
| prender la pantalla | encender la pantalla |
| apretar / mantener apretado | presionar / mantener presionado |
| la cortina (de notificaciones) | el panel de notificaciones |
| saltear | saltar |
| dale | de acuerdo |

**Lo que se mantiene igual:**
- «celular»: es lo habitual en Latinoamérica. Si más adelante hay versión para España (`values-es-rES`), ahí va «móvil».
- Las onomatopeyas y los nombres de las mascotas.
- Los placeholders (`%1$s`, `%1$d`) y los saltos de línea `\n`.

## 3. Ejemplos del archivo actual

Estos salen de la copia 1.0.33; en tu versión puede haber más.

| String | Voseo (es-rAR) | Tuteo (es) |
| --- | --- | --- |
| `notif_pet_text` | Tocá para configurar | Toca para configurar |
| `section_pet` | Elegí tu mascota | Elige tu mascota |
| `insist_title` | Insistir si no respondés | Insistir si no respondes |
| `hide_fullscreen_help` | Cuando mirás un video o jugás a pantalla completa… | Cuando miras un video o juegas a pantalla completa… |
| `delete_repeat_title` | ¿Qué querés borrar? | ¿Qué quieres borrar? |
| `pro_status_owned` | Tenés Pro. ¡Gracias por apoyar la app! | Tienes Pro. ¡Gracias por apoyar la app! |
| `trial_notice_body` | Yo me quedo con vos igual… | Yo me quedo contigo igual… |
| `ob_done_body` | Tocame, arrastrame o tocá «Probar un aviso»… | Tócame, arrástrame o toca «Probar un aviso»… |
| `name_body` | Ponele el nombre que quieras. | Ponle el nombre que quieras. |
| `tip_7` | Si me mantenés apretada, abrís la app. | Si me mantienes presionada, abres la app. |

Al revisar, ojo también con textos viejos que quedaron desactualizados. Por ejemplo, `free_pets_body` dice «Con Pro tenés las 10», y ahora son 7 en el lanzamiento más el pack Amigos salvajes.

## 4. Google Play

En la Play Console, cargá la ficha en **es-419 (Latinoamérica)** y en **es-ES** con el texto neutro. Si querés una para Argentina, Play no tiene ficha por país en español, así que la neutra sirve para todos.

## 5. Cómo probarlo

1. Celular o emulador en Español (México): tiene que aparecer «Toca», «quieres».
2. En Español (Argentina): tiene que aparecer «Tocá», «querés».
3. Revisá que ningún texto quede cortado: algunas formas del tuteo son más largas («Mantén presionado»).

---

Estado: hecho en 1.0.60 (`tools/i18n/tuteo.py`, `values-es` neutro, `values-es-rAR` y `values-es-rUY` con voseo). «Con Pro tenés las 10/11» corregido a «las 7» en 1.0.62, y «Chancho» pasa a «Cerdito» en el neutro. Falta probar en el Xiaomi (PRUEBAS 69). Anotado por Code el 05/11/2026.
