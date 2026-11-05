/*
 * Lo único que hay que tocar para actualizar la página. Pegá cada enlace entre las comillas y guardá:
 * mientras un enlace esté vacío, la página muestra "Muy pronto" en su lugar.
 */
window.MIMUPET = {
  // Enlace de la app en Google Play.
  play: "",

  // Correo de contacto. Aparece en la política de privacidad.
  contacto: "",

  // Las redes. Al pegar un enlace, el botón pasa a llevar a esa red.
  redes: {
    tiktok: "",
    instagram: "",
    youtube: "",
  },

  // Los videos, en orden. Para publicar uno, pegá su enlace en "enlace" y aparece el botón para verlo.
  // Para sumar otro, copiá una línea entera. "texto_en" es la frase en inglés. "mascota" puede ser: dog, cat, rabbit, duck, frog, cow, pig, chick, lion, monkey, owl.
  videos: [
    { texto: "Cuando ignoras todas las notificaciones menos esta", texto_en: "When you ignore every notification but this one", mascota: "dog", enlace: "" },
    { texto: "¿Cuál sería la tuya?", texto_en: "Which one would be yours?", mascota: "rabbit", enlace: "" },
    { texto: "Mi recordatorio tiene más estilo que yo", texto_en: "My reminder has more style than I do", mascota: "cat", enlace: "" },
    { texto: "Se duerme cuando no la usas", texto_en: "It falls asleep when you’re not using it", mascota: "owl", enlace: "" },
    { texto: "Cada cosa que haces, se hace más amiga", texto_en: "Everything you do makes you better friends", mascota: "frog", enlace: "" },
    { texto: "Sin internet. Sin cuentas. Tus datos no salen del celular", texto_en: "No internet. No accounts. Your data stays on your phone", mascota: "lion", enlace: "" },
  ],
};
