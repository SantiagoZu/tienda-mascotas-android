package co.edu.uniminuto.tiendamascotas.ui.catalogo

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniminuto.tiendamascotas.R
import co.edu.uniminuto.tiendamascotas.data.model.CategoriaProducto
import co.edu.uniminuto.tiendamascotas.data.model.Producto
import co.edu.uniminuto.tiendamascotas.ui.common.EstadoCargando
import co.edu.uniminuto.tiendamascotas.ui.common.EstadoError
import co.edu.uniminuto.tiendamascotas.ui.common.ImagenRecurso
import co.edu.uniminuto.tiendamascotas.ui.common.UiState
import co.edu.uniminuto.tiendamascotas.ui.common.formatearPrecio

/**
 * Pantalla del catálogo de productos.
 *
 * En pantallas compactas (teléfono vertical) muestra una lista; en pantallas medianas o
 * expandidas (tablet u horizontal) usa una grilla de 2 o 3 columnas.
 *
 * @param anchoVentana clase de ancho de ventana calculada con WindowSizeClass.
 * @param onProductoClick abre el detalle del producto con el id indicado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    anchoVentana: WindowWidthSizeClass,
    onProductoClick: (String) -> Unit,
    viewModel: CatalogoViewModel = viewModel(factory = CatalogoViewModel.Factory),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_catalogo)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            FiltroCategorias(
                seleccionada = estado.categoriaSeleccionada,
                onSeleccionar = viewModel::seleccionarCategoria,
            )
            when (val productos = estado.productos) {
                UiState.Cargando -> EstadoCargando()
                is UiState.Error -> EstadoError(productos.mensaje, onReintentar = viewModel::cargar)
                is UiState.Exito -> when {
                    productos.datos.isEmpty() -> Box(
                        Modifier.fillMaxSize(), contentAlignment = Alignment.Center,
                    ) { Text("No hay productos en esta categoría") }

                    anchoVentana == WindowWidthSizeClass.Compact ->
                        ListaProductos(productos.datos, onProductoClick)

                    else -> GrillaProductos(
                        productos = productos.datos,
                        columnas = if (anchoVentana == WindowWidthSizeClass.Expanded) 3 else 2,
                        onProductoClick = onProductoClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltroCategorias(
    seleccionada: CategoriaProducto?,
    onSeleccionar: (CategoriaProducto?) -> Unit,
) {
    val opciones: List<CategoriaProducto?> = listOf(null) + CategoriaProducto.entries
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(opciones, key = { it?.name ?: "TODOS" }) { categoria ->
            FilterChip(
                selected = categoria == seleccionada,
                onClick = { onSeleccionar(categoria) },
                label = { Text(categoria?.etiqueta ?: stringResource(R.string.filtro_todos)) },
            )
        }
    }
}

@Composable
private fun ListaProductos(productos: List<Producto>, onProductoClick: (String) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(productos, key = { it.id }) { producto ->
            ProductoFila(producto, onClick = { onProductoClick(producto.id) })
        }
    }
}

@Composable
private fun GrillaProductos(
    productos: List<Producto>,
    columnas: Int,
    onProductoClick: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnas),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(productos, key = { it.id }) { producto ->
            ProductoTarjeta(producto, onClick = { onProductoClick(producto.id) })
        }
    }
}

/** Fila horizontal de producto (teléfono). */
@Composable
private fun ProductoFila(producto: Producto, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagenRecurso(
                imagenRes = producto.imagenRes,
                descripcion = producto.nombre,
                tamanoPx = 240,
                modifier = Modifier.size(96.dp).clip(RoundedCornerShape(12.dp)),
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                InfoResumida(producto)
            }
        }
    }
}

/** Tarjeta vertical de producto (grilla en tablet / horizontal). */
@Composable
private fun ProductoTarjeta(producto: Producto, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ImagenRecurso(
            imagenRes = producto.imagenRes,
            descripcion = producto.nombre,
            modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
        )
        Column(Modifier.padding(12.dp)) { InfoResumida(producto) }
    }
}

@Composable
private fun InfoResumida(producto: Producto) {
    Text(
        producto.nombre,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
    Text(
        producto.descripcionCorta,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Text(
            formatearPrecio(producto.precio),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        if (producto.stock == 0) {
            Text(
                stringResource(R.string.sin_stock),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
