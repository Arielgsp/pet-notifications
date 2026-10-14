package com.petnotifications.overlay

import android.content.Context
import android.media.AudioAttributes
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.petnotifications.R
import java.util.Locale

/**
 * La voz de la mascota (motor de voz de Android). Todo corre en un hilo propio: el motor puede
 * tardar segundos en responder (recién arrancado, con poca memoria o colgado) y, si se lo llama
 * desde el hilo principal, congela la app entera (pasó en Android 17).
 */
class PetVoice(context: Context) {
    private val appContext = context.applicationContext
    private val thread = HandlerThread("pet-voice").apply { start() }
    private val handler = Handler(thread.looper)

    // Solo se tocan desde [thread].
    private var tts: TextToSpeech? = null
    private var ready = false
    private var pending: Utterance? = null
    private var voiceLanguage: String? = null
    @Volatile private var started = false

    private data class Utterance(val text: String, val pitch: Float, val quiet: Boolean)

    /** Arranca el motor por adelantado: la primera vez tarda y, si no, la voz llega tarde. */
    fun warmUp() = handler.post { ensureEngine() }

    /** [quiet] = teléfono en silencio/No Molestar pero el usuario pidió que hable igual. */
    fun say(text: String, pitch: Float, quiet: Boolean) = handler.post {
        val engine = tts
        if (engine == null || !ready) {
            pending = Utterance(text, pitch, quiet)
            ensureEngine()
            return@post
        }
        speakNow(engine, Utterance(text, pitch, quiet))
        // Si el sistema cerró o colgó el motor, no avisa ningún error: simplemente no habla.
        // A los 3 s sin empezar se descarta y se vuelve a arrancar para decir el sonido igual.
        handler.postDelayed({
            if (!started && tts === engine) {
                runCatching { engine.shutdown() }
                tts = null
                ready = false
                voiceLanguage = null
                pending = Utterance(text, pitch, quiet)
                ensureEngine()
            }
        }, WATCHDOG_MS)
    }

    fun release() {
        handler.post {
            runCatching { tts?.shutdown() }
            tts = null
            thread.quitSafely()
        }
    }

    private fun ensureEngine() {
        if (tts != null) return
        tts = TextToSpeech(appContext) { status ->
            // El aviso de "listo" puede llegar en otro hilo: se vuelve al de la voz.
            handler.post { onEngineReady(status) }
        }
    }

    private fun onEngineReady(status: Int) {
        val engine = tts ?: return
        if (status != TextToSpeech.SUCCESS) return
        ready = true
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { started = true }
            override fun onDone(utteranceId: String?) = Unit
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) = Unit
        })
        pending?.let { speakNow(engine, it) }
        pending = null
    }

    /**
     * Idioma de la voz = idioma de los textos de la app (R.string.tts_language), así la onomatopeya
     * se dice como está escrita ("Woof!" en inglés, "¡Guau!" en español). Se revisa antes de cada
     * frase porque el usuario puede cambiar el idioma con la app abierta.
     */
    private fun applyLanguage(engine: TextToSpeech) {
        val res = appContext.resources
        val language = res.getString(R.string.tts_language)
        if (language == voiceLanguage) return
        voiceLanguage = language
        val country = res.configuration.locales[0].country
        engine.language = Locale(language, country)
        // Una voz ya descargada y que funcione sin internet: las voces "de servidor" fallan sin
        // buena conexión (y mandarían el texto afuera del teléfono), y una voz sin descargar falla
        // en silencio. Se prefiere la del país del teléfono. Si no hay ninguna, elige el motor.
        runCatching {
            engine.voices
                ?.filter {
                    it.locale.language == language && !it.isNetworkConnectionRequired &&
                        TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features
                }
                ?.minByOrNull { if (it.locale.country == country) 0 else 1 }
                ?.let { engine.voice = it }
        }
    }

    private fun speakNow(engine: TextToSpeech, u: Utterance) {
        started = false
        applyLanguage(engine)
        engine.setAudioAttributes(attributes(u.quiet))
        engine.setPitch(u.pitch)
        engine.speak(u.text, TextToSpeech.QUEUE_FLUSH, null, "pet:${SystemClock.uptimeMillis()}")
    }

    /**
     * Normalmente la voz usa el volumen de notificaciones: si el usuario lo baja, la mascota también.
     * Si pidió hablar aunque el teléfono esté en silencio, ese volumen está en cero, así que se usa
     * el multimedia.
     */
    private fun attributes(quiet: Boolean): AudioAttributes = AudioAttributes.Builder()
        .setUsage(if (quiet) AudioAttributes.USAGE_MEDIA else AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private companion object {
        const val WATCHDOG_MS = 3_000L
    }
}
