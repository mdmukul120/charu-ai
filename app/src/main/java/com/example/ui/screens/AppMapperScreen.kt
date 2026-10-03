package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CharuAction
import com.example.model.InstalledAppInfo
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

data class CommonPackage(
    val name: String,
    val packageName: String,
    val description: String
)

@Composable
fun AppMapperScreen(
    installedApps: List<InstalledAppInfo>,
    onLaunchApp: (CharuAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val commonPackages = listOf(
        CommonPackage("WhatsApp", "com.whatsapp", "Messaging & Calls"),
        CommonPackage("YouTube", "com.google.android.youtube", "Video Platform"),
        CommonPackage("Messenger", "com.facebook.orca", "Facebook Chat"),
        CommonPackage("Google Chrome", "com.android.chrome", "Web Browser"),
        CommonPackage("Settings", "com.android.settings", "System Settings"),
        CommonPackage("Phone / Dialer", "com.google.android.dialer", "Voice Calls"),
        CommonPackage("Messages", "com.google.android.apps.messaging", "SMS & Chat"),
        CommonPackage("Facebook", "com.facebook.katana", "Social Network")
    )

    val filteredApps = if (searchQuery.isBlank()) {
        installedApps
    } else {
        installedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Info
        item {
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PACKAGE MAPPER & DISPATCHER",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Charu AI resolves natural language app names to Android Package IDs. Test app opening or search installed device targets.",
                        color = CyberTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Common Core Package Map
        item {
            Text(
                text = "CHARU OS CORE PACKAGE MAP (8)",
                color = CyberGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        items(commonPackages) { pkg ->
            CommonPackageCard(
                pkg = pkg,
                onLaunch = {
                    onLaunchApp(CharuAction(action = "OPEN_APP", packageName = pkg.packageName))
                },
                onCopy = {
                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cb.setPrimaryClip(ClipData.newPlainText("Package ID", pkg.packageName))
                    Toast.makeText(context, "Copied ${pkg.packageName}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Installed Device Apps
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DEVICE INSTALLED APPS (${installedApps.size})",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter installed apps by name or package…", fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
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
                modifier = Modifier.fillMaxWidth().testTag("app_search_field")
            )
        }

        items(filteredApps.take(40)) { app ->
            InstalledAppCard(
                app = app,
                onLaunch = {
                    onLaunchApp(CharuAction(action = "OPEN_APP", packageName = app.packageName))
                },
                onCopy = {
                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cb.setPrimaryClip(ClipData.newPlainText("Package ID", app.packageName))
                    Toast.makeText(context, "Copied ${app.packageName}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CommonPackageCard(
    pkg: CommonPackage,
    onLaunch: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        color = CyberDark,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.8.dp, CyberSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CyberCyan.copy(alpha = 0.15f))
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pkg.name,
                    color = CyberTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = pkg.packageName,
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = pkg.description,
                    color = CyberTextSecondary,
                    fontSize = 10.sp
                )
            }

            IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Package",
                    tint = CyberTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(onClick = onLaunch, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Launch,
                    contentDescription = "Open App",
                    tint = CyberEmerald,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun InstalledAppCard(
    app: InstalledAppInfo,
    onLaunch: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        color = CyberSurfaceCard,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.6.dp, CyberSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.appName,
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (app.isSystemApp) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(CyberDark)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("SYS", color = CyberTextSecondary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                Text(
                    text = app.packageName,
                    color = CyberTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            IconButton(onClick = onCopy, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = CyberTextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }

            IconButton(onClick = onLaunch, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Launch",
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
