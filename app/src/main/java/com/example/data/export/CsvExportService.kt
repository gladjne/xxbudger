// Copyright (c) 2025 Gladstone Joy. Licensed under the MIT License.
package com.example.data.export

import android.content.Context
import com.example.data.security.SafeLog as Log
import com.example.domain.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvExportService {
    private val tag = "CsvExportService"

    fun generateCsv(
        context: Context,
        userEmail: String?,
        totalIncome: Double,
        totalExpense: Double,
        totalSaving: Double,
        balance: Double,
        savingsRate: Double,
        periodName: String,
        transactions: List<Transaction>
    ): File? {
        Log.d(tag, "Generating budget CSV report...")
        try {
            val cacheDir = context.cacheDir
            val joyReportDir = File(cacheDir, "joy_reports").apply { mkdirs() }
            val formattedDateName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val csvFile = File(joyReportDir, "budget-joy-$formattedDateName.csv")

            val writer = FileOutputStream(csvFile).bufferedWriter(Charsets.UTF_8)
            writer.use { out ->
                // Helper to safely write CSV entries with quotes
                fun writeRow(vararg fields: String) {
                    val row = fields.joinToString(",") { field ->
                        val escaped = field.replace("\"", "\"\"")
                        "\"$escaped\""
                    }
                    out.write(row)
                    out.newLine()
                }

                // Header section
                writeRow("BUDGET JOY - RAPPORT D'EXPORTATION")
                writeRow("Utilisateur", userEmail ?: "Utilisateur Démo")
                writeRow("Période", periodName)
                writeRow("Date de génération", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
                writeRow()

                // Totals section
                writeRow("RÉSUMÉ FINANCIER")
                writeRow("Revenus totaux", String.format(Locale.US, "%.2f", totalIncome))
                writeRow("Dépenses totales", String.format(Locale.US, "%.2f", totalExpense))
                writeRow("Épargne totale", String.format(Locale.US, "%.2f", totalSaving))
                writeRow("Solde disponible", String.format(Locale.US, "%.2f", balance))
                writeRow("Taux d'épargne (%)", String.format(Locale.US, "%.1f%%", savingsRate))
                writeRow()

                // Details of Transactions
                writeRow("LISTE DES TRANSACTIONS")
                writeRow("ID", "Type", "Catégorie", "Description / Libellé", "Montant", "Date")

                val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                transactions.forEach { tx ->
                    val txDate = dateFormatter.format(Date(tx.dateTimestamp))
                    writeRow(
                        tx.id.toString(),
                        tx.type,
                        tx.displayCategory,
                        tx.label,
                        String.format(Locale.US, "%.2f", tx.amount),
                        txDate
                    )
                }
            }

            Log.d(tag, "CSV generation completed successfully: ${csvFile.absolutePath}")
            return csvFile
        } catch (e: Exception) {
            Log.e(tag, "Failed to generate CSV file", e)
            return null
        }
    }
}
