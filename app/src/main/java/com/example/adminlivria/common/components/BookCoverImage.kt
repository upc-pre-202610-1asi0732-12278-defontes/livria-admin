package com.example.adminlivria.common.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/**
 * Composable que muestra la portada de un libro.
 * - Si [cover] empieza con "data:image" (Base64), lo decodifica manualmente a Bitmap.
 * - Si es una URL normal, usa Coil (AsyncImage).
 * Coil no soporta data URIs nativamente, por eso se requiere este componente.
 */
@Composable
fun BookCoverImage(
    cover: String,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (cover.startsWith("data:image")) {
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
        }
    } else {
        AsyncImage(
            model = cover,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}
