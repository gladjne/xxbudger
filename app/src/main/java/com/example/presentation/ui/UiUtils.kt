package com.example.presentation.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UiUtils {
    var currentCurrencySymbol: String = "€"
    var currentLanguage: com.example.ui.localization.AppLanguageSupported = com.example.ui.localization.AppLanguageSupported.FRANCAIS

    /**
     * Formats a double amount into a beautiful currency string based on selected symbol
     */
    fun formatCurrency(amount: Double): String {
        return formatMoney(amount, currentCurrencySymbol, currentLanguage)
    }

    /**
     * Formats a double amount dynamically with specified currency and language
     */
    fun formatMoney(
        amount: Double,
        currency: String = currentCurrencySymbol,
        language: com.example.ui.localization.AppLanguageSupported = currentLanguage
    ): String {
        val locale = when (language) {
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

        return when (currency) {
            "€" -> String.format(locale, "%,.2f €", amount)
            "$" -> String.format(locale, "$%,.2f", amount)
            "£" -> String.format(locale, "£%,.2f", amount)
            "¥" -> String.format(locale, "¥%,.0f", amount)
            "₹" -> String.format(locale, "₹%,.2f", amount)
            "FCFA" -> String.format(locale, "%,.0f FCFA", amount)
            "₦" -> String.format(locale, "₦%,.2f", amount)
            "R" -> String.format(locale, "R %,.2f", amount)
            "CA$" -> String.format(locale, "CAD %,.2f", amount)
            "A$" -> String.format(locale, "AUD %,.2f", amount)
            else -> String.format(locale, "%,.2f %s", amount, currency)
        }
    }

    /**
     * Formats a timestamp into a clean, human-readable date based on active language
     */
    fun formatDate(timestamp: Long, language: com.example.ui.localization.AppLanguageSupported = currentLanguage): String {
        val locale = when (language) {
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
        val pattern = when (language) {
            com.example.ui.localization.AppLanguageSupported.CHINESE,
            com.example.ui.localization.AppLanguageSupported.JAPANESE,
            com.example.ui.localization.AppLanguageSupported.KOREAN -> "yyyy年MM月dd日"
            com.example.ui.localization.AppLanguageSupported.ENGLISH -> "EEEE, MMMM d, yyyy"
            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "EEEE, d. MMMM yyyy"
            com.example.ui.localization.AppLanguageSupported.ESPANOL -> "EEEE, d 'de' MMMM 'de' yyyy"
            com.example.ui.localization.AppLanguageSupported.ITALIANO -> "EEEE d MMMM yyyy"
            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "EEEE, d 'de' MMMM 'de' yyyy"
            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "EEEE d MMMM yyyy"
            else -> "EEEE d MMMM yyyy"
        }
        val sdf = SimpleDateFormat(pattern, locale)
        val raw = sdf.format(Date(timestamp))
        return when (language) {
            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> raw.replaceFirstChar { it.uppercase() }
            com.example.ui.localization.AppLanguageSupported.ENGLISH -> raw.replaceFirstChar { it.uppercase() }
            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> raw.replaceFirstChar { it.uppercase() }
            com.example.ui.localization.AppLanguageSupported.ESPANOL -> raw.replaceFirstChar { it.lowercase() }
            com.example.ui.localization.AppLanguageSupported.ITALIANO -> raw.replaceFirstChar { it.lowercase() }
            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> raw.replaceFirstChar { it.lowercase() }
            else -> raw
        }
    }

    /**
     * Formats a month and year into a short localized month label
     */
    fun formatMonthYear(year: Int, month0Indexed: Int, language: com.example.ui.localization.AppLanguageSupported = currentLanguage): String {
        val locale = when (language) {
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
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.YEAR, year)
            set(java.util.Calendar.MONTH, month0Indexed)
        }
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        
        val sdf = SimpleDateFormat("MMM", locale)
        val monthStr = sdf.format(cal.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }.removeSuffix(".")
        
        return if (year != currentYear) {
            "$monthStr ${year % 100}"
        } else {
            monthStr
        }
    }

    /**
     * Formats a timestamp into a shorter clean date format without day of week, based on active language
     */
    fun formatShortDate(timestamp: Long, language: com.example.ui.localization.AppLanguageSupported = currentLanguage): String {
        val locale = when (language) {
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
        val pattern = when (language) {
            com.example.ui.localization.AppLanguageSupported.CHINESE,
            com.example.ui.localization.AppLanguageSupported.JAPANESE,
            com.example.ui.localization.AppLanguageSupported.KOREAN -> "yyyy年MM月dd日"
            com.example.ui.localization.AppLanguageSupported.ENGLISH -> "MMMM d, yyyy"
            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "d. MMMM yyyy"
            com.example.ui.localization.AppLanguageSupported.ESPANOL -> "d 'de' MMMM 'de' yyyy"
            com.example.ui.localization.AppLanguageSupported.ITALIANO -> "d MMMM yyyy"
            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "d 'de' MMMM 'de' yyyy"
            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "d MMMM yyyy"
            else -> "d MMMM yyyy"
        }
        val sdf = SimpleDateFormat(pattern, locale)
        val raw = sdf.format(Date(timestamp))
        return when (language) {
            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> raw.replaceFirstChar { it.lowercase() }
            com.example.ui.localization.AppLanguageSupported.ENGLISH -> raw.replaceFirstChar { it.uppercase() }
            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> raw.replaceFirstChar { it.uppercase() }
            com.example.ui.localization.AppLanguageSupported.ESPANOL -> raw.replaceFirstChar { it.lowercase() }
            com.example.ui.localization.AppLanguageSupported.ITALIANO -> raw.replaceFirstChar { it.lowercase() }
            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> raw.replaceFirstChar { it.lowercase() }
            else -> raw
        }
    }

    /**
     * Returns a localized category display name
     */
    fun getLocalizedCategory(category: String, t: com.example.ui.localization.AppStrings): String {
        return when (category) {
            // Income categories
            "Salaire job étudiant" -> t.catStudentJob
            "Bourse" -> t.catScholarship
            "Aide familiale" -> t.catFamilyHelp
            "Remboursement prêt" -> t.catLoanRepayment
            "Autre entrée" -> t.catOtherIncome
            
            // Expense categories
            "Logement" -> t.catRent
            "Courses" -> t.catGroceries
            "Transport" -> t.catTransport
            "Études" -> t.catStudies
            "Projet étude" -> t.catStudyProject
            "Santé" -> t.catHealth
            "Loisirs" -> t.catLeisure
            "Remboursement de prêt" -> t.labelLoanRepayment
            "Autre dépense" -> t.catOtherExpense
            
            // Saving categories
            "Urgence" -> t.catEmergency
            "Ordinateur" -> t.catComputer
            "Voyage" -> t.catTravel
            "Frais universitaires" -> t.catUniFees
            "Autre objectif" -> t.catOtherGoal
            
            else -> category
        }
    }

    /**
     * Centralized budget status translation helper.
     */
    fun getBudgetStatus(
        totalIncome: Double,
        totalExpense: Double,
        balance: Double,
        savingsRate: Double,
        language: com.example.ui.localization.AppLanguageSupported
    ): String {
        return when (language) {
            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> when {
                totalIncome == 0.0 && totalExpense == 0.0 -> "En attente de données ⏳"
                balance < 0.0 -> "En déséquilibre temporaire ⚠️"
                savingsRate >= 25.0 -> "Épargnant d'élite 💎"
                savingsRate >= 10.0 -> "Budget sain et équilibré 🍃"
                else -> "Gestion active au quotidien 🎯"
            }
            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> when {
                totalIncome == 0.0 && totalExpense == 0.0 -> "Warten auf Daten ⏳"
                balance < 0.0 -> "Vorübergehendes Ungleichgewicht ⚠️"
                savingsRate >= 25.0 -> "Elite-Sparer 💎"
                savingsRate >= 10.0 -> "Gesundes & ausgewogenes Budget 🍃"
                else -> "Aktive tägliche Verwaltung 🎯"
            }
            com.example.ui.localization.AppLanguageSupported.ESPANOL -> when {
                totalIncome == 0.0 && totalExpense == 0.0 -> "Esperando datos ⏳"
                balance < 0.0 -> "Desequilibrio temporal ⚠️"
                savingsRate >= 25.0 -> "Ahorrador de élite 💎"
                savingsRate >= 10.0 -> "Presupuesto sano y equilibrado 🍃"
                else -> "Gestión activa diaria 🎯"
            }
            com.example.ui.localization.AppLanguageSupported.ITALIANO -> when {
                totalIncome == 0.0 && totalExpense == 0.0 -> "In attesa di dati ⏳"
                balance < 0.0 -> "Squilibrio temporaneo ⚠️"
                savingsRate >= 25.0 -> "Risparmiatore d'élite 💎"
                savingsRate >= 10.0 -> "Budget sano e bilanciato 🍃"
                else -> "Gestione attiva quotidiana 🎯"
            }
            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> when {
                totalIncome == 0.0 && totalExpense == 0.0 -> "Aguardando dados ⏳"
                balance < 0.0 -> "Desequilíbrio temporário ⚠️"
                savingsRate >= 25.0 -> "Poupador de elite 💎"
                savingsRate >= 10.0 -> "Orçamento saudável e equilibrado 🍃"
                else -> "Gestão diária ativa 🎯"
            }
            com.example.ui.localization.AppLanguageSupported.CHINESE -> when {
                totalIncome == 0.0 && totalExpense == 0.0 -> "等待数据中 ⏳"
                balance < 0.0 -> "临时财务失衡 ⚠️"
                savingsRate >= 25.0 -> "精锐储蓄家 💎"
                savingsRate >= 10.0 -> "健康平衡的预算 🍃"
                else -> "每日积极管理 🎯"
            }
            com.example.ui.localization.AppLanguageSupported.JAPANESE -> when {
                totalIncome == 0.0 && totalExpense == 0.0 -> "データ待ち ⏳"
                balance < 0.0 -> "一時的な不均衡 ⚠️"
                savingsRate >= 25.0 -> "エリート貯蓄家 💎"
                savingsRate >= 10.0 -> "健全でバランスの取れた予算 🍃"
                else -> "毎日のアクティブ管理 🎯"
            }
            com.example.ui.localization.AppLanguageSupported.KOREAN -> when {
                totalIncome == 0.0 && totalExpense == 0.0 -> "데이터 대기 중 ⏳"
                balance < 0.0 -> "일시적 불균형 상태 ⚠️"
                savingsRate >= 25.0 -> "엘리트 저축가 💎"
                savingsRate >= 10.0 -> "건강하고 균형 잡힌 예산 🍃"
                else -> "매일 적극적인 관리 🎯"
            }
            com.example.ui.localization.AppLanguageSupported.RUSSIAN -> when {
                totalIncome == 0.0 && totalExpense == 0.0 -> "Ожидание данных ⏳"
                balance < 0.0 -> "Временный дисбаланс ⚠️"
                savingsRate >= 25.0 -> "Элитный вкладчик 💎"
                savingsRate >= 10.0 -> "Здоровый и сбалансированный бюджет 🍃"
                else -> "Активное ежедневное управление 🎯"
            }
            com.example.ui.localization.AppLanguageSupported.ARABIC -> when {
                totalIncome == 0.0 && totalExpense == 0.0 -> "في انتظار البيانات ⏳"
                balance < 0.0 -> "عدم توازن مؤقت ⚠️"
                savingsRate >= 25.0 -> "مدخر نخبة 💎"
                savingsRate >= 10.0 -> "ميزانية صحية ومتوازنة 🍃"
                else -> "إدارة يومية نشطة 🎯"
            }
            else -> when { // ENGLISH / fallback
                totalIncome == 0.0 && totalExpense == 0.0 -> "Awaiting data ⏳"
                balance < 0.0 -> "Temporary imbalance ⚠️"
                savingsRate >= 25.0 -> "Elite Saver 💎"
                savingsRate >= 10.0 -> "Healthy & Balanced Budget 🍃"
                else -> "Active Daily Management 🎯"
            }
        }
    }
}
