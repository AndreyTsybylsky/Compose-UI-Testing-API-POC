package org.example.project

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun NativeBanner(modifier: Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(Color(0xFF2D2D2D), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Native Component: Android View (e.g. Google Maps)",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
