package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CandlestickChart
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.SunsetOrange

enum class QuantKitTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    BOTS("Bots", Icons.Filled.SmartToy, Icons.Outlined.SmartToy),
    FORWARD("Forward", Icons.Filled.CandlestickChart, Icons.Outlined.CandlestickChart),
    LAB("Lab", Icons.Filled.Science, Icons.Outlined.Science),
    STRATEGY("Strategy", Icons.Filled.Tune, Icons.Outlined.Tune),
    WATCHDOG("Watchdog", Icons.Filled.Radar, Icons.Outlined.Radar)
}

@Composable
fun QuantKitBottomNav(
    selectedTab: QuantKitTab,
    onTabSelected: (QuantKitTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface.copy(alpha = 0.95f))
            .border(width = 1.dp, color = colors.borderSubtle)
            .navigationBarsPadding()
            .height(64.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuantKitTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            val icon = if (isSelected) tab.selectedIcon else tab.unselectedIcon
            val tint = if (isSelected) SunsetOrange else colors.textMuted

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("nav_tab_${tab.name.lowercase()}")
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = tab.title,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = tab.title,
                    color = tint,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    letterSpacing = (-0.2).sp
                )
            }
        }
    }
}
