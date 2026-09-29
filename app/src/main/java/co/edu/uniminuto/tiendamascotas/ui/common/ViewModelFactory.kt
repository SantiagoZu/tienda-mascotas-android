package co.edu.uniminuto.tiendamascotas.ui.common

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import co.edu.uniminuto.tiendamascotas.AppContainer
import co.edu.uniminuto.tiendamascotas.TiendaMascotasApplication

/** Obtiene el [AppContainer] de la aplicación desde los [CreationExtras] de un ViewModel. */
fun CreationExtras.appContainer(): AppContainer =
    (checkNotNull(this[APPLICATION_KEY]) as TiendaMascotasApplication).container
