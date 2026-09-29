package co.edu.uniminuto.tiendamascotas.ui.audios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.uniminuto.tiendamascotas.data.model.AudioCuidado
import co.edu.uniminuto.tiendamascotas.data.repository.TiendaRepository
import co.edu.uniminuto.tiendamascotas.player.AudioPlayerManager
import co.edu.uniminuto.tiendamascotas.player.EstadoReproductorAudio
import co.edu.uniminuto.tiendamascotas.ui.common.UiState
import co.edu.uniminuto.tiendamascotas.ui.common.aUiState
import co.edu.uniminuto.tiendamascotas.ui.common.appContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel de la pantalla de audios de cuidado. Carga la lista desde el repositorio y
 * delega la reproducción en el [AudioPlayerManager] compartido.
 */
class AudiosViewModel(
    private val repositorio: TiendaRepository,
    private val reproductor: AudioPlayerManager,
) : ViewModel() {

    private val _audios = MutableStateFlow<UiState<List<AudioCuidado>>>(UiState.Cargando)

    /** Lista de audios disponibles. */
    val audios: StateFlow<UiState<List<AudioCuidado>>> = _audios.asStateFlow()

    /** Estado del reproductor (audio actual, progreso, errores). */
    val reproductorEstado: StateFlow<EstadoReproductorAudio> = reproductor.estado

    init {
        cargar()
    }

    /** Carga (o recarga) la lista de audios. */
    fun cargar() {
        viewModelScope.launch {
            _audios.value = UiState.Cargando
            _audios.value = repositorio.obtenerAudios().aUiState()
        }
    }

    /** Si [audio] ya es el actual alterna play/pausa; si no, lo reproduce desde el inicio. */
    fun seleccionarAudio(audio: AudioCuidado) {
        if (reproductor.estado.value.audioActual?.id == audio.id) {
            reproductor.alternarReproduccion()
        } else {
            reproductor.reproducir(audio)
        }
    }

    fun alternarReproduccion() = reproductor.alternarReproduccion()

    fun buscar(posicionMs: Long) = reproductor.buscar(posicionMs)

    /** Pausa el audio (por ejemplo al salir de la pantalla). */
    fun pausar() = reproductor.pausar()

    /** Libera el reproductor cuando la app deja de estar visible (`onStop`). */
    fun liberarRecursos() = reproductor.liberar()

    fun limpiarError() = reproductor.limpiarError()

    override fun onCleared() {
        reproductor.liberar()
        super.onCleared()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = appContainer()
                AudiosViewModel(container.repositorio, container.reproductorAudio)
            }
        }
    }
}
