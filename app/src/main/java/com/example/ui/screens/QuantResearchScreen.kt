package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuantEngineClient
import com.example.data.QuantEngineHealth
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.PnlNegative
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.SunsetOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuantResearchScreen(
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current
    val scope = rememberCoroutineScope()
    var health by remember { mutableStateOf(QuantEngineHealth(false, message = "Checking backend...")) }

    suspend fun refresh() {
        health = QuantEngineClient.health()
    }

    LaunchedEffect(Unit) {
        refresh()
        while (true) {
            delay(15_000)
            refresh()
        }
    }

    Column(
        modifier = modifier.fillMaxSize().background(colors.canvas).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, null, tint = SunsetOrange)
                Column {
                    Text("Quant Research Engine", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Server-side backtesting • reproducible research", color = colors.textSecondary, fontSize = 11.sp)
                }
            }
            Button(
                onClick = onToggleTheme,
                colors = ButtonDefaults.buttonColors(containerColor = colors.container)
            ) { Text("THEME", color = colors.textPrimary, fontSize = 10.sp) }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                .background(colors.surface, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (health.online) Icons.Default.CloudDone else Icons.Default.Warning,
                    null,
                    tint = if (health.online) PnlPositive else PnlNegative
                )
                Text(
                    if (health.online) "QUANT BACKEND ONLINE" else "QUANT BACKEND OFFLINE",
                    color = if (health.online) PnlPositive else PnlNegative,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text("Service: " + health.service, color = colors.textPrimary, fontSize = 12.sp)
            Text(health.message, color = colors.textSecondary, fontSize = 11.sp)
            Button(
                onClick = { scope.launch { refresh() } },
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
            ) {
                Icon(Icons.Default.Refresh, null, tint = Color.White)
                Spacer(Modifier.height(0.dp))
                Text("  CHECK BACKEND", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                .background(colors.surface, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("RESEARCH SOURCE OF TRUTH", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text("Backtest metrics are no longer intended to be calculated from UI sliders or hard-coded catalogue values.", color = colors.textSecondary, fontSize = 12.sp)
            Text("Pipeline: market data → strategy engine → realistic costs/slippage → metrics → run ID → Android dashboard.", color = colors.textSecondary, fontSize = 12.sp)
            Text("Current API: POST /v1/research/backtests • GET /v1/research/backtests/{run_id}", color = SunsetOrange, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}
