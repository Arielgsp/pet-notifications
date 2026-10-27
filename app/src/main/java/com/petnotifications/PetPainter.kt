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
 */
@Composable
fun petPainter(pet: PetType, view: PetView, coat: Int = 0): Painter {
    val res = if (coat == 0) pet.art(view) else null
    if (res != null) return painterResource(res)
    val density = LocalDensity.current.density
    return remember(pet, view, coat, density) { ModelPainter(pet, view, coat, density) }
}

private class ModelPainter(val pet: PetType, val view: PetView, val coat: Int, density: Float) : Painter() {
    override val intrinsicSize = Size(256f * density, 240f * density)
    override fun DrawScope.onDraw() = PetModel.drawView(this, pet, view, coat)
}
