package org.example.project

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.File
import java.io.FileOutputStream

actual fun saveScreenshot(name: String, bitmap: ImageBitmap) {
    try {
        val androidBitmap = bitmap.asAndroidBitmap()
        // Save to emulator sdcard screenshots folder
        val targetDir = File("/sdcard/screenshots")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        val file = File(targetDir, name)
        FileOutputStream(file).use { out ->
            androidBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        println("Saved Android screenshot to: ${file.absolutePath}")
    } catch (e: Exception) {
        println("Failed to save Android screenshot: ${e.message}")
    }
}
