package org.example.project

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Cross-platform helper to save an ImageBitmap screenshot to a file during UI tests.
 */
expect fun saveScreenshot(name: String, bitmap: ImageBitmap)
