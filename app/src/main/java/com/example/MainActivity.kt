package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.WiChatApp
import com.example.ui.WiChatViewModel
import com.example.ui.theme.WiChatTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WiChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            WiChatTheme(themeMode = themeMode) {
                WiChatApp(viewModel = viewModel)
            }
        }
    }
}

