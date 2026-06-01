// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.security.SecureStorageManager
import com.example.ui.theme.BudgetThemeType
import com.example.ui.localization.AppLanguageSupported
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map

class ThemePreferencesRepository(private val context: Context) {
    private val securePrefs: SharedPreferences by lazy {
        SecureStorageManager.getEncryptedSharedPreferences(context)
    }

    private val themeKey = "app_theme"
    private val languageKey = "app_language"
    private val currencyKey = "app_currency"

    private fun SharedPreferences.observeKey(key: String, defaultValue: String): Flow<String> = callbackFlow {
        // Emit the current state first
        trySend(getString(key, defaultValue) ?: defaultValue)

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                trySend(getString(key, defaultValue) ?: defaultValue)
            }
        }
        registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val themeTypeFlow: Flow<BudgetThemeType> = securePrefs.observeKey(themeKey, BudgetThemeType.BENTO_NUIT.name)
        .map { themeName ->
            try {
                BudgetThemeType.valueOf(themeName)
            } catch (e: Exception) {
                BudgetThemeType.BENTO_NUIT
            }
        }

    suspend fun saveThemeType(themeType: BudgetThemeType) {
        securePrefs.edit().putString(themeKey, themeType.name).apply()
    }

    val languageFlow: Flow<AppLanguageSupported> = securePrefs.observeKey(languageKey, AppLanguageSupported.FRANCAIS.name)
        .map { langName ->
            try {
                AppLanguageSupported.valueOf(langName)
            } catch (e: Exception) {
                AppLanguageSupported.FRANCAIS
            }
        }

    suspend fun saveLanguage(language: AppLanguageSupported) {
        securePrefs.edit().putString(languageKey, language.name).apply()
    }

    val currencyFlow: Flow<String> = securePrefs.observeKey(currencyKey, "€")

    suspend fun saveCurrency(currency: String) {
        securePrefs.edit().putString(currencyKey, currency).apply()
    }
}
