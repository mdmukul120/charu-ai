package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CharuHeader
import com.example.ui.screens.AppMapperScreen
import com.example.ui.screens.ControllerHubScreen
import com.example.ui.screens.MacroStudioScreen
import com.example.ui.screens.TelemetryLogsScreen
import com.example.ui.screens.TerminalHudScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.CharuViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CharuMainScreen()
            }
        }
    }
}

data class NavTabItem(
    val title: String,
    val icon: ImageVector,
    val tag: String
)

@Composable
fun CharuMainScreen(
    vm: CharuViewModel = viewModel()
) {
    val context = LocalContext.current

    val isAccessibilityOnline by vm.isServiceConnected.collectAsStateWithLifecycle()
    val isListening by vm.isListening.collectAsStateWithLifecycle()
    val isProcessing by vm.isProcessing.collectAsStateWithLifecycle()
    val audioRms by vm.audioRms.collectAsStateWithLifecycle()
    val isContinuousListening by vm.isContinuousListening.collectAsStateWithLifecycle()
    val isFloatingBubbleActive by vm.isFloatingBubbleActive.collectAsStateWithLifecycle()

    val currentInput by vm.currentInput.collectAsStateWithLifecycle()
    val latestResponse by vm.latestResponse.collectAsStateWithLifecycle()
    val latestRawJson by vm.latestRawJson.collectAsStateWithLifecycle()
    val executionSteps by vm.executionSteps.collectAsStateWithLifecycle()
    val commandHistory by vm.commandHistory.collectAsStateWithLifecycle()
    val installedApps by vm.installedApps.collectAsStateWithLifecycle()
    val selectedTab by vm.selectedTab.collectAsStateWithLifecycle()
    val customJsonInput by vm.customJsonInput.collectAsStateWithLifecycle()
    val systemStatusText by vm.systemStatusText.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            vm.startVoiceInput(onPermissionNeeded = {})
        } else {
            Toast.makeText(context, "Microphone permission is required for voice commands", Toast.LENGTH_SHORT).show()
        }
    }

    val overlayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context)) {
            vm.toggleFloatingBubble(context) {}
        } else {
            Toast.makeText(context, "Overlay permission needed for floating bubble", Toast.LENGTH_SHORT).show()
        }
    }

    val onToggleContinuous = {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            vm.toggleContinuousListening(onPermissionNeeded = {})
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val onToggleBubble = {
        vm.toggleFloatingBubble(context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    )
                    overlayLauncher.launch(intent)
                } catch (e: Exception) {
                    val fallbackIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    overlayLauncher.launch(fallbackIntent)
                }
            }
        }
    }

    val tabs = listOf(
        NavTabItem("Terminal", Icons.Default.Terminal, "tab_terminal"),
        NavTabItem("Controller", Icons.Default.SettingsInputComponent, "tab_controller"),
        NavTabItem("App Map", Icons.Default.Apps, "tab_appmap"),
        NavTabItem("Macros", Icons.Default.Code, "tab_macros"),
        NavTabItem("Logs", Icons.Default.History, "tab_logs")
    )

    BackHandler(enabled = selectedTab > 0) {
        vm.setTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CharuHeader(
                isAccessibilityOnline = isAccessibilityOnline,
                isVoiceOutputEnabled = vm.isVoiceOutputEnabled(),
                onToggleVoice = { vm.toggleVoiceOutput() },
                isContinuousListening = isContinuousListening,
                onToggleContinuous = onToggleContinuous,
                isFloatingBubbleActive = isFloatingBubbleActive,
                onToggleBubble = onToggleBubble
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberSurface,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(width = 0.8.dp, color = CyberSurfaceBorder)
                    .testTag("charu_bottom_nav")
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { vm.setTab(index) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) CyberCyan else CyberTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) CyberCyan else CyberTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyberCyan.copy(alpha = 0.15f),
                            selectedIconColor = CyberCyan,
                            unselectedIconColor = CyberTextSecondary
                        ),
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        },
        containerColor = CyberDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberDark)
        ) {
            when (selectedTab) {
                0 -> {
                    TerminalHudScreen(
                        currentInput = currentInput,
                        onInputChange = { vm.updateCurrentInput(it) },
                        onSubmitCommand = { vm.submitCommand(it) },
                        isListening = isListening,
                        isProcessing = isProcessing,
                        audioRms = audioRms,
                        onOrbClick = {
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                vm.startVoiceInput(onPermissionNeeded = {})
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        isContinuousListening = isContinuousListening,
                        onToggleContinuous = onToggleContinuous,
                        isFloatingBubbleActive = isFloatingBubbleActive,
                        onToggleBubble = onToggleBubble,
                        statusText = systemStatusText,
                        latestResponse = latestResponse,
                        latestRawJson = latestRawJson,
                        executionSteps = executionSteps,
                        onCopyJson = { vm.copyJsonToClipboard(context, it) }
                    )
                }

                1 -> {
                    ControllerHubScreen(
                        isAccessibilityOnline = isAccessibilityOnline,
                        onExecuteAction = { vm.executeDirectAction(it) }
                    )
                }

                2 -> {
                    AppMapperScreen(
                        installedApps = installedApps,
                        onLaunchApp = { vm.executeDirectAction(it) }
                    )
                }

                3 -> {
                    MacroStudioScreen(
                        customJson = customJsonInput,
                        onJsonChange = { vm.updateCustomJsonInput(it) },
                        onExecuteJson = { vm.executeRawJsonDirectly(it) },
                        onCopyJson = { vm.copyJsonToClipboard(context, it) }
                    )
                }

                4 -> {
                    TelemetryLogsScreen(
                        historyItems = commandHistory,
                        onReExecute = { vm.submitCommand(it) },
                        onCopyJson = { vm.copyJsonToClipboard(context, it) },
                        onClearHistory = { vm.clearHistory() }
                    )
                }
            }
        }
    }
}
