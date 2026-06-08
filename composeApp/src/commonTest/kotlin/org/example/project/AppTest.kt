package org.example.project

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import kotlin.test.AfterTest
import kotlin.test.Test

class AppTest {

    @AfterTest
    fun tearDown() {
        // Reset override after each test to keep environment clean
        PlatformProvider.nativeBannerOverride = null
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testValidationErrors() = runComposeUiTest {
        setContent {
            App()
        }

        // Capture initial login screen
        saveScreenshot("validation_login_start.png", onRoot().captureToImage())

        // Initially we should be on the login screen
        onNodeWithTag("login_card").assertExists()
        onNodeWithTag("dashboard_card").assertDoesNotExist()

        // 1. Try with empty credentials
        onNodeWithTag("login_button").performClick()
        onNodeWithTag("error_text").assertTextEquals("Username must be at least 4 characters")

        // Capture validation error 1
        saveScreenshot("validation_error_empty.png", onRoot().captureToImage())

        // 2. Type username too short
        onNodeWithTag("username_input").performTextReplacement("abc")
        onNodeWithTag("login_button").performClick()
        onNodeWithTag("error_text").assertTextEquals("Username must be at least 4 characters")

        // 3. Type username OK but password too short
        onNodeWithTag("username_input").performTextReplacement("kotlinUser")
        onNodeWithTag("password_input").performTextReplacement("12345")
        onNodeWithTag("login_button").performClick()
        onNodeWithTag("error_text").assertTextEquals("Password must be at least 6 characters")

        // Capture validation error 2
        saveScreenshot("validation_error_password.png", onRoot().captureToImage())
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testSuccessfulLogin() = runComposeUiTest {
        setContent {
            App()
        }

        // Enter valid credentials
        onNodeWithTag("username_input").performTextInput("KotlinMultiplatform")
        onNodeWithTag("password_input").performTextInput("superSecretPassword")

        // Capture entered credentials
        saveScreenshot("login_filled.png", onRoot().captureToImage())

        // Perform login
        onNodeWithTag("login_button").performClick()

        // Verify dashboard is shown
        onNodeWithTag("login_card").assertDoesNotExist()
        onNodeWithTag("dashboard_card").assertExists()

        // Verify custom greeting message contains the username
        onNodeWithTag("welcome_text").assertTextEquals("Hello, KotlinMultiplatform!")

        // Capture dashboard
        saveScreenshot("login_success_dashboard.png", onRoot().captureToImage())

        // Log out
        onNodeWithTag("logout_button").performClick()
        onNodeWithTag("login_card").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testNativeComponentMocking() = runComposeUiTest {
        // Override the expect/actual component with a test stub
        PlatformProvider.nativeBannerOverride = { modifier ->
            Text(
                text = "Mock Native Component: Standard Compose Stub",
                modifier = modifier
            )
        }

        setContent {
            App()
        }

        // Verify that the overridden mock banner is rendered instead of the native actual implementation
        onNodeWithTag("native_banner").assertExists()
        onNodeWithTag("native_banner").assertTextEquals("Mock Native Component: Standard Compose Stub")

        // Capture mocked component
        saveScreenshot("native_component_mocked.png", onRoot().captureToImage())
    }
}
