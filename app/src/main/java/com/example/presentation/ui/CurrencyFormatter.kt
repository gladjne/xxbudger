// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.presentation.ui

import androidx.compose.runtime.staticCompositionLocalOf
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

class CurrencyFormatter(val currencySymbol: String, val language: com.example.ui.localization.AppLanguageSupported) {

    fun format(amount: Double): String {
        val locale = getLocaleForLanguage(language)
        val format = NumberFormat.getCurrencyInstance(locale)
        val code = getCurrencyCodeFromSymbol(currencySymbol)
        try {
            val currency = Currency.getInstance(code)
            format.currency = currency
        } catch (e: Throwable) {
            // Fallback
        }
        
        if (code == "JPY" || code == "XOF" || code == "XAF") {
            format.maximumFractionDigits = 0
            format.minimumFractionDigits = 0
        } else {
            format.maximumFractionDigits = 2
            format.minimumFractionDigits = 2
        }

        return format.format(amount)
    }

    private fun getLocaleForLanguage(language: com.example.ui.localization.AppLanguageSupported): Locale {
        return when (language) {
            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> Locale.FRANCE
            com.example.ui.localization.AppLanguageSupported.ENGLISH -> Locale.US
            com.example.ui.localization.AppLanguageSupported.ESPANOL -> Locale("es", "ES")
            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> Locale.GERMANY
            com.example.ui.localization.AppLanguageSupported.ITALIANO -> Locale.ITALY
            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> Locale("pt", "PT")
            com.example.ui.localization.AppLanguageSupported.CHINESE -> Locale.CHINA
            com.example.ui.localization.AppLanguageSupported.JAPANESE -> Locale.JAPAN
            com.example.ui.localization.AppLanguageSupported.ARABIC -> Locale("ar", "SA")
            com.example.ui.localization.AppLanguageSupported.RUSSIAN -> Locale("ru", "RU")
            com.example.ui.localization.AppLanguageSupported.KOREAN -> Locale.KOREA
        }
    }

    private fun getCurrencyCodeFromSymbol(symbol: String): String {
        return when (symbol) {
            "€" -> "EUR"
            "$" -> "USD"
            "£" -> "GBP"
            "¥" -> "JPY"
            "₹" -> "INR"
            "CFA", "FCFA" -> "XOF"
            "₦" -> "NGN"
            "R" -> "ZAR"
            "C$", "CA$" -> "CAD"
            "A$" -> "AUD"
            else -> {
                if (symbol.length == 3 && symbol.all { it.isLetter() }) symbol else "EUR"
            }
        }
    }
}

val LocalCurrencyFormatter = staticCompositionLocalOf<CurrencyFormatter> {
    CurrencyFormatter("€", com.example.ui.localization.AppLanguageSupported.FRANCAIS)
}
