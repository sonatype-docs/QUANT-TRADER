package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class QuantEngineHealth(
    val online: Boolean,
    val service: String = "quant-engine",
    val message: String
)

object QuantEngineClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .build()

    suspend fun health(): QuantEngineHealth = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(BuildConfig.QUANT_ENGINE_BASE_URL.trimEnd('/') + "/health")
                .get()
                .build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return@withContext QuantEngineHealth(false, message = "HTTP " + response.code)
                }
                val json = JSONObject(body)
                QuantEngineHealth(
                    online = json.optString("status") == "ok",
                    service = json.optString("service", "quant-engine"),
                    message = "Backend connected"
                )
            }
        } catch (e: Exception) {
            QuantEngineHealth(false, message = e.message ?: "Backend unavailable")
        }
    }
}
