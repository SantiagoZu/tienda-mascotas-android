package co.edu.uniminuto.tiendamascotas.ui.galeria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniminuto.tiendamascotas.R
import co.edu.uniminuto.tiendamascotas.player.VideoPlayer
import co.edu.uniminuto.tiendamascotas.ui.common.EstadoCargando
import co.edu.uniminuto.tiendamascotas.ui.common.EstadoError
import co.edu.uniminuto.tiendamascotas.ui.common.UiState
import co.edu.uniminuto.tiendamascotas.ui.common.formatearPrecio

/**
 * Pantalla que reproduce un video de la galería dentro de la app con ExoPlayer.
 * Si ocurre un error de reproducción se muestra un mensaje con opción de reintentar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReproductorVideoScreen(
    onVolver: () -> Unit,
    viewModel: ReproductorVideoViewModel = viewModel(factory = ReproductorVideoViewModel.Factory),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var errorReproduccion by remember { mutableStateOf<String?>(null) }
    var intento by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text((estado as? UiState.Exito)?.datos?.nombre ?: stringResource(R.string.video))
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.volver))
                    }
                },
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(padding).fillMaxSize()
        when (val actual = estado) {
            UiState.Cargando -> EstadoCargando(modifier)
            is UiState.Error -> EstadoError(actual.mensaje, viewModel::cargar, modifier)
            is UiState.Exito -> Column(
                modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val video = actual.datos
                // Ocupa el ancho disponible, pero en horizontal se limita por la altura restante.
                Box(
                    Modifier.weight(1f, fill = false).widthIn(max = 720.dp).aspectRatio(16f / 9f)
                        .background(Color.Black),
                ) {
                    val error = errorReproduccion
                    if (error == null) {
                        // Cambiar `intento` recrea el reproductor para reintentar tras un error.
                        key(intento) {
                            VideoPlayer(
                                videoRes = video.videoRes,
                                modifier = Modifier.fillMaxSize(),
                                onError = { errorReproduccion = it },
                            )
                        }
                    } else {
                        EstadoError(
                            mensaje = stringResource(R.string.error_reproduccion, error),
                            onReintentar = { errorReproduccion = null; intento++ },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        )
                    }
                }
                Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                    Text(video.nombre, style = MaterialTheme.typography.headlineSmall)
                    Text(video.detalle, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        video.estado.etiqueta + (video.precio?.let { " · " + formatearPrecio(it) } ?: ""),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
