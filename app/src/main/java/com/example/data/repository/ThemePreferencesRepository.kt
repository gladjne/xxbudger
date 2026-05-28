package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.BudgetThemeType
import com.example.ui.localization.AppLanguageSupported
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemePreferencesRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("app_theme")
    private val languageKey = stringPreferencesKey("app_language")
    private val currencyKey = stringPreferencesKey("app_currency")

    val themeTypeFlow: Flow<BudgetThemeType> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[themeKey] ?: BudgetThemeType.BENTO_NUIT.name
            try {
                BudgetThemeType.valueOf(themeName)
            } catch (e: Exception) {
                BudgetThemeType.BENTO_NUIT
            }
        }

    suspend fun saveThemeType(themeType: BudgetThemeType) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = themeType.name
        }
    }

    val languageFlow: Flow<AppLanguageSupported> = context.dataStore.data
        .map { preferences ->
            val langName = preferences[languageKey] ?: AppLanguageSupported.FRANCAIS.name
            try {
                AppLanguageSupported.valueOf(langName)
            } catch (e: Exception) {
                AppLanguageSupported.FRANCAIS
            }
        }

    suspend fun saveLanguage(language: AppLanguageSupported) {
        context.dataStore.edit { preferences ->
            preferences[languageKey] = language.name
        }
    }

    val currencyFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[currencyKey] ?: "€"
        }

    suspend fun saveCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[currencyKey] = currency
        }
    }
}
