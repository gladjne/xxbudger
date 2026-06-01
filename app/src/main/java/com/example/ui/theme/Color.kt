// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Let's specify the theme preferences enum
enum class BudgetThemeType {
    BENTO_NUIT,
    OCEAN_BLUE,
    LAVENDER_SOFT,
    EMERALD_FINANCE,
    LIGHT_STUDENT,
    SOFT_LIGHT,
    OCEAN_CALM,
    GREEN_FOCUS,
    LAVENDER_CLEAN,
    LIGHT_PRO
}

data class AppThemeColors(
    val type: BudgetThemeType,
    val name: String,
    val description: String,
    val isLight: Boolean,
    val primaryBlue: Color,
    val primaryBlueLight: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val borderColor: Color,
    val colorIncomeBg: Color,
    val colorIncomeBorder: Color,
    val colorIncomeIconBg: Color,
    val colorIncome: Color,
    val colorExpenseBg: Color,
    val colorExpenseBorder: Color,
    val colorExpenseIconBg: Color,
    val colorExpense: Color,
    val colorSavingBg: Color,
    val colorSavingBorder: Color,
    val colorSavingIconBg: Color,
    val colorSaving: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textWhite: Color
)

object ThemeManager {
    val bentoNuit = AppThemeColors(
        type = BudgetThemeType.BENTO_NUIT,
        name = "Bento Nuit",
        description = "Sombre & Premium",
        isLight = false,
        primaryBlue = Color(0xFFD0BCFF),
        primaryBlueLight = Color(0xFFE8DDFF),
        background = Color(0xFF141517),
        surface = Color(0xFF1F2023),
        surfaceVariant = Color(0xFF2B2C30),
        borderColor = Color(0xFF33353B),
        colorIncomeBg = Color(0xFF18221B),
        colorIncomeBorder = Color(0xFF223628),
        colorIncomeIconBg = Color(0xFF284131),
        colorIncome = Color(0xFF8CE09D),
        colorExpenseBg = Color(0xFF271B1B),
        colorExpenseBorder = Color(0xFF402727),
        colorExpenseIconBg = Color(0xFF4E2E2E),
        colorExpense = Color(0xFFF19E9E),
        colorSavingBg = Color(0xFF171B26),
        colorSavingBorder = Color(0xFF252D40),
        colorSavingIconBg = Color(0xFF2C3953),
        colorSaving = Color(0xFF9EC7F1),
        textPrimary = Color(0xFFE2E2E6),
        textSecondary = Color(0xFF909094),
        textMuted = Color(0xFF6A6C70),
        textWhite = Color(0xFFFFFFFF)
    )

    val oceanBlue = AppThemeColors(
        type = BudgetThemeType.OCEAN_BLUE,
        name = "Ocean Blue",
        description = "Calme & Moderne",
        isLight = false,
        primaryBlue = Color(0xFF80DEEA),
        primaryBlueLight = Color(0xFFE0F7FA),
        background = Color(0xFF0D1B2A),
        surface = Color(0xFF1B263B),
        surfaceVariant = Color(0xFF2E3D52),
        borderColor = Color(0xFF3E506B),
        colorIncomeBg = Color(0xFF122C27),
        colorIncomeBorder = Color(0xFF1B4E43),
        colorIncomeIconBg = Color(0xFF226053),
        colorIncome = Color(0xFF4EE2C6),
        colorExpenseBg = Color(0xFF351F22),
        colorExpenseBorder = Color(0xFF5A2A2D),
        colorExpenseIconBg = Color(0xFF6E3236),
        colorExpense = Color(0xFFFF8B80),
        colorSavingBg = Color(0xFF14243B),
        colorSavingBorder = Color(0xFF2A4269),
        colorSavingIconBg = Color(0xFF325387),
        colorSaving = Color(0xFF8EE3FF),
        textPrimary = Color(0xFFE0E6ED),
        textSecondary = Color(0xFF9CAEC4),
        textMuted = Color(0xFF70859E),
        textWhite = Color(0xFFFFFFFF)
    )

    val lavenderSoft = AppThemeColors(
        type = BudgetThemeType.LAVENDER_SOFT,
        name = "Lavender Soft",
        description = "Dux & Élégant",
        isLight = false,
        primaryBlue = Color(0xFFD1C4E9),
        primaryBlueLight = Color(0xFFEDE7F6),
        background = Color(0xFF1C1326),
        surface = Color(0xFF2B1F3B),
        surfaceVariant = Color(0xFF3B2D4F),
        borderColor = Color(0xFF4C3B63),
        colorIncomeBg = Color(0xFF1C2C23),
        colorIncomeBorder = Color(0xFF2D4D3A),
        colorIncomeIconBg = Color(0xFF3A634B),
        colorIncome = Color(0xFF94EAA4),
        colorExpenseBg = Color(0xFF36202D),
        colorExpenseBorder = Color(0xFF592D46),
        colorExpenseIconBg = Color(0xFF6E3957),
        colorExpense = Color(0xFFFF9EB1),
        colorSavingBg = Color(0xFF211E3F),
        colorSavingBorder = Color(0xFF343063),
        colorSavingIconBg = Color(0xFF3F3B77),
        colorSaving = Color(0xFFD6B2FF),
        textPrimary = Color(0xFFECE5F5),
        textSecondary = Color(0xFFA69BB5),
        textMuted = Color(0xFF796D8A),
        textWhite = Color(0xFFFFFFFF)
    )

    val emeraldFinance = AppThemeColors(
        type = BudgetThemeType.EMERALD_FINANCE,
        name = "Emerald Finance",
        description = "Finance & Épargne",
        isLight = false,
        primaryBlue = Color(0xFF80CBC4),
        primaryBlueLight = Color(0xFFE0F2F1),
        background = Color(0xFF0B1411),
        surface = Color(0xFF14241F),
        surfaceVariant = Color(0xFF1C362D),
        borderColor = Color(0xFF254B3E),
        colorIncomeBg = Color(0xFF183321),
        colorIncomeBorder = Color(0xFF2A5E3A),
        colorIncomeIconBg = Color(0xFF337346),
        colorIncome = Color(0xFF81C784),
        colorExpenseBg = Color(0xFF2A1C14),
        colorExpenseBorder = Color(0xFF4C2F1F),
        colorExpenseIconBg = Color(0xFF5D3B26),
        colorExpense = Color(0xFFFFB74D),
        colorSavingBg = Color(0xFF122C33),
        colorSavingBorder = Color(0xFF1C4D59),
        colorSavingIconBg = Color(0xFF225E6E),
        colorSaving = Color(0xFF4DB6AC),
        textPrimary = Color(0xFFE0ECE8),
        textSecondary = Color(0xFF94ABA4),
        textMuted = Color(0xFF6A807A),
        textWhite = Color(0xFFFFFFFF)
    )

    val lightStudent = AppThemeColors(
        type = BudgetThemeType.LIGHT_STUDENT,
        name = "Light Student",
        description = "Clair & Lisible",
        isLight = true,
        primaryBlue = Color(0xFF6200EE),
        primaryBlueLight = Color(0xFFEFF6FF),
        background = Color(0xFFF7F8FA),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFECEFF1),
        borderColor = Color(0xFFECEFF1),
        colorIncomeBg = Color(0xFFE8F5E9),
        colorIncomeBorder = Color(0xFFC8E6C9),
        colorIncomeIconBg = Color(0xFFA5D6A7),
        colorIncome = Color(0xFF2E7D32),
        colorExpenseBg = Color(0xFFFFEBEE),
        colorExpenseBorder = Color(0xFFFFCDD2),
        colorExpenseIconBg = Color(0xFFEF9A9A),
        colorExpense = Color(0xFFC62828),
        colorSavingBg = Color(0xFFE3F2FD),
        colorSavingBorder = Color(0xFFBBDEFB),
        colorSavingIconBg = Color(0xFF90CAF9),
        colorSaving = Color(0xFF1565C0),
        textPrimary = Color(0xFF111827),
        textSecondary = Color(0xFF4B5563),
        textMuted = Color(0xFF9CA3AF),
        textWhite = Color(0xFF111827)
    )

    val softLight = AppThemeColors(
        type = BudgetThemeType.SOFT_LIGHT,
        name = "Soft Light",
        description = "Clair & Violet",
        isLight = true,
        primaryBlue = Color(0xFF7C3AED),
        primaryBlueLight = Color(0xFFF5F3FF),
        background = Color(0xFFF9FAFB),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF3F4F6),
        borderColor = Color(0xFFE5E7EB),
        colorIncomeBg = Color(0xFFEFFDF5),
        colorIncomeBorder = Color(0xFFDCFCE7),
        colorIncomeIconBg = Color(0xFFBBF7D0),
        colorIncome = Color(0xFF15803D),
        colorExpenseBg = Color(0xFFFEF2F2),
        colorExpenseBorder = Color(0xFFFEE2E2),
        colorExpenseIconBg = Color(0xFFFCA5A5),
        colorExpense = Color(0xFFB91C1C),
        colorSavingBg = Color(0xFFEFF6FF),
        colorSavingBorder = Color(0xFFDBEAFE),
        colorSavingIconBg = Color(0xFFBFDBFE),
        colorSaving = Color(0xFF1D4ED8),
        textPrimary = Color(0xFF111827),
        textSecondary = Color(0xFF4B5563),
        textMuted = Color(0xFF9CA3AF),
        textWhite = Color(0xFF111827)
    )

    val oceanCalm = AppThemeColors(
        type = BudgetThemeType.OCEAN_CALM,
        name = "Ocean Calm",
        description = "Clair & Océan",
        isLight = true,
        primaryBlue = Color(0xFF00ACC1),
        primaryBlueLight = Color(0xFFE0F7FA),
        background = Color(0xFFE6F3F7),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFDCEEF3),
        borderColor = Color(0xFFBFE0E7),
        colorIncomeBg = Color(0xFFE8F5E9),
        colorIncomeBorder = Color(0xFFC8E6C9),
        colorIncomeIconBg = Color(0xFFA5D6A7),
        colorIncome = Color(0xFF2E7D32),
        colorExpenseBg = Color(0xFFFFEBEE),
        colorExpenseBorder = Color(0xFFFFCDD2),
        colorExpenseIconBg = Color(0xFFEF9A9A),
        colorExpense = Color(0xFFC62828),
        colorSavingBg = Color(0xFFE3F2FD),
        colorSavingBorder = Color(0xFFBBDEFB),
        colorSavingIconBg = Color(0xFF90CAF9),
        colorSaving = Color(0xFF1565C0),
        textPrimary = Color(0xFF07212B),
        textSecondary = Color(0xFF3F5A66),
        textMuted = Color(0xFF76949F),
        textWhite = Color(0xFF07212B)
    )

    val greenFocus = AppThemeColors(
        type = BudgetThemeType.GREEN_FOCUS,
        name = "Green Focus",
        description = "Clair & Émeraude",
        isLight = true,
        primaryBlue = Color(0xFF059669),
        primaryBlueLight = Color(0xFFECFDF5),
        background = Color(0xFFEBF6F0),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFD6EDE0),
        borderColor = Color(0xFFBEDFCD),
        colorIncomeBg = Color(0xFFE8F5E9),
        colorIncomeBorder = Color(0xFFC8E6C9),
        colorIncomeIconBg = Color(0xFFA5D6A7),
        colorIncome = Color(0xFF2E7D32),
        colorExpenseBg = Color(0xFFFFEBEE),
        colorExpenseBorder = Color(0xFFFFCDD2),
        colorExpenseIconBg = Color(0xFFEF9A9A),
        colorExpense = Color(0xFFC62828),
        colorSavingBg = Color(0xFFE3F2FD),
        colorSavingBorder = Color(0xFFBBDEFB),
        colorSavingIconBg = Color(0xFF90CAF9),
        colorSaving = Color(0xFF1565C0),
        textPrimary = Color(0xFF042D1A),
        textSecondary = Color(0xFF325E47),
        textMuted = Color(0xFF6F9A83),
        textWhite = Color(0xFF042D1A)
    )

    val lavenderClean = AppThemeColors(
        type = BudgetThemeType.LAVENDER_CLEAN,
        name = "Lavender Clean",
        description = "Clair & Lavande",
        isLight = true,
        primaryBlue = Color(0xFF8B5CF6),
        primaryBlueLight = Color(0xFFF5F3FF),
        background = Color(0xFFF3E8FF),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFEAD5FF),
        borderColor = Color(0xFFDCBCFC),
        colorIncomeBg = Color(0xFFE8F5E9),
        colorIncomeBorder = Color(0xFFC8E6C9),
        colorIncomeIconBg = Color(0xFFA5D6A7),
        colorIncome = Color(0xFF2E7D32),
        colorExpenseBg = Color(0xFFFFEBEE),
        colorExpenseBorder = Color(0xFFFFCDD2),
        colorExpenseIconBg = Color(0xFFEF9A9A),
        colorExpense = Color(0xFFC62828),
        colorSavingBg = Color(0xFFE3F2FD),
        colorSavingBorder = Color(0xFFBBDEFB),
        colorSavingIconBg = Color(0xFF90CAF9),
        colorSaving = Color(0xFF1565C0),
        textPrimary = Color(0xFF2E1065),
        textSecondary = Color(0xFF5B3B9C),
        textMuted = Color(0xFF9172CD),
        textWhite = Color(0xFF2E1065)
    )

    val lightPro = AppThemeColors(
        type = BudgetThemeType.LIGHT_PRO,
        name = "Light Pro",
        description = "Clair & Professionnel",
        isLight = true,
        primaryBlue = Color(0xFF2563EB),
        primaryBlueLight = Color(0xFFEFF6FF),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF9FAFB),
        surfaceVariant = Color(0xFFF3F4F6),
        borderColor = Color(0xFFE5E7EB),
        colorIncomeBg = Color(0xFFE8F5E9),
        colorIncomeBorder = Color(0xFFC8E6C9),
        colorIncomeIconBg = Color(0xFFA5D6A7),
        colorIncome = Color(0xFF2E7D32),
        colorExpenseBg = Color(0xFFFFEBEE),
        colorExpenseBorder = Color(0xFFFFCDD2),
        colorExpenseIconBg = Color(0xFFEF9A9A),
        colorExpense = Color(0xFFC62828),
        colorSavingBg = Color(0xFFE3F2FD),
        colorSavingBorder = Color(0xFFBBDEFB),
        colorSavingIconBg = Color(0xFF90CAF9),
        colorSaving = Color(0xFF1565C0),
        textPrimary = Color(0xFF111827),
        textSecondary = Color(0xFF4B5563),
        textMuted = Color(0xFF9CA3AF),
        textWhite = Color(0xFF111827)
    )

    var currentColors by mutableStateOf<AppThemeColors>(bentoNuit)

    fun getThemeByType(type: BudgetThemeType): AppThemeColors {
        return when (type) {
            BudgetThemeType.BENTO_NUIT -> bentoNuit
            BudgetThemeType.OCEAN_BLUE -> oceanBlue
            BudgetThemeType.LAVENDER_SOFT -> lavenderSoft
            BudgetThemeType.EMERALD_FINANCE -> emeraldFinance
            BudgetThemeType.LIGHT_STUDENT -> lightStudent
            BudgetThemeType.SOFT_LIGHT -> softLight
            BudgetThemeType.OCEAN_CALM -> oceanCalm
            BudgetThemeType.GREEN_FOCUS -> greenFocus
            BudgetThemeType.LAVENDER_CLEAN -> lavenderClean
            BudgetThemeType.LIGHT_PRO -> lightPro
        }
    }

    fun setTheme(type: BudgetThemeType) {
        currentColors = getThemeByType(type)
    }
}

// Map individual core palette colors dynamically to the active colors!
val PrimaryBlue: Color get() = ThemeManager.currentColors.primaryBlue
val PrimaryBlueLight: Color get() = ThemeManager.currentColors.primaryBlueLight
val DarkBackground: Color get() = ThemeManager.currentColors.background
val DarkSurface: Color get() = ThemeManager.currentColors.surface
val DarkSurfaceVariant: Color get() = ThemeManager.currentColors.surfaceVariant
val BorderColor: Color get() = ThemeManager.currentColors.borderColor

val ColorIncomeBg: Color get() = ThemeManager.currentColors.colorIncomeBg
val ColorIncomeBorder: Color get() = ThemeManager.currentColors.colorIncomeBorder
val ColorIncomeIconBg: Color get() = ThemeManager.currentColors.colorIncomeIconBg
val ColorIncome: Color get() = ThemeManager.currentColors.colorIncome

val ColorExpenseBg: Color get() = ThemeManager.currentColors.colorExpenseBg
val ColorExpenseBorder: Color get() = ThemeManager.currentColors.colorExpenseBorder
val ColorExpenseIconBg: Color get() = ThemeManager.currentColors.colorExpenseIconBg
val ColorExpense: Color get() = ThemeManager.currentColors.colorExpense

val ColorSavingBg: Color get() = ThemeManager.currentColors.colorSavingBg
val ColorSavingBorder: Color get() = ThemeManager.currentColors.colorSavingBorder
val ColorSavingIconBg: Color get() = ThemeManager.currentColors.colorSavingIconBg
val ColorSaving: Color get() = ThemeManager.currentColors.colorSaving

val TextPrimary: Color get() = ThemeManager.currentColors.textPrimary
val TextSecondary: Color get() = ThemeManager.currentColors.textSecondary
val TextMuted: Color get() = ThemeManager.currentColors.textMuted
val TextWhite: Color get() = ThemeManager.currentColors.textWhite
