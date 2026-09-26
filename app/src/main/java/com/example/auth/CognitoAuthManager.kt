package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.BuildConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import kotlin.coroutines.resume

class CognitoAuthManager(context: Context) {
    companion object {
        const val REQUEST_CODE = 9101
        private const val PREFS = "quant_cognito_auth"
        private const val STATE = "auth_state"
        private const val REDIRECT_URI = "com.aistudio.quantkit.fx8w:/oauth2redirect"
    }

    private val appContext = context.applicationContext
    private val prefs by lazy {
        val key = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            PREFS,
            key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private var authState: AuthState = loadState()
    private var service: AuthorizationService? = null

    fun isConfigured(): Boolean =
        BuildConfig.COGNITO_ISSUER.isNotBlank() && BuildConfig.COGNITO_APP_CLIENT_ID.isNotBlank()

    fun isAuthorized(): Boolean = authState.isAuthorized

    fun startLogin(activity: Activity, onError: (String) -> Unit) {
        if (!isConfigured()) {
            onError("Cognito is not configured in this build")
            return
        }
        AuthorizationServiceConfiguration.fetchFromIssuer(
            Uri.parse(BuildConfig.COGNITO_ISSUER)
        ) { configuration, exception ->
            if (configuration == null) {
                onError(exception?.errorDescription ?: "Unable to load Cognito discovery")
                return@fetchFromIssuer
            }
            val request = AuthorizationRequest.Builder(
                configuration,
                BuildConfig.COGNITO_APP_CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(REDIRECT_URI)
            )
                .setScopes("openid", "email")
                .build()
            service = AuthorizationService(activity)
            activity.startActivityForResult(
                service!!.getAuthorizationRequestIntent(request),
                REQUEST_CODE
            )
        }
    }

    fun handleAuthorizationResult(data: Intent?): String? {
        val response = AuthorizationResponse.fromIntent(data ?: return "Missing authorization response")
        val exception = AuthorizationException.fromIntent(data)
        if (response == null) {
            return exception?.errorDescription ?: "Authorization failed"
        }
        val tokenRequest = response.createTokenExchangeRequest()
        service = service ?: AuthorizationService(appContext)
        service!!.performTokenRequest(tokenRequest) { tokenResponse, tokenException ->
            authState.update(tokenResponse, tokenException)
            saveState()
        }
        return null
    }

    suspend fun accessToken(): String? = suspendCancellableCoroutine { continuation ->
        if (!authState.isAuthorized) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        val authService = service ?: AuthorizationService(appContext).also { service = it }
        authState.performActionWithFreshTokens(authService) { accessToken, _, exception ->
            saveState()
            if (exception != null || accessToken.isNullOrBlank()) {
                continuation.resume(null)
            } else {
                continuation.resume(accessToken)
            }
        }
    }

    fun logout() {
        authState = AuthState()
        saveState()
    }

    private fun loadState(): AuthState {
        val json = prefs.getString(STATE, null) ?: return AuthState()
        return try {
            AuthState.jsonDeserialize(json)
        } catch (_: Exception) {
            AuthState()
        }
    }

    private fun saveState() {
        prefs.edit().putString(STATE, authState.jsonSerializeString()).apply()
    }
}
