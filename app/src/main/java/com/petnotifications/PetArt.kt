package com.petnotifications

// Generado desde el lienzo "Mascotas tiernas"; no editar a mano.

/** Dibujos de cada mascota: de frente (con sus caras), de perfil, de espaldas, echada y dando la patita. */
enum class PetView { FRONT, BLINK, HAPPY, SIDE, BACK, LIE, SLEEP, PAW }

/** Las aves no se echan: se esponjan en el lugar, así que su pose dormida no cambia el encuadre. */
val PetType.liesDown: Boolean
    get() = this != PetType.DUCK && this != PetType.CHICK && this != PetType.OWL

fun PetType.art(view: PetView): Int = when (this) {
    PetType.DOG -> when (view) {
        PetView.FRONT -> R.drawable.pet_dog_front
        PetView.BLINK -> R.drawable.pet_dog_blink
        PetView.HAPPY -> R.drawable.pet_dog_happy
        PetView.SIDE -> R.drawable.pet_dog_side
        PetView.BACK -> R.drawable.pet_dog_back
        PetView.LIE -> R.drawable.pet_dog_lie
        PetView.SLEEP -> R.drawable.pet_dog_sleep
        PetView.PAW -> R.drawable.pet_dog_paw
    }
    PetType.CAT -> when (view) {
        PetView.FRONT -> R.drawable.pet_cat_front
        PetView.BLINK -> R.drawable.pet_cat_blink
        PetView.HAPPY -> R.drawable.pet_cat_happy
        PetView.SIDE -> R.drawable.pet_cat_side
        PetView.BACK -> R.drawable.pet_cat_back
        PetView.LIE -> R.drawable.pet_cat_lie
        PetView.SLEEP -> R.drawable.pet_cat_sleep
        PetView.PAW -> R.drawable.pet_cat_paw
    }
    PetType.DUCK -> when (view) {
        PetView.FRONT -> R.drawable.pet_duck_front
        PetView.BLINK -> R.drawable.pet_duck_blink
        PetView.HAPPY -> R.drawable.pet_duck_happy
        PetView.SIDE -> R.drawable.pet_duck_side
        PetView.BACK -> R.drawable.pet_duck_back
        PetView.LIE -> R.drawable.pet_duck_lie
        PetView.SLEEP -> R.drawable.pet_duck_sleep
        PetView.PAW -> R.drawable.pet_duck_paw
    }
    PetType.FROG -> when (view) {
        PetView.FRONT -> R.drawable.pet_frog_front
        PetView.BLINK -> R.drawable.pet_frog_blink
        PetView.HAPPY -> R.drawable.pet_frog_happy
        PetView.SIDE -> R.drawable.pet_frog_side
        PetView.BACK -> R.drawable.pet_frog_back
        PetView.LIE -> R.drawable.pet_frog_lie
        PetView.SLEEP -> R.drawable.pet_frog_sleep
        PetView.PAW -> R.drawable.pet_frog_paw
    }
    PetType.COW -> when (view) {
        PetView.FRONT -> R.drawable.pet_cow_front
        PetView.BLINK -> R.drawable.pet_cow_blink
        PetView.HAPPY -> R.drawable.pet_cow_happy
        PetView.SIDE -> R.drawable.pet_cow_side
        PetView.BACK -> R.drawable.pet_cow_back
        PetView.LIE -> R.drawable.pet_cow_lie
        PetView.SLEEP -> R.drawable.pet_cow_sleep
        PetView.PAW -> R.drawable.pet_cow_paw
    }
    PetType.PIG -> when (view) {
        PetView.FRONT -> R.drawable.pet_pig_front
        PetView.BLINK -> R.drawable.pet_pig_blink
        PetView.HAPPY -> R.drawable.pet_pig_happy
        PetView.SIDE -> R.drawable.pet_pig_side
        PetView.BACK -> R.drawable.pet_pig_back
        PetView.LIE -> R.drawable.pet_pig_lie
        PetView.SLEEP -> R.drawable.pet_pig_sleep
        PetView.PAW -> R.drawable.pet_pig_paw
    }
    PetType.CHICK -> when (view) {
        PetView.FRONT -> R.drawable.pet_chick_front
        PetView.BLINK -> R.drawable.pet_chick_blink
        PetView.HAPPY -> R.drawable.pet_chick_happy
        PetView.SIDE -> R.drawable.pet_chick_side
        PetView.BACK -> R.drawable.pet_chick_back
        PetView.LIE -> R.drawable.pet_chick_lie
        PetView.SLEEP -> R.drawable.pet_chick_sleep
        PetView.PAW -> R.drawable.pet_chick_paw
    }
    PetType.LION -> when (view) {
        PetView.FRONT -> R.drawable.pet_lion_front
        PetView.BLINK -> R.drawable.pet_lion_blink
        PetView.HAPPY -> R.drawable.pet_lion_happy
        PetView.SIDE -> R.drawable.pet_lion_side
        PetView.BACK -> R.drawable.pet_lion_back
        PetView.LIE -> R.drawable.pet_lion_lie
        PetView.SLEEP -> R.drawable.pet_lion_sleep
        PetView.PAW -> R.drawable.pet_lion_paw
    }
    PetType.MONKEY -> when (view) {
        PetView.FRONT -> R.drawable.pet_monkey_front
        PetView.BLINK -> R.drawable.pet_monkey_blink
        PetView.HAPPY -> R.drawable.pet_monkey_happy
        PetView.SIDE -> R.drawable.pet_monkey_side
        PetView.BACK -> R.drawable.pet_monkey_back
        PetView.LIE -> R.drawable.pet_monkey_lie
        PetView.SLEEP -> R.drawable.pet_monkey_sleep
        PetView.PAW -> R.drawable.pet_monkey_paw
    }
    PetType.OWL -> when (view) {
        PetView.FRONT -> R.drawable.pet_owl_front
        PetView.BLINK -> R.drawable.pet_owl_blink
        PetView.HAPPY -> R.drawable.pet_owl_happy
        PetView.SIDE -> R.drawable.pet_owl_side
        PetView.BACK -> R.drawable.pet_owl_back
        PetView.LIE -> R.drawable.pet_owl_lie
        PetView.SLEEP -> R.drawable.pet_owl_sleep
        PetView.PAW -> R.drawable.pet_owl_paw
    }
}
