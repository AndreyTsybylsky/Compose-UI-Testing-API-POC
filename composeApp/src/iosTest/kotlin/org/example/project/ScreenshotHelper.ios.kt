package org.example.project

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToFile
import platform.Foundation.NSFileManager

@OptIn(ExperimentalForeignApi::class)
actual fun saveScreenshot(name: String, bitmap: ImageBitmap) {
    val skiaBitmap = bitmap.asSkiaBitmap()
    val bytes = org.jetbrains.skia.Image.makeFromBitmap(skiaBitmap).encodeToData()?.bytes
    if (bytes != null) {
        val nsData = bytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
        }
        
        // We write to /tmp/screenshots on the iOS Simulator.
        // Since the simulator runs on the macOS host, /tmp is mapped to the macOS host's /tmp,
        // which makes it very easy to collect screenshot files in GitHub Actions!
        val fileManager = NSFileManager.defaultManager()
        val targetDir = "/tmp/screenshots"
        fileManager.createDirectoryAtPath(targetDir, withIntermediateDirectories = true, attributes = null, error = null)
        
        val path = "$targetDir/$name"
        val success = nsData.writeToFile(path, atomically = true)
        println("Saved iOS Simulator screenshot to: $path (success: $success)")
    } else {
        println("Failed to encode iOS Simulator screenshot to PNG bytes")
    }
}
