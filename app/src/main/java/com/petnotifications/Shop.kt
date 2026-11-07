package com.petnotifications

/**
 * Lo que se vende aparte de Pro. En el lanzamiento solo se vende Pro (decisión del 07/11/2026): lo demás
 * queda en el código, escondido con estos interruptores, para sacarlo en una actualización. Al pasar uno a
 * true falta cargar sus productos en Play Console (docs/PENDIENTES.md, X13 y X18). El pack de animales
 * nuevos tiene su propio interruptor: PetType.WILD_PACK_OUT.
 */
object Shop {
    /**
     * Packs de pelajes, uno por mascota (Prefs.coatPackId). Apagado: no se ven en ningún lado y todas las
     * mascotas usan el pelaje original, también durante la prueba de Pro.
     */
    const val COATS_FOR_SALE = false

    /** Accesorios de temporada (gorro navideño, calabacita, casco de calabaza): ni se ofrecen ni se prueban. */
    const val SEASONAL_FOR_SALE = false
}
