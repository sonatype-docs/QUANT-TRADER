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
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderSide
import com.example.data.StrategyCatalog
import com.example.data.StrategyCategory
import com.example.data.StrategyModel
import com.example.data.VirtualForwardTestingEngine
import com.example.ui.components.StartForwardTestDialog
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.MlEnginePurple
import com.example.ui.theme.PnlNegative
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.PnlPositiveCyan
import com.example.ui.theme.SunsetOrange

@Composable
fun StrategyResearchScreen(
    isResearchMode: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current
    val allStrategies = StrategyCatalog.strategies
    var selectedStrategyIndex by remember { mutableStateOf(0) }
    val currentStrategy = allStrategies[selectedStrategyIndex]

    // Parameter sliders for current strategy
    var p1 by remember(currentStrategy.id) { mutableFloatStateOf(currentStrategy.param1Default) }
    var p2 by remember(currentStrategy.id) { mutableFloatStateOf(currentStrategy.param2Default) }
    var p3 by remember(currentStrategy.id) { mutableFloatStateOf(currentStrategy.param3Default) }
    var p4 by remember(currentStrategy.id) { mutableFloatStateOf(currentStrategy.param4Default) }
    var p5 by remember(currentStrategy.id) { mutableFloatStateOf(currentStrategy.param5Default) }

    // Legacy catalogue estimate; authoritative metrics come from the Quant Research backend
    val dynamicStats = remember(currentStrategy.id, p1, p2, p3, p4, p5) {
        StrategyCatalog.calculateDynamicImpact(currentStrategy, p1, p2, p3, p4, p5)
    }

    var showDeployDialog by remember { mutableStateOf(false) }
    var deployedNotice by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
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
                    imageVector = if (isResearchMode) Icons.Default.Psychology else Icons.Default.Tune,
                    contentDescription = null,
                    tint = SunsetOrange,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = if (isResearchMode) "Quantitative Research & Backtest Lab" else "Strategy Optimization Engine",
                        color = colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "8 Quant models • server-side backtest required • legacy catalogue metrics",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                }
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

        // Strategy Selector Carousel / Pills
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "SELECT STRATEGY MODEL (${allStrategies.size} AVAILABLE)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted,
                fontFamily = FontFamily.Monospace
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allStrategies.take(4).forEachIndexed { index, strat ->
                    val isSelected = index == selectedStrategyIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SunsetOrange.copy(alpha = 0.2f) else colors.container)
                            .border(1.dp, if (isSelected) SunsetOrange else colors.borderSubtle, RoundedCornerShape(8.dp))
                            .clickable { selectedStrategyIndex = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strat.name.split(" ").firstOrNull() ?: strat.name,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SunsetOrange else colors.textPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allStrategies.drop(4).forEachIndexed { i, strat ->
                    val index = i + 4
                    val isSelected = index == selectedStrategyIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SunsetOrange.copy(alpha = 0.2f) else colors.container)
                            .border(1.dp, if (isSelected) SunsetOrange else colors.borderSubtle, RoundedCornerShape(8.dp))
                            .clickable { selectedStrategyIndex = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strat.name.split(" ").firstOrNull() ?: strat.name,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SunsetOrange else colors.textPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Active Strategy Header Card
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
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SunsetOrange.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentStrategy.category.label.uppercase(),
                            color = SunsetOrange,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.container)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentStrategy.recommendedTimeframe,
                            color = colors.textMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Text(
                    text = currentStrategy.backtestPeriod.substringBefore(" ("),
                    fontSize = 10.sp,
                    color = PnlPositiveCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = currentStrategy.name,
                color = colors.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currentStrategy.description,
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            // Pros & Cons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Pros
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PnlPositive.copy(alpha = 0.05f))
                        .border(1.dp, PnlPositive.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("PROS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                    currentStrategy.pros.forEach { pro ->
                        Text("• $pro", fontSize = 10.sp, color = colors.textPrimary, lineHeight = 13.sp)
                    }
                }

                // Cons
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SunsetOrange.copy(alpha = 0.05f))
                        .border(1.dp, SunsetOrange.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("CONS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                    currentStrategy.cons.forEach { con ->
                        Text("• $con", fontSize = 10.sp, color = colors.textPrimary, lineHeight = 13.sp)
                    }
                }
            }
        }

        // Backtest Evidence Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
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
                    Text("HISTORICAL BACKTEST AUDIT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                }
                Text(currentStrategy.tickModelingQuality, fontSize = 9.sp, color = PnlPositive, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }

            // Grid of 4 main metrics
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.container).padding(8.dp)) {
                    Column {
                        Text("WIN RATE", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text("${currentStrategy.baseWinRate}%", fontSize = 13.sp, color = PnlPositive, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("%,d trades".format(currentStrategy.baseTradesCount), fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.container).padding(8.dp)) {
                    Column {
                        Text("PROFIT FACTOR", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text("${currentStrategy.baseProfitFactor}", fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Max DD: ${currentStrategy.baseMaxDrawdown}%", fontSize = 8.sp, color = PnlNegative, fontFamily = FontFamily.Monospace)
                    }
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.container).padding(8.dp)) {
                    Column {
                        Text("SHARPE", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text("${currentStrategy.baseSharpeRatio}", fontSize = 13.sp, color = PnlPositiveCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Sortino: ${currentStrategy.baseSortinoRatio}", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }
                }
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.container).padding(8.dp)) {
                    Column {
                        Text("EXPECTANCY", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text("+${currentStrategy.baseExpectancyR}R", fontSize = 13.sp, color = PnlPositive, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Avg 1:${currentStrategy.baseAvgRiskReward}", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Live Parameter Customizer & Dynamic Impact
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface)
                .border(1.5.dp, SunsetOrange.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(18.dp))
                    Text("LIVE PARAMETER PLAYGROUND", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.container)
                        .clickable {
                            p1 = currentStrategy.param1Default
                            p2 = currentStrategy.param2Default
                            p3 = currentStrategy.param3Default
                            p4 = currentStrategy.param4Default
                            p5 = currentStrategy.param5Default
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(12.dp))
                        Text("RESET", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real-Time Dynamic Impact HUD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.container)
                    .border(1.dp, dynamicStats.statusColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SIMULATED IMPACT ON BACKTEST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textMuted, fontFamily = FontFamily.Monospace)
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.surface).padding(6.dp)) {
                        Column {
                            Text("ADJ. WIN RATE", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("${String.format("%.1f", dynamicStats.adjustedWinRate)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (dynamicStats.winRateDelta >= 0) PnlPositive else PnlNegative, fontFamily = FontFamily.Monospace)
                            Text(if (dynamicStats.winRateDelta >= 0) "+${String.format("%.1f", dynamicStats.winRateDelta)}%" else "${String.format("%.1f", dynamicStats.winRateDelta)}%", fontSize = 8.sp, color = if (dynamicStats.winRateDelta >= 0) PnlPositive else PnlNegative, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.surface).padding(6.dp)) {
                        Column {
                            Text("ADJ. PROFIT FACTOR", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text(String.format("%.2f", dynamicStats.adjustedProfitFactor), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                            Text(if (dynamicStats.profitFactorDelta >= 0) "+${String.format("%.2f", dynamicStats.profitFactorDelta)}" else String.format("%.2f", dynamicStats.profitFactorDelta), fontSize = 8.sp, color = if (dynamicStats.profitFactorDelta >= 0) PnlPositive else PnlNegative, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.surface).padding(6.dp)) {
                        Column {
                            Text("ADJ. MAX DD", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("${String.format("%.1f", dynamicStats.adjustedMaxDrawdown)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PnlNegative, fontFamily = FontFamily.Monospace)
                            Text(if (dynamicStats.maxDrawdownDelta > 0) "+${String.format("%.1f", dynamicStats.maxDrawdownDelta)}% safer" else "${String.format("%.1f", dynamicStats.maxDrawdownDelta)}%", fontSize = 8.sp, color = if (dynamicStats.maxDrawdownDelta >= 0) PnlPositive else PnlNegative, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(6.dp)).background(colors.surface).padding(6.dp)) {
                        Column {
                            Text("ADJ. TRADES", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("%,d".format(dynamicStats.adjustedTradesCount), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                            Text(if (dynamicStats.tradesDelta >= 0) "+${dynamicStats.tradesDelta}" else "${dynamicStats.tradesDelta} filter", fontSize = 8.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Text(
                    text = dynamicStats.diagnosisNote,
                    fontSize = 10.sp,
                    color = colors.textSecondary,
                    lineHeight = 14.sp
                )
            }

            // Sliders
            ResearchParamSlider(name = currentStrategy.param1Name, value = p1, onValueChange = { p1 = it }, min = currentStrategy.param1Min, max = currentStrategy.param1Max, step = currentStrategy.param1Step, unit = currentStrategy.param1Unit)
            ResearchParamSlider(name = currentStrategy.param2Name, value = p2, onValueChange = { p2 = it }, min = currentStrategy.param2Min, max = currentStrategy.param2Max, step = currentStrategy.param2Step, unit = currentStrategy.param2Unit)
            ResearchParamSlider(name = currentStrategy.param3Name, value = p3, onValueChange = { p3 = it }, min = currentStrategy.param3Min, max = currentStrategy.param3Max, step = currentStrategy.param3Step, unit = currentStrategy.param3Unit)
            ResearchParamSlider(name = currentStrategy.param4Name, value = p4, onValueChange = { p4 = it }, min = currentStrategy.param4Min, max = currentStrategy.param4Max, step = currentStrategy.param4Step, unit = currentStrategy.param4Unit)
            ResearchParamSlider(name = currentStrategy.param5Name, value = p5, onValueChange = { p5 = it }, min = currentStrategy.param5Min, max = currentStrategy.param5Max, step = currentStrategy.param5Step, unit = currentStrategy.param5Unit)

            // Direct Forward Runner Dispatch Button
            Button(
                onClick = {
                    val symbol = currentStrategy.compatibleSymbols.firstOrNull() ?: "XAU/USD"
                    val currentPrice = if (symbol.contains("XAU")) 2647.70 else if (symbol.contains("BTC")) 94820.0 else 28.24
                    val sl = currentPrice * (1.0 - (p2 * 0.008))
                    val tp = currentPrice * (1.0 + (p3 * 0.010))
                    val summary = "${currentStrategy.param1Name}: ${String.format("%.1f", p1)} | ${currentStrategy.param2Name}: ${String.format("%.1f", p2)} | ${currentStrategy.param3Name}: ${String.format("%.1f", p3)} | Risk: ${String.format("%.1f", p5)}%"

                    VirtualForwardTestingEngine.placeVirtualForwardOrder(
                        runnerId = "FWD-${symbol.replace("/", "").take(4)}-${System.currentTimeMillis().toString().takeLast(4)}",
                        symbol = symbol,
                        strategy = currentStrategy.name,
                        side = OrderSide.BUY,
                        qty = 1.0,
                        stopLoss = sl,
                        takeProfit = tp,
                        customParametersSummary = summary
                    )
                    deployedNotice = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_custom_strategy_params_btn")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("DEPLOY FORWARD RUNNER ON THESE SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
            }

            AnimatedVisibility(visible = deployedNotice) {
                Text(
                    text = "Runner active! Forward order placed in virtual account with your customized parameters.",
                    fontSize = 10.sp,
                    color = PnlPositive,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Section 2: Factor Correlation Matrix (Research)
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
                    Icon(Icons.Default.Hub, contentDescription = null, tint = MlEnginePurple, modifier = Modifier.size(18.dp))
                    Text("Cross-Strategy Correlation Heatmap", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                }
                Text("Orthogonal: 0.12", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CorrelationRow(assetA = "London Gold ORB (Breakout)", assetB = "Macro Trend Turtle (Trend)", corr = "+0.12", isLow = true)
                CorrelationRow(assetA = "Statistical Dispersion (StatArb)", assetB = "BTC Liquidity Void (OrderFlow)", corr = "+0.08", isLow = true)
                CorrelationRow(assetA = "VWAP Chandelier (MeanRev)", assetB = "Asian Range Sweep (SMC)", corr = "+0.22", isLow = true)
            }
        }

        // Section 3: Machine Learning Execution Optimization
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(18.dp))
                Text("RL Execution Router (Q-Learning L2)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            }

            Text(
                text = "Dynamic tick-level urgency policy selects between Maker Passive Limit and Post-Only cross queue based on real-time Order Book Imbalance (OBI).",
                fontSize = 11.sp,
                color = colors.textSecondary,
                lineHeight = 16.sp
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.container)
                        .padding(8.dp)
                ) {
                    Column {
                        Text("OBI Threshold", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text("+0.68 Bid Favor", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
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
                        Text("Fee Rebate Accrued", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text("+$84.10", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ResearchParamSlider(
    name: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    step: Float,
    unit: String
) {
    val colors = LocalQuantKitColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.container)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, fontSize = 11.sp, color = colors.textPrimary)
            Text(
                text = "${if (step < 1.0f) String.format("%.1f", value) else String.format("%.0f", value)} $unit",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SunsetOrange,
                fontFamily = FontFamily.Monospace
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(thumbColor = SunsetOrange, activeTrackColor = SunsetOrange),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
private fun CorrelationRow(
    assetA: String,
    assetB: String,
    corr: String,
    isLow: Boolean
) {
    val colors = LocalQuantKitColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.container)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$assetA vs $assetB", fontSize = 11.sp, color = colors.textPrimary)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (isLow) PnlPositive.copy(alpha = 0.15f) else SunsetOrange.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = corr,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isLow) PnlPositive else SunsetOrange,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
