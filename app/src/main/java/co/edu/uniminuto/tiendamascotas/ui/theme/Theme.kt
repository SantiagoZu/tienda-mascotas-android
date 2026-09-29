package co.edu.uniminuto.tiendamascotas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Naranja = Color(0xFFE8651A)
private val NaranjaClaro = Color(0xFFFFDBCB)
private val Turquesa = Color(0xFF00897B)
private val TurquesaClaro = Color(0xFFB2DFDB)
private val Crema = Color(0xFFFFF8F3)

private val EsquemaClaro = lightColorScheme(
    primary = Naranja,
    onPrimary = Color.White,
    primaryContainer = NaranjaClaro,
    onPrimaryContainer = Color(0xFF3A1500),
    secondary = Turquesa,
    onSecondary = Color.White,
    secondaryContainer = TurquesaClaro,
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = Color(0xFF7E57C2),
    background = Crema,
    surface = Crema,
    surfaceContainer = Color(0xFFFCEEE6),
)

private val EsquemaOscuro = darkColorScheme(
    primary = Color(0xFFFFB690),
    onPrimary = Color(0xFF552100),
    primaryContainer = Color(0xFF793100),
    onPrimaryContainer = NaranjaClaro,
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = TurquesaClaro,
    tertiary = Color(0xFFD1C4E9),
)

/**
 * Tema Material 3 de la tienda.
 *
 * @param colorDinamico usa los colores del fondo de pantalla (Android 12+) en lugar de la marca.
 */
@Composable
fun TiendaMascotasTheme(
    oscuro: Boolean = isSystemInDarkTheme(),
    colorDinamico: Boolean = false,
    content: @Composable () -> Unit,
) {
    val esquema = when {
        colorDinamico && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (oscuro) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        oscuro -> EsquemaOscuro
        else -> EsquemaClaro
    }
    MaterialTheme(colorScheme = esquema, typography = Typography(), content = content)
}
