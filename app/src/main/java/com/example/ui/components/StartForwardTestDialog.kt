package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.OrderSide
import com.example.data.StrategyCatalog
import com.example.data.StrategyCategory
import com.example.data.StrategyModel
import com.example.data.VirtualForwardTestingEngine
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.PnlNegative
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.PnlPositiveCyan
import com.example.ui.theme.SunsetOrange

@Composable
fun StartForwardTestDialog(
    onDismiss: () -> Unit,
    onStart: (strategy: String, symbol: String, side: OrderSide, qty: Double, sl: Double, tp: Double) -> Unit,
    onStartWithParams: ((strategy: String, symbol: String, side: OrderSide, qty: Double, sl: Double, tp: Double, paramsSummary: String) -> Unit)? = null
) {
    val colors = LocalQuantKitColors.current
    val tickers by VirtualForwardTestingEngine.tickers.collectAsState()
    val virtualAccount by VirtualForwardTestingEngine.virtualAccount.collectAsState()

    val allStrategies = StrategyCatalog.strategies
    var selectedCategory by remember { mutableStateOf(StrategyCategory.ALL) }
    var detailedStrategy by remember { mutableStateOf<StrategyModel?>(null) }

    // Dialog size & properties
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, SunsetOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            if (detailedStrategy == null) {
                // VIEW 1: STRATEGY CATALOG & BACKTEST OVERVIEW
                StrategyCatalogOverview(
                    strategies = allStrategies,
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    onSelectStrategy = { detailedStrategy = it },
                    onDismiss = onDismiss
                )
            } else {
                // VIEW 2: STRATEGY DEEP-DIVE & REAL-TIME PARAMETER PLAYGROUND
                StrategyDeepDiveAndCustomizer(
                    strategy = detailedStrategy!!,
                    onBack = { detailedStrategy = null },
                    onDismiss = onDismiss,
                    tickers = tickers,
                    accountEquity = virtualAccount.totalEquity,
                    onDeploy = { sym, side, qty, sl, tp, summary ->
                        if (onStartWithParams != null) {
                            onStartWithParams(detailedStrategy!!.name, sym, side, qty, sl, tp, summary)
                        } else {
                            VirtualForwardTestingEngine.placeVirtualForwardOrder(
                                runnerId = "FWD-${sym.replace("/", "").take(4)}-${System.currentTimeMillis().toString().takeLast(4)}",
                                symbol = sym,
                                strategy = detailedStrategy!!.name,
                                side = side,
                                qty = qty,
                                stopLoss = sl,
                                takeProfit = tp,
                                customParametersSummary = summary
                            )
                            onStart(detailedStrategy!!.name, sym, side, qty, sl, tp)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StrategyCatalogOverview(
    strategies: List<StrategyModel>,
    selectedCategory: StrategyCategory,
    onSelectCategory: (StrategyCategory) -> Unit,
    onSelectStrategy: (StrategyModel) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalQuantKitColors.current
    val filtered = if (selectedCategory == StrategyCategory.ALL) {
        strategies
    } else {
        strategies.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dialog Top Bar
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
                    imageVector = Icons.Default.Science,
                    contentDescription = null,
                    tint = SunsetOrange,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "QUANT STRATEGY LAB",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Backtested Quant Edge • Customize & Forward Test",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.container)
                    .clickable { onDismiss() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            StrategyCategory.values().forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) SunsetOrange.copy(alpha = 0.2f) else colors.container)
                        .border(1.dp, if (isSelected) SunsetOrange else colors.borderSubtle, RoundedCornerShape(8.dp))
                        .clickable { onSelectCategory(cat) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SunsetOrange else colors.textPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Text(
            text = "Choose a strategy below to inspect its multi-year backtest stats, pros/cons, and customize live parameters before launching a forward runner:",
            fontSize = 11.sp,
            color = colors.textSecondary
        )

        // Strategies List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            filtered.forEach { strat ->
                StrategyCatalogCard(
                    strategy = strat,
                    onClick = { onSelectStrategy(strat) }
                )
            }
        }
    }
}

@Composable
private fun StrategyCatalogCard(
    strategy: StrategyModel,
    onClick: () -> Unit
) {
    val colors = LocalQuantKitColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.container)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row: Category Badge & Timeframe
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SunsetOrange.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = strategy.category.label.uppercase(),
                            color = SunsetOrange,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = strategy.recommendedTimeframe,
                            color = colors.textMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Backtest duration badge
                Text(
                    text = strategy.backtestPeriod.substringBefore(" ("),
                    fontSize = 10.sp,
                    color = PnlPositiveCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Strategy Name & Tagline
            Column {
                Text(
                    text = strategy.name,
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = strategy.tagline,
                    color = colors.textMuted,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 4-Key Metrics Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("WIN RATE", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    Text("${strategy.baseWinRate}%", fontSize = 13.sp, color = PnlPositive, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("PROFIT FACTOR", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    Text("${strategy.baseProfitFactor}", fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("MAX DD", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    Text("${strategy.baseMaxDrawdown}%", fontSize = 13.sp, color = PnlNegative, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Column {
                    Text("TRADES", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    Text("%,d".format(strategy.baseTradesCount), fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            // Footer / Action Cue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pairs: ${strategy.compatibleSymbols.joinToString(", ")}",
                    fontSize = 10.sp,
                    color = colors.textMuted,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Customize & Deploy",
                        fontSize = 11.sp,
                        color = SunsetOrange,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = SunsetOrange,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StrategyDeepDiveAndCustomizer(
    strategy: StrategyModel,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
    tickers: Map<String, com.example.data.MarketTicker>,
    accountEquity: Double,
    onDeploy: (symbol: String, side: OrderSide, qty: Double, sl: Double, tp: Double, paramsSummary: String) -> Unit
) {
    val colors = LocalQuantKitColors.current

    // Parameter sliders state (initialized to strategy base defaults)
    var p1 by remember(strategy.id) { mutableFloatStateOf(strategy.param1Default) }
    var p2 by remember(strategy.id) { mutableFloatStateOf(strategy.param2Default) }
    var p3 by remember(strategy.id) { mutableFloatStateOf(strategy.param3Default) }
    var p4 by remember(strategy.id) { mutableFloatStateOf(strategy.param4Default) }
    var p5 by remember(strategy.id) { mutableFloatStateOf(strategy.param5Default) }

    // Live dynamic backtest calculation
    val dynamicStats = remember(strategy.id, p1, p2, p3, p4, p5) {
        StrategyCatalog.calculateDynamicImpact(strategy, p1, p2, p3, p4, p5)
    }

    // Execution Form State
    var selectedSymbol by remember(strategy.id) { mutableStateOf(strategy.compatibleSymbols.firstOrNull() ?: "XAU/USD") }
    var selectedSide by remember { mutableStateOf(OrderSide.BUY) }
    var qtyInput by remember { mutableStateOf("1.0") }

    val currentTicker = tickers[selectedSymbol]
    val currentPrice = currentTicker?.price ?: if (selectedSymbol.contains("XAU")) 2647.70 else if (selectedSymbol.contains("BTC")) 94820.0 else 28.24

    // Stop Loss & Take Profit Price fields (linked to custom p2 & p3)
    var slInput by remember { mutableStateOf("") }
    var tpInput by remember { mutableStateOf("") }

    // Automatically recalculate initial SL & TP prices when Symbol, Side, or Custom Parameters shift
    LaunchedEffect(selectedSymbol, selectedSide, currentPrice, p2, p3) {
        val slPrice = if (selectedSide == OrderSide.BUY) {
            currentPrice * (1.0 - (p2 * 0.008))
        } else {
            currentPrice * (1.0 + (p2 * 0.008))
        }
        val tpPrice = if (selectedSide == OrderSide.BUY) {
            currentPrice * (1.0 + (p3 * 0.010))
        } else {
            currentPrice * (1.0 - (p3 * 0.010))
        }
        slInput = String.format("%.2f", slPrice)
        tpInput = String.format("%.2f", tpPrice)
    }

    val qty = qtyInput.toDoubleOrNull() ?: 1.0
    val slPrice = slInput.toDoubleOrNull() ?: 0.0
    val tpPrice = tpInput.toDoubleOrNull() ?: 0.0

    val dollarRisk = if (slPrice > 0) Math.abs(currentPrice - slPrice) * qty else 0.0
    val riskPctOfEquity = if (accountEquity > 0) (dollarRisk / accountEquity) * 100.0 else 0.0
    val dollarGain = if (tpPrice > 0) Math.abs(tpPrice - currentPrice) * qty else 0.0
    val rMultiple = if (dollarRisk > 0) dollarGain / dollarRisk else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        // Top Navigation Header
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
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.container)
                        .clickable { onBack() }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SunsetOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = strategy.name,
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${strategy.category.label} • ${strategy.recommendedTimeframe} Timeframe",
                        color = SunsetOrange,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.container)
                    .clickable { onDismiss() }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Deep-Dive & Lab Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. STRATEGY THESIS & EXPLANATION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.container)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(16.dp))
                    Text(
                        text = "STRATEGY THESIS & MECHANICS",
                        color = colors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = strategy.description,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            // 2. PROS & CONS BREAKDOWN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pros
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PnlPositive.copy(alpha = 0.05f))
                        .border(1.dp, PnlPositive.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PnlPositive, modifier = Modifier.size(14.dp))
                        Text("PROS & EDGES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                    }
                    strategy.pros.forEach { pro ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 1.dp)) {
                            Text("•", color = PnlPositive, fontSize = 11.sp)
                            Text(pro, color = colors.textPrimary, fontSize = 10.sp, lineHeight = 14.sp)
                        }
                    }
                }

                // Cons
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SunsetOrange.copy(alpha = 0.05f))
                        .border(1.dp, SunsetOrange.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(14.dp))
                        Text("CONS & RISKS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                    }
                    strategy.cons.forEach { con ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 1.dp)) {
                            Text("•", color = SunsetOrange, fontSize = 11.sp)
                            Text(con, color = colors.textPrimary, fontSize = 10.sp, lineHeight = 14.sp)
                        }
                    }
                }
            }

            // 3. BACKTEST EVIDENCE & DATA DURATION PROOF
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.container)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = PnlPositiveCyan, modifier = Modifier.size(16.dp))
                        Text("BACKTEST VERIFICATION EVIDENCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                    }
                    Text(strategy.tickModelingQuality, fontSize = 9.sp, color = PnlPositive, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                // Period, Execution model
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.surface)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("BACKTEST DATA PERIOD", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text(strategy.backtestPeriod, fontSize = 11.sp, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("SIMULATED COSTS", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text(strategy.executionModel.take(28), fontSize = 10.sp, color = colors.textSecondary, fontFamily = FontFamily.Monospace)
                    }
                }

                // Comprehensive Metrics Grid (Sharpe, Sortino, R:R, Win rate, Expectancy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Win rate & Total Trades
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("WIN RATE", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("${strategy.baseWinRate}%", fontSize = 14.sp, color = PnlPositive, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("%,d trades".format(strategy.baseTradesCount), fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Profit Factor & Drawdown
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("PROFIT FACTOR", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("${strategy.baseProfitFactor}", fontSize = 14.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Max DD: ${strategy.baseMaxDrawdown}%", fontSize = 9.sp, color = PnlNegative, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Sharpe & Sortino
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("SHARPE / SORTINO", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("${strategy.baseSharpeRatio} / ${strategy.baseSortinoRatio}", fontSize = 12.sp, color = PnlPositiveCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Avg Hold: ${strategy.baseAvgDuration}", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        }
                    }

                    // Expectancy & R:R
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("EXPECTANCY", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("+${strategy.baseExpectancyR}R", fontSize = 14.sp, color = PnlPositive, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("Avg R:R: 1:${strategy.baseAvgRiskReward}", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // 4. INTERACTIVE PARAMETER CUSTOMIZATION LAB & REALTIME IMPACT HUD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .border(1.5.dp, SunsetOrange.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header & Reset Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(18.dp))
                        Column {
                            Text(
                                text = "REAL-TIME PARAMETER PLAYGROUND",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SunsetOrange,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Tweak variables to observe simulated impact on backtested edge",
                                fontSize = 10.sp,
                                color = colors.textMuted
                            )
                        }
                    }

                    // Reset Defaults
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.container)
                            .clickable {
                                p1 = strategy.param1Default
                                p2 = strategy.param2Default
                                p3 = strategy.param3Default
                                p4 = strategy.param4Default
                                p5 = strategy.param5Default
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(12.dp))
                            Text("RESET", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // REALTIME DYNAMIC IMPACT HUD (SHOWS HOW CUSTOMIZATION SHIFTS BACKTEST STATS)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.container)
                        .border(1.dp, dynamicStats.statusColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SIMULATED BACKTEST IMPACT (LIVE)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(dynamicStats.statusColor.copy(alpha = 0.2f))
                                .border(1.dp, dynamicStats.statusColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = dynamicStats.optimizationStatus,
                                color = dynamicStats.statusColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Dynamic Metrics Delta Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Win Rate
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surface)
                                .padding(6.dp)
                        ) {
                            Column {
                                Text("ADJ. WIN RATE", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = "${String.format("%.1f", dynamicStats.adjustedWinRate)}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dynamicStats.winRateDelta >= 0) PnlPositive else PnlNegative,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (dynamicStats.winRateDelta >= 0) "+${String.format("%.1f", dynamicStats.winRateDelta)}%" else "${String.format("%.1f", dynamicStats.winRateDelta)}%",
                                    fontSize = 9.sp,
                                    color = if (dynamicStats.winRateDelta >= 0) PnlPositive else PnlNegative,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Profit Factor
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surface)
                                .padding(6.dp)
                        ) {
                            Column {
                                Text("ADJ. PROFIT FACTOR", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = String.format("%.2f", dynamicStats.adjustedProfitFactor),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (dynamicStats.profitFactorDelta >= 0) "+${String.format("%.2f", dynamicStats.profitFactorDelta)}" else String.format("%.2f", dynamicStats.profitFactorDelta),
                                    fontSize = 9.sp,
                                    color = if (dynamicStats.profitFactorDelta >= 0) PnlPositive else PnlNegative,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Max Drawdown
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surface)
                                .padding(6.dp)
                        ) {
                            Column {
                                Text("ADJ. MAX DD", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = "${String.format("%.1f", dynamicStats.adjustedMaxDrawdown)}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PnlNegative,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (dynamicStats.maxDrawdownDelta > 0) "+${String.format("%.1f", dynamicStats.maxDrawdownDelta)}% safer" else "${String.format("%.1f", dynamicStats.maxDrawdownDelta)}%",
                                    fontSize = 9.sp,
                                    color = if (dynamicStats.maxDrawdownDelta >= 0) PnlPositive else PnlNegative,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Trades Count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surface)
                                .padding(6.dp)
                        ) {
                            Column {
                                Text("ADJ. TRADES", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                                Text(
                                    text = "%,d".format(dynamicStats.adjustedTradesCount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (dynamicStats.tradesDelta >= 0) "+${dynamicStats.tradesDelta}" else "${dynamicStats.tradesDelta} filter",
                                    fontSize = 9.sp,
                                    color = colors.textMuted,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Diagnosis Note
                    Text(
                        text = dynamicStats.diagnosisNote,
                        fontSize = 10.sp,
                        color = colors.textSecondary,
                        lineHeight = 14.sp
                    )
                }

                // 5 CUSTOMIZABLE PARAMETER SLIDERS
                // Param 1: Lookback
                ParameterSliderRow(
                    name = strategy.param1Name,
                    value = p1,
                    onValueChange = { p1 = it },
                    min = strategy.param1Min,
                    max = strategy.param1Max,
                    step = strategy.param1Step,
                    unit = strategy.param1Unit,
                    defaultValue = strategy.param1Default
                )

                // Param 2: Stop Loss ATR / Multiple
                ParameterSliderRow(
                    name = strategy.param2Name,
                    value = p2,
                    onValueChange = { p2 = it },
                    min = strategy.param2Min,
                    max = strategy.param2Max,
                    step = strategy.param2Step,
                    unit = strategy.param2Unit,
                    defaultValue = strategy.param2Default
                )

                // Param 3: Take Profit Target
                ParameterSliderRow(
                    name = strategy.param3Name,
                    value = p3,
                    onValueChange = { p3 = it },
                    min = strategy.param3Min,
                    max = strategy.param3Max,
                    step = strategy.param3Step,
                    unit = strategy.param3Unit,
                    defaultValue = strategy.param3Default
                )

                // Param 4: Filter Threshold
                ParameterSliderRow(
                    name = strategy.param4Name,
                    value = p4,
                    onValueChange = { p4 = it },
                    min = strategy.param4Min,
                    max = strategy.param4Max,
                    step = strategy.param4Step,
                    unit = strategy.param4Unit,
                    defaultValue = strategy.param4Default
                )

                // Param 5: Risk per Trade
                ParameterSliderRow(
                    name = strategy.param5Name,
                    value = p5,
                    onValueChange = { p5 = it },
                    min = strategy.param5Min,
                    max = strategy.param5Max,
                    step = strategy.param5Step,
                    unit = strategy.param5Unit,
                    defaultValue = strategy.param5Default
                )
            }

            // 5. LIVE EXECUTION CONFIGURATION & STOP LOSS (SL) SPECIFICATION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.container)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "RUNNER DEPLOYMENT SETTINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    fontFamily = FontFamily.Monospace
                )

                // Symbol Picker & Live Mark Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MARKET PAIR", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("LIVE: $${String.format("%,.2f", currentPrice)}", fontSize = 11.sp, color = PnlPositive, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val availablePairs = (strategy.compatibleSymbols + listOf("XAU/USD", "BTC/USDT", "BTC / ETH")).distinct()
                    availablePairs.forEach { sym ->
                        val isSelected = sym == selectedSymbol
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SunsetOrange.copy(alpha = 0.15f) else colors.surface)
                                .border(1.dp, if (isSelected) SunsetOrange else colors.borderSubtle, RoundedCornerShape(8.dp))
                                .clickable { selectedSymbol = sym }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sym,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) SunsetOrange else colors.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Order Side (BUY / LONG vs SELL / SHORT) & Qty Size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedSide == OrderSide.BUY) PnlPositive else Color.Transparent)
                                .clickable { selectedSide = OrderSide.BUY }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("BUY (LONG)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedSide == OrderSide.BUY) Color.White else colors.textMuted, fontFamily = FontFamily.Monospace)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedSide == OrderSide.SELL) PnlNegative else Color.Transparent)
                                .clickable { selectedSide = OrderSide.SELL }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SELL (SHORT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedSide == OrderSide.SELL) Color.White else colors.textMuted, fontFamily = FontFamily.Monospace)
                        }
                    }

                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it },
                        label = { Text("Qty Size", fontSize = 10.sp) },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SunsetOrange,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }

                // STOP LOSS (SL) SPECIFICATION (EXPLICIT & BOUND TO USER'S CUSTOM PARAMETERS)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PnlNegative.copy(alpha = 0.05f))
                        .border(1.dp, PnlNegative.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = PnlNegative, modifier = Modifier.size(14.dp))
                            Text("CUSTOM STOP LOSS (SL)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlNegative, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            text = "RISK: $${String.format("%.2f", dollarRisk)} (${String.format("%.2f", riskPctOfEquity)}%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PnlNegative,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    OutlinedTextField(
                        value = slInput,
                        onValueChange = { slInput = it },
                        label = { Text("Stop Loss Price ($)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forward_test_custom_sl_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PnlNegative,
                            unfocusedBorderColor = PnlNegative.copy(alpha = 0.5f),
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }

                // TAKE PROFIT (TP) TARGET
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PnlPositive.copy(alpha = 0.05f))
                        .border(1.dp, PnlPositive.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CUSTOM TAKE PROFIT (TP)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "+$${String.format("%.2f", dollarGain)} (${String.format("%.1f", rMultiple)}R)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PnlPositive,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    OutlinedTextField(
                        value = tpInput,
                        onValueChange = { tpInput = it },
                        label = { Text("Take Profit Price ($)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forward_test_custom_tp_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PnlPositive,
                            unfocusedBorderColor = PnlPositive.copy(alpha = 0.5f),
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // BOTTOM ACTION ROW: DEPLOY RUNNER ON CUSTOMIZED SETTINGS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = colors.container),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(0.7f)
            ) {
                Text("BACK", color = colors.textMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = {
                    val finalQty = qtyInput.toDoubleOrNull() ?: 1.0
                    val finalSl = slInput.toDoubleOrNull() ?: (if (selectedSide == OrderSide.BUY) currentPrice * 0.99 else currentPrice * 1.01)
                    val finalTp = tpInput.toDoubleOrNull() ?: (if (selectedSide == OrderSide.BUY) currentPrice * 1.02 else currentPrice * 0.98)
                    val summary = "${strategy.param1Name}: ${String.format("%.1f", p1)} | ${strategy.param2Name}: ${String.format("%.1f", p2)} | ${strategy.param3Name}: ${String.format("%.1f", p3)} | Risk: ${String.format("%.1f", p5)}%"
                    onDeploy(selectedSymbol, selectedSide, finalQty, finalSl, finalTp, summary)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .testTag("deploy_runner_on_custom_settings_btn")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "START RUNNER ON THESE SETTINGS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ParameterSliderRow(
    name: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    step: Float,
    unit: String,
    defaultValue: Float
) {
    val colors = LocalQuantKitColors.current
    val isChanged = Math.abs(value - defaultValue) > 0.01f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isChanged) SunsetOrange.copy(alpha = 0.05f) else colors.container)
            .border(1.dp, if (isChanged) SunsetOrange.copy(alpha = 0.3f) else colors.borderSubtle, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(name, fontSize = 11.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                if (isChanged) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SunsetOrange)
                    )
                }
            }
            Text(
                text = "${if (step < 1.0f) String.format("%.1f", value) else String.format("%.0f", value)} $unit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isChanged) SunsetOrange else colors.textPrimary,
                fontFamily = FontFamily.Monospace
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = SunsetOrange,
                activeTrackColor = SunsetOrange,
                inactiveTrackColor = colors.borderSubtle
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
        )
    }
}
