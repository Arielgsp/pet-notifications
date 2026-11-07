package com.petnotifications

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.edit
import com.petnotifications.data.AppDatabase
import com.petnotifications.overlay.PetAlert
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * El camino de la mascota (pedido de Cowork del 07/11/2026). Cada "Listo" suma un corazón (tope diario) y
 * el primero del día uno más; el primer mimo del día y abrir la app una vez por día suman otro. Con los
 * corazones la mascota gana 8 premios: trucos, accesorios y un juguete. No hay niveles a la vista. Nunca
 * resta. Es del usuario, no de cada mascota, y se guarda en las preferencias (viaja con la copia de
 * seguridad de Android).
 */
object Bond {

    enum class Kind { TRICK, WEAR, TOY }

    /**
     * Los premios, en orden. [at]: corazones con los que se gana (las cifras están solo acá). [title]: su
     * nombre ("Gorrito de lana"); [phrase]: cómo se dice en las frases ("gorrito", "dar la patita");
     * [won]: el título del festejo ("¡Estreno gorrito!").
     */
    enum class Reward(
        val at: Int,
        val kind: Kind,
        @StringRes val title: Int,
        @StringRes val phrase: Int,
        @StringRes val won: Int,
    ) {
        SCARF(3, Kind.WEAR, R.string.acc_scarf, R.string.bond_p_scarf, R.string.bond_won_scarf),
        PAW(10, Kind.TRICK, R.string.bond_r_paw, R.string.bond_p_paw, R.string.bond_won_paw),
        BED(22, Kind.WEAR, R.string.acc_bed, R.string.bond_p_bed, R.string.bond_won_bed),
        BALL(40, Kind.TOY, R.string.acc_ball, R.string.bond_p_ball, R.string.bond_won_ball),
        DANCE(65, Kind.TRICK, R.string.bond_r_dance, R.string.bond_p_dance, R.string.bond_won_dance),
        HAT(95, Kind.WEAR, R.string.acc_hat, R.string.bond_p_hat, R.string.bond_won_hat),
        TAG(135, Kind.WEAR, R.string.acc_tag, R.string.bond_p_tag, R.string.bond_won_tag),
        CROWN(180, Kind.WEAR, R.string.acc_crown, R.string.bond_p_crown, R.string.bond_won_crown),
    }

    val REWARDS = Reward.entries

    /** Corazones por "Listo" en un mismo día (evita marcar "Listo" en serie). */
    const val DAILY_LISTO_CAP = 10

    /** Con este "Listo" ya se dicen "casi": la tarjeta pasa de "Lo próximo" a "Un poco más y...". */
    const val ALMOST = 10

    const val KEY_PREFIX = "bond_"
    private const val KEY_HEARTS = "bond_hearts"
    private const val KEY_DAY = "bond_day"
    private const val KEY_DAY_LISTO = "bond_day_listo"
    private const val KEY_PET_DAY = "bond_pet_day"
    private const val KEY_VISIT_DAY = "bond_visit_day"
    private const val KEY_SEEDED = "bond_seeded"
    /** Premios ya festejados (nombres de Reward). Los de versiones anteriores siguen sirviendo. */
    private const val KEY_GOT = "bond_got"
    private const val KEY_GREET_DAY = "bond_greet_day"
    private const val KEY_DANCE_DAY = "bond_dance_day"
    private const val KEY_DANCE_COUNT = "bond_dance_count"

    private fun sp(ctx: Context) = Prefs(ctx).sp

    private fun today(): Long = LocalDate.now().toEpochDay()

    fun hearts(ctx: Context): Int = sp(ctx).getInt(KEY_HEARTS, 0)

    /** Los ganados con [hearts]. */
    fun earned(hearts: Int): List<Reward> = REWARDS.filter { hearts >= it.at }

    /** El próximo premio; null si ya ganó todos. */
    fun next(hearts: Int): Reward? = REWARDS.firstOrNull { hearts < it.at }

    /** Avance hacia el próximo premio, de 0 a 1 (1 si ya ganó todos). */
    fun progress(hearts: Int): Float {
        val next = next(hearts) ?: return 1f
        val from = REWARDS.getOrNull(next.ordinal - 1)?.at ?: 0
        return ((hearts - from).toFloat() / (next.at - from)).coerceIn(0f, 1f)
    }

    // ---------------------------------------------------------------- sumar corazones

    /** Un "Listo" que cuenta (fuentes del usuario: recordatorios y calendario, no pruebas ni mensajes). */
    fun counts(alert: PetAlert) = alert.source == PetAlert.Source.LOCAL || alert.source == PetAlert.Source.CALENDAR

    /**
     * Suma el corazón de un "Listo" y, si es el primero del día, uno más. false si ya se llegó al tope
     * del día.
     */
    @Synchronized
    fun addListo(ctx: Context): Boolean {
        val sp = sp(ctx)
        val day = today()
        val count = if (sp.getLong(KEY_DAY, -1) == day) sp.getInt(KEY_DAY_LISTO, 0) else 0
        if (count >= DAILY_LISTO_CAP) return false
        val gain = if (count == 0) 2 else 1
        sp.edit(commit = true) {
            putLong(KEY_DAY, day)
            putInt(KEY_DAY_LISTO, count + 1)
            putInt(KEY_HEARTS, sp.getInt(KEY_HEARTS, 0) + gain)
        }
        return true
    }

    /** El primer mimo del día (1 o 2 toques) suma un corazón. */
    @Synchronized
    fun addPetting(ctx: Context): Boolean = addDaily(ctx, KEY_PET_DAY)

    /** Pasar a saludarla: abrir la app suma un corazón, una vez por día. */
    @Synchronized
    fun addVisit(ctx: Context): Boolean = sp(ctx).getBoolean(KEY_SEEDED, false) && addDaily(ctx, KEY_VISIT_DAY)

    private fun addDaily(ctx: Context, key: String): Boolean {
        val sp = sp(ctx)
        val day = today()
        if (sp.getLong(key, -1) == day) return false
        sp.edit(commit = true) {
            putLong(key, day)
            putInt(KEY_HEARTS, sp.getInt(KEY_HEARTS, 0) + 1)
        }
        return true
    }

    private val seedLock = Mutex()

    /**
     * La primera vez (también al actualizar desde una versión sin vínculo) se cuentan los "Listo" ya
     * registrados, con el mismo tope diario. Si con eso ya gana premios, se festejan todos juntos en un
     * solo festejo ([Celebration.CatchUp]).
     */
    suspend fun seedIfNeeded(ctx: Context) = seedLock.withLock {
        val sp = sp(ctx)
        if (sp.getBoolean(KEY_SEEDED, false)) return@withLock
        val zone = ZoneId.systemDefault()
        val done = runCatching { AppDatabase.get(ctx).completions().all() }.getOrDefault(emptyList())
            .filter { it.source == PetAlert.Source.LOCAL.name || it.source == PetAlert.Source.CALENDAR.name }
            .groupBy { Instant.ofEpochMilli(it.doneAt).atZone(zone).toLocalDate() }
        val seeded = done.values.sumOf { minOf(it.size, DAILY_LISTO_CAP) }
        synchronized(this) {
            sp.edit(commit = true) {
                putInt(KEY_HEARTS, sp.getInt(KEY_HEARTS, 0) + seeded)
                done[LocalDate.now()]?.let {
                    putLong(KEY_DAY, today())
                    putInt(KEY_DAY_LISTO, minOf(it.size, DAILY_LISTO_CAP))
                }
                putBoolean(KEY_SEEDED, true)
            }
        }
    }

    // ---------------------------------------------------------------- herramientas de dueño

    /** Suma corazones para probar los premios (los festejos salen como siempre). */
    @Synchronized
    fun ownerAdd(ctx: Context, n: Int) {
        val sp = sp(ctx)
        sp.edit(commit = true) { putInt(KEY_HEARTS, sp.getInt(KEY_HEARTS, 0) + n) }
    }

    /** Vuelve a cero: sin premios ni festejos pendientes, y los corazones de hoy se pueden volver a ganar. */
    @Synchronized
    fun ownerReset(ctx: Context) {
        sp(ctx).edit(commit = true) {
            putInt(KEY_HEARTS, 0)
            putStringSet(KEY_GOT, emptySet())
            remove(KEY_DAY); remove(KEY_DAY_LISTO); remove(KEY_PET_DAY); remove(KEY_VISIT_DAY); remove(KEY_GREET_DAY)
            putBoolean(KEY_SEEDED, true)
        }
    }

    // ---------------------------------------------------------------- festejos

    sealed interface Celebration {
        val rewards: List<Reward>

        /** Ganó un premio. */
        data class Earned(val reward: Reward) : Celebration {
            override val rewards get() = listOf(reward)
        }

        /**
         * Ganó varios de una vez (quien ya tenía corazones, al actualizar o al cambiar la tabla): un solo
         * festejo que los resume.
         */
        data class CatchUp(override val rewards: List<Reward>) : Celebration
    }

    /** Lo que falta festejar, si hay algo. Un truco lo festeja la mascota; lo demás, la app. */
    fun pending(ctx: Context): Celebration? {
        val sp = sp(ctx)
        if (!sp.getBoolean(KEY_SEEDED, false)) return null
        val got = sp.getStringSet(KEY_GOT, emptySet()).orEmpty()
        val missing = earned(hearts(ctx)).filter { it.name !in got }
        return when (missing.size) {
            0 -> null
            1 -> Celebration.Earned(missing.single())
            else -> Celebration.CatchUp(missing)
        }
    }

    /** Un truco ganado que todavía no se festejó: lo festeja la mascota en el momento (OverlayService). */
    fun pendingTrick(ctx: Context): Reward? {
        val sp = sp(ctx)
        if (!sp.getBoolean(KEY_SEEDED, false)) return null
        val got = sp.getStringSet(KEY_GOT, emptySet()).orEmpty()
        val missing = earned(hearts(ctx)).filter { it.name !in got }
        // Si además hay otros premios sin festejar, va todo junto en el resumen de la app.
        return missing.singleOrNull()?.takeIf { it.kind == Kind.TRICK }
    }

    /** Cómo se usa un truco ("Tócame dos veces y, entre vuelta y vuelta, te la doy."). */
    @StringRes
    fun trickHow(r: Reward): Int = if (r == Reward.DANCE) R.string.bond_how_dance else R.string.bond_how_paw

    @Synchronized
    fun markCelebrated(ctx: Context, c: Celebration) {
        val sp = sp(ctx)
        val got = sp.getStringSet(KEY_GOT, emptySet()).orEmpty()
        sp.edit(commit = true) { putStringSet(KEY_GOT, got + c.rewards.map { it.name }) }
    }

    // ---------------------------------------------------------------- trucos

    fun knows(ctx: Context, reward: Reward): Boolean = hearts(ctx) >= reward.at

    /** El saludo de buenos días ya se dio hoy. */
    fun greetedToday(ctx: Context) = sp(ctx).getLong(KEY_GREET_DAY, -1) == today()

    fun markGreeted(ctx: Context) = sp(ctx).edit { putLong(KEY_GREET_DAY, today()) }

    /** Bailes sueltos (sin "Listo") que ya hizo hoy; como máximo 2 por día. */
    fun dancesToday(ctx: Context): Int {
        val sp = sp(ctx)
        return if (sp.getLong(KEY_DANCE_DAY, -1) == today()) sp.getInt(KEY_DANCE_COUNT, 0) else 0
    }

    fun markDance(ctx: Context) {
        val n = dancesToday(ctx)
        sp(ctx).edit {
            putLong(KEY_DANCE_DAY, today())
            putInt(KEY_DANCE_COUNT, n + 1)
        }
    }
}
