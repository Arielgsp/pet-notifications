package com.petnotifications

import android.content.Context
import androidx.annotation.StringRes

/**
 * Mascotas disponibles. [mouthX]/[mouthY] son la posición aproximada de la boca/hocico
 * dentro del dibujo de frente (0..1), desde donde sale la onomatopeya. El [emoji] queda
 * solo para textos (notificación, aviso de prueba); en pantalla se usan los dibujos de [art].
 * El nombre y la onomatopeya salen de strings.xml, así cambian con el idioma del teléfono.
 */
enum class PetType(
    val emoji: String,
    @StringRes val nameRes: Int,
    @StringRes val soundRes: Int,
    val mouthX: Float,
    val mouthY: Float,
    val voicePitch: Float,
) {
    DOG("🐶", R.string.pet_dog, R.string.pet_dog_sound, 0.5f, 0.53f, 0.9f),
    CAT("🐱", R.string.pet_cat, R.string.pet_cat_sound, 0.5f, 0.5f, 1.3f),
    DUCK("🦆", R.string.pet_duck, R.string.pet_duck_sound, 0.5f, 0.52f, 1.4f),
    FROG("🐸", R.string.pet_frog, R.string.pet_frog_sound, 0.5f, 0.52f, 0.7f),
    COW("🐮", R.string.pet_cow, R.string.pet_cow_sound, 0.5f, 0.56f, 0.6f),
    PIG("🐷", R.string.pet_pig, R.string.pet_pig_sound, 0.5f, 0.55f, 1.1f),
    CHICK("🐤", R.string.pet_chick, R.string.pet_chick_sound, 0.5f, 0.55f, 1.8f),
    LION("🦁", R.string.pet_lion, R.string.pet_lion_sound, 0.5f, 0.53f, 0.5f),
    MONKEY("🐵", R.string.pet_monkey, R.string.pet_monkey_sound, 0.5f, 0.55f, 1.2f),
    OWL("🦉", R.string.pet_owl, R.string.pet_owl_sound, 0.5f, 0.52f, 0.8f),
    /** Sin dibujos fijos: todas sus vistas salen del modelo 2,5D (petPainter). */
    RABBIT("🐰", R.string.pet_rabbit, R.string.pet_rabbit_sound, 0.5f, 0.54f, 1.5f);

    fun label(ctx: Context): String = ctx.getString(nameRes)

    /** Su nombre ("Luna") o, si no tiene, "Tu perro": para empezar una oración. */
    fun subject(ctx: Context): String =
        Prefs(ctx).petName(this) ?: ctx.getString(R.string.pet_your, label(ctx).lowercase())

    /** Su nombre ("Luna") o, si no tiene, "tu perro": para el medio de una oración ("Soy tu perro"). */
    fun mention(ctx: Context): String =
        Prefs(ctx).petName(this) ?: ctx.getString(R.string.pet_your_lower, label(ctx).lowercase())
    fun sound(ctx: Context): String = ctx.getString(soundRes)

    companion object {
        fun from(name: String?) = entries.firstOrNull { it.name == name } ?: DOG
    }
}

/** Qué pasa al tocar el globo de texto. */
enum class TapAction(@StringRes val labelRes: Int, @StringRes val descriptionRes: Int) {
    OPEN(R.string.tap_open, R.string.tap_open_desc),
    DELETE(R.string.tap_delete, R.string.tap_delete_desc),
    SNOOZE(R.string.tap_snooze, R.string.tap_snooze_desc),
    DISMISS(R.string.tap_dismiss, R.string.tap_dismiss_desc);

    companion object {
        fun from(name: String?) = entries.firstOrNull { it.name == name } ?: OPEN
    }
}

enum class Repeat(@StringRes val labelRes: Int) {
    NONE(R.string.repeat_none), DAILY(R.string.repeat_daily), WEEKLY(R.string.repeat_weekly);

    companion object {
        fun from(name: String?) = entries.firstOrNull { it.name == name } ?: NONE
    }
}
