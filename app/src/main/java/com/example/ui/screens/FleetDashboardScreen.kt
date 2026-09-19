package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderSide
import com.example.data.QuantKitRepository
import com.example.data.Runner
import com.example.data.RunnerStatus
import com.example.data.VirtualForwardTestingEngine
import com.example.ui.components.SevenDayEquityCurve
import com.example.ui.components.StartForwardTestDialog
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.PnlNegative
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.PnlPositiveCyan
import com.example.ui.theme.StatusScanning
import com.example.ui.theme.SunsetOrange

@Composable
fun FleetDashboardScreen(
    onSelectRunner: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current
    var isLiveMode by remember { mutableStateOf(false) } // Default to Paper/Forward mode
    var isKillSwitchTripped by remember { mutableStateOf(false) }
    var showAdjustDialog by remember { mutableStateOf<String?>(null) }
    var showFlattenDialog by remember { mutableStateOf<String?>(null) }
    var showNewRunnerNotice by remember { mutableStateOf(false) }
    var showStartForwardTestModal by remember { mutableStateOf(false) }

    val virtualAccount by VirtualForwardTestingEngine.virtualAccount.collectAsState()
    val runners by VirtualForwardTestingEngine.activeRunners.collectAsState()
    val forwardLogs by VirtualForwardTestingEngine.forwardTestFeed.collectAsState()

    // Dynamic metrics computed directly from virtual account (starts strictly at 0.00)
    val netRealizedPnl = virtualAccount.realizedPnl
    val netRealizedPnlStr = if (netRealizedPnl >= 0) "+$${String.format("%,.2f", netRealizedPnl)}" else "-$${String.format("%,.2f", Math.abs(netRealizedPnl))}"
    val pnlColor = if (netRealizedPnl >= 0) PnlPositive else PnlNegative
    val roiPct = virtualAccount.roiPct
    val roiPctStr = if (roiPct >= 0) "+${String.format("%.2f", roiPct)}%" else "${String.format("%.2f", roiPct)}%"

    val wins = virtualAccount.winTradesCount
    val losses = virtualAccount.lossTradesCount
    val totalTrades = virtualAccount.totalTradesExecuted
    val winRate = if (totalTrades > 0) (wins.toDouble() / totalTrades.toDouble()) * 100.0 else 0.0
    val profitFactor = if (losses == 0 && wins > 0) 9.99 else if (losses == 0) 0.0 else (wins.toDouble() / losses.toDouble())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. SYSTEM HEALTH & SAFETY PILL BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kill Switch interactive toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { isKillSwitchTripped = !isKillSwitchTripped }
                    .testTag("kill_switch_toggle")
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isKillSwitchTripped) PnlNegative.copy(alpha = 0.15f) else PnlPositive.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (isKillSwitchTripped) PnlNegative.copy(alpha = 0.4f) else PnlPositive.copy(alpha = 0.4f),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isKillSwitchTripped) Icons.Default.Warning else Icons.Default.Security,
                        contentDescription = "Kill Switch",
                        tint = if (isKillSwitchTripped) PnlNegative else PnlPositive,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "KILL SWITCH",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isKillSwitchTripped) "TRIPPED / HALTED" else "ARMED / IDLE",
                        color = if (isKillSwitchTripped) PnlNegative else PnlPositive,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .height(30.dp)
                    .width(1.dp)
                    .background(colors.border)
            )

            // Watchdog Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PnlPositiveCyan.copy(alpha = 0.15f))
                        .border(1.dp, PnlPositiveCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Watchdog",
                        tint = PnlPositiveCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "WATCHDOG",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "HEALTHY (4m)",
                        color = PnlPositiveCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 2. HERO PERFORMANCE & PnL CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card Top: Net PnL Big Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "NET REALIZED PNL",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = netRealizedPnlStr,
                            color = pnlColor,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(pnlColor.copy(alpha = 0.15f))
                                .border(1.dp, pnlColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = roiPctStr,
                                color = pnlColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "7D GAIN",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = netRealizedPnlStr,
                        color = pnlColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // 7-Day Rolling Equity Curve Chart Canvas
            SevenDayEquityCurve(realizedPnl = netRealizedPnl, totalTrades = totalTrades)

            // 7-Day Session Indicators (Pills)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7-Day Wins / Losses",
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (totalTrades == 0) {
                        repeat(6) {
                            Box(
                                modifier = Modifier
                                    .size(width = 14.dp, height = 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(colors.border)
                            )
                        }
                    } else {
                        repeat(wins.coerceAtMost(6)) {
                            Box(
                                modifier = Modifier
                                    .size(width = 14.dp, height = 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(PnlPositive)
                            )
                        }
                        repeat(losses.coerceAtMost(6)) {
                            Box(
                                modifier = Modifier
                                    .size(width = 14.dp, height = 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(PnlNegative)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${wins}W-${losses}L",
                        color = colors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Metrics Grid (Win Rate / Profit Factor / Max DD)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = colors.borderSubtle, shape = RoundedCornerShape(12.dp))
                    .background(colors.container.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "WIN RATE",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${String.format("%.1f", winRate)}%",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "${wins}W / ${losses}L",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column {
                    Text(
                        text = "PROFIT FACTOR",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format("%.2f", profitFactor),
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = if (totalTrades > 0) "1.48R exp." else "0.0R exp.",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column {
                    Text(
                        text = "MAX DD",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "0.0%",
                        color = if (totalTrades > 0) PnlNegative else colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "< 15% limit",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 3. QUICK LAUNCH & RUNNER FILTER ACTIONS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showStartForwardTestModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("new_runner_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "NEW RUNNER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                        .clickable { showStartForwardTestModal = true }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Forward Test",
                            tint = SunsetOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "START TEST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // Live / Paper Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.container)
                    .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (isLiveMode) colors.surface else Color.Transparent)
                        .clickable { isLiveMode = true }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Live",
                        fontSize = 11.sp,
                        fontWeight = if (isLiveMode) FontWeight.Bold else FontWeight.Normal,
                        color = if (isLiveMode) SunsetOrange else colors.textMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(7.dp))
                        .background(if (!isLiveMode) colors.surface else Color.Transparent)
                        .clickable { isLiveMode = false }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Forward",
                        fontSize = 11.sp,
                        fontWeight = if (!isLiveMode) FontWeight.Bold else FontWeight.Normal,
                        color = if (!isLiveMode) PnlPositiveCyan else colors.textMuted
                    )
                }
            }
        }

        // 4. ACTIVE FLEET RUNNERS SECTION
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Active Runners",
                    tint = SunsetOrange,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "ACTIVE RUNNERS",
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.container)
                        .padding(horizontal = 7.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${runners.size}",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = if (runners.isNotEmpty()) "FORWARD TESTING ACTIVE" else "0 ACTIVE TRADES",
                color = if (runners.isNotEmpty()) PnlPositive else colors.textMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Runner Cards List or Zero State
        if (runners.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CandlestickChart,
                        contentDescription = "Forward Test",
                        tint = SunsetOrange,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "NO FORWARD TEST RUNNERS ACTIVE",
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "All placeholder data has been reset to 0. Choose a strategy and select your Stop Loss (SL) to start live forward testing with virtual funds.",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { showStartForwardTestModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("start_forward_testing_cta")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "START FORWARD TESTING",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        } else {
            runners.forEach { runner ->
                RunnerCard(
                    runner = runner,
                    onClick = { onSelectRunner(runner.id) }
                )
            }
        }

        // 5. LIVE ENGINE LOG SNIPPET
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Exec Stream",
                        tint = SunsetOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "FORWARD TEST FEED",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Engine v4.2 (Live)",
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.canvas)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (forwardLogs.isEmpty()) {
                    Text(
                        text = "[STANDBY] Waiting for forward testing strategy deployment...",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    forwardLogs.take(6).forEach { logMsg ->
                        val isHighlight = logMsg.contains("[TP HIT]") || logMsg.contains("[SL HIT]") || logMsg.contains("[SYNTH-ORDER]")
                        Text(
                            text = logMsg,
                            color = if (isHighlight) SunsetOrange else colors.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Modal to Start Forward Testing with Strategy Catalog & Parameter Lab
    if (showStartForwardTestModal) {
        StartForwardTestDialog(
            onDismiss = { showStartForwardTestModal = false },
            onStart = { strategy, symbol, side, qty, sl, tp ->
                showStartForwardTestModal = false
            },
            onStartWithParams = { strategy, symbol, side, qty, sl, tp, summary ->
                VirtualForwardTestingEngine.placeVirtualForwardOrder(
                    runnerId = "FWD-${symbol.replace("/", "").take(4)}-${System.currentTimeMillis().toString().takeLast(4)}",
                    symbol = symbol,
                    strategy = strategy,
                    side = side,
                    qty = qty,
                    stopLoss = sl,
                    takeProfit = tp,
                    customParametersSummary = summary
                )
                showStartForwardTestModal = false
            }
        )
    }
}

@Composable
private fun RunnerCard(
    runner: Runner,
    onClick: () -> Unit
) {
    val colors = LocalQuantKitColors.current

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("runner_card_${runner.id}"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header: Icon + Pair + TF + Status Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Asset Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (runner.baseAsset) {
                                "AU" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                "BTC" -> SunsetOrange.copy(alpha = 0.15f)
                                else -> Color(0xFF6366F1).copy(alpha = 0.15f)
                            }
                        )
                        .border(
                            1.dp,
                            when (runner.baseAsset) {
                                "AU" -> Color(0xFFF59E0B).copy(alpha = 0.35f)
                                "BTC" -> SunsetOrange.copy(alpha = 0.35f)
                                else -> Color(0xFF6366F1).copy(alpha = 0.35f)
                            },
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = runner.baseAsset,
                        color = when (runner.baseAsset) {
                            "AU" -> Color(0xFFF59E0B)
                            "BTC" -> SunsetOrange
                            else -> Color(0xFF6366F1)
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = runner.symbol,
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.container)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = runner.timeframe,
                                color = colors.textSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SunsetOrange.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = runner.leverage,
                                color = SunsetOrange,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = runner.strategyName,
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Status Badge
            when (runner.status) {
                RunnerStatus.IN_POSITION -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(PnlPositive.copy(alpha = 0.12f))
                            .border(1.dp, PnlPositive.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(PnlPositive)
                            )
                            Text(
                                text = runner.statusLabel,
                                color = PnlPositive,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                RunnerStatus.SCANNING -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(StatusScanning.copy(alpha = 0.12f))
                            .border(1.dp, StatusScanning.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Scanning",
                                tint = StatusScanning,
                                modifier = Modifier
                                    .size(12.dp)
                                    .rotate(rotationAngle)
                            )
                            Text(
                                text = runner.statusLabel,
                                color = StatusScanning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                RunnerStatus.STANDBY -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(colors.container)
                            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(colors.textMuted)
                            )
                            Text(
                                text = runner.statusLabel,
                                color = colors.textMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                RunnerStatus.HALTED -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(PnlNegative.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "HALTED",
                            color = PnlNegative,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Middle metrics depending on state
        when (runner.status) {
            RunnerStatus.IN_POSITION -> {
                // Entry vs PnL Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.canvas)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LONG ENTRY",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = runner.entryPrice ?: "",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "UNREALIZED PNL",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = runner.unrealizedPnl ?: "",
                                color = PnlPositive,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "(${runner.unrealizedPnlPct})",
                                color = PnlPositive,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Levels (Risk / SL / TP)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.container)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RISK", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text(runner.riskUsd ?: "$10.00", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PnlNegative.copy(alpha = 0.10f))
                            .border(1.dp, PnlNegative.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STOP (SL)", fontSize = 9.sp, color = PnlNegative, fontFamily = FontFamily.Monospace)
                            Text(runner.stopLoss ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PnlNegative, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PnlPositive.copy(alpha = 0.10f))
                            .border(1.dp, PnlPositive.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TARGET (TP)", fontSize = 9.sp, color = PnlPositive, fontFamily = FontFamily.Monospace)
                            Text(runner.takeProfit ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Bottom Footer: Hold time + actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Hold",
                            tint = colors.textMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = runner.holdTime ?: "18m hold",
                            color = colors.textMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.container)
                                .clickable { onClick() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "ADJUST TP",
                                color = colors.textPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PnlNegative.copy(alpha = 0.15f))
                                .clickable { onClick() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "FLATTEN",
                                color = PnlNegative,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            RunnerStatus.SCANNING -> {
                // Range High and ADX
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.canvas)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "20D RANGE HIGH",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = runner.rangeHigh ?: "94,800.00",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "ADX (14)",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = runner.adxValue ?: "32.4 (Strong)",
                            color = PnlPositiveCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = runner.pendingDelta ?: "Pending: +184.2 pts",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.container)
                            .clickable { /* inspect */ }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "INSPECT LOGIC",
                            color = colors.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            RunnerStatus.STANDBY -> {
                // Z-Score deviation progress
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.canvas)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Z-Score Deviation", fontSize = 11.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text(runner.zScore ?: "-1.40 (Goal ±2.0)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                    }

                    // Progress bar track with center marker
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.container)
                    ) {
                        // Center zero mark
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(2.dp)
                                .height(6.dp)
                                .background(colors.textMuted)
                        )
                        // Active offset bar (-1.40 on left side)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(PnlPositiveCyan)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = runner.halfLife ?: "Half-life: 14.2h",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.container)
                            .clickable { /* overrides */ }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "OVERRIDES",
                            color = colors.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            else -> Unit
        }
    }
}
