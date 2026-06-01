// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.domain.ai

data class BudgetAiResult(
    val summary: String,
    val adviceList: List<String>,
    val isDemo: Boolean // Indique si le conseil vient du simulateur local ou de Gemini de manière transparente
)

interface BudgetAiService {
    suspend fun generateAdvice(
        totalIncome: Double,
        totalExpense: Double,
        totalSaving: Double,
        recentExpensesByCategory: Map<String, Double>,
        goalsProgress: List<GoalProgressInfo>,
        selectedLanguage: com.example.ui.localization.AppLanguageSupported = com.example.ui.localization.AppLanguageSupported.FRANCAIS
    ): BudgetAiResult
}

data class GoalProgressInfo(
    val id: Int,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val progressPercent: Double,
    val remainingAmount: Double,
    val projectionText: String
)
