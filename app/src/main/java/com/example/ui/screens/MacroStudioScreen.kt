package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDark
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceCard
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary

data class SchemaPreset(
    val title: String,
    val description: String,
    val json: String
)

@Composable
fun MacroStudioScreen(
    customJson: String,
    onJsonChange: (String) -> Unit,
    onExecuteJson: (String) -> Unit,
    onCopyJson: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        SchemaPreset(
            title = "Messenger Sequence",
            description = "Multi-step message sequence from prompt example",
            json = """{
  "assistant_reply": "Sending message to Roni, sir.",
  "sequence": [
    { "action": "OPEN_APP", "package_name": "com.facebook.orca" },
    { "action": "CLICK_TEXT", "target_text": "Search" },
    { "action": "TYPE_TEXT", "text": "রনি" },
    { "action": "CLICK_TEXT", "target_text": "রনি" },
    { "action": "TYPE_TEXT", "text": "কেমন আছো" },
    { "action": "CLICK_TEXT", "target_text": "Send" }
  ]
}"""
        ),
        SchemaPreset(
            title = "WhatsApp Sequence",
            description = "Multi-step WhatsApp message to John",
            json = """{
  "assistant_reply": "Sending WhatsApp message to John, sir.",
  "sequence": [
    { "action": "OPEN_APP", "package_name": "com.whatsapp" },
    { "action": "CLICK_TEXT", "target_text": "Search" },
    { "action": "TYPE_TEXT", "text": "John" },
    { "action": "CLICK_TEXT", "target_text": "John" },
    { "action": "TYPE_TEXT", "text": "I am on my way, sir." },
    { "action": "CLICK_TEXT", "target_text": "Send" }
  ]
}"""
        ),
        SchemaPreset(
            title = "YouTube Launcher",
            description = "Single action open YouTube",
            json = """{
  "assistant_reply": "Opening YouTube, sir.",
  "action": "OPEN_APP",
  "package_name": "com.google.android.youtube"
}"""
        ),
        SchemaPreset(
            title = "System Home Routine",
            description = "Navigate home and open recent apps",
            json = """{
  "assistant_reply": "Returning home and viewing recents.",
  "sequence": [
    { "action": "GLOBAL_HOME" },
    { "action": "GLOBAL_RECENTS" }
  ]
}"""
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CHARU JSON MACRO STUDIO",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Design, simulate, or directly execute raw Charu JSON execution instructions matching the exact OS schema.",
                        color = CyberTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Presets Selector
        item {
            Text(
                text = "PRE-BUILT SCHEMA PRESETS",
                color = CyberGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { preset ->
                    Surface(
                        color = CyberDark,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.8.dp, CyberSurfaceBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onJsonChange(preset.json)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.title,
                                    color = CyberCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = preset.description,
                                    color = CyberTextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Button(
                                onClick = { onJsonChange(preset.json) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberSurfaceCard,
                                    contentColor = CyberCyan
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Load", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Live JSON Editor Box
        item {
            Text(
                text = "INTERACTIVE JSON SCHEMA EDITOR",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customJson,
                onValueChange = onJsonChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .testTag("custom_json_editor"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CyberEmerald,
                    unfocusedTextColor = CyberEmerald,
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = CyberSurfaceBorder,
                    focusedContainerColor = CyberDark,
                    unfocusedContainerColor = CyberDark
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onCopyJson(customJson) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy", fontSize = 12.sp)
                }

                Button(
                    onClick = { onExecuteJson(customJson) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = CyberDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(2f)
                        .testTag("execute_macro_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Execute Macro Sequence", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
