package co.edu.uniminuto.tiendamascotas

import co.edu.uniminuto.tiendamascotas.data.repository.LocalTiendaRepository
import co.edu.uniminuto.tiendamascotas.player.formatearTiempo
import co.edu.uniminuto.tiendamascotas.ui.common.UiState
import co.edu.uniminuto.tiendamascotas.ui.common.aUiState
import co.edu.uniminuto.tiendamascotas.ui.common.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas unitarias de utilidades y del repositorio local. */
class TiendaUnitTest {

    private val repositorio = LocalTiendaRepository(Dispatchers.Unconfined, latenciaMs = 0L)

    @Test
    fun formatearTiempo_usaMinutosYSegundos() {
        assertEquals("0:00", formatearTiempo(0))
        assertEquals("1:05", formatearTiempo(65_000))
        assertEquals("0:00", formatearTiempo(-10))
    }

    @Test
    fun uiState_mapConservaErrores() {
        val error: UiState<Int> = UiState.Error("falló")
        assertEquals(error, error.map { it * 2 })
        assertEquals(UiState.Exito(4), UiState.Exito(2).map { it * 2 })
    }

    @Test
    fun repositorio_devuelveProductosConIdsUnicos() = runBlocking {
        val productos = repositorio.obtenerProductos().getOrThrow()
        assertTrue(productos.isNotEmpty())
        assertEquals(productos.size, productos.map { it.id }.toSet().size)
    }

    @Test
    fun repositorio_productoInexistenteEsError() = runBlocking {
        val estado = repositorio.obtenerProducto("no-existe").aUiState()
        assertTrue(estado is UiState.Error)
    }

    @Test
    fun repositorio_galeriaIncluyeDosVideos() = runBlocking {
        val videos = repositorio.obtenerGaleria().getOrThrow()
            .filterIsInstance<co.edu.uniminuto.tiendamascotas.data.model.ItemGaleria.Video>()
        assertEquals(2, videos.size)
        assertTrue(repositorio.obtenerVideo(videos.first().id).isSuccess)
    }
}
