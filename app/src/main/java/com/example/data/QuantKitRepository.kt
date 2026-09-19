package com.example.data

import com.example.ui.theme.PnlNegative
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.PnlPositiveCyan
import com.example.ui.theme.SunsetOrange

object QuantKitRepository {

    val runners = listOf(
        Runner(
            id = "QK-99214-GOLD",
            symbol = "XAU/USD",
            baseAsset = "AU",
            timeframe = "5m",
            leverage = "75x",
            strategyName = "London ORB • Shark Perpetuals",
            venue = "SharkEx",
            status = RunnerStatus.IN_POSITION,
            statusLabel = "IN POSITION",
            entryPrice = "2,642.10",
            currentMark = "2,647.70",
            unrealizedPnl = "+$420.50",
            unrealizedPnlPct = "+15.8%",
            isPnlPositive = true,
            riskUsd = "$10.00",
            stopLoss = "2,636.50",
            takeProfit = "2,653.20",
            holdTime = "18m hold"
        ),
        Runner(
            id = "QK-88102-BTC",
            symbol = "BTC/USDT",
            baseAsset = "BTC",
            timeframe = "4h",
            leverage = "150x",
            strategyName = "Turtle Donchian S1 • Breakout",
            venue = "SharkPerp",
            status = RunnerStatus.SCANNING,
            statusLabel = "SCANNING",
            rangeHigh = "94,800.00",
            adxValue = "32.4 (Strong)",
            pendingDelta = "Pending: +184.2 pts"
        ),
        Runner(
            id = "QK-77401-PAIRS",
            symbol = "BTC / ETH",
            baseAsset = "ETH",
            timeframe = "1h",
            leverage = "PAIRS",
            strategyName = "Cointegration Mean Reversion",
            venue = "Cross-Perp",
            status = RunnerStatus.STANDBY,
            statusLabel = "STANDBY",
            zScore = "-1.40 (Goal ±2.0)",
            halfLife = "Half-life: 14.2h"
        )
    )

    val executionLadderSteps = listOf(
        ExecutionLadderStep(
            id = "tp2",
            levelName = "TP2",
            price = "2,653.20",
            rMultiple = "+3.0R",
            badgeText = "+3.0R",
            description = "Active Pending Limit (50% remaining)",
            state = StepState.PENDING,
            rightValue = null,
            isStrikethrough = false
        ),
        ExecutionLadderStep(
            id = "mark",
            levelName = "MARK",
            price = "2,647.70",
            rMultiple = "LIVE",
            badgeText = "LIVE",
            description = "Trailing Stop @ 2,645.10 (Locking +$180)",
            state = StepState.LIVE_MARK,
            rightValue = null,
            isStrikethrough = false
        ),
        ExecutionLadderStep(
            id = "tp1",
            levelName = "TP1",
            price = "2,647.20",
            rMultiple = "+1.5R FILLED",
            badgeText = "+1.5R FILLED",
            description = "50% Closed • SL automatically shifted to BE",
            state = StepState.FILLED,
            rightValue = "+$210.25",
            isStrikethrough = true
        ),
        ExecutionLadderStep(
            id = "entry",
            levelName = "ENTRY",
            price = "2,642.10",
            rMultiple = "MAKER 0.02%",
            badgeText = "MAKER 0.02%",
            description = "Filled via Post-Only Passive Limit",
            state = StepState.ENTRY,
            rightValue = null,
            isStrikethrough = false
        ),
        ExecutionLadderStep(
            id = "be-stop",
            levelName = "BE-STOP",
            price = "2,642.10",
            rMultiple = "RISK-FREE",
            badgeText = "RISK-FREE",
            description = "Initial SL was 2,636.50 (-1.0R)",
            state = StepState.RISK_FREE_STOP,
            rightValue = "0.00 Risk",
            isStrikethrough = false
        )
    )

    val botTelemetryLogs = listOf(
        TelemetryLog("[12:44:02]", "TP1 hit @ 2,647.20. Closed 50% size (+0.75R).", SunsetOrange),
        TelemetryLog("[12:44:03]", "Watchdog auto-adjusted SL to 2,642.10 (Risk-free lock).", PnlPositive),
        TelemetryLog("[12:46:10]", "Heartbeat OK. Spread 0.12 pts. 0 orphan orders.", null),
        TelemetryLog("[12:47:01]", "Chandelier Trailing Floor calculated @ 2,645.10.", PnlPositiveCyan)
    )

    val execStreamLogs = listOf(
        TelemetryLog("[12:44:02]", "XAU/USD: Partial TP1 order placed at 2,653.20.", PnlPositive),
        TelemetryLog("[12:43:15]", "Watchdog: 2 orders active on SharkEx. Healthy.", PnlPositiveCyan)
    )

    val comparatorMetrics = listOf(
        StrategyMetric("Sharpe Ratio", "1.84", "1.62", stratAHighlight = true),
        StrategyMetric("Profit Factor", "2.14", "1.95", stratAHighlight = true),
        StrategyMetric("Win Rate", "53.8%", "44.1%", stratAHighlight = true),
        StrategyMetric("Max Drawdown", "-6.2%", "-14.8%", stratAHighlight = true),
        StrategyMetric("Expectancy", "+0.75R", "+1.12R", stratBHighlight = true),
        StrategyMetric("Calmar", "3.80", "2.40", stratAHighlight = true),
        StrategyMetric("Edge Metric", "Ultra High Consistency", "Exceptional Trend Capture", stratAHighlight = true, stratBHighlight = true)
    )

    val monteCarloRows = listOf(
        MonteCarloRow(
            percentile = "95th %ile",
            regimeDescription = "(Optimistic Momentum)",
            simulatedOutcome = "+310.4%",
            maxDrawdown = "Max DD: -4.1%",
            isStress = false
        ),
        MonteCarloRow(
            percentile = "50th %ile",
            regimeDescription = "(Median Path)",
            simulatedOutcome = "+198.2%",
            maxDrawdown = "Max DD: -8.8%",
            isStress = false
        ),
        MonteCarloRow(
            percentile = "05th %ile",
            regimeDescription = "(Adverse Regime Stress)",
            simulatedOutcome = "+42.5%",
            maxDrawdown = "Max DD: -18.2%",
            isStress = true
        )
    )

    val regimeRows = listOf(
        RegimeRow("Bull Trend", "trending_up", "Grade A", "Grade A+"),
        RegimeRow("Bear Trend", "trending_down", "Grade B+", "Grade A"),
        RegimeRow("High-Vol Chop", "waves", "Grade B (Filter)", "Grade C- (Chop)"),
        RegimeRow("Low-Vol Squeeze", "compress", "Grade A (Tight)", "Grade D (Flat)")
    )

    val circuitBreakers = listOf(
        CircuitBreakerItem(
            id = "dd_guard",
            title = "Daily Drawdown Guard",
            statusBadge = "Safe",
            isSafe = true,
            description = "Breaches automatically trigger an immediate 24h trading cooldown.",
            currentValue = "-3.5% Today",
            maxValue = "-10.0% Max Cap",
            progressPct = 0.35f
        ),
        CircuitBreakerItem(
            id = "slippage_sentinel",
            title = "Max Slippage Sentinel",
            statusBadge = "Active",
            isSafe = true,
            description = "Tolerates ≤ 0.25% on entry fills"
        ),
        CircuitBreakerItem(
            id = "symbol_lock",
            title = "Symbol Stacking Lock",
            statusBadge = "Enforced",
            isSafe = true,
            description = "Strict 1 position per symbol (BTC, XAU)"
        ),
        CircuitBreakerItem(
            id = "quick_cancel",
            title = "Quick-Cancel Watchdog",
            statusBadge = "0 Orphans",
            isSafe = true,
            description = "Scans every 30 mins for stuck exchange orders. Last ran 4m ago."
        )
    )

    val watchdogAuditLogs = listOf(
        WatchdogLogItem(
            timeUtc = "12:44:03 UTC",
            tag = "AUTO-STEP SL",
            isPositive = true,
            description = "XAU/USD SL auto-stepped to Breakeven (+0.0R) upon TP1 hit."
        ),
        WatchdogLogItem(
            timeUtc = "12:30:00 UTC",
            tag = "HEARTBEAT",
            isPositive = true,
            description = "Routine 30-min heartbeat: SharkExchange socket OK, clock drift +0.002s."
        ),
        WatchdogLogItem(
            timeUtc = "11:15:22 UTC",
            tag = "CONSTRAINT OK",
            isPositive = true,
            description = "Symbol lock validated for BTC/USDT Donchian runner."
        )
    )
}
