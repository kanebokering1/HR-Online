package com.binahr.util


import com.binahr.BuildConfig
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object CameraHelper {

    /**
     * Saves a CameraX ImageProxy to internal storage (attendance/selfies/ folder),
     * ALSO saves a copy to the device Gallery so employees can keep their selfie.
     * Returns (File, base64String) pair where File is the internal copy used for
     * in-app preview. The base64 payload is the same for both internal and gallery.
     *
     * Gallery save: API 29+ uses MediaStore; API 23–28 uses DIRECTORY_PICTURES + MediaScanner.
     */
    fun saveImageProxyLocally(
        context: Context,
        imageProxy: ImageProxy,
    ): Pair<File, String>? {
        return try {
            // Convert to Bitmap and fix orientation.
            val bitmap = imageProxyToBitmap(imageProxy) ?: return null

            // Mirror front-camera (selfie) horizontally so it looks natural.
            val matrix = Matrix().apply { postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f) }
            val mirrored = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // Compress at 60 % quality (smaller payload).
            val outputStream = ByteArrayOutputStream()
            mirrored.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val bytes = outputStream.toByteArray()

            // Save to app-internal storage (used for in-app preview).
            val dir = File(context.filesDir, "attendance/selfies").apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
            val file = File(dir, "selfie_$timestamp.jpg")
            file.writeBytes(bytes)

            // Also save to public Gallery so the employee can see it.
            saveToGallery(context, bytes, "selfie_$timestamp.jpg")

            // Base64 for API payload.
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            Pair(file, base64)
        } catch (e: Exception) {
            null
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val buffer = imageProxy.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Loads a previously saved selfie file as a Bitmap.
     * Returns null if the file does not exist or cannot be decoded.
     */
    fun loadSelfie(file: File?): Bitmap? {
        if (file == null || !file.exists()) return null
        return try {
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    /** Writes JPEG bytes to the public Gallery. Fire-and-forget — errors are silently ignored. */
    private fun saveToGallery(context: Context, bytes: ByteArray, fileName: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // API 29+: use MediaStore (no permission needed for own app's files)
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BinaHR")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    context.contentResolver.openOutputStream(it)?.use { out -> out.write(bytes) }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(it, values, null, null)
                }
            } else {
                // API 23–28: write directly to DIRECTORY_PICTURES and trigger media scan
                @Suppress("DEPRECATION")
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "BinaHR"
                ).apply { mkdirs() }
                val file = File(dir, fileName)
                file.writeBytes(bytes)
                MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf("image/jpeg"), null)
            }
        } catch (_: Exception) {
            // Gallery save is best-effort; don't crash the app
        }
    }
}
