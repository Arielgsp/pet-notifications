package com.petnotifications.overlay

import com.petnotifications.PetType
import com.petnotifications.PetView
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.Locale
import kotlin.math.abs

/**
 * El motor de la app (PetModel) tiene que dibujar exactamente lo mismo que el de la vista previa
 * (tools/pet_art/model3d.py): pet_model_golden.txt tiene cientos de casos (mascota, pelaje, parada o
 * sentada, ángulo, cara, cabeza girada, poses) con las operaciones de dibujo que da Python; se generan
 * con tools/pet_art/gen_kotlin.py junto con PetModelData.kt.
 */
class PetModelTest {

    private fun hex(c: Long) = if (c == PetModel.NO_COLOR) "none" else String.format(Locale.ROOT, "#%06X", c and 0xFFFFFF)
    private fun f4(v: Double) = String.format(Locale.ROOT, "%.4f", v)

    private fun line(o: PetModel.MOp): String {
        var s = when (o) {
            is PetModel.MOp.Ell -> "ell ${f4(o.cx)} ${f4(o.cy)} ${f4(o.rx)} ${f4(o.ry)} ${hex(o.fill)} ${f4(o.rot)} ${if (o.stroke) 1 else 0} ${f4(o.opacity)}"
            is PetModel.MOp.Circ -> "circ ${f4(o.cx)} ${f4(o.cy)} ${f4(o.r)} ${hex(o.fill)}"
            is PetModel.MOp.PathOp -> "path ${hex(o.fill)} ${hex(o.color)} ${f4(o.w)} | " +
                o.segs.joinToString(" ") { (cmd, xs) -> cmd + xs.joinToString("") { " " + f4(it) } }
            is PetModel.MOp.Mane -> "mane ${f4(o.cx)} ${f4(o.cy)} ${f4(o.r)} ${o.n} ${f4(o.rr)} ${hex(o.fill)}"
        }
        o.clip?.let { s += " clip " + it.joinToString(" ") { v -> f4(v) } }
        o.turn?.let { s += " turn " + it.joinToString(" ") { v -> f4(v) } }
        return s
    }

    /** Iguales salvo números a menos de 0,002 (redondeo de cuatro decimales) y mayúsculas en colores. */
    private fun same(a: String, b: String): Boolean {
        val ta = a.split(' '); val tb = b.split(' ')
        if (ta.size != tb.size) return false
        return ta.indices.all { i ->
            val x = ta[i].toDoubleOrNull(); val y = tb[i].toDoubleOrNull()
            if (x != null && y != null) abs(x - y) <= 2e-3 else ta[i].equals(tb[i], ignoreCase = true)
        }
    }

    private fun pose(s: String): Map<String, PetModel.Pose> = if (s == "-") emptyMap() else s.split(';').associate {
        val (tag, rot, dy) = it.split(':')
        tag to PetModel.Pose(rot.toDouble(), dy.toDouble())
    }

    @Test
    fun sameDrawingAsPythonPreview() {
        val text = javaClass.classLoader!!.getResourceAsStream("pet_model_golden.txt")!!.bufferedReader().readText()
        val cases = text.trim().split("\n").fold(mutableListOf<MutableList<String>>()) { acc, l ->
            if (l.startsWith("CASE ")) acc += mutableListOf(l) else acc.last() += l
            acc
        }
        assertTrue("pocos casos: ${cases.size}", cases.size > 500)
        var failures = 0
        val report = StringBuilder()
        for (c in cases) {
            val h = c[0].split(' ')
            val pet = h[1]; val pal = h[2].toInt(); val shape = h[3].toInt(); val th = h[4].toDouble()
            val face = PetModel.Face.valueOf(h[5].uppercase())
            val head = h[6].takeIf { it != "-" }?.toDouble()
            val layer = h[8].takeIf { it != "-" }?.toInt()
            val m = PetModel.model(pet, shape, pal)
            assertNotNull("sin modelo: ${c[0]}", m)
            val got = PetModel.drawOps(m!!.parts, th, face, pose(h[7]), head, layer).map { line(it) }
            val want = c.drop(1)
            val ok = got.size == want.size && got.indices.all { same(got[it], want[it]) }
            if (!ok) {
                failures++
                if (failures <= 3) {
                    report.append("\n${c[0]} (${got.size} vs ${want.size} operaciones)")
                    val i = got.indices.firstOrNull { it >= want.size || !same(got[it], want[it]) } ?: got.size
                    report.append("\n  app:    ${got.getOrNull(i)}\n  python: ${want.getOrNull(i)}")
                }
            }
        }
        if (failures > 0) fail("$failures de ${cases.size} casos distintos:$report")
    }

    @Test
    fun everyPetTypeHasAModelForEveryViewAndCoat() {
        for (p in PetType.entries) {
            assertTrue(p.name, PetModel.has(p))
            for (shape in 0..2) assertNotNull(p.name, PetModel.model(p.name, shape, 0))
            // SIT_BODY (cuerpo sentado sin cabeza) solo lo usan las que giran solo la cabeza.
            val views = PetView.entries.filter { it != PetView.SIT_BODY || PetModel.hasHead(p) }
            for (coat in PetModel.coats(p).indices) for (v in views) {
                assertTrue("${p.name} $coat $v", PetModel.viewOps(p.name, v, coat)!!.isNotEmpty())
            }
        }
        assertEquals(false, PetModel.hasHead(PetType.OWL))
        assertEquals(true, PetModel.hasHead(PetType.DOG))
    }

    @Test
    fun maneArcGoesClockwiseThroughBothEnds() {
        // Festón de (0, -10) a (10, 0) con radio 8: el arco empieza y termina en esos puntos, es el chico
        // y va en el sentido de las agujas del reloj (ángulo creciente con y hacia abajo).
        val a = PetModel.svgArc(0.0, -10.0, 10.0, 0.0, 8.0)
        val (cx, cy, start, sweep) = a.toList()
        fun at(deg: Double) = Pair(cx + 8 * kotlin.math.cos(Math.toRadians(deg)), cy + 8 * kotlin.math.sin(Math.toRadians(deg)))
        val p0 = at(start); val p1 = at(start + sweep)
        assertEquals(0.0, p0.first, 1e-6); assertEquals(-10.0, p0.second, 1e-6)
        assertEquals(10.0, p1.first, 1e-6); assertEquals(0.0, p1.second, 1e-6)
        assertTrue(sweep in 0.0..180.0)
    }
}
