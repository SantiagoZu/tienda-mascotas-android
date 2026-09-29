#!/usr/bin/env python3
"""
Genera los recursos multimedia de ejemplo de la app (imágenes, videos y audios)
para que funcione sin conexión a internet.

Requisitos: Python 3 + Pillow, ffmpeg (libx264, libmp3lame) y espeak-ng.
La fuente de emojis usada es Noto Color Emoji (paquete fonts-noto-color-emoji).

Uso:  python3 tools/generar_recursos.py
"""
import os
import subprocess
import tempfile

from PIL import Image, ImageDraw, ImageFont

RAIZ = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(RAIZ, "app", "src", "main", "res")
DRAWABLE = os.path.join(RES, "drawable-nodpi")
RAW = os.path.join(RES, "raw")

FUENTE_EMOJI = "/usr/share/fonts/truetype/noto/NotoColorEmoji.ttf"
FUENTE_TEXTO = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"

LADO = 480  # Tamaño acotado de las imágenes (px) para no inflar el APK.

# (nombre del recurso, emojis, color1, color2, texto)
PRODUCTOS = [
    ("prod_concentrado_perro", "🦴🐕", "#FFB74D", "#FF7043", "Concentrado"),
    ("prod_alimento_gato", "🐟🐈", "#81D4FA", "#4FC3F7", "Alimento gato"),
    ("prod_collar", "📿🐶", "#CE93D8", "#AB47BC", "Collar"),
    ("prod_cama", "🛏️🐾", "#A5D6A7", "#66BB6A", "Cama"),
    ("prod_rascador", "🪵🐱", "#FFCC80", "#8D6E63", "Rascador"),
    ("prod_jaula_aves", "🏠🐦", "#FFF59D", "#FBC02D", "Jaula"),
    ("prod_acuario", "🐠🫧", "#80DEEA", "#0097A7", "Acuario"),
    ("prod_juguete", "🎾🧸", "#F48FB1", "#EC407A", "Juguetes"),
    ("prod_arena", "🪣🐈", "#E6EE9C", "#9E9D24", "Arena"),
    ("prod_casa_hamster", "🏡🐹", "#BCAAA4", "#795548", "Casa hámster"),
]

MASCOTAS = [
    ("pet_max", "🐕", "#FFE0B2", "#FFB74D", "Max"),
    ("pet_luna", "🐈", "#E1BEE7", "#BA68C8", "Luna"),
    ("pet_kiwi", "🦜", "#C8E6C9", "#66BB6A", "Kiwi"),
    ("pet_nemo", "🐠", "#B3E5FC", "#29B6F6", "Nemo"),
    ("pet_coco", "🐇", "#F8BBD0", "#F06292", "Coco"),
    ("pet_bruno", "🐹", "#D7CCC8", "#A1887F", "Bruno"),
    ("pet_toby", "🐶", "#FFF9C4", "#FFD54F", "Toby"),
    ("pet_mia", "😺", "#D1C4E9", "#9575CD", "Mía"),
]

VIDEOS = [
    ("video_perro_jugando", "🐕", "🎾", "#FFE0B2", "#FF8A65", "Rocky juega con su pelota"),
    ("video_gato_curioso", "🐈", "🧶", "#E1BEE7", "#7E57C2", "Nala explora su nuevo hogar"),
]

AUDIOS = [
    ("audio_cuidado_perros", 440,
     "Consejos para el cuidado de tu perro. Pasea a tu perro al menos dos veces al día. "
     "Ofrécele agua fresca siempre y un concentrado de calidad según su edad y tamaño. "
     "Mantén al día sus vacunas y desparasitaciones, y visita al veterinario una vez al año."),
    ("audio_cuidado_gatos", 523,
     "Consejos para el cuidado de tu gato. Limpia la caja de arena todos los días. "
     "Dale un rascador para que afile sus uñas y juega con él para que haga ejercicio. "
     "Cepilla su pelaje con frecuencia para evitar bolas de pelo."),
    ("audio_cuidado_aves", 659,
     "Consejos para el cuidado de tus aves. Ubica la jaula en un lugar iluminado, "
     "lejos de corrientes de aire y del sol directo. Cambia el agua y las semillas a diario "
     "y ofrécele frutas y verduras frescas."),
    ("audio_cuidado_peces", 587,
     "Consejos para el cuidado de tus peces. No sobrealimentes a tus peces: "
     "dales solo lo que coman en dos minutos. Cambia una parte del agua cada semana "
     "y revisa que el filtro y la temperatura funcionen bien."),
    ("audio_cuidado_roedores", 698,
     "Consejos para el cuidado de tus roedores. Hámsteres, conejos y cobayos necesitan "
     "una jaula amplia, con viruta limpia y un lugar para esconderse. "
     "Dales heno, verduras frescas y juguetes para roer que cuiden sus dientes."),
]


def hex_rgb(color):
    color = color.lstrip("#")
    return tuple(int(color[i:i + 2], 16) for i in (0, 2, 4))


def degradado(ancho, alto, c1, c2):
    """Crea un fondo con degradado diagonal entre dos colores."""
    a, b = hex_rgb(c1), hex_rgb(c2)
    img = Image.new("RGB", (ancho, alto))
    px = img.load()
    for y in range(alto):
        for x in range(ancho):
            t = (x / ancho + y / alto) / 2
            px[x, y] = tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3))
    return img


def emoji(texto, alto):
    """Renderiza emojis a color (la fuente solo soporta tamaño 109) y los escala."""
    fuente = ImageFont.truetype(FUENTE_EMOJI, 109)
    tmp = Image.new("RGBA", (160 * len(texto), 160), (0, 0, 0, 0))
    ImageDraw.Draw(tmp).text((8, 8), texto, font=fuente, embedded_color=True)
    tmp = tmp.crop(tmp.getbbox())
    factor = alto / tmp.height
    return tmp.resize((int(tmp.width * factor), alto), Image.LANCZOS)


def tarjeta(nombre, emojis, c1, c2, texto, destino, ancho=LADO, alto=LADO):
    img = degradado(ancho, alto, c1, c2).convert("RGBA")
    draw = ImageDraw.Draw(img)
    # Círculos decorativos semitransparentes.
    capa = Image.new("RGBA", img.size, (0, 0, 0, 0))
    dc = ImageDraw.Draw(capa)
    dc.ellipse((-80, -80, 200, 200), fill=(255, 255, 255, 60))
    dc.ellipse((ancho - 160, alto - 200, ancho + 100, alto + 60), fill=(255, 255, 255, 50))
    img = Image.alpha_composite(img, capa)
    e = emoji(emojis, int(alto * 0.42))
    if e.width > ancho * 0.85:
        e = e.resize((int(ancho * 0.85), int(e.height * ancho * 0.85 / e.width)), Image.LANCZOS)
    img.alpha_composite(e, ((ancho - e.width) // 2, int(alto * 0.18)))
    draw = ImageDraw.Draw(img)
    fuente = ImageFont.truetype(FUENTE_TEXTO, int(alto * 0.08))
    w = draw.textlength(texto, font=fuente)
    y = int(alto * 0.74)
    draw.rounded_rectangle(((ancho - w) / 2 - 24, y - 12, (ancho + w) / 2 + 24, y + alto * 0.08 + 18),
                           radius=28, fill=(255, 255, 255, 215))
    draw.text(((ancho - w) / 2, y), texto, font=fuente, fill=(55, 55, 55))
    img.convert("RGB").save(os.path.join(destino, nombre + ".jpg"), quality=82, optimize=True)


def generar_video(nombre, emoji_mascota, emoji_juguete, c1, c2, titulo, tmpdir):
    ancho, alto = 640, 360
    fondo = degradado(ancho, alto, c1, c2)
    d = ImageDraw.Draw(fondo)
    fuente = ImageFont.truetype(FUENTE_TEXTO, 26)
    w = d.textlength(titulo, font=fuente)
    d.text(((ancho - w) / 2, 22), titulo, font=fuente, fill=(60, 60, 60))
    d.text((20, alto - 40), "Tienda de Mascotas UNIMINUTO", font=ImageFont.truetype(FUENTE_TEXTO, 16),
           fill=(80, 80, 80))
    fondo_png = os.path.join(tmpdir, nombre + "_fondo.png")
    fondo.save(fondo_png)
    mascota_png = os.path.join(tmpdir, nombre + "_m.png")
    emoji(emoji_mascota, 150).save(mascota_png)
    juguete_png = os.path.join(tmpdir, nombre + "_j.png")
    emoji(emoji_juguete, 70).save(juguete_png)
    salida = os.path.join(RAW, nombre + ".mp4")
    filtro = (
        "[0][1]overlay=x='(W-w)/2+180*sin(t*1.3)':y='H-h-60-abs(60*sin(t*3))'[a];"
        "[a][2]overlay=x='(W-w)/2-200*sin(t*1.3+1.2)':y='H-h-70-abs(110*sin(t*4))',format=yuv420p[v]"
    )
    subprocess.run([
        "ffmpeg", "-y", "-loglevel", "error",
        "-loop", "1", "-framerate", "24", "-i", fondo_png,
        "-loop", "1", "-framerate", "24", "-i", mascota_png,
        "-loop", "1", "-framerate", "24", "-i", juguete_png,
        "-f", "lavfi", "-i", "sine=frequency=330:beep_factor=2:sample_rate=44100",
        "-filter_complex", filtro, "-map", "[v]", "-map", "3:a",
        "-af", "volume=0.15", "-t", "8",
        "-c:v", "libx264", "-profile:v", "baseline", "-level", "3.0", "-crf", "30",
        "-preset", "veryslow", "-c:a", "aac", "-b:a", "48k", "-movflags", "+faststart",
        salida,
    ], check=True)
    # Miniatura del video para la galería.
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-ss", "1.5", "-i", salida,
                    "-frames:v", "1", "-q:v", "5",
                    os.path.join(DRAWABLE, nombre.replace("video_", "thumb_") + ".jpg")], check=True)


def generar_audio(nombre, frecuencia, texto, tmpdir):
    voz = os.path.join(tmpdir, nombre + ".wav")
    subprocess.run(["espeak-ng", "-v", "es-419", "-s", "150", "-w", voz, texto], check=True)
    salida = os.path.join(RAW, nombre + ".mp3")
    # Campanita de entrada (tono con ffmpeg) + narración TTS.
    filtro = (
        f"sine=frequency={frecuencia}:duration=0.6,volume=0.3,afade=t=out:st=0.2:d=0.4[c];"
        "[0:a]aresample=22050,aformat=channel_layouts=mono[v];"
        "[c]aresample=22050,aformat=channel_layouts=mono[c2];"
        "[c2][v]concat=n=2:v=0:a=1[out]"
    )
    subprocess.run(["ffmpeg", "-y", "-loglevel", "error", "-i", voz, "-filter_complex", filtro,
                    "-map", "[out]", "-c:a", "libmp3lame", "-b:a", "48k", salida], check=True)


def main():
    os.makedirs(DRAWABLE, exist_ok=True)
    os.makedirs(RAW, exist_ok=True)
    for p in PRODUCTOS:
        tarjeta(*p, destino=DRAWABLE)
    for m in MASCOTAS:
        tarjeta(*m, destino=DRAWABLE)
    with tempfile.TemporaryDirectory() as tmp:
        for v in VIDEOS:
            generar_video(*v, tmpdir=tmp)
        for a in AUDIOS:
            generar_audio(*a, tmpdir=tmp)
    print("Recursos generados en", RES)


if __name__ == "__main__":
    main()
