package com.example.data

import androidx.compose.ui.graphics.Color

enum class RunnerStatus {
    IN_POSITION,
    SCANNING,
    STANDBY,
    HALTED
}

data class Runner(
    val id: String,
    val symbol: String,
    val baseAsset: String,
    val timeframe: String,
    val leverage: String,
    val strategyName: String,
    val venue: String,
    val status: RunnerStatus,
    val statusLabel: String,
    val entryPrice: String? = null,
    val currentMark: String? = null,
    val unrealizedPnl: String? = null,
    val unrealizedPnlPct: String? = null,
    val isPnlPositive: Boolean = true,
    val riskUsd: String? = null,
    val stopLoss: String? = null,
    val takeProfit: String? = null,
    val holdTime: String? = null,
    val rangeHigh: String? = null,
    val adxValue: String? = null,
    val pendingDelta: String? = null,
    val zScore: String? = null,
    val halfLife: String? = null
)

data class ExecutionLadderStep(
    val id: String,
    val levelName: String,
    val price: String,
    val rMultiple: String,
    val badgeText: String,
    val description: String,
    val state: StepState,
    val rightValue: String? = null,
    val isStrikethrough: Boolean = false
)

enum class StepState {
    PENDING,
    LIVE_MARK,
    FILLED,
    ENTRY,
    RISK_FREE_STOP
}

data class MicrostructureMetric(
    val title: String,
    val primaryValue: String,
    val secondaryValue: String,
    val isAccent: Boolean = false
)

data class TelemetryLog(
    val timestamp: String,
    val message: String,
    val highlightColor: Color? = null
)

data class StrategyMetric(
    val label: String,
    val stratAValue: String,
    val stratBValue: String,
    val stratAHighlight: Boolean = false,
    val stratBHighlight: Boolean = false
)

data class MonteCarloRow(
    val percentile: String,
    val regimeDescription: String,
    val simulatedOutcome: String,
    val maxDrawdown: String,
    val isStress: Boolean = false
)

data class RegimeRow(
    val regimeName: String,
    val iconName: String,
    val stratAGrade: String,
    val stratBGrade: String,
    val stratANote: String? = null,
    val stratBNote: String? = null
)

data class CircuitBreakerItem(
    val id: String,
    val title: String,
    val statusBadge: String,
    val isSafe: Boolean,
    val description: String,
    val currentValue: String? = null,
    val maxValue: String? = null,
    val progressPct: Float? = null
)

data class WatchdogLogItem(
    val timeUtc: String,
    val tag: String,
    val isPositive: Boolean,
    val description: String
)
