# Guía para lanzar MimuPet: lo que hacés vos

Son pasos en páginas de Google y Cloudflare, que Claude Code no puede hacer por vos. Los nombres de los menús pueden variar un poco: si alguno no aparece, buscá la palabra parecida.

## 1. Publicar la web en Cloudflare Pages (gratis)

Sirve para que la web y la **política de privacidad** tengan una dirección pública. Google Play exige esa dirección.

1. **Cuenta:** entrá a <https://dash.cloudflare.com/sign-up> y creá una cuenta con tu correo. No pide tarjeta.
2. **Proyecto:** en el menú de la izquierda, entrá a **Workers & Pages** y tocá **Create** (Crear). Elegí **Pages** y después **Upload assets** (Subir archivos).
3. **Nombre:** poné `mimupet`. La dirección queda `https://mimupet.pages.dev`. Si ese nombre está ocupado, Cloudflare propone otro.
4. **Archivos:** arrastrá la carpeta `web` entera, desde `C:\Users\ariel\Desktop\pet-notifications\web`, y tocá **Deploy** (Publicar).
5. **Listo:** en un minuto quedan publicadas estas páginas:
   - la web: `https://mimupet.pages.dev`
   - la política en español: `https://mimupet.pages.dev/privacidad.html`
   - la política en inglés: `https://mimupet.pages.dev/privacy.html`

**Para actualizarla** (cuando cambies `web/datos.js`, por ejemplo): entrá al proyecto, tocá **Create deployment** (Nueva publicación) y volvé a arrastrar la carpeta `web`.

**Antes de publicar**, conviene cargar `contacto` en `web/datos.js`, el correo que aparece en la política. Sin ese dato, la política remite al correo de la ficha de Play.

**Más adelante, si querés:**

- **Dominio propio:** `mimupet.app` o `mimupet.com`, unos USD 10 a 20 por año, se conecta desde **Custom domains**.
- **Publicación automática:** Cloudflare puede publicar cada vez que se sube algo a GitHub, en la rama `main`, con la carpeta `web`. Claude Code te ayuda a configurarlo.

## 2. Ficha de Google Play en español neutro (es-419 y es-ES)

La ficha es lo que la gente ve en la tienda: título, descripción corta, descripción larga y capturas.

1. En la Play Console, entrá a **Crecimiento › Presencia en Play Store › Ficha principal de Play Store**.
2. En **Administrar traducciones**, agregá **Español (Latinoamérica) – es-419** y **Español (España) – es-ES**.
3. Pegá en cada una el texto neutro de Cowork. Está en «Textos para la tienda y frases»: <https://claude.ai/artifact/XhKHMSp3EqBsNwnSRy3GdB>.
4. **Corregir antes de pegar:**
   - La parte de los gestos dice que dos toques la hacen saltar y tres la hacen dar la vuelta. Lo correcto es: un toque, salta y tira corazones; dos toques, da una vuelta.
   - Pro trae **7 mascotas**, no 10 ni 11.
   - En neutro es **cerdito**, no chancho.
5. **Opcional:** cargá también la ficha en voseo para Argentina. Play no permite una ficha distinta por país dentro del español, así que la de Latinoamérica la ve también Argentina. Por eso conviene el neutro.

Si querés, Claude Code te prepara los textos corregidos para copiar y pegar.

## 3. Política de privacidad y seguridad de los datos en Play

1. **Política:** entrá a **Política › Contenido de la app › Política de privacidad** y pegá `https://mimupet.pages.dev/privacidad.html`. Antes tenés que haber hecho el paso 1.
2. **Seguridad de los datos:** está en la misma sección. Es un cuestionario sobre qué datos junta la app. Las respuestas, según lo que hace MimuPet:
   - **¿Recopila o comparte datos?** No. La app no tiene acceso a internet: recordatorios, nombre de la mascota y calendario quedan en el celular.
   - **Compras:** las hace Google Play, no la app. Google las declara por su lado.
   - **«Enviar comentarios»:** abre tu app de correo, y la persona decide si lo manda. La app no envía nada sola.
   - **Copia de seguridad:** es la de Android, en la cuenta de Google del usuario. No llega a nosotros.

Confirmá cada respuesta con la ayuda de Google que aparece al lado de cada pregunta. Si alguna no está clara, mandale una captura a Claude Code.

## 4. Productos de compra en Play

**Para el lanzamiento se carga un solo producto: Pro** (decisión del 07/11). Se crea una sola vez en **Monetizar › Productos › Productos integrados en la aplicación › Crear producto**. El **ID tiene que ser exactamente este**: la app lo busca por ese nombre y después no se puede cambiar.

| ID | Qué es | Precio |
| --- | --- | --- |
| `pro_lifetime` | MimuPet Pro, pago único | USD 2,49 de lanzamiento, USD 3,99 de base, por país |

Después de crearlo, tocá **Activar**.

**Más adelante, con una actualización** (no hace falta cargarlos ahora; PENDIENTES X13, X16 y X18):

| ID | Qué es | Precio |
| --- | --- | --- |
| `acc_xmas_hat`, `acc_pumpkin`, `acc_pumpkin_helmet` | Accesorios de temporada | ≈ USD 2 en Latinoamérica, ≈ USD 4 en EE. UU. y Europa |
| `coats_dog`, `coats_cat`, `coats_duck`, `coats_cow`, `coats_pig`, `coats_chick`, `coats_rabbit` | Pack de pelajes de cada mascota | USD 0,99, por país |
| (a definir) | Pack de animales nuevos | USD 1,49 |
