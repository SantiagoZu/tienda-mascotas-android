package co.edu.uniminuto.tiendamascotas.ui.galeria

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.uniminuto.tiendamascotas.data.model.EstadoMascota
import co.edu.uniminuto.tiendamascotas.data.model.ItemGaleria
import co.edu.uniminuto.tiendamascotas.data.repository.TiendaRepository
import co.edu.uniminuto.tiendamascotas.ui.common.UiState
import co.edu.uniminuto.tiendamascotas.ui.common.aUiState
import co.edu.uniminuto.tiendamascotas.ui.common.appContainer
import co.edu.uniminuto.tiendamascotas.ui.common.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de la galería: elementos filtrados y filtro activo. */
data class GaleriaUiState(
    val items: UiState<List<ItemGaleria>> = UiState.Cargando,
    val filtro: EstadoMascota? = null,
)

/** ViewModel de la galería de mascotas (imágenes y videos). */
class GaleriaViewModel(private val repositorio: TiendaRepository) : ViewModel() {

    private val items = MutableStateFlow<UiState<List<ItemGaleria>>>(UiState.Cargando)
    private val filtro = MutableStateFlow<EstadoMascota?>(null)

    /** Estado expuesto a la UI. */
    val uiState: StateFlow<GaleriaUiState> =
        combine(items, filtro) { estado, f ->
            GaleriaUiState(estado.map { lista -> lista.filter { f == null || it.estado == f } }, f)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GaleriaUiState())

    init {
        cargar()
    }

    /** Carga (o recarga) los elementos de la galería. */
    fun cargar() {
        viewModelScope.launch {
            items.value = UiState.Cargando
            items.value = repositorio.obtenerGaleria().aUiState()
        }
    }

    /** Filtra por adopción/venta; `null` muestra todo. */
    fun seleccionarFiltro(nuevo: EstadoMascota?) {
        filtro.value = nuevo
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { GaleriaViewModel(appContainer().repositorio) }
        }
    }
}

/** ViewModel de la pantalla de reproducción de un video de la galería. */
class ReproductorVideoViewModel(
    savedStateHandle: SavedStateHandle,
    private val repositorio: TiendaRepository,
) : ViewModel() {

    private val videoId: String = checkNotNull(savedStateHandle[ARG_VIDEO_ID])
    private val _uiState = MutableStateFlow<UiState<ItemGaleria.Video>>(UiState.Cargando)

    /** Video a reproducir. */
    val uiState: StateFlow<UiState<ItemGaleria.Video>> = _uiState.asStateFlow()

    init {
        cargar()
    }

    /** Busca el video en el repositorio. */
    fun cargar() {
        viewModelScope.launch {
            _uiState.value = UiState.Cargando
            _uiState.value = repositorio.obtenerVideo(videoId).aUiState()
        }
    }

    companion object {
        /** Nombre del argumento de navegación con el id del video. */
        const val ARG_VIDEO_ID = "videoId"

        val Factory = viewModelFactory {
            initializer {
                ReproductorVideoViewModel(createSavedStateHandle(), appContainer().repositorio)
            }
        }
    }
}
