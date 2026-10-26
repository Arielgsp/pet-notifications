package com.petnotifications.overlay

import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.petnotifications.PetType

/** Un aviso que la mascota tiene que dar. */
data class PetAlert(
    val key: String,
    val title: String,
    val body: String,
    val source: Source,
    val reminderId: Long? = null,
    val eventId: Long? = null,
    val eventBegin: Long? = null,
    val repeating: Boolean = false,
    /** Fin del evento (solo calendario, no se guarda): para saber si un aviso atrasado ya terminó. */
    val eventEnd: Long? = null,
) {
    /** APP: mensajes de la propia app dichos por la mascota (por ejemplo, que termina la prueba de Pro). */
    enum class Source { LOCAL, CALENDAR, TEST, APP }
}

sealed interface FxPhase {
    data object None : FxPhase
    /** La onomatopeya saliendo de la boca. */
    data class Sound(val text: String, val id: Long) : FxPhase
    /** El globo de cómic con el aviso. */
    data class Bubble(val alert: PetAlert, val autoHideMs: Long?, val hint: String) : FxPhase
    /** Panel con los próximos avisos (tirón hacia la izquierda sobre la mascota). */
    data class Upcoming(val items: List<UpcomingItem>, val id: Long) : FxPhase
}

/** Un aviso futuro para el panel: recordatorio propio o evento del calendario. */
data class UpcomingItem(val at: Long, val title: String, val calendar: Boolean)

/** Estado observable por las vistas Compose de la mascota; solo se modifica desde el hilo principal. */
class OverlayState {
    var pet by mutableStateOf(PetType.DOG)
    /** Cada incremento: se da una vuelta despacito (frente → perfil → espalda → perfil → frente). */
    var spinTick by mutableIntStateOf(0)
    var bounceTick by mutableIntStateOf(0)
    var talking by mutableStateOf(false)
    /** Echada durmiendo (hora de dormir o mucho rato sin avisos). */
    var sleeping by mutableStateOf(false)
    var phase by mutableStateOf<FxPhase>(FxPhase.None)
    /** Posición x (px) dentro de la ventana de efectos adonde apunta el globo / sale el sonido. */
    var anchorX by mutableFloatStateOf(0f)
    /**
     * true: no hay lugar arriba de la mascota (está cerca del borde superior), así que el sonido,
     * el globo y los corazones van debajo y la colita del globo apunta hacia arriba.
     */
    var fxBelow by mutableStateOf(false)
    /**
     * Si arriba no entra, lo que dice la mascota va al costado: 1 = la ventana de efectos está a la
     * derecha de la mascota, -1 = a la izquierda, 0 = arriba o abajo (ver [fxBelow]).
     */
    var fxSide by mutableIntStateOf(0)
    /** Al costado: altura (px, desde arriba de la ventana de efectos) de la cabeza de la mascota. */
    var anchorY by mutableFloatStateOf(0f)
    val hearts = mutableStateListOf<Long>()
    /** "+1" del vínculo que sube desde la mascota al marcar "Listo". */
    val plusOnes = mutableStateListOf<Long>()
    /** Truco a hacer; cada incremento de [trickTick] lo dispara (aunque sea el mismo). */
    var trick by mutableStateOf(Trick.WAG)
    var trickTick by mutableIntStateOf(0)
    /** Tarjeta flotante arriba de la pantalla (festejo del vínculo o saludo de buenos días). */
    var card by mutableStateOf<BondCard?>(null)

    fun play(t: Trick) {
        trick = t
        trickTick++
    }
}

/**
 * Trucos que aprende con el vínculo (WAG, PAW, DANCE; CHEER: contenta con un rebote, para mostrar un
 * premio) y cosas que hace sola cada tanto para parecer viva (LOOK a HEAD; HEAD: gira solo la cabeza).
 */
enum class Trick { WAG, PAW, DANCE, CHEER, LOOK, PEEK, STRETCH, YAWN, SIT, HOP, SHAKE, HEAD }

/** Tarjetas de [OverlayState.card]. */
sealed interface BondCard {
    val id: Long

    /** Festejo: chip "Nivel 3 · Amigos", título, cómo se usa y dos botones. */
    data class Celebrate(
        override val id: Long,
        val chip: String,
        val title: String,
        val body: String,
        val primary: String,
        val secondary: String,
    ) : BondCard

    /** "¡Buen día! Hoy tenemos 3 cosas." Tocarla abre la app. */
    data class Morning(override val id: Long, val text: String) : BondCard
}

/**
 * Las ventanas de un Service no tienen Lifecycle; Compose lo necesita para recomponer,
 * así que se lo damos a mano.
 */
class OverlayOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val registry = LifecycleRegistry(this)
    private val savedState = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry
    override val viewModelStore = ViewModelStore()

    fun start() {
        savedState.performRestore(null)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun stop() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }

    fun attach(view: View) {
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
        view.setViewTreeViewModelStoreOwner(this)
    }
}
