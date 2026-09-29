package co.edu.uniminuto.tiendamascotas.ui.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniminuto.tiendamascotas.R
import co.edu.uniminuto.tiendamascotas.data.model.Producto
import co.edu.uniminuto.tiendamascotas.ui.common.EstadoCargando
import co.edu.uniminuto.tiendamascotas.ui.common.EstadoError
import co.edu.uniminuto.tiendamascotas.ui.common.ImagenRecurso
import co.edu.uniminuto.tiendamascotas.ui.common.UiState
import co.edu.uniminuto.tiendamascotas.ui.common.formatearPrecio
import kotlinx.coroutines.launch

/**
 * Pantalla de detalle de un producto. En pantallas anchas la imagen y la información
 * se muestran lado a lado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoScreen(
    anchoVentana: WindowWidthSizeClass,
    onVolver: () -> Unit,
    viewModel: DetalleProductoViewModel = viewModel(factory = DetalleProductoViewModel.Factory),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.titulo_detalle)) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.volver))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        val modifier = Modifier.padding(padding).fillMaxSize()
        when (val actual = estado) {
            UiState.Cargando -> EstadoCargando(modifier)
            is UiState.Error -> EstadoError(actual.mensaje, viewModel::cargar, modifier)
            is UiState.Exito -> {
                val producto = actual.datos
                val mensaje = stringResource(R.string.agregado_carrito, producto.nombre)
                val agregar: () -> Unit = { scope.launch { snackbar.showSnackbar(mensaje) } }
                if (anchoVentana == WindowWidthSizeClass.Compact) {
                    Column(modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
                        ImagenRecurso(
                            imagenRes = producto.imagenRes,
                            descripcion = producto.nombre,
                            tamanoPx = 720,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                                .clip(RoundedCornerShape(20.dp)),
                        )
                        InfoProducto(producto, agregar, Modifier.padding(top = 16.dp))
                    }
                } else {
                    Row(
                        modifier.padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        ImagenRecurso(
                            imagenRes = producto.imagenRes,
                            descripcion = producto.nombre,
                            tamanoPx = 720,
                            modifier = Modifier.weight(1f).widthIn(max = 480.dp).aspectRatio(1f)
                                .clip(RoundedCornerShape(20.dp)),
                        )
                        InfoProducto(
                            producto, agregar,
                            Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoProducto(producto: Producto, onAgregar: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(onClick = {}, label = { Text(producto.categoria.etiqueta) })
        Text(producto.nombre, style = MaterialTheme.typography.headlineSmall)
        Text(
            formatearPrecio(producto.precio),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = if (producto.stock > 0) {
                stringResource(R.string.en_stock, producto.stock)
            } else {
                stringResource(R.string.sin_stock)
            },
            style = MaterialTheme.typography.labelLarge,
            color = if (producto.stock > 0) {
                MaterialTheme.colorScheme.secondary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
        Text(producto.descripcion, style = MaterialTheme.typography.bodyLarge)
        Button(
            onClick = onAgregar,
            enabled = producto.stock > 0,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        ) {
            Icon(Icons.Filled.ShoppingCart, contentDescription = null)
            Text(stringResource(R.string.agregar_carrito), Modifier.padding(start = 8.dp))
        }
    }
}
