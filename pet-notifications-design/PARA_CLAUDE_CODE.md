# Handoff de diseño — Pet Notifications

Esta carpeta es la referencia visual para implementar los estilos de notificación en la app Android.

## Qué hay
- `README.md`: guía completa (principios, niveles de aviso, voz y textos, color, tipografía, movimiento, accesibilidad). Leer primero.
- `tokens.json` / `tokens.css`: colores (tema claro y oscuro), tipografía, espacios, radios y sombras.
- `fonts/Nunito-Variable-latin.woff2`: fuente (licencia OFL).
- `componentes/<Nombre>/README.md`: especificación de cada estilo (medidas, colores, tiempos de animación, esfuerzo).
- `componentes/<Nombre>/preview.html`: maqueta animada de referencia (abrir en un navegador). Es HTML/CSS solo como referencia; NO se copia a la app.

## Cómo implementarlo
1. Leer `README.md` y el README de cada componente pedido.
2. Mirar el código actual de la app y usar su mismo stack y estructura: no agregar dependencias nuevas sin avisar.
3. Pasar los tokens al sistema de temas de la app (claro y oscuro), con los nombres de `tokens.json`.
4. Reemplazar el globo blanco actual y el estallido amarillo "¡Guau!" por los componentes pedidos.
5. El perro y el gato de las maquetas son dibujos de muestra: usar los sprites reales de la app. La onomatopeya sale de una tabla por especie (guau, miau, pío pío...).
6. Respetar tiempos y curvas de animación del README, y mostrar el estado final sin animación si el sistema tiene las animaciones desactivadas.
7. Hacerlo de a un componente, probarlo y recién después seguir con el siguiente.
