package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WifiQrFabIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        modifier = modifier.size(28.dp),
        contentAlignment = Alignment.Center
    ) {
        // Main Wi-Fi symbol offset slightly up-left
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = "Connection Button",
            tint = tint,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.TopStart)
                .offset(x = (-1.5).dp, y = (-1.5).dp)
        )

        // Accent QR badge in bottom-right corner
        Box(
            modifier = Modifier
                .size(17.dp)
                .align(Alignment.BottomEnd)
                .background(
                    color = tint,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(1.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCode,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
