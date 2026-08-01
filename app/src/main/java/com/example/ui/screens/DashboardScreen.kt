package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToSection: (String) -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val savingsVaults by viewModel.savingsVaults.collectAsState()
    val links by viewModel.links.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val receipts by viewModel.receipts.collectAsState()

    // Financial Computations
    val owedToMe = debts.filter { it.type == "TO_RECEIVE" && !it.isPaid }.sumOf { it.amount }
    val iOwe = debts.filter { it.type == "TO_PAY" && !it.isPaid }.sumOf { it.amount }
    val netDebtBalance = owedToMe - iOwe

    val totalSaved = savingsVaults.sumOf { it.currentAmount }
    val totalTarget = savingsVaults.sumOf { it.targetAmount }.coerceAtLeast(1.0)
    val totalReceiptsSpent = receipts.sumOf { it.amount }

    val pendingReminders = reminders.count { !it.isCompleted }

    val context = LocalContext.current
    val quranPrefs = remember { context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE) }
    val quranPage = quranPrefs.getInt("last_page", 1)
    val quranSurah = quranPrefs.getString("last_surah", "الفاتحة") ?: "الفاتحة"
    val quranProgressPercent = remember(quranPage) { (quranPage / 604f * 100).coerceIn(0.1f, 100f) }

    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag("dashboard_screen")
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Hero Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    PrimaryTeal.copy(alpha = 0.92f)
                                )
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "منظم حياتي 🌟",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مرحباً بك! جميع مهامك، ديونك وحسابتك في مكان واحد آمن.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }

        // Quran Khatma Progress Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp))
                .clickable { onNavigateToSection("quran") },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF0)),
            border = BorderStroke(1.5.dp, Color(0xFFC9A227))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B4332)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "المصحف",
                        tint = Color(0xFFFAF0CA),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "متابع ختمة القرآن 📖",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B4332)
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1B4332).copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "صفحة $quranPage من ٦٠٤",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B4332)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "آخر قراءة: سورة $quranSurah",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { quranPage / 604f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF2D6A4F),
                        trackColor = Color(0xFFE5C158).copy(alpha = 0.3f)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = { onNavigateToSection("quran") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4332)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("قراءة ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Overview Grid
        Text(
            text = "📊 نظرة عامة وإحصائيات",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Financial Summary Cards Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Debts Overview
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "الديون الصافية",
                value = "${if (netDebtBalance >= 0) "+" else ""}${netDebtBalance.toInt()} ج.م",
                subtitle = "لي: ${owedToMe.toInt()} | علي: ${iOwe.toInt()}",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = if (netDebtBalance >= 0) MintGreen else RoseRed,
                onClick = { onNavigateToSection("debts") }
            )

            // Savings Overview
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "إجمالي التحويش",
                value = "${totalSaved.toInt()} ج.م",
                subtitle = "الهدف: ${totalTarget.toInt()} ج.م",
                icon = Icons.Default.Savings,
                accentColor = AccentGold,
                onClick = { onNavigateToSection("savings") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Visual Debt Bar Chart Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "مقارنة الديون (لي مقابل علي)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                val maxVal = (owedToMe.coerceAtLeast(iOwe)).coerceAtLeast(100.0).toFloat()
                val owedRatio = (owedToMe.toFloat() / maxVal).coerceIn(0.05f, 1f)
                val iOweRatio = (iOwe.toFloat() / maxVal).coerceIn(0.05f, 1f)

                // Custom Bar Drawing
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    val barHeight = 18.dp.toPx()
                    val totalWidth = size.width

                    // Row 1: Owed to me (Green)
                    drawRoundRect(
                        color = MintGreen.copy(alpha = 0.2f),
                        topLeft = Offset(0f, 0f),
                        size = Size(totalWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = MintGreen,
                        topLeft = Offset(0f, 0f),
                        size = Size(totalWidth * owedRatio, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )

                    // Row 2: I owe (Red)
                    drawRoundRect(
                        color = RoseRed.copy(alpha = 0.2f),
                        topLeft = Offset(0f, 32.dp.toPx()),
                        size = Size(totalWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = RoseRed,
                        topLeft = Offset(0f, 32.dp.toPx()),
                        size = Size(totalWidth * iOweRatio, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🟢 لي (أطلب الناس): ${owedToMe.toInt()} ج.م",
                        style = MaterialTheme.typography.bodySmall,
                        color = MintGreen
                    )
                    Text(
                        text = "🔴 علي (يطالبونني): ${iOwe.toInt()} ج.م",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoseRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Life Sections Grid (3 per row)
        Text(
            text = "⚡ جميع الأقسام والخدمات",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        val categories = listOf(
            CategoryShortcut("الوردي والمصحف", "اقرأ وتدبر 📖", Icons.Default.MenuBook, "quran", Color(0xFF059669)),
            CategoryShortcut("الأذكار والفوائد", "حصن المسلم 📿", Icons.Default.Mosque, "azkar", Color(0xFFD97706)),
            CategoryShortcut("الملاحظات", "${notes.size} ملاحظة", Icons.Default.EditNote, "notes", PrimaryTeal),
            CategoryShortcut("التذكيرات", "$pendingReminders معلقة", Icons.Default.Notifications, "reminders", SecondaryTeal),
            CategoryShortcut("الديون", "إدارة المستحقات", Icons.Default.AttachMoney, "debts", MintGreen),
            CategoryShortcut("صندوق التحويش", "${(totalSaved/totalTarget*100).toInt()}% من الهدف", Icons.Default.Savings, "savings", AccentGold),
            CategoryShortcut("الروابط", "${links.size} رابط مضاف", Icons.Default.Link, "links", Color(0xFF8B5CF6)),
            CategoryShortcut("الملفات", "${documents.size} وثيقة", Icons.Default.Folder, "documents", Color(0xFF6366F1)),
            CategoryShortcut("الفواتير والصور", "${receipts.size} فاتورة", Icons.Default.ReceiptLong, "receipts", Color(0xFFEC4899)),
            CategoryShortcut("النسخ الاحتياطي", "تصدير واستعادة", Icons.Default.CloudSync, "backup", Color(0xFF14B8A6)),
            CategoryShortcut("عن التطبيق والمطور", "واتساب المطور 💬", Icons.Default.Chat, "about", Color(0xFF25D366)),
            CategoryShortcut("قفل التطبيق", "حماية بالرمز", Icons.Default.Lock, "security", Color(0xFF64748B))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            SectionShortcutCard(
                                title = item.title,
                                countText = item.subtitle,
                                icon = item.icon,
                                color = item.color,
                                onClick = { onNavigateToSection(item.route) }
                            )
                        }
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recent Notes & Reminders Highlights
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📌 الملاحظات المثبتة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = { onNavigateToSection("notes") }) {
                        Text("عرض الكل")
                    }
                }

                val pinnedNotes = notes.filter { it.isPinned }
                if (pinnedNotes.isEmpty()) {
                    Text(
                        text = "لا توجد ملاحظات مثبتة حالياً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    pinnedNotes.take(2).forEach { note ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- CUSTOM USER HOME SCREEN WIDGETS SECTION ---
        CustomHomeWidgetsSection(onNavigateToSection = onNavigateToSection)
    }
}

data class CustomHomeWidget(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val subtitle: String,
    val actionRoute: String = "",
    val count: Int = 0
)

@Composable
fun CustomHomeWidgetsSection(onNavigateToSection: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("custom_home_widgets", Context.MODE_PRIVATE) }

    var widgetList by remember {
        mutableStateOf(loadCustomWidgets(prefs))
    }
    var showAddWidgetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨ عناصرك المخصصة للشاشة الرئيسية",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Button(
                onClick = { showAddWidgetDialog = true },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة عنصر ➕", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (widgetList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "يمكنك إضافة اختصارات أو بطاقات مخصصة أو أهداف يومية هنا على الشاشة الرئيسية بضغطة زر.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                widgetList.forEach { widget ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(widget.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                if (widget.subtitle.isNotEmpty()) {
                                    Text(widget.subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (widget.actionRoute.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { onNavigateToSection(widget.actionRoute) },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("فتح ➔", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                } else {
                                    // Custom Counter Button
                                    Button(
                                        onClick = {
                                            val updated = widgetList.map {
                                                if (it.id == widget.id) it.copy(count = it.count + 1) else it
                                            }
                                            widgetList = updated
                                            saveCustomWidgets(prefs, updated)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("العدد: ${widget.count} ➕", fontSize = 11.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                IconButton(
                                    onClick = {
                                        val updated = widgetList.filter { it.id != widget.id }
                                        widgetList = updated
                                        saveCustomWidgets(prefs, updated)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddWidgetDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newSubtitle by remember { mutableStateOf("") }
        var selectedRoute by remember { mutableStateOf("notes") }

        AlertDialog(
            onDismissRequest = { showAddWidgetDialog = false },
            title = { Text("إضافة عنصر مخصص للشاشة الرئيسية") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("عنوان العنصر (مثال: شرب الماء / كتابة يوميات)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newSubtitle,
                        onValueChange = { newSubtitle = it },
                        label = { Text("وصف أو ملحوظة مختصرة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("الانتقال السريع عند الضغط:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = selectedRoute == "notes",
                            onClick = { selectedRoute = "notes" },
                            label = { Text("الملاحظات", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedRoute == "quran",
                            onClick = { selectedRoute = "quran" },
                            label = { Text("المصحف", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedRoute == "counter",
                            onClick = { selectedRoute = "counter" },
                            label = { Text("عداد رقمي 🔢", fontSize = 11.sp) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            val newW = CustomHomeWidget(
                                title = newTitle,
                                subtitle = newSubtitle,
                                actionRoute = if (selectedRoute == "counter") "" else selectedRoute,
                                count = 0
                            )
                            val updated = widgetList + newW
                            widgetList = updated
                            saveCustomWidgets(prefs, updated)
                            showAddWidgetDialog = false
                        }
                    },
                    enabled = newTitle.isNotBlank()
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddWidgetDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

fun loadCustomWidgets(prefs: android.content.SharedPreferences): List<CustomHomeWidget> {
    val raw = prefs.getString("widgets_data", "") ?: ""
    if (raw.isBlank()) return emptyList()
    return try {
        raw.split(";;;").mapNotNull { itemStr ->
            val parts = itemStr.split("|||")
            if (parts.size >= 5) {
                CustomHomeWidget(
                    id = parts[0],
                    title = parts[1],
                    subtitle = parts[2],
                    actionRoute = parts[3],
                    count = parts[4].toIntOrNull() ?: 0
                )
            } else null
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun saveCustomWidgets(prefs: android.content.SharedPreferences, list: List<CustomHomeWidget>) {
    val encoded = list.joinToString(";;;") { "${it.id}|||${it.title}|||${it.subtitle}|||${it.actionRoute}|||${it.count}" }
    prefs.edit().putString("widgets_data", encoded).apply()
}

data class CategoryShortcut(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SectionShortcutCard(
    title: String,
    countText: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(105.dp)
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = countText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
