package com.example.domain.analytics

import com.example.domain.model.SavingsGoal
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import java.util.Calendar

data class GoalAnalysis(
    val goal: SavingsGoal,
    val currentCollected: Double,
    val progressPercentage: Double, // 0.0 to 100.0
    val remainingAmount: Double,
    val averageMonthlySaving: Double,
    val estimatedMonths: Int, // -1 means infinite/cannot calculate
    val projectionMessage: String,
    val insightMessage: String?
)

object GoalAnalyzer {

    /**
     * Analyzes a single SavingsGoal and produces dynamic stats, projections, and insights.
     */
    fun analyze(
        goal: SavingsGoal,
        allTransactions: List<Transaction>,
        language: com.example.ui.localization.AppLanguageSupported = com.example.ui.localization.AppLanguageSupported.FRANCAIS
    ): GoalAnalysis {
        // Find transactions specifically associated with this goal ID
        val associatedTxs = allTransactions.filter { 
            it.type == TransactionType.SAVING.name && it.associatedGoalId == goal.id 
        }

        // Total collected is initial user input + sum of associated saving transactions
        val associatedSums = associatedTxs.sumOf { it.amount }
        val currentCollected = (goal.initialAmount + associatedSums).coerceIn(0.0, goal.targetAmount)
        
        val progressPercent = if (goal.targetAmount > 0.0) {
            (currentCollected / goal.targetAmount) * 100.0
        } else {
            100.0
        }

        val remaining = (goal.targetAmount - currentCollected).coerceAtLeast(0.0)

        // Calculate average monthly saving capacity based on all SAVING transactions
        val allSavingTxs = allTransactions.filter { it.type == TransactionType.SAVING.name }
        
        val monthsCount = allTransactions.map {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateTimestamp }
            Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH))
        }.distinct().size.coerceAtLeast(1)

        val totalSavedOverall = allSavingTxs.sumOf { it.amount }
        val avgMonthlySaving = if (monthsCount > 0) totalSavedOverall / monthsCount else 0.0

        val (estimatedMonths, projMsg) = when {
            remaining <= 0.0 -> {
                val msg = when (language) {
                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Ziel erreicht! 🎉"
                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Goal achieved! 🎉"
                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "¡Objetivo alcanzado! 🎉"
                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Obiettivo raggiunto! 🎉"
                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Meta alcançada! 🎉"
                    else -> "Objectif atteint ! 🎉"
                }
                Pair(0, msg)
            }
            avgMonthlySaving <= 0.0 -> {
                val msg = when (language) {
                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Neutrales Sparen. Verknüpfe Ersparnisse, um eine Schätzung zu erhalten."
                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Neutral savings. Link savings to estimate."
                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Ahorro neutral. Vincula un ahorro para estimar."
                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Risparmio neutro. Associa un risparmio per stimare."
                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Poupança neutra. Associe uma poupança para estimar."
                    else -> "Épargne neutre. Associe une épargne pour estimer."
                }
                Pair(-1, msg)
            }
            else -> {
                val months = Math.ceil(remaining / avgMonthlySaving).toInt()
                val msg = when (language) {
                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Du erreichst dein Ziel in $months ${if (months == 1) "Monat" else "Monaten"}"
                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "You will reach your goal in $months ${if (months == 1) "month" else "months"}"
                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Alcanzarás tu objetivo en $months ${if (months == 1) "mes" else "meses"}"
                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Raggiungerai il tuo obiettivo in $months ${if (months == 1) "mese" else "mesi"}"
                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Você atingirá seu objetivo em $months ${if (months == 1) "mês" else "meses"}"
                    else -> "Tu atteindras ton objectif en $months ${if (months == 1) "mois" else "mois"}"
                }
                Pair(months, msg)
            }
        }

        // Smart dynamic insights matcher
        val lastSavingTx = associatedTxs.maxByOrNull { it.dateTimestamp }
        val daysSinceLastSaving = if (lastSavingTx != null) {
            val diffMs = System.currentTimeMillis() - lastSavingTx.dateTimestamp
            diffMs / (1000 * 60 * 60 * 24)
        } else {
            // If never saved and goal isn't new, mark as stagnant
            35
        }

        val insight = when {
            remaining <= 0.0 -> {
                when (language) {
                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Herzlichen Glückwunsch, Sie haben dieses Ziel erreicht! 🥳"
                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Congratulations, you achieved this goal! 🥳"
                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "¡Felicitaciones, has alcanzado este objetivo! 🥳"
                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Congratulazioni, hai raggiunto questo obiettivo! 🥳"
                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Parabéns, você alcançou este objetivo! 🥳"
                    else -> "Félicitations, tu as accompli cet objectif ! 🥳"
                }
            }
            progressPercent >= 80.0 -> {
                when (language) {
                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Sie sind kurz vor Ihrem Ziel!"
                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "You are close to your goal!"
                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "¡Estás cerca de tu objetivo!"
                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Sei vicino al tuo obiettivo!"
                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Você está perto de seu objetivo!"
                    else -> "Tu es proche de ton objectif !"
                }
            }
            avgMonthlySaving <= 0.0 || (associatedTxs.isEmpty() && goal.initialAmount == 0.0) -> {
                when (language) {
                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Beginne, für dieses Ziel zu sparen."
                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Start saving for this goal."
                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Empieza a ahorrar para este objetivo."
                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Inizia a risparmiare per questo obiettivo."
                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Comece a poupar para este objetivo."
                    else -> "Commence à épargner pour cet objectif."
                }
            }
            daysSinceLastSaving >= 30 -> {
                when (language) {
                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Du hast in letzter Zeit nicht gespart"
                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "You haven't saved recently"
                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "No has ahorrado recientemente"
                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Non hai risparmiato di recente"
                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Você não poupou recentemente"
                    else -> "Tu n’as pas épargné récemment"
                }
            }
            estimatedMonths > 8 -> {
                when (language) {
                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Versuche, deine monatliche Ersparnis zu erhöhen"
                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Try increasing your monthly savings"
                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Intenta aumentar tu ahorro mensual"
                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Prova ad aumentare il tuo risparmio mensile"
                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Tente aumentar sua poupança mensal"
                    else -> "Essaie d’augmenter ton épargne mensuelle"
                }
            }
            else -> {
                when (language) {
                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Tolles Tempo, mach weiter so!"
                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Great pace, keep it up!"
                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "¡Buen ritmo, sigue así!"
                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Ottimo ritmo, continua così!"
                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Excelente ritmo, continue assim!"
                    else -> "Super rythme, continue comme ça !"
                }
            }
        }

        return GoalAnalysis(
            goal = goal,
            currentCollected = currentCollected,
            progressPercentage = progressPercent,
            remainingAmount = remaining,
            averageMonthlySaving = avgMonthlySaving,
            estimatedMonths = estimatedMonths,
            projectionMessage = projMsg,
            insightMessage = insight
        )
    }

    /**
     * Batch analyzes list of goals.
     */
    fun analyzeAll(
        goals: List<SavingsGoal>,
        allTransactions: List<Transaction>,
        language: com.example.ui.localization.AppLanguageSupported = com.example.ui.localization.AppLanguageSupported.FRANCAIS
    ): List<GoalAnalysis> {
        return goals.map { analyze(it, allTransactions, language) }
    }
}
