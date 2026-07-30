package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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

        // Life Sections Horizontal Cards
        Text(
            text = "⚡ وصول سريع للأقسام",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        val categories = listOf(
            CategoryShortcut("الملاحظات", "${notes.size} ملاحظة", Icons.Default.EditNote, "notes", PrimaryTeal),
            CategoryShortcut("التذكيرات", "$pendingReminders معلقة", Icons.Default.Notifications, "reminders", SecondaryTeal),
            CategoryShortcut("الديون", "إدارة المستحقات", Icons.Default.AttachMoney, "debts", MintGreen),
            CategoryShortcut("صندوق التحويش", "${(totalSaved/totalTarget*100).toInt()}% من الهدف", Icons.Default.Savings, "savings", AccentGold),
            CategoryShortcut("الروابط", "${links.size} رابط مضاف", Icons.Default.Link, "links", Color(0xFF8B5CF6)),
            CategoryShortcut("الملفات", "${documents.size} وثيقة", Icons.Default.Folder, "documents", Color(0xFF6366F1)),
            CategoryShortcut("الفواتير والصور", "${receipts.size} فاتورة", Icons.Default.ReceiptLong, "receipts", Color(0xFFEC4899)),
            CategoryShortcut("النسخ الاحتياطي", "تصدير واستعادة", Icons.Default.CloudSync, "backup", Color(0xFF14B8A6)),
            CategoryShortcut("قفل التطبيق", "حماية بالرمز", Icons.Default.Lock, "security", Color(0xFF64748B))
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { item ->
                SectionShortcutCard(
                    title = item.title,
                    countText = item.subtitle,
                    icon = item.icon,
                    color = item.color,
                    onClick = { onNavigateToSection(item.route) }
                )
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
    }
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
            .width(135.dp)
            .height(115.dp)
            .clickable { onClick() }
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = countText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}
