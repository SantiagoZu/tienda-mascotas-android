# 🐾 Tienda de Mascotas — App Android

[![Build APK](https://github.com/SantiagoZu/tienda-mascotas-android/actions/workflows/build.yml/badge.svg)](https://github.com/SantiagoZu/tienda-mascotas-android/actions/workflows/build.yml)

Aplicación Android nativa para una tienda de mascotas, desarrollada para la materia
**Desarrollo Móvil** de la **Corporación Universitaria Minuto de Dios – UNIMINUTO**.

Está construida con **Android Studio + Kotlin + Jetpack Compose (Material 3)** y reproduce
contenido multimedia con **Media3 ExoPlayer**. Todos los recursos (imágenes, videos y audios)
vienen **dentro del APK**, así que la app funciona **sin conexión a internet**.

## ✨ Funcionalidades

| Sección | Descripción |
|---|---|
| 🛒 **Catálogo** | Lista de productos con imagen, nombre, descripción corta y precio en pesos colombianos (COP). Filtro por categoría. Al tocar un producto se abre su **pantalla de detalle** (descripción completa, stock y botón "Agregar al carrito"). |
| 🖼️ **Galería** | Grilla de fotos y videos de mascotas en **adopción** o **venta**, con filtro. Las fotos se amplían en un diálogo y los **videos se reproducen dentro de la app** con ExoPlayer (`PlayerView` embebido con `AndroidView`). |
| 🎧 **Audios de cuidado** | Consejos narrados para **perros, gatos, aves, peces y roedores** con un reproductor sencillo: play/pausa, barra de progreso desplazable y tiempo transcurrido/total. |

### Interfaz adaptable
- **Bottom navigation** (Catálogo, Galería, Audios) con **Navigation Compose**.
- **WindowSizeClass**: en tablet u orientación horizontal se usa **NavigationRail** lateral,
  grillas con más columnas (catálogo 2–3, galería 3–4) y paneles lado a lado (detalle y audios).
- Tema Material 3 con colores de marca, modo claro/oscuro y diseño *edge-to-edge*.

## 🏗️ Arquitectura

```
app/src/main/java/co/edu/uniminuto/tiendamascotas/
├── TiendaMascotasApplication.kt   # Application + AppContainer (inyección manual) + Coil
├── MainActivity.kt                # Calcula WindowSizeClass y lanza la UI
├── data/
│   ├── model/Modelos.kt           # Producto, ItemGaleria (Imagen/Video), AudioCuidado…
│   └── repository/TiendaRepository.kt  # Interfaz + implementación local (Result<T>)
├── player/
│   ├── AudioPlayerManager.kt      # Único ExoPlayer de audio compartido + StateFlow
│   ├── VideoPlayer.kt             # Composable con PlayerView (AndroidView)
│   └── MediaUtils.kt              # URIs de res/raw, mensajes de error, formato de tiempo
└── ui/
    ├── common/                    # UiState (Cargando/Éxito/Error), componentes, formato COP
    ├── navigation/TiendaApp.kt    # Scaffold, NavigationBar / NavigationRail, NavHost
    ├── catalogo/                  # CatalogoViewModel, DetalleProductoViewModel y pantallas
    ├── galeria/                   # GaleriaViewModel, ReproductorVideoViewModel y pantallas
    ├── audios/                    # AudiosViewModel y pantalla del reproductor
    └── theme/                     # Tema Material 3
```

**Buenas prácticas aplicadas**
- Patrón **MVVM**: `ViewModel` + `StateFlow` (recolectado con `collectAsStateWithLifecycle`) y
  un **repositorio** como fuente única de datos.
- **Estados de carga y error** en todas las pantallas (`UiState`), con botón *Reintentar*.
- **Manejo de errores de reproducción** con `Player.Listener.onPlayerError`, traducidos a un
  mensaje comprensible (Snackbar en audios, mensaje con reintento en video).
- Imágenes cargadas con **Coil** con **tamaño de decodificación acotado** y caché de memoria
  limitada al 20 %.
- Comentarios **KDoc** en clases y funciones públicas.
- Pruebas unitarias básicas (`app/src/test`).

**Eficiencia y ciclo de vida**
- El `ExoPlayer` de video se **libera en `onDispose`** (`DisposableEffect`) y se **pausa en `ON_STOP`**.
- Un **solo reproductor de audio compartido** (`AudioPlayerManager`): se **pausa al salir de la
  pantalla**, se **libera en `ON_STOP`** (conservando la posición para reanudar) y en `onCleared`.
  Gestiona el foco de audio y pausa al desconectar los audífonos.
- `LazyColumn` / `LazyVerticalGrid` / `LazyRow` con **`key`** estables (y `contentType` en la galería).
- La barra de progreso solo se actualiza mientras hay reproducción.

## 🎞️ Recursos multimedia

Los recursos de ejemplo se generaron con el script [`tools/generar_recursos.py`](tools/generar_recursos.py):

| Tipo | Ubicación | Cómo se generaron |
|---|---|---|
| 18 imágenes JPG (480×480) + 2 miniaturas | `app/src/main/res/drawable-nodpi/` | Pillow + emojis Noto Color Emoji |
| 2 videos MP4 (H.264, 8 s, 640×360) | `app/src/main/res/raw/video_*.mp4` | ffmpeg (animación + tono) |
| 5 audios MP3 (15–20 s) | `app/src/main/res/raw/audio_cuidado_*.mp3` | espeak-ng (TTS en español) + tono con ffmpeg |

Para regenerarlos: `sudo apt install ffmpeg espeak-ng fonts-noto-color-emoji && pip install pillow && python3 tools/generar_recursos.py`

## 📸 Capturas de pantalla

> _Pendientes._ Se agregarán en la carpeta `docs/capturas/`.

| Catálogo | Detalle | Galería | Video | Audios | Tablet |
|---|---|---|---|---|---|
| _pendiente_ | _pendiente_ | _pendiente_ | _pendiente_ | _pendiente_ | _pendiente_ |

## 📲 Cómo instalar el APK

### Opción 1: descargar el APK compilado por GitHub Actions
1. Ve a la pestaña **[Actions](https://github.com/SantiagoZu/tienda-mascotas-android/actions/workflows/build.yml)** del repositorio.
2. Abre la ejecución más reciente del workflow **Build APK** que esté en verde ✅.
3. En la sección **Artifacts**, descarga **`app-debug`** (se descarga un `.zip`) y descomprímelo para obtener `app-debug.apk`.
4. Instálalo:
   - **En el celular:** copia el APK al teléfono, ábrelo y acepta *"Permitir instalar apps de origen desconocido"* cuando lo pida.
   - **Con ADB:** conecta el celular con la depuración USB activada y ejecuta
     ```bash
     adb install -r app-debug.apk
     ```

### Opción 2: compilar desde Android Studio
1. Clona el repositorio:
   ```bash
   git clone https://github.com/SantiagoZu/tienda-mascotas-android.git
   ```
2. Ábrelo en **Android Studio** (Ladybug o superior) y espera la sincronización de Gradle.
3. Ejecuta la configuración **app** en un emulador o dispositivo (**Android 8.0 / API 26** o superior).

También desde la terminal (requiere JDK 17 y Android SDK):
```bash
./gradlew assembleDebug
# APK generado en app/build/outputs/apk/debug/app-debug.apk
```

## 🔧 Tecnologías

- Kotlin 2.0 · Jetpack Compose (BOM 2024.12) · Material 3 · material3-window-size-class
- Navigation Compose · Lifecycle ViewModel / runtime-compose · Kotlin Coroutines + StateFlow
- Media3 ExoPlayer + UI 1.5 · Coil 2.7
- Gradle 8.11 (Kotlin DSL + version catalog) · AGP 8.7 · compileSdk 35 · minSdk 26
- GitHub Actions (JDK 17) para compilar y publicar el APK

## 🔄 Integración continua

El workflow [`.github/workflows/build.yml`](.github/workflows/build.yml) se ejecuta en cada push y
pull request: configura **JDK 17**, corre las pruebas unitarias, compila el **APK debug** con Gradle
y lo publica como artifact **`app-debug`**.

---
Proyecto académico — UNIMINUTO · Desarrollo Móvil
