package com.shopping.shoply.profile.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

/** Turns a picked image into a small Base64 JPEG for Firestore, and back into bytes for display. */
class PhotoEncoder @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /** Base64 JPEG whose longest side is at most [ProfilePhoto.MAX_SIDE_PX], or null if the image can't be read. */
    suspend fun encode(uri: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeScaled(Uri.parse(uri)) ?: return@runCatching null
            val jpeg = ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, ProfilePhoto.JPEG_QUALITY, out)
                out.toByteArray()
            }
            bitmap.recycle()
            Base64.encodeToString(jpeg, Base64.NO_WRAP)
        }.getOrNull()
    }

    /** The JPEG bytes, or null when [base64] is malformed. */
    fun decode(base64: String): ByteArray? =
        runCatching { Base64.decode(base64, Base64.NO_WRAP) }.getOrNull()

    private fun decodeScaled(uri: Uri): Bitmap? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder also applies the EXIF rotation, so camera photos stay upright.
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val (width, height) = scaledSize(info.size.width, info.size.height)
                decoder.setTargetSize(width, height)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?.let { full ->
                    val (width, height) = scaledSize(full.width, full.height)
                    Bitmap.createScaledBitmap(full, width, height, true)
                        .also { if (it !== full) full.recycle() }
                }
        }

    private fun scaledSize(width: Int, height: Int): Pair<Int, Int> {
        val scale = minOf(1f, ProfilePhoto.MAX_SIDE_PX.toFloat() / max(width, height))
        return (width * scale).roundToInt().coerceAtLeast(1) to (height * scale).roundToInt().coerceAtLeast(1)
    }
}
