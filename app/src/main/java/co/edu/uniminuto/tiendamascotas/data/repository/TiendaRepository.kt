package co.edu.uniminuto.tiendamascotas.data.repository

import co.edu.uniminuto.tiendamascotas.R
import co.edu.uniminuto.tiendamascotas.data.model.AudioCuidado
import co.edu.uniminuto.tiendamascotas.data.model.CategoriaProducto
import co.edu.uniminuto.tiendamascotas.data.model.EstadoMascota
import co.edu.uniminuto.tiendamascotas.data.model.ItemGaleria
import co.edu.uniminuto.tiendamascotas.data.model.Producto
import co.edu.uniminuto.tiendamascotas.data.model.TipoAnimal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Repositorio de datos de la tienda. Las operaciones devuelven [Result] para que la capa de UI
 * pueda mostrar estados de error sin propagar excepciones.
 */
interface TiendaRepository {
    /** Obtiene todos los productos del catálogo. */
    suspend fun obtenerProductos(): Result<List<Producto>>

    /** Busca un producto por su [id]; falla si no existe. */
    suspend fun obtenerProducto(id: String): Result<Producto>

    /** Obtiene las imágenes y videos de la galería de mascotas. */
    suspend fun obtenerGaleria(): Result<List<ItemGaleria>>

    /** Busca un video de la galería por su [id]; falla si no existe o no es un video. */
    suspend fun obtenerVideo(id: String): Result<ItemGaleria.Video>

    /** Obtiene la lista de audios de cuidado. */
    suspend fun obtenerAudios(): Result<List<AudioCuidado>>
}

/**
 * Implementación local (sin internet) del [TiendaRepository]. Todos los recursos multimedia
 * están empaquetados en el APK. Simula una pequeña latencia para mostrar el estado de carga,
 * como ocurriría con una fuente remota.
 *
 * @param dispatcher dispatcher de E/S donde se ejecutan las consultas.
 * @param latenciaMs retardo simulado en milisegundos.
 */
class LocalTiendaRepository(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val latenciaMs: Long = 350L,
) : TiendaRepository {

    override suspend fun obtenerProductos(): Result<List<Producto>> = consulta { PRODUCTOS }

    override suspend fun obtenerProducto(id: String): Result<Producto> = consulta {
        PRODUCTOS.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("No existe el producto con id '$id'")
    }

    override suspend fun obtenerGaleria(): Result<List<ItemGaleria>> = consulta { GALERIA }

    override suspend fun obtenerVideo(id: String): Result<ItemGaleria.Video> = consulta {
        GALERIA.filterIsInstance<ItemGaleria.Video>().firstOrNull { it.id == id }
            ?: throw NoSuchElementException("No existe el video con id '$id'")
    }

    override suspend fun obtenerAudios(): Result<List<AudioCuidado>> = consulta { AUDIOS }

    /** Ejecuta [bloque] en el [dispatcher] y encapsula cualquier error en un [Result]. */
    private suspend fun <T> consulta(bloque: () -> T): Result<T> = withContext(dispatcher) {
        delay(latenciaMs)
        runCatching { bloque() }
    }

    private companion object {
        val PRODUCTOS = listOf(
            Producto(
                id = "p01", nombre = "Concentrado perro adulto 8 kg",
                descripcionCorta = "Nutrición completa con pollo y arroz.",
                descripcion = "Alimento balanceado para perros adultos de razas medianas y grandes. " +
                    "Contiene proteína de pollo, arroz, omega 3 y 6 para un pelaje brillante y " +
                    "fibra natural para una buena digestión.",
                precio = 145_900, categoria = CategoriaProducto.ALIMENTO, stock = 24,
                imagenRes = R.drawable.prod_concentrado_perro,
            ),
            Producto(
                id = "p02", nombre = "Alimento para gato 3 kg",
                descripcionCorta = "Sabor salmón, con control de bolas de pelo.",
                descripcion = "Croquetas para gatos adultos con salmón y atún. Fórmula con taurina " +
                    "para la salud visual y cardíaca, y fibra para controlar las bolas de pelo.",
                precio = 68_500, categoria = CategoriaProducto.ALIMENTO, stock = 30,
                imagenRes = R.drawable.prod_alimento_gato,
            ),
            Producto(
                id = "p03", nombre = "Collar ajustable reflectivo",
                descripcionCorta = "Nylon resistente con banda reflectiva.",
                descripcion = "Collar de nylon con hebilla de liberación rápida y banda reflectiva " +
                    "para paseos nocturnos más seguros. Talla ajustable de 30 a 50 cm.",
                precio = 32_000, categoria = CategoriaProducto.ACCESORIOS, stock = 15,
                imagenRes = R.drawable.prod_collar,
            ),
            Producto(
                id = "p04", nombre = "Cama acolchada mediana",
                descripcionCorta = "Suave, lavable y con base antideslizante.",
                descripcion = "Cama ortopédica para perros y gatos de hasta 15 kg. Funda removible " +
                    "y lavable a máquina, relleno de fibra siliconada y base antideslizante.",
                precio = 119_900, categoria = CategoriaProducto.HOGAR, stock = 8,
                imagenRes = R.drawable.prod_cama,
            ),
            Producto(
                id = "p05", nombre = "Rascador para gato",
                descripcionCorta = "Torre de 80 cm con sisal natural.",
                descripcion = "Rascador de dos niveles recubierto de sisal natural, con plataforma " +
                    "acolchada y juguete colgante. Ayuda a mantener las uñas sanas y protege tus muebles.",
                precio = 89_900, categoria = CategoriaProducto.HOGAR, stock = 6,
                imagenRes = R.drawable.prod_rascador,
            ),
            Producto(
                id = "p06", nombre = "Jaula para aves",
                descripcionCorta = "Incluye comederos, bebedero y perchas.",
                descripcion = "Jaula metálica de 60 × 40 × 70 cm para canarios, periquitos y " +
                    "agapornis. Bandeja extraíble para fácil limpieza, dos comederos y tres perchas.",
                precio = 159_000, categoria = CategoriaProducto.HOGAR, stock = 4,
                imagenRes = R.drawable.prod_jaula_aves,
            ),
            Producto(
                id = "p07", nombre = "Acuario 40 L con filtro",
                descripcionCorta = "Kit completo con luz LED y filtro.",
                descripcion = "Acuario de vidrio de 40 litros con tapa, iluminación LED y filtro " +
                    "interno silencioso. Ideal para peces tropicales de agua dulce.",
                precio = 239_900, categoria = CategoriaProducto.HOGAR, stock = 3,
                imagenRes = R.drawable.prod_acuario,
            ),
            Producto(
                id = "p08", nombre = "Kit de juguetes",
                descripcionCorta = "Pelota, mordedor y peluche.",
                descripcion = "Set de tres juguetes resistentes para perros: pelota de caucho, " +
                    "mordedor de cuerda y peluche con sonido. Estimulan el juego y la actividad física.",
                precio = 18_500, categoria = CategoriaProducto.JUGUETES, stock = 40,
                imagenRes = R.drawable.prod_juguete,
            ),
            Producto(
                id = "p09", nombre = "Arena sanitaria 10 kg",
                descripcionCorta = "Aglutinante y con control de olores.",
                descripcion = "Arena de bentonita aglutinante con carbón activado que controla " +
                    "los olores hasta por 7 días. Baja en polvo.",
                precio = 42_000, categoria = CategoriaProducto.HIGIENE, stock = 0,
                imagenRes = R.drawable.prod_arena,
            ),
            Producto(
                id = "p10", nombre = "Casa para hámster",
                descripcionCorta = "Madera natural, segura para roer.",
                descripcion = "Casita de madera sin tratar químicamente, con dos entradas y techo " +
                    "removible. Perfecta para hámsteres, jerbos y ratones.",
                precio = 54_900, categoria = CategoriaProducto.HOGAR, stock = 12,
                imagenRes = R.drawable.prod_casa_hamster,
            ),
        )

        val GALERIA: List<ItemGaleria> = listOf(
            ItemGaleria.Video(
                id = "v01", nombre = "Rocky", detalle = "Perro criollo · 1 año · juguetón",
                estado = EstadoMascota.ADOPCION, precio = null,
                miniaturaRes = R.drawable.thumb_perro_jugando, videoRes = R.raw.video_perro_jugando,
            ),
            ItemGaleria.Imagen(
                id = "g01", nombre = "Max", detalle = "Perro criollo · 2 años · vacunado",
                estado = EstadoMascota.ADOPCION, precio = null, miniaturaRes = R.drawable.pet_max,
            ),
            ItemGaleria.Imagen(
                id = "g02", nombre = "Luna", detalle = "Gata angora · 8 meses · esterilizada",
                estado = EstadoMascota.ADOPCION, precio = null, miniaturaRes = R.drawable.pet_luna,
            ),
            ItemGaleria.Imagen(
                id = "g03", nombre = "Kiwi", detalle = "Periquito australiano · 6 meses",
                estado = EstadoMascota.VENTA, precio = 85_000, miniaturaRes = R.drawable.pet_kiwi,
            ),
            ItemGaleria.Video(
                id = "v02", nombre = "Nala", detalle = "Gata criolla · 1 año · muy curiosa",
                estado = EstadoMascota.ADOPCION, precio = null,
                miniaturaRes = R.drawable.thumb_gato_curioso, videoRes = R.raw.video_gato_curioso,
            ),
            ItemGaleria.Imagen(
                id = "g04", nombre = "Nemo", detalle = "Pez payaso · agua salada",
                estado = EstadoMascota.VENTA, precio = 45_000, miniaturaRes = R.drawable.pet_nemo,
            ),
            ItemGaleria.Imagen(
                id = "g05", nombre = "Coco", detalle = "Conejo belier · 4 meses",
                estado = EstadoMascota.VENTA, precio = 120_000, miniaturaRes = R.drawable.pet_coco,
            ),
            ItemGaleria.Imagen(
                id = "g06", nombre = "Bruno", detalle = "Hámster sirio · 3 meses",
                estado = EstadoMascota.VENTA, precio = 35_000, miniaturaRes = R.drawable.pet_bruno,
            ),
            ItemGaleria.Imagen(
                id = "g07", nombre = "Toby", detalle = "Beagle · 3 años · desparasitado",
                estado = EstadoMascota.ADOPCION, precio = null, miniaturaRes = R.drawable.pet_toby,
            ),
            ItemGaleria.Imagen(
                id = "g08", nombre = "Mía", detalle = "Gata siamesa · 2 años",
                estado = EstadoMascota.ADOPCION, precio = null, miniaturaRes = R.drawable.pet_mia,
            ),
        )

        val AUDIOS = listOf(
            AudioCuidado(
                id = "a01", titulo = "Cuidado de perros",
                descripcion = "Paseos, alimentación, vacunas y visitas al veterinario.",
                tipo = TipoAnimal.PERROS, duracionSeg = 20, audioRes = R.raw.audio_cuidado_perros,
            ),
            AudioCuidado(
                id = "a02", titulo = "Cuidado de gatos",
                descripcion = "Caja de arena, rascador, juego y cepillado.",
                tipo = TipoAnimal.GATOS, duracionSeg = 17, audioRes = R.raw.audio_cuidado_gatos,
            ),
            AudioCuidado(
                id = "a03", titulo = "Cuidado de aves",
                descripcion = "Ubicación de la jaula, agua, semillas y frutas.",
                tipo = TipoAnimal.AVES, duracionSeg = 15, audioRes = R.raw.audio_cuidado_aves,
            ),
            AudioCuidado(
                id = "a04", titulo = "Cuidado de peces",
                descripcion = "Alimentación correcta, cambios de agua y filtro.",
                tipo = TipoAnimal.PECES, duracionSeg = 16, audioRes = R.raw.audio_cuidado_peces,
            ),
            AudioCuidado(
                id = "a05", titulo = "Cuidado de roedores",
                descripcion = "Jaula, viruta, heno y juguetes para roer.",
                tipo = TipoAnimal.ROEDORES, duracionSeg = 17, audioRes = R.raw.audio_cuidado_roedores,
            ),
        )
    }
}
