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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CommandHistoryItem
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TelemetryLogsScreen(
    historyItems: List<CommandHistoryItem>,
    onReExecute: (String) -> Unit,
    onCopyJson: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Surface(
                color = CyberSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CyberSurfaceBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHARU TELEMETRY LOGS",
                                color = CyberCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }

                        if (historyItems.isNotEmpty()) {
                            IconButton(
                                onClick = onClearHistory,
                                modifier = Modifier.size(32.dp).testTag("clear_history_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear Logs",
                                    tint = CyberTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "History of voice and text directives, interpreted schema payloads, and controller execution telemetry.",
                        color = CyberTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        if (historyItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No directives logged yet. Execute a command from the Terminal HUD.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            items(historyItems) { item ->
                HistoryItemCard(
                    item = item,
                    dateFormat = dateFormat,
                    onReExecute = { onReExecute(item.command) },
                    onCopyJson = { onCopyJson(item.rawJson) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun HistoryItemCard(
    item: CommandHistoryItem,
    dateFormat: SimpleDateFormat,
    onReExecute: () -> Unit,
    onCopyJson: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        color = CyberDark,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(0.8.dp, CyberSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .clickable { isExpanded = !isExpanded }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (item.isSuccess) CyberEmerald else CyberRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\"${item.command}\"",
                        color = CyberTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = dateFormat.format(Date(item.timestamp)),
                    color = CyberTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.assistantReply,
                color = CyberGold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.stepCount} ${if (item.stepCount == 1) "step" else "steps"} executed",
                    color = CyberTextSecondary,
                    fontSize = 10.sp
                )

                Row {
                    IconButton(onClick = onCopyJson, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy JSON",
                            tint = CyberTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(onClick = onReExecute, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Re-Execute",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyberSurfaceCard)
                        .padding(8.dp)
                ) {
                    Text(
                        text = item.rawJson,
                        color = CyberEmerald,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
