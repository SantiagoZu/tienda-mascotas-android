package co.edu.uniminuto.tiendamascotas.player

import androidx.annotation.OptIn
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Reproductor de video embebido en Compose mediante [AndroidView] + [PlayerView] de Media3.
 *
 * Gestión de ciclo de vida:
 * - `ON_STOP` → pausa el video (la app pasa a segundo plano).
 * - Al salir de la composición → libera el [ExoPlayer] en `onDispose`.
 *
 * @param videoRes video local en res/raw.
 * @param onError se invoca con un mensaje legible cuando ExoPlayer reporta un error.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    @RawRes videoRes: Int,
    modifier: Modifier = Modifier,
    reproducirAlIniciar: Boolean = true,
    onError: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onErrorActual by rememberUpdatedState(onError)

    val exoPlayer = remember(videoRes) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uriDeRecursoRaw(context, videoRes)))
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = reproducirAlIniciar
            prepare()
        }
    }

    DisposableEffect(exoPlayer, lifecycleOwner) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                onErrorActual(error.mensajeUsuario())
            }
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) exoPlayer.pause()
        }
        exoPlayer.addListener(listener)
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                keepScreenOn = true
            }
        },
        update = { view -> view.player = exoPlayer },
        onRelease = { view -> view.player = null },
        modifier = modifier,
    )
}
