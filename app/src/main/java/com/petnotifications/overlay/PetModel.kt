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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import com.petnotifications.PetType
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

/**
 * Mascotas "2,5D" para el giro continuo: cada parte (ojos, hocico, orejas, patas, cola) sabe en qué
 * ángulo está alrededor de la cabeza o del cuerpo, así la mascota se puede dibujar en cualquier ángulo
 * y la vuelta es pareja, sin cambiar de dibujo de golpe. Mismas formas y colores que los dibujos fijos
 * (tools/pet_art/gen.py), en el mismo lienzo de 200×240 centrado en el de 256×240.
 *
 * Solo se dibuja así mientras gira; quieta usa los dibujos fijos.
 */
object PetModel {

    /** Mascotas que ya tienen modelo (las demás siguen girando con los 4 dibujos). */
    fun has(pet: PetType) = pet == PetType.DOG

    private val Ink = Color(0xFF4A3228)
    private val BlushC = Color(0xFFFF9EAE)
    private val Tongue = Color(0xFFFF8FA3)

    private class Ctx(val ds: DrawScope) {
        val outline = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        /** Óvalo relleno con borde (como los del dibujo), rotado [rot] grados sobre su centro. */
        fun ell(cx: Float, cy: Float, rx: Float, ry: Float, fill: Color, rot: Float = 0f, stroke: Boolean = true, alpha: Float = 1f) {
            if (rx <= 0.01f || ry <= 0.01f) return
            ds.rotate(rot, Offset(cx, cy)) {
                val tl = Offset(cx - rx, cy - ry)
                val sz = Size(rx * 2, ry * 2)
                drawOval(fill, tl, sz, alpha = alpha)
                if (stroke) drawOval(Ink, tl, sz, style = outline)
            }
        }

        fun path(p: Path, fill: Color?, stroke: Boolean = true, width: Float = 4f) {
            if (fill != null) ds.drawPath(p, fill)
            if (stroke) ds.drawPath(p, Ink, style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }

    /**
     * Dibuja la mascota girada [theta] grados (0 de frente, 90 mirando a la derecha, 180 de espaldas)
     * ocupando todo el [DrawScope] (proporción 256×240, igual que los dibujos fijos).
     */
    fun draw(ds: DrawScope, pet: PetType, theta: Float, happy: Boolean) {
        val k = ds.size.width / 256f
        ds.scale(k, k, pivot = Offset.Zero) {
            translate(left = 28f) {
                when (pet) {
                    else -> dog(Ctx(this), theta, happy)
                }
            }
        }
    }

    private fun rad(d: Float) = Math.toRadians(d.toDouble()).toFloat()

    // ---------------------------------------------------------------- perro

    private val DogMain = Color(0xFFF2C38B)
    private val DogLight = Color(0xFFFFF4E2)
    private val DogDark = Color(0xFFC98A55)

    private fun dog(g: Ctx, th: Float, happy: Boolean) {
        val s = sin(rad(th))
        val c = cos(rad(th))
        val sa = abs(s)
        fun aroundX(phi: Float, r: Float) = r * sin(rad(phi + th))
        fun aroundZ(phi: Float) = cos(rad(phi + th))

        // cola: base y punta en 3D, giradas alrededor del eje vertical
        fun rot3x(x: Float, z: Float) = x * c + z * s
        fun rot3z(x: Float, z: Float) = -x * s + z * c
        val tbx = rot3x(0f, -34f); val tbz = rot3z(0f, -34f)
        val ttx = rot3x(56f, -56f); val ttz = rot3z(56f, -56f)
        val tailZ = (tbz + ttz) / 2
        val tail = Path().apply {
            val bx = 100 + tbx; val by = 190f; val tx = 100 + ttx; val ty = 142f
            val dx = tx - bx; val dy = ty - by
            val len = hypot(dx, dy).takeIf { it > 0f } ?: 1f
            val nx = -dy / len; val ny = dx / len
            val w = 9f
            val side = if (dx >= 0) 1f else -1f
            moveTo(bx + nx * w, by + ny * w)
            quadraticTo(bx + dx * 0.5f + nx * (w + 14f * side * 0.2f) + 8 * side, by + dy * 0.5f + ny * w + 6, tx, ty)
            quadraticTo(bx + dx * 0.5f - nx * w + 8 * side, by + dy * 0.5f - ny * w + 6, bx - nx * w, by - ny * w)
            close()
        }

        // patas en un anillo en el piso; las de atrás, tapadas por el cuerpo
        data class Part(val z: Float, val draw: () -> Unit)
        val feet = listOf(-35f, 35f, -145f, 145f).mapNotNull { phi ->
            val z = aroundZ(phi)
            if (z < -0.7f) return@mapNotNull null
            val near = z > -0.2f
            val x = 100 + aroundX(phi, 30f)
            val y = 213 - (1 - z) + if (near) 1 else 0
            Part(z) { g.ell(x, y, if (near) 15f else 14f, if (near) 10f else 9f, if (near) DogLight else DogDark) }
        }.sortedBy { it.z }
        val arms = listOf(-58f, 58f).map { phi ->
            val dx = aroundX(phi, 38f)
            Part(aroundZ(phi)) { g.ell(100 + dx, 181f, 10f, 15f, DogMain, -22f * dx / 34f) }
        }

        val bodyCx = 100 - 4 * s; val bodyRx = 42 + 6 * sa; val bodyRy = 38 - 4 * sa; val bodyCy = 176 + 2 * sa

        if (tailZ < 0) g.path(tail, DogMain)
        feet.forEach { it.draw() }
        arms.filter { it.z < -0.1f }.forEach { it.draw() }
        g.ell(bodyCx, bodyCy, bodyRx, bodyRy, DogMain)
        // panza: se corre con el cuerpo y se recorta con su contorno
        if (c > -0.3f) {
            val clip = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(bodyCx - bodyRx + 2, bodyCy - bodyRy + 2, bodyCx + bodyRx - 2, bodyCy + bodyRy - 2))
            }
            g.ds.clipPath(clip) {
                g.ell(bodyCx + 10 * s, 186 + 10 * sa, (25 + 5 * sa) * minOf(1f, (c + 0.3f) / 0.6f), 22 - 11 * sa, DogLight, stroke = false)
            }
        }
        arms.filter { it.z >= -0.1f }.forEach { it.draw() }
        if (tailZ >= 0) g.path(tail, DogMain)

        // cabeza
        val headRx = 68 - 6 * sa; val headCx = 100 + 6 * s
        val ears = listOf(-100f, 100f).map { phi ->
            val dx = aroundX(phi, 64f)
            Part(aroundZ(phi)) { g.ell(headCx + dx, 97f, 15f, 31f, DogDark, -18f * dx / 62f) }
        }
        val muzX = headCx + 54 * s; val muzY = 126 - 5 * sa
        val noseX = headCx + 71 * s; val noseY = 116 - 4 * sa
        fun muzzle() {
            g.ell(muzX, muzY, 25 - sa, 18 - sa, DogLight)
            g.ell(noseX, noseY, 8 - 1.5f * sa, 6f, Ink, stroke = false)
        }

        ears.filter { it.z < -0.35f }.forEach { it.draw() }
        if (c < -0.1f) muzzle() // de espaldas, el hocico se asoma por detrás de la cabeza
        g.ell(headCx, 98f, headRx, 60 - 2 * sa, DogMain)

        // marca clara de la frente, o rulito del lomo de la cabeza
        if (c > 0.05f) {
            val x = headCx + 40 * s; val w = 8 * c
            g.path(Path().apply {
                moveTo(x, 40f); quadraticTo(x + w, 52f, x, 60f); quadraticTo(x - w, 52f, x, 40f); close()
            }, DogLight, stroke = false)
        }
        if (c < -0.05f) {
            val x = headCx - 40 * s; val w = 6 * -c
            g.ds.drawPath(Path().apply { moveTo(x - w, 50f); quadraticTo(x, 40f, x + w, 50f) }, DogDark,
                style = Stroke(width = 4f, cap = StrokeCap.Round))
        }
        // mejillas y ojos: se angostan hacia el borde y desaparecen al pasar atrás
        listOf(-44f, 44f).forEach { phi ->
            val z = aroundZ(phi)
            if (z > 0.05f) g.ell(headCx + aroundX(phi, 64f), 124f, 11 * z.pow(0.7f), 6.5f, BlushC, stroke = false, alpha = 0.65f)
        }
        listOf(-25f, 25f).forEach { phi ->
            val z = aroundZ(phi)
            if (z <= 0.22f) return@forEach
            val x = headCx + aroundX(phi, 64f); val y = 100f; val kk = z.pow(0.6f)
            if (happy) {
                g.ds.drawPath(Path().apply { moveTo(x - 10 * kk, y + 3); quadraticTo(x, y - 10, x + 10 * kk, y + 3) }, Ink,
                    style = Stroke(width = 4.5f, cap = StrokeCap.Round))
            } else {
                g.ell(x, y, 10 * kk, 12f, Ink, stroke = false)
                g.ds.drawCircle(Color.White, 4 * kk, Offset(x - 3 * kk, y - 5))
                g.ds.drawCircle(Color.White, 1.8f * kk, Offset(x + 4 * kk, y + 4))
            }
        }
        if (c >= -0.1f) {
            muzzle()
            if (c > 0.15f) { // boca y lengüita
                val mx = headCx + 56 * s; val my = 127 - 4 * sa; val kk = maxOf(0.4f, c)
                val line = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                g.ds.drawLine(Ink, Offset(mx, my - 5), Offset(mx, my), strokeWidth = 3.5f, cap = StrokeCap.Round)
                g.path(Path().apply {
                    moveTo(mx - 3 * kk, my + 3); quadraticTo(mx, my + 11, mx + 3 * kk, my + 3)
                }, Tongue, width = 2.5f)
                g.ds.drawPath(Path().apply {
                    moveTo(mx - 9 * kk, my); quadraticTo(mx - 4.5f * kk, my + 6, mx, my)
                    quadraticTo(mx + 4.5f * kk, my + 6, mx + 9 * kk, my)
                }, Ink, style = line)
            }
        }
        ears.filter { it.z >= -0.35f }.forEach { it.draw() }
    }
}
