# Accesorios para las 11 especies, de frente y de perfil

Dibujos para MimuPet en el mismo lienzo de gen.py: 200 × 240 u, con el origen arriba a la izquierda. Cada SVG va encima de la mascota tal cual, sin moverlo ni escalarlo.

## Carpetas

- `<especie>/frente/` y `<especie>/perfil/` tienen 5 archivos cada una: panuelo, gorro_navideno, gorrito_lana, calabacita y corona_flores.
- `camita-frente.svg` y `camita-perfil.svg` son la camita, igual para todas las especies. Va detrás de todo.
- `anclajes.json` trae la transformación que se aplicó por especie y vista. Sirve si preferís ubicar por código un solo dibujo base, el del perro:
  - cabeza (gorros y calabacita);
  - cuello (pañuelo).
- `vista-previa-frente.png` y `vista-previa-perfil.png` muestran todas las combinaciones.

## Cómo se ubicaron

- **Gorros y calabacita.** La elipse de la cabeza del perro se lleva a la elipse de cada especie. La escala horizontal es el ancho de la cabeza. La vertical se limita al 88–108 % de la horizontal para que el gorro no se deforme.
- **Pañuelo.** Se usa el ancho del cuello, que es la cuerda de la cabeza a la altura del cuello que marcaste. De perfil, la recta del cuello tiene la misma inclinación que en el perro.
- **Corona de flores.** No se escala: se arma de nuevo sobre el borde de arriba de cada cabeza.

## Casos especiales

| Especie | Qué se hizo |
| --- | --- |
| León | Los gorros se apoyan sobre la melena, no sobre el óvalo de la cara. Frente: (100, 96), 148 × 144. Perfil: (110, 96), 132 × 132 |
| Rana | Los ojos asoman sobre la cabeza. Los gorros van chicos: de frente entre los ojos (escala 0,46) y de perfil detrás del ojo, inclinados −14°. La corona pasa por arriba de los ojos |
| Conejo | Los gorros tapan la base de las orejas y las orejas asoman por arriba. Se dibujó sobre el modelo 2,5D (0° y 90°) |
| Pollito | **Los accesorios de cabeza reemplazan la cáscara**: con gorro, corona o calabacita no se dibuja la cáscara (así está en la vista previa) |
| Pollito y búho | Sin cuello: el pañuelo va sobre la línea marcada, del ancho del cuerpo y un poco más corto (alto al 86 %) |
| Vaca y pato | **El pañuelo reemplaza la campana de la vaca y el moñito del pato**: con pañuelo puesto, no dibujar lo que traen en el cuello |

## Capas

- Pañuelo, gorros, calabacita y corona: encima de la mascota.
- Camita: debajo de todo, solo echada o dormida.

## Ojo

- De perfil, las láminas son de los dibujos fijos. El modelo que gira lleva la cabeza un poco más atrás (en el perro, x = 106 en lugar de 112). Si en la app el gorro queda corrido, mover el anclaje de cabeza la misma diferencia.
- Las vistas previas de gato, chancho, león, búho (frente y perfil) y conejo (perfil) se dibujaron con la versión de gen.py y del modelo 2,5D que hay en la carpeta Pet_Notifications de la PC del trabajo (1.0.33). Los anclajes son los de tus láminas.

## Casco de calabaza (disfraz de Halloween)

Es un ítem aparte de la calabacita, no la reemplaza. El archivo es `casco_calabaza.svg` en `<especie>/frente/` y `<especie>/perfil/`. La vista previa es `vista-previa-casco-calabaza.png`.

- **Qué es:** una calabaza achatada que cubre toda la cabeza. Tiene ojos triangulares con puntas redondeadas, nariz triangular (menos en las especies con pico u hocico grande) y una sonrisa sin dientes. Los bordes de los agujeros son amarillos, como si adentro hubiera una vela.
- **Agujeros:** por cada agujero se ve la cara de la mascota. El parpadeo y la cara contenta siguen funcionando. Cada agujero está ubicado sobre los ojos y la boca de esa especie.
- **De perfil:** tiene un solo ojo. El hocico o el pico asoma por adelante.
- **Capa:** va encima de todo. Las orejas, cuernos y orejas de búho que sobresalen quedan a la vista.
- **Cómo se recorta:** con `clipPath`, no con `mask`, para que se pueda pasar a VectorDrawable. El recorte es un rectángulo antihorario más los agujeros en sentido horario, con regla nonzero: en Android va como `<clip-path>` dentro de un `<group>`. Cada archivo tiene su propio id (`casco-<especie>-<vista>`).
- **Al girar:** los agujeros están fijos para frente y perfil. Para los ángulos intermedios del modelo que gira, usá el más cercano o mové los agujeros junto con la cara.
- **Para dormir:** no se ve.
