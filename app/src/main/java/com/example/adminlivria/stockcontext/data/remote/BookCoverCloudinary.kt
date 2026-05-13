package com.example.adminlivria.stockcontext.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject

/**
 * Sube la portada a Cloudinary (unsigned preset) y devuelve [secure_url].
 * Misma cuenta/preset que la app user (`PaymentService` en livria-user).
 */
object BookCoverCloudinary {

    private const val CLOUD_NAME = "dd2fmyphr"
    private const val UPLOAD_PRESET = "livria_preset"
    private const val MAX_COVER_DIMENSION = 1600
    private const val JPEG_QUALITY = 88

    private val uploadUrl = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"
    private val httpClient = OkHttpClient()

    suspend fun uploadGalleryCover(context: Context, uriString: String): String? = withContext(Dispatchers.IO) {
        if (uriString.isBlank()) return@withContext null
        val uri = uriString.toUri()
        val jpegBytes = buildCoverJpegBytes(context, uri) ?: return@withContext null
        postJpeg(jpegBytes)
    }

    private fun buildCoverJpegBytes(context: Context, uri: Uri): ByteArray? {
        val bitmap = decodeAndScaleCoverBitmap(context, uri) ?: return null
        return try {
            val os = ByteArrayOutputStream()
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, os)) {
                null
            } else {
                os.toByteArray()
            }
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    private fun decodeAndScaleCoverBitmap(context: Context, uri: Uri): Bitmap? {
        val cr = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        bounds.inSampleSize = calculateInSampleSize(bounds, MAX_COVER_DIMENSION, MAX_COVER_DIMENSION)
        bounds.inJustDecodeBounds = false

        val decoded = cr.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: return null

        val maxSide = maxOf(decoded.width, decoded.height)
        if (maxSide <= MAX_COVER_DIMENSION) return decoded

        val scale = MAX_COVER_DIMENSION.toFloat() / maxSide
        val w = (decoded.width * scale).toInt().coerceAtLeast(1)
        val h = (decoded.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(decoded, w, h, true)
        if (scaled != decoded && !decoded.isRecycled) decoded.recycle()
        return scaled
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun postJpeg(jpegBytes: ByteArray): String? {
        val mediaType = "image/jpeg".toMediaType()
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .addFormDataPart("file", "cover.jpg", jpegBytes.toRequestBody(mediaType))
            .build()

        val request = Request.Builder()
            .url(uploadUrl)
            .post(body)
            .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                val str = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@use null
                JSONObject(str).optString("secure_url").takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            null
        }
    }
}
