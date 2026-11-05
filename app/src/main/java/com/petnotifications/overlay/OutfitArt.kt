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
}
