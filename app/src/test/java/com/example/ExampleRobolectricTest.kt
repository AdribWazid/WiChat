package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.WiChatProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

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
}

