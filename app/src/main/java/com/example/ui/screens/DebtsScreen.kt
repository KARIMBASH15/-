package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entities.DebtEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.MintGreen
import com.example.ui.theme.RoseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(viewModel: MainViewModel) {
    val debts by viewModel.debts.collectAsState()
    var selectedTypeTab by remember { mutableStateOf(0) } // 0: "TO_RECEIVE" (لي), 1: "TO_PAY" (علي)
    var showAddDialog by remember { mutableStateOf(false) }
    var debtToEdit by remember { mutableStateOf<DebtEntity?>(null) }

    val owedToMeTotal = debts.filter { it.type == "TO_RECEIVE" && !it.isPaid }.sumOf { it.amount }
    val iOweTotal = debts.filter { it.type == "TO_PAY" && !it.isPaid }.sumOf { it.amount }
    val netTotal = owedToMeTotal - iOweTotal

    val currentType = if (selectedTypeTab == 0) "TO_RECEIVE" else "TO_PAY"
    val filteredDebts = debts.filter { it.type == currentType }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    debtToEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_debt_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة دين")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("debts_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "💰 إدارة الديون والمستحقات",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Balance Summary Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "صافي الميزانية",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${if (netTotal >= 0) "+" else ""}${netTotal.toInt()} ج.م",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (netTotal >= 0) MintGreen else RoseRed
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "🟢 لي (عند الناس): ${owedToMeTotal.toInt()} ج.م",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MintGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🔴 علي (للناس): ${iOweTotal.toInt()} ج.م",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = RoseRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs for TO_RECEIVE vs TO_PAY
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                TabRow(
                    selectedTabIndex = selectedTypeTab,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedTypeTab == 0,
                        onClick = { selectedTypeTab = 0 },
                        text = { Text("ديون لي (أطلب الناس)") }
                    )
                    Tab(
                        selected = selectedTypeTab == 1,
                        onClick = { selectedTypeTab = 1 },
                        text = { Text("ديون علي (يطالبونني)") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredDebts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTypeTab == 0) "لا توجد ديون لك عند أحد 👍" else "لا توجد ديون عليك لأحد 👍",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredDebts, key = { it.id }) { debt ->
                        DebtCardItem(
                            debt = debt,
                            onTogglePaid = {
                                viewModel.updateDebt(debt.copy(isPaid = !debt.isPaid))
                            },
                            onEdit = {
                                debtToEdit = debt
                                showAddDialog = true
                            },
                            onDelete = { viewModel.deleteDebt(debt) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditDebtDialog(
            existingDebt = debtToEdit,
            defaultType = currentType,
            onDismiss = { showAddDialog = false },
            onSave = { name, type, amount, dueDate, notes ->
                if (debtToEdit == null) {
                    viewModel.addDebt(
                        DebtEntity(
                            personName = name,
                            type = type,
                            amount = amount,
                            dueDate = dueDate,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.updateDebt(
                        debtToEdit!!.copy(
                            personName = name,
                            type = type,
                            amount = amount,
                            dueDate = dueDate,
                            notes = notes
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DebtCardItem(
    debt: DebtEntity,
    onTogglePaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isOwedToMe = debt.type == "TO_RECEIVE"
    val accentColor = if (isOwedToMe) MintGreen else RoseRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (debt.isPaid) Color.Gray.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isOwedToMe) Icons.Default.CallMade else Icons.Default.CallReceived,
                    contentDescription = null,
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = debt.personName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (debt.isPaid) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MintGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "مسدد 👍",
                                style = MaterialTheme.typography.labelSmall,
                                color = MintGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "${debt.amount.toInt()} جنيه",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (debt.isPaid) Color.Gray else accentColor
                    )
                )

                if (debt.dueDate.isNotEmpty()) {
                    Text(
                        text = "موعد السداد: ${debt.dueDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                if (debt.notes.isNotEmpty()) {
                    Text(
                        text = debt.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onTogglePaid) {
                    Icon(
                        imageVector = if (debt.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "تحديد كمسدد",
                        tint = if (debt.isPaid) MintGreen else Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف",
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditDebtDialog(
    existingDebt: DebtEntity?,
    defaultType: String,
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, amount: Double, dueDate: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(existingDebt?.personName ?: "") }
    var type by remember { mutableStateOf(existingDebt?.type ?: defaultType) }
    var amountText by remember { mutableStateOf(existingDebt?.amount?.toInt()?.toString() ?: "") }
    var dueDate by remember { mutableStateOf(existingDebt?.dueDate ?: "2026-08-15") }
    var notes by remember { mutableStateOf(existingDebt?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingDebt == null) "تسجيل دين جديد" else "تعديل الدين") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "TO_RECEIVE",
                        onClick = { type = "TO_RECEIVE" },
                        label = { Text("دين لي (أطلب)") }
                    )
                    FilterChip(
                        selected = type == "TO_PAY",
                        onClick = { type = "TO_PAY" },
                        label = { Text("دين علي (يطالبني)") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الشخص أو الجهة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ (بالجنيه)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("تاريخ السداد المتوقع") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amt > 0) {
                        onSave(name, type, amt, dueDate, notes)
                    }
                },
                enabled = name.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
