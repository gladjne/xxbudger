package com.example.presentation.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Categories
import com.example.domain.model.CategoryLimit
import com.example.domain.model.TransactionType
import com.example.presentation.viewmodel.BudgetUiState
import com.example.presentation.viewmodel.BudgetViewModel
import com.example.ui.localization.AppLanguageSupported
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryLimitsScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = remember(currentLang) { getLimitsLocalization(currentLang) }
    
    val limits by viewModel.allCategoryLimits.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showEditDialog by remember { mutableStateOf<String?>(null) } // category name
    var dialogAmountText by remember { mutableStateOf("") }
    
    // Get month spent amounts grouped by category
    val currentMonthExpensesByCategory = remember(uiState) {
        if (uiState is BudgetUiState.Success) {
            val success = uiState as BudgetUiState.Success
            success.transactions
                .filter { 
                    it.type == TransactionType.EXPENSE.name && 
                    com.example.domain.analytics.BudgetAnalyzer.isTimestampInCurrentMonth(it.dateTimestamp)
                }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        } else {
            emptyMap()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("limits_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back,
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextWhite
                )
            )
        },
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header Info Card (Bento Style)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PrimaryBlue.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.howItWorksTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = TextWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.howItWorksDesc,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    lineHeight = 16.sp,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // Categories Section Title
            item {
                Text(
                    text = strings.categoriesSectionTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // List of Categories with limits
            items(Categories.expenseCategories) { category ->
                val limitForCat = limits.find { it.category == category }
                val spent = currentMonthExpensesByCategory[category] ?: 0.0
                val localizedCatName = UiUtils.getLocalizedCategory(category, com.example.ui.localization.LocalAppStrings.current)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("limit_item_${category.lowercase()}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, if (limitForCat != null) BorderColor else BorderColor.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            (if (limitForCat != null) PrimaryBlue else TextMuted).copy(alpha = 0.12f),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val catIcon = UiUtils.getCategoryIcon(category)
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = null,
                                        tint = if (limitForCat != null) PrimaryBlue else TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = localizedCatName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextWhite,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = if (limitForCat != null) {
                                            strings.limitValue.format(UiUtils.formatCurrency(limitForCat.limitAmount))
                                        } else {
                                            strings.noLimitConfigured
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = if (limitForCat != null) ColorSaving else TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            // Edit/Add and Delete Actions
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (limitForCat != null) {
                                    IconButton(
                                        onClick = {
                                            dialogAmountText = limitForCat.limitAmount.toInt().toString()
                                            showEditDialog = category
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("edit_limit_$category")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = strings.edit,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteCategoryLimit(limitForCat)
                                            Toast.makeText(context, strings.deletedToast, Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("delete_limit_$category")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = strings.delete,
                                            tint = ColorExpense.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    TextButton(
                                        onClick = {
                                            dialogAmountText = ""
                                            showEditDialog = category
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryBlue),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("set_limit_$category")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = strings.configureBtn,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Progress and Alerts if limit exists
                        if (limitForCat != null) {
                            val ratio = (spent / limitForCat.limitAmount).coerceIn(0.0, 2.0).toFloat()
                            val ratioPercent = (spent / limitForCat.limitAmount * 100).toInt()
                            
                            val (progressColor, alertLabel) = when {
                                ratioPercent >= 100 -> ColorExpense to strings.alertCritical.format(ratioPercent)
                                ratioPercent >= 80 -> ColorSaving to strings.alertWarning.format(ratioPercent)
                                else -> PrimaryBlue to strings.alertNormal.format(ratioPercent)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Progress bar with animateFloatAsState in final production code
                                LinearProgressIndicator(
                                    progress = { ratio.coerceAtMost(1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = progressColor,
                                    trackColor = progressColor.copy(alpha = 0.15f)
                                )

                                // Progress details row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.spentThisMonth.format(UiUtils.formatCurrency(spent)),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Text(
                                        text = alertLabel,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = progressColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Edit Limit Dialog
        if (showEditDialog != null) {
            val category = showEditDialog!!
            val localizedCatName = UiUtils.getLocalizedCategory(category, com.example.ui.localization.LocalAppStrings.current)

            AlertDialog(
                onDismissRequest = { showEditDialog = null },
                title = {
                    Text(
                        text = strings.dialogTitle.format(localizedCatName),
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = strings.dialogInstructions,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        OutlinedTextField(
                            value = dialogAmountText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    dialogAmountText = input
                                }
                            },
                            label = { Text(strings.amountLabel, color = TextMuted) },
                            placeholder = { Text("0", color = TextMuted) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderColor,
                                containerColor = DarkBackground
                            ),
                            suffix = {
                                Text(
                                    text = UiUtils.currentCurrencySymbol,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("limit_input_field")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val limitAmt = dialogAmountText.toDoubleOrNull() ?: 0.0
                            if (limitAmt > 0.0) {
                                viewModel.saveCategoryLimit(category, limitAmt)
                                Toast.makeText(context, strings.savedToast, Toast.LENGTH_SHORT).show()
                                showEditDialog = null
                            } else {
                                Toast.makeText(context, strings.invalidAmountToast, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.save, color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEditDialog = null }
                    ) {
                        Text(strings.cancel, color = TextSecondary)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

data class LimitsStrings(
    val title: String,
    val back: String,
    val howItWorksTitle: String,
    val howItWorksDesc: String,
    val categoriesSectionTitle: String,
    val noLimitConfigured: String,
    val configureBtn: String,
    val limitValue: String,
    val edit: String,
    val delete: String,
    val spentThisMonth: String,
    val alertCritical: String,
    val alertWarning: String,
    val alertNormal: String,
    val dialogTitle: String,
    val dialogInstructions: String,
    val amountLabel: String,
    val save: String,
    val cancel: String,
    val savedToast: String,
    val deletedToast: String,
    val invalidAmountToast: String
)

fun getLimitsLocalization(language: AppLanguageSupported): LimitsStrings {
    val englishLimits = LimitsStrings(
        title = "Expense Limits",
        back = "Back",
        howItWorksTitle = "Smart Spent Control",
        howItWorksDesc = "Configure custom maximum monthly thresholds on major expense groups to avoid overdrafts. Joy AI automatically alerts you at 80% and 100%.",
        categoriesSectionTitle = "My Monthly Limits",
        noLimitConfigured = "No custom limit set. Tap configure.",
        configureBtn = "Define",
        limitValue = "Limit: %s / month",
        edit = "Edit",
        delete = "Delete",
        spentThisMonth = "Spent: %s",
        alertCritical = "⚠️ Exceeded (%d%%)",
        alertWarning = "🔔 Alert (%d%%)",
        alertNormal = "Normal (%d%%)",
        dialogTitle = "Define Budget Limit for %s",
        dialogInstructions = "Enter maximum monthly spending threshold. Leave active to automatically track and receive alerts.",
        amountLabel = "Monthly Budget Limit",
        save = "Confirm",
        cancel = "Cancel",
        savedToast = "Category limit configured successfully!",
        deletedToast = "Spending limit removed.",
        invalidAmountToast = "Please enter a valid amount superior to 0."
    )

    return when (language) {
        AppLanguageSupported.FRANCAIS -> LimitsStrings(
            title = "Limites de dépenses",
            back = "Retour",
            howItWorksTitle = "Gestion intelligente du budget",
            howItWorksDesc = "Définis des seuils mensuels pour tes catégories afin de contrôler tes dépenses. Joy AI et le système t'avertissent automatiquement à 80% (alerte douce) et 100% (alerte forte).",
            categoriesSectionTitle = "Mes seuils par catégorie",
            noLimitConfigured = "Aucune limite. Définir un seuil.",
            configureBtn = "Définir",
            limitValue = "Limite : %s / mois",
            edit = "Modifier",
            delete = "Supprimer",
            spentThisMonth = "Dépensé : %s",
            alertCritical = "⚠️ Seuil dépassé (%d%%)",
            alertWarning = "🔔 Alerte (%d%%)",
            alertNormal = "Normal (%d%%)",
            dialogTitle = "Configurer le seuil de %s",
            dialogInstructions = "Spécifie le montant de dépenses mensuel à ne pas dépasser pour cette catégorie.",
            amountLabel = "Montant de la limite",
            save = "Enregistrer",
            cancel = "Annuler",
            savedToast = "Limite enregistrée avec succès !",
            deletedToast = "Limite supprimée sous cette catégorie.",
            invalidAmountToast = "Indique un montant valide supérieur à 0."
        )
        AppLanguageSupported.DEUTSCH -> LimitsStrings(
            title = "Ausgabelimits",
            back = "Zurück",
            howItWorksTitle = "Intelligente Ausgabenkontrolle",
            howItWorksDesc = "Definieren Sie monatliche Grenzwerte, um das Budget stets im Auge zu behalten. Joy AI warnt Sie automatisch bei 80 % und 100 %.",
            categoriesSectionTitle = "Kategorie-Grenzwerte",
            noLimitConfigured = "Kein Limit konfiguriert.",
            configureBtn = "Festlegen",
            limitValue = "Limit: %s / Monat",
            edit = "Bearbeiten",
            delete = "Löschen",
            spentThisMonth = "Ausgegeben: %s",
            alertCritical = "⚠️ Überschritten (%d%%)",
            alertWarning = "🔔 Warnung (%d%%)",
            alertNormal = "Normal (%d%%)",
            dialogTitle = "Limit für %s festlegen",
            dialogInstructions = "Geben Sie die monatliche Höchstgrenze für diese Kategorie ein.",
            amountLabel = "Monatlicher Grenzwert",
            save = "Speichern",
            cancel = "Abbrechen",
            savedToast = "Limit erfolgreich gespeichert!",
            deletedToast = "Limit gelöscht.",
            invalidAmountToast = "Geben Sie einen Betrag über 0 ein."
        )
        AppLanguageSupported.ESPANOL -> LimitsStrings(
            title = "Límites de gastos",
            back = "Atrás",
            howItWorksTitle = "Control de Gastos Inteligente",
            howItWorksDesc = "Establece topes mensuales en tus categorías. Joy AI te notificará automáticamente cuando alcances el 80% y el 100% de tu presupuesto.",
            categoriesSectionTitle = "Mis límites de gastos",
            noLimitConfigured = "Sin límites. Configura uno.",
            configureBtn = "Definir",
            limitValue = "Límite: %s / mes",
            edit = "Editar",
            delete = "Eliminar",
            spentThisMonth = "Gastado: %s",
            alertCritical = "⚠️ ¡Excedido! (%d%%)",
            alertWarning = "🔔 Alerta (%d%%)",
            alertNormal = "Normal (%d%%)",
            dialogTitle = "Definir límite para %s",
            dialogInstructions = "Introduce el importe máximo de gasto mensual.",
            amountLabel = "Límite de gasto mensual",
            save = "Guardar",
            cancel = "Cancelar",
            savedToast = "¡Límite guardado correctamente!",
            deletedToast = "Límite de la categoría eliminado.",
            invalidAmountToast = "Por favor ingrese un monto superior a 0."
        )
        AppLanguageSupported.ITALIANO -> LimitsStrings(
            title = "Limiti di spesa",
            back = "Indietro",
            howItWorksTitle = "Controllo Spese Intelligente",
            howItWorksDesc = "Imposta soglie mensili per le tue spese per non sforare mai. Joy AI ti avvisa automaticamente all'80% e al 100%.",
            categoriesSectionTitle = "Soglie di spesa mensili",
            noLimitConfigured = "Nessun limite configurato.",
            configureBtn = "Imposta",
            limitValue = "Limite: %s / mese",
            edit = "Modifica",
            delete = "Elimina",
            spentThisMonth = "Speso: %s",
            alertCritical = "⚠️ Sforato (%d%%)",
            alertWarning = "🔔 Allerta (%d%%)",
            alertNormal = "Normale (%d%%)",
            dialogTitle = "Imposta limite per %s",
            dialogInstructions = "Inserisci la soglia di spesa massima mensile.",
            amountLabel = "Soglia massima mensile",
            save = "Salva",
            cancel = "Annulla",
            savedToast = "Limite configurato correttamente!",
            deletedToast = "Limite rimosso.",
            invalidAmountToast = "Inserisci una cifra valida superiore a 0."
        )
        AppLanguageSupported.PORTUGUES -> LimitsStrings(
            title = "Limites de despesas",
            back = "Voltar",
            howItWorksTitle = "Controle de Gastos Inteligente",
            howItWorksDesc = "Configure limites mensais personalizados para evitar surpresas. Joy AI enviará alertas quando alcançar 80% ou 100%.",
            categoriesSectionTitle = "Limites por categoria",
            noLimitConfigured = "Nenhum limite configurado.",
            configureBtn = "Definir",
            limitValue = "Limite: %s / mês",
            edit = "Editar",
            delete = "Excluir",
            spentThisMonth = "Gasto: %s",
            alertCritical = "⚠️ Excedido (%d%%)",
            alertWarning = "🔔 Alerta (%d%%)",
            alertNormal = "Normal (%d%%)",
            dialogTitle = "Definir limite para %s",
            dialogInstructions = "Insira o limite de gasto mensal máximo.",
            amountLabel = "Limite de gasto mensal",
            save = "Salvar",
            cancel = "Cancelar",
            savedToast = "Limite configurado!",
            deletedToast = "Limite removido.",
            invalidAmountToast = "Indique um valor válido maior do que 0."
        )
        else -> englishLimits
    }
}
