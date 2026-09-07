package com.example.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * MediaUploadService optimizes and processes images for cloud sharing.
 * Converts local device URIs (content://, file://) into lightweight, universally
 * renderable image formats and handles Cloudinary/Cloud distribution.
 */
object MediaUploadService {
    private const val TAG = "MediaUploadService"

    suspend fun prepareImageForPublishing(context: Context, uriString: String): String = withContext(Dispatchers.IO) {
        if (uriString.startsWith("http://") || uriString.startsWith("https://") || uriString.startsWith("data:image/")) {
            return@withContext uriString
        }

        try {
            val uri = Uri.parse(uriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                // Decode image bounds first to calculate inSampleSize
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()

                var sampleSize = 1
                val maxDimension = 1080
                if (options.outHeight > maxDimension || options.outWidth > maxDimension) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while (halfHeight / sampleSize >= maxDimension && halfWidth / sampleSize >= maxDimension) {
                        sampleSize *= 2
                    }
                }

                // Decode bitmap with calculated sampleSize
                val actualStream: InputStream? = context.contentResolver.openInputStream(uri)
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565 // Memory-efficient config
                }
                val originalBitmap = BitmapFactory.decodeStream(actualStream, null, decodeOptions)
                actualStream?.close()

                if (originalBitmap != null) {
                    val outputStream = ByteArrayOutputStream()
                    originalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                    val byteArray = outputStream.toByteArray()
                    originalBitmap.recycle()

                    // 1. Try Direct Upload to Supabase Storage Bucket
                    try {
                        val client = SupabaseClient()
                        val fileName = "img_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}.jpg"
                        val uploadResult = client.uploadFile(
                            bucket = com.example.config.SupabaseConfig.BUCKET_MEDIA,
                            path = "posts/$fileName",
                            fileBytes = byteArray,
                            mimeType = "image/jpeg"
                        )
                        if (uploadResult.isSuccess) {
                            val publicUrl = uploadResult.getOrNull()
                            if (!publicUrl.isNullOrBlank()) {
                                Log.i(TAG, "Image uploaded to Supabase Storage: $publicUrl")
                                return@withContext publicUrl
                            }
                        }
                    } catch (uploadEx: Exception) {
                        Log.w(TAG, "Supabase Storage direct upload fallback: ${uploadEx.message}")
                    }
                    
                    // 2. Fallback to optimized data URI
                    val base64Encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                    Log.d(TAG, "Image optimized with fallback base64 (${byteArray.size / 1024} KB)")
                    return@withContext "data:image/jpeg;base64,$base64Encoded"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to optimize image: ${e.message}", e)
            AppAnalytics.recordException(e, "Image optimization failure")
        }

        // Return original if conversion fails
        return@withContext uriString
    }

    suspend fun prepareImagesForPublishing(context: Context, uriStrings: List<String>): List<String> = withContext(Dispatchers.IO) {
        uriStrings.map { uriString ->
            prepareImageForPublishing(context, uriString)
        }
    }
}
