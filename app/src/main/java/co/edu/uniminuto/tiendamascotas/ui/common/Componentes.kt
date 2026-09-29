package co.edu.uniminuto.tiendamascotas.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.edu.uniminuto.tiendamascotas.R
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.text.NumberFormat
import java.util.Locale

/** Indicador de carga centrado. */
@Composable
fun EstadoCargando(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.cargando), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * Mensaje de error centrado con botón para reintentar.
 */
@Composable
fun EstadoError(mensaje: String, onReintentar: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Filled.Warning, contentDescription = null,
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp),
            )
            Text(mensaje, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onReintentar) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text(stringResource(R.string.reintentar), Modifier.padding(start = 8.dp))
            }
        }
    }
}

/**
 * Imagen local cargada con Coil. Se decodifica a un tamaño acotado ([tamanoPx]) para
 * ahorrar memoria, con marcador de posición y una imagen de respaldo si falla.
 */
@Composable
fun ImagenRecurso(
    @DrawableRes imagenRes: Int,
    descripcion: String?,
    modifier: Modifier = Modifier,
    tamanoPx: Int = 400,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val solicitud = remember(imagenRes, tamanoPx) {
        ImageRequest.Builder(context)
            .data(imagenRes)
            .size(tamanoPx)
            .crossfade(true)
            .build()
    }
    AsyncImage(
        model = solicitud,
        contentDescription = descripcion,
        contentScale = contentScale,
        placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
        error = painterResource(R.drawable.ic_pets),
        modifier = modifier,
    )
}

private val formatoCop: NumberFormat =
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

/** Formatea un valor en pesos colombianos, por ejemplo `$ 145.900`. */
fun formatearPrecio(valor: Long): String = synchronized(formatoCop) { formatoCop.format(valor) }
