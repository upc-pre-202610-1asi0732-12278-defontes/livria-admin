package com.example.adminlivria.common.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

// Placeholder gris neutro usado cuando la imagen no puede cargarse
private val placeholderColor = Color(0xFFE0E0E0)

/**
 * Composable que muestra la portada de un libro.
 * - Si [cover] empieza con "data:image" (Base64), lo decodifica manualmente a Bitmap.
 * - Si es una URL normal, usa Coil (AsyncImage).
 * - Si la imagen falla o el cover está vacío, muestra un placeholder gris.
 * Coil no soporta data URIs nativamente, por eso se requiere este componente.
 */
@Composable
fun BookCoverImage(
    cover: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    when {
        cover.isBlank() -> {
            Box(modifier = modifier.background(placeholderColor))
        }

        cover.startsWith("data:image") -> {
            val bitmap = remember(cover) {
                try {
                    val base64Data = cover.substringAfter("base64,")
                    val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (e: Exception) {
                    null
                }
            }

            if (bitmap != null) {
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = modifier
                )
            } else {
                Box(modifier = modifier.background(placeholderColor))
            }
        }

        else -> {
            AsyncImage(
                model = cover,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier,
                error = ColorPainter(placeholderColor),
                placeholder = ColorPainter(placeholderColor)
            )
        }
    }
}

