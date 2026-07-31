package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

data class ZikrItem(
    val id: Int,
    val category: String,
    val text: String,
    val countTarget: Int,
    val timeToSay: String,
    val benefits: String
)

val AZKAR_DATABASE = listOf(
    // Morning Azkar
    ZikrItem(
        id = 1,
        category = "أذكار الصباح 🌅",
        text = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
        countTarget = 1,
        timeToSay = "بعد صلاة الفجر وحتى طلوع الشمس",
        benefits = "افتتاح اليوم بالتوحيد وحفظ النفس والبركة في الرزق واليوم."
    ),
    ZikrItem(
        id = 2,
        category = "أذكار الصباح 🌅",
        text = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ وَإِلَيْكَ النُّشُورُ.",
        countTarget = 1,
        timeToSay = "مرة واحدة في الصباح",
        benefits = "تسليم الأمر لله والاستعانة به في بداية اليوم."
    ),
    ZikrItem(
        id = 3,
        category = "أذكار الصباح 🌅",
        text = "سَيِّدُ الاِسْتِغْفَارِ: اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ.",
        countTarget = 1,
        timeToSay = "صباحاً ومساءً",
        benefits = "من قالها موقناً بها ومات من يومه أو ليلته دخل الجنة."
    ),
    ZikrItem(
        id = 4,
        category = "أذكار الصباح 🌅",
        text = "آيَةُ الْكُرْسِيِّ: ﴿اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ...﴾",
        countTarget = 1,
        timeToSay = "مرة في الصباح ومرة بعد كل صلاة",
        benefits = "من قالها حين يصبح أجير من الجن حتى يمسي."
    ),
    ZikrItem(
        id = 5,
        category = "أذكار الصباح 🌅",
        text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ.",
        countTarget = 3,
        timeToSay = "3 مرات صباحاً",
        benefits = "تعدل أذكاراً كثيرة وساعات طويلة من التسبيح."
    ),

    // Evening Azkar
    ZikrItem(
        id = 6,
        category = "أذكار المساء 🌆",
        text = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ.",
        countTarget = 1,
        timeToSay = "بعد صلاة العصر وحتى غروب الشمس",
        benefits = "حفظ العبد وأهله وممتلكاته خلال الليل."
    ),
    ZikrItem(
        id = 7,
        category = "أذكار المساء 🌆",
        text = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ.",
        countTarget = 3,
        timeToSay = "3 مرات مساءً",
        benefits = "لم يضره شيء في تلك الليلة ولن تصيبه سموم أو شياطين."
    ),
    ZikrItem(
        id = 8,
        category = "أذكار المساء 🌆",
        text = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ.",
        countTarget = 3,
        timeToSay = "3 مرات صباحاً ومساءً",
        benefits = "لم يصبه فجأة بلاء حتى يصبح أو يمسي."
    ),

    // Sleep Azkar
    ZikrItem(
        id = 9,
        category = "أذكار النوم 🌙",
        text = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِاسْمِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ.",
        countTarget = 1,
        timeToSay = "عند الاضطجاع على الفراش للنوع",
        benefits = "حفظ الروح عند النوم وراحة البال والسكينة."
    ),
    ZikrItem(
        id = 10,
        category = "أذكار النوم 🌙",
        text = "قراءة سورة الإخلاص والمعوذتين (الفلق والناس) ومسح الجسد بها.",
        countTarget = 3,
        timeToSay = "قبل النوم مباشرة",
        benefits = "الحماية والوقاية والتحصين الشامل طوال الليل."
    ),

    // Post Prayer Azkar
    ZikrItem(
        id = 11,
        category = "أذكار بعد الصلاة 🕌",
        text = "أَسْتَغْفِرُ اللَّهَ (3 مرات)، اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ، تَبَارَكْتَ يَا ذَا الْجَلاَلِ وَالإِكْرَامِ.",
        countTarget = 1,
        timeToSay = "دبر كل صلاة مكتوبة",
        benefits = "جبر ما قد يقع في الصلاة من سهو أو تقصير."
    ),
    ZikrItem(
        id = 12,
        category = "أذكار بعد الصلاة 🕌",
        text = "التسبيح: سُبْحَانَ اللَّهِ (33)، الْحَمْدُ لِلَّهِ (33)، اللَّهُ أَكْبَرُ (33)، وختمها بـ: لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ.",
        countTarget = 100,
        timeToSay = "عقب الصلوات الخمس",
        benefits = "غُفرت خطاياك وإن كانت مثل زبد البحر."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarScreen(viewModel: MainViewModel) {
    val context = LocalContext.current

    val categories = listOf("الكل", "أذكار الصباح 🌅", "أذكار المساء 🌆", "أذكار النوم 🌙", "أذكار بعد الصلاة 🕌")
    var selectedCategory by remember { mutableStateOf("الكل") }

    // Store user counters map
    val userCounters = remember { mutableStateMapOf<Int, Int>() }

    val filteredAzkar = remember(selectedCategory) {
        if (selectedCategory == "الكل") AZKAR_DATABASE
        else AZKAR_DATABASE.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📿", fontSize = 32.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "حصن المسلم - الأذكار والفوائد",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "أذكار اليوم والليلة مع توضيح أوقات قولها وفضلها وتكرارها",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Categories Scrollable Chips
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
            edgePadding = 0.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(filteredAzkar) { _, zikr ->
                val currentCount = userCounters[zikr.id] ?: 0
                val isCompleted = currentCount >= zikr.countTarget

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = zikr.category,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            if (isCompleted) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تم الاكتفاء ✓", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Zikr Text
                        Text(
                            text = zikr.text,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                lineHeight = 30.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Time & Benefits Info Cards
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "وقت قولها: ${zikr.timeToSay}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "الفوائد والفضل: ${zikr.benefits}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tasbeeh Counter Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "العدد: $currentCount / ${zikr.countTarget}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                                    )
                                )

                                if (currentCount > 0) {
                                    IconButton(
                                        onClick = { userCounters[zikr.id] = 0 },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "إعادة", tint = Color.Gray)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    val next = (userCounters[zikr.id] ?: 0) + 1
                                    userCounters[zikr.id] = next
                                    if (next == zikr.countTarget) {
                                        Toast.makeText(context, "تقبل الله منك الذكر! ✨", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isCompleted) "زيادة التسبيح" else "تسبيح (+1)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
