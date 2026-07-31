package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel

data class SurahInfo(
    val number: Int,
    val name: String,
    val englishName: String,
    val versesCount: Int,
    val startPage: Int,
    val isMeccan: Boolean
)

val QURAN_SURAHS = listOf(
    SurahInfo(1, "الفاتحة", "Al-Fatiha", 7, 1, true),
    SurahInfo(2, "البقرة", "Al-Baqarah", 286, 2, false),
    SurahInfo(3, "آل عمران", "Ali 'Imran", 200, 50, false),
    SurahInfo(4, "النساء", "An-Nisa", 176, 77, false),
    SurahInfo(5, "المائدة", "Al-Ma'idah", 120, 106, false),
    SurahInfo(6, "الأنعام", "Al-An'am", 165, 128, true),
    SurahInfo(7, "الأعراف", "Al-A'raf", 206, 151, true),
    SurahInfo(8, "الأنفال", "Al-Anfal", 75, 177, false),
    SurahInfo(9, "التوبة", "At-Tawbah", 129, 187, false),
    SurahInfo(10, "يونس", "Yunus", 109, 208, true),
    SurahInfo(11, "هود", "Hud", 123, 221, true),
    SurahInfo(12, "يوسف", "Yusuf", 111, 235, true),
    SurahInfo(13, "الرعد", "Ar-Ra'd", 43, 249, false),
    SurahInfo(14, "إبراهيم", "Ibrahim", 52, 255, true),
    SurahInfo(15, "الحجر", "Al-Hijr", 99, 262, true),
    SurahInfo(16, "النحل", "An-Nahl", 128, 267, true),
    SurahInfo(17, "الإسراء", "Al-Isra", 111, 282, true),
    SurahInfo(18, "الكهف", "Al-Kahf", 110, 293, true),
    SurahInfo(19, "مريم", "Maryam", 98, 305, true),
    SurahInfo(20, "طه", "Taha", 135, 312, true),
    SurahInfo(21, "الأنبياء", "Al-Anbiya", 112, 322, true),
    SurahInfo(22, "الحج", "Al-Hajj", 78, 332, false),
    SurahInfo(23, "المؤمنون", "Al-Mu'minun", 118, 342, true),
    SurahInfo(24, "النور", "An-Nur", 64, 350, false),
    SurahInfo(25, "الفرقان", "Al-Furqan", 77, 359, true),
    SurahInfo(26, "الشعراء", "Ash-Shu'ara", 227, 367, true),
    SurahInfo(27, "النمل", "An-Naml", 93, 377, true),
    SurahInfo(28, "القصص", "Al-Qasas", 88, 385, true),
    SurahInfo(29, "العنكبوت", "Al-'Ankabut", 69, 396, true),
    SurahInfo(30, "الروم", "Ar-Rum", 60, 404, true),
    SurahInfo(31, "لقمان", "Luqman", 34, 411, true),
    SurahInfo(32, "السجدة", "As-Sajdah", 30, 415, true),
    SurahInfo(33, "الأحزاب", "Al-Ahzab", 73, 418, false),
    SurahInfo(34, "سبأ", "Saba", 54, 428, true),
    SurahInfo(35, "فاطر", "Fatir", 45, 434, true),
    SurahInfo(36, "يس", "Ya-Sin", 83, 440, true),
    SurahInfo(37, "الصافات", "As-Saffat", 182, 446, true),
    SurahInfo(38, "ص", "Sad", 88, 453, true),
    SurahInfo(39, "الزمر", "Az-Zumar", 75, 458, true),
    SurahInfo(40, "غافر", "Ghafir", 85, 467, true),
    SurahInfo(41, "فصلت", "Fussilat", 54, 477, true),
    SurahInfo(42, "الشورى", "Ash-Shura", 53, 483, true),
    SurahInfo(43, "الزخرف", "Az-Zukhruf", 89, 489, true),
    SurahInfo(44, "الدخان", "Ad-Dukhan", 59, 496, true),
    SurahInfo(45, "الجاثية", "Al-Jathiyah", 37, 499, true),
    SurahInfo(46, "الأحقاف", "Al-Ahqaf", 35, 502, true),
    SurahInfo(47, "محمد", "Muhammad", 38, 507, false),
    SurahInfo(48, "الفتح", "Al-Fath", 29, 511, false),
    SurahInfo(49, "الحجرات", "Al-Hujurat", 18, 515, false),
    SurahInfo(50, "ق", "Qaf", 45, 518, true),
    SurahInfo(51, "الذاريات", "Adh-Dhariyat", 60, 520, true),
    SurahInfo(52, "الطور", "At-Tur", 49, 523, true),
    SurahInfo(53, "النجم", "An-Najm", 62, 526, true),
    SurahInfo(54, "القمر", "Al-Qamar", 55, 528, true),
    SurahInfo(55, "الرحمن", "Ar-Rahman", 78, 531, false),
    SurahInfo(56, "الواقعة", "Al-Waqi'ah", 96, 534, true),
    SurahInfo(57, "الحديد", "Al-Hadid", 29, 537, false),
    SurahInfo(58, "المجادلة", "Al-Mujadila", 22, 542, false),
    SurahInfo(59, "الحشر", "Al-Hashr", 24, 545, false),
    SurahInfo(60, "الممتحنة", "Al-Mumtahanah", 13, 549, false),
    SurahInfo(61, "الصف", "As-Saff", 14, 551, false),
    SurahInfo(62, "الجمعة", "Al-Jumu'ah", 11, 553, false),
    SurahInfo(63, "المنافقون", "Al-Munafiqun", 11, 554, false),
    SurahInfo(64, "التغابن", "At-Taghabun", 18, 556, false),
    SurahInfo(65, "الطلاق", "At-Talaq", 12, 558, false),
    SurahInfo(66, "التحريم", "At-Tahrim", 12, 560, false),
    SurahInfo(67, "الملك", "Al-Mulk", 30, 562, true),
    SurahInfo(68, "القلم", "Al-Qalam", 52, 564, true),
    SurahInfo(69, "الحاقة", "Al-Haqqah", 52, 566, true),
    SurahInfo(70, "المعارج", "Al-Ma'arij", 44, 568, true),
    SurahInfo(71, "نوح", "Nuh", 28, 570, true),
    SurahInfo(72, "الجن", "Al-Jinn", 28, 572, true),
    SurahInfo(73, "المزمل", "Al-Muzzammil", 20, 574, true),
    SurahInfo(74, "المدثر", "Al-Muddaththir", 56, 575, true),
    SurahInfo(75, "القيامة", "Al-Qiyamah", 40, 577, true),
    SurahInfo(76, "الإنسان", "Al-Insan", 31, 578, false),
    SurahInfo(77, "المرسلات", "Al-Mursalat", 50, 580, true),
    SurahInfo(78, "النبأ", "An-Naba", 40, 582, true),
    SurahInfo(79, "النازعات", "An-Nazi'at", 46, 583, true),
    SurahInfo(80, "عبس", "Abasa", 42, 585, true),
    SurahInfo(81, "التكوير", "At-Takwir", 29, 586, true),
    SurahInfo(82, "الانفطار", "Al-Infitar", 19, 587, true),
    SurahInfo(83, "المطففين", "Al-Mutaffifin", 36, 587, true),
    SurahInfo(84, "الانشقاق", "Al-Inshiqaq", 25, 589, true),
    SurahInfo(85, "البروج", "Al-Buruj", 22, 590, true),
    SurahInfo(86, "الطارق", "At-Tariq", 17, 591, true),
    SurahInfo(87, "الأعلى", "Al-A'la", 19, 591, true),
    SurahInfo(88, "الغاشية", "Al-Ghashiyah", 26, 592, true),
    SurahInfo(89, "الفجر", "Al-Fajr", 30, 593, true),
    SurahInfo(90, "البلد", "Al-Balad", 20, 594, true),
    SurahInfo(91, "الشمس", "Ash-Shams", 15, 595, true),
    SurahInfo(92, "الليل", "Al-Layl", 21, 595, true),
    SurahInfo(93, "الضحى", "Ad-Duha", 11, 596, true),
    SurahInfo(94, "الشرح", "Ash-Sharh", 8, 596, true),
    SurahInfo(95, "التين", "At-Tin", 8, 597, true),
    SurahInfo(96, "العلق", "Al-'Alaq", 19, 597, true),
    SurahInfo(97, "القدر", "Al-Qadr", 5, 598, true),
    SurahInfo(98, "البينة", "Al-Bayyinah", 8, 598, false),
    SurahInfo(99, "الزلزلة", "Az-Zalzalah", 8, 599, false),
    SurahInfo(100, "العاديات", "Al-'Adiyat", 11, 599, true),
    SurahInfo(101, "القارعة", "Al-Qari'ah", 11, 600, true),
    SurahInfo(102, "التكاثر", "At-Takathur", 8, 600, true),
    SurahInfo(103, "العصر", "Al-'Asr", 3, 601, true),
    SurahInfo(104, "الهمزة", "Al-Humazah", 9, 601, true),
    SurahInfo(105, "الفيل", "Al-Fil", 5, 601, true),
    SurahInfo(106, "قريش", "Quraysh", 4, 602, true),
    SurahInfo(107, "الماعون", "Al-Ma'un", 7, 602, true),
    SurahInfo(108, "الكوثر", "Al-Kawthar", 3, 602, true),
    SurahInfo(109, "الكافرون", "Al-Kafirun", 6, 603, true),
    SurahInfo(110, "النصر", "An-Nasr", 3, 603, false),
    SurahInfo(111, "المسد", "Al-Masad", 5, 603, true),
    SurahInfo(112, "الإخلاص", "Al-Ikhlas", 4, 604, true),
    SurahInfo(113, "الفلق", "Al-Falaq", 5, 604, true),
    SurahInfo(114, "الناس", "An-Nas", 6, 604, true)
)

// Sample surah verses for rich Quran reading view
val SURAH_SAMPLE_VERSES = mapOf(
    1 to listOf(
        "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
        "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ (1)",
        "الرَّحْمَٰنِ الرَّحِيمِ (2)",
        "مَالِكِ يَوْمِ الدِّينِ (3)",
        "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ (4)",
        "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ (5)",
        "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ (6)"
    ),
    67 to listOf(
        "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ (1)",
        "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ (2)",
        "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ ۖ فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ (3)",
        "ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ (4)"
    ),
    112 to listOf(
        "قُلْ هُوَ اللَّهُ أَحَدٌ (1)",
        "اللَّهُ الصَّمَدُ (2)",
        "لَمْ يَلِدْ وَلَمْ يُولَدْ (3)",
        "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ (4)"
    ),
    113 to listOf(
        "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ (1)",
        "مِن شَرِّ مَا خَلَقَ (2)",
        "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ (3)",
        "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ (4)",
        "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ (5)"
    ),
    114 to listOf(
        "قُلْ أَعُوذُ بِرَبِّ النَّاسِ (1)",
        "مَلِكِ النَّاسِ (2)",
        "إِلَٰهِ النَّاسِ (3)",
        "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ (4)",
        "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ (5)",
        "مِنَ الْجِنَّةِ وَالنَّاسِ (6)"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE) }

    var lastSavedPage by remember { mutableIntStateOf(sharedPrefs.getInt("last_page", 1)) }
    var lastSavedSurah by remember { mutableStateOf(sharedPrefs.getString("last_surah", "الفاتحة") ?: "الفاتحة") }

    var currentPage by remember { mutableIntStateOf(lastSavedPage) }
    var selectedSurah by remember { mutableStateOf<SurahInfo?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Index/Surahs, 1 = Reading View

    val filteredSurahs = remember(searchQuery) {
        if (searchQuery.isBlank()) QURAN_SURAHS
        else QURAN_SURAHS.filter { it.name.contains(searchQuery) || it.englishName.contains(searchQuery, ignoreCase = true) || it.number.toString() == searchQuery }
    }

    fun saveLastPosition(page: Int, surahName: String) {
        lastSavedPage = page
        lastSavedSurah = surahName
        sharedPrefs.edit()
            .putInt("last_page", page)
            .putString("last_surah", surahName)
            .apply()
        Toast.makeText(context, "تم حفظ صفحة $page ($surahName) كآخر صفحة وصلت لها 🔖", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📖", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "الوردي اليومي والمصحف الشريف",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "اقرأ وتدبر كتاب الله واحتفظ بآخر موضع وصلت له",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Last Saved Position Resume Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            currentPage = lastSavedPage
                            val surah = QURAN_SURAHS.find { it.name == lastSavedSurah } ?: QURAN_SURAHS.first()
                            selectedSurah = surah
                            activeTab = 1
                        },
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "آخر صفحة وصلت لها: سورة $lastSavedSurah (صفحة $lastSavedPage)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Tabs (Index / Reading View)
        TabRow(
            selectedTabIndex = activeTab,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("فهرس السور 📜", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("قراءة المصحف 📖", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeTab == 0) {
            // INDEX / SURAH LIST VIEW
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("ابحث باسم السورة أو رقمها...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSurahs) { surah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedSurah = surah
                                currentPage = surah.startPage
                                activeTab = 1
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${surah.number}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "سورة ${surah.name}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${if (surah.isMeccan) "مكية" else "مدنية"} • ${surah.versesCount} آية",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = "صفحة ${surah.startPage}",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        } else {
            // READING VIEW
            val currentSurahObj = selectedSurah ?: QURAN_SURAHS.find { it.startPage <= currentPage && (QURAN_SURAHS.getOrNull(QURAN_SURAHS.indexOf(it) + 1)?.startPage ?: 605) > currentPage } ?: QURAN_SURAHS.first()

            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Top Bar inside Reader
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "سورة ${currentSurahObj.name}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "صفحة $currentPage من 604",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        Button(
                            onClick = { saveLastPosition(currentPage, currentSurahObj.name) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ الموضع 🔖", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Reading Canvas
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFFFFDF5), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2D6B5), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B4332),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            val sampleVerses = SURAH_SAMPLE_VERSES[currentSurahObj.number] ?: listOf(
                                "وَالْقُرْآنِ الْحَكِيمِ ۚ إِنَّكَ لَمِنَ الْمُرْسَلِينَ ۚ عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ ۚ تَنزِيلَ الْعَزِيزِ الرَّحِيمِ",
                                "إِنَّا جَعَلْنَا فِي أَعْنَاقِهِمْ أَغْلَالًا فَهِيَ إِلَى الْأَذْقَانِ فَهُم مُّقْمَحُونَ",
                                "وَجَعَلْنَا مِن بَيْنِ أَيْدِيهِمْ سَدًّا وَمِنْ خَلْفِهِمْ سَدًّا فَأَغْشَيْنَاهُمْ فَهُمْ لَا يُبْصِرُونَ"
                            )

                            items(sampleVerses) { verse ->
                                Text(
                                    text = verse,
                                    fontSize = 20.sp,
                                    lineHeight = 36.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2B2D42),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "﴿ صَدَقَ اللَّهُ الْعَظِيمُ ﴾",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D6A4F)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Page Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { if (currentPage > 1) currentPage-- },
                            enabled = currentPage > 1,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الصفحة السابقة")
                        }

                        Text(
                            text = "$currentPage / 604",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Button(
                            onClick = { if (currentPage < 604) currentPage++ },
                            enabled = currentPage < 604,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("الصفحة التالية")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
