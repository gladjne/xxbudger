// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.TransactionRepository
import com.example.domain.analytics.BudgetAnalyzer
import com.example.domain.analytics.BudgetAnalysisResult
import com.example.domain.analytics.AdvancedFinancialAnalyzer
import com.example.domain.analytics.AdvancedAnalysisResult
import com.example.domain.model.SavingsGoal
import com.example.domain.model.Debt
import com.example.domain.analytics.GoalAnalyzer
import com.example.domain.analytics.GoalAnalysis
import com.example.domain.ai.BudgetAiResult
import com.example.domain.ai.BudgetAiService
import com.example.domain.ai.GoalProgressInfo
import com.example.data.ai.GeminiBudgetService
import com.example.ui.theme.BudgetThemeType
import com.example.ui.theme.ThemeManager
import com.example.data.repository.ThemePreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AiUiState {
    object Idle : AiUiState
    object Loading : AiUiState
    data class Success(val result: BudgetAiResult) : AiUiState
    data class Error(val message: String) : AiUiState
}

sealed interface BudgetUiState {
    object Loading : BudgetUiState
    data class Success(
        val transactions: List<Transaction>,
        val totalIncome: Double,
        val totalExpense: Double,
        val totalSaving: Double,
        val availableBalance: Double,
        val recentTransactions: List<Transaction>,
        val analysisResult: BudgetAnalysisResult,
        val advancedAnalysisResult: AdvancedAnalysisResult,
        val goals: List<SavingsGoal>,
        val goalsAnalysis: List<GoalAnalysis>,
        val debts: List<Debt>
    ) : BudgetUiState
}

class BudgetViewModel(
    private val repository: TransactionRepository,
    private val context: android.content.Context
) : ViewModel() {

    // SharedPreferences setup
    private val sharedPrefs = com.example.data.security.SecureStorageManager.getEncryptedSharedPreferences(context)

    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "Joy Amedjonekou") ?: "Joy Amedjonekou")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userFirstName = MutableStateFlow(sharedPrefs.getString("user_first_name", "Joy") ?: "Joy")
    val userFirstName: StateFlow<String> = _userFirstName.asStateFlow()

    private val _userLastName = MutableStateFlow(sharedPrefs.getString("user_last_name", "Amedjonekou") ?: "Amedjonekou")
    val userLastName: StateFlow<String> = _userLastName.asStateFlow()

    private val _userDisplayName = MutableStateFlow(sharedPrefs.getString("user_display_name", "Joy") ?: "Joy")
    val userDisplayName: StateFlow<String> = _userDisplayName.asStateFlow()

    private val _studentLevel = MutableStateFlow(sharedPrefs.getString("student_level", "Alternant") ?: "Alternant")
    val studentLevel: StateFlow<String> = _studentLevel.asStateFlow()

    private val _primaryGoal = MutableStateFlow(sharedPrefs.getString("primary_goal", "Se constituer une épargne de précaution") ?: "Se constituer une épargne de précaution")
    val primaryGoal: StateFlow<String> = _primaryGoal.asStateFlow()

    private val _onboardingCompleted = MutableStateFlow(sharedPrefs.getBoolean("onboarding_completed", false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _currencySelected = MutableStateFlow(sharedPrefs.getBoolean("currency_selected", false))
    val currencySelected: StateFlow<Boolean> = _currencySelected.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(sharedPrefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        sharedPrefs.edit().putBoolean("notifications_enabled", enabled).apply()
        // WorkManager alarm initialization
        com.example.data.notification.DailyNotificationWorker.scheduleOrCancel(context, enabled)
    }

    private val _biometricsEnabled = MutableStateFlow(sharedPrefs.getBoolean("biometrics_enabled", false))
    val biometricsEnabled: StateFlow<Boolean> = _biometricsEnabled.asStateFlow()

    fun setBiometricsEnabled(enabled: Boolean) {
        _biometricsEnabled.value = enabled
        sharedPrefs.edit().putBoolean("biometrics_enabled", enabled).apply()
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    private val themeRepository = ThemePreferencesRepository(context)

    private val _currentThemeType = MutableStateFlow(BudgetThemeType.BENTO_NUIT)
    val currentThemeType: StateFlow<BudgetThemeType> = _currentThemeType.asStateFlow()

    private val _currentLanguage = MutableStateFlow(com.example.ui.localization.AppLanguageSupported.FRANCAIS)
    val currentLanguage: StateFlow<com.example.ui.localization.AppLanguageSupported> = _currentLanguage.asStateFlow()

    private val _currentCurrency = MutableStateFlow("€")
    val currentCurrency: StateFlow<String> = _currentCurrency.asStateFlow()

    private val _isAppLocked = MutableStateFlow(sharedPrefs.getBoolean("biometrics_enabled", false))
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private var backgroundTimestamp: Long? = null

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun onAppBackgrounded() {
        if (!sharedPrefs.getBoolean("biometrics_enabled", false)) {
            return
        }
        if (!_isAppLocked.value) {
            backgroundTimestamp = System.currentTimeMillis()
        }
    }

    fun onAppForegrounded(activity: androidx.appcompat.app.AppCompatActivity, onAuthRequired: () -> Unit) {
        if (!sharedPrefs.getBoolean("biometrics_enabled", false)) {
            _isAppLocked.value = false
            return
        }
        val currentTimestamp = System.currentTimeMillis()
        val bgTime = backgroundTimestamp
        
        if (_isAppLocked.value) {
            onAuthRequired()
            return
        }

        if (bgTime == null) {
            _isAppLocked.value = true
            onAuthRequired()
        } else {
            val elapsedMillis = currentTimestamp - bgTime
            if (elapsedMillis > 120_000) {
                _isAppLocked.value = true
                onAuthRequired()
            }
        }
        backgroundTimestamp = null
    }

    init {
        viewModelScope.launch {
            themeRepository.themeTypeFlow.collect { themeType ->
                _currentThemeType.value = themeType
                ThemeManager.setTheme(themeType)
            }
        }
        viewModelScope.launch {
            themeRepository.languageFlow.collect { language ->
                _currentLanguage.value = language
                com.example.presentation.ui.UiUtils.currentLanguage = language
            }
        }
        viewModelScope.launch {
            themeRepository.currencyFlow.collect { currency ->
                _currentCurrency.value = currency
                com.example.presentation.ui.UiUtils.currentCurrencySymbol = currency
            }
        }
    }

    fun selectTheme(themeType: BudgetThemeType) {
        viewModelScope.launch {
            themeRepository.saveThemeType(themeType)
            _currentThemeType.value = themeType
            ThemeManager.setTheme(themeType)
        }
    }

    fun selectLanguage(language: com.example.ui.localization.AppLanguageSupported) {
        viewModelScope.launch {
            themeRepository.saveLanguage(language)
            _currentLanguage.value = language
            com.example.presentation.ui.UiUtils.currentLanguage = language
        }
    }

    fun selectCurrency(currency: String) {
        viewModelScope.launch {
            themeRepository.saveCurrency(currency)
            _currentCurrency.value = currency
            com.example.presentation.ui.UiUtils.currentCurrencySymbol = currency
            sharedPrefs.edit().putBoolean("currency_selected", true).apply()
            _currencySelected.value = true
        }
    }

    fun updateProfile(
        firstName: String,
        lastName: String,
        displayName: String,
        level: String,
        goal: String
    ) {
        val calculatedName = if (lastName.isNotBlank()) "$firstName $lastName" else firstName
        _userFirstName.value = firstName
        _userLastName.value = lastName
        _userDisplayName.value = displayName
        _studentLevel.value = level
        _primaryGoal.value = goal
        _userName.value = calculatedName

        sharedPrefs.edit()
            .putString("user_first_name", firstName)
            .putString("user_last_name", lastName)
            .putString("user_display_name", displayName)
            .putString("student_level", level)
            .putString("primary_goal", goal)
            .putString("user_name", calculatedName)
            .apply()

        // Sync with Firestore if available under users/{uid}/profile/info or users/{uid}
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val firebaseAuthObj = com.google.firebase.auth.FirebaseAuth.getInstance()
                val firebaseUserObj = firebaseAuthObj.currentUser
                if (firebaseUserObj != null) {
                    val uid = firebaseUserObj.uid
                    val email = firebaseUserObj.email ?: ""
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    
                    val profileMap = hashMapOf<String, Any>(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "displayName" to displayName,
                        "email" to email,
                        "role" to level,
                        "mainGoal" to goal,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                    
                    // Recommended path users/{uid}/profile, typically users/{uid} with document ID as profile or merged user data
                    db.collection("users").document(uid).set(profileMap, com.google.firebase.firestore.SetOptions.merge())
                    db.collection("users").document(uid).collection("profile").document("info")
                        .set(profileMap, com.google.firebase.firestore.SetOptions.merge())
                }
            } catch (e: Exception) {
                com.example.data.security.SafeLog.e("BudgetViewModel", "Error syncing profile to Firestore")
            }
        }
    }

    fun completeOnboarding() {
        _onboardingCompleted.value = true
        sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
    }

    fun resetOnboarding() {
        _onboardingCompleted.value = false
        _currencySelected.value = false
        sharedPrefs.edit()
            .putBoolean("onboarding_completed", false)
            .putBoolean("currency_selected", false)
            .apply()
    }

    // AI Advisor Setup
    private val aiService: BudgetAiService = GeminiBudgetService(
        apiKey = com.example.BuildConfig.GEMINI_API_KEY
    )

    private val _aiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val aiState: StateFlow<AiUiState> = _aiState.asStateFlow()

    // Interactive Advice History in session memory
    private val _adviceHistory = MutableStateFlow<List<BudgetAiResult>>(emptyList())
    val adviceHistory: StateFlow<List<BudgetAiResult>> = _adviceHistory.asStateFlow()

    // Main source stream from repository
    val allTransactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allGoals: StateFlow<List<SavingsGoal>> = repository.getAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allDebts: StateFlow<List<Debt>> = repository.getAllDebts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCategoryLimits: StateFlow<List<com.example.domain.model.CategoryLimit>> = repository.getAllCategoryLimits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Live calculations for the dashboard state
    val uiState: StateFlow<BudgetUiState> = combine(allTransactions, allGoals, allDebts, currentLanguage) { list, goalsList, debtsList, lang ->
        val totalIncome = list.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
        val totalExpense = list.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }
        val totalSaving = list.filter { it.type == TransactionType.SAVING.name }.sumOf { it.amount }
        val balance = totalIncome - totalExpense - totalSaving
        val recent = list.take(5)
        val analysis = BudgetAnalyzer.analyze(list)
        val advancedAnalysis = AdvancedFinancialAnalyzer.analyze(list)
        val goalsAnalysis = GoalAnalyzer.analyzeAll(goalsList, list, lang)

        BudgetUiState.Success(
            transactions = list,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalSaving = totalSaving,
            availableBalance = balance,
            recentTransactions = recent,
            analysisResult = analysis,
            advancedAnalysisResult = advancedAnalysis,
            goals = goalsList,
            goalsAnalysis = goalsAnalysis,
            debts = debtsList
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetUiState.Loading
    )

    // State for Search and Filter in History tab
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filters: "TOUTES", "INCOME", "EXPENSE", "SAVING"
    private val _filterType = MutableStateFlow("TOUTES")
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    // Derived filtered transactions list
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        _searchQuery,
        _filterType
    ) { list, query, filter ->
        list.filter { tx ->
            // Filter by type
            val matchesType = when (filter) {
                "INCOME" -> tx.type == TransactionType.INCOME.name
                "EXPENSE" -> tx.type == TransactionType.EXPENSE.name
                "SAVING" -> tx.type == TransactionType.SAVING.name
                else -> true
            }

            // Filter by search query (label or category/displayCategory)
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                tx.label.contains(query, ignoreCase = true) ||
                tx.displayCategory.contains(query, ignoreCase = true)
            }

            matchesType && matchesQuery
        }
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterTypeChange(filter: String) {
        _filterType.value = filter
    }

    // Database Actions
    fun addTransaction(
        type: TransactionType,
        category: String,
        customCategory: String?,
        label: String,
        amount: Double,
        dateTimestamp: Long,
        associatedGoalId: Int? = null,
        associatedDebtId: Int? = null,
        savingGoalName: String? = null
    ) {
        viewModelScope.launch {
            var finalGoalId = associatedGoalId

            if (type == TransactionType.SAVING && finalGoalId == null && !savingGoalName.isNullOrBlank()) {
                val allGoalsList = repository.getAllGoals().firstOrNull() ?: emptyList()
                val existingGoal = allGoalsList.find { it.name.trim().equals(savingGoalName.trim(), ignoreCase = true) }
                if (existingGoal != null) {
                    finalGoalId = existingGoal.id
                } else {
                    val targetAmount = maxOf(amount, 500.0)
                    val newGoal = SavingsGoal(
                        name = savingGoalName.trim(),
                        targetAmount = targetAmount,
                        initialAmount = 0.0,
                        targetDateTimestamp = null
                    )
                    val insertedId = repository.insertGoal(newGoal)
                    finalGoalId = insertedId.toInt()
                }
            }

            val transaction = Transaction(
                type = type.name,
                category = category,
                customCategory = customCategory,
                label = label,
                amount = amount,
                dateTimestamp = dateTimestamp,
                associatedGoalId = finalGoalId,
                associatedDebtId = associatedDebtId
            )
            repository.insertTransaction(transaction)

            // Calculate current month category limit updates for expenses
            if (type == TransactionType.EXPENSE) {
                // Fetch recent limits
                val limits = repository.getAllCategoryLimits().firstOrNull() ?: emptyList()
                val limitForCategory = limits.find { it.category == category }
                if (limitForCategory != null && limitForCategory.limitAmount > 0.0) {
                    val allTxsList = repository.getAllTransactions().firstOrNull() ?: emptyList()
                    val currentMonthTxs = allTxsList.filter { 
                        it.type == TransactionType.EXPENSE.name && 
                        it.category == category && 
                        com.example.domain.analytics.BudgetAnalyzer.isTimestampInCurrentMonth(it.dateTimestamp)
                    }
                    val oldTotalSpent = currentMonthTxs.sumOf { it.amount }
                    val newTotalSpent = oldTotalSpent + amount
                    val limitVal = limitForCategory.limitAmount

                    val oldRatio = oldTotalSpent / limitVal
                    val newRatio = newTotalSpent / limitVal

                    val oldPercent = (oldRatio * 100).toInt()
                    val newPercent = (newRatio * 100).toInt()

                    // Check if we crossed 80% or 100% threshold newly with this transaction
                    val shouldWarn = when {
                        newPercent >= 100 && oldPercent < 100 -> true
                        newPercent >= 80 && oldPercent < 80 -> true
                        else -> false
                    }

                    if (shouldWarn) {
                        com.example.data.notification.LimitNotificationHelper.showLimitNotification(
                            context = context,
                            category = category,
                            amount = newTotalSpent,
                            limit = limitVal,
                            percentage = newPercent,
                            lang = currentLanguage.value
                        )
                    }
                }
            }
        }
    }

    fun saveCategoryLimit(category: String, limitAmount: Double) {
        viewModelScope.launch {
            val limit = com.example.domain.model.CategoryLimit(category = category, limitAmount = limitAmount)
            repository.insertCategoryLimit(limit)
        }
    }

    fun deleteCategoryLimit(limit: com.example.domain.model.CategoryLimit) {
        viewModelScope.launch {
            repository.deleteCategoryLimit(limit)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Savings Goals actions
    fun addGoal(
        name: String,
        targetAmount: Double,
        initialAmount: Double = 0.0,
        targetDateTimestamp: Long? = null
    ) {
        if (name.isBlank() || targetAmount <= 0.0) {
            com.example.data.security.SafeLog.e("BudgetViewModel", "Failed to add goal: name is empty or targetAmount <= 0")
            return
        }
        viewModelScope.launch {
            val goal = SavingsGoal(
                name = name,
                targetAmount = targetAmount,
                initialAmount = initialAmount,
                targetDateTimestamp = targetDateTimestamp
            )
            repository.insertGoal(goal)
        }
    }

    fun updateGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.insertGoal(goal)
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    init {
        viewModelScope.launch {
            // Déclenche une analyse dès le premier chargement réussi des données
            uiState.first { it is BudgetUiState.Success }
            kotlinx.coroutines.delay(800)
            generateAiAdvice()
        }
    }

    fun generateAiAdvice() {
        val currentState = uiState.value as? BudgetUiState.Success ?: return
        viewModelScope.launch {
            _aiState.value = AiUiState.Loading
            try {
                val goalsInfo = currentState.goalsAnalysis.map { analysis ->
                    GoalProgressInfo(
                        id = analysis.goal.id,
                        name = analysis.goal.name,
                        targetAmount = analysis.goal.targetAmount,
                        currentAmount = analysis.currentCollected,
                        progressPercent = analysis.progressPercentage,
                        remainingAmount = analysis.remainingAmount,
                        projectionText = analysis.projectionMessage
                    )
                }

                val categoriesMap = currentState.advancedAnalysisResult.categorizedExpenses.associate {
                    it.category to it.totalAmount
                }

                val result = aiService.generateAdvice(
                    totalIncome = currentState.totalIncome,
                    totalExpense = currentState.totalExpense,
                    totalSaving = currentState.totalSaving,
                    recentExpensesByCategory = categoriesMap,
                    goalsProgress = goalsInfo,
                    selectedLanguage = currentLanguage.value
                )

                // Post-process to inject Category Limit warnings into Coach AI advice stream
                val activeLimits = allCategoryLimits.value
                val transactionsList = allTransactions.value
                val expenseTxs = transactionsList.filter { it.type == TransactionType.EXPENSE.name }
                
                val limitAdvices = mutableListOf<String>()
                activeLimits.forEach { limit ->
                    val catTxs = expenseTxs.filter { 
                        it.category == limit.category && 
                        com.example.domain.analytics.BudgetAnalyzer.isTimestampInCurrentMonth(it.dateTimestamp)
                    }
                    val totalSpent = catTxs.sumOf { it.amount }
                    if (limit.limitAmount > 0.0) {
                        val percentage = (totalSpent / limit.limitAmount * 100).toInt()
                        if (percentage >= 100) {
                            val localizedMsg = when (currentLanguage.value) {
                                com.example.ui.localization.AppLanguageSupported.FRANCAIS ->
                                    "⚠️ Limite dépassée pour la catégorie \"${limit.category}\" : ${percentage}% (${com.example.presentation.ui.UiUtils.formatCurrency(totalSpent)} / ${com.example.presentation.ui.UiUtils.formatCurrency(limit.limitAmount)})"
                                else ->
                                    "⚠️ Limit exceeded for category \"${limit.category}\": ${percentage}% (${com.example.presentation.ui.UiUtils.formatCurrency(totalSpent)} / ${com.example.presentation.ui.UiUtils.formatCurrency(limit.limitAmount)})"
                            }
                            limitAdvices.add(localizedMsg)
                        } else if (percentage >= 80) {
                            val localizedMsg = when (currentLanguage.value) {
                                com.example.ui.localization.AppLanguageSupported.FRANCAIS ->
                                    "🔔 Seuil d'alerte de 80% approché pour la catégorie \"${limit.category}\" : ${percentage}% (${com.example.presentation.ui.UiUtils.formatCurrency(totalSpent)} / ${com.example.presentation.ui.UiUtils.formatCurrency(limit.limitAmount)})"
                                else ->
                                    "🔔 Warning threshold of 80% approached for category \"${limit.category}\": ${percentage}% (${com.example.presentation.ui.UiUtils.formatCurrency(totalSpent)} / ${com.example.presentation.ui.UiUtils.formatCurrency(limit.limitAmount)})"
                            }
                            limitAdvices.add(localizedMsg)
                        }
                    }
                }

                val finalResult = if (limitAdvices.isNotEmpty()) {
                    result.copy(
                        adviceList = limitAdvices + result.adviceList
                    )
                } else {
                    result
                }

                _aiState.value = AiUiState.Success(finalResult)

                // Enregistrement dans l'historique de session
                val currentHistory = _adviceHistory.value.toMutableList()
                if (currentHistory.none { it.summary == result.summary }) {
                    currentHistory.add(0, result)
                    _adviceHistory.value = currentHistory.take(10)
                }
            } catch (e: Exception) {
                _aiState.value = AiUiState.Error("Échec de l'obtention des conseils de Joy AI")
            }
        }
    }

    fun deleteGoalById(id: Int) {
        viewModelScope.launch {
            repository.deleteGoalById(id)
        }
    }

    fun clearAllTransactions() {
        viewModelScope.launch {
            repository.clearAllTransactions()
        }
    }

    fun clearAllGoals() {
        viewModelScope.launch {
            repository.clearAllGoals()
        }
    }

    fun clearAllDebts() {
        viewModelScope.launch {
            repository.clearAllDebts()
        }
    }

    fun updateTransaction(
        id: Int,
        type: TransactionType,
        category: String,
        customCategory: String?,
        label: String,
        amount: Double,
        dateTimestamp: Long,
        associatedGoalId: Int? = null,
        associatedDebtId: Int? = null
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                id = id,
                type = type.name,
                category = category,
                customCategory = customCategory,
                label = label,
                amount = amount,
                dateTimestamp = dateTimestamp,
                associatedGoalId = associatedGoalId,
                associatedDebtId = associatedDebtId
            )
            repository.insertTransaction(transaction)
        }
    }

    fun addDebt(name: String, totalAmount: Double, reimbursedAmount: Double = 0.0, dateTimestamp: Long? = null) {
        if (name.isBlank() || totalAmount <= 0.0) {
            com.example.data.security.SafeLog.e("BudgetViewModel", "Failed to add debt: name is empty or totalAmount <= 0")
            return
        }
        viewModelScope.launch {
            val debt = Debt(
                name = name,
                totalAmount = totalAmount,
                reimbursedAmount = reimbursedAmount,
                dateTimestamp = dateTimestamp ?: System.currentTimeMillis()
            )
            repository.insertDebt(debt)
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    fun deleteDebtById(id: Int) {
        viewModelScope.launch {
            repository.deleteDebtById(id)
        }
    }

    fun addDebtRepayment(debtId: Int, amount: Double, label: String, dateTimestamp: Long) {
        viewModelScope.launch {
            val transaction = Transaction(
                type = TransactionType.EXPENSE.name,
                category = "Remboursement de dette",
                label = label,
                amount = amount,
                dateTimestamp = dateTimestamp,
                associatedDebtId = debtId
            )
            repository.insertTransaction(transaction)
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            repository.syncWithCloud()
        }
    }
}

class BudgetViewModelFactory(
    private val repository: TransactionRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
