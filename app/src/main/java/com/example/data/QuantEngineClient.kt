package com.example.data

import com.example.BuildConfig
import com.example.auth.CognitoAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.TimeUnit

data class QuantEngineHealth(val online: Boolean, val service: String = "quant-engine", val message: String)
data class ResearchJobStatus(val jobId: String, val status: String, val resultRunId: String?, val error: String?)

object QuantEngineClient {
    private val client = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build()

    suspend fun health(): QuantEngineHealth = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(BuildConfig.QUANT_ENGINE_BASE_URL.trimEnd('/') + "/health").get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@withContext QuantEngineHealth(false, message = "HTTP " + response.code)
                val json = JSONObject(body)
                QuantEngineHealth(json.optString("status") == "ok", json.optString("service", "quant-engine"), "Backend connected")
            }
        } catch (e: Exception) {
            QuantEngineHealth(false, message = e.message ?: "Backend unavailable")
        }
    }

    suspend fun createSampleBacktest(auth: CognitoAuthManager): Result<String> = withContext(Dispatchers.IO) {
        val token = auth.accessToken() ?: return@withContext Result.failure(IllegalStateException("Sign in required"))
        try {
            val body = JSONObject().put("symbol", "BTCUSDT").put("strategy_id", "STRAT-02-TURTLE-DONCHIAN")
                .put("initial_capital", 25000.0).put("risk_per_trade", 0.01).put("fee_bps", 4.0)
                .put("slippage_bps", 1.0).put("bars", sampleBars()).toString()
            val request = Request.Builder()
                .url(BuildConfig.QUANT_ENGINE_BASE_URL.trimEnd('/') + "/v1/research/jobs")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Content-Type", "application/json")
                .post(okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"), body))
                .build()
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP " + response.code + ": " + responseBody))
                Result.success(JSONObject(responseBody).getString("job_id"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJob(auth: CognitoAuthManager, jobId: String): Result<ResearchJobStatus> = withContext(Dispatchers.IO) {
        val token = auth.accessToken() ?: return@withContext Result.failure(IllegalStateException("Sign in required"))
        try {
            val request = Request.Builder()
                .url(BuildConfig.QUANT_ENGINE_BASE_URL.trimEnd('/') + "/v1/research/jobs/" + jobId)
                .addHeader("Authorization", "Bearer " + token).get().build()
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@withContext Result.failure(IllegalStateException("HTTP " + response.code + ": " + responseBody))
                val json = JSONObject(responseBody)
                Result.success(ResearchJobStatus(
                    json.getString("job_id"), json.getString("status"),
                    json.optString("result_run_id").takeIf { it.isNotBlank() },
                    json.optString("error").takeIf { it.isNotBlank() }
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sampleBars(): JSONArray {
        val bars = JSONArray()
        var price = 100.0
        val start = Instant.parse("2026-01-01T00:00:00Z")
        for (i in 0 until 48) {
            price += if (i < 22) 0.35 else if (i < 34) -0.10 else 0.45
            val open = price - 0.15
            bars.put(JSONObject().put("timestamp", start.plusSeconds(i.toLong() * 3600).toString())
                .put("open", open).put("high", price + 0.30).put("low", open - 0.20)
                .put("close", price).put("volume", 1000.0 + i * 10))
        }
        return bars
    }
}
