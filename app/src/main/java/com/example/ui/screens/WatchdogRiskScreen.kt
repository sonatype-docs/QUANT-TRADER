package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CircuitBreakerItem
import com.example.data.QuantKitRepository
import com.example.data.VirtualForwardTestingEngine
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.PnlNegative
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.PnlPositiveCyan
import com.example.ui.theme.SunsetOrange

@Composable
fun WatchdogRiskScreen(
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current

    var masterHaltTripped by remember { mutableStateOf(false) }
    var showHaltConfirm by remember { mutableStateOf(false) }
    var showScanSuccessNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Meta & Theme Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PnlPositive)
                )
                Text(
                    text = "AUTONOMOUS SENTINEL",
                    color = PnlPositive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "•",
                    color = colors.textMuted,
                    fontSize = 11.sp
                )
                Text(
                    text = "v4.2.1",
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.container)
                    .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                    .clickable { onToggleTheme() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = "Theme",
                        tint = SunsetOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = if (colors.isDark) "LIGHT" else "DARK",
                        color = colors.textPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Title and Status
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = "Watchdog",
                    tint = SunsetOrange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Watchdog & Risk Limits",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Continuous hardware & exchange-level circuit breakers",
                color = colors.textSecondary,
                fontSize = 12.sp
            )
        }

        // System Nominal Beacon Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (masterHaltTripped) PnlNegative.copy(alpha = 0.15f) else PnlPositive.copy(alpha = 0.12f))
                .border(
                    1.dp,
                    if (masterHaltTripped) PnlNegative.copy(alpha = 0.35f) else PnlPositive.copy(alpha = 0.35f),
                    RoundedCornerShape(14.dp)
                )
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (masterHaltTripped) PnlNegative else PnlPositive)
                )
                Column {
                    Text(
                        text = if (masterHaltTripped) "SYSTEM EMERGENCY HALTED" else "ALL SYSTEMS NOMINAL",
                        color = if (masterHaltTripped) PnlNegative else PnlPositive,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (masterHaltTripped) "Active positions flattened. Sockets closed." else "No active breaches or slippage anomalies.",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .clickable { showScanSuccessNotice = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "MANUAL SCAN",
                    color = colors.textPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        AnimatedVisibility(visible = showScanSuccessNotice) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PnlPositiveCyan.copy(alpha = 0.15f))
                    .border(1.dp, PnlPositiveCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Orphan order sweep completed. 0 hanging orders on SharkEx.",
                        fontSize = 11.sp,
                        color = PnlPositiveCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "OK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PnlPositiveCyan,
                        modifier = Modifier.clickable { showScanSuccessNotice = false }
                    )
                }
            }
        }

        // Master Emergency Kill Switch Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(
                    1.dp,
                    if (masterHaltTripped) PnlNegative else colors.borderSubtle,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PnlNegative.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = "Halt",
                            tint = PnlNegative,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "MASTER EMERGENCY HALT",
                            color = PnlNegative,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Instantly closes all active positions & brackets",
                            color = colors.textMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (masterHaltTripped) PnlNegative else colors.container)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (masterHaltTripped) "TRIPPED" else "ARMED",
                        color = if (masterHaltTripped) Color.White else colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (!showHaltConfirm && !masterHaltTripped) {
                Button(
                    onClick = { showHaltConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PnlNegative),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("engage_kill_switch_btn")
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ENGAGE FLEET KILL SWITCH", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            } else if (showHaltConfirm) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            masterHaltTripped = true
                            showHaltConfirm = false
                            VirtualForwardTestingEngine.flattenAllVirtualOrders()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CONFIRM EMERGENCY HALT", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    Button(
                        onClick = { showHaltConfirm = false },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.container),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Text("CANCEL", color = colors.textPrimary, fontSize = 11.sp)
                    }
                }
            } else {
                Button(
                    onClick = { masterHaltTripped = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PnlPositive),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESET & RE-ARM SENTINEL", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Circuit Breaker Matrix (4 Cards)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "CIRCUIT BREAKER MATRIX",
                color = colors.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )

            QuantKitRepository.circuitBreakers.forEach { breaker ->
                CircuitBreakerCard(item = breaker)
            }
        }

        // Exchange Connectivity & Latency Ribbon
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = SunsetOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Exchange Sockets & Latency",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Sub-10ms Target",
                    color = PnlPositive,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // 3 Nodes
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // SharkEx
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.container)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("SharkEx WS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("8.12 ms", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                        Text("TY3 Tokyo", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }
                }

                // Binance
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.container)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Binance Feed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("14.4 ms", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                        Text("SGP AWS", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }
                }

                // Bybit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.container)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Bybit Hedge", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("19.2 ms", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                        Text("LD4 Slough", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Watchdog Audit & Event Log
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Audit",
                        tint = SunsetOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Watchdog Audit Log",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Immutable",
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuantKitRepository.watchdogAuditLogs.forEach { logItem ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.canvas)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = logItem.timeUtc,
                                color = colors.textMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PnlPositive.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = logItem.tag,
                                    color = PnlPositive,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        Text(
                            text = logItem.description,
                            color = colors.textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CircuitBreakerCard(item: CircuitBreakerItem) {
    val colors = LocalQuantKitColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(PnlPositive.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = item.statusBadge,
                    color = PnlPositive,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Text(
            text = item.description,
            color = colors.textSecondary,
            fontSize = 11.sp
        )

        if (item.currentValue != null && item.maxValue != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.currentValue,
                    color = PnlPositive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = item.maxValue,
                    color = colors.textMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            LinearProgressIndicator(
                progress = { item.progressPct ?: 0.35f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SunsetOrange,
                trackColor = colors.container
            )
        }
    }
}
