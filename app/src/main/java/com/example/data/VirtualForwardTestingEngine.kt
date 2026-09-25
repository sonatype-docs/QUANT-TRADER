package com.example.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

object VirtualForwardTestingEngine {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    // Virtual Account State - Starts strictly at 0 Realized PnL & 0 Trades
    private val _virtualAccount = MutableStateFlow(
        VirtualAccount(
            accountId = "VA-FWD-00001",
            accountName = "Alpha Forward Test",
            initialBalance = 25000.0,
            currentBalance = 25000.0,
            realizedPnl = 0.0,
            activeOrdersCount = 0,
            totalTradesExecuted = 0,
            winTradesCount = 0,
            lossTradesCount = 0
        )
    )
    val virtualAccount: StateFlow<VirtualAccount> = _virtualAccount.asStateFlow()

    // Virtual Orders List - Starts completely empty (no placeholder/dummy data)
    private val _orders = MutableStateFlow<List<VirtualOrder>>(emptyList())
    val orders: StateFlow<List<VirtualOrder>> = _orders.asStateFlow()

    // Active Runners derived strictly from Virtual Orders
    private val _activeRunners = MutableStateFlow<List<Runner>>(emptyList())
    val activeRunners: StateFlow<List<Runner>> = _activeRunners.asStateFlow()

    // Live Tickers
    private val _tickers = MutableStateFlow<Map<String, MarketTicker>>(
        mapOf(
            "XAU/USD" to MarketTicker("XAU/USD", 2647.70, 0.85, 2658.40, 2634.10, "82,410 oz"),
            "BTC/USDT" to MarketTicker("BTC/USDT", 94820.0, 2.45, 95400.0, 92600.0, "34,120 BTC"),
            "BTC / ETH" to MarketTicker("BTC / ETH", 28.24, 0.45, 28.90, 27.80, "1,840 Pairs")
        )
    )
    val tickers: StateFlow<Map<String, MarketTicker>> = _tickers.asStateFlow()

    // Forward Testing Running Flag
    private val _isForwardTestingActive = MutableStateFlow(true)
    val isForwardTestingActive: StateFlow<Boolean> = _isForwardTestingActive.asStateFlow()

    // Real-time Event Feed
    private val _forwardTestFeed = MutableStateFlow<List<String>>(
        listOf(
            "[SYSTEM] Forward Testing Mode initialized. Reset to $0.00 PnL.",
            "[STATUS] Awaiting strategy deployment. Select a strategy & configure Stop Loss to begin."
        )
    )
    val forwardTestFeed: StateFlow<List<String>> = _forwardTestFeed.asStateFlow()

    init {
        syncActiveRunners(_orders.value)
        startForwardTestingLoop()
    }

    private fun syncActiveRunners(ordersList: List<VirtualOrder>) {
        _activeRunners.value = ordersList
            .filter { it.status == VirtualOrderStatus.OPEN }
            .map { order ->
                Runner(
                    id = order.id,
                    symbol = order.symbol,
                    baseAsset = when {
                        order.symbol.contains("XAU") -> "AU"
                        order.symbol.contains("BTC") -> "BTC"
                        order.symbol.contains("ETH") -> "ETH"
                        else -> "PAIR"
                    },
                    timeframe = "5m",
                    leverage = "FORWARD",
                    strategyName = order.strategy,
                    venue = "Virtual Broker",
                    status = RunnerStatus.IN_POSITION,
                    statusLabel = "FORWARD ACTIVE",
                    entryPrice = String.format("%,.2f", order.entryPrice),
                    currentMark = String.format("%,.2f", order.currentPrice),
                    unrealizedPnl = String.format("%+,.2f", order.unrealizedPnl),
                    unrealizedPnlPct = String.format("%+.2f%%", order.unrealizedPnlPct),
                    isPnlPositive = order.unrealizedPnl >= 0,
                    riskUsd = String.format("$%.2f", Math.abs(order.entryPrice - order.stopLoss) * order.qty),
                    stopLoss = String.format("%,.2f", order.stopLoss),
                    takeProfit = String.format("%,.2f", order.takeProfit),
                    holdTime = "Forward Testing"
                )
            }
    }

    private fun startForwardTestingLoop() {
        scope.launch {
            while (isActive) {
                if (_isForwardTestingActive.value) {
                    try {
                        val liveTickers = LiveMarketService.fetchLivePrices()
                        if (liveTickers.isNotEmpty()) {
                            _tickers.value = liveTickers
                            updateActiveOrdersWithLivePrices(liveTickers)
                        }
                    } catch (e: Exception) {
                        // Keep running smoothly
                    }
                }
                delay(2500) // update every 2.5s
            }
        }
    }

    private fun updateActiveOrdersWithLivePrices(liveTickers: Map<String, MarketTicker>) {
        val currentOrders = _orders.value.toMutableList()
        var balanceAdjustment = 0.0
        val updatedOrders = currentOrders.map { order ->
            if (order.status == VirtualOrderStatus.OPEN) {
                val ticker = liveTickers[order.symbol]
                if (ticker != null) {
                    val markPrice = ticker.price
                    val pnl = if (order.side == OrderSide.BUY) {
                        (markPrice - order.entryPrice) * order.qty
                    } else {
                        (order.entryPrice - markPrice) * order.qty
                    }
                    val pnlPct = if (order.entryPrice > 0) {
                        val directionalMove = if (order.side == OrderSide.BUY) markPrice - order.entryPrice else order.entryPrice - markPrice
                        (directionalMove / order.entryPrice) * 100.0
                    } else 0.0

                    // Check TP hit
                    if ((order.side == OrderSide.BUY && markPrice >= order.takeProfit) ||
                        (order.side == OrderSide.SELL && markPrice <= order.takeProfit)) {
                        addFeedLog("[TP HIT] ${order.symbol} hit Take Profit @ $${String.format("%.2f", markPrice)}! +$${String.format("%.2f", pnl)} realized.")
                        balanceAdjustment += pnl
                        order.copy(
                            currentPrice = markPrice,
                            unrealizedPnl = pnl,
                            unrealizedPnlPct = pnlPct,
                            status = VirtualOrderStatus.FILLED_TP
                        )
                    } else if ((order.side == OrderSide.BUY && markPrice <= order.stopLoss) ||
                        (order.side == OrderSide.SELL && markPrice >= order.stopLoss)) {
                        addFeedLog("[SL HIT] ${order.symbol} stopped out @ $${String.format("%.2f", markPrice)}! $${String.format("%.2f", pnl)} realized.")
                        balanceAdjustment += pnl
                        order.copy(
                            currentPrice = markPrice,
                            unrealizedPnl = pnl,
                            unrealizedPnlPct = pnlPct,
                            status = VirtualOrderStatus.STOPPED_SL
                        )
                    } else {
                        order.copy(
                            currentPrice = markPrice,
                            unrealizedPnl = pnl,
                            unrealizedPnlPct = pnlPct
                        )
                    }
                } else order
            } else order
        }

        _orders.value = updatedOrders
        syncActiveRunners(updatedOrders)
        $equityLine
        if (balanceAdjustment != 0.0) {
            val acc = _virtualAccount.value
            _virtualAccount.value = acc.copy(
                currentBalance = acc.currentBalance + balanceAdjustment,
                realizedPnl = acc.realizedPnl + balanceAdjustment,
                unrealizedPnl = 0.0,
                totalTradesExecuted = acc.totalTradesExecuted + 1,
                winTradesCount = if (balanceAdjustment > 0) acc.winTradesCount + 1 else acc.winTradesCount,
                lossTradesCount = if (balanceAdjustment < 0) acc.lossTradesCount + 1 else acc.lossTradesCount,
                activeOrdersCount = updatedOrders.count { it.status == VirtualOrderStatus.OPEN }
            )
        }
    }

    fun setVirtualCustomBalance(newBalance: Double) {
        val acc = _virtualAccount.value
        _virtualAccount.value = acc.copy(
            initialBalance = newBalance,
            currentBalance = newBalance + acc.realizedPnl
        )
        addFeedLog("[ACCOUNT] Virtual forward testing balance set to $${String.format("%,.2f", newBalance)}.")
    }

    fun toggleForwardTesting(active: Boolean) {
        _isForwardTestingActive.value = active
        addFeedLog(if (active) "[FORWARD TEST] Resumed real-market forward testing." else "[FORWARD TEST] Paused forward testing.")
    }

    fun placeVirtualForwardOrder(
        runnerId: String,
        symbol: String,
        strategy: String,
        side: OrderSide,
        qty: Double,
        stopLoss: Double,
        takeProfit: Double,
        customParametersSummary: String? = null
    ) {
        val ticker = _tickers.value[symbol]
        val currentPrice = ticker?.price ?: if (symbol.contains("XAU")) 2647.70 else if (symbol.contains("BTC")) 94820.0 else 28.24

        val newOrder = VirtualOrder(
            id = "ORD-QK-${UUID.randomUUID().toString().take(6).uppercase()}",
            runnerId = runnerId,
            symbol = symbol,
            strategy = strategy,
            side = side,
            qty = qty,
            entryPrice = currentPrice,
            currentPrice = currentPrice,
            stopLoss = stopLoss,
            takeProfit = takeProfit,
            unrealizedPnl = 0.0,
            unrealizedPnlPct = 0.0,
            status = VirtualOrderStatus.OPEN
        )

        val updated = listOf(newOrder) + _orders.value
        _orders.value = updated
        syncActiveRunners(updated)

        val acc = _virtualAccount.value
        _virtualAccount.value = acc.copy(
            activeOrdersCount = updated.count { it.status == VirtualOrderStatus.OPEN }
        )

        addFeedLog("[SYNTH-ORDER] Placed ${side.name} ${qty}x $symbol ($strategy) @ $${String.format("%.2f", currentPrice)} SL: $${String.format("%.2f", stopLoss)} TP: $${String.format("%.2f", takeProfit)}")
        if (!customParametersSummary.isNullOrBlank()) {
            addFeedLog("[PARAMS] $customParametersSummary")
        }
    }

    fun updateOrderStopLoss(orderIdOrRunnerId: String, newStopLoss: Double) {
        val currentOrders = _orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderIdOrRunnerId || it.runnerId == orderIdOrRunnerId }
        if (index != -1) {
            val order = currentOrders[index]
            currentOrders[index] = order.copy(stopLoss = newStopLoss)
            _orders.value = currentOrders
            syncActiveRunners(currentOrders)
            addFeedLog("[SL ADJUST] ${order.symbol} stop loss updated to $${String.format("%.2f", newStopLoss)}")
        }
    }

    fun findOrder(orderIdOrRunnerId: String): VirtualOrder? {
        return _orders.value.firstOrNull { it.id == orderIdOrRunnerId || it.runnerId == orderIdOrRunnerId }
    }

    fun closeVirtualOrder(orderId: String) {
        val currentOrders = _orders.value.toMutableList()
        val index = currentOrders.indexOfFirst { it.id == orderId || it.runnerId == orderId }
        if (index != -1) {
            val order = currentOrders[index]
            if (order.status == VirtualOrderStatus.OPEN) {
                val pnl = order.unrealizedPnl
                currentOrders[index] = order.copy(status = VirtualOrderStatus.CLOSED_MANUAL)
                _orders.value = currentOrders
                syncActiveRunners(currentOrders)

                val acc = _virtualAccount.value
                _virtualAccount.value = acc.copy(
                    currentBalance = acc.currentBalance + pnl,
                    realizedPnl = acc.realizedPnl + pnl,
                    unrealizedPnl = 0.0,
                    totalTradesExecuted = acc.totalTradesExecuted + 1,
                    winTradesCount = if (pnl >= 0) acc.winTradesCount + 1 else acc.winTradesCount,
                    lossTradesCount = if (pnl < 0) acc.lossTradesCount + 1 else acc.lossTradesCount,
                    activeOrdersCount = currentOrders.count { it.status == VirtualOrderStatus.OPEN }
                )
                addFeedLog("[MANUAL CLOSE] Virtual position #${order.id} (${order.symbol}) flattened at $${String.format("%.2f", order.currentPrice)} (PnL: $${String.format("%+.2f", pnl)})")
            }
        }
    }

    fun flattenAllVirtualOrders() {
        val currentOrders = _orders.value.toMutableList()
        var totalClosedPnl = 0.0
        var closedCount = 0

        for (i in currentOrders.indices) {
            val ord = currentOrders[i]
            if (ord.status == VirtualOrderStatus.OPEN) {
                totalClosedPnl += ord.unrealizedPnl
                currentOrders[i] = ord.copy(status = VirtualOrderStatus.CLOSED_MANUAL)
                closedCount++
            }
        }

        _orders.value = currentOrders
        syncActiveRunners(currentOrders)
        val acc = _virtualAccount.value
        _virtualAccount.value = acc.copy(
            currentBalance = acc.currentBalance + totalClosedPnl,
            realizedPnl = acc.realizedPnl + totalClosedPnl,
            unrealizedPnl = 0.0,
            totalTradesExecuted = acc.totalTradesExecuted + closedCount,
            activeOrdersCount = 0
        )
        addFeedLog("[MASS FLATTEN] Closed all $closedCount virtual forward orders! Net PnL: $${String.format("%+.2f", totalClosedPnl)}")
    }

    fun resetVirtualAccount(initialBalance: Double = 25000.0) {
        _virtualAccount.value = VirtualAccount(
            accountId = "VA-FWD-${System.currentTimeMillis().toString().takeLast(5)}",
            accountName = "Alpha Forward Test #1",
            initialBalance = initialBalance,
            currentBalance = initialBalance,
            realizedPnl = 0.0,
            activeOrdersCount = 0,
            totalTradesExecuted = 0,
            winTradesCount = 0,
            lossTradesCount = 0
        )
        _orders.value = emptyList()
        syncActiveRunners(emptyList())
        addFeedLog("[RESET] Virtual account reset to fresh balance $${String.format("%,.2f", initialBalance)}.")
    }

    private fun addFeedLog(msg: String) {
        val list = listOf(msg) + _forwardTestFeed.value.take(20)
        _forwardTestFeed.value = list
    }
}
