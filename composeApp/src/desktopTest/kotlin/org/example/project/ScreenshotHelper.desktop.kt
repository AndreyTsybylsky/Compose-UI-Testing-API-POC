package org.example.project

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import java.io.File
import javax.imageio.ImageIO

actual fun saveScreenshot(name: String, bitmap: ImageBitmap) {
    val buildDir = File("build/screenshots")
    if (!buildDir.exists()) {
        buildDir.mkdirs()
    }
    val file = File(buildDir, name)
    ImageIO.write(bitmap.toAwtImage(), "png", file)
    println("Saved JVM desktop screenshot to: ${file.absolutePath}")
}
