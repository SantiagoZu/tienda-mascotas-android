package co.edu.uniminuto.tiendamascotas.ui.galeria

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniminuto.tiendamascotas.R
import co.edu.uniminuto.tiendamascotas.data.model.EstadoMascota
import co.edu.uniminuto.tiendamascotas.data.model.ItemGaleria
import co.edu.uniminuto.tiendamascotas.ui.common.EstadoCargando
import co.edu.uniminuto.tiendamascotas.ui.common.EstadoError
import co.edu.uniminuto.tiendamascotas.ui.common.ImagenRecurso
import co.edu.uniminuto.tiendamascotas.ui.common.UiState
import co.edu.uniminuto.tiendamascotas.ui.common.formatearPrecio

/**
 * Galería de mascotas en adopción o venta. Muestra una grilla (2, 3 o 4 columnas según
 * el ancho de ventana). Las fotos se amplían en un diálogo y los videos abren el reproductor.
 *
 * @param onVideoClick navega a la pantalla del reproductor con el id del video.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaleriaScreen(
    anchoVentana: WindowWidthSizeClass,
    onVideoClick: (String) -> Unit,
    viewModel: GaleriaViewModel = viewModel(factory = GaleriaViewModel.Factory),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    var imagenAbiertaId by rememberSaveable { mutableStateOf<String?>(null) }
    val columnas = when (anchoVentana) {
        WindowWidthSizeClass.Expanded -> 4
        WindowWidthSizeClass.Medium -> 3
        else -> 2
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_galeria)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                ),
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            FiltroEstado(estado.filtro, viewModel::seleccionarFiltro)
            when (val items = estado.items) {
                UiState.Cargando -> EstadoCargando()
                is UiState.Error -> EstadoError(items.mensaje, viewModel::cargar)
                is UiState.Exito -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columnas),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items.datos,
                            key = { it.id },
                            contentType = { it::class },
                        ) { item ->
                            TarjetaGaleria(item) {
                                when (item) {
                                    is ItemGaleria.Video -> onVideoClick(item.id)
                                    is ItemGaleria.Imagen -> imagenAbiertaId = item.id
                                }
                            }
                        }
                    }
                    val abierta = items.datos.firstOrNull { it.id == imagenAbiertaId }
                    if (abierta != null) {
                        DialogoImagen(abierta, onCerrar = { imagenAbiertaId = null })
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltroEstado(seleccionado: EstadoMascota?, onSeleccionar: (EstadoMascota?) -> Unit) {
    val opciones: List<EstadoMascota?> = listOf(null) + EstadoMascota.entries
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(opciones, key = { it?.name ?: "TODOS" }) { opcion ->
            FilterChip(
                selected = opcion == seleccionado,
                onClick = { onSeleccionar(opcion) },
                label = { Text(opcion?.etiqueta ?: stringResource(R.string.filtro_todos)) },
            )
        }
    }
}

@Composable
private fun TarjetaGaleria(item: ItemGaleria, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Box {
            ImagenRecurso(
                imagenRes = item.miniaturaRes,
                descripcion = item.nombre,
                tamanoPx = 360,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            )
            EtiquetaEstado(item.estado, Modifier.align(Alignment.TopStart).padding(8.dp))
            if (item is ItemGaleria.Video) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.55f),
                    modifier = Modifier.align(Alignment.Center).size(56.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_play_circle),
                        contentDescription = stringResource(R.string.video),
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(item.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                item.detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.precio?.let {
                Text(
                    formatearPrecio(it),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun EtiquetaEstado(estado: EstadoMascota, modifier: Modifier = Modifier) {
    val color = if (estado == EstadoMascota.ADOPCION) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }
    Text(
        estado.etiqueta,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun DialogoImagen(item: ItemGaleria, onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } },
        title = { Text(item.nombre) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ImagenRecurso(
                    imagenRes = item.miniaturaRes,
                    descripcion = item.nombre,
                    tamanoPx = 720,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EtiquetaEstado(item.estado)
                    item.precio?.let {
                        Text(formatearPrecio(it), Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                    }
                }
                Text(item.detalle)
            }
        },
    )
}
