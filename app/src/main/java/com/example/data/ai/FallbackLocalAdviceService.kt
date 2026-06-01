// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.ai

import com.example.domain.ai.BudgetAiResult
import com.example.domain.ai.BudgetAiService
import com.example.domain.ai.GoalProgressInfo
import java.util.Locale

class FallbackLocalAdviceService : BudgetAiService {

    override suspend fun generateAdvice(
        totalIncome: Double,
        totalExpense: Double,
        totalSaving: Double,
        recentExpensesByCategory: Map<String, Double>,
        goalsProgress: List<GoalProgressInfo>,
        selectedLanguage: com.example.ui.localization.AppLanguageSupported
    ): BudgetAiResult {
        // Find actual language to use or fall back
        val actualLanguage = when (selectedLanguage) {
            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> selectedLanguage
            com.example.ui.localization.AppLanguageSupported.ENGLISH -> selectedLanguage
            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> selectedLanguage
            com.example.ui.localization.AppLanguageSupported.ESPANOL -> selectedLanguage
            com.example.ui.localization.AppLanguageSupported.ITALIANO -> selectedLanguage
            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> selectedLanguage
            else -> com.example.ui.localization.AppLanguageSupported.ENGLISH // Default fallback
        }

        val savingsPercent = if (totalIncome > 0.0) (totalSaving / totalIncome) * 100 else 0.0

        // 1. Generate Automatic summary
        val summaryHeader = when (actualLanguage) {
            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> 
                "Ce mois-ci, de manière globale, tes revenus sont de ${formatAmount(totalIncome, selectedLanguage)}, tes dépenses de ${formatAmount(totalExpense, selectedLanguage)} et ton épargne de ${formatAmount(totalSaving, selectedLanguage)}."
            com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                "Diesen Monat betragen Ihre Gesamteinnahmen ${formatAmount(totalIncome, selectedLanguage)}, Ihre Ausgaben ${formatAmount(totalExpense, selectedLanguage)} und Ihre Ersparnisse ${formatAmount(totalSaving, selectedLanguage)}."
            com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                "Este mes, de manera general, tus ingresos son de ${formatAmount(totalIncome, selectedLanguage)}, tus gastos de ${formatAmount(totalExpense, selectedLanguage)} y tus ahorros de ${formatAmount(totalSaving, selectedLanguage)}."
            com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                "Questo mese, in generale, le tue entrate sono di ${formatAmount(totalIncome, selectedLanguage)}, le tue uscite di ${formatAmount(totalExpense, selectedLanguage)} e i tuoi risparmi di ${formatAmount(totalSaving, selectedLanguage)}."
            com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                "Este mês, de maneira geral, suas receitas são de ${formatAmount(totalIncome, selectedLanguage)}, suas despesas de ${formatAmount(totalExpense, selectedLanguage)} e suas poupanças de ${formatAmount(totalSaving, selectedLanguage)}."
            else -> // English / fallback
                "This month, overall, your income is ${formatAmount(totalIncome, selectedLanguage)}, your expenses are ${formatAmount(totalExpense, selectedLanguage)} and your savings are ${formatAmount(totalSaving, selectedLanguage)}."
        }

        // Find top 1 or 2 spending categories
        val sortedCategories = recentExpensesByCategory.toList()
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }

        // Localized category name helper:
        fun getCatName(cat: String): String {
            val strings = com.example.ui.localization.getStringsForLanguage(actualLanguage)
            return com.example.presentation.ui.UiUtils.getLocalizedCategory(cat, strings)
        }

        val summaryCategories = when (actualLanguage) {
            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> {
                when {
                    sortedCategories.isEmpty() -> "Aucune dépense significative n'a été enregistrée pour le moment."
                    sortedCategories.size == 1 -> "Ta dépense principale concerne le poste \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)})."
                    else -> "Tes dépenses principales sont le poste \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)}) et \"${getCatName(sortedCategories[1].first)}\" (${formatAmount(sortedCategories[1].second, selectedLanguage)})."
                }
            }
            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> {
                when {
                    sortedCategories.isEmpty() -> "Es wurden noch keine wesentlichen Ausgaben erfasst."
                    sortedCategories.size == 1 -> "Ihre Hauptausgabe betrifft den Bereich \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)})."
                    else -> "Ihre Hauptausgaben sind \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)}) und \"${getCatName(sortedCategories[1].first)}\" (${formatAmount(sortedCategories[1].second, selectedLanguage)})."
                }
            }
            com.example.ui.localization.AppLanguageSupported.ESPANOL -> {
                when {
                    sortedCategories.isEmpty() -> "No se ha registrado ningún gasto significativo por el momento."
                    sortedCategories.size == 1 -> "Tu gasto principal corresponde a \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)})."
                    else -> "Tus gastos principales son \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)}) y \"${getCatName(sortedCategories[1].first)}\" (${formatAmount(sortedCategories[1].second, selectedLanguage)})."
                }
            }
            com.example.ui.localization.AppLanguageSupported.ITALIANO -> {
                when {
                    sortedCategories.isEmpty() -> "Nessuna spesa significativa è stata registrata al momento."
                    sortedCategories.size == 1 -> "La tua spesa principale riguarda \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)})."
                    else -> "Le tue spese principali sono \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)}) e \"${getCatName(sortedCategories[1].first)}\" (${formatAmount(sortedCategories[1].second, selectedLanguage)})."
                }
            }
            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> {
                when {
                    sortedCategories.isEmpty() -> "Nenhuma despesa significativa foi registrada até o momento."
                    sortedCategories.size == 1 -> "Sua despesa principal é com \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)})."
                    else -> "Suas despesas principais são \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)}) e \"${getCatName(sortedCategories[1].first)}\" (${formatAmount(sortedCategories[1].second, selectedLanguage)})."
                }
            }
            else -> { // English / Fallback
                when {
                    sortedCategories.isEmpty() -> "No significant expenses have been recorded yet."
                    sortedCategories.size == 1 -> "Your main expense is on \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)})."
                    else -> "Your main expenses are \"${getCatName(sortedCategories[0].first)}\" (${formatAmount(sortedCategories[0].second, selectedLanguage)}) and \"${getCatName(sortedCategories[1].first)}\" (${formatAmount(sortedCategories[1].second, selectedLanguage)})."
                }
            }
        }

        val fullSummary = "$summaryHeader\n$summaryCategories"

        // 2. Draft customized dynamic advices (1-3 items)
        val advices = mutableListOf<String>()

        // Rule A: critical budget warnings or general flow
        if (totalExpense > totalIncome && totalIncome > 0.0) {
            when (actualLanguage) {
                com.example.ui.localization.AppLanguageSupported.FRANCAIS -> 
                    advices.add("Tes dépenses mensuelles dépassent tes revenus. Essaie de rationaliser les abonnements ou dépenses superflues immédiates.")
                com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                    advices.add("Ihre monatlichen Ausgaben übersteigen Ihre Einnahmen. Versuchen Sie, Abonnements oder unnötige Ausgaben zu reduzieren.")
                com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                    advices.add("Tus gastos mensuales superan tus ingresos. Intenta optimizar suscripciones o gastos innecesarios de inmediato.")
                com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                    advices.add("Le tue uscite mensili superano le tue entrate. Prova a tagliare gli abbonamenti o le spese superflue.")
                com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                    advices.add("Suas despesas mensais superam suas receitas. Tente cancelar assinaturas ou reduzir gastos supérfluos imediatamente.")
                else -> // English
                    advices.add("Your monthly expenses exceed your income. Try to streamline subscriptions or immediate unnecessary expenses.")
            }
        } else if (totalSaving <= 0.0 && totalIncome > 0.0) {
            when (actualLanguage) {
                com.example.ui.localization.AppLanguageSupported.FRANCAIS ->
                    advices.add("Tu n'as pas encore d'épargne de côté ce mois-ci. Essaie d'épargner même 10€ pour commencer à ancrer cette habitude.")
                com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                    advices.add("Sie haben diesen Monat noch nichts gespart. Versuchen Sie, auch nur 10€ beiseite zu legen, um diese Gewohnheit aufzubauen.")
                com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                    advices.add("Aún no has ahorrado nada este mes. Intenta ahorrar al menos 10€ para empezar a consolidar este hábito.")
                com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                    advices.add("Non hai ancora messo da parte risparmi questo mese. Prova a risparmiare anche solo 10€ per iniziare questa abitudine.")
                com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                    advices.add("Você ainda não guardou dinheiro este mês. Tente poupar mesmo que seja 10€ para começar a criar este hábito.")
                else -> // English
                    advices.add("You haven't set aside any savings yet this month. Try to save even $10 to start building this habit.")
            }
        } else if (savingsPercent < 10.0 && totalIncome > 0.0) {
            when (actualLanguage) {
                com.example.ui.localization.AppLanguageSupported.FRANCAIS ->
                    advices.add("Essaie d'augmenter ton épargne mensuelle petit à petit pour atteindre un coussin de sécurité confortable (idéalement 15% de tes revenus).")
                com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                    advices.add("Versuchen Sie, Ihre monatlichen Ersparnisse schrittweise zu erhöhen, um ein komfortables Polster aufzubauen (ideal sind 15 % der Einnahmen).")
                com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                    advices.add("Intenta aumentar tus ahorros mensuales poco a poco para acumular un fondo de emergencia (idealmente el 15% de tus ingresos).")
                com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                    advices.add("Prova ad aumentare i tuoi risparmi mensili a poco a poco per creare un fondo di sicurezza (idealmente il 15% delle tue entrate).")
                com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                    advices.add("Tente aumentar suas poupanças mensais pouco a pouco para construir uma reserva estável (idealmente 15% das suas receitas).")
                else -> // English
                    advices.add("Try to increase your monthly savings little by little to build a comfortable financial cushion (ideally 15% of your income).")
            }
        }

        // Rule B: Category-specific feedback
        val loisirsValue = recentExpensesByCategory.entries.find { 
            it.key.contains("Loisir", ignoreCase = true) || it.key.contains("Divertissement", ignoreCase = true) || it.key.contains("Leisure", ignoreCase = true)
        }?.value ?: 0.0
        val alimentationValue = recentExpensesByCategory.entries.find { 
            it.key.contains("Alimentation", ignoreCase = true) || it.key.contains("Course", ignoreCase = true) || it.key.contains("Groceries", ignoreCase = true)
        }?.value ?: 0.0

        if (loisirsValue > 0.0 && (loisirsValue >= totalIncome * 0.15 || loisirsValue >= totalExpense * 0.20)) {
            when (actualLanguage) {
                com.example.ui.localization.AppLanguageSupported.FRANCAIS ->
                    advices.add("Tu dépenses beaucoup en loisirs (${formatAmount(loisirsValue, selectedLanguage)}). Profite d'idées de sorties gratuites ce week-end pour alléger le budget !")
                com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                    advices.add("Sie geben viel für Freizeitbeschäftigungen aus (${formatAmount(loisirsValue, selectedLanguage)}). Nutzen Sie dieses Wochenende kostenlose Aktivitäten, um Ihr Budget zu entlasten!")
                com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                    advices.add("Estás gastando mucho en ocio y entretenimiento (${formatAmount(loisirsValue, selectedLanguage)}). ¡Considera actividades gratuitas este fin de semana para aliviar tu presupuesto!")
                com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                    advices.add("Stai spendendo molto per il tempo libero (${formatAmount(loisirsValue, selectedLanguage)}). Cerca attività gratuite questo fine settimana per alleggerire il bilancio!")
                com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                    advices.add("Você está gastando muito em lazer (${formatAmount(loisirsValue, selectedLanguage)}). Aproveite sugestões de passeios gratuitos este fim de semana para poupar!")
                else -> // English
                    advices.add("You are spending a lot on leisure and entertainment (${formatAmount(loisirsValue, selectedLanguage)}). Spot free activities this weekend to lighten your budget!")
            }
        } else if (alimentationValue > 0.0 && (alimentationValue >= totalIncome * 0.35 || alimentationValue >= totalExpense * 0.45)) {
            when (actualLanguage) {
                com.example.ui.localization.AppLanguageSupported.FRANCAIS ->
                    advices.add("Réduire un peu le budget alimentation et courses (ex: cuisiner maison ou faire de plus petites listes) t’aiderait à épargner plus.")
                com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                    advices.add("Eine Reduzierung Ihres Lebensmittelbudgets (z. B. durch Vorkochen oder kleinere Einkaufszettel) würde Ihnen helfen, mehr zu sparen.")
                com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                    advices.add("Reducir un poco el presupuesto en alimentación y compras (como cocinar más en casa) te ayudaría a ahorrar mucho más.")
                com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                    advices.add("Ridurre un po' il budget per la spesa alimentare (es. cucinando a casa o pianificando i pasti) ti aiuterebbe a risparmiare di più.")
                com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                    advices.add("Reduzir um pouco os gastos com alimentação (por exemplo, cozinhando em casa ou planejando melhor as compras) ajudaria a poupar mais.")
                else -> // English
                    advices.add("Reducing your grocery budget (e.g., cooking at home or preparing shopping lists) would help you save more.")
            }
        }

        // Rule C: Goal analysis insights
        val closeToGoal = goalsProgress.firstOrNull { it.progressPercent in 80.0..99.9 }
        val slowGoal = goalsProgress.firstOrNull { it.progressPercent in 1.0..49.9 }

        if (closeToGoal != null) {
            when (actualLanguage) {
                com.example.ui.localization.AppLanguageSupported.FRANCAIS ->
                    advices.add("Tu es hyper proche de ton objectif '${closeToGoal.name}' (${closeToGoal.progressPercent.toInt()}% atteint) ! Un dernier coup de collier pour finaliser le projet.")
                com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                    advices.add("Sie sind Ihrem Ziel '${closeToGoal.name}' sehr nahe (${closeToGoal.progressPercent.toInt()}% erreicht)! Machen Sie eine letzte Anstrengung, um Ihr Projekt abzuschließen.")
                com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                    advices.add("¡Estás increíblemente cerca de tu objetivo '${closeToGoal.name}' (${closeToGoal.progressPercent.toInt()}% alcanzado)! Un último esfuerzo para consolidar tu proyecto.")
                com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                    advices.add("Sei vicinissimo al tuo traguardo '${closeToGoal.name}' (raggiunto al ${closeToGoal.progressPercent.toInt()}%)! Un ultimo sforzo per finalizzare il progetto.")
                com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                    advices.add("Você está muito perto de sua meta '${closeToGoal.name}' (${closeToGoal.progressPercent.toInt()}% alcançado)! Um último esforço para concluir seu projeto.")
                else -> // English
                    advices.add("You are very close to your goal '${closeToGoal.name}' (${closeToGoal.progressPercent.toInt()}% achieved)! A small final effort to finalize this project.")
            }
        } else if (slowGoal != null) {
            when (actualLanguage) {
                com.example.ui.localization.AppLanguageSupported.FRANCAIS ->
                    advices.add("Ton objectif '${slowGoal.name}' progresse encore doucement. L'associer à une petite épargne automatique récurrente t'aiderait à garder le cap.")
                com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                    advices.add("Ihr Ziel '${slowGoal.name}' schreitet noch langsam voran. Die Verknüpfung mit einem wiederkehrenden automatischen Sparbetrag könnte Ihnen helfen, auf Kurs zu bleiben.")
                com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                    advices.add("Tu objetivo '${slowGoal.name}' avanza lentamente. Asociarlo a un ahorro automático recurrente te ayudaría a no perder el rumbo.")
                com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                    advices.add("Il tuo obiettivo '${slowGoal.name}' sta progredendo lentamente. Collegarlo a un risparmio automatico ricorrente ti aiuterebbe a mantenere la rotta.")
                com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                    advices.add("Sua meta '${slowGoal.name}' está avançando devagar. Associá-la a uma poupança automática recorrente ajudaria você a manter o foco.")
                else -> // English
                    advices.add("Your goal '${slowGoal.name}' is progressing slowly. Linking it to a small, automatic recurring saving would help you stay on track.")
            }
        } else if (goalsProgress.isEmpty()) {
            when (actualLanguage) {
                com.example.ui.localization.AppLanguageSupported.FRANCAIS ->
                    advices.add("Définis un objectif d'épargne concret (ex: Vacances, Ordinateur) dans l'onglet Analyses pour canaliser ton excédent budgétaire.")
                com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                    advices.add("Definieren Sie im Analyse-Tab ein konkretes Sparziel (z. B. Urlaub, Laptop), um Ihren Haushaltsüberschuss gezielt zu nutzen.")
                com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                    advices.add("Define un objetivo de ahorro concreto (ej: Vacaciones, Ordenador) en la pestaña de Análisis para rentabilizar tu excedente presupuestario.")
                com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                    advices.add("Definisci un obiettivo di risparmio concreto (es. Vacanze, Computer) nella scheda Analisi per canalizzare il rimanente del budget.")
                com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                    advices.add("Defina um objetivo concreto de poupança (ex: Férias, Computador) na guia Análise para canalizar seu excedente do orçamento.")
                else -> // English
                    advices.add("Define a concrete savings target (e.g., Vacation, Laptop) in the Analysis tab to guide your surplus income.")
            }
        }

        // Keep advice list to 1-3 items
        if (advices.isEmpty()) {
            when (actualLanguage) {
                com.example.ui.localization.AppLanguageSupported.FRANCAIS ->
                    advices.add("Budget bien équilibré ! Ton rythme actuel de dépenses est très sain. Continue ainsi pour préserver ta sérénité financière.")
                com.example.ui.localization.AppLanguageSupported.DEUTSCH ->
                    advices.add("Sehr gut ausgewogenes Budget! Ihre aktuelle Ausgabenrate ist sehr gesund. Machen Sie weiter so, um Ihre finanzielle Gelassenheit zu bewahren.")
                com.example.ui.localization.AppLanguageSupported.ESPANOL ->
                    advices.add("¡Presupuesto bien equilibrado! Tu ritmo de gasto actual es muy saludable. Sigue así para mantener tu tranquilidad financiera.")
                com.example.ui.localization.AppLanguageSupported.ITALIANO ->
                    advices.add("Bilancio ben equilibrato! Il tuo ritmo di spesa attuale è molto sano. Continua così per preservare la tua serenità finanziaria.")
                com.example.ui.localization.AppLanguageSupported.PORTUGUES ->
                    advices.add("Orçamento muito bien equilibrado! O ritmo das suas despesas é super saudável. Continue assim para preservar sua segurança financeira.")
                else -> // English
                    advices.add("Well-balanced budget! Your current spending rate is very healthy. Keep it up to preserve your financial peace of mind.")
            }
        }

        val finalAdvices = advices.distinct().take(3)

        return BudgetAiResult(
            summary = fullSummary,
            adviceList = finalAdvices,
            isDemo = true
        )
    }

    private fun formatAmount(value: Double, language: com.example.ui.localization.AppLanguageSupported): String {
        return com.example.presentation.ui.UiUtils.formatMoney(
            value,
            com.example.presentation.ui.UiUtils.currentCurrencySymbol,
            language
        )
    }
}
