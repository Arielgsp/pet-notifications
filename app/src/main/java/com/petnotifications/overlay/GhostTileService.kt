package com.petnotifications.overlay

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.petnotifications.Prefs

/**
 * Botón "Mascota fantasma" en los ajustes rápidos. En modo fantasma la mascota no recibe toques,
 * así que hace falta una forma de salir que no sea tocarla.
 */
class GhostTileService : TileService() {

    override fun onStartListening() = refresh()

    override fun onClick() {
        val prefs = Prefs(this)
        prefs.ghostMode = !prefs.ghostMode // el servicio de la mascota lo aplica al instante
        refresh()
    }

    private fun refresh() {
        val tile = qsTile ?: return
        val prefs = Prefs(this)
        tile.state = when {
            !prefs.petEnabled -> Tile.STATE_UNAVAILABLE
            prefs.ghostMode -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        tile.updateTile()
    }
}
