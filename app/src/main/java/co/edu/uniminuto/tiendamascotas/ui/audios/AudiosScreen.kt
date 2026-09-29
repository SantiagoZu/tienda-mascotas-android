package co.edu.uniminuto.tiendamascotas.ui.audios

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniminuto.tiendamascotas.R
import co.edu.uniminuto.tiendamascotas.data.model.AudioCuidado
import co.edu.uniminuto.tiendamascotas.player.EstadoReproductorAudio
import co.edu.uniminuto.tiendamascotas.player.formatearTiempo
import co.edu.uniminuto.tiendamascotas.ui.common.EstadoCargando
import co.edu.uniminuto.tiendamascotas.ui.common.EstadoError
import co.edu.uniminuto.tiendamascotas.ui.common.UiState

/**
 * Pantalla de audios de cuidado con un reproductor sencillo (play/pausa, barra de progreso
 * y tiempo). Usa un único ExoPlayer compartido:
 * - se **pausa** al salir de la pantalla (onDispose),
 * - se **libera** cuando la app pasa a segundo plano (`ON_STOP`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiosScreen(
    anchoVentana: WindowWidthSizeClass,
    viewModel: AudiosViewModel = viewModel(factory = AudiosViewModel.Factory),
) {
    val audios by viewModel.audios.collectAsStateWithLifecycle()
    val reproductor by viewModel.reproductorEstado.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP) viewModel.liberarRecursos()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.pausar()
        }
    }

    // Errores de ExoPlayer (Player.Listener.onPlayerError) → mensaje al usuario.
    val textoError = reproductor.error?.let { stringResource(R.string.error_reproduccion, it) }
    LaunchedEffect(textoError) {
        if (textoError != null) {
            snackbar.showSnackbar(textoError)
            viewModel.limpiarError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_audios)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val modifier = Modifier.padding(padding).fillMaxSize()
        when (val lista = audios) {
            UiState.Cargando -> EstadoCargando(modifier)
            is UiState.Error -> EstadoError(lista.mensaje, viewModel::cargar, modifier)
            is UiState.Exito -> {
                val listaAudios: @Composable (Modifier) -> Unit = { mod ->
                    ListaAudios(lista.datos, reproductor, viewModel::seleccionarAudio, mod)
                }
                val panel: @Composable (Modifier) -> Unit = { mod ->
                    PanelReproductor(
                        estado = reproductor,
                        onAlternar = viewModel::alternarReproduccion,
                        onBuscar = viewModel::buscar,
                        modifier = mod,
                    )
                }
                if (anchoVentana == WindowWidthSizeClass.Expanded) {
                    Row(modifier) {
                        listaAudios(Modifier.weight(1f))
                        Box(Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
                            panel(Modifier.padding(24.dp))
                        }
                    }
                } else {
                    Column(modifier) {
                        listaAudios(Modifier.weight(1f))
                        panel(Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ListaAudios(
    audios: List<AudioCuidado>,
    estado: EstadoReproductorAudio,
    onClick: (AudioCuidado) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(audios, key = { it.id }) { audio ->
            val esActual = estado.audioActual?.id == audio.id
            val sonando = esActual && estado.reproduciendo
            ListItem(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick(audio) },
                colors = ListItemDefaults.colors(
                    containerColor = if (esActual) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                ),
                leadingContent = { AvatarAnimal(audio.tipo.emoji) },
                headlineContent = { Text(audio.titulo) },
                supportingContent = { Text(audio.descripcion, maxLines = 2) },
                trailingContent = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = iconoPlayPausa(sonando),
                            contentDescription = stringResource(
                                if (sonando) R.string.pausar else R.string.reproducir,
                            ),
                        )
                        Text(
                            formatearTiempo(audio.duracionSeg * 1000L),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun AvatarAnimal(emoji: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 24.sp)
        }
    }
}

@Composable
private fun iconoPlayPausa(reproduciendo: Boolean): Painter =
    if (reproduciendo) painterResource(R.drawable.ic_pause) else rememberVectorPainter(Icons.Filled.PlayArrow)

/**
 * Panel del reproductor: título, barra de progreso desplazable, tiempos y botón play/pausa.
 */
@Composable
private fun PanelReproductor(
    estado: EstadoReproductorAudio,
    onAlternar: () -> Unit,
    onBuscar: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        val audio = estado.audioActual
        if (audio == null) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.ic_headphones), contentDescription = null)
                Text(stringResource(R.string.seleccione_audio), Modifier.padding(start = 12.dp))
            }
        } else {
            ControlesReproductor(audio, estado, onAlternar, onBuscar)
        }
    }
}

@Composable
private fun ControlesReproductor(
    audio: AudioCuidado,
    estado: EstadoReproductorAudio,
    onAlternar: () -> Unit,
    onBuscar: (Long) -> Unit,
) {
    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarAnimal(audio.tipo.emoji)
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(audio.titulo, style = MaterialTheme.typography.titleMedium)
                Text(audio.tipo.etiqueta, style = MaterialTheme.typography.bodySmall)
            }
            Box(contentAlignment = Alignment.Center) {
                FilledIconButton(onClick = onAlternar, modifier = Modifier.size(56.dp)) {
                    Icon(
                        painter = iconoPlayPausa(estado.reproduciendo),
                        contentDescription = stringResource(
                            if (estado.reproduciendo) R.string.pausar else R.string.reproducir,
                        ),
                    )
                }
                if (estado.cargando) CircularProgressIndicator(Modifier.size(60.dp))
            }
        }

        val duracion = estado.duracionMs.coerceAtLeast(1L)
        // Mientras el usuario arrastra, se muestra su posición y no la del reproductor.
        var arrastre by remember(audio.id) { mutableStateOf<Float?>(null) }
        val progreso = arrastre ?: (estado.posicionMs.toFloat() / duracion).coerceIn(0f, 1f)
        Slider(
            value = progreso,
            onValueChange = { arrastre = it },
            onValueChangeFinished = {
                arrastre?.let { onBuscar((it * duracion).toLong()) }
                arrastre = null
            },
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatearTiempo((progreso * duracion).toLong()), style = MaterialTheme.typography.labelMedium)
            Text(formatearTiempo(estado.duracionMs), style = MaterialTheme.typography.labelMedium)
        }
    }
}
