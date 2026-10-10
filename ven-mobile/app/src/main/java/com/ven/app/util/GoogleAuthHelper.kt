package com.ven.app.util

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.ven.app.core.config.AppConfig

object GoogleAuthHelper {

    suspend fun getGoogleIdToken(
        context: Context,
        serverClientId: String = AppConfig.googleServerClientId
    ): Result<String> {
        if (serverClientId.isBlank()) {
            return Result.failure(Exception("Google Client ID belum dikonfigurasi di file .env"))
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
                Result.failure(Exception("Kredensial Google tidak dikenali"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Login Google dibatalkan"))
        } catch (e: GetCredentialException) {
            Result.failure(Exception(e.localizedMessage ?: "Gagal autentikasi Google"))
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Terjadi kesalahan pada Google Sign-In"))
        }
    }
}
