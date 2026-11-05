package com.petnotifications

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.edit
import java.time.LocalDate
import java.time.MonthDay

/*
 * El vestidor (Design: Vestidor): accesorios que se ganan con el vínculo o se compran, y se ponen o
 * sacan. Cada uno va en un lugar (cabeza, cuello o camita) y en cada lugar va uno solo. La camita solo
 * se ve cuando la mascota está acostada o duerme. Los dibuja PetModel (OutfitArt) encima de cualquier
 * dibujo.
 */

enum class Slot { HEAD, NECK, BED }

/**
 * Temporada de un accesorio a la venta: solo se ve y se compra entre [from] y [to] (puede cruzar el
 * año, como Navidad). Lo comprado queda para siempre.
 */
enum class Season(val from: MonthDay, val to: MonthDay) {
    HALLOWEEN(MonthDay.of(10, 15), MonthDay.of(11, 2)),
    CHRISTMAS(MonthDay.of(12, 1), MonthDay.of(1, 6));

    fun isOn(day: LocalDate = LocalDate.now()): Boolean {
        val d = MonthDay.from(day)
        return if (from <= to) d in from..to else d >= from || d <= to
    }
}

/**
 * Accesorios en el orden de la grilla. Los del vínculo ([reward]) se ganan y nunca se venden; los de
 * temporada ([productId]) se compran por Google Play, con el precio de cada país que se carga en Play
 * Console (ver docs/PENDIENTES.md).
 */
enum class Accessory(
    val slot: Slot,
    @StringRes val label: Int,
    val reward: Bond.Reward? = null,
    val productId: String? = null,
    val season: Season? = null,
) {
    SCARF(Slot.NECK, R.string.acc_scarf, reward = Bond.Reward.SCARF),
    XMAS_HAT(Slot.HEAD, R.string.acc_xmas_hat, productId = "acc_xmas_hat", season = Season.CHRISTMAS),
    PUMPKIN(Slot.HEAD, R.string.acc_pumpkin, productId = "acc_pumpkin", season = Season.HALLOWEEN),
    PUMPKIN_HELMET(Slot.HEAD, R.string.acc_pumpkin_helmet, productId = "acc_pumpkin_helmet", season = Season.HALLOWEEN),
    HAT(Slot.HEAD, R.string.acc_hat, reward = Bond.Reward.HAT),
    BED(Slot.BED, R.string.acc_bed, reward = Bond.Reward.BED),
    CROWN(Slot.HEAD, R.string.acc_crown, reward = Bond.Reward.CROWN);

    /** Nivel del vínculo en el que se gana (0 = se compra). */
    val level: Int get() = reward?.let { r -> Bond.LEVELS.first { it.reward == r }.number } ?: 0

    val forSale: Boolean get() = productId != null

    companion object {
        fun of(reward: Bond.Reward) = entries.firstOrNull { it.reward == reward }
        fun ofProduct(id: String) = entries.firstOrNull { it.productId == id }
        val forSale get() = entries.filter { it.forSale }
    }
}

/** Lo que tiene puesto: uno por lugar (o nada). */
data class Outfit(val head: Accessory? = null, val neck: Accessory? = null, val bed: Accessory? = null) {

    operator fun get(slot: Slot) = when (slot) {
        Slot.HEAD -> head
        Slot.NECK -> neck
        Slot.BED -> bed
    }

    fun isEmpty() = head == null && neck == null && bed == null

    fun wearing(a: Accessory) = get(a.slot) == a

    /** Se lo pone (y saca lo que había en ese lugar). */
    fun with(a: Accessory) = when (a.slot) {
        Slot.HEAD -> copy(head = a)
        Slot.NECK -> copy(neck = a)
        Slot.BED -> copy(bed = a)
    }

    fun without(slot: Slot) = when (slot) {
        Slot.HEAD -> copy(head = null)
        Slot.NECK -> copy(neck = null)
        Slot.BED -> copy(bed = null)
    }

    fun toggle(a: Accessory) = if (wearing(a)) without(a.slot) else with(a)

    companion object {
        val NONE = Outfit()

        /** Empieza con el prefijo del vínculo: la app y la mascota ya escuchan esos cambios. */
        const val KEY = Bond.KEY_PREFIX + "outfit"

        /** Comprados según Google Play (ProBilling lo reemplaza entero en cada consulta). */
        const val KEY_BOUGHT = Bond.KEY_PREFIX + "bought"

        /** Comprados de mentira con las herramientas de dueño (aparte, para que Play no los borre). */
        const val KEY_OWNER_BOUGHT = Bond.KEY_PREFIX + "owner_bought"

        fun bought(ctx: Context): Set<Accessory> {
            val sp = Prefs(ctx).sp
            val ids = sp.getStringSet(KEY_BOUGHT, emptySet()).orEmpty() +
                if (BuildConfig.OWNER_TOOLS) sp.getStringSet(KEY_OWNER_BOUGHT, emptySet()).orEmpty() else emptySet()
            return ids.mapNotNull(Accessory::ofProduct).toSet()
        }

        fun setBought(ctx: Context, items: Set<Accessory>) = Prefs(ctx).sp.edit {
            putStringSet(KEY_BOUGHT, items.mapNotNull { it.productId }.toSet())
        }

        fun ownerBuy(ctx: Context, a: Accessory) = Prefs(ctx).sp.let { sp ->
            sp.edit { putStringSet(KEY_OWNER_BOUGHT, sp.getStringSet(KEY_OWNER_BOUGHT, emptySet()).orEmpty() + a.productId!!) }
        }

        fun ownerClearBought(ctx: Context) = Prefs(ctx).sp.edit { remove(KEY_OWNER_BOUGHT) }

        /** Herramientas de dueño: mostrar los de todas las temporadas, para probarlos fuera de fecha. */
        const val KEY_OWNER_SEASONS = Bond.KEY_PREFIX + "owner_seasons"

        fun ownerAllSeasons(ctx: Context) = BuildConfig.OWNER_TOOLS && Prefs(ctx).sp.getBoolean(KEY_OWNER_SEASONS, false)

        fun setOwnerAllSeasons(ctx: Context, on: Boolean) = Prefs(ctx).sp.edit { putBoolean(KEY_OWNER_SEASONS, on) }

        /** A la venta ahora: en su temporada (o todas, con las herramientas de dueño). */
        fun onSale(ctx: Context, a: Accessory) = a.forSale && (a.season?.isOn() != false || ownerAllSeasons(ctx))

        /** Lo tiene: ganado con el vínculo o comprado. */
        fun owns(ctx: Context, a: Accessory, level: Int = Bond.level(ctx).number, bought: Set<Accessory> = bought(ctx)) =
            if (a.forSale) a in bought else a.level <= level

        /** Lo que tiene puesto, solo con lo que todavía tiene (si el vínculo vuelve a cero, se lo saca). */
        fun current(ctx: Context): Outfit {
            val level = Bond.level(ctx).number
            val bought = bought(ctx)
            val worn = Prefs(ctx).sp.getStringSet(KEY, emptySet()).orEmpty()
                .mapNotNull { name -> Accessory.entries.firstOrNull { it.name == name } }
                .filter { owns(ctx, it, level, bought) }
            return worn.fold(NONE) { o, a -> o.with(a) }
        }

        fun save(ctx: Context, outfit: Outfit) = Prefs(ctx).sp.edit {
            putStringSet(KEY, listOfNotNull(outfit.head, outfit.neck, outfit.bed).map { it.name }.toSet())
        }

        /** Recién ganado o comprado: se lo pone enseguida, para que se vea. */
        fun wear(ctx: Context, a: Accessory) = save(ctx, current(ctx).with(a))
    }
}
