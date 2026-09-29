package co.edu.uniminuto.tiendamascotas.ui.catalogo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import co.edu.uniminuto.tiendamascotas.data.model.CategoriaProducto
import co.edu.uniminuto.tiendamascotas.data.model.Producto
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

/** Estado de la pantalla de catálogo. */
data class CatalogoUiState(
    val productos: UiState<List<Producto>> = UiState.Cargando,
    val categoriaSeleccionada: CategoriaProducto? = null,
)

/**
 * ViewModel del catálogo: carga los productos del [TiendaRepository] y aplica el filtro
 * por categoría.
 */
class CatalogoViewModel(private val repositorio: TiendaRepository) : ViewModel() {

    private val productos = MutableStateFlow<UiState<List<Producto>>>(UiState.Cargando)
    private val categoria = MutableStateFlow<CategoriaProducto?>(null)

    /** Estado combinado (productos filtrados + categoría) expuesto a la UI. */
    val uiState: StateFlow<CatalogoUiState> =
        combine(productos, categoria) { estado, cat ->
            CatalogoUiState(
                productos = estado.map { lista -> lista.filter { cat == null || it.categoria == cat } },
                categoriaSeleccionada = cat,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogoUiState())

    init {
        cargar()
    }

    /** Carga (o recarga tras un error) la lista de productos. */
    fun cargar() {
        viewModelScope.launch {
            productos.value = UiState.Cargando
            productos.value = repositorio.obtenerProductos().aUiState()
        }
    }

    /** Filtra por [nueva] categoría; `null` muestra todos los productos. */
    fun seleccionarCategoria(nueva: CategoriaProducto?) {
        categoria.value = nueva
    }

    companion object {
        /** Fábrica que obtiene el repositorio del contenedor de la aplicación. */
        val Factory = viewModelFactory {
            initializer { CatalogoViewModel(appContainer().repositorio) }
        }
    }
}

/**
 * ViewModel del detalle de producto. Lee el `productoId` de los argumentos de navegación
 * mediante [SavedStateHandle].
 */
class DetalleProductoViewModel(
    savedStateHandle: SavedStateHandle,
    private val repositorio: TiendaRepository,
) : ViewModel() {

    private val productoId: String = checkNotNull(savedStateHandle[ARG_PRODUCTO_ID])

    private val _uiState = MutableStateFlow<UiState<Producto>>(UiState.Cargando)

    /** Estado del producto mostrado. */
    val uiState: StateFlow<UiState<Producto>> = _uiState.asStateFlow()

    init {
        cargar()
    }

    /** Carga el producto; en caso de error la UI ofrece reintentar. */
    fun cargar() {
        viewModelScope.launch {
            _uiState.value = UiState.Cargando
            _uiState.value = repositorio.obtenerProducto(productoId).aUiState()
        }
    }

    companion object {
        /** Nombre del argumento de navegación con el id del producto. */
        const val ARG_PRODUCTO_ID = "productoId"

        val Factory = viewModelFactory {
            initializer {
                DetalleProductoViewModel(createSavedStateHandle(), appContainer().repositorio)
            }
        }
    }
}
