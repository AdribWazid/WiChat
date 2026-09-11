package com.example

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.model.LocalNetworkInfo
import com.example.model.UserProfile
import com.example.model.WiChatProtocol
import com.example.ui.screens.SettingsAboutScreen
import com.example.ui.theme.WiChatTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WiChat", appName)
    }

    @Test
    fun `test protocol beacon formatting and parsing`() {
        val beaconJson = WiChatProtocol.createBeaconJson("user_123", "Phone A", 8888)
        val parsed = WiChatProtocol.parseBeacon(beaconJson)
        assertNotNull(parsed)
        assertEquals("user_123", parsed?.userId)
        assertEquals("Phone A", parsed?.displayName)
        assertEquals(8888, parsed?.port)
    }

    @Test
    fun `test qr payload generation and parsing`() {
        val qrUri = WiChatProtocol.generateQrPayload("user_456", "Phone B", "192.168.1.150", 8888)
        val parsed = WiChatProtocol.parseQrPayload(qrUri)
        assertNotNull(parsed)
        assertEquals("user_456", parsed?.userId)
        assertEquals("Phone B", parsed?.displayName)
        assertEquals("192.168.1.150", parsed?.ipAddress)
        assertEquals(8888, parsed?.port)
    }

    @Test
    fun `test SettingsAboutScreen renders single About WiChat and Key Features`() {
        val userProfile = UserProfile(userId = "wc_testuser", displayName = "Tester", isRegistered = true)
        val networkInfo = LocalNetworkInfo(
            isConnectedToWifi = true,
            ipAddress = "192.168.1.50",
            subnetBroadcast = "192.168.1.255",
            listeningPort = 8888,
            ssid = "TestWiFi"
        )

        composeTestRule.setContent {
            WiChatTheme {
                SettingsAboutScreen(
                    userProfile = userProfile,
                    networkInfo = networkInfo,
                    onSaveProfile = {},
                    onOpenDirectIp = {},
                    onOpenQrConnect = {},
                    onOpenMyQr = {},
                    onOpenScanQr = {},
                    onBack = {}
                )
            }
        }

        // Verify Profile section and edit elements exist
        composeTestRule.onNodeWithTag("settings_profile_card").assertExists()
        composeTestRule.onNodeWithTag("profile_picture_container").assertExists()
        composeTestRule.onNodeWithTag("edit_profile_picture_badge").assertExists()
        composeTestRule.onNodeWithTag("change_picture_button").assertExists()
        composeTestRule.onNodeWithTag("edit_display_name_input").assertExists()
        composeTestRule.onNodeWithTag("save_profile_button").assertExists()

        // Verify About WiChat section exists
        composeTestRule.onNodeWithTag("about_wichat_section").assertExists()

        // Verify direct connection options exist
        composeTestRule.onNodeWithTag("settings_direct_ip_connect").assertExists()
        composeTestRule.onNodeWithTag("settings_qr_connect").assertExists()

        // Verify Developer Info section exists
        composeTestRule.onNodeWithTag("developer_info_section").assertExists()

        // Verify all 5 feature cards exist
        composeTestRule.onNodeWithTag("feature_same_wifi_network").assertExists()
        composeTestRule.onNodeWithTag("feature_direct_real_time").assertExists()
        composeTestRule.onNodeWithTag("feature_zero_cloud_experience").assertExists()
        composeTestRule.onNodeWithTag("feature_direct_ip_connect").assertExists()
        composeTestRule.onNodeWithTag("feature_qr_connect").assertExists()
    }

    @Test
    fun userPreferences_savesAndPersistsProfileAndAvatar() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val userPreferences = com.example.data.UserPreferences(context)

        // Save new display name and avatar URI
        userPreferences.saveProfileWithAvatar("Alice Wonderland", "file:///data/user/0/com.example/files/avatar.jpg")

        // Read back with a new instance to simulate app restart
        val reloadedPreferences = com.example.data.UserPreferences(context)
        val profile = reloadedPreferences.getUserProfile()

        org.junit.Assert.assertEquals("Alice Wonderland", profile.displayName)
        org.junit.Assert.assertTrue(profile.isRegistered)
    }
}

