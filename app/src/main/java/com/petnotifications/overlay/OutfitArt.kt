package com.petnotifications.overlay

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import com.petnotifications.Accessory
import kotlin.math.abs

/** Una figura de un accesorio: trazado en el lienzo de 200×240, relleno y borde (ver gen_outfit.py). */
internal class ArtOp(d: String, fill: Long?, stroke: Long?, val width: Float, val alpha: Float) {
    val path: Path by lazy { PathParser().parsePathString(d).toPath() }
    val fill = fill?.let { Color(it) }
    val stroke = stroke?.let { Color(it) }
}

/** Un accesorio ya ubicado sobre una especie y una vista, en el lienzo de 200×240. */
internal class Art(val ops: List<ArtOp>, clip: String? = null, val clipped: Int = 0) {
    val top by lazy { ops.minOf { it.path.getBounds().top - it.width / 2 } }

    /** Los agujeros del casco, por donde se ve la cara: recorta las primeras [clipped] figuras. */
    val clip: Path? by lazy { clip?.let { PathParser().parsePathString(it).toPath() } }
}

/**
 * Los accesorios del vestidor: el arte de Cowork para cada especie, de frente y de perfil (OutfitArtData).
 * Está dibujado sobre las láminas de docs/referencia-mascotas, así que se corre lo que la mascota se aparte
 * de ellas (al girar, sentada, echada). El borde queda siempre del mismo grosor que el de la mascota.
 */
internal object OutfitArt {
    private val headFiles = mapOf(
        Accessory.HAT to "gorrito_lana",
        Accessory.XMAS_HAT to "gorro_navideno",
        Accessory.CROWN to "corona_flores",
        Accessory.PUMPKIN to "calabacita",
        Accessory.PUMPKIN_HELMET to "casco_calabaza",
    )

    /** Margen arriba del lienzo: lo que se pasa se recorta, así que el accesorio se achata desde su base. */
    private const val TOP_MARGIN = 1f
    private const val BASE_BELOW_CROWN = 30f

    private fun DrawScope.paint(art: Art, k: Float) {
        val clip = art.clip
        if (clip == null) {
            paint(art.ops, k)
            return
        }
        clipPath(clip) { paint(art.ops.take(art.clipped), k) }
        paint(art.ops.drop(art.clipped), k)
    }

    private fun DrawScope.paint(ops: List<ArtOp>, k: Float) {
        for (o in ops) {
            o.fill?.let { drawPath(o.path, it, alpha = o.alpha) }
            o.stroke?.let {
                drawPath(o.path, it, alpha = o.alpha, style = Stroke(o.width / k, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }

    /**
     * Lo de la cabeza de [pet]: el punto (refX, refY) de la lámina va a ([x], [y]), escalado [k]. [crownY]:
     * coronilla de la mascota. [c] y [s]: coseno y seno del giro de la cabeza; de perfil usa su dibujo
     * (espejado si mira a la izquierda) y de espaldas el de frente. Con la cara hacia atrás, el casco va sin agujeros.
     */
    fun head(
        ds: DrawScope, pet: String, a: Accessory, x: Double, y: Double, refX: Double, refY: Double, k: Double,
        crownY: Double, c: Double, s: Double,
    ) = with(ds) {
        val side = abs(s) > abs(c)
        val file = headFiles[a] ?: return@with
        val art = (if (c < 0) OutfitArtData.get(pet, file, if (side) "perfil_atras" else "espalda") else null)
            ?: OutfitArtData.get(pet, file, if (side) "perfil" else "frente") ?: return@with
        val kf = k.toFloat()
        val artTop = y.toFloat() + (art.top - refY.toFloat()) * kf
        val base = crownY.toFloat() + BASE_BELOW_CROWN * kf
        val squash = if (artTop < TOP_MARGIN) (base - TOP_MARGIN) / (base - artTop) else 1f
        scale(1f, squash, pivot = Offset(0f, base)) {
            translate(x.toFloat(), y.toFloat()) {
                scale(if (side && s < 0) -kf else kf, kf, pivot = Offset.Zero) {
                    translate(-refX.toFloat(), -refY.toFloat()) { paint(art, kf) }
                }
            }
        }
    }

    /**
     * Pañuelo de [pet]: de frente, de perfil (espejado si mira a la izquierda) o solo el nudo. El punto
     * (refX, refY) de la lámina va a ([x], [y]). [c] y [s]: coseno y seno del giro del cuerpo.
     */
    fun scarf(ds: DrawScope, pet: String, x: Double, y: Double, refX: Double, refY: Double, c: Double, s: Double) = with(ds) {
        val side = abs(s) > abs(c)
        val view = if (side) "perfil" else if (c < 0) "espalda" else "frente"
        val art = OutfitArtData.get(pet, "panuelo", view) ?: return@with
        translate(x.toFloat(), y.toFloat()) {
            scale(if (side && s < 0) -1f else 1f, 1f, pivot = Offset.Zero) {
                translate(-refX.toFloat(), -refY.toFloat()) { paint(art.ops, 1f) }
            }
        }
    }

    /**
     * Cuello de cada especie en la lámina (docs/accesorios-especies/anclajes.json): x, y y ancho, de frente y de
     * perfil. Ahí va la chapita.
     */
    private class Neck(val fx: Float, val fy: Float, val fw: Float, val sx: Float, val sy: Float, val sw: Float)

    private val necks = mapOf(
        "DOG" to Neck(100f, 151.1f, 63.3f, 103.7f, 150.1f, 51.8f),
        "CAT" to Neck(100f, 151.1f, 63.3f, 103.7f, 150.1f, 51.8f),
        "DUCK" to Neck(100f, 150.8f, 62.7f, 108.3f, 148.7f, 55.1f),
        "FROG" to Neck(100f, 153.5f, 61.4f, 104.3f, 151.7f, 55.7f),
        "COW" to Neck(100f, 151.1f, 63.3f, 103.9f, 149f, 46.8f),
        "PIG" to Neck(100f, 151.1f, 63.3f, 103.7f, 150.1f, 51.8f),
        "LION" to Neck(100f, 146.4f, 52.4f, 109f, 146f, 21.1f),
        "MONKEY" to Neck(100f, 149.3f, 59.7f, 103.9f, 149f, 46.8f),
        "RABBIT" to Neck(100f, 155.7f, 71f, 100.5f, 153.5f, 65.9f),
        "CHICK" to Neck(100f, 165.6f, 123.4f, 100f, 165.6f, 119.8f),
        "OWL" to Neck(100f, 164.4f, 127.1f, 100f, 164.4f, 116.2f),
    )

    /** Ancho de cuello más grande que se usa para la chapita: el pollito y el búho no tienen cuello y quedaría enorme. */
    private const val TAG_MAX_NECK = 66f

    /**
     * Chapita con su nombre (Cowork: camino/chapita.svg): un cordón marrón y una medalla dorada con un
     * corazón, centrada debajo del mentón; la medalla mide el 45 % del ancho del pañuelo. Va donde el
     * pañuelo (es una opción en su lugar). De perfil la medalla queda adelante; de espaldas no se ve.
     */
    fun tag(ds: DrawScope, pet: String, x: Double, y: Double, refX: Double, refY: Double, c: Double, s: Double) = with(ds) {
        val side = abs(s) > abs(c)
        if (!side && c < 0) return@with
        val n = necks[pet] ?: return@with
        val w = (if (side) n.sw else n.fw).coerceAtMost(TAG_MAX_NECK).coerceAtLeast(40f)
        val nx = if (side) n.sx else n.fx
        val ny = if (side) n.sy else n.fy
        val r = w * 0.45f / 2
        // De perfil la medalla cuelga del lado de la cara (adelante); el cordón da la vuelta por atrás.
        val discX = if (side) nx + w * 0.28f else nx
        val discY = ny + 6f + r * 1.25f
        translate(x.toFloat(), y.toFloat()) {
            scale(if (side && s < 0) -1f else 1f, 1f, pivot = Offset.Zero) {
                translate(-refX.toFloat(), -refY.toFloat()) {
                    val cord = Path().apply {
                        if (side) {
                            moveTo(nx - w * 0.42f, ny - 4f)
                            quadraticTo(nx - w * 0.05f, ny + 8f, discX, ny + 6f)
                        } else {
                            moveTo(nx - w * 0.4f, ny - 3f)
                            quadraticTo(nx, ny + 12f, nx + w * 0.4f, ny - 3f)
                        }
                    }
                    drawPath(cord, INK, style = Stroke(9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawPath(cord, TAG_CORD, style = Stroke(5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    drawLine(INK, Offset(discX, ny + 4f), Offset(discX, discY - r), strokeWidth = 4f, cap = StrokeCap.Round)
                    drawCircle(TAG_GOLD, r, Offset(discX, discY))
                    drawCircle(INK, r, Offset(discX, discY), style = Stroke(4f))
                    // El corazón de la medalla (el del dibujo de Cowork, escalado al disco).
                    val k = r / 24f
                    val heart = Path().apply {
                        fun p(px: Float, py: Float) = Offset(discX + (px - 50f) * k, discY + (py - 64f) * k)
                        p(50f, 74f).let { moveTo(it.x, it.y) }
                        p(40f, 64f).let { lineTo(it.x, it.y) }
                        val a = p(35f, 58f); val b = p(42f, 50f); val e = p(50f, 57f)
                        cubicTo(a.x, a.y, b.x, b.y, e.x, e.y)
                        val a2 = p(58f, 50f); val b2 = p(65f, 58f); val e2 = p(60f, 64f)
                        cubicTo(a2.x, a2.y, b2.x, b2.y, e2.x, e2.y)
                        close()
                    }
                    drawPath(heart, TAG_HEART)
                    drawPath(heart, INK, style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
    }

    /**
     * Pelotita (Cowork: camino/pelotita.svg), en el piso al lado de la mascota, en el lienzo de 200×240.
     * [roll]: cuánto rodó (en unidades del lienzo, negativo hacia la mascota); gira con lo que rueda.
     * [lift]: cuánto está en el aire (rebote); la sombrita queda en el piso y se achica.
     */
    fun ball(ds: DrawScope, roll: Float = 0f, lift: Float = 0f) = with(ds) {
        val r = BALL_R
        val cx = BALL_X + roll
        val cy = FLOOR - r - lift
        val sh = 1f - (lift / 60f).coerceIn(0f, 0.5f)
        drawOval(INK.copy(alpha = 0.14f * sh), Offset(cx - r * 0.9f * sh, FLOOR - 3f), Size(r * 1.8f * sh, 6f))
        drawCircle(BALL_PINK, r, Offset(cx, cy))
        rotate(roll / r * 57.3f, Offset(cx, cy)) {
            val band = Path().apply {
                moveTo(cx - r * 0.95f, cy - r * 0.2f)
                quadraticTo(cx, cy + r * 0.75f, cx + r * 0.95f, cy - r * 0.2f)
            }
            drawPath(band, BALL_CREAM, style = Stroke(5f, cap = StrokeCap.Round))
            val seam = Path().apply {
                moveTo(cx - r * 0.95f, cy - r * 0.42f)
                quadraticTo(cx, cy + r * 0.05f, cx + r * 0.95f, cy - r * 0.42f)
            }
            drawPath(seam, INK, style = Stroke(3f, cap = StrokeCap.Round))
        }
        drawCircle(INK, r, Offset(cx, cy), style = Stroke(4f))
        drawCircle(BALL_CREAM.copy(alpha = 0.8f), r * 0.18f, Offset(cx - r * 0.37f, cy - r * 0.47f))
    }

    /** Camita, detrás de todo: base verde, almohadón crema y la sombra del borde. */
    fun bed(ds: DrawScope, cx: Double, rx: Double) = with(ds) {
        val x = cx.toFloat()
        val r = rx.toFloat()
        val line = Stroke(4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun oval(y: Float, w: Float, h: Float, fill: Color) {
            val tl = Offset(x - w, y - h)
            val sz = Size(w * 2, h * 2)
            drawOval(fill, tl, sz)
            drawOval(INK, tl, sz, style = line)
        }
        oval(BED_Y, r, 20f, BED_GREEN)
        oval(BED_Y - 6f, r - 20f, 12f, BED_CREAM)
        drawPath(
            Path().apply {
                moveTo(x - r, BED_Y)
                quadraticTo(x, BED_Y + 26f, x + r, BED_Y)
            },
            BED_SHADE, alpha = 0.6f, style = Stroke(5f, cap = StrokeCap.Round),
        )
    }

    private val INK = Color(PetModel.INK)
    private val BED_GREEN = Color(0xFF8FC1A0)
    private val BED_SHADE = Color(0xFF74A886)
    private val BED_CREAM = Color(0xFFFFF4E2)
    private const val BED_Y = 216f

    private val TAG_CORD = Color(0xFFC98A55)
    private val TAG_GOLD = Color(0xFFF7C873)
    private val TAG_HEART = Color(0xFFFF9EAE)
    private val BALL_PINK = Color(0xFFFF9EAE)
    private val BALL_CREAM = Color(0xFFFFF4E2)
    private const val BALL_R = 17f
    private const val BALL_X = 206f
    private const val FLOOR = 228f
}
