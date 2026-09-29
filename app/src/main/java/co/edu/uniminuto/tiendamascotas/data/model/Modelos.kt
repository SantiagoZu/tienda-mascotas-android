package co.edu.uniminuto.tiendamascotas.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

/** Categorías del catálogo de productos. */
enum class CategoriaProducto(val etiqueta: String) {
    ALIMENTO("Alimento"),
    ACCESORIOS("Accesorios"),
    HOGAR("Hogar"),
    HIGIENE("Higiene"),
    JUGUETES("Juguetes"),
}

/**
 * Producto a la venta en la tienda.
 *
 * @property id identificador único (se usa como `key` en las listas y en la navegación).
 * @property descripcionCorta texto breve que se muestra en la lista del catálogo.
 * @property descripcion texto completo que se muestra en el detalle.
 * @property precio precio en pesos colombianos (COP), sin decimales.
 * @property imagenRes imagen local del producto (res/drawable-nodpi).
 */
data class Producto(
    val id: String,
    val nombre: String,
    val descripcionCorta: String,
    val descripcion: String,
    val precio: Long,
    val categoria: CategoriaProducto,
    val stock: Int,
    @DrawableRes val imagenRes: Int,
)

/** Indica si una mascota de la galería está en adopción o a la venta. */
enum class EstadoMascota(val etiqueta: String) {
    ADOPCION("Adopción"),
    VENTA("Venta"),
}

/**
 * Elemento de la galería de mascotas: puede ser una imagen o un video.
 */
sealed interface ItemGaleria {
    val id: String
    val nombre: String
    val detalle: String
    val estado: EstadoMascota

    /** Precio en COP; `null` cuando la mascota está en adopción. */
    val precio: Long?

    /** Imagen que se muestra en la grilla (foto o miniatura del video). */
    @get:DrawableRes
    val miniaturaRes: Int

    /** Foto de una mascota. */
    data class Imagen(
        override val id: String,
        override val nombre: String,
        override val detalle: String,
        override val estado: EstadoMascota,
        override val precio: Long?,
        @DrawableRes override val miniaturaRes: Int,
    ) : ItemGaleria

    /** Video corto de una mascota, reproducible con ExoPlayer. */
    data class Video(
        override val id: String,
        override val nombre: String,
        override val detalle: String,
        override val estado: EstadoMascota,
        override val precio: Long?,
        @DrawableRes override val miniaturaRes: Int,
        @RawRes val videoRes: Int,
    ) : ItemGaleria
}

/** Tipos de animal para los audios de cuidado. */
enum class TipoAnimal(val etiqueta: String, val emoji: String) {
    PERROS("Perros", "🐕"),
    GATOS("Gatos", "🐈"),
    AVES("Aves", "🦜"),
    PECES("Peces", "🐠"),
    ROEDORES("Roedores", "🐹"),
}

/**
 * Audio con consejos de cuidado almacenado en res/raw.
 *
 * @property duracionSeg duración aproximada, usada para mostrarla antes de reproducir.
 */
data class AudioCuidado(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val tipo: TipoAnimal,
    val duracionSeg: Int,
    @RawRes val audioRes: Int,
)
