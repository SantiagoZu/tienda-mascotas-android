package co.edu.uniminuto.tiendamascotas.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.edu.uniminuto.tiendamascotas.R
import co.edu.uniminuto.tiendamascotas.ui.audios.AudiosScreen
import co.edu.uniminuto.tiendamascotas.ui.catalogo.CatalogoScreen
import co.edu.uniminuto.tiendamascotas.ui.catalogo.DetalleProductoScreen
import co.edu.uniminuto.tiendamascotas.ui.catalogo.DetalleProductoViewModel
import co.edu.uniminuto.tiendamascotas.ui.galeria.GaleriaScreen
import co.edu.uniminuto.tiendamascotas.ui.galeria.ReproductorVideoScreen
import co.edu.uniminuto.tiendamascotas.ui.galeria.ReproductorVideoViewModel

/** Destinos de primer nivel que aparecen en la barra/riel de navegación. */
enum class DestinoPrincipal(
    val ruta: String,
    @StringRes val etiqueta: Int,
    @DrawableRes val icono: Int,
) {
    CATALOGO("catalogo", R.string.nav_catalogo, R.drawable.ic_storefront),
    GALERIA("galeria", R.string.nav_galeria, R.drawable.ic_photo_library),
    AUDIOS("audios", R.string.nav_audios, R.drawable.ic_headphones),
}

/** Rutas secundarias (con argumentos). */
private object Rutas {
    const val DETALLE = "producto/{${DetalleProductoViewModel.ARG_PRODUCTO_ID}}"
    const val VIDEO = "video/{${ReproductorVideoViewModel.ARG_VIDEO_ID}}"

    fun detalle(productoId: String) = "producto/$productoId"
    fun video(videoId: String) = "video/$videoId"
}

/**
 * Raíz de la interfaz. Según el [WindowSizeClass]:
 * - ancho compacto (teléfono vertical) → `NavigationBar` inferior;
 * - ancho mediano/expandido (tablet u horizontal) → `NavigationRail` lateral.
 */
@Composable
fun TiendaApp(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination
    val anchoVentana = windowSizeClass.widthSizeClass
    val usarRail = anchoVentana != WindowWidthSizeClass.Compact
    val esPrincipal = DestinoPrincipal.entries.any { destinoActual.esRuta(it.ruta) }

    val navegarA: (DestinoPrincipal) -> Unit = { destino ->
        navController.navigate(destino.ruta) {
            // Evita acumular pantallas y conserva el estado de cada pestaña.
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Row(Modifier.fillMaxSize()) {
        if (usarRail && esPrincipal) {
            NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                DestinoPrincipal.entries.forEach { destino ->
                    NavigationRailItem(
                        selected = destinoActual.esRuta(destino.ruta),
                        onClick = { navegarA(destino) },
                        icon = { Icon(painterResource(destino.icono), contentDescription = null) },
                        label = { Text(stringResource(destino.etiqueta)) },
                    )
                }
            }
        }
        Scaffold(
            modifier = Modifier.weight(1f),
            contentWindowInsets = WindowInsets.navigationBars,
            bottomBar = {
                if (!usarRail && esPrincipal) {
                    NavigationBar {
                        DestinoPrincipal.entries.forEach { destino ->
                            NavigationBarItem(
                                selected = destinoActual.esRuta(destino.ruta),
                                onClick = { navegarA(destino) },
                                icon = { Icon(painterResource(destino.icono), contentDescription = null) },
                                label = { Text(stringResource(destino.etiqueta)) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            TiendaNavHost(
                navController = navController,
                anchoVentana = anchoVentana,
                modifier = Modifier.padding(padding).consumeWindowInsets(padding),
            )
        }
    }
}

/** Grafo de navegación de la app (Navigation Compose). */
@Composable
private fun TiendaNavHost(
    navController: NavHostController,
    anchoVentana: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = DestinoPrincipal.CATALOGO.ruta,
        modifier = modifier,
    ) {
        composable(DestinoPrincipal.CATALOGO.ruta) {
            CatalogoScreen(
                anchoVentana = anchoVentana,
                onProductoClick = { navController.navigate(Rutas.detalle(it)) },
            )
        }
        composable(
            route = Rutas.DETALLE,
            arguments = listOf(
                navArgument(DetalleProductoViewModel.ARG_PRODUCTO_ID) { type = NavType.StringType },
            ),
        ) {
            DetalleProductoScreen(anchoVentana = anchoVentana, onVolver = { navController.popBackStack() })
        }
        composable(DestinoPrincipal.GALERIA.ruta) {
            GaleriaScreen(
                anchoVentana = anchoVentana,
                onVideoClick = { navController.navigate(Rutas.video(it)) },
            )
        }
        composable(
            route = Rutas.VIDEO,
            arguments = listOf(
                navArgument(ReproductorVideoViewModel.ARG_VIDEO_ID) { type = NavType.StringType },
            ),
        ) {
            ReproductorVideoScreen(onVolver = { navController.popBackStack() })
        }
        composable(DestinoPrincipal.AUDIOS.ruta) {
            AudiosScreen(anchoVentana = anchoVentana)
        }
    }
}

private fun NavDestination?.esRuta(ruta: String): Boolean =
    this?.hierarchy?.any { it.route == ruta } == true
