package com.example.presentation.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Categories
import com.example.domain.model.TransactionType
import com.example.presentation.viewmodel.BudgetViewModel
import com.example.ui.theme.*
import java.util.Date

import com.example.presentation.viewmodel.BudgetUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionTab(
    viewModel: BudgetViewModel,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val t = com.example.ui.localization.LocalAppStrings.current
    
    val uiState by viewModel.uiState.collectAsState()
    val goals = (uiState as? BudgetUiState.Success)?.goals ?: emptyList()
    val debts = (uiState as? BudgetUiState.Success)?.debts ?: emptyList()

    // Form Fields State
    var selectedType by remember { mutableStateOf(TransactionType.INCOME) }
    var selectedGoalId by remember { mutableStateOf<Int?>(null) }
    var selectedDebtId by remember { mutableStateOf<Int?>(null) }
    
    var showCreateDebtDialog by remember { mutableStateOf(false) }
    var autoSelectNewDebtName by remember { mutableStateOf<String?>(null) }

    // Maintain list of categories based on selectedType
    val categoryList = when (selectedType) {
        TransactionType.INCOME -> Categories.incomeCategories
        TransactionType.EXPENSE -> Categories.expenseCategories
        TransactionType.SAVING -> {
            goals.map { it.name }
        }
    }

    var selectedCategory by remember(selectedType, goals) { 
        mutableStateOf(
            if (selectedType == TransactionType.SAVING) {
                if (goals.isEmpty()) "" else goals.first().name
            } else {
                if (categoryList.isNotEmpty()) categoryList.first() else ""
            }
        ) 
    }
    var customCategoryText by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    
    // Auto-select newly created debt
    LaunchedEffect(debts) {
        val targetName = autoSelectNewDebtName
        if (targetName != null) {
            val matchingDebt = debts.find { it.name.trim().equals(targetName, ignoreCase = true) }
            if (matchingDebt != null) {
                selectedDebtId = matchingDebt.id
                autoSelectNewDebtName = null
            }
        }
    }

    // Helper for beautiful French / other languages mapping
    fun getLocalizedCategoryText(category: String): String {
        return if (selectedType == TransactionType.SAVING) {
            category
        } else {
            UiUtils.getLocalizedCategory(category, t)
        }
    }

    // Date state
    var selectedDateTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Dropdown expanded state
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Validation State / Feedback
    var labelError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var customCategoryError by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }

    val requiresCustomCategory = if (selectedType == TransactionType.SAVING) {
        false
    } else {
        Categories.isCustomCategory(selectedCategory)
    }

    if (showDatePicker) {
        BudgetDatePickerDialog(
            initialDateTimestamp = selectedDateTimestamp,
            onDateSelected = {
                selectedDateTimestamp = it
                showDatePicker = false
            },
            onDismissRequest = { showDatePicker = false },
            language = viewModel.currentLanguage.value
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = t.addTransactionTitle,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )

        // Success Feedback Banner
        if (showSuccessBanner) {
            Card(
                colors = CardDefaults.cardColors(containerColor = ColorIncome.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(ColorIncome.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ColorIncome,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = t.toastTxAddedSuccessfully,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ColorIncome
                            )
                        )
                        Text(
                            text = t.txSuccessBannerSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Section: Select Type
        Text(
            text = t.txTypePrompt,
            style = MaterialTheme.typography.labelMedium.copy(
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        )

        // Triple styled selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransactionTypeSelector(
                type = TransactionType.INCOME,
                isSelected = selectedType == TransactionType.INCOME,
                activeColor = ColorIncome,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedType = TransactionType.INCOME
                    selectedGoalId = null
                    selectedDebtId = null
                    showSuccessBanner = false
                }
            )
            TransactionTypeSelector(
                type = TransactionType.EXPENSE,
                isSelected = selectedType == TransactionType.EXPENSE,
                activeColor = ColorExpense,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedType = TransactionType.EXPENSE
                    selectedGoalId = null
                    selectedDebtId = null
                    showSuccessBanner = false
                }
            )
            TransactionTypeSelector(
                type = TransactionType.SAVING,
                isSelected = selectedType == TransactionType.SAVING,
                activeColor = ColorSaving,
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedType = TransactionType.SAVING
                    selectedGoalId = null
                    selectedDebtId = null
                    showSuccessBanner = false
                }
            )
        }

        Divider(color = DarkSurfaceVariant, thickness = 1.dp)

        // Section: Category Selector / Saving Goal Selector
        if (selectedType == TransactionType.SAVING && goals.isEmpty()) {
            var showCreateGoalDialogInTab by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = ColorSaving.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ColorSaving.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ColorSaving,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (viewModel.currentLanguage.value) {
                            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Aucun objectif d'épargne créé"
                            else -> "No Savings Goals Created"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        ),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = when (viewModel.currentLanguage.value) {
                            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Veuillez créer un objectif d'épargne dans l'onglet Analyse pour y affecter vos économies, ou créez-en un directement ci-dessous :"
                            else -> "Please create a savings goal in the Analysis tab to assign your savings, or create one directly below:"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCreateGoalDialogInTab = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorSaving),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (viewModel.currentLanguage.value) {
                                com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "+ Créer un objectif"
                                else -> "+ Create a Goal"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            if (showCreateGoalDialogInTab) {
                CreateSavingsGoalDialog(
                    onDismiss = { showCreateGoalDialogInTab = false },
                    onConfirm = { name, target, initial, targetDate ->
                        viewModel.addGoal(name, target, initial, targetDate)
                        showCreateGoalDialogInTab = false
                    }
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (selectedType == TransactionType.SAVING) t.labelSavingsGoal else t.txCategory,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (selectedType == TransactionType.SAVING) ColorSaving else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .clickable { dropdownExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("category_dropdown")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getLocalizedCategoryText(selectedCategory),
                            style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = t.changeCategory,
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(DarkSurface)
                            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    ) {
                        categoryList.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = getLocalizedCategoryText(category),
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                    )
                                },
                                onClick = {
                                    selectedCategory = category
                                    dropdownExpanded = false
                                    customCategoryError = false
                                    showSuccessBanner = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Conditional text field for "Autre..." custom entry
        if (requiresCustomCategory) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (selectedType == TransactionType.SAVING) t.labelSpecifyGoal else t.specifyCategoryRequired,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ColorSaving,
                        fontWeight = FontWeight.Bold
                    )
                )
                OutlinedTextField(
                    value = customCategoryText,
                    onValueChange = {
                        customCategoryText = it
                        customCategoryError = false
                        showSuccessBanner = false
                    },
                    placeholder = { Text(t.customCategoryPlaceholder, color = TextMuted) },
                    singleLine = true,
                    isError = customCategoryError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_category_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ColorSaving,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedLabelColor = ColorSaving,
                        unfocusedLabelColor = TextSecondary,
                        errorBorderColor = ColorExpense
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (customCategoryError) {
                    Text(
                        text = t.errSpecifyCategory,
                        color = ColorExpense,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Form: Libellé (Hidden / Supprimé for SAVING transaction typed operations, as it is automatically labeled with selected goal name)
        if (selectedType != TransactionType.SAVING) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = t.txLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        labelError = false
                        showSuccessBanner = false
                    },
                    placeholder = { Text(t.txLabelPlaceholder, color = TextMuted) },
                    singleLine = true,
                    isError = labelError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("label_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedLabelColor = PrimaryBlue,
                        unfocusedLabelColor = TextSecondary,
                        errorBorderColor = ColorExpense
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (labelError) {
                    Text(
                        text = t.errEnterLabel,
                        color = ColorExpense,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Form: Montant
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "${t.txAmount} (${UiUtils.currentCurrencySymbol})",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    // Accept only numbers and one decimal dot/comma
                    val formattedInput = input.replace(',', '.')
                    if (formattedInput.isEmpty() || formattedInput.toDoubleOrNull() != null || formattedInput.endsWith('.')) {
                        amountText = formattedInput
                        amountError = false
                        showSuccessBanner = false
                    }
                },
                placeholder = { Text("0.00", color = TextMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = amountError,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedLabelColor = PrimaryBlue,
                    unfocusedLabelColor = TextSecondary,
                    errorBorderColor = ColorExpense
                ),
                shape = RoundedCornerShape(12.dp)
            )
            if (amountError) {
                Text(
                    text = t.errEnterAmount,
                    color = ColorExpense,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Form: Date selector card
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = t.txDateLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDatePicker = true }
                    .testTag("date_picker_button"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = UiUtils.formatShortDate(selectedDateTimestamp, viewModel.currentLanguage.value),
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = t.txDateLabel,
                        tint = PrimaryBlue
                    )
                }
            }
        }

        // Section: Associate Debt (Conditional - only shown if type is EXPENSE and category is exactly "Remboursement de prêt")
        if (selectedType == TransactionType.EXPENSE && selectedCategory == "Remboursement de prêt") {
            var debtDropdownExpanded by remember { mutableStateOf(false) }
            val selectedDebt = debts.find { it.id == selectedDebtId }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = t.labelAssociatedDebt,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .clickable { debtDropdownExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("associate_debt_dropdown")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedDebt?.name ?: t.labelNoDebtAssociated,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (selectedDebtId == null) TextMuted else ColorExpense,
                                fontWeight = if (selectedDebtId == null) FontWeight.Normal else FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = t.changeDebt,
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = debtDropdownExpanded,
                        onDismissRequest = { debtDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(DarkSurface)
                            .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = t.labelNoDebtAssociated,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
                                )
                            },
                            onClick = {
                                selectedDebtId = null
                                debtDropdownExpanded = false
                            }
                        )
                        
                        debts.forEach { d ->
                            val remainingAmt = (d.totalAmount - d.reimbursedAmount).coerceAtLeast(0.0)
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${d.name} (Reste: ${UiUtils.formatCurrency(remainingAmt)})",
                                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                                    )
                                },
                                onClick = {
                                    selectedDebtId = d.id
                                    debtDropdownExpanded = false
                                }
                            )
                        }

                        Divider(color = DarkSurfaceVariant)

                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = PrimaryBlue
                                    )
                                    Text(
                                        text = when (viewModel.currentLanguage.value) {
                                            com.example.ui.localization.AppLanguageSupported.ENGLISH -> "+ Create a new loan..."
                                            com.example.ui.localization.AppLanguageSupported.ESPANOL -> "+ Crear un nuevo préstamo..."
                                            com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "+ Neuen Kredit erstellen..."
                                            com.example.ui.localization.AppLanguageSupported.ITALIANO -> "+ Crea un nuovo prestito..."
                                            com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "+ Criar um novo empréstimo..."
                                            com.example.ui.localization.AppLanguageSupported.KOREAN -> "+ 새 대출 만들기..."
                                            com.example.ui.localization.AppLanguageSupported.CHINESE -> "+ 创建新贷款..."
                                            com.example.ui.localization.AppLanguageSupported.JAPANESE -> "+ 新しいローンを作成..."
                                            com.example.ui.localization.AppLanguageSupported.ARABIC -> "+ إنشاء قرض جديد..."
                                            com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "+ Создать новый кредит..."
                                            com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "+ Créer un nouveau prêt..."
                                            else -> "+ Create a new loan..."
                                        },
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = PrimaryBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            },
                            onClick = {
                                debtDropdownExpanded = false
                                showCreateDebtDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Render Quick Create Debt Dialog
        if (showCreateDebtDialog) {
            var debtName by remember { mutableStateOf("") }
            var debtTotal by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreateDebtDialog = false },
                title = { Text(
                    text = when (viewModel.currentLanguage.value) {
                        com.example.ui.localization.AppLanguageSupported.ENGLISH -> "New Loan / Debt"
                        com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Nuevo préstamo / deuda"
                        com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Neuer Kredit / Schulden"
                        com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Nuovo prestito / debito"
                        com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Novo empréstimo / dívida"
                        com.example.ui.localization.AppLanguageSupported.KOREAN -> "새 대출 / 부채"
                        com.example.ui.localization.AppLanguageSupported.CHINESE -> "新贷款 / 债务"
                        com.example.ui.localization.AppLanguageSupported.JAPANESE -> "新しいローン / 負債"
                        com.example.ui.localization.AppLanguageSupported.ARABIC -> "قرض جديد / دين"
                        com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Новый кредит / долг"
                        com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Nouveau prêt / dette"
                        else -> "New Loan / Debt"
                    },
                    color = TextWhite, 
                    fontWeight = FontWeight.Bold
                ) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = debtName,
                            onValueChange = { debtName = it },
                            label = { Text(
                                text = when (viewModel.currentLanguage.value) {
                                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Name or creditor (e.g., Bank Loan)"
                                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Nombre o acreedor (ej., Préstamo Banco)"
                                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Name oder Gläubiger (z.B. Bankkredit)"
                                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Nome o creditore (es., Prestito Bancario)"
                                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Nome ou credor (ex., Empréstimo do Banco)"
                                    com.example.ui.localization.AppLanguageSupported.KOREAN -> "이름 또는 채권자 (예: 은행 대출)"
                                    com.example.ui.localization.AppLanguageSupported.CHINESE -> "名称或债权人（例如，银行贷款）"
                                    com.example.ui.localization.AppLanguageSupported.JAPANESE -> "名前または債権者（例：銀行ローン）"
                                    com.example.ui.localization.AppLanguageSupported.ARABIC -> "الاسم أو الدائن (مثال: قرض بنكي)"
                                    com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Имя или кредитор (например, банковский кредит)"
                                    com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Nom ou créancier (ex: Prêt Banque Ax)"
                                    else -> "Name or creditor (e.g., Bank Loan)"
                                }
                            ) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.testTag("add_debt_name_input_quick")
                        )

                        OutlinedTextField(
                            value = debtTotal,
                            onValueChange = { debtTotal = it },
                            label = { Text(
                                text = when (viewModel.currentLanguage.value) {
                                    com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Total amount"
                                    com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Monto total"
                                    com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Gesamtbetrag"
                                    com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Importo totale"
                                    com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Valor total"
                                    com.example.ui.localization.AppLanguageSupported.KOREAN -> "총 금액"
                                    com.example.ui.localization.AppLanguageSupported.CHINESE -> "总金额"
                                    com.example.ui.localization.AppLanguageSupported.JAPANESE -> "総額"
                                    com.example.ui.localization.AppLanguageSupported.ARABIC -> "المبلغ الإجمالي"
                                    com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Общая сумма"
                                    com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Montant total"
                                    else -> "Total amount"
                                }
                            ) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = BorderColor
                            ),
                            modifier = Modifier.testTag("add_debt_total_input_quick")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val tot = debtTotal.toDoubleOrNull()
                            if (debtName.isNotBlank() && tot != null) {
                                autoSelectNewDebtName = debtName.trim()
                                viewModel.addDebt(debtName.trim(), tot)
                                showCreateDebtDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            text = when (viewModel.currentLanguage.value) {
                                com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Create"
                                com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Crear"
                                com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Erstellen"
                                com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Crea"
                                com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Criar"
                                com.example.ui.localization.AppLanguageSupported.KOREAN -> "생성"
                                com.example.ui.localization.AppLanguageSupported.CHINESE -> "创建"
                                com.example.ui.localization.AppLanguageSupported.JAPANESE -> "作成"
                                com.example.ui.localization.AppLanguageSupported.ARABIC -> "إنشاء"
                                com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Создать"
                                com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Créer"
                                else -> "Create"
                            },
                            color = DarkBackground
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDebtDialog = false }) {
                        Text(
                            text = when (viewModel.currentLanguage.value) {
                                com.example.ui.localization.AppLanguageSupported.ENGLISH -> "Cancel"
                                com.example.ui.localization.AppLanguageSupported.ESPANOL -> "Cancelar"
                                com.example.ui.localization.AppLanguageSupported.DEUTSCH -> "Abbrechen"
                                com.example.ui.localization.AppLanguageSupported.ITALIANO -> "Annulla"
                                com.example.ui.localization.AppLanguageSupported.PORTUGUES -> "Cancelar"
                                com.example.ui.localization.AppLanguageSupported.KOREAN -> "취소"
                                com.example.ui.localization.AppLanguageSupported.CHINESE -> "取消"
                                com.example.ui.localization.AppLanguageSupported.JAPANESE -> "キャンセル"
                                com.example.ui.localization.AppLanguageSupported.ARABIC -> "إلغاء"
                                com.example.ui.localization.AppLanguageSupported.RUSSIAN -> "Отмена"
                                com.example.ui.localization.AppLanguageSupported.FRANCAIS -> "Annuler"
                                else -> "Cancel"
                            },
                            color = TextSecondary
                        )
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save Button
        Button(
            onClick = {
                // Validation checks
                val finalAmount = amountText.toDoubleOrNull()
                val finalLabel = if (selectedType == TransactionType.SAVING) selectedCategory else label.trim()
                val isLabelValid = finalLabel.isNotBlank()
                val isAmountValid = finalAmount != null && finalAmount > 0
                val isCustomCategoryValid = !requiresCustomCategory || customCategoryText.isNotBlank()

                if (!isLabelValid && selectedType != TransactionType.SAVING) labelError = true
                if (!isAmountValid) amountError = true
                if (!isCustomCategoryValid) customCategoryError = true

                if (isLabelValid && isAmountValid && isCustomCategoryValid && finalAmount != null) {
                    val selectedGoal = if (selectedType == TransactionType.SAVING) {
                        goals.find { it.name == selectedCategory }
                    } else null

                    // Call viewmodel
                    viewModel.addTransaction(
                        type = selectedType,
                        category = if (selectedType == TransactionType.SAVING) selectedCategory else (if (selectedCategory == "Autre objectif") customCategoryText.trim() else selectedCategory),
                        customCategory = if (selectedType != TransactionType.SAVING && requiresCustomCategory) customCategoryText.trim() else null,
                        label = finalLabel,
                        amount = finalAmount,
                        dateTimestamp = selectedDateTimestamp,
                        associatedGoalId = selectedGoal?.id,
                        associatedDebtId = if (selectedType == TransactionType.EXPENSE && selectedCategory == "Remboursement de prêt") selectedDebtId else null,
                        savingGoalName = if (selectedType == TransactionType.SAVING) selectedCategory else null
                    )

                    // Success trigger
                    showSuccessBanner = true
                    Toast.makeText(context, t.toastTxAddedSuccess, Toast.LENGTH_SHORT).show()

                    // Reset form fields neatly
                    label = ""
                    amountText = ""
                    customCategoryText = ""
                    selectedGoalId = null
                    selectedDebtId = null
                    // reset selection category back to list head
                    selectedCategory = if (selectedType == TransactionType.SAVING) {
                        if (goals.isEmpty()) "" else goals.first().name
                    } else {
                        if (categoryList.isNotEmpty()) categoryList.first() else ""
                    }

                    // Optional callback
                    onSuccess()
                } else {
                    Toast.makeText(context, t.toastValidationErr, Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_transaction_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = t.saveTxButton,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = DarkBackground
                )
            )
        }
    }
}

@Composable
fun TransactionTypeSelector(
    type: TransactionType,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val t = com.example.ui.localization.LocalAppStrings.current
    val label = when (type) {
        TransactionType.INCOME -> t.txTypeIncome
        TransactionType.EXPENSE -> t.txTypeExpense
        TransactionType.SAVING -> t.txTypeSaving
    }
    val backgroundColor = if (isSelected) activeColor.copy(alpha = 0.15f) else DarkSurface
    val borderColor = if (isSelected) activeColor else DarkSurfaceVariant
    val textColor = if (isSelected) activeColor else TextSecondary

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("type_selector_${type.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
