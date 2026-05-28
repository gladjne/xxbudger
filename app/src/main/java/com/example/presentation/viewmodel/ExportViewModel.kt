package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.export.CsvExportService
import com.example.data.export.PdfExportService
import com.example.data.export.PdfShareHelper
import com.example.domain.ai.BudgetAiResult
import com.example.domain.analytics.GoalAnalysis
import com.example.domain.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.net.Uri
import com.example.data.export.DownloadHelper


sealed interface ExportUiState {
    object Idle : ExportUiState
    object Loading : ExportUiState
    data class Success(
        val file: File,
        val savedUri: Uri?,
        val destinationPath: String,
        val mimeType: String
    ) : ExportUiState
    data class Error(val message: String) : ExportUiState
}

class ExportViewModel : ViewModel() {
    private val _exportUiState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val exportUiState: StateFlow<ExportUiState> = _exportUiState.asStateFlow()

    private val pdfExportService = PdfExportService()
    private val csvExportService = CsvExportService()

    fun resetState() {
        _exportUiState.value = ExportUiState.Idle
    }

    fun exportBudgetReport(
        context: Context,
        userEmail: String?,
        totalIncome: Double,
        totalExpense: Double,
        totalSaving: Double,
        balance: Double,
        savingsRate: Double,
        budgetStatus: String,
        goals: List<GoalAnalysis>,
        aiResult: BudgetAiResult?,
        transactions: List<Transaction>
    ) {
        viewModelScope.launch {
            _exportUiState.value = ExportUiState.Loading
            try {
                val file = withContext(Dispatchers.IO) {
                    pdfExportService.generatePdf(
                        context = context.applicationContext,
                        userEmail = userEmail,
                        totalIncome = totalIncome,
                        totalExpense = totalExpense,
                        totalSaving = totalSaving,
                        balance = balance,
                        savingsRate = savingsRate,
                        budgetStatus = budgetStatus,
                        goals = goals,
                        aiResult = aiResult,
                        transactions = transactions
                    )
                }
                if (file != null && file.exists()) {
                    // Save to Downloads directory
                    val formattedMonthYear = SimpleDateFormat("MMMM-yyyy", Locale.getDefault()).format(Date())
                        .lowercase()
                        .replace("é", "e")
                        .replace("û", "u")
                        .replace("â", "a")
                    val displayName = "budget-joy-report-$formattedMonthYear.pdf"
                    
                    val (savedUri, destPath) = DownloadHelper.saveFileToDownloads(
                        context = context,
                        srcFile = file,
                        displayName = displayName,
                        mimeType = "application/pdf"
                    )
                    
                    _exportUiState.value = ExportUiState.Success(
                        file = file,
                        savedUri = savedUri,
                        destinationPath = destPath,
                        mimeType = "application/pdf"
                    )
                } else {
                    _exportUiState.value = ExportUiState.Error("Impossible de générer le fichier PDF")
                }
            } catch (e: Exception) {
                _exportUiState.value = ExportUiState.Error(e.message ?: "Une erreur est survenue lors de l'exportation.")
            }
        }
    }

    fun exportFlexibleReport(
        context: Context,
        format: String, // "PDF" or "CSV"
        periodType: String, // "THIS_MONTH", "PREVIOUS_MONTH", "LAST_3_MONTHS", "CUSTOM"
        customStartDate: Long?,
        customEndDate: Long?,
        userEmail: String?,
        allTransactions: List<Transaction>,
        goals: List<GoalAnalysis>,
        aiResult: BudgetAiResult?,
        budgetStatus: String
    ) {
        viewModelScope.launch {
            _exportUiState.value = ExportUiState.Loading
            try {
                // Filter transactions by period
                val (filteredTxs, periodLabel) = withContext(Dispatchers.Default) {
                    val cal = java.util.Calendar.getInstance()
                    val startMs: Long
                    val endMs: Long
                    val label: String

                    when (periodType) {
                        "THIS_MONTH" -> {
                            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                            cal.set(java.util.Calendar.MINUTE, 0)
                            cal.set(java.util.Calendar.SECOND, 0)
                            cal.set(java.util.Calendar.MILLISECOND, 0)
                            startMs = cal.timeInMillis

                            cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                            cal.set(java.util.Calendar.MINUTE, 59)
                            cal.set(java.util.Calendar.SECOND, 59)
                            cal.set(java.util.Calendar.MILLISECOND, 999)
                            endMs = cal.timeInMillis

                            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            label = sdf.format(Date()).replaceFirstChar { it.uppercase() }
                        }
                        "PREVIOUS_MONTH" -> {
                            cal.add(java.util.Calendar.MONTH, -1)
                            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                            cal.set(java.util.Calendar.MINUTE, 0)
                            cal.set(java.util.Calendar.SECOND, 0)
                            cal.set(java.util.Calendar.MILLISECOND, 0)
                            startMs = cal.timeInMillis

                            cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                            cal.set(java.util.Calendar.MINUTE, 59)
                            cal.set(java.util.Calendar.SECOND, 59)
                            cal.set(java.util.Calendar.MILLISECOND, 999)
                            endMs = cal.timeInMillis

                            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            label = sdf.format(Date(startMs)).replaceFirstChar { it.uppercase() }
                        }
                        "LAST_3_MONTHS" -> {
                            cal.add(java.util.Calendar.MONTH, -2)
                            cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                            cal.set(java.util.Calendar.MINUTE, 0)
                            cal.set(java.util.Calendar.SECOND, 0)
                            cal.set(java.util.Calendar.MILLISECOND, 0)
                            startMs = cal.timeInMillis

                            val endCal = java.util.Calendar.getInstance()
                            endCal.set(java.util.Calendar.DAY_OF_MONTH, endCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
                            endCal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                            endCal.set(java.util.Calendar.MINUTE, 59)
                            endCal.set(java.util.Calendar.SECOND, 59)
                            endCal.set(java.util.Calendar.MILLISECOND, 999)
                            endMs = endCal.timeInMillis

                            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                            label = "${sdf.format(Date(startMs))} - ${sdf.format(Date())}"
                        }
                        "CUSTOM" -> {
                            val startCal = java.util.Calendar.getInstance().apply {
                                timeInMillis = customStartDate ?: Date().time
                                set(java.util.Calendar.HOUR_OF_DAY, 0)
                                set(java.util.Calendar.MINUTE, 0)
                                set(java.util.Calendar.SECOND, 0)
                                set(java.util.Calendar.MILLISECOND, 0)
                            }
                            startMs = startCal.timeInMillis

                            val endCal = java.util.Calendar.getInstance().apply {
                                timeInMillis = customEndDate ?: Date().time
                                set(java.util.Calendar.HOUR_OF_DAY, 23)
                                set(java.util.Calendar.MINUTE, 59)
                                set(java.util.Calendar.SECOND, 59)
                                set(java.util.Calendar.MILLISECOND, 999)
                            }
                            endMs = endCal.timeInMillis

                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            label = "${sdf.format(Date(startMs))} - ${sdf.format(Date(endMs))}"
                        }
                        else -> {
                            startMs = 0L
                            endMs = Long.MAX_VALUE
                            label = "Toutes les périodes"
                        }
                    }

                    val filtered = allTransactions.filter { it.dateTimestamp in startMs..endMs }
                    Pair(filtered, label)
                }

                if (filteredTxs.isEmpty() && periodType == "CUSTOM") {
                    _exportUiState.value = ExportUiState.Error("Aucune transaction trouvée pour cette période.")
                    return@launch
                }

                // Calculate metrics
                val totalIncome = filteredTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
                val totalExpense = filteredTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val totalSaving = filteredTxs.filter { it.type == "SAVING" }.sumOf { it.amount }
                val balance = totalIncome - totalExpense - totalSaving
                val savingsRate = if (totalIncome > 0) (totalSaving / totalIncome) * 100.0 else 0.0

                val file = withContext(Dispatchers.IO) {
                    if (format.uppercase() == "CSV") {
                        csvExportService.generateCsv(
                            context = context.applicationContext,
                            userEmail = userEmail,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            totalSaving = totalSaving,
                            balance = balance,
                            savingsRate = savingsRate,
                            periodName = periodLabel,
                            transactions = filteredTxs
                        )
                    } else {
                        pdfExportService.generatePdf(
                            context = context.applicationContext,
                            userEmail = userEmail,
                            totalIncome = totalIncome,
                            totalExpense = totalExpense,
                            totalSaving = totalSaving,
                            balance = balance,
                            savingsRate = savingsRate,
                            budgetStatus = budgetStatus,
                            goals = goals,
                            aiResult = aiResult,
                            transactions = filteredTxs
                        )
                    }
                }

                if (file != null && file.exists()) {
                    // Save to Downloads directory
                    val displayName = if (format.uppercase() == "CSV") {
                        val dateString = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                        "budget-joy-data-$dateString.csv"
                    } else {
                        val cal = java.util.Calendar.getInstance()
                        val refDate = when (periodType) {
                            "PREVIOUS_MONTH" -> {
                                cal.add(java.util.Calendar.MONTH, -1)
                                cal.time
                            }
                            else -> Date()
                        }
                        val formattedMonthYear = SimpleDateFormat("MMMM-yyyy", Locale.getDefault()).format(refDate)
                            .lowercase()
                            .replace("é", "e")
                            .replace("û", "u")
                            .replace("â", "a")
                        "budget-joy-report-$formattedMonthYear.pdf"
                    }
                    
                    val mimeType = if (format.uppercase() == "CSV") "text/csv" else "application/pdf"
                    
                    val (savedUri, destPath) = DownloadHelper.saveFileToDownloads(
                        context = context,
                        srcFile = file,
                        displayName = displayName,
                        mimeType = mimeType
                    )

                    _exportUiState.value = ExportUiState.Success(
                        file = file,
                        savedUri = savedUri,
                        destinationPath = destPath,
                        mimeType = mimeType
                    )
                } else {
                    _exportUiState.value = ExportUiState.Error("Impossible de générer le fichier d'export")
                }

            } catch (e: Exception) {
                _exportUiState.value = ExportUiState.Error(e.message ?: "Une erreur est survenue lors de l'exportation.")
            }
        }
    }
}
