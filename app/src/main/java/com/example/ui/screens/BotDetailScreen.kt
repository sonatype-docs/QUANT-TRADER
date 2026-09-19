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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderSide
import com.example.data.QuantKitRepository
import com.example.data.VirtualForwardTestingEngine
import com.example.data.VirtualOrderStatus
import com.example.ui.components.ExecutionLadderView
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.PnlNegative
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.PnlPositiveCyan
import com.example.ui.theme.SunsetOrange

@Composable
fun BotDetailScreen(
    onBack: () -> Unit,
    onToggleTheme: () -> Unit,
    runnerId: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current
    var isBeLocked by remember { mutableStateOf(false) }
    var flattenState by remember { mutableStateOf(0) } // 0: Normal, 1: Confirm, 2: Sent to market
    var showAdjustModal by remember { mutableStateOf(false) }

    val orders by VirtualForwardTestingEngine.orders.collectAsState()
    val activeRunners by VirtualForwardTestingEngine.activeRunners.collectAsState()

    val matchedOrder = orders.find { it.runnerId == runnerId || it.id == runnerId }
        ?: orders.firstOrNull { it.status == VirtualOrderStatus.OPEN }
    val matchedRunner = activeRunners.find { it.id == runnerId }

    val symbol = matchedOrder?.symbol ?: matchedRunner?.symbol ?: "XAU/USD"
    val strategy = matchedOrder?.strategy ?: matchedRunner?.strategyName ?: "Mean Reversion"
    val side = matchedOrder?.side?.name ?: "LONG"
    val entryPrice = matchedOrder?.entryPrice ?: 2642.10
    val currentPrice = matchedOrder?.currentPrice ?: 2647.70
    val unrealizedPnl = matchedOrder?.unrealizedPnl ?: 0.0
    val pnlPercent = matchedOrder?.unrealizedPnlPct ?: 0.0
    val stopLoss = matchedOrder?.stopLoss ?: (if (side == "LONG") entryPrice * 0.99 else entryPrice * 1.01)
    val takeProfit = matchedOrder?.takeProfit ?: (if (side == "LONG") entryPrice * 1.02 else entryPrice * 0.98)
    val qty = matchedOrder?.qty ?: 0.1

    var editableSl by remember(stopLoss) { mutableStateOf(String.format("%.2f", stopLoss)) }
    var slUpdateNotice by remember { mutableStateOf<String?>(null) }

    val pnlColor = if (unrealizedPnl >= 0) PnlPositive else PnlNegative
    val pnlStr = if (unrealizedPnl >= 0) "+$${String.format("%,.2f", unrealizedPnl)}" else "-$${String.format("%,.2f", Math.abs(unrealizedPnl))}"
    val pnlPctStr = if (pnlPercent >= 0) "+${String.format("%.2f", pnlPercent)}%" else "${String.format("%.2f", pnlPercent)}%"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TOP NAVIGATION SUBHEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back to All Bots
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBack() }
                    .padding(4.dp)
                    .testTag("back_to_bots_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SunsetOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "All Bots",
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Active Position Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(PnlPositive.copy(alpha = 0.12f))
                    .border(1.dp, PnlPositive.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(PnlPositive)
                    )
                    Text(
                        text = "ACTIVE • IN POSITION",
                        color = PnlPositive,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // In-page Theme Toggle
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.container)
                    .clickable { onToggleTheme() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LightMode,
                    contentDescription = "Theme",
                    tint = SunsetOrange,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Asset Sub-Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$symbol Perpetual",
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SunsetOrange.copy(alpha = 0.15f))
                        .border(1.dp, SunsetOrange.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$side • 50x",
                        color = SunsetOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = "ID: #${matchedOrder?.id ?: (runnerId ?: "FWD-TEST")}",
                color = colors.textMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // 2. HERO LIVE TRADE SUMMARY CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & Live Unrealized PnL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "UNREALIZED NET PNL",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.8.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = pnlStr,
                            color = pnlColor,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(pnlColor.copy(alpha = 0.15f))
                                .border(1.dp, pnlColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = pnlPctStr,
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
                        text = "STATUS",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = SunsetOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (matchedOrder?.status == VirtualOrderStatus.OPEN) "RUNNING" else "COMPLETED",
                            color = if (matchedOrder?.status == VirtualOrderStatus.OPEN) PnlPositive else colors.textMuted,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 2x2 Metric Ribbon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Entry
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.container)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("$side ENTRY", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        Text(String.format("%,.2f", entryPrice), fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 2.dp))
                    }
                }

                // Current Mark
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.container)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("CURRENT MARK", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        Text(String.format("%,.2f", currentPrice), fontSize = 14.sp, color = PnlPositiveCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stop Loss
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.container)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("STOP LOSS (SL)", fontSize = 10.sp, color = PnlNegative, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        Text(String.format("%,.2f", stopLoss), fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 2.dp))
                    }
                }

                // Take Profit
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.container)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("TAKE PROFIT (TP)", fontSize = 10.sp, color = PnlPositive, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        Text(String.format("%,.2f", takeProfit), fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }

        // 3. INTERACTIVE VISUAL TRADE EXECUTION & TARGET LADDER
        ExecutionLadderView(steps = QuantKitRepository.executionLadderSteps)

        // 4. MICROSTRUCTURE AUDIT CARD
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QueryStats,
                    contentDescription = "Microstructure",
                    tint = SunsetOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Microstructure Audit",
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Order Routing
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.container)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
                            Text("ORDER ROUTING", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Post-Only Limit", fontSize = 12.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Maker Tier (0.02%)", fontSize = 11.sp, color = PnlPositive, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Slippage Saved
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.container)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
                            Text("FORWARD MODE", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Virtual Paper", fontSize = 12.sp, color = PnlPositive, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Real-Time Feed", fontSize = 11.sp, color = colors.textMuted)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Socket Latency
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.container)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
                            Text("SOCKET LATENCY", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        }
                        Text("8.12 ms", fontSize = 12.sp, color = PnlPositiveCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Tokyo Edge Colo", fontSize = 11.sp, color = colors.textMuted)
                    }
                }

                // Watchdog Guard
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.container)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
                            Text("WATCHDOG GUARD", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                        }
                        Text("Armed & Idle", fontSize = 12.sp, color = PnlPositive, fontWeight = FontWeight.Bold)
                        Text("Halt Trigger 0.8%", fontSize = 11.sp, color = colors.textMuted)
                    }
                }
            }
        }

        // 5. EMERGENCY & RISK ACTION BAR
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick BE Push Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.container)
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                    .clickable {
                        isBeLocked = !isBeLocked
                        if (isBeLocked && matchedOrder != null) {
                            VirtualForwardTestingEngine.updateOrderStopLoss(matchedOrder.id, entryPrice)
                            editableSl = String.format("%.2f", entryPrice)
                            slUpdateNotice = "SL locked at Entry (${String.format("%,.2f", entryPrice)})"
                        }
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield",
                        tint = if (isBeLocked) PnlPositive else colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Lock Stop Loss at Entry (+0.0R)",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isBeLocked) PnlPositive.copy(alpha = 0.15f) else colors.surface)
                        .border(1.dp, if (isBeLocked) PnlPositive.copy(alpha = 0.3f) else colors.border, RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isBeLocked) "LOCKED" else "UNLOCKED",
                        color = if (isBeLocked) PnlPositive else colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Dual Primary Buttons: Adjust SL / TP and Flatten Now
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.container)
                        .border(1.dp, colors.border, RoundedCornerShape(12.dp))
                        .clickable { showAdjustModal = !showAdjustModal }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Adjust",
                            tint = SunsetOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Adjust SL / TP",
                            color = colors.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (flattenState) {
                                1 -> Color(0xFF991B1B) // Deep red for confirm
                                2 -> Color(0xFF059669) // Green for sent
                                else -> PnlNegative
                            }
                        )
                        .clickable {
                            when (flattenState) {
                                0 -> flattenState = 1
                                1 -> {
                                    flattenState = 2
                                    matchedOrder?.let {
                                        VirtualForwardTestingEngine.closeVirtualOrder(it.id)
                                    }
                                }
                                else -> flattenState = 0
                            }
                        }
                        .padding(vertical = 12.dp)
                        .testTag("flatten_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = when (flattenState) {
                                1 -> Icons.Default.Warning
                                2 -> Icons.Default.Done
                                else -> Icons.Default.Close
                            },
                            contentDescription = "Flatten",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = when (flattenState) {
                                1 -> "Confirm Close?"
                                2 -> "Position Closed"
                                else -> "Flatten Now"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Adjust SL/TP Modal
            AnimatedVisibility(visible = showAdjustModal) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.canvas)
                        .border(1.dp, SunsetOrange.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Live Bracket & Stop Loss (SL) Adjustment",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SunsetOrange
                        )

                        Text(
                            text = "Modify Stop Loss for $symbol ($side position):",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )

                        OutlinedTextField(
                            value = editableSl,
                            onValueChange = { editableSl = it },
                            label = { Text("Stop Loss (SL) Price") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SunsetOrange,
                                unfocusedBorderColor = colors.border,
                                focusedLabelColor = SunsetOrange
                            ),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    val newSlDouble = editableSl.toDoubleOrNull()
                                    if (newSlDouble != null && matchedOrder != null) {
                                        VirtualForwardTestingEngine.updateOrderStopLoss(matchedOrder.id, newSlDouble)
                                        slUpdateNotice = "Stop Loss updated to ${String.format("%,.2f", newSlDouble)}"
                                        showAdjustModal = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "UPDATE STOP LOSS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (slUpdateNotice != null) {
                            Text(
                                text = slUpdateNotice ?: "",
                                color = PnlPositive,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 6. LIVE WATCHDOG TELEMETRY STREAM
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
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Terminal",
                        tint = SunsetOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Watchdog Telemetry",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(PnlPositive)
                    )
                    Text(
                        text = "Streaming",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Log output box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.canvas)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuantKitRepository.botTelemetryLogs.forEach { log ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = log.timestamp,
                            color = SunsetOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = log.message,
                            color = log.highlightColor ?: colors.textSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
