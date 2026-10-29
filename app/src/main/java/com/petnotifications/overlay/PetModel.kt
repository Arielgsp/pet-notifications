package com.petnotifications.overlay

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
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
import com.petnotifications.PetType
import com.petnotifications.PetView
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Mascotas "2,5D": cada parte (cabeza, cuerpo, ojos, orejas, hocico, patas, cola) es un óvalo o un trazo
 * con su posición en 3D alrededor del eje vertical de la mascota, así se puede dibujar en cualquier
 * ángulo (giro continuo, girar solo la cabeza) y mover patas y orejas por separado.
 *
 * Es el mismo motor que tools/pet_art/model3d.py (draw_ops), con las mismas piezas (PetModelData.kt,
 * generado por tools/pet_art/gen_kotlin.py). PetModelTest compara lo que dibuja este con lo que dibuja
 * el de Python en cientos de casos: si se cambia uno, hay que cambiar el otro.
 *
 * Lienzo: 200×240 (el de los dibujos), centrado en el de 256×240 de los dibujos fijos.
 */
object PetModel {

    /** Mascotas que tienen modelo (todas las de PetType). */
    fun has(pet: PetType) = PetModelData.parts(pet.name, STAND, lop = false) != null

    /**
     * Mascotas que pueden girar solo la cabeza sentadas: la cabeza del modelo va sobre el cuerpo sentado
     * sin cabeza (PetView.SIT_BODY). Las aves y la rana no: su cuerpo sentado incluye la cabeza.
     */
    fun hasHead(pet: PetType) = has(pet) && pet !in setOf(PetType.DUCK, PetType.CHICK, PetType.OWL, PetType.FROG)

    enum class Face { OPEN, HAPPY, CLOSED }

    /** Formas del modelo: parada, sentada (pose "peluche") y acostada (ver model3d.LIE_Y). */
    const val STAND = 0
    const val SIT = 1
    const val LIE = 2

    /** Cuánto más abajo está la cabeza en la pose sentada (gen.py / model3d.py SIT_HEAD_DY). */
    const val SIT_HEAD_DY = 8f

    /** Acostada de costado: cuerpo de perfil mirando a la izquierda y cabeza de frente (model3d.LIE_TH, LIE_HEAD). */
    private const val LIE_TH = 270.0
    private const val LIE_HEAD = 0.0

    /** Brazo derecho levantado de "dar la patita" (PetView.PAW). */
    private val PAW_POSE = mapOf("armR" to Pose(rot = -118.0))

    /** Nombres de los pelajes de la mascota (el primero es el de siempre). */
    fun coats(pet: PetType): List<String> = PetModelData.palettes[pet.name].orEmpty().map { it.name }

    /** Colores principales del pelaje, para la muestra de la fila de pelajes. */
    fun coatColor(pet: PetType, coat: Int): Long =
        PetModelData.palettes[pet.name]?.getOrNull(coat)?.colors?.get("main") ?: INK

    /**
     * Dibuja la mascota girada [theta] grados (0 de frente, 90 mirando a la derecha, 180 de espaldas)
     * ocupando todo el [DrawScope] (proporción 256×240, igual que los dibujos fijos).
     */
    fun draw(
        ds: DrawScope, pet: PetType, theta: Float, face: Face = Face.OPEN, sit: Boolean = false,
        coat: Int = 0, pose: Map<String, Pose> = emptyMap(), head: Float? = null,
    ) {
        val m = model(pet.name, if (sit) SIT else STAND, coat) ?: return
        paint(ds, drawOps(m.parts, theta.toDouble(), face, pose, head?.toDouble(), null), pet.name)
    }

    /**
     * Solo la cabeza (sentada) girada [theta] grados. Se dibuja encima del cuerpo sentado sin cabeza
     * (PetView.SIT_BODY); ver [hasHead].
     */
    fun drawHead(ds: DrawScope, pet: PetType, theta: Float, face: Face = Face.OPEN, coat: Int = 0) {
        val m = model(pet.name, SIT, coat) ?: return
        paint(ds, drawOps(m.parts, 0.0, face, emptyMap(), theta.toDouble(), HEAD), pet.name)
    }

    /**
     * Uno de los dibujos de la mascota (los mismos que los fijos: de frente, de perfil, acostada,
     * dormida, sentada...) hecho con el modelo: así salen en cualquier pelaje y para las mascotas que no
     * tienen dibujos fijos (el conejo). Ver PetArt.petPainter.
     */
    fun drawView(ds: DrawScope, pet: PetType, view: PetView, coat: Int = 0) {
        val ops = viewOps(pet.name, view, coat) ?: return
        paint(ds, ops, pet.name)
    }

    internal fun viewOps(pet: String, view: PetView, coat: Int): List<MOp>? {
        val shape = when (view) {
            PetView.LIE, PetView.SLEEP -> LIE
            PetView.SIT, PetView.SIT_BLINK, PetView.SIT_HAPPY, PetView.SIT_BODY -> SIT
            else -> STAND
        }
        val m = model(pet, shape, coat) ?: return null
        val face = when (view) {
            PetView.BLINK, PetView.SLEEP, PetView.SIT_BLINK -> Face.CLOSED
            PetView.HAPPY, PetView.PAW, PetView.SIT_HAPPY -> Face.HAPPY
            else -> Face.OPEN
        }
        val side = shape == LIE && pet in PetModelData.lying
        val th = when {
            side -> LIE_TH
            view == PetView.SIDE -> 90.0
            view == PetView.BACK -> 180.0
            else -> 0.0
        }
        val pose = if (view == PetView.PAW) PAW_POSE else emptyMap()
        val layer = if (view == PetView.SIT_BODY) BODY else null
        return drawOps(m.parts, th, face, pose, if (side) LIE_HEAD else null, layer)
    }

    /** Pose de una parte con etiqueta (armL/armR, footL/footR, earL/earR): giro desde su base y subida. */
    data class Pose(val rot: Double = 0.0, val dy: Double = 0.0)

    // ---------------------------------------------------------------- modelos y pelajes

    internal const val BODY = 10
    internal const val HEAD = 20
    internal const val INK = 0xFF4A3228
    private const val WHITE = 0xFFFFFFFF
    internal const val NO_COLOR = -1L

    /** Modelo listo para dibujar: piezas con los colores del pelaje ya puestos. */
    internal class Resolved(val parts: List<RPart>)

    private val cache = HashMap<String, Resolved?>()

    internal fun model(pet: String, shape: Int, coat: Int): Resolved? = synchronized(cache) {
        val key = "$pet/$shape/$coat"
        if (key !in cache) cache[key] = build(pet, shape, coat)
        cache[key]
    }

    private fun build(pet: String, shape: Int, coat: Int): Resolved? {
        val pals = PetModelData.palettes[pet] ?: return null
        val pi = if (coat in pals.indices) coat else 0
        val pal = pals[pi]
        val base = PetModelData.parts(pet, shape, pal.lop) ?: return null
        val spots = if (pal.spots) PetModelData.spots(pet, pi, shape) else emptyList()
        return Resolved((base + spots).map { RPart(it, pal) })
    }
    /** Pieza con sus colores resueltos según el pelaje. */
    internal class RPart(val p: ModelPart, pal: ModelPalette) {
        val fill = resolve(p.fill, pal)
        val color = resolve(p.color, pal)
        val pivotXz = p.pivot
        private fun resolve(c: MC, pal: ModelPalette): Long = when {
            c.key != null -> pal.colors[c.key] ?: INK
            else -> c.argb
        }
        /** Plantilla del trazo ("M{0} Q{1} {2} Z") ya separada en comandos e índices de puntos. */
        val segs: List<Pair<Char, IntArray>>? = p.d?.let { parseTemplate(it) }
    }

    // ---------------------------------------------------------------- operaciones de dibujo

    /** Lo que dibuja el motor, en orden (mismas operaciones que draw_ops de model3d.py). */
    internal sealed class MOp {
        var clip: DoubleArray? = null // cx, cy, rx, ry
        var turn: DoubleArray? = null // grados, px, py

        class Ell(val cx: Double, val cy: Double, val rx: Double, val ry: Double, val fill: Long, val rot: Double = 0.0,
                  val stroke: Boolean = true, val opacity: Double = 1.0) : MOp()
        class Circ(val cx: Double, val cy: Double, val r: Double, val fill: Long) : MOp()
        /** segs: comando (M Q L C Z) y sus coordenadas x, y. */
        class PathOp(val segs: List<Pair<Char, DoubleArray>>, val fill: Long = NO_COLOR, val color: Long = INK, val w: Double = 4.0) : MOp()
        class Mane(val cx: Double, val cy: Double, val r: Double, val n: Int, val rr: Double, val fill: Long) : MOp()
    }

    private class Frame(th: Double) {
        val s = sin(Math.toRadians(th))
        val c = cos(Math.toRadians(th))
        val sa = abs(s)
        fun projX(x: Double, z: Double) = 100 + x * c + z * s
        fun projZ(x: Double, z: Double) = -x * s + z * c
        fun facing(x: Double, z: Double): Double {
            val n = hypot(x, z)
            return if (n < 1e-6) c else (-x * s + z * c) / n
        }
    }

    private class Item(val key: Double, val depth: Double, val ops: List<MOp>)

    internal fun drawOps(
        parts: List<RPart>, th: Double, face: Face, pose: Map<String, Pose>, head: Double?, layer: Int?,
    ): List<MOp> {
        val fb = Frame(th)
        val fh = Frame(head ?: th)
        val items = ArrayList<Item>(parts.size)
        val clips = arrayOfNulls<DoubleArray>(3) // [CLIP_BODY], [CLIP_HEAD]
        for (rp in parts) {
            val el = rp.p
            if (el.kind != ModelPart.VOL) continue
            val f = if (el.layer == HEAD) fh else fb
            val sa = f.sa
            clips[if (el.layer == BODY) ModelPart.CLIP_BODY else ModelPart.CLIP_HEAD] = doubleArrayOf(
                f.projX(el.x.at(sa), el.z.at(sa)), el.y.at(sa), el.rx.at(sa) - 2, el.ry.at(sa) - 2)
        }
        for (rp in parts) {
            val el = rp.p
            if (layer != null && el.layer != layer) continue
            val f = if (el.layer == HEAD) fh else fb
            val sa = f.sa
            val ps = el.tag?.let { pose[it] }
            val dyPose = ps?.dy ?: 0.0
            var drotPose = ps?.rot ?: 0.0
            when (el.kind) {
                ModelPart.VOL -> {
                    val x = el.x.at(sa); val z = el.z.at(sa)
                    items += Item(el.layer.toDouble(), 0.0, listOf(MOp.Ell(f.projX(x, z), el.y.at(sa), el.rx.at(sa), el.ry.at(sa), rp.fill,
                        stroke = el.stroke)))
                }
                ModelPart.PART, ModelPart.EYE -> {
                    val x = el.x.at(sa); val z = el.z.at(sa)
                    val px = f.projX(x, z); val pz = f.projZ(x, z)
                    val fz = f.facing(x, z)
                    if (el.hide != null && fz < el.hide) continue
                    val y = el.y.at(sa) + dyPose
                    if (el.kind == ModelPart.EYE) {
                        val kk = max(fz, 0.0).pow(0.6)
                        val r0 = el.rx.a; val r1 = el.ry.a
                        val ops = when {
                            face == Face.OPEN && el.kawaii -> listOf(
                                MOp.Ell(px, y, r0 * kk, r1, INK, stroke = false),
                                MOp.Circ(px - r0 * 0.3 * kk, y - r1 * 0.38, r0 * 0.48 * kk, WHITE),
                                MOp.Circ(px + r0 * 0.42 * kk, y + r1 * 0.4, r0 * 0.24 * kk, WHITE))
                            face == Face.OPEN -> listOf(
                                MOp.Ell(px, y, r0 * kk, r1, INK, stroke = false),
                                MOp.Circ(px - 3 * kk, y - 5, r0 * 0.4 * kk, WHITE),
                                MOp.Circ(px + 4 * kk, y + 4, 1.8 * kk, WHITE))
                            face == Face.HAPPY -> listOf(MOp.PathOp(listOf('M' to doubleArrayOf(px - r0 * kk, y + 3),
                                'Q' to doubleArrayOf(px, y - 10, px + r0 * kk, y + 3)), w = 4.5))
                            else -> listOf(MOp.PathOp(listOf('M' to doubleArrayOf(px - r0 * kk, y - 2),
                                'Q' to doubleArrayOf(px, y + 7, px + r0 * kk, y - 2)), w = 4.5))
                        }
                        items += Item(HEAD + 0.5, pz, ops)
                        continue
                    }
                    var rx = el.rx.at(sa)
                    if (el.flat) rx *= max(fz, 0.0).pow(el.p)
                    val rot0 = el.rot.at(sa)
                    val rot = if (abs(x) > 1) rot0 * (px - 100) / x else rot0 * (1 - sa)
                    var key = el.layer + if (fz < el.behind) -0.5 else 0.5
                    val o = MOp.Ell(px, y, rx, el.ry.at(sa), rp.fill, rot, el.stroke, el.opacity)
                    if (el.clip != 0) clips[el.clip]?.let {
                        o.clip = it
                        key = el.layer + 0.1 // justo encima de su volumen
                    }
                    var baseRot = el.baseRot.at(sa)
                    if (baseRot != 0.0 && abs(x) > 1 && (px - 100) * x < 0) baseRot = -baseRot
                    drotPose += baseRot
                    if (drotPose != 0.0) {
                        val pvy = if (el.pivotY != null) el.pivotY + dyPose else y - el.ry.at(sa) * 0.8
                        o.turn = doubleArrayOf(drotPose, px, pvy)
                        // brazo levantado: pasa por delante de la cabeza
                        if (abs(drotPose) > 60 && fz >= el.behind) key = HEAD + 0.6
                    }
                    items += Item(key, pz, listOf(o))
                }
                ModelPart.PATH -> {
                    val pts = el.pts!!
                    val n = pts.size / 3
                    val xs = DoubleArray(n); val ds = DoubleArray(n); val ys = DoubleArray(n)
                    val fz: Double
                    val bb = el.bb
                    if (bb != null) {
                        val apx = f.projX(bb[0], bb[1]); val apz = f.projZ(bb[0], bb[1])
                        for (i in 0 until n) { xs[i] = apx + (pts[3 * i] - bb[0]); ds[i] = apz; ys[i] = pts[3 * i + 1] }
                        fz = if (bb[0] != 0.0 || bb[1] != 0.0) f.facing(bb[0], bb[1]) else 1.0
                    } else {
                        var sum = 0.0
                        for (i in 0 until n) {
                            val px = pts[3 * i]; val pz = pts[3 * i + 2]
                            xs[i] = f.projX(px, pz); ds[i] = f.projZ(px, pz); ys[i] = pts[3 * i + 1]
                            sum += f.facing(px, pz)
                        }
                        fz = sum / n
                    }
                    if (el.hide != null && fz < el.hide) continue
                    val o = MOp.PathOp(fillTemplate(rp.segs!!, xs, ys, dyPose), rp.fill, rp.color, el.width)
                    val piv = el.pivot
                    if (drotPose != 0.0 && piv != null) o.turn = doubleArrayOf(drotPose, f.projX(piv[0], piv[2]), piv[1] + dyPose)
                    items += Item(el.layer + if (fz < el.behind) -0.5 else 0.5, ds.average(), listOf(o))
                }
                ModelPart.TAIL -> {
                    val b = el.base!!; val t = el.tip!!
                    val bx = f.projX(b[0], b[1]); val bz = f.projZ(b[0], b[1])
                    val tx = f.projX(t[0], t[1]); val tz = f.projZ(t[0], t[1])
                    val by = el.ty!![0]; val ty = el.ty[1]
                    val dx = tx - bx; val dy = ty - by
                    val ln = hypot(dx, dy).takeIf { it != 0.0 } ?: 1.0
                    val nx = -dy / ln; val ny = dx / ln
                    val w = el.width
                    val side = if (dx >= 0) 1 else -1
                    val c1x = bx + dx * .5 + nx * (w + 14 * side * .2) + el.bend * side; val c1y = by + dy * .5 + ny * w + 6
                    val c2x = bx + dx * .5 - nx * w + el.bend * side; val c2y = by + dy * .5 - ny * w + 6
                    val segs = listOf('M' to doubleArrayOf(bx + nx * w, by + ny * w), 'Q' to doubleArrayOf(c1x, c1y, tx, ty),
                        'Q' to doubleArrayOf(c2x, c2y, bx - nx * w, by - ny * w), 'Z' to DoubleArray(0))
                    val zz = (bz + tz) / 2
                    items += Item(el.layer + if (zz >= 0) 0.5 else -0.5, zz, listOf(MOp.PathOp(segs, rp.fill)))
                }
                ModelPart.ROPE -> {
                    val pts = el.pts!!
                    val n = pts.size / 3
                    val xs = DoubleArray(n) { f.projX(pts[3 * it], pts[3 * it + 2]) }
                    val ys = DoubleArray(n) { pts[3 * it + 1] }
                    val zz = (0 until n).sumOf { f.projZ(pts[3 * it], pts[3 * it + 2]) } / n
                    val segs = fillTemplate(parseTemplate(if (n > 3) "M{0} C{1} {2} {3}" else "M{0} Q{1} {2}"), xs, ys, 0.0)
                    items += Item(el.layer + if (zz >= 0) 0.5 else -0.5, zz,
                        listOf(MOp.PathOp(segs, w = el.width), MOp.PathOp(segs, color = rp.color, w = el.width - 6)))
                }
                ModelPart.MANE -> {
                    val r = el.rx.a; val n = 14
                    items += Item(HEAD - 0.5, -999.0, listOf(MOp.Mane(f.projX(el.x.a, el.z.a), el.y.a, r, n, r * sin(PI / n) * 1.15, rp.fill)))
                }
            }
        }
        // Mismo orden que Python (orden estable por clave y profundidad).
        items.sortWith(compareBy<Item> { it.key }.thenBy { it.depth })
        return items.flatMap { it.ops }
    }

    private fun parseTemplate(d: String): List<Pair<Char, IntArray>> {
        val out = ArrayList<Pair<Char, MutableList<Int>>>()
        for (tok0 in d.split(' ').filter { it.isNotEmpty() }) {
            var tok = tok0
            if (tok[0].isLetter()) {
                out += tok[0] to mutableListOf()
                tok = tok.substring(1)
            }
            if (tok.isNotEmpty()) out.last().second += tok.trim('{', '}').toInt()
        }
        return out.map { it.first to it.second.toIntArray() }
    }

    private fun fillTemplate(t: List<Pair<Char, IntArray>>, xs: DoubleArray, ys: DoubleArray, dy: Double) =
        t.map { (cmd, idx) -> cmd to DoubleArray(idx.size * 2) { j -> if (j % 2 == 0) xs[idx[j / 2]] else ys[idx[j / 2]] + dy } }

    // ---------------------------------------------------------------- dibujo (Compose)

    private val outline = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    private fun col(c: Long, alpha: Float = 1f) = Color(c.toInt()).copy(alpha = alpha)

    /**
     * Achique de las mascotas que se salen por arriba del lienzo (las orejas del conejo llegan a y = −16):
     * la app recorta lo que queda afuera, y al saltar o girar la mascota sube un poco. Se achica desde el
     * piso (los pies quedan en su lugar) hasta dejar arriba un margen como el del gato (~20).
     */
    private val fit = mapOf("RABBIT" to 0.85f)
    private const val FLOOR_Y = 228f

    private fun paint(ds: DrawScope, ops: List<MOp>, pet: String) {
        val k = ds.size.width / 256f
        val f = fit[pet] ?: 1f
        ds.scale(k, k, pivot = Offset.Zero) {
            scale(f, f, pivot = Offset(128f, FLOOR_Y)) {
                translate(left = 28f) {
                    for (o in ops) paintOp(this, o)
                }
            }
        }
    }

    private fun paintOp(ds: DrawScope, o: MOp) {
        val turn = o.turn
        if (turn != null) {
            ds.rotate(turn[0].toFloat(), Offset(turn[1].toFloat(), turn[2].toFloat())) { paintClipped(this, o) }
        } else paintClipped(ds, o)
    }

    private fun paintClipped(ds: DrawScope, o: MOp) {
        val clip = o.clip
        if (clip != null) {
            val p = Path().apply { addOval(ovalRect(clip[0], clip[1], max(clip[2], 0.01), clip[3])) }
            ds.clipPath(p) { paintShape(this, o) }
        } else paintShape(ds, o)
    }

    private fun ovalRect(cx: Double, cy: Double, rx: Double, ry: Double) =
        Rect((cx - rx).toFloat(), (cy - ry).toFloat(), (cx + rx).toFloat(), (cy + ry).toFloat())

    private fun paintShape(ds: DrawScope, o: MOp) {
        when (o) {
            is MOp.Ell -> {
                val rx = max(o.rx, 0.01)
                val a = o.opacity.toFloat()
                ds.rotate(o.rot.toFloat(), Offset(o.cx.toFloat(), o.cy.toFloat())) {
                    val tl = Offset((o.cx - rx).toFloat(), (o.cy - o.ry).toFloat())
                    val sz = Size((rx * 2).toFloat(), (o.ry * 2).toFloat())
                    drawOval(col(o.fill), tl, sz, alpha = a)
                    if (o.stroke) drawOval(col(INK), tl, sz, alpha = a, style = outline)
                }
            }
            is MOp.Circ -> ds.drawCircle(col(o.fill), o.r.toFloat(), Offset(o.cx.toFloat(), o.cy.toFloat()))
            is MOp.PathOp -> {
                val p = toPath(o.segs)
                if (o.fill != NO_COLOR) ds.drawPath(p, col(o.fill))
                if (o.w > 0) ds.drawPath(p, col(o.color), style = Stroke(o.w.toFloat(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            is MOp.Mane -> {
                val p = manePath(o)
                ds.drawPath(p, col(o.fill))
                ds.drawPath(p, col(INK), style = outline)
            }
        }
    }

    private fun toPath(segs: List<Pair<Char, DoubleArray>>) = Path().apply {
        for ((cmd, v) in segs) {
            val f = FloatArray(v.size) { v[it].toFloat() }
            when (cmd) {
                'M' -> moveTo(f[0], f[1])
                'L' -> lineTo(f[0], f[1])
                'Q' -> quadraticTo(f[0], f[1], f[2], f[3])
                'C' -> cubicTo(f[0], f[1], f[2], f[3], f[4], f[5])
                'Z' -> close()
            }
        }
    }

    /** Melena: n festones (arcos de radio rr, en el sentido de las agujas del reloj) alrededor de la cabeza. */
    private fun manePath(o: MOp.Mane): Path {
        val pts = List(o.n) { i ->
            val a = 2 * PI * i / o.n - PI / 2
            (o.cx + o.r * cos(a)) to (o.cy + o.r * sin(a))
        }
        return Path().apply {
            moveTo(pts[0].first.toFloat(), pts[0].second.toFloat())
            for (i in 0 until o.n) {
                val (x1, y1) = pts[i]
                val (x2, y2) = pts[(i + 1) % o.n]
                val arc = svgArc(x1, y1, x2, y2, o.rr)
                arcTo(ovalRect(arc[0], arc[1], o.rr, o.rr), arc[2].toFloat(), arc[3].toFloat(), false)
            }
            close()
        }
    }

    /**
     * Arco SVG "A r r 0 0 1 x2 y2" (círculo, arco chico, sentido horario) de (x1, y1): centro y ángulos
     * de inicio y barrido en grados, como los pide Path.arcTo.
     */
    internal fun svgArc(x1: Double, y1: Double, x2: Double, y2: Double, r0: Double): DoubleArray {
        val hx = (x1 - x2) / 2; val hy = (y1 - y2) / 2
        val d2 = hx * hx + hy * hy
        val r = max(r0, sqrt(d2))
        val coef = sqrt(max(0.0, (r * r - d2) / d2)) // arco chico + horario: signo +
        val cxp = coef * hy; val cyp = -coef * hx
        val cx = cxp + (x1 + x2) / 2; val cy = cyp + (y1 + y2) / 2
        val start = Math.toDegrees(atan2((hy - cyp) / r, (hx - cxp) / r))
        val ux = (hx - cxp) / r; val uy = (hy - cyp) / r
        val vx = (-hx - cxp) / r; val vy = (-hy - cyp) / r
        var sweep = Math.toDegrees(acos(((ux * vx + uy * vy) / (hypot(ux, uy) * hypot(vx, vy))).coerceIn(-1.0, 1.0)))
        if (ux * vy - uy * vx < 0) sweep = -sweep
        if (sweep < 0) sweep += 360.0
        return doubleArrayOf(cx, cy, start, sweep)
    }
}

/** Valor que cambia de frente (a) a perfil (b): a + (b − a)·|sen θ| (model3d.vary). */
internal class V(val a: Double, val b: Double) {
    fun at(sa: Double) = if (a == b) a else a + (b - a) * sa
}

internal fun v(a: Double) = V(a, a)
internal fun v(a: Double, b: Double) = V(a, b)

/** Color fijo ([argb]) o con nombre ([key], lo pone el pelaje). */
internal class MC(val argb: Long, val key: String?)

internal fun c(rgb: Long) = MC(0xFF000000 or rgb, null)
internal fun k(key: String) = MC(PetModel.NO_COLOR, key)
internal val NONE = MC(PetModel.NO_COLOR, null)

/** Pelaje: nombre, colores con nombre, orejas caídas y si tiene manchas (PetModelData.spots). */
internal class ModelPalette(val name: String, val colors: Map<String, Long>, val lop: Boolean = false, val spots: Boolean = false)

/** Una pieza del modelo (ver model3d.py: vol, part, eye, path, tail, rope_tail y la melena). */
internal class ModelPart(
    val kind: Int,
    val layer: Int = PetModel.BODY,
    val x: V = V0, val y: V = V0, val z: V = V0,
    val rx: V = V0, val ry: V = V0,
    val fill: MC = NONE,
    val stroke: Boolean = true,
    val rot: V = V0,
    val behind: Double = -0.35,
    val flat: Boolean = false,
    val p: Double = 0.7,
    val hide: Double? = null,
    val opacity: Double = 1.0,
    val clip: Int = 0,
    val pivotY: Double? = null,
    val baseRot: V = V0,
    val kawaii: Boolean = false,
    /** Trazos y colas de cuerda: puntos x, y, z seguidos. */
    val pts: DoubleArray? = null,
    val d: String? = null,
    val width: Double = 4.0,
    val color: MC = MC(PetModel.INK, null),
    val bb: DoubleArray? = null,
    val pivot: DoubleArray? = null,
    val base: DoubleArray? = null,
    val tip: DoubleArray? = null,
    val bend: Double = 8.0,
    val ty: DoubleArray? = null,
    val tag: String? = null,
) {
    companion object {
        const val VOL = 0
        const val PART = 1
        const val EYE = 2
        const val PATH = 3
        const val TAIL = 4
        const val ROPE = 5
        const val MANE = 6
        const val CLIP_BODY = 1
        const val CLIP_HEAD = 2
        private val V0 = V(0.0, 0.0)
    }
}
