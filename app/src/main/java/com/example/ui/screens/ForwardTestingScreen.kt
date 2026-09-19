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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderSide
import com.example.data.VirtualForwardTestingEngine
import com.example.data.VirtualOrder
import com.example.data.VirtualOrderStatus
import com.example.ui.components.StartForwardTestDialog
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.PnlNegative
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.PnlPositiveCyan
import com.example.ui.theme.StatusScanning
import com.example.ui.theme.SunsetOrange
import com.example.ui.theme.SunsetOrangeDark

@Composable
fun ForwardTestingScreen(
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current

    val account by VirtualForwardTestingEngine.virtualAccount.collectAsState()
    val orders by VirtualForwardTestingEngine.orders.collectAsState()
    val tickers by VirtualForwardTestingEngine.tickers.collectAsState()
    val isRunning by VirtualForwardTestingEngine.isForwardTestingActive.collectAsState()
    val logs by VirtualForwardTestingEngine.forwardTestFeed.collectAsState()

    var showEditBalanceDialog by remember { mutableStateOf(false) }
    var customBalanceInput by remember { mutableStateOf("") }
    var showNewOrderDialog by remember { mutableStateOf(false) }

    var selectedSymbol by remember { mutableStateOf("XAU/USD") }
    var selectedSide by remember { mutableStateOf(OrderSide.BUY) }
    var qtyInput by remember { mutableStateOf("5.0") }
    var slInput by remember { mutableStateOf("2635.00") }
    var tpInput by remember { mutableStateOf("2658.00") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. HEADER WITH LIVE FEED BADGE & CONTROLS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = SunsetOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Forward Testing Lab",
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Live exchange mark-prices • Synthetic virtual order book",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pause / Run Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isRunning) PnlPositive.copy(alpha = 0.15f) else SunsetOrange.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            if (isRunning) PnlPositive.copy(alpha = 0.35f) else SunsetOrange.copy(alpha = 0.35f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { VirtualForwardTestingEngine.toggleForwardTesting(!isRunning) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("toggle_forward_test_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Sync else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isRunning) PnlPositive else SunsetOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = if (isRunning) "RUNNING" else "PAUSED",
                            color = if (isRunning) PnlPositive else SunsetOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Theme Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.container)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .clickable { onToggleTheme() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LightMode,
                        contentDescription = "Theme",
                        tint = SunsetOrange,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // 2. VIRTUAL ACCOUNT CARD (CUSTOM BALANCE & EQUITY)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(18.dp))
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
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SunsetOrange.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = SunsetOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = account.accountName,
                            color = colors.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Account ID: ${account.accountId}",
                            color = colors.textMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Custom Balance Edit Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.container)
                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                        .clickable {
                            customBalanceInput = account.initialBalance.toInt().toString()
                            showEditBalanceDialog = true
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("edit_custom_balance_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Balance",
                            tint = SunsetOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "SET BALANCE",
                            color = colors.textPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Big Numbers & Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "VIRTUAL TOTAL EQUITY",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$${String.format("%,.2f", account.totalEquity)}",
                            color = if (account.realizedPnl >= 0) PnlPositive else PnlNegative,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (account.roiPct >= 0) PnlPositive.copy(alpha = 0.15f) else PnlNegative.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = String.format("%+.2f%%", account.roiPct),
                                color = if (account.roiPct >= 0) PnlPositive else PnlNegative,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "REALIZED PNL",
                        color = colors.textMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format("%+,.2f", account.realizedPnl),
                        color = if (account.realizedPnl >= 0) PnlPositive else PnlNegative,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Sub Stats Grid: Initial Balance, Win Rate, Active Brackets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.container)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("INITIAL CAPITAL", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text("$${String.format("%,.0f", account.initialBalance)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.container)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("WIN RATIO", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        val total = account.totalTradesExecuted
                        val wr = if (total > 0) (account.winTradesCount.toDouble() / total * 100.0) else 78.5
                        Text(String.format("%.1f%% (%dW/%dL)", wr, account.winTradesCount, account.lossTradesCount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.container)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("OPEN POSITIONS", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text("${account.activeOrdersCount} Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // 3. LIVE MARKET DATA TICKER RIBBON (REAL-TIME MARK PRICES)
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
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(PnlPositive)
                    )
                    Text(
                        text = "REAL MARKET TICKERS (LIVE FEED)",
                        color = colors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Binance / Spot API",
                    color = colors.textMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Ticker Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tickers.values.forEach { ticker ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.canvas)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(ticker.symbol, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(
                                    text = String.format("%+.2f%%", ticker.change24h),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ticker.change24h >= 0) PnlPositive else PnlNegative,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = if (ticker.price > 1000) String.format("$%,.2f", ticker.price) else String.format("%.4f", ticker.price),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Vol: ${ticker.volume}",
                                fontSize = 9.sp,
                                color = colors.textMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 4. FORWARD TESTING POSITIONS & BRACKETS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    Text(
                        text = "VIRTUAL POSITIONS",
                        color = colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.container)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${orders.size}",
                            color = SunsetOrange,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Flatten All
                    if (orders.any { it.status == VirtualOrderStatus.OPEN }) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PnlNegative.copy(alpha = 0.15f))
                                .border(1.dp, PnlNegative.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable { VirtualForwardTestingEngine.flattenAllVirtualOrders() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "FLATTEN ALL",
                                color = PnlNegative,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Place Forward Order
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SunsetOrange)
                            .clickable { showNewOrderDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("launch_virtual_order_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text(
                                text = "TEST ORDER",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Orders list
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No virtual forward test orders yet. Tap 'TEST ORDER' or trigger from Strategy screen.",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            } else {
                orders.forEach { order ->
                    VirtualOrderCard(
                        order = order,
                        onClose = { VirtualForwardTestingEngine.closeVirtualOrder(order.id) }
                    )
                }
            }
        }

        // 5. REALTIME FORWARD TEST FEED & EVENT LOG
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
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = SunsetOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "FORWARD EXECUTION LOG",
                        color = colors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "Sub-tick latency",
                    color = colors.textMuted,
                    fontSize = 10.sp,
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
                logs.take(6).forEach { logLine ->
                    val color = when {
                        logLine.contains("TP HIT") -> PnlPositive
                        logLine.contains("SL HIT") -> PnlNegative
                        logLine.contains("SYNTH-ORDER") -> PnlPositiveCyan
                        else -> colors.textSecondary
                    }
                    Text(
                        text = logLine,
                        fontSize = 11.sp,
                        color = color,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // DIALOG 1: Set Custom Balance
    if (showEditBalanceDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showEditBalanceDialog = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Custom Virtual Balance",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textMuted,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { showEditBalanceDialog = false }
                    )
                }

                Text(
                    text = "Set your desired paper balance to simulate forward testing against live market feeds.",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = customBalanceInput,
                    onValueChange = { customBalanceInput = it },
                    label = { Text("Starting Capital ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_balance_textfield"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SunsetOrange,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    )
                )

                // Quick presets ($10k, $25k, $50k, $100k)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("10000", "25000", "50000", "100000").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.container)
                                .border(1.dp, colors.border, RoundedCornerShape(6.dp))
                                .clickable { customBalanceInput = preset }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$${preset.take(3)}k",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            VirtualForwardTestingEngine.resetVirtualAccount(customBalanceInput.toDoubleOrNull() ?: 25000.0)
                            showEditBalanceDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.container),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset All", color = colors.textPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val bal = customBalanceInput.toDoubleOrNull()
                            if (bal != null && bal > 0) {
                                VirtualForwardTestingEngine.setVirtualCustomBalance(bal)
                            }
                            showEditBalanceDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_balance_btn")
                    ) {
                        Text("Apply Balance", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // DIALOG 2: Start Forward Testing with Strategy Catalog & Parameter Lab
    if (showNewOrderDialog) {
        StartForwardTestDialog(
            onDismiss = { showNewOrderDialog = false },
            onStart = { strategy, symbol, side, qty, sl, tp ->
                showNewOrderDialog = false
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
                showNewOrderDialog = false
            }
        )
    }
}

@Composable
private fun VirtualOrderCard(
    order: VirtualOrder,
    onClose: () -> Unit
) {
    val colors = LocalQuantKitColors.current

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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (order.side == OrderSide.BUY) PnlPositive.copy(alpha = 0.15f) else PnlNegative.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.side.name,
                        color = if (order.side == OrderSide.BUY) PnlPositive else PnlNegative,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "${order.qty}x ${order.symbol}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Text(
                    text = order.strategy,
                    fontSize = 10.sp,
                    color = colors.textMuted
                )
            }

            // Status Badge
            when (order.status) {
                VirtualOrderStatus.OPEN -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PnlPositive.copy(alpha = 0.12f))
                            .border(1.dp, PnlPositive.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("OPEN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                    }
                }
                VirtualOrderStatus.FILLED_TP -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PnlPositive.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("TP HIT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                    }
                }
                VirtualOrderStatus.STOPPED_SL -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PnlNegative.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("STOPPED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PnlNegative, fontFamily = FontFamily.Monospace)
                    }
                }
                VirtualOrderStatus.CLOSED_MANUAL -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.container)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("CLOSED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Pricing details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ENTRY", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                Text(String.format("$%,.2f", order.entryPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
            }

            Column {
                Text("CURRENT MARK", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                Text(String.format("$%,.2f", order.currentPrice), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("UNREALIZED PNL", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                Text(
                    text = String.format("%+,.2f (%+.1f%%)", order.unrealizedPnl, order.unrealizedPnlPct),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (order.unrealizedPnl >= 0) PnlPositive else PnlNegative,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Bracket targets & Close Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("SL: $${String.format("%.2f", order.stopLoss)}", fontSize = 10.sp, color = PnlNegative, fontFamily = FontFamily.Monospace)
                Text("TP: $${String.format("%.2f", order.takeProfit)}", fontSize = 10.sp, color = PnlPositive, fontFamily = FontFamily.Monospace)
            }

            if (order.status == VirtualOrderStatus.OPEN) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.container)
                        .clickable { onClose() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("CLOSE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
