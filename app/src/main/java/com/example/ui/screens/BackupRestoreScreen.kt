package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentUsername by viewModel.currentUsername.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val isAdmin = currentUserRole.equals("ADMIN", ignoreCase = true) || currentUsername.equals("km512", ignoreCase = true)

    val currentSyncPin by viewModel.syncPinState.collectAsState()
    val lastSyncTime by viewModel.lastSyncTimeState.collectAsState()
    val isAutoSync by viewModel.isAutoSyncState.collectAsState()

    val allUsers by viewModel.allUsers.collectAsState()
    val recoveryRequests by viewModel.recoveryRequests.collectAsState()

    var pinInput by remember(currentSyncPin) { mutableStateOf(currentSyncPin) }
    var jsonOutput by remember { mutableStateOf("") }
    var jsonInput by remember { mutableStateOf("") }
    var isSyncing by remember { mutableStateOf(false) }

    var adminTabSelected by remember { mutableIntStateOf(0) } // 0 = Sync & Admin Backup, 1 = Users & Recovery Requests, 2 = Broadcast Notifications

    // Admin Broadcast Notification State
    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }
    var isSendingNotif by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("backup_restore_screen")
    ) {
        // --- USER ACCOUNT BAR ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "الحساب الحالي: $currentUsername 👤",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "إدارة النسخ الاحتياطي والمزامنة السحابية",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        viewModel.logoutUser()
                        Toast.makeText(context, "تم تسجيل الخروج بنجاح", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("خروج")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // IF ADMIN SHOW ADMIN TAB SELECTOR
        if (isAdmin) {
            TabRow(
                selectedTabIndex = adminTabSelected,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Tab(
                    selected = adminTabSelected == 0,
                    onClick = { adminTabSelected = 0 },
                    text = { Text("المزامنة ☁️", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = adminTabSelected == 1,
                    onClick = { adminTabSelected = 1 },
                    text = { Text("المستخدمين (${recoveryRequests.size}) 👥", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = adminTabSelected == 2,
                    onClick = { adminTabSelected = 2 },
                    text = { Text("إرسال إشعارات 🔔", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (isAdmin && adminTabSelected == 1) {
            // --- ADMIN TAB 1: USERS LIST & RECOVERY REQUESTS ---
            Text(
                text = "📩 طلبات نسيت كلمة السر والنسخ الاحتياطي ($ {recoveryRequests.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (recoveryRequests.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        "لا توجد طلبات استرداد حالياً.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else {
                recoveryRequests.forEach { req ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "اسم المستخدم: ${req.username}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                IconButton(onClick = { viewModel.deleteRecoveryRequest(req.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "مسح الطلب", tint = Color.Red)
                                }
                            }
                            Text("إيميل الاسترداد: ${req.recoveryEmail}", style = MaterialTheme.typography.bodySmall)
                            val df = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                            Text("وقت الطلب: ${df.format(Date(req.createdAt))}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "👥 جميع المستخدمين المسجلين بقاعدة البيانات (${allUsers.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            allUsers.forEach { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.username, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (user.role.equals("ADMIN", ignoreCase = true)) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        user.role,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (user.role.equals("ADMIN", ignoreCase = true)) Color.White else Color.Black
                                    )
                                }
                            }
                            if (user.recoveryEmail.isNotBlank()) {
                                Text("الإيميل: ${user.recoveryEmail}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Text("كلمة السر: ${user.passwordHash}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }

                        if (!user.username.equals("km512", ignoreCase = true)) {
                            IconButton(onClick = {
                                viewModel.deleteUser(user.username)
                                Toast.makeText(context, "تم حذف المستخدم ${user.username}", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف المستخدم", tint = Color.Red)
                            }
                        }
                    }
                }
            }

        } else if (isAdmin && adminTabSelected == 2) {
            // --- ADMIN TAB 2: SEND BROADCAST NOTIFICATIONS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "إرسال إشعار تنبيه للمستخدمين 🔔",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "سيصل الإشعار بشريط التنبيهات مع صوت رنة تن الهادئ غير المزعج",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        label = { Text("عنوان الإشعار") },
                        placeholder = { Text("مثال: تحديث هام في النظام 📢") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notifMessage,
                        onValueChange = { notifMessage = it },
                        label = { Text("محتوى نص الإشعار") },
                        placeholder = { Text("أدخل تفاصيل التنبيه الموجه لجميع المستخدمين...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (notifTitle.isBlank() || notifMessage.isBlank()) {
                                Toast.makeText(context, "الرجاء إدخال عنوان ونص الإشعار", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSendingNotif = true
                            viewModel.sendBroadcastNotification(
                                title = notifTitle,
                                message = notifMessage
                            ) {
                                isSendingNotif = false
                                notifTitle = ""
                                notifMessage = ""
                                Toast.makeText(context, "تم إرسال الإشعار بنجاح بشريط التنبيهات! 🔔", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSendingNotif
                    ) {
                        if (isSendingNotif) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إرسال الإشعار بشريط التنبيهات 🚀", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

        } else {
            // --- SYNC SECTION (VISIBLE TO ALL USERS & ADMIN) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "مزامنة قاعدة البيانات سحابياً ☁️",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "حفظ ومزامنة كافة بياناتك بضغطة زر باستخدام رمز السر المعتمد",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Database Connection Status Badge Lights (Green, Yellow, Red)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "مؤشر حالة الاتصال بقاعدة البيانات",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Green Status Badge (Connected)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                            .border(2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("متصل 🟢", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                }

                                // Yellow Status Badge (Syncing Offline Data)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B))
                                            .border(2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("مزامنة أوفلاين 🟡", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                }

                                // Red Status Badge (Disconnected)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444))
                                            .border(2.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("غير متصل 🔴", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            viewModel.setSyncPin(it)
                        },
                        label = { Text("رمز سر المزامنة (PIN)") },
                        placeholder = { Text("أدخل رمز السر الخاص بك") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "آخر حفظ تلقائي مع السحابة:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = lastSyncTime,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cloud Restore Button
                    OutlinedButton(
                        onClick = {
                            isSyncing = true
                            viewModel.downloadFromFirebase(pinInput) { success, msg ->
                                isSyncing = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSyncing && pinInput.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.primary)
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استرجاع البيانات من السحابة عند الحاجة ☁️", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- LOCAL EXPORT CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير ملف JSON الشامل للنسخ الاحتياطي", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "نسخ كود JSON كاملاً لقواعد البيانات للنسخ الاحتياطي الخارجي.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                jsonOutput = viewModel.backupManager.exportToJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("LifeOrganizerBackup", jsonOutput)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم تصدير البيانات ونسخها للحافظة! 📋", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير ونسخ نص JSON")
                    }

                    if (jsonOutput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = jsonOutput,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            label = { Text("معاينة نص النسخة الاحتياطية") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- LOCAL IMPORT CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("استعادة ملف JSON محلي يدوياً", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "الصق نص النسخة الاحتياطية لاستعادة كافة السجلات يدوياً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = jsonInput,
                            onValueChange = { jsonInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("الصق كود النسخة الاحتياطية هنا...") },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (jsonInput.isNotBlank()) {
                                    scope.launch {
                                        val success = viewModel.backupManager.importFromJson(jsonInput)
                                        if (success) {
                                            Toast.makeText(context, "تمت استعادة البيانات بنجاح! 🎉", Toast.LENGTH_LONG).show()
                                            jsonInput = ""
                                        } else {
                                            Toast.makeText(context, "حدث خطأ في قراءة صيغة النسخة الاحتياطية.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp),
                            enabled = jsonInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("استعادة البيانات المحلية")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- CLEAR ALL DATABASE CARD ---
                var showClearDbDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "مسح كافة البيانات والبدء من جديد",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "سيؤدي هذا الإجراء لمسح كافة البيانات والمدونات والديون والتحويش نهائياً والبدء بسجل فارغ تماماً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showClearDbDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مسح كافة البيانات والبدء من جديد 🗑️")
                        }
                    }
                }

                if (showClearDbDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDbDialog = false },
                        title = { Text("تأكيد مسح البيانات") },
                        text = { Text("هل أنت متاكد من رغبتك في مسح كافة بيانات التطبيق وقاعدة البيانات والبدء من جديد؟ لن يمكنك التراجع بعد هذا الإجراء.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.clearAllDatabase {
                                        Toast.makeText(context, "تم مسح كافة البيانات وتفريغ قاعدة البيانات بنجاح! 🧹", Toast.LENGTH_LONG).show()
                                    }
                                    showClearDbDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("نعم، إريد المسح والبدء من جديد")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDbDialog = false }) {
                                Text("إلغاء")
                            }
                        }
                    )
                }
        }
    }
}
