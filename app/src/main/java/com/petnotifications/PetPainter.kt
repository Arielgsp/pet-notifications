package com.petnotifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.petnotifications.overlay.PetModel

/**
 * Dibujo de la mascota en una vista y un pelaje: con el pelaje original, el dibujo fijo de siempre
 * ([art]); con otro pelaje, o si la mascota no tiene dibujos fijos (el conejo), el mismo dibujo hecho
 * con el modelo 2,5D (PetModel.drawView). Mismo tamaño y encuadre que los fijos (256×240).
 * Con [outfit], además lo que tiene puesto (la camita detrás, lo demás encima); si eso reemplaza una pieza
 * del dibujo (la cáscara del pollito, la campana de la vaca), va el dibujo sin esa pieza.
 */
@Composable
fun petPainter(pet: PetType, view: PetView, coat: Int = 0, outfit: Outfit = Outfit.NONE): Painter {
    val res = when {
        coat != 0 -> null
        PetModel.coversArt(pet, view, outfit) -> pet.plainArt(view)
        else -> pet.art(view)
    }
    val density = LocalDensity.current.density
    val base = if (res != null) painterResource(res) else remember(pet, view, coat, density, outfit) { ModelPainter(pet, view, coat, density, outfit) }
    if (outfit.isEmpty()) return base
    return remember(base, pet, view, outfit) { DressedPainter(base, pet, view, outfit, fixedArt = res != null) }
}

private class DressedPainter(val base: Painter, val pet: PetType, val view: PetView, val outfit: Outfit, val fixedArt: Boolean) : Painter() {
    override val intrinsicSize = base.intrinsicSize
    override fun DrawScope.onDraw() {
        PetModel.drawOutfitBehind(this, pet, view, outfit)
        with(base) { draw(size) }
        PetModel.drawOutfitFront(this, pet, view, outfit, fixedArt)
        if (outfit.toy != null) PetModel.drawToy(this, pet)
    }
}

/**
 * Solo [a], para verlo transparente encima de la mascota (el próximo premio del camino, "¿para mí?"). Mismo
 * encuadre que [petPainter] con los mismos [pet], [view], [coat] y lo que tiene puesto ([worn]).
 */
@Composable
fun accessoryPainter(pet: PetType, view: PetView, coat: Int, worn: Outfit, a: Accessory): Painter {
    val density = LocalDensity.current.density
    val fixedArt = coat == 0 && (if (PetModel.coversArt(pet, view, worn)) pet.plainArt(view) else pet.art(view)) != null
    return remember(pet, view, coat, a, density) { AccessoryPainter(pet, view, Outfit.NONE.with(a), fixedArt, density) }
}

private class AccessoryPainter(val pet: PetType, val view: PetView, val outfit: Outfit, val fixedArt: Boolean, density: Float) : Painter() {
    override val intrinsicSize = Size(256f * density, 240f * density)
    override fun DrawScope.onDraw() {
        PetModel.drawOutfitFront(this, pet, view, outfit, fixedArt)
        if (outfit.toy != null) PetModel.drawToy(this, pet)
    }
}

private class ModelPainter(val pet: PetType, val view: PetView, val coat: Int, density: Float, val outfit: Outfit) : Painter() {
    override val intrinsicSize = Size(256f * density, 240f * density)
    override fun DrawScope.onDraw() = PetModel.drawView(this, pet, view, coat, outfit)
}
