package org.example.project

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * PlatformProvider allows common UI tests in commonTest to override platform-specific
 * native components (like native maps, payment gateways, etc.) with mock/stub components.
 */
object PlatformProvider {
    // Allows tests to plug in a mock/stub UI implementation of the native banner
    var nativeBannerOverride: (@Composable (Modifier) -> Unit)? = null

    @Composable
    fun RenderNativeBanner(modifier: Modifier = Modifier) {
        val override = nativeBannerOverride
        if (override != null) {
            override(modifier)
        } else {
            NativeBanner(modifier)
        }
    }
}
