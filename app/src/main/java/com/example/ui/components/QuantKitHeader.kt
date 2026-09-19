package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.VirtualForwardTestingEngine
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.SunsetOrange
import com.example.ui.theme.SunsetOrangeDark

@Composable
fun QuantKitHeader(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onBalanceClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current
    val virtualAccount by VirtualForwardTestingEngine.virtualAccount.collectAsState()

    Column(modifier = modifier.fillMaxWidth()) {
        // Dedicated persistent black status bar scrim so status bar is always pitch black
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
                .statusBarsPadding()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(colors.surface.copy(alpha = 0.95f))
                .border(width = 1.dp, color = colors.borderSubtle)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Brand & Status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Brand Logo Box with Active Indicator
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(SunsetOrange, SunsetOrangeDark)
                            )
                        )
                        .border(1.dp, SunsetOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "QuantKit Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Live green dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(PnlPositive)
                        .border(2.dp, colors.surface, CircleShape)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "QUANTKIT",
                        color = colors.textPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SunsetOrange.copy(alpha = 0.15f))
                            .border(1.dp, SunsetOrange.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            color = SunsetOrange,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SharkEx",
                        color = colors.textMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "•",
                        color = colors.border,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "8ms",
                        color = PnlPositive,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Right Controls: Balance + Theme Toggle + Avatar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .then(
                        if (onBalanceClick != null) Modifier.clickable { onBalanceClick() }
                        else Modifier
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "V-BALANCE",
                    color = colors.textMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$${String.format("%,.2f", virtualAccount.totalEquity)}",
                    color = if (virtualAccount.realizedPnl >= 0) PnlPositive else Color(0xFFEF4444),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Theme Switcher Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.container)
                    .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                    .clickable { onToggleTheme() }
                    .testTag("theme_toggle_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Theme",
                    tint = SunsetOrange,
                    modifier = Modifier.size(18.dp)
                )
            }

            // QK Avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SunsetOrange, Color(0xFFF59E0B))
                        )
                    )
                    .border(1.dp, SunsetOrange.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "QK",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
}
