package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.components.QuantKitBottomNav
import com.example.ui.components.QuantKitHeader
import com.example.ui.components.QuantKitTab
import com.example.ui.screens.BotDetailScreen
import com.example.ui.screens.ComparatorLabScreen
import com.example.ui.screens.FleetDashboardScreen
import com.example.ui.screens.ForwardTestingScreen
import com.example.ui.screens.StrategyResearchScreen
import com.example.ui.screens.QuantResearchScreen
import com.example.ui.screens.WatchdogRiskScreen
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                QuantKitApp(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun QuantKitApp(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val colors = LocalQuantKitColors.current
    var selectedTab by remember { mutableStateOf(QuantKitTab.BOTS) }
    var activeRunnerId by remember { mutableStateOf<String?>("QK-99214-GOLD") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            QuantKitHeader(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                onBalanceClick = { selectedTab = QuantKitTab.FORWARD }
            )
        },
        bottomBar = {
            QuantKitBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    // If switching to BOTS from another tab, keep detail state if relevant or show list
                }
            )
        },
        containerColor = colors.canvas
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.canvas)
                .padding(innerPadding)
        ) {
            Crossfade(targetState = selectedTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    QuantKitTab.BOTS -> {
                        if (activeRunnerId != null) {
                            BotDetailScreen(
                                runnerId = activeRunnerId,
                                onBack = { activeRunnerId = null },
                                onToggleTheme = onToggleTheme
                            )
                        } else {
                            FleetDashboardScreen(
                                onSelectRunner = { runnerId ->
                                    activeRunnerId = runnerId
                                }
                            )
                        }
                    }
                    QuantKitTab.FORWARD -> {
                        ForwardTestingScreen(
                            onToggleTheme = onToggleTheme
                        )
                    }
                    QuantKitTab.LAB -> {
                        ComparatorLabScreen(
                            onToggleTheme = onToggleTheme
                        )
                    }
                    QuantKitTab.STRATEGY -> {
                        StrategyResearchScreen(
                            isResearchMode = false,
                            onToggleTheme = onToggleTheme
                        )
                    }
                    QuantKitTab.WATCHDOG -> {
                        WatchdogRiskScreen(
                            onToggleTheme = onToggleTheme
                        )
                    }
                    QuantKitTab.RESEARCH -> {
                        QuantResearchScreen(
                            onToggleTheme = onToggleTheme
                        )
                    }
                }
            }
        }
    }
}
