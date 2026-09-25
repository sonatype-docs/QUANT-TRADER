package com.example.data

data class VirtualAccount(
    val accountId: String = "VA-QK-88901",
    val accountName: String = "Alpha Forward Test",
    val initialBalance: Double = 25000.0,
    val currentBalance: Double = 25000.0,
    val realizedPnl: Double = 0.0,
    val unrealizedPnl: Double = 0.0,
    val activeOrdersCount: Int = 0,
    val totalTradesExecuted: Int = 0,
    val winTradesCount: Int = 0,
    val lossTradesCount: Int = 0
) {
    val totalEquity: Double get() = currentBalance + unrealizedPnl
    val roiPct: Double get() = if (initialBalance > 0) ((totalEquity - initialBalance) / initialBalance) * 100.0 else 0.0
}

data class VirtualOrder(
    val id: String,
    val runnerId: String,
    val symbol: String,
    val strategy: String,
    val side: OrderSide, // BUY, SELL
    val orderType: String = "MARKET_MAKER",
    val qty: Double,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val unrealizedPnl: Double,
    val unrealizedPnlPct: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val status: VirtualOrderStatus = VirtualOrderStatus.OPEN
)

enum class OrderSide {
    BUY,
    SELL
}

enum class VirtualOrderStatus {
    OPEN,
    FILLED_TP,
    STOPPED_SL,
    CLOSED_MANUAL
}

data class MarketTicker(
    val symbol: String,
    val price: Double,
    val change24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume: String,
    val lastUpdate: Long = System.currentTimeMillis()
)
