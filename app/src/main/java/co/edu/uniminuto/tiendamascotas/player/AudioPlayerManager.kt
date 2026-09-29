package co.edu.uniminuto.tiendamascotas.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import co.edu.uniminuto.tiendamascotas.data.model.AudioCuidado
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Estado observable del reproductor de audio.
 *
 * @property audioActual audio cargado (o `null` si aún no se ha elegido ninguno).
 * @property posicionMs posición actual de reproducción.
 * @property duracionMs duración total conocida del audio (0 mientras se prepara).
 * @property error mensaje de error pendiente de mostrar al usuario.
 */
data class EstadoReproductorAudio(
    val audioActual: AudioCuidado? = null,
    val reproduciendo: Boolean = false,
    val cargando: Boolean = false,
    val posicionMs: Long = 0L,
    val duracionMs: Long = 0L,
    val error: String? = null,
)

/**
 * Reproductor de audio **único y compartido** de la app, basado en Media3 ExoPlayer.
 *
 * - El [ExoPlayer] se crea de forma perezosa al reproducir por primera vez.
 * - [liberar] libera el decodificador y la memoria (por ejemplo en `onStop`), pero conserva
 *   el audio y la posición para poder reanudar después con [alternarReproduccion].
 * - Gestiona el foco de audio y pausa cuando se desconectan los audífonos.
 *
 * Debe usarse desde el hilo principal.
 */
class AudioPlayerManager(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var player: ExoPlayer? = null
    private var trabajoProgreso: Job? = null

    private val _estado = MutableStateFlow(EstadoReproductorAudio())

    /** Estado actual del reproductor para la UI. */
    val estado: StateFlow<EstadoReproductorAudio> = _estado.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _estado.update { it.copy(reproduciendo = isPlaying) }
            if (isPlaying) iniciarProgreso() else detenerProgreso()
            actualizarPosicion()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _estado.update { it.copy(cargando = playbackState == Player.STATE_BUFFERING) }
            if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                actualizarPosicion()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            detenerProgreso()
            _estado.update {
                it.copy(reproduciendo = false, cargando = false, error = error.mensajeUsuario())
            }
        }
    }

    /** Carga y reproduce [audio] desde el inicio (reemplaza el audio anterior). */
    fun reproducir(audio: AudioCuidado) {
        _estado.update {
            EstadoReproductorAudio(audioActual = audio, cargando = true, duracionMs = audio.duracionSeg * 1000L)
        }
        prepararPlayer(audio, posicionMs = 0L, reproducir = true)
    }

    /** Alterna entre reproducir y pausar el audio actual. */
    fun alternarReproduccion() {
        val audio = _estado.value.audioActual ?: return
        val p = player
        if (p == null) {
            // El reproductor fue liberado (p. ej. la app pasó a segundo plano): se recrea.
            prepararPlayer(audio, _estado.value.posicionMs, reproducir = true)
            return
        }
        when {
            p.isPlaying -> p.pause()
            p.playbackState == Player.STATE_ENDED -> { p.seekTo(0L); p.play() }
            p.playbackState == Player.STATE_IDLE -> { p.prepare(); p.play() } // tras un error
            else -> p.play()
        }
    }

    /** Mueve la reproducción a [posicionMs]. */
    fun buscar(posicionMs: Long) {
        player?.seekTo(posicionMs)
        _estado.update { it.copy(posicionMs = posicionMs) }
    }

    /** Pausa la reproducción si está sonando. */
    fun pausar() {
        player?.pause()
    }

    /** Marca el error actual como ya mostrado. */
    fun limpiarError() {
        _estado.update { it.copy(error = null) }
    }

    /**
     * Libera el [ExoPlayer] y sus recursos nativos. Se conserva el audio actual y la posición
     * para poder reanudar más tarde.
     */
    fun liberar() {
        val p = player ?: return
        detenerProgreso()
        _estado.update { it.copy(posicionMs = p.currentPosition, reproduciendo = false, cargando = false) }
        p.removeListener(listener)
        p.release()
        player = null
    }

    private fun prepararPlayer(audio: AudioCuidado, posicionMs: Long, reproducir: Boolean) {
        val p = player ?: crearPlayer().also { player = it }
        p.setMediaItem(MediaItem.fromUri(uriDeRecursoRaw(appContext, audio.audioRes)), posicionMs)
        p.prepare()
        p.playWhenReady = reproducir
    }

    private fun crearPlayer(): ExoPlayer =
        ExoPlayer.Builder(appContext)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { it.addListener(listener) }

    private fun actualizarPosicion() {
        val p = player ?: return
        val duracion = p.duration.takeIf { it != C.TIME_UNSET && it > 0 }
        _estado.update {
            it.copy(posicionMs = p.currentPosition, duracionMs = duracion ?: it.duracionMs)
        }
    }

    /** Actualiza la barra de progreso cada 250 ms solo mientras hay reproducción. */
    private fun iniciarProgreso() {
        if (trabajoProgreso?.isActive == true) return
        trabajoProgreso = scope.launch {
            while (isActive) {
                actualizarPosicion()
                delay(250L)
            }
        }
    }

    private fun detenerProgreso() {
        trabajoProgreso?.cancel()
        trabajoProgreso = null
    }
}
