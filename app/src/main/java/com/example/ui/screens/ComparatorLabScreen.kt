package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SsidChart
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OrderSide
import com.example.data.QuantKitRepository
import com.example.data.VirtualForwardTestingEngine
import com.example.ui.components.ComparativeEquityTrajectoryChart
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.MlEnginePurple
import com.example.ui.theme.PnlNegative
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.PnlPositiveCyan
import com.example.ui.theme.StatusScanning
import com.example.ui.theme.SunsetOrange
import com.example.ui.theme.SunsetOrangeDark

@Composable
fun ComparatorLabScreen(
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current

    var showCurveA by remember { mutableStateOf(true) }
    var showCurveB by remember { mutableStateOf(true) }
    var deployState by remember { mutableStateOf(0) } // 0: Normal, 1: Deploying, 2: Deployed
    var exportNotice by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Meta & Quick Actions
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            .clip(RoundedCornerShape(20.dp))
                            .background(PnlPositiveCyan.copy(alpha = 0.15f))
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
                                    .background(PnlPositiveCyan)
                            )
                            Text(
                                text = "180k Bars Synced",
                                color = PnlPositiveCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "Walk-Forward 10x",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Theme Toggle Pill
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

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Comparator & Monte Carlo",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.SsidChart,
                        contentDescription = "Chart",
                        tint = SunsetOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "Head-to-head out-of-sample statistical edge & ruin probability",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Active Strategy Pair & Filter Strip
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
                Text(
                    text = "ACTIVE STRATEGY PAIR",
                    color = colors.textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Resampled: 1,000 runs",
                    color = SunsetOrange,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // Strategy A Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.container)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SunsetOrange)
                    )
                    Column {
                        Text("Gold London ORB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("M15 • VWAP Reversion • SharkEx", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SunsetOrange.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("A", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                }
            }

            // Strategy B Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.container)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PnlPositiveCyan)
                    )
                    Column {
                        Text("BTC Turtle Donchian", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("H4 • Trend Breakout • SharkPerp", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(PnlPositiveCyan.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("B", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                }
            }

            // Filter Scroller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.containerSubtle)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(12.dp))
                        Text("Realistic Slippage (0.04%)", fontSize = 10.sp, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.container)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("10-Fold OOS", fontSize = 10.sp, color = colors.textSecondary, fontFamily = FontFamily.Monospace)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.container)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Maker Tier 1", fontSize = 10.sp, color = colors.textSecondary, fontFamily = FontFamily.Monospace)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(PnlPositiveCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PnlPositiveCyan, modifier = Modifier.size(12.dp))
                        Text("Add Strat C (Basis Carry)", fontSize = 10.sp, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Head-to-Head KPI Comparative Columns
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Head-to-Head Edge Metrics",
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Leader",
                        tint = PnlPositive,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "ORB Leads Sharpe (+0.22)",
                        color = PnlPositive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Strategy A Column Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SunsetOrange))
                            Text("ORB LONDON", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                        }
                        Text("+148.5%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                    }

                    Column {
                        Text("SHARPE RATIO", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text("1.84", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                    }

                    MetricTile(label = "Profit Factor", value = "2.14")
                    MetricTile(label = "Win Rate", value = "53.8%")
                    MetricTile(label = "Max Drawdown", value = "-6.2%", valueColor = PnlPositive)
                    MetricTile(label = "Expectancy", value = "+0.75R", valueColor = SunsetOrange)
                    MetricTile(label = "Calmar", value = "3.80")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.container)
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Consistency", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Ultra High", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Strategy B Column Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(PnlPositiveCyan))
                            Text("BTC TURTLE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                        }
                        Text("+245.0%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                    }

                    Column {
                        Text("SHARPE RATIO", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Text("1.62", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                    }

                    MetricTile(label = "Profit Factor", value = "1.95")
                    MetricTile(label = "Win Rate", value = "44.1%")
                    MetricTile(label = "Max Drawdown", value = "-14.8%", valueColor = PnlNegative)
                    MetricTile(label = "Expectancy", value = "+1.12R", valueColor = PnlPositiveCyan)
                    MetricTile(label = "Calmar", value = "2.40")

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.container)
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Trend Capture", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Exceptional", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Equity Trajectory ($10,000 Base) Rolling 24-Month Chart
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
                Column {
                    Text(
                        text = "Equity Trajectory ($10,000 Base)",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Rolling 24-Month Walk-Forward Out-Of-Sample",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (showCurveA) SunsetOrange else colors.container)
                            .clickable { showCurveA = !showCurveA }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ORB",
                            color = if (showCurveA) Color.White else colors.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (showCurveB) PnlPositiveCyan else colors.container)
                            .clickable { showCurveB = !showCurveB }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Turtle",
                            color = if (showCurveB) Color.Black else colors.textMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                ComparativeEquityTrajectoryChart(
                    showCurveA = showCurveA,
                    showCurveB = showCurveB
                )

                // Terminal Collateral Floating Pill
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface.copy(alpha = 0.9f))
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column {
                        Text("Terminal Collateral", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("A: $24,850", fontSize = 11.sp, color = SunsetOrange, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text("B: $34,500", fontSize = 11.sp, color = PnlPositiveCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Drawdown Underwater footer label
                Text(
                    text = "Drawdown Underwater: A (-6.2%) vs B (-14.8%)",
                    color = colors.textMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                )
            }
        }

        // Monte Carlo Ruin Engine
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
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Dice",
                        tint = MlEnginePurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text("Monte Carlo Ruin Engine", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("1,000 trade-order resamples & parameter bootstrapping", fontSize = 10.sp, color = colors.textMuted)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MlEnginePurple.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("N=1000", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MlEnginePurple, fontFamily = FontFamily.Monospace)
                }
            }

            // Security Ruin Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.container)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PnlPositive.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = PnlPositive,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("0.02%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PnlPositive.copy(alpha = 0.15f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text("EXTREMELY SAFE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Text("Combined Composite Ruin Probability", fontSize = 10.sp, color = colors.textMuted)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("MIN SAFETY CAPITAL", fontSize = 9.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                    Text("$1,250", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                }
            }

            // Percentile Rows
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuantKitRepository.monteCarloRows.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.container.copy(alpha = 0.7f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = row.percentile,
                                color = if (row.isStress) PnlNegative else colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = row.regimeDescription,
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = row.simulatedOutcome,
                                color = if (row.isStress) PnlNegative else PnlPositive,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = row.maxDrawdown,
                                color = if (row.isStress) PnlNegative else colors.textMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Market Regime Robustness Matrix (HMM)
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Hub, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(18.dp))
                    Column {
                        Text("Regime Robustness (HMM)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        Text("Walk-Forward stress across volatility regimes", fontSize = 11.sp, color = colors.textMuted)
                    }
                }
                Text("DUAL ALPHA", fontSize = 10.sp, color = PnlPositiveCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            // 2x2 Regime Tiles
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Bull Trend
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.container)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("BULL TREND", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PnlPositive, modifier = Modifier.size(14.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ORB", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Grade A", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Turtle", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Grade A+", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Bear Trend
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.container)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("BEAR TREND", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = PnlNegative, modifier = Modifier.size(14.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ORB", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Grade B+", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Turtle", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Grade A", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlPositiveCyan, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // High-Vol Chop
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.container)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("HIGH-VOL CHOP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                            Icon(Icons.Default.Waves, contentDescription = null, tint = StatusScanning, modifier = Modifier.size(14.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ORB", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Grade B", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SunsetOrange, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Turtle", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Grade C-", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlNegative, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                // Low-Vol Squeeze
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.container)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("LOW-VOL SQUEEZE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary, fontFamily = FontFamily.Monospace)
                            Icon(Icons.Default.Compress, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(14.dp))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("ORB", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Grade A", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlPositive, fontFamily = FontFamily.Monospace)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Turtle", fontSize = 10.sp, color = colors.textMuted, fontFamily = FontFamily.Monospace)
                            Text("Grade D", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PnlNegative, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // QuantKit AI Recommendation Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        listOf(colors.surface, colors.containerSubtle, colors.surface)
                    )
                )
                .border(1.dp, SunsetOrange.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Recommend,
                    contentDescription = "AI",
                    tint = SunsetOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "QUANTKIT AI RECOMMENDATION",
                    color = colors.textPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            Text(
                text = "Combining 60% Gold London ORB with 40% BTC Turtle yields an optimal Sharpe of 2.28 with maximum portfolio drawdown contained under 7.4% across all resampled Monte Carlo cones.",
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Correlation: +0.12 (Orthogonal)",
                    color = colors.textMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Composite Edge: +212%",
                    color = PnlPositive,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Action CTA Buttons
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (deployState == 2) Brush.horizontalGradient(listOf(PnlPositive, PnlPositive))
                        else Brush.horizontalGradient(listOf(SunsetOrange, SunsetOrangeDark))
                    )
                    .clickable {
                        if (deployState == 0) {
                            deployState = 1
                        } else if (deployState == 1) {
                            deployState = 2
                            // Automatically arm virtual forward test order into virtual account
                            VirtualForwardTestingEngine.placeVirtualForwardOrder(
                                runnerId = "QK-COMP-6040",
                                symbol = "BTC/USDT",
                                strategy = "60/40 Momentum MeanRevert",
                                side = OrderSide.BUY,
                                qty = 0.5,
                                stopLoss = 93200.0,
                                takeProfit = 96400.0
                            )
                        } else {
                            deployState = 0
                        }
                    }
                    .padding(vertical = 14.dp)
                    .testTag("deploy_fleet_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = when (deployState) {
                            1 -> Icons.Default.Sync
                            2 -> Icons.Default.CheckCircle
                            else -> Icons.Default.RocketLaunch
                        },
                        contentDescription = "Deploy",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when (deployState) {
                            1 -> "Arming Fleet Allocation..."
                            2 -> "Composite Armed to Live Fleet!"
                            else -> "DEPLOY 60/40 COMPOSITE TO FLEET"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.container)
                        .clickable { exportNotice = "Tear Sheet exported as PDF (QuantKit_Edge_Report.pdf)" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = PnlPositiveCyan, modifier = Modifier.size(16.dp))
                        Text("Export Tear Sheet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.container)
                        .clickable { exportNotice = "Raw trade resample logs exported as CSV (MonteCarlo_N1000.csv)" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Dataset, contentDescription = "CSV", tint = SunsetOrange, modifier = Modifier.size(16.dp))
                        Text("Export Raw CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                }
            }

            AnimatedVisibility(visible = exportNotice != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(PnlPositive.copy(alpha = 0.15f))
                        .border(1.dp, PnlPositive.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(exportNotice ?: "", fontSize = 11.sp, color = PnlPositive, fontFamily = FontFamily.Monospace)
                        Text(
                            text = "OK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PnlPositive,
                            modifier = Modifier.clickable { exportNotice = null }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    valueColor: Color? = null
) {
    val colors = LocalQuantKitColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.container.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 10.sp, color = colors.textSecondary, fontFamily = FontFamily.Monospace)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor ?: colors.textPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}
