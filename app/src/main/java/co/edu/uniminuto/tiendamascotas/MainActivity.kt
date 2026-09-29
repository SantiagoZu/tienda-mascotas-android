package co.edu.uniminuto.tiendamascotas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import co.edu.uniminuto.tiendamascotas.ui.navigation.TiendaApp
import co.edu.uniminuto.tiendamascotas.ui.theme.TiendaMascotasTheme

/**
 * Única actividad de la app. Calcula el [androidx.compose.material3.windowsizeclass.WindowSizeClass]
 * para adaptar la interfaz a teléfonos, tablets y orientación horizontal.
 */
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TiendaMascotasTheme {
                TiendaApp(windowSizeClass = calculateWindowSizeClass(this))
            }
        }
    }
}
