package com.ven.app.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ven.app.R
import com.ven.app.core.config.AppConfig

object GoogleAuthHelper {

    suspend fun getGoogleIdToken(
        context: Context,
        serverClientId: String = AppConfig.googleServerClientId
    ): Result<String> {
        if (serverClientId.isBlank()) {
            return Result.failure(Exception(context.getString(R.string.auth_google_error_not_configured)))
        }

        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Result.success(googleIdTokenCredential.idToken)
            } else {
                Result.failure(Exception(context.getString(R.string.auth_google_error_credentials)))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception(context.getString(R.string.auth_google_cancelled)))
        } catch (e: GetCredentialException) {
            Result.failure(Exception(e.localizedMessage ?: context.getString(R.string.auth_google_sign_in_failed)))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: context.getString(R.string.auth_google_generic_error)))
        }
    }
}
