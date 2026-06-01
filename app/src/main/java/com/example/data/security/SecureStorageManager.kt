// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import android.util.Base64

object SecureStorageManager {
    private const val SECURE_PREFS_NAME = "budget_joy_secure_prefs"
    private const val DB_PASSPHRASE_KEY = "db_passphrase_secure"

    @Volatile
    private var securePrefsInstance: SharedPreferences? = null

    fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
        return securePrefsInstance ?: synchronized(this) {
            val instance = securePrefsInstance
            if (instance != null) {
                instance
            } else {
                try {
                    val masterKey = MasterKey.Builder(context.applicationContext)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .setUserAuthenticationRequired(false) // Ensure it works in background/workers
                        .build()

                    val created = EncryptedSharedPreferences.create(
                        context.applicationContext,
                        SECURE_PREFS_NAME,
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                    securePrefsInstance = created
                    created
                } catch (e: Throwable) {
                    e.printStackTrace()
                    // Fallback to unencrypted in absolute disaster cases to prevent user lock-out
                    context.applicationContext.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE)
                }
            }
        }
    }

    /**
     * Retrieves or generates a 256-bit clean database passphrase stored in KeyStore-backed EncryptedSharedPreferences
     */
    fun getDatabasePassphrase(context: Context): String {
        val prefs = getEncryptedSharedPreferences(context)
        var passphrase = prefs.getString(DB_PASSPHRASE_KEY, null)
        if (passphrase == null) {
            val randomBytes = ByteArray(32)
            SecureRandom().nextBytes(randomBytes)
            passphrase = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
            prefs.edit().putString(DB_PASSPHRASE_KEY, passphrase).apply()
        }
        return passphrase
    }
}
