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
 * El vínculo con la mascota (Design: Vinculo y SubeDeNivel). Cada "Listo" suma un corazón (tope
 * diario), el primer mimo del día suma otro, y con los corazones se sube de nivel: la mascota aprende
 * trucos o gana accesorios. Nunca resta. Es del usuario, no de cada mascota, y se guarda en las
 * preferencias (viaja con la copia de seguridad de Android).
 */
object Bond {

    /**
     * Lo que se gana en cada nivel. [ready]: ya existe en la app; si no, se anuncia y en "Nuestro
     * camino" aparece como "Muy pronto" (se entrega cuando esté dibujado).
     */
    enum class Reward(
        @StringRes val title: Int,
        @StringRes val learned: Int,
        @StringRes val howTo: Int,
        val ready: Boolean,
    ) {
        WAG(R.string.bond_r_wag, R.string.bond_learned_wag, R.string.bond_how_wag, true),
        SCARF(R.string.bond_r_scarf, R.string.bond_learned_scarf, R.string.bond_how_scarf, false),
        PAW(R.string.bond_r_paw, R.string.bond_learned_paw, R.string.bond_how_paw, true),
        MORNING(R.string.bond_r_morning, R.string.bond_learned_morning, R.string.bond_how_morning, true),
        BED(R.string.bond_r_bed, R.string.bond_learned_bed, R.string.bond_how_bed, false),
        HAT(R.string.bond_r_hat, R.string.bond_learned_hat, R.string.bond_how_hat, false),
        DANCE(R.string.bond_r_dance, R.string.bond_learned_dance, R.string.bond_how_dance, true),
        CROWN(R.string.bond_r_crown, R.string.bond_learned_crown, R.string.bond_how_crown, false),
    }

    class Level(val number: Int, val from: Int, @StringRes val name: Int, @StringRes val title: Int, val reward: Reward)

    val LEVELS = listOf(
        Level(1, 0, R.string.bond_l1, R.string.bond_t1, Reward.WAG),
        Level(2, 10, R.string.bond_l2, R.string.bond_t2, Reward.SCARF),
        Level(3, 30, R.string.bond_l3, R.string.bond_t3, Reward.PAW),
        Level(4, 60, R.string.bond_l4, R.string.bond_t4, Reward.MORNING),
        Level(5, 100, R.string.bond_l5, R.string.bond_t5, Reward.BED),
        Level(6, 150, R.string.bond_l6, R.string.bond_t6, Reward.HAT),
        Level(7, 220, R.string.bond_l7, R.string.bond_t7, Reward.DANCE),
        Level(8, 300, R.string.bond_l8, R.string.bond_t8, Reward.CROWN),
    )

    /** Corazones por "Listo" en un mismo día (evita marcar "Listo" en serie). */
    const val DAILY_LISTO_CAP = 10

    const val KEY_PREFIX = "bond_"
    private const val KEY_HEARTS = "bond_hearts"
    private const val KEY_DAY = "bond_day"
    private const val KEY_DAY_LISTO = "bond_day_listo"
    private const val KEY_PET_DAY = "bond_pet_day"
    private const val KEY_SEEDED = "bond_seeded"
    private const val KEY_CELEBRATED = "bond_celebrated"
    private const val KEY_WELCOME = "bond_welcome"
    private const val KEY_GOT = "bond_got"
    private const val KEY_GREET_DAY = "bond_greet_day"
    private const val KEY_DANCE_DAY = "bond_dance_day"
    private const val KEY_DANCE_COUNT = "bond_dance_count"

    private fun sp(ctx: Context) = Prefs(ctx).sp

    private fun today(): Long = LocalDate.now().toEpochDay()

    fun hearts(ctx: Context): Int = sp(ctx).getInt(KEY_HEARTS, 0)

    fun levelFor(hearts: Int): Level = LEVELS.last { hearts >= it.from }

    fun level(ctx: Context): Level = levelFor(hearts(ctx))

    fun next(level: Level): Level? = LEVELS.getOrNull(level.number)

    /** Avance dentro del nivel: corazones ya juntados y los que pide el próximo (0/0 en el último). */
    fun progress(hearts: Int): Pair<Int, Int> {
        val level = levelFor(hearts)
        val next = next(level) ?: return 0 to 0
        return (hearts - level.from) to (next.from - level.from)
    }

    // ---------------------------------------------------------------- sumar corazones

    /** Un "Listo" que cuenta (fuentes del usuario: recordatorios y calendario, no pruebas ni mensajes). */
    fun counts(alert: PetAlert) = alert.source == PetAlert.Source.LOCAL || alert.source == PetAlert.Source.CALENDAR

    /** Suma el corazón de un "Listo". false si ya se llegó al tope del día. */
    @Synchronized
    fun addListo(ctx: Context): Boolean {
        val sp = sp(ctx)
        val day = today()
        val count = if (sp.getLong(KEY_DAY, -1) == day) sp.getInt(KEY_DAY_LISTO, 0) else 0
        if (count >= DAILY_LISTO_CAP) return false
        sp.edit(commit = true) {
            putLong(KEY_DAY, day)
            putInt(KEY_DAY_LISTO, count + 1)
            putInt(KEY_HEARTS, sp.getInt(KEY_HEARTS, 0) + 1)
        }
        return true
    }

    /** El primer mimo del día (1, 2 o 3 toques) suma un corazón. */
    @Synchronized
    fun addPetting(ctx: Context): Boolean {
        val sp = sp(ctx)
        val day = today()
        if (sp.getLong(KEY_PET_DAY, -1) == day) return false
        sp.edit(commit = true) {
            putLong(KEY_PET_DAY, day)
            putInt(KEY_HEARTS, sp.getInt(KEY_HEARTS, 0) + 1)
        }
        return true
    }

    private val seedLock = Mutex()

    /**
     * La primera vez (también al actualizar desde una versión sin vínculo) se cuentan los "Listo" ya
     * registrados, con el mismo tope diario. Si eso ya sube de nivel, se festeja una sola vez
     * ("¡Ya nos conocíamos!"), no un nivel por vez.
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
            val hearts = sp.getInt(KEY_HEARTS, 0) + seeded
            val level = levelFor(hearts)
            sp.edit(commit = true) {
                putInt(KEY_HEARTS, hearts)
                putInt(KEY_CELEBRATED, level.number)
                putBoolean(KEY_WELCOME, level.number > 1)
                putStringSet(KEY_GOT, readyUpTo(level))
                done[LocalDate.now()]?.let {
                    putLong(KEY_DAY, today())
                    putInt(KEY_DAY_LISTO, minOf(it.size, DAILY_LISTO_CAP))
                }
                putBoolean(KEY_SEEDED, true)
            }
        }
    }

    private fun readyUpTo(level: Level): Set<String> =
        LEVELS.filter { it.number <= level.number && it.reward.ready }.map { it.reward.name }.toSet()

    // ---------------------------------------------------------------- herramientas de dueño

    /** Suma corazones para probar los niveles (los festejos salen como siempre). */
    @Synchronized
    fun ownerAdd(ctx: Context, n: Int) {
        val sp = sp(ctx)
        sp.edit(commit = true) { putInt(KEY_HEARTS, sp.getInt(KEY_HEARTS, 0) + n) }
    }

    /** Vuelve a cero: nivel 1, sin festejos pendientes ni premios entregados. */
    @Synchronized
    fun ownerReset(ctx: Context) {
        sp(ctx).edit(commit = true) {
            putInt(KEY_HEARTS, 0)
            putInt(KEY_CELEBRATED, 1)
            putBoolean(KEY_WELCOME, false)
            putStringSet(KEY_GOT, readyUpTo(LEVELS.first()))
            remove(KEY_DAY); remove(KEY_DAY_LISTO); remove(KEY_PET_DAY); remove(KEY_GREET_DAY)
            putBoolean(KEY_SEEDED, true)
        }
    }

    // ---------------------------------------------------------------- festejos

    sealed interface Celebration {
        /** Subió de nivel jugando: lo que aprendió o ganó. */
        data class LevelUp(val level: Level) : Celebration
        /** Usuario que ya tenía la app: un solo festejo con el nivel al que llegó. */
        data class Welcome(val level: Level) : Celebration
        /** Llegó un premio que antes estaba "Muy pronto" (por ejemplo, el pañuelo). */
        data class Arrived(val level: Level) : Celebration
    }

    /** Lo que falta festejar, si hay algo. Lo muestra la mascota o, con la app abierta, la app. */
    fun pending(ctx: Context): Celebration? {
        val sp = sp(ctx)
        if (!sp.getBoolean(KEY_SEEDED, false)) return null
        val level = level(ctx)
        if (sp.getBoolean(KEY_WELCOME, false)) return Celebration.Welcome(level)
        if (level.number > sp.getInt(KEY_CELEBRATED, 1)) return Celebration.LevelUp(level)
        val got = sp.getStringSet(KEY_GOT, emptySet()).orEmpty()
        LEVELS.firstOrNull { it.number in 2..level.number && it.reward.ready && it.reward.name !in got }
            ?.let { return Celebration.Arrived(it) }
        return null
    }

    @Synchronized
    fun markCelebrated(ctx: Context, c: Celebration) {
        val sp = sp(ctx)
        val got = sp.getStringSet(KEY_GOT, emptySet()).orEmpty()
        sp.edit(commit = true) {
            when (c) {
                is Celebration.Welcome -> {
                    putBoolean(KEY_WELCOME, false)
                    putInt(KEY_CELEBRATED, maxOf(c.level.number, sp.getInt(KEY_CELEBRATED, 1)))
                    putStringSet(KEY_GOT, got + readyUpTo(c.level))
                }
                // Si subió varios niveles de golpe se festeja el último; los trucos de antes quedan entregados.
                is Celebration.LevelUp -> {
                    putInt(KEY_CELEBRATED, c.level.number)
                    putStringSet(KEY_GOT, got + readyUpTo(c.level))
                }
                is Celebration.Arrived -> putStringSet(KEY_GOT, got + c.level.reward.name)
            }
        }
    }

    // ---------------------------------------------------------------- trucos

    fun knows(ctx: Context, reward: Reward): Boolean =
        reward.ready && level(ctx).number >= LEVELS.first { it.reward == reward }.number

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
