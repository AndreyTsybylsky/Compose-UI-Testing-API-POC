package org.example.project

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

class IosPlatform : Platform {
    override val name: String = "iOS Simulator/Device"
}

actual fun getPlatform(): Platform = IosPlatform()

@Composable
actual fun NativeBanner(modifier: Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(Color(0xFF2D2D2D), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Native Component: UIKit View (e.g., Apple Maps)",
            color = Color.LightGray,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
