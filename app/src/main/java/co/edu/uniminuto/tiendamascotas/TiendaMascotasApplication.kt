package co.edu.uniminuto.tiendamascotas

import android.app.Application
import android.content.Context
import co.edu.uniminuto.tiendamascotas.data.repository.LocalTiendaRepository
import co.edu.uniminuto.tiendamascotas.data.repository.TiendaRepository
import co.edu.uniminuto.tiendamascotas.player.AudioPlayerManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache

/**
 * Clase [Application] de la tienda. Crea el [AppContainer] con las dependencias
 * compartidas y configura el cargador de imágenes de Coil con una caché acotada.
 */
class TiendaMascotasApplication : Application(), ImageLoaderFactory {

    /** Contenedor de dependencias (inyección manual). */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    /** Limita la caché en memoria de Coil al 20 % de la memoria disponible para la app. */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.20).build() }
            .crossfade(true)
            .build()
}

/**
 * Contenedor sencillo de dependencias de la aplicación.
 *
 * @param context contexto de aplicación (no se guarda un contexto de actividad para evitar fugas).
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    /** Fuente única de datos de productos, galería y audios. */
    val repositorio: TiendaRepository = LocalTiendaRepository()

    /** Único reproductor de audio compartido por toda la app; se crea al primer uso. */
    val reproductorAudio: AudioPlayerManager by lazy { AudioPlayerManager(appContext) }
}
