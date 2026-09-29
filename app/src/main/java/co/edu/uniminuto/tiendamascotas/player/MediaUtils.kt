package co.edu.uniminuto.tiendamascotas.player

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.annotation.RawRes
import androidx.media3.common.PlaybackException

/**
 * Construye el [Uri] `android.resource://` de un recurso de res/raw, que ExoPlayer
 * lee mediante su `DefaultDataSource` sin necesidad de internet.
 */
fun uriDeRecursoRaw(context: Context, @RawRes resId: Int): Uri =
    Uri.Builder()
        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
        .authority(context.packageName)
        .appendPath(resId.toString())
        .build()

/** Traduce un [PlaybackException] a un mensaje comprensible para el usuario. */
fun PlaybackException.mensajeUsuario(): String = when (errorCode) {
    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "el archivo no existe"
    PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED,
    PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED -> "el archivo está dañado"
    PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
    PlaybackException.ERROR_CODE_DECODING_FAILED,
    PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED -> "formato no soportado por el dispositivo"
    else -> localizedMessage ?: errorCodeName
}

/** Formatea milisegundos como `m:ss` (por ejemplo `1:05`). */
fun formatearTiempo(ms: Long): String {
    val totalSeg = (ms.coerceAtLeast(0L) / 1000L)
    return "%d:%02d".format(totalSeg / 60, totalSeg % 60)
}
