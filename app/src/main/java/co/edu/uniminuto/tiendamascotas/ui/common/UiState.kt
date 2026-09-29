package co.edu.uniminuto.tiendamascotas.ui.common

/**
 * Estado genérico de una pantalla que carga datos de forma asíncrona.
 */
sealed interface UiState<out T> {
    /** Los datos se están cargando. */
    data object Cargando : UiState<Nothing>

    /** Los datos se cargaron correctamente. */
    data class Exito<T>(val datos: T) : UiState<T>

    /** Ocurrió un error; [mensaje] se muestra al usuario. */
    data class Error(val mensaje: String) : UiState<Nothing>
}

/** Transforma los datos de un [UiState.Exito] conservando los estados de carga y error. */
inline fun <T, R> UiState<T>.map(transformar: (T) -> R): UiState<R> = when (this) {
    UiState.Cargando -> UiState.Cargando
    is UiState.Error -> this
    is UiState.Exito -> UiState.Exito(transformar(datos))
}

/** Convierte un [Result] del repositorio en un [UiState]. */
fun <T> Result<T>.aUiState(): UiState<T> = fold(
    onSuccess = { UiState.Exito(it) },
    onFailure = { UiState.Error(it.message ?: "Ocurrió un error inesperado") },
)
