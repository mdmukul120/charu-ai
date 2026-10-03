package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CharuAction
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

@Composable
fun ControllerHubScreen(
    isAccessibilityOnline: Boolean,
    onExecuteAction: (CharuAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var testClickText by remember { mutableStateOf("Search") }
    var testTypeText by remember { mutableStateOf("Hello from Charu OS") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Accessibility Status Card
        item {
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (isAccessibilityOnline) CyberEmerald else CyberGold
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAccessibilityOnline) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isAccessibilityOnline) CyberEmerald else CyberGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACCESSIBILITY CONTROLLER",
                                color = CyberCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isAccessibilityOnline) CyberEmerald.copy(alpha = 0.2f) else CyberGold.copy(alpha = 0.2f))
                                .border(
                                    width = 1.dp,
                                    color = if (isAccessibilityOnline) CyberEmerald else CyberGold,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isAccessibilityOnline) "ACTIVE" else "STANDBY",
                                color = if (isAccessibilityOnline) CyberEmerald else CyberGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isAccessibilityOnline) {
                            "Charu OS is granted real-time accessibility permissions. Full hardware and screen controller commands (Global Home, Back, Recents, Lock, Click, and Type) execute directly on device."
                        } else {
                            "Accessibility service is not active. Charu will automatically fallback to intent dispatch and simulated execution. Enable accessibility to grant direct screen tapping and global gestures."
                        },
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Fallback
                                val generalSettings = Intent(Settings.ACTION_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(generalSettings)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAccessibilityOnline) CyberSurfaceBorder else CyberCyan,
                            contentColor = if (isAccessibilityOnline) CyberCyan else CyberDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_accessibility_settings_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAccessibilityOnline) "Manage Accessibility Settings" else "Enable Charu Accessibility Service",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Live Action Test Suite
        item {
            Text(
                text = "SUPPORTED ACTIONS TEST PANEL",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        // Global Navigation Actions Grid
        item {
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "GLOBAL SYSTEM DIRECTIVES",
                        color = CyberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionCard(
                            title = "GLOBAL_HOME",
                            subtitle = "Go to home",
                            icon = Icons.Default.Home,
                            onClick = { onExecuteAction(CharuAction(action = "GLOBAL_HOME")) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionCard(
                            title = "GLOBAL_BACK",
                            subtitle = "Press Back",
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = { onExecuteAction(CharuAction(action = "GLOBAL_BACK")) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionCard(
                            title = "GLOBAL_RECENTS",
                            subtitle = "Open Recent Apps",
                            icon = Icons.Default.Layers,
                            onClick = { onExecuteAction(CharuAction(action = "GLOBAL_RECENTS")) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionCard(
                            title = "LOCK_SCREEN",
                            subtitle = "Lock Device",
                            icon = Icons.Default.Lock,
                            onClick = { onExecuteAction(CharuAction(action = "LOCK_SCREEN")) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionCard(
                            title = "SCROLL_DOWN",
                            subtitle = "Scroll down screen",
                            icon = Icons.Default.ArrowDownward,
                            onClick = { onExecuteAction(CharuAction(action = "SCROLL_DOWN")) },
                            modifier = Modifier.weight(1f)
                        )
                        ActionCard(
                            title = "SCROLL_UP",
                            subtitle = "Scroll up screen",
                            icon = Icons.Default.ArrowUpward,
                            onClick = { onExecuteAction(CharuAction(action = "SCROLL_UP")) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Click Text & Type Text Test
        item {
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "UI INTERACTION DIRECTIVES",
                        color = CyberGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Click Text Tester
                    Text(
                        text = "CLICK_TEXT Directive",
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = testClickText,
                            onValueChange = { testClickText = it },
                            label = { Text("Target Button / Visible Text", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary,
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberSurfaceBorder,
                                focusedContainerColor = CyberDark,
                                unfocusedContainerColor = CyberDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onExecuteAction(CharuAction(action = "CLICK_TEXT", targetText = testClickText))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Test Click", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Type Text Tester
                    Text(
                        text = "TYPE_TEXT Directive",
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = testTypeText,
                            onValueChange = { testTypeText = it },
                            label = { Text("Text to Input into Focused Field", fontSize = 11.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CyberTextPrimary,
                                unfocusedTextColor = CyberTextPrimary,
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberSurfaceBorder,
                                focusedContainerColor = CyberDark,
                                unfocusedContainerColor = CyberDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onExecuteAction(CharuAction(action = "TYPE_TEXT", text = testTypeText))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberDark),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Test Type", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberDark,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.8.dp, CyberSurfaceBorder),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = CyberTextSecondary,
                fontSize = 10.sp
            )
        }
    }
}
