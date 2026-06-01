// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.presentation.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.analytics.AdvancedAnalysisResult
import com.example.domain.analytics.CategorizedExpense
import com.example.domain.analytics.MonthlySnapshot
import com.example.presentation.viewmodel.BudgetUiState
import com.example.presentation.viewmodel.BudgetViewModel
import com.example.domain.model.SavingsGoal
import com.example.domain.analytics.GoalAnalysis
import com.example.domain.analytics.GoalAnalyzer
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisTab(
    uiState: BudgetUiState,
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val exportViewModel: com.example.presentation.viewmodel.ExportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val exportUiState by exportViewModel.exportUiState.collectAsState()

    val headerTitle = when (currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Analyse"
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Analysis"
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Análisis"
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Analyse"
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Analisi"
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Análise"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "分析"
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "分析"
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "분석"
        com.example.ui.localization.AppLanguageSupported.ARABIC -> "التحليل"
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Анализ"
        else -> "Analysis"
    }

    val financesSeparator = when (currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Mes finances"
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "My Finances"
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Mis finanzas"
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Meine Finanzen"
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Le mie finanze"
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Minhas finanças"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "我的财务"
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "自己の財務"
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "내 재정"
        com.example.ui.localization.AppLanguageSupported.ARABIC -> "شؤوني المالية"
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Мои финансы"
        else -> "My Finances"
    }

    val goalsSectionTitle = when (currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Objectifs"
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Goals"
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Objetivos"
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Ziele"
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Obiettivi"
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Objetivos"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "目标"
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "目標"
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "목표"
        com.example.ui.localization.AppLanguageSupported.ARABIC -> "الأهداف"
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Цели"
        else -> "Goals"
    }

    val debtsSectionTitle = when (currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Dettes"
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Debts"
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Deudas"
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Schulden"
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Debiti"
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Dívidas"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "债务"
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "負債"
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "부채"
        com.example.ui.localization.AppLanguageSupported.ARABIC -> "الديون"
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Долги"
        else -> "Debts"
    }

    val simulationSectionTitle = when (currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Simulation"
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Simulation"
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Simulación"
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Simulation"
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Simulazione"
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Simulação"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "模拟"
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "シミュレーション"
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "시뮬레이션"
        com.example.ui.localization.AppLanguageSupported.ARABIC -> "المحاكاة"
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Симуляция"
        else -> "Simulation"
    }

    when (uiState) {
        is BudgetUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
        is BudgetUiState.Success -> {
            val result = uiState.advancedAnalysisResult

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("analysis_scroll_view"),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = headerTitle,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue,
                                fontSize = 24.sp,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                }

                if (uiState.transactions.isEmpty()) {
                    item {
                        EmptyStateAnalysis()
                    }
                } else {
                    // Top Bento Row: Dominant Category & Savings Detail (2 per line style)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DominantCategoryCard(
                                category = result.dominantCategory,
                                amount = result.dominantCategoryAmount,
                                modifier = Modifier.weight(1f)
                            )
                            SavingQualityCard(
                                totalSaving = result.savingDetail.totalSaving,
                                savingRatio = result.savingDetail.savingRatio,
                                message = result.savingDetail.ratingMessage,
                                isGood = result.savingDetail.isGood,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Bento Section: Monthly Evolution Chart
                    item {
                        MonthlyHistoryBentoCard(
                            snapshots = result.monthlyHistory,
                            currentLanguage = currentLanguage
                        )
                    }

                    // Bento Section: Categories Distribution Chart
                    item {
                        CategoryDistributionBentoCard(categorizedExpenses = result.categorizedExpenses)
                    }
                }

                // Separator "Mes finances"
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = financesSeparator,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite,
                                fontSize = 18.sp,
                                letterSpacing = (-0.3).sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), thickness = 1.dp)
                    }
                }

                // --- SECTION OBJECTIFS D'ÉPARGNE ---
                item {
                    var showCreateGoalDialog by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = goalsSectionTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ColorSaving,
                                    fontSize = 20.sp
                                )
                            )
                        }

                        Button(
                            onClick = { showCreateGoalDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSaving),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = t.createGoalDialogTitle,
                                tint = DarkBackground,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = t.newGoalBtn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkBackground,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (showCreateGoalDialog) {
                        CreateSavingsGoalDialog(
                            onDismiss = { showCreateGoalDialog = false },
                            onConfirm = { name, target, initial, targetDate ->
                                viewModel.addGoal(name, target, initial, targetDate)
                                showCreateGoalDialog = false
                            }
                        )
                    }
                }

                // Listing active goals
                if (uiState.goalsAnalysis.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = t.noGoalsMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextMuted,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.goalsAnalysis) { analysis ->
                        SavingsGoalCardItem(
                            analysis = analysis,
                            onDelete = { viewModel.deleteGoal(analysis.goal) },
                            onEdit = { name, target, initial, targetDate ->
                                val updatedGoal = analysis.goal.copy(
                                    name = name,
                                    targetAmount = target,
                                    initialAmount = initial,
                                    targetDateTimestamp = targetDate
                                )
                                viewModel.updateGoal(updatedGoal)
                            }
                        )
                    }
                }

                // --- SECTION DETTES & PRÊTS ---
                item {
                    var showAddDebtDialog by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = debtsSectionTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ColorExpense,
                                    fontSize = 20.sp
                                )
                            )
                        }

                        Button(
                            onClick = { showAddDebtDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorExpense),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("add_debt_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = t.newDebtDialogTitle,
                                tint = TextWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = t.newGoalBtn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    if (showAddDebtDialog) {
                        var debtName by remember { mutableStateOf("") }
                        var debtTotal by remember { mutableStateOf("") }

                        AlertDialog(
                            onDismissRequest = { showAddDebtDialog = false },
                            title = { Text(t.newDebtDialogTitle, color = TextWhite, fontWeight = FontWeight.Bold) },
                            text = {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = debtName,
                                        onValueChange = { debtName = it },
                                        label = { Text(t.debtNameInputLabel) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite,
                                            focusedBorderColor = PrimaryBlue,
                                            unfocusedBorderColor = BorderColor
                                        ),
                                        modifier = Modifier.testTag("add_debt_name_input")
                                    )

                                    OutlinedTextField(
                                        value = debtTotal,
                                        onValueChange = { debtTotal = it },
                                        label = { Text(t.debtTotalInputLabel) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite,
                                            focusedBorderColor = PrimaryBlue,
                                            unfocusedBorderColor = BorderColor
                                        ),
                                        modifier = Modifier.testTag("add_debt_total_input")
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val tot = debtTotal.toDoubleOrNull()
                                        if (debtName.isNotBlank() && tot != null) {
                                            viewModel.addDebt(debtName.trim(), tot)
                                            showAddDebtDialog = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Text(t.createBtn, color = DarkBackground)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAddDebtDialog = false }) {
                                    Text(t.cancelBtn, color = TextSecondary)
                                }
                            },
                            containerColor = DarkSurface,
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }

                if (uiState.debts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, BorderColor)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = t.noDebtsMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextMuted,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.debts) { debt ->
                        DebtCardItem(
                            debt = debt,
                            viewModel = viewModel
                        )
                    }
                }

                // --- SECTION SIMULATION DE BUDGET ---
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = simulationSectionTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryBlue,
                                    fontSize = 20.sp
                                )
                            )
                        }
                    }
                }

                item {
                    BudgetSimulationCard(currentLanguage = currentLanguage)
                }
            }

            ExportStatusDialog(
                exportUiState = exportUiState,
                onDismiss = { exportViewModel.resetState() }
            )
        }
    }
}

/**
 * Bento Block for Dominant Category
 */
@Composable
fun DominantCategoryCard(
    category: String?,
    amount: Double,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val rawCategory = category ?: t.categoryNoGoals
    val displayCategory = if (category != null) UiUtils.getLocalizedCategory(rawCategory, t) else rawCategory

    val labelText = when (UiUtils.currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "DÉPENSE PRINCIPALE"
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "MAIN EXPENSE"
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "GASTO PRINCIPAL"
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "HAUPTAUSGABE"
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "SPESA PRINCIPALE"
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "DESPESA PRINCIPAL"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "主要支出"
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "主な支出"
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "주요 지출"
        com.example.ui.localization.AppLanguageSupported.ARABIC -> "النفقات الرئيسية"
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "ОСНОВНАЯ СТАТЬЯ РАСХОДОВ"
        else -> "MAIN EXPENSE"
    }

    Card(
        modifier = modifier.heightIn(min = 130.dp).fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(ColorExpenseBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Leaderboard,
                    contentDescription = null,
                    tint = ColorExpense,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = labelText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 9.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = displayCategory,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = TextWhite,
                        lineHeight = 18.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (amount > 0.0) {
                    Text(
                        text = UiUtils.formatCurrency(amount),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ColorExpense,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

/**
 * Bento Block for Savings summary & prompt message
 */
@Composable
fun SavingQualityCard(
    totalSaving: Double,
    savingRatio: Double,
    message: String,
    isGood: Boolean,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val accentColor = if (isGood) ColorIncome else ColorSaving
    val displayMessage = if (isGood) t.ratingMessageGood else t.ratingMessageImprove

    Card(
        modifier = modifier.heightIn(min = 130.dp).fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = t.savingQualityLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 9.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = displayMessage,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    ),
                    maxLines = 2,
                    lineHeight = 15.sp,
                     overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(t.savingRatioTemplate, savingRatio.toInt(), UiUtils.formatCurrency(totalSaving)),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Monthly Evolution Bento Card (Multi Column Rounded Chart)
 */
@Composable
fun MonthlyHistoryBentoCard(
    snapshots: List<MonthlySnapshot>,
    currentLanguage: com.example.ui.localization.AppLanguageSupported,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Stats Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = t.monthlyTrendTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = t.monthlyTrendSub,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                // Legend row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(t.legendIncome, ColorIncome)
                    LegendItem(t.legendExpense, ColorExpense)
                    LegendItem(t.legendSaving, PrimaryBlue)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (snapshots.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t.noHistoryData,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
            } else {
                // Find global max to scale columns cleanly
                val maxVal = snapshots.flatMap { listOf(it.totalIncome, it.totalExpense, it.totalSaving) }
                    .maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    snapshots.forEach { snap ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val animIncome = animateFloatAsState(
                                    targetValue = (snap.totalIncome / maxVal).toFloat(),
                                    animationSpec = tween(durationMillis = 600),
                                    label = "IncomeBar"
                                )
                                val animExpense = animateFloatAsState(
                                    targetValue = (snap.totalExpense / maxVal).toFloat(),
                                    animationSpec = tween(durationMillis = 600),
                                    label = "ExpenseBar"
                                )
                                val animSaving = animateFloatAsState(
                                    targetValue = (snap.totalSaving / maxVal).toFloat(),
                                    animationSpec = tween(durationMillis = 600),
                                    label = "SavingBar"
                                )

                                // Income Bar (Green)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(animIncome.value.coerceIn(0.01f, 1f))
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(ColorIncome)
                                )
                                // Expense Bar (Red)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(animExpense.value.coerceIn(0.01f, 1f))
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(ColorExpense)
                                )
                                // Saving Bar (Violet)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(animSaving.value.coerceIn(0.01f, 1f))
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(PrimaryBlue)
                                )
                             }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = UiUtils.formatMonthYear(snap.year, snap.month, currentLanguage),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Categories Distribution Bento Card (Contains Donut representation and progress list)
 */
@Composable
fun CategoryDistributionBentoCard(
    categorizedExpenses: List<CategorizedExpense>,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = t.expenseDistributionTitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = PrimaryBlue,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = t.expenseDistributionSub,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (categorizedExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t.noExpensesMessage,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Custom Donut Canvas representing distribution
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            val strokeWidth = 14.dp.toPx()

                            // Set distinct colors for top 4 sectors, then gray for the rest
                            categorizedExpenses.forEachIndexed { index, item ->
                                val sweep = (item.percentage.toFloat() / 100f) * 360f
                                if (sweep > 0f) {
                                    val color = getCategoryColor(index)
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                    startAngle += sweep
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = t.expense,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary
                                )
                            )
                        }
                    }

                    // Legend list
                    Column(
                        modifier = Modifier.weight(1.8f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorizedExpenses.take(4).forEachIndexed { i, cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(getCategoryColor(i))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = UiUtils.getLocalizedCategory(cat.category, t),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "${cat.percentage.toInt()}%",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom categorized visual list with progress lanes
                Text(
                    text = t.categoriesDetailTitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categorizedExpenses.forEachIndexed { index, item ->
                        val animWidth = animateFloatAsState(
                            targetValue = (item.percentage / 100.0).toFloat(),
                            animationSpec = tween(durationMillis = 500),
                            label = "CategoryLane"
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = UiUtils.getLocalizedCategory(item.category, t),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = "${UiUtils.formatCurrency(item.totalAmount)} (${item.percentage.toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))

                            // Horizontal progress box matching Bento aesthetic
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(BorderColor)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animWidth.value)
                                        .fillMaxHeight()
                                        .background(getCategoryColor(index))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Small helper to get color mapping for category list rendering
 */
private fun getCategoryColor(index: Int): Color {
    return when (index % 5) {
        0 -> ColorExpense // Rose
        1 -> PrimaryBlue // Violet
        2 -> ColorSaving // Blue
        3 -> ColorIncome // Green
        else -> Color(0xFFFFB74D) // Amber Accent
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
fun EmptyStateAnalysis() {
    val t = com.example.ui.localization.LocalAppStrings.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Assessment,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = t.emptyAnalysisTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = t.emptyAnalysisDesc,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                ),
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Individual Savings Goal Item matching the beautiful Bento look
 */
@Composable
fun SavingsGoalCardItem(
    analysis: GoalAnalysis,
    onDelete: () -> Unit,
    onEdit: (name: String, target: Double, initial: Double, targetDate: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val g = analysis.goal
    val percentInt = analysis.progressPercentage.toInt()
    
    var showEditDialog by remember { mutableStateOf(false) }

    val animatedPercent by animateFloatAsState(
        targetValue = percentInt / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "SavingsGoalItemProgressAnim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("savings_goal_item_${g.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            if (showEditDialog) {
                EditSavingsGoalDialog(
                    goal = g,
                    onDismiss = { showEditDialog = false },
                    onConfirm = { name, target, initial, targetDate ->
                        onEdit(name, target, initial, targetDate)
                        showEditDialog = false
                    }
                )
            }

            // First Row: Header, Name and actions (Edit, Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(ColorSaving.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = ColorSaving,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = g.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite,
                                fontSize = 16.sp
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = String.format(t.savedLabelTemplate, UiUtils.formatCurrency(analysis.currentCollected), UiUtils.formatCurrency(g.targetAmount)),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit goal",
                            tint = ColorSaving,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = t.deleteGoalDesc,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progression lane
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${UiUtils.formatCurrency(analysis.remainingAmount)} ${t.labelRemaining}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "$percentInt%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ColorSaving,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BorderColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedPercent.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(ColorSaving)
                    )
                }
            }

            // Target Date
            g.targetDateTimestamp?.let { date ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(t.targetDeadline, UiUtils.formatDate(date)),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Projection analysis message
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ColorSaving.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = ColorSaving,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = analysis.projectionMessage,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = ColorSaving,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // Insight message (only if present)
            analysis.insightMessage?.let { insight ->
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 $insight",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    )
                }
            }
        }
    }
}

/**
 * Custom modern Dialog to create a savings goal simply
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSavingsGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, target: Double, initial: Double, targetDate: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var initialText by remember { mutableStateOf("") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateTimestamp by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("create_goal_dialog"),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = t.createGoalDialogTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorSaving,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = t.createGoalDialogSub,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Field 1: Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(t.goalNameInputLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ColorSaving,
                            focusedLabelColor = ColorSaving,
                            cursorColor = ColorSaving,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 2: Target Amount
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text(t.goalTargetInputLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ColorSaving,
                            focusedLabelColor = ColorSaving,
                            cursorColor = ColorSaving,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_target")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 3: Initial existing amount
                    OutlinedTextField(
                        value = initialText,
                        onValueChange = { initialText = it },
                        label = { Text(t.goalInitialInputLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ColorSaving,
                            focusedLabelColor = ColorSaving,
                            cursorColor = ColorSaving,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_initial")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 4: Optional target Date selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = ColorSaving,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (selectedDateTimestamp != null) {
                                    UiUtils.formatShortDate(selectedDateTimestamp!!)
                                } else {
                                    t.goalDeadlineInputLabel
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (selectedDateTimestamp != null) TextWhite else TextSecondary
                                )
                            )
                        }

                        if (selectedDateTimestamp != null) {
                            Text(
                                text = t.clearBtn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ColorExpense,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.clickable { selectedDateTimestamp = null }
                            )
                        } else {
                            Text(
                                text = t.chooseBtn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ColorSaving,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Calendar Dialog
                    if (showDatePicker) {
                        BudgetDatePickerDialog(
                            initialDateTimestamp = selectedDateTimestamp ?: System.currentTimeMillis(),
                            onDateSelected = {
                                selectedDateTimestamp = it
                                showDatePicker = false
                            },
                            onDismissRequest = { showDatePicker = false },
                            language = UiUtils.currentLanguage
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text(t.cancelBtn, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                val targetVal = targetText.toDoubleOrNull() ?: 0.0
                                val initialVal = initialText.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && targetVal > 0.0) {
                                    onConfirm(name.trim(), targetVal, initialVal, selectedDateTimestamp)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSaving),
                            enabled = name.isNotBlank() && (targetText.toDoubleOrNull() ?: 0.0) > 0.0,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = t.createBtn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkBackground,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSavingsGoalDialog(
    goal: SavingsGoal,
    onDismiss: () -> Unit,
    onConfirm: (name: String, target: Double, initial: Double, targetDate: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    var name by remember { mutableStateOf(goal.name) }
    var targetText by remember { mutableStateOf(goal.targetAmount.toString()) }
    var initialText by remember { mutableStateOf(goal.initialAmount.toString()) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateTimestamp by remember { mutableStateOf<Long?>(goal.targetDateTimestamp) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("edit_goal_dialog"),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(28.dp),
                color = DarkSurface,
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = when (UiUtils.currentLanguage) {
                            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Modifier l'objectif"
                            else -> "Edit Goal"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorSaving,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = when (UiUtils.currentLanguage) {
                            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Ajustez les détails de votre objectif d'épargne"
                            else -> "Adjust the details of your savings goal"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Field 1: Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(t.goalNameInputLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ColorSaving,
                            focusedLabelColor = ColorSaving,
                            cursorColor = ColorSaving,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 2: Target Amount
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it },
                        label = { Text(t.goalTargetInputLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ColorSaving,
                            focusedLabelColor = ColorSaving,
                            cursorColor = ColorSaving,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_target")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 3: Initial amount
                    OutlinedTextField(
                        value = initialText,
                        onValueChange = { initialText = it },
                        label = { Text(t.goalInitialInputLabel) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = ColorSaving,
                            focusedLabelColor = ColorSaving,
                            cursorColor = ColorSaving,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_initial")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Field 4: Optional target Date selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = ColorSaving,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (selectedDateTimestamp != null) {
                                    UiUtils.formatShortDate(selectedDateTimestamp!!)
                                } else {
                                    t.goalDeadlineInputLabel
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (selectedDateTimestamp != null) TextWhite else TextSecondary
                                )
                            )
                        }

                        if (selectedDateTimestamp != null) {
                            Text(
                                text = t.clearBtn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ColorExpense,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.clickable { selectedDateTimestamp = null }
                            )
                        } else {
                            Text(
                                text = t.chooseBtn,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = ColorSaving,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Calendar Dialog
                    if (showDatePicker) {
                        BudgetDatePickerDialog(
                            initialDateTimestamp = selectedDateTimestamp ?: System.currentTimeMillis(),
                            onDateSelected = {
                                selectedDateTimestamp = it
                                showDatePicker = false
                            },
                            onDismissRequest = { showDatePicker = false },
                            language = UiUtils.currentLanguage
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text(t.cancelBtn, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                val targetVal = targetText.toDoubleOrNull() ?: 0.0
                                val initialVal = initialText.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && targetVal > 0.0) {
                                    onConfirm(name.trim(), targetVal, initialVal, selectedDateTimestamp)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSaving),
                            enabled = name.isNotBlank() && (targetText.toDoubleOrNull() ?: 0.0) > 0.0,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when (UiUtils.currentLanguage) {
                                    com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Enregistrer"
                                    else -> "Save"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = DarkBackground,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun DebtCardItem(
    debt: com.example.domain.model.Debt,
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val context = androidx.compose.ui.platform.LocalContext.current
    var showRepayDialog by remember { mutableStateOf(false) }
    
    val progress = if (debt.totalAmount > 0) (debt.reimbursedAmount / debt.totalAmount).toFloat().coerceIn(0f, 1f) else 0f
    val progressPercent = (progress * 100).toInt()
    val remaining = (debt.totalAmount - debt.reimbursedAmount).coerceAtLeast(0.0)

    val animatedPercent by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800),
        label = "DebtItemProgressAnim"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("debt_item_${debt.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // First Row: Header, Name and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(ColorExpense.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = ColorExpense,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = debt.name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite,
                                fontSize = 16.sp
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = String.format(t.repaidLabelTemplate, UiUtils.formatCurrency(debt.reimbursedAmount), UiUtils.formatCurrency(debt.totalAmount)),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showRepayDialog = true },
                        modifier = Modifier.size(32.dp).background(PrimaryBlue.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = t.repayDebtDesc,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.deleteDebt(debt) },
                        modifier = Modifier.size(32.dp).background(ColorExpense.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = t.deleteDebtDesc,
                            tint = ColorExpense,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progression lane
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${UiUtils.formatCurrency(remaining)} ${t.labelRemaining}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ColorExpense,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BorderColor)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedPercent.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(ColorExpense)
                    )
                }
            }
        }
    }

    if (showRepayDialog) {
        var repayAmount by remember { mutableStateOf("") }
        var repayLabel by remember { mutableStateOf(String.format(t.repayLabelTemplateForm, debt.name)) }

        AlertDialog(
            onDismissRequest = { showRepayDialog = false },
            title = { Text(t.repayDebtDialogTitle, color = TextWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = repayAmount,
                        onValueChange = { repayAmount = it },
                        label = { Text(t.repayAmountInputLabel) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.testTag("repay_amount_input")
                    )

                    OutlinedTextField(
                        value = repayLabel,
                        onValueChange = { repayLabel = it },
                        label = { Text(t.repayDescriptionInputLabel) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderColor
                        ),
                        modifier = Modifier.testTag("repay_description_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = repayAmount.toDoubleOrNull()
                        if (amt != null && amt > 0.0) {
                            viewModel.addDebtRepayment(debt.id, amt, repayLabel, System.currentTimeMillis())
                            showRepayDialog = false
                            android.widget.Toast.makeText(context, t.toastRepaymentSaved, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(t.repayBtn, color = DarkBackground)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepayDialog = false }) {
                    Text(t.cancelBtn, color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

data class SimulatedExpense(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val amount: Double
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetSimulationCard(
    currentLanguage: com.example.ui.localization.AppLanguageSupported,
    modifier: Modifier = Modifier
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val simulateForecastTitleSimp = when (currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Simuler budget"
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Simulate budget"
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Simular presupuesto"
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Budget simulieren"
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Simula budget"
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Simular orçamento"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "模拟预算"
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "予算シミュレート"
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "예산 시뮬레이션"
        com.example.ui.localization.AppLanguageSupported.ARABIC -> "محاكاة الميزانية"
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Симулировать бюджет"
        else -> "Simulate budget"
    }

    val simulateForecastShowSimp = when (currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Estimer finances"
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Estimate finances"
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Estimar finanzas"
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Finanzen schätzen"
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Stima finanze"
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Estimar finanças"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "估算财务"
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "財務の見積もり"
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "재정 추정"
        com.example.ui.localization.AppLanguageSupported.ARABIC -> "تقدير الشؤون المالية"
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Оценить финансы"
        else -> "Estimate finances"
    }

    val simulateForecastHideSimp = when (currentLanguage) {
        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Masquer le simulateur"
        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Hide simulator"
        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Ocultar simulador"
        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Simulator ausblenden"
        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Nascondi simulatore"
        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Ocultar simulador"
        com.example.ui.localization.AppLanguageSupported.CHINESE -> "隐藏模拟器"
        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "シミュレータを非表示"
        com.example.ui.localization.AppLanguageSupported.KOREAN -> "시뮬레이터 숨기기"
        com.example.ui.localization.AppLanguageSupported.ARABIC -> "إخفاء المحاكي"
        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Скрыть симулятор"
        else -> "Hide simulator"
    }

    var isExpanded by remember { mutableStateOf(false) }
    var simIncomeText by remember { mutableStateOf("0") }
    var simSavingText by remember { mutableStateOf("0") }
    
    var simExpensesList by remember {
        mutableStateOf(emptyList<SimulatedExpense>())
    }

    var newExpenseName by remember { mutableStateOf("") }
    var newExpenseAmount by remember { mutableStateOf("") }

    // Computations
    val income = simIncomeText.toDoubleOrNull() ?: 0.0
    val saving = simSavingText.toDoubleOrNull() ?: 0.0
    val totalExpenses = simExpensesList.sumOf { it.amount }
    val balance = income - totalExpenses - saving

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_simulation_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Clickable Header to Expand/Collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PrimaryBlue.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = simulateForecastTitleSimp,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (isExpanded) simulateForecastHideSimp else simulateForecastShowSimp,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) simulateForecastHideSimp else simulateForecastShowSimp,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                HorizontalDivider(color = BorderColor.copy(alpha = 0.3f), thickness = 1.dp)
            // INFO BOX AND RESET TRIGGER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = t.tempDataMessage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }

                TextButton(
                    onClick = {
                        simIncomeText = "0"
                        simSavingText = "0"
                        simExpensesList = emptyList()
                        newExpenseName = ""
                        newExpenseAmount = ""
                    },
                    modifier = Modifier.height(32.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = t.resetBtn,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = t.resetBtn,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = PrimaryBlue,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // INCOME AND SAVING ESTIMATIONS SIDE BY SIDE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = simIncomeText,
                    onValueChange = { simIncomeText = it },
                    label = { Text(t.estimatedIncome) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ColorIncome,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = ColorIncome,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sim_income_input")
                )

                OutlinedTextField(
                    value = simSavingText,
                    onValueChange = { simSavingText = it },
                    label = { Text(t.estimatedSaving) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ColorSaving,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = ColorSaving,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sim_saving_input")
                )
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f), thickness = 1.dp)

            // ESTIMATED EXPENSES ROW LIST AND ADD ACTION
            Text(
                text = t.estimatedExpenses,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newExpenseName,
                    onValueChange = { newExpenseName = it },
                    label = { Text(t.estimatedExpenseLabel) },
                    placeholder = { Text(t.estimatedExpensePlaceholder) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ColorExpense,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = ColorExpense,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("sim_new_expense_name")
                )

                OutlinedTextField(
                    value = newExpenseAmount,
                    onValueChange = { newExpenseAmount = it },
                    label = { Text(t.estimatedExpenseAmount) },
                    placeholder = { Text(t.estimatedExpenseAmountPlaceholder) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = ColorExpense,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = ColorExpense,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sim_new_expense_amount")
                )

                IconButton(
                    onClick = {
                        val amt = newExpenseAmount.toDoubleOrNull()
                        if (newExpenseName.isNotBlank() && amt != null && amt > 0.0) {
                            simExpensesList = simExpensesList + SimulatedExpense(
                                name = newExpenseName.trim(),
                                amount = amt
                            )
                            newExpenseName = ""
                            newExpenseAmount = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(ColorExpense.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = t.addSimulated,
                        tint = ColorExpense
                    )
                }
            }

            // EXPENSES RENDER COLUMN
            if (simExpensesList.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground, RoundedCornerShape(16.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    simExpensesList.forEach { exp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurface, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exp.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                )
                                Text(
                                    text = UiUtils.formatCurrency(exp.amount),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ColorExpense,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }

                            IconButton(
                                onClick = {
                                    simExpensesList = simExpensesList.filter { it.id != exp.id }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = t.deleteDebtDesc,
                                    tint = ColorExpense,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBackground, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t.noSimulatedExpenses,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        textAlign = TextAlign.Center
                    )
                }
            }

            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f), thickness = 1.dp)

            // RESULTS BENTO DISPLAY
            Text(
                text = t.forecastReportTitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextWhite,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            )

            val balanceColor = if (balance >= 0) ColorSaving else ColorExpense
            val balanceBgColor = if (balance >= 0) ColorSaving.copy(alpha = 0.12f) else ColorExpense.copy(alpha = 0.12f)
            val balanceBorder = if (balance >= 0) ColorSaving.copy(alpha = 0.3f) else ColorExpense.copy(alpha = 0.3f)

            // HERO BALANCE PREVIEW BLOCK
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = balanceBgColor),
                border = BorderStroke(1.dp, balanceBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = t.estimatedMonthlyBalance,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = UiUtils.formatCurrency(balance),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = balanceColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 26.sp
                            )
                        )
                    }
                }
            }

            // REVENUES, EXPENSES, SAVINGS TOTAL BARS SIDE-BY-SIDE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Revenus
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(DarkBackground, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = t.income,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = UiUtils.formatCurrency(income),
                        style = MaterialTheme.typography.bodyMedium.copy(color = ColorIncome, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Dépenses
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(DarkBackground, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = t.expense,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = UiUtils.formatCurrency(totalExpenses),
                        style = MaterialTheme.typography.bodyMedium.copy(color = ColorExpense, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Épargne
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(DarkBackground, RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = t.saving,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 10.sp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = UiUtils.formatCurrency(saving),
                        style = MaterialTheme.typography.bodyMedium.copy(color = ColorSaving, fontWeight = FontWeight.Bold, fontSize = 12.sp),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            }
        }
    }
}

