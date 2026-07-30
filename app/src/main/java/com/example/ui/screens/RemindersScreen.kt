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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.entities.ReminderEntity
import com.example.ui.MainViewModel
import com.example.ui.theme.AccentGold
import com.example.ui.theme.MintGreen
import com.example.ui.theme.RoseRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: MainViewModel) {
    val reminders by viewModel.reminders.collectAsState()
    var selectedFilterTab by remember { mutableStateOf(0) } // 0: المعلقة, 1: المكتملة
    var showAddDialog by remember { mutableStateOf(false) }
    var reminderToEdit by remember { mutableStateOf<ReminderEntity?>(null) }

    val filteredReminders = reminders.filter { r ->
        if (selectedFilterTab == 0) !r.isCompleted else r.isCompleted
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    reminderToEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_reminder_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة تذكير")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("reminders_screen")
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "📅 التذكيرات والمهام المؤرخة",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Tabs for Pending vs Completed
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                TabRow(
                    selectedTabIndex = selectedFilterTab,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedFilterTab == 0,
                        onClick = { selectedFilterTab = 0 },
                        text = { Text("المعلقة (${reminders.count { !it.isCompleted }})") }
                    )
                    Tab(
                        selected = selectedFilterTab == 1,
                        onClick = { selectedFilterTab = 1 },
                        text = { Text("المكتملة (${reminders.count { it.isCompleted }})") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredReminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedFilterTab == 0) "لا توجد تذكيرات معلقة 🎉" else "لا توجد تذكيرات مكتملة بعد.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredReminders, key = { it.id }) { reminder ->
                        ReminderCardItem(
                            reminder = reminder,
                            onToggleComplete = {
                                viewModel.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
                            },
                            onEdit = {
                                reminderToEdit = reminder
                                showAddDialog = true
                            },
                            onDelete = { viewModel.deleteReminder(reminder) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditReminderDialog(
            existingReminder = reminderToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, timeFormatted, priority, repeatType ->
                if (reminderToEdit == null) {
                    viewModel.addReminder(
                        ReminderEntity(
                            title = title,
                            description = desc,
                            timeFormatted = timeFormatted,
                            priority = priority,
                            repeatType = repeatType
                        )
                    )
                } else {
                    viewModel.updateReminder(
                        reminderToEdit!!.copy(
                            title = title,
                            description = desc,
                            timeFormatted = timeFormatted,
                            priority = priority,
                            repeatType = repeatType
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ReminderCardItem(
    reminder: ReminderEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (reminder.priority) {
        "عالي" -> RoseRed
        "متوسط" -> AccentGold
        else -> MintGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = reminder.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (reminder.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(priorityColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = reminder.priority,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor
                        )
                    }
                }

                if (reminder.description.isNotEmpty()) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (reminder.timeFormatted.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = reminder.timeFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = reminder.repeatType,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "حذف",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun AddEditReminderDialog(
    existingReminder: ReminderEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, timeFormatted: String, priority: String, repeatType: String) -> Unit
) {
    var title by remember { mutableStateOf(existingReminder?.title ?: "") }
    var desc by remember { mutableStateOf(existingReminder?.description ?: "") }
    var timeFormatted by remember { mutableStateOf(existingReminder?.timeFormatted ?: "09:00 ص") }
    var priority by remember { mutableStateOf(existingReminder?.priority ?: "متوسط") }
    var repeatType by remember { mutableStateOf(existingReminder?.repeatType ?: "مرة واحدة") }

    val priorityOptions = listOf("منخفض", "متوسط", "عالي")
    val repeatOptions = listOf("مرة واحدة", "يومي", "أسبوعي", "شهري")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingReminder == null) "إضافة تذكير جديد" else "تعديل التذكير") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان التذكير") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("الوصف أو التفاصيل") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = timeFormatted,
                    onValueChange = { timeFormatted = it },
                    label = { Text("الوقت (مثال: 10:30 ص)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("الأولوية:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    priorityOptions.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("التكرار:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    repeatOptions.forEach { rep ->
                        FilterChip(
                            selected = repeatType == rep,
                            onClick = { repeatType = rep },
                            label = { Text(rep) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, desc, timeFormatted, priority, repeatType)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
