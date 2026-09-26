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
import androidx.compose.material.icons.filled.Lock
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
import com.example.auth.CognitoAuthManager
import com.example.data.QuantEngineClient
import com.example.data.QuantEngineHealth
import com.example.data.ResearchJobStatus
import com.example.ui.theme.LocalQuantKitColors
import com.example.ui.theme.PnlNegative
import com.example.ui.theme.PnlPositive
import com.example.ui.theme.SunsetOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuantResearchScreen(
    onToggleTheme: () -> Unit,
    auth: CognitoAuthManager,
    authVersion: Int,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuantKitColors.current
    val scope = rememberCoroutineScope()
    var health by remember { mutableStateOf(QuantEngineHealth(false, message = "Checking backend...")) }
    var jobId by remember { mutableStateOf<String?>(null) }
    var job by remember { mutableStateOf<ResearchJobStatus?>(null) }
    var result by remember { mutableStateOf<org.json.JSONObject?>(null) }
    var actionMessage by remember { mutableStateOf("") }

    suspend fun refresh() { health = QuantEngineClient.health() }

    LaunchedEffect(Unit) {
        refresh()
        while (true) { delay(15_000); refresh() }
    }

    LaunchedEffect(jobId) {
        val id = jobId ?: return@LaunchedEffect
        while (true) {
            QuantEngineClient.getJob(auth, id).onSuccess { job = it }.onFailure { actionMessage = it.message ?: "Job status failed" }
            if (job?.status == "SUCCEEDED") {\n                QuantEngineClient.getJobResult(auth, id).onSuccess { result = it }\n                    .onFailure { actionMessage = it.message ?: "Result retrieval failed" }\n                break\n            }\n            if (job?.status == "FAILED") break
            delay(2_000)
        }
    }

    Column(modifier.fillMaxSize().background(colors.canvas).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Science, null, tint = SunsetOrange)
                Column {
                    Text("Quant Research Engine", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Authenticated asynchronous research", color = colors.textSecondary, fontSize = 11.sp)
                }
            }
            Button(onClick = onToggleTheme, colors = ButtonDefaults.buttonColors(containerColor = colors.container)) {
                Text("THEME", color = colors.textPrimary, fontSize = 10.sp)
            }
        }

        Column(Modifier.fillMaxWidth().border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp)).background(colors.surface, RoundedCornerShape(14.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (health.online) Icons.Default.CloudDone else Icons.Default.Warning, null, tint = if (health.online) PnlPositive else PnlNegative)
                Text(if (health.online) "QUANT BACKEND ONLINE" else "QUANT BACKEND OFFLINE", color = if (health.online) PnlPositive else PnlNegative, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Text("Service: " + health.service, color = colors.textPrimary, fontSize = 12.sp)
            Text(health.message, color = colors.textSecondary, fontSize = 11.sp)
            Button(onClick = { scope.launch { refresh() } }, colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)) {
                Icon(Icons.Default.Refresh, null, tint = Color.White)
                Spacer(Modifier.height(0.dp))
                Text("  CHECK BACKEND", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(Modifier.fillMaxWidth().border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp)).background(colors.surface, RoundedCornerShape(14.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, null, tint = SunsetOrange)
                Text("RESEARCH AUTHENTICATION", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Text(
                if (auth.isAuthorized()) "Signed in — Cognito access token is available."
                else if (auth.isConfigured()) "Not signed in — research jobs require a Cognito session."
                else "Cognito is not configured for this build. Set COGNITO_ISSUER and COGNITO_APP_CLIENT_ID.",
                color = colors.textSecondary, fontSize = 11.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (auth.isAuthorized()) {
                    Button(onClick = { onLogout(); jobId = null; job = null; result = null }, colors = ButtonDefaults.buttonColors(containerColor = colors.container)) {
                        Text("SIGN OUT", color = colors.textPrimary, fontSize = 10.sp)
                    }
                    Button(onClick = {
                        scope.launch {
                            actionMessage = "Submitting asynchronous backtest..."
                            QuantEngineClient.createSampleBacktest(auth)
                                .onSuccess { id -> jobId = id; job = null; result = null; actionMessage = "Queued " + id }
                                .onFailure { actionMessage = it.message ?: "Submission failed" }
                        }
                    }, colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)) {
                        Text("RUN SAMPLE BACKTEST", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(onClick = onLogin, colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange), enabled = auth.isConfigured()) {
                        Text("SIGN IN WITH COGNITO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Column(Modifier.fillMaxWidth().border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp)).background(colors.surface, RoundedCornerShape(14.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ASYNC RESEARCH JOB", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(job?.let { "Job " + it.jobId + ": " + it.status } ?: "No active job", color = colors.textPrimary, fontSize = 12.sp)
            if (!job?.resultRunId.isNullOrBlank()) Text("Result run: " + job?.resultRunId, color = PnlPositive, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            if (!job?.error.isNullOrBlank()) Text("Error: " + job?.error, color = PnlNegative, fontSize = 11.sp)
            if (actionMessage.isNotBlank()) Text(actionMessage, color = colors.textSecondary, fontSize = 11.sp)
            Text("Pipeline: Android → Cognito → API → DynamoDB → SQS → ECS worker → S3 result.", color = colors.textSecondary, fontSize = 11.sp)
            result?.let { json ->
                val metrics = json.optJSONObject("metrics")
                Text("RESULT READY • " + json.optString("strategy_id"), color = PnlPositive, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Return " + "%.2f".format(metrics?.optDouble("total_return_pct", 0.0)) +
                        "% • Sharpe " + "%.2f".format(metrics?.optDouble("sharpe", 0.0)) +
                        " • Max DD " + "%.2f".format(metrics?.optDouble("max_drawdown_pct", 0.0)) + "%",
                    color = colors.textPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace
                )
                Text("Trades: " + (metrics?.optInt("trade_count", 0) ?: 0) + " • Run: " + json.optString("run_id"), color = colors.textSecondary, fontSize = 10.sp)
            }
        }
    }
}
