package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DirectIpConnectDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyQrCodeScreen
import com.example.ui.screens.ScanQrScreen
import com.example.ui.screens.SettingsAboutScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.WiChatTheme

object WiChatRoutes {
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val CHAT = "chat"
    const val MY_QR = "my_qr"
    const val SCAN_QR = "scan_qr"
    const val SETTINGS = "settings"
}

@Composable
fun WiChatApp(
    viewModel: WiChatViewModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val userProfile by viewModel.userProfile.collectAsState()
    val networkInfo by viewModel.networkInfo.collectAsState()
    val allPeers by viewModel.allPeers.collectAsState()
    val allMessages by viewModel.allMessages.collectAsState()
    val currentChatPeer by viewModel.currentChatPeer.collectAsState()
    val currentMessages by viewModel.currentMessages.collectAsState()
    val directIpState by viewModel.directIpState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    var showDirectIpDialog by remember { mutableStateOf(false) }

    val startDestination = if (userProfile.isRegistered) WiChatRoutes.HOME else WiChatRoutes.WELCOME

    WiChatTheme(themeMode = themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(220)) }
        ) {
            composable(WiChatRoutes.WELCOME) {
                WelcomeScreen(
                    generatedUserId = userProfile.userId,
                    onProfileCreated = { name ->
                        viewModel.saveProfile(name)
                        navController.navigate(WiChatRoutes.HOME) {
                            popUpTo(WiChatRoutes.WELCOME) { inclusive = true }
                        }
                    }
                )
            }

            composable(WiChatRoutes.HOME) {
                HomeScreen(
                    userProfile = userProfile,
                    networkInfo = networkInfo,
                    allPeers = allPeers,
                    allMessages = allMessages,
                    onSelectPeer = { peerId ->
                        viewModel.selectPeer(peerId)
                        navController.navigate(WiChatRoutes.CHAT)
                    },
                    onOpenDirectIp = {
                        viewModel.resetDirectIpState()
                        showDirectIpDialog = true
                    },
                    onOpenMyQr = {
                        navController.navigate(WiChatRoutes.MY_QR)
                    },
                    onOpenScanQr = {
                        navController.navigate(WiChatRoutes.SCAN_QR)
                    },
                    onOpenSettings = {
                        navController.navigate(WiChatRoutes.SETTINGS)
                    },
                    onRefreshNetwork = {
                        viewModel.refreshNetwork()
                        Toast.makeText(context, "Scanning local network...", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            composable(WiChatRoutes.CHAT) {
                ChatScreen(
                    peer = currentChatPeer,
                    messages = currentMessages,
                    onSendMessage = { content ->
                        viewModel.sendMessage(content)
                    },
                    onRetryMessage = { message ->
                        viewModel.retryMessage(message)
                    },
                    onBack = {
                        viewModel.clearSelectedPeer()
                        navController.popBackStack()
                    }
                )
            }

            composable(WiChatRoutes.MY_QR) {
                MyQrCodeScreen(
                    userProfile = userProfile,
                    networkInfo = networkInfo,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(WiChatRoutes.SCAN_QR) {
                ScanQrScreen(
                    onQrPayloadReceived = { payload ->
                        viewModel.connectFromQr(
                            payload = payload,
                            onSuccess = { peer ->
                                navController.navigate(WiChatRoutes.CHAT) {
                                    popUpTo(WiChatRoutes.SCAN_QR) { inclusive = true }
                                }
                            },
                            onError = { error ->
                                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(WiChatRoutes.SETTINGS) {
                SettingsAboutScreen(
                    userProfile = userProfile,
                    networkInfo = networkInfo,
                    currentThemeMode = themeMode,
                    onSelectThemeMode = { newMode ->
                        viewModel.setThemeMode(newMode)
                    },
                    onSaveProfile = { newName ->
                        viewModel.saveProfile(newName)
                    },
                    onSaveProfileWithAvatar = { newName, newAvatarUri ->
                        viewModel.saveProfileWithAvatar(newName, newAvatarUri)
                    },
                    onCopyAvatarUri = { uri ->
                        viewModel.copyAvatarFromUri(uri)
                    },
                    onRemoveAvatar = {
                        viewModel.removeAvatar()
                    },
                    onOpenDirectIp = {
                        viewModel.resetDirectIpState()
                        showDirectIpDialog = true
                    },
                    onOpenQrConnect = {
                        navController.navigate(WiChatRoutes.SCAN_QR)
                    },
                    onOpenMyQr = {
                        navController.navigate(WiChatRoutes.MY_QR)
                    },
                    onOpenScanQr = {
                        navController.navigate(WiChatRoutes.SCAN_QR)
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (showDirectIpDialog) {
            DirectIpConnectDialog(
                currentNetworkIp = networkInfo.ipAddress,
                state = directIpState,
                onConnect = { ip, port ->
                    viewModel.connectDirectIp(ip, port) { peer ->
                        showDirectIpDialog = false
                        navController.navigate(WiChatRoutes.CHAT)
                    }
                },
                onDismiss = {
                    viewModel.resetDirectIpState()
                    showDirectIpDialog = false
                },
                onSuccessChat = { peer ->
                    showDirectIpDialog = false
                    navController.navigate(WiChatRoutes.CHAT)
                }
            )
        }
    }
}
}
