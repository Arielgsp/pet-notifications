package com.petnotifications

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Configuración del usuario, compartida entre la pantalla de ajustes y el servicio de la mascota. */
class Prefs(context: Context) {
    val sp: SharedPreferences =
        context.applicationContext.getSharedPreferences("pet_prefs", Context.MODE_PRIVATE)

    var petEnabled: Boolean
        get() = sp.getBoolean(KEY_ENABLED, false)
        set(v) = sp.edit { putBoolean(KEY_ENABLED, v) }

    /** Sin Pro, solo las 2 mascotas gratis que eligió el usuario (hasta que elija, la que tenía). */
    var pet: PetType
        get() {
            val p = PetType.from(sp.getString(KEY_PET, null))
            val free = freePets
            return if (isPro || free.isEmpty() || p in free) p else free.first()
        }
        set(v) = sp.edit { putString(KEY_PET, v.name) }

    /** Pelaje de cada mascota (Pro; 0 = el original, el único sin Pro). Ver PetModel.coats. */
    fun coat(p: PetType): Int = if (isPro) sp.getInt(KEY_COAT + p.name, 0) else 0

    fun setCoat(p: PetType, coat: Int) = sp.edit { putInt(KEY_COAT + p.name, coat) }

    /**
     * Prueba de una mascota o pelaje Pro sin Pro: la mascota flotante pasa a ser esa durante
     * [TRY_MS] y después vuelve sola a la del usuario. null si no hay una prueba en curso.
     */
    fun trying(now: Long = System.currentTimeMillis()): Pair<PetType, Int>? {
        if (isPro || now >= tryUntil) return null
        val p = sp.getString(KEY_TRY_PET, null) ?: return null
        return PetType.from(p) to sp.getInt(KEY_TRY_COAT, 0)
    }

    val tryUntil: Long get() = sp.getLong(KEY_TRY_UNTIL, 0L)

    fun startTry(p: PetType, coat: Int) = sp.edit {
        putString(KEY_TRY_PET, p.name)
        putInt(KEY_TRY_COAT, coat)
        putLong(KEY_TRY_UNTIL, System.currentTimeMillis() + TRY_MS)
    }

    fun endTry() = sp.edit { putLong(KEY_TRY_UNTIL, 0L) }

    /** Tema de las pantallas de la app: [THEME_SYSTEM] (como el celular), [THEME_LIGHT] o [THEME_DARK]. */
    var theme: Int
        get() = sp.getInt(KEY_THEME, THEME_SYSTEM)
        set(v) = sp.edit { putInt(KEY_THEME, v) }

    /** La mascota y el pelaje que se ven flotando: los de la prueba, si hay una, o los del usuario. */
    fun shownPet(): Pair<PetType, Int> = trying() ?: pet.let { it to coat(it) }

    var tapAction: TapAction
        get() = TapAction.from(sp.getString(KEY_TAP_ACTION, null))
        set(v) = sp.edit { putString(KEY_TAP_ACTION, v.name) }

    /** true = el globo se oculta solo a los [autoHideSeconds]; false = queda fijo hasta tocarlo. */
    var autoHide: Boolean
        get() = sp.getBoolean(KEY_AUTO_HIDE, true)
        set(v) = sp.edit { putBoolean(KEY_AUTO_HIDE, v) }

    var autoHideSeconds: Int
        get() = sp.getInt(KEY_AUTO_SECONDS, 8)
        set(v) = sp.edit { putInt(KEY_AUTO_SECONDS, v) }

    /** Insistir: volver a avisar a los N minutos si no hubo respuesta (0 = no insistir). */
    var insistMinutes: Int
        get() = if (isPro) sp.getInt(KEY_INSIST_MIN, FREE_INSIST_MINUTES) else FREE_INSIST_MINUTES
        set(v) = sp.edit { putInt(KEY_INSIST_MIN, v) }

    /** Máximo de repeticiones de "insistir" por aviso. */
    var insistMax: Int
        get() = if (isPro) sp.getInt(KEY_INSIST_MAX, FREE_INSIST_MAX) else FREE_INSIST_MAX
        set(v) = sp.edit { putInt(KEY_INSIST_MAX, v) }

    var snoozeMinutes: Int
        get() = sp.getInt(KEY_SNOOZE, 10)
        set(v) = sp.edit { putInt(KEY_SNOOZE, v) }

    var voice: Boolean
        get() = isPro && sp.getBoolean(KEY_VOICE, false)
        set(v) = sp.edit { putBoolean(KEY_VOICE, v) }

    /** Hablar aunque el teléfono esté en silencio, vibración o No Molestar (por defecto, no). */
    var voiceIgnoreSilent: Boolean
        get() = sp.getBoolean(KEY_VOICE_IGNORE_SILENT, false)
        set(v) = sp.edit { putBoolean(KEY_VOICE_IGNORE_SILENT, v) }

    var petSizeDp: Int
        get() = if (isPro) sp.getInt(KEY_SIZE, DEFAULT_SIZE) else DEFAULT_SIZE
        set(v) = sp.edit { putInt(KEY_SIZE, v) }

    var calendarEnabled: Boolean
        get() = isPro && sp.getBoolean(KEY_CAL_ENABLED, false)
        set(v) = sp.edit { putBoolean(KEY_CAL_ENABLED, v) }

    var calendarLeadMinutes: Int
        get() = sp.getInt(KEY_CAL_LEAD, 10)
        set(v) = sp.edit { putInt(KEY_CAL_LEAD, v) }

    /** Calendarios elegidos; vacío = todos. */
    var calendarIds: Set<Long>
        get() = sp.getStringSet(KEY_CAL_IDS, emptySet())!!.mapNotNull { it.toLongOrNull() }.toSet()
        set(v) = sp.edit { putStringSet(KEY_CAL_IDS, v.map { it.toString() }.toSet()) }

    /** Último instante hasta el que ya se revisó el calendario (evita avisos repetidos). */
    var lastCalendarCheck: Long
        get() = sp.getLong(KEY_CAL_LAST, 0L)
        set(v) = sp.edit { putLong(KEY_CAL_LAST, v) }

    /**
     * Ya pasó por la configuración guiada. Quien usaba la app antes de que existiera (ya tiene
     * ajustes guardados) no la vuelve a ver.
     */
    var onboardingDone: Boolean
        get() = sp.getBoolean(KEY_ONBOARDING, sp.contains(KEY_ENABLED))
        set(v) = sp.edit { putBoolean(KEY_ONBOARDING, v) }

    /** Qué tan inquieta es: cada cuánto hace algo sola (0 tranquila, 1 normal, 2 inquieta). */
    var liveliness: Int
        get() = sp.getInt(KEY_LIVELINESS, LIVELY_NORMAL).coerceIn(LIVELY_CALM, LIVELY_ACTIVE)
        set(v) = sp.edit { putInt(KEY_LIVELINESS, v) }

    /** Ya vio el tutorial de lo que sabe hacer la mascota (se muestra una vez, después de la configuración). */
    var tutorialSeen: Boolean
        get() = sp.getBoolean(KEY_TUTORIAL, false)
        set(v) = sp.edit { putBoolean(KEY_TUTORIAL, v) }

    /** Se duerme si pasa [sleepIdleHours] sin avisos ni mimos. */
    var sleepWhenIdle: Boolean
        get() = sp.getBoolean(KEY_SLEEP_IDLE, true)
        set(v) = sp.edit { putBoolean(KEY_SLEEP_IDLE, v) }

    var sleepIdleHours: Int
        get() = sp.getInt(KEY_SLEEP_IDLE_HOURS, 2)
        set(v) = sp.edit { putInt(KEY_SLEEP_IDLE_HOURS, v) }

    /** Se duerme dentro del horario de sueño del usuario (minutos desde las 00:00; puede cruzar la medianoche). */
    var sleepAtBedtime: Boolean
        get() = isPro && sp.getBoolean(KEY_BEDTIME, false)
        set(v) = sp.edit { putBoolean(KEY_BEDTIME, v) }

    var bedtimeStartMin: Int
        get() = sp.getInt(KEY_BEDTIME_START, 22 * 60)
        set(v) = sp.edit { putInt(KEY_BEDTIME_START, v) }

    var bedtimeEndMin: Int
        get() = sp.getInt(KEY_BEDTIME_END, 7 * 60)
        set(v) = sp.edit { putInt(KEY_BEDTIME_END, v) }

    /**
     * Cuántos avisos sin atender repite la mascota al desbloquear: 0 = ninguno, [UNLOCK_REPEAT_ALL] = todos.
     */
    var unlockRepeat: Int
        get() = sp.getInt(KEY_UNLOCK_REPEAT, 3)
        set(v) = sp.edit { putInt(KEY_UNLOCK_REPEAT, v) }

    /** Esconderse cuando otra app está a pantalla completa (videos, juegos). */
    var hideInFullscreen: Boolean
        get() = sp.getBoolean(KEY_HIDE_FULLSCREEN, true)
        set(v) = sp.edit { putBoolean(KEY_HIDE_FULLSCREEN, v) }

    /** Opacidad de la mascota, en % (40 a 100). */
    var petOpacity: Int
        get() = if (isPro) sp.getInt(KEY_OPACITY, 100) else 100
        set(v) = sp.edit { putInt(KEY_OPACITY, v) }

    /** Modo fantasma: la mascota se ve semitransparente y los toques la atraviesan. */
    var ghostMode: Boolean
        get() = sp.getBoolean(KEY_GHOST, false) // gratis (decisión de Cowork, 28/09)
        set(v) = sp.edit { putBoolean(KEY_GHOST, v) }

    /** "Pausar 1 hora": hasta cuándo está escondida la mascota (0 = no está pausada). */
    var petPausedUntil: Long
        get() = sp.getLong(KEY_PAUSED_UNTIL, 0L)
        set(v) = sp.edit { putLong(KEY_PAUSED_UNTIL, v) }

    fun petPaused(now: Long = System.currentTimeMillis()) = petPausedUntil > now

    // ---------------------------------------------------------------- Pro
    // Sin Pro, las opciones Pro valen lo que dice el plan gratis, pero lo que el usuario eligió queda
    // guardado: si compra, vuelve todo como lo tenía.

    /** Compró Pro (se confirma con Google Play cada vez que se abre la app). */
    var proPurchased: Boolean
        get() = sp.getBoolean(KEY_PRO, false)
        set(v) = sp.edit { putBoolean(KEY_PRO, v) }

    /** Primera vez que se abrió la app: desde ahí corre la prueba gratis de Pro. */
    val firstOpenAt: Long
        get() = sp.getLong(KEY_FIRST_OPEN, 0L)

    fun markFirstOpen(now: Long = System.currentTimeMillis()) {
        if (!sp.contains(KEY_FIRST_OPEN)) sp.edit { putLong(KEY_FIRST_OPEN, now) }
    }

    val trialEndsAt: Long
        get() = firstOpenAt + TRIAL_DAYS * DAY_MS

    /** Si el reloj se atrasa más de un día respecto de la primera apertura, la prueba no cuenta. */
    fun trialActive(now: Long = System.currentTimeMillis()) =
        firstOpenAt > 0 && now >= firstOpenAt - DAY_MS && now < trialEndsAt

    val isPro: Boolean
        get() = when {
            BuildConfig.OWNER_TOOLS && ownerPlan == OWNER_PRO -> true
            BuildConfig.OWNER_TOOLS && ownerPlan == OWNER_FREE -> false
            else -> proPurchased || trialActive()
        }

    /**
     * Herramientas de dueño (solo versiones de prueba, BuildConfig.OWNER_TOOLS): forzar el plan para
     * probar. OWNER_NORMAL: lo que diga la compra y la prueba gratis.
     */
    var ownerPlan: Int
        get() = sp.getInt(KEY_OWNER_PLAN, OWNER_NORMAL)
        set(v) = sp.edit { putInt(KEY_OWNER_PLAN, v) }

    /** Lista de lo que se perdió con el celular apagado, para mostrarla al tocar el aviso resumen. */
    var missedList: List<String>
        get() = sp.getString(KEY_MISSED, "").orEmpty().split("\n").filter { it.isNotBlank() }
        set(v) = sp.edit { putString(KEY_MISSED, v.joinToString("\n") { it.replace('\n', ' ') }) }

    /** Nombre que el usuario le puso a cada mascota (null = sin nombre). */
    fun petName(p: PetType): String? = sp.getString(KEY_PET_NAME + p.name, null)?.takeIf { it.isNotBlank() }

    fun setPetName(p: PetType, name: String?) = sp.edit {
        putBoolean(KEY_PET_NAME_ASKED + p.name, true)
        if (name.isNullOrBlank()) remove(KEY_PET_NAME + p.name) else putString(KEY_PET_NAME + p.name, name.trim())
    }

    /** Ya se le preguntó el nombre de esta mascota (se pregunta una vez, la primera vez que se elige). */
    fun petNameAsked(p: PetType) = sp.getBoolean(KEY_PET_NAME_ASKED + p.name, false)

    /** Día (epochDay) en que el usuario cerró el "consejo del día" de la pantalla de Avisos. */
    var tipDismissedDay: Long
        get() = sp.getLong(KEY_TIP_DAY, -1L)
        set(v) = sp.edit { putLong(KEY_TIP_DAY, v) }

    /** Avisos de la app en silencio (tirón a la derecha): llegan sin sonido ni voz hasta otro tirón. */
    var muted: Boolean
        get() = sp.getBoolean(KEY_MUTED, false)
        set(v) = sp.edit { putBoolean(KEY_MUTED, v) }

    /** Último "latido" de la mascota (cada minuto mientras está en pantalla). */
    var petAliveAt: Long
        get() = sp.getLong(KEY_PET_ALIVE, 0L)
        set(v) = sp.edit { putLong(KEY_PET_ALIVE, v) }

    /** true si la mascota se cerró de forma normal (ocultar, pausar, apagar); false si el sistema la mató. */
    var petCleanStop: Boolean
        get() = sp.getBoolean(KEY_PET_CLEAN_STOP, true)
        set(v) = sp.edit { putBoolean(KEY_PET_CLEAN_STOP, v) }

    /**
     * Cierres inesperados de la mascota: "cuándo se cerró|cuándo volvió|cómo volvió" (watchdog, app,
     * boot). Se guardan los últimos 20 para el diagnóstico.
     */
    var petKills: List<String>
        get() = sp.getString(KEY_PET_KILLS, "").orEmpty().split("\n").filter { it.isNotBlank() }
        set(v) = sp.edit { putString(KEY_PET_KILLS, v.takeLast(20).joinToString("\n")) }

    /** Ya se avisó que la prueba de Pro termina mañana (se avisa una sola vez). */
    var trialNoticeShown: Boolean
        get() = sp.getBoolean(KEY_TRIAL_NOTICE, false)
        set(v) = sp.edit { putBoolean(KEY_TRIAL_NOTICE, v) }

    /**
     * La mascota del plan gratis ([FREE_PET_COUNT]): la que elige al instalar (Onboarding) o, si no la
     * eligió ahí, al terminar la prueba (vacío = todavía no eligió). Quien tenía 2 de antes se queda con
     * la que estaba usando.
     */
    var freePets: Set<PetType>
        get() {
            val all = sp.getStringSet(KEY_FREE_PETS, emptySet())!!.mapNotNull { n -> PetType.entries.firstOrNull { it.name == n } }
            if (all.size <= FREE_PET_COUNT) return all.toSet()
            val current = PetType.entries.firstOrNull { it.name == sp.getString(KEY_PET, null) }
            return setOf(current?.takeIf { it in all } ?: all.first())
        }
        set(v) = sp.edit { putStringSet(KEY_FREE_PETS, v.map { it.name }.toSet()) }

    /** Cuántas veces cambió la mascota gratis después de elegirla (se permite 1 gratis). */
    var freePetChanges: Int
        get() = sp.getInt(KEY_FREE_PET_CHANGES, 0)
        set(v) = sp.edit { putInt(KEY_FREE_PET_CHANGES, v) }

    /** Sin Pro, con las mascotas gratis ya elegidas y sin haber usado el cambio gratis. */
    fun canChangeFreePets() = !isPro && freePets.size >= FREE_PET_COUNT && freePetChanges < FREE_PET_CHANGES

    /** Sin Pro y sin haber elegido la mascota gratis: hay que pedírselo. */
    fun needsFreePetChoice() = !isPro && freePets.size < FREE_PET_COUNT

    /** Posición de la mascota: x desde la izquierda, y desde abajo (px). -1 = sin definir. */
    var petX: Int
        get() = sp.getInt(KEY_X, -1)
        set(v) = sp.edit { putInt(KEY_X, v) }
    var petY: Int
        get() = sp.getInt(KEY_Y, -1)
        set(v) = sp.edit { putInt(KEY_Y, v) }

    /** Tamaño de la pantalla (px) cuando se guardó la posición: para ubicarla igual al girar. 0 = no se sabe. */
    var petScreenW: Int
        get() = sp.getInt(KEY_SCREEN_W, 0)
        set(v) = sp.edit { putInt(KEY_SCREEN_W, v) }
    var petScreenH: Int
        get() = sp.getInt(KEY_SCREEN_H, 0)
        set(v) = sp.edit { putInt(KEY_SCREEN_H, v) }

    companion object {
        const val KEY_ENABLED = "enabled"
        const val KEY_ONBOARDING = "onboarding_done"
        const val KEY_TUTORIAL = "tutorial_seen"
        const val KEY_LIVELINESS = "liveliness"
        const val LIVELY_CALM = 0
        const val LIVELY_NORMAL = 1
        const val LIVELY_ACTIVE = 2
        const val KEY_PET = "pet"
        const val KEY_TAP_ACTION = "tap_action"
        const val KEY_AUTO_HIDE = "auto_hide"
        const val KEY_AUTO_SECONDS = "auto_seconds"
        const val KEY_SNOOZE = "snooze"
        const val KEY_INSIST_MIN = "insist_minutes"
        const val KEY_INSIST_MAX = "insist_max"
        const val KEY_VOICE = "voice"
        const val KEY_VOICE_IGNORE_SILENT = "voice_ignore_silent"
        const val KEY_SIZE = "size"
        const val KEY_CAL_ENABLED = "cal_enabled"
        const val KEY_CAL_LEAD = "cal_lead"
        const val KEY_CAL_IDS = "cal_ids"
        const val KEY_CAL_LAST = "cal_last"
        const val KEY_SLEEP_IDLE = "sleep_idle"
        const val KEY_SLEEP_IDLE_HOURS = "sleep_idle_hours"
        const val KEY_BEDTIME = "bedtime"
        const val KEY_BEDTIME_START = "bedtime_start"
        const val KEY_BEDTIME_END = "bedtime_end"
        val SLEEP_KEYS = setOf(KEY_SLEEP_IDLE, KEY_SLEEP_IDLE_HOURS, KEY_BEDTIME, KEY_BEDTIME_START, KEY_BEDTIME_END)
        const val KEY_UNLOCK_REPEAT = "unlock_repeat"
        const val KEY_GHOST = "ghost"
        const val KEY_OPACITY = "opacity"
        const val KEY_HIDE_FULLSCREEN = "hide_fullscreen"
        const val KEY_PAUSED_UNTIL = "paused_until"
        const val UNLOCK_REPEAT_ALL = -1
        const val KEY_PRO = "pro_purchased"
        const val KEY_OWNER_PLAN = "owner_plan"
        const val OWNER_NORMAL = 0
        const val OWNER_PRO = 1
        const val OWNER_FREE = 2
        const val KEY_FIRST_OPEN = "first_open"
        const val KEY_FREE_PETS = "free_pets"
        /** Duración de la prueba de Pro: 7 días en la prueba cerrada y 14 en la publicada (app/build.gradle.kts). */
        const val TRIAL_DAYS = BuildConfig.TRIAL_DAYS
        const val KEY_TRIAL_NOTICE = "trial_notice_shown"
        const val KEY_MISSED = "missed_list"
        const val KEY_PET_ALIVE = "pet_alive_at"
        const val KEY_MUTED = "muted"
        const val KEY_TIP_DAY = "tip_dismissed_day"
        const val KEY_PET_NAME = "pet_name_"
        const val KEY_COAT = "coat_"
        const val KEY_THEME = "theme"
        const val THEME_SYSTEM = 0
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
        const val KEY_TRY_PET = "try_pet"
        const val KEY_TRY_COAT = "try_coat"
        const val KEY_TRY_UNTIL = "try_until"
        const val TRY_MS = 5 * 60_000L
        const val KEY_PET_NAME_ASKED = "pet_name_asked_"
        const val KEY_PET_CLEAN_STOP = "pet_clean_stop"
        const val KEY_PET_KILLS = "pet_kills"
        const val DAY_MS = 24 * 3_600_000L
        const val FREE_PET_COUNT = 1
        /** Cambios gratis de las 2 mascotas después de elegirlas (decisión de Cowork, 28/09). */
        const val FREE_PET_CHANGES = 1
        const val KEY_FREE_PET_CHANGES = "free_pet_changes"
        /** "Insistencia básica" del plan gratis: cada 10 minutos, hasta 3 veces. */
        const val FREE_INSIST_MINUTES = 10
        const val FREE_INSIST_MAX = 3
        const val DEFAULT_SIZE = 72
        const val KEY_X = "x"
        const val KEY_Y = "y"
        const val KEY_SCREEN_W = "pos_screen_w"
        const val KEY_SCREEN_H = "pos_screen_h"
    }
}
