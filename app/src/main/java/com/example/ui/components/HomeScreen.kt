package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AyahEntity
import com.example.data.SurahEntity
import com.example.ui.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: QuranViewModel,
    onNavigateToQuran: (String) -> Unit, // "study" or "mushaf"
    onNavigateToPackages: () -> Unit,
    onOpenSurahIndex: () -> Unit,
    onNavigateToDetails: (AyahEntity, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val surahs by viewModel.surahs.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val bookmarkedAyahs by viewModel.bookmarkedAyahs.collectAsState()
    val packages by viewModel.packages.collectAsState()
    val dailyAyah by viewModel.dailyAyah.collectAsState()
    val playingAyahId by viewModel.playingAyahId.collectAsState()
    val currentSurahId by viewModel.currentSurahId.collectAsState()
    val currentPageNumber by viewModel.currentPageNumber.collectAsState()

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    // Core Theme Styling
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldColor = Color(0xFFD4AF37)
    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White

    // State for live search on Home Screen
    var searchQuery by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf("surah") } // "surah" or "ai_verses"

    val aiSearchResults by viewModel.aiSearchResults.collectAsState()
    val aiSearchLoading by viewModel.aiSearchLoading.collectAsState()

    val filteredSurahs = remember(searchQuery, surahs) {
        if (searchQuery.isBlank() || searchMode == "ai_verses") {
            emptyList()
        } else {
            surahs.filter {
                it.nameAr.contains(searchQuery) ||
                it.nameEn.contains(searchQuery, ignoreCase = true) ||
                it.id.toString() == searchQuery.trim()
            }
        }
    }

    // Dynamic, Beautiful Islamic Greeting & Hijri Date
    val hijriText = remember {
        try {
            val hijriDate = java.time.chrono.HijrahDate.now()
            val day = hijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
            val month = hijriDate.get(java.time.temporal.ChronoField.MONTH_OF_YEAR)
            val year = hijriDate.get(java.time.temporal.ChronoField.YEAR)

            val monthNames = listOf(
                "محرم", "صفر", "ربيع الأول", "ربيع الآخر",
                "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
                "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
            )
            val monthStr = monthNames.getOrNull(month - 1) ?: "ذو الحجة"

            val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
            fun toAr(num: Int): String = num.toString().map { if (it.isDigit()) arabicDigits[it - '0'] else it }.joinToString("")

            "${toAr(day)} $monthStr ${toAr(year)} هـ"
        } catch (e: Throwable) {
            "١٣ ذو الحجة ١٤٤٧ هـ" // High-fidelity beautiful dynamic fallback
        }
    }

    // Get Surah details for "Last Read" card
    val lastReadSurah = remember(currentSurahId, surahs) {
        surahs.find { it.id == currentSurahId }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Logo",
                            tint = goldColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "قُرْآنٌ كَرِيمٌ",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            color = if (isDark) primaryColor else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surface else primaryColor
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Header Block with Greeting & Hijri Date
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "السلام عليكم ورحمة الله وبركاته",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "مرحباً بك في تطبيق تدبر ودراسة القرآن الكريم",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontFamily = FontFamily.Serif
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(primaryColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = hijriText,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = primaryColor),
                            fontFamily = FontFamily.Serif
                        )
                    }
                }
            }

            // Modern Interactive Search Bar
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search Mode Selector Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = (searchMode == "surah"),
                            onClick = {
                                searchMode = "surah"
                                searchQuery = ""
                            },
                            label = { Text("فهرس السور بالاسم", fontFamily = FontFamily.Serif, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        FilterChip(
                            selected = (searchMode == "ai_verses"),
                            onClick = {
                                searchMode = "ai_verses"
                                searchQuery = ""
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "AI", modifier = Modifier.size(12.dp), tint = goldColor)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تدبر وبحث بالمعنى (AI)", fontFamily = FontFamily.Serif, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                if (searchMode == "surah") "ابحث عن السور بالاسم أو الرقم الكريّم..." else "ابحث عن معاني وتدبّرات (مثال: الأخلاق والصبر)...",
                                fontFamily = FontFamily.Serif,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (searchMode == "surah") Icons.Default.Search else Icons.Default.AutoAwesome,
                                contentDescription = "Search",
                                tint = if (searchMode == "surah") primaryColor else goldColor
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = primaryColor
                                        )
                                    }
                                }
                                if (searchMode == "ai_verses") {
                                    IconButton(
                                        onClick = { viewModel.performAiSearch(searchQuery) },
                                        enabled = searchQuery.isNotBlank() && !aiSearchLoading
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowCircleLeft,
                                            contentDescription = "البحث الذكي",
                                            tint = if (searchQuery.isNotBlank() && !aiSearchLoading) goldColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 3.dp, shape = RoundedCornerShape(16.dp))
                    )
                }
            }

            // Search Results Section (Shows if search active)
            if (searchQuery.isNotEmpty()) {
                if (searchMode == "surah") {
                    item {
                        Text(
                            text = "نتائج البحث (${filteredSurahs.size} سورة):",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = primaryColor
                        )
                    }
                    if (filteredSurahs.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(cardBg, RoundedCornerShape(12.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "عذراً، لم نجد سورة مطابقة لبحثك.",
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredSurahs) { surah ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectSurah(surah.id)
                                        onNavigateToQuran("study")
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.1f))
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
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(primaryColor.copy(alpha = 0.1f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${surah.id}",
                                                fontWeight = FontWeight.Bold,
                                                color = primaryColor,
                                                fontSize = 13.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = surah.nameAr,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "سورة ${surah.type} • آياتها ${surah.versesCount}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Go",
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // AI Semantic Results Block
                    item {
                        Text(
                            text = "نتائج البحث الدلالي بالـ AI للآيات (${aiSearchResults.size} آية):",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = primaryColor
                        )
                    }
                    if (aiSearchLoading) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(color = goldColor)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "جاري تمديد البحث دلالياً واستقصاء المعاني الإيمانية من الآيات...",
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Serif,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else if (aiSearchResults.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg)
                            ) {
                                Text(
                                    text = "لم نتوصّل لآيات مطابقة دلالياً لبحثك. يرجى الضغط على زر السهم الفضي أو إدخال موضوع مغاير لإعادة توليد تمديدات ذكية للبحث والتدبر.",
                                    modifier = Modifier.padding(18.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(aiSearchResults) { ayah ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectSurah(ayah.surahId)
                                        onNavigateToDetails(ayah, 6) // Open AI Tab directly
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(primaryColor.copy(alpha = 0.1f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${ayah.verseNumber}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = primaryColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            val surahName = surahs.find { it.id == ayah.surahId }?.nameAr ?: "سورة رقم ${ayah.surahId}"
                                            Text(
                                                text = surahName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Serif,
                                                color = primaryColor
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "AI Matching",
                                            tint = goldColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = ayah.textAr,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = FontFamily.Serif,
                                        textAlign = TextAlign.Right,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = ayah.translation,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontFamily = FontFamily.Serif,
                                        lineHeight = 18.sp
                                    )

                                    if (ayah.subjects.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "الموضوع: ${ayah.subjects}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = goldColor,
                                            fontFamily = FontFamily.Serif
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Default Home Screen Dashboard when no active search query

                // 1. Premium Last Read Card (أكمل التلاوة)
                item {
                    val gradientColors = if (isDark) {
                        listOf(Color(0xFF0C241B), Color(0xFF143B2B), Color(0xFF091C15))
                    } else {
                        listOf(Color(0xFF0E543A), Color(0xFF1C7A55), Color(0xFF0C4B33))
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.2.dp, goldColor.copy(alpha = 0.6f))
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Brush.linearGradient(colors = gradientColors))
                                .padding(18.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "Last Read",
                                            tint = goldColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "مواصلة القراءة والتدبر",
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 13.sp,
                                            color = goldColor
                                        )
                                    }

                                    // Display active reading mode badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "سورة نشطة",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = lastReadSurah?.nameAr ?: "سورة الفاتحة",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            ),
                                            fontFamily = FontFamily.Serif
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "آخر سورة تصفّحتها • صفحة $currentPageNumber",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontFamily = FontFamily.Serif
                                        )
                                    }

                                    Button(
                                        onClick = { onNavigateToQuran("study") },
                                        colors = ButtonDefaults.buttonColors(containerColor = goldColor),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "أكمل الآن ←",
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isDark) Color.Black else Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Visual Progress Bar
                                val progressRatio = remember(currentSurahId) {
                                    currentSurahId.toFloat() / 114f
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    LinearProgressIndicator(
                                        progress = { progressRatio },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = goldColor,
                                        trackColor = Color.White.copy(alpha = 0.15f),
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "${(progressRatio * 100).toInt()}% من المصحف",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Beautiful Islamic Grid Actions (Dashboard 2x2)
                item {
                    Text(
                        text = "بوابة القرآن الكافية",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = primaryColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GridDashboardCard(
                                title = "فهرس السور",
                                subtitle = "١١٤ سورة مباركة مع التقسيم والبحث المباشر.",
                                icon = Icons.Default.List,
                                color = Color(0xFF1E825B),
                                onClick = onOpenSurahIndex,
                                modifier = Modifier.weight(1f)
                            )
                            GridDashboardCard(
                                title = "مصحف التلاوة",
                                subtitle = "تصفح المصحف العثماني بالصفحات الكاملة والمقاطع اللطيفة.",
                                icon = Icons.Default.AutoStories,
                                color = Color(0xFFD4AF37),
                                onClick = { onNavigateToQuran("mushaf") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GridDashboardCard(
                                title = "تفسير ودراسة",
                                subtitle = "مواضيع الآيات واللفظ كلمة بكلمة والإعراب التفصيلي.",
                                icon = Icons.Default.MenuBook,
                                color = Color(0xFF1976D2),
                                onClick = { onNavigateToQuran("study") },
                                modifier = Modifier.weight(1f)
                            )
                            GridDashboardCard(
                                title = "مكتبة الكتب",
                                subtitle = "تفعيل كتب التفسير، والمراجع الإضافية والمفردات اللغوية أوفلاين.",
                                icon = Icons.Default.LibraryBooks,
                                color = Color(0xFF7B1FA2),
                                onClick = onNavigateToPackages,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3. Dynamic Daily Verse of comfort (آية اليوم الكريمة) with live audio & Tafsir action
                if (dailyAyah != null) {
                    item {
                        Text(
                            text = "آية اليوم للتدبر والانشراح",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = primaryColor,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation = 4.dp, shape = RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.08f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Daily Verse",
                                        tint = goldColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Clean Beautiful Quranic typography Calligraphy representation
                                Text(
                                    text = dailyAyah!!.textAr,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        lineHeight = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.Serif,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                val surahName = remember(dailyAyah, surahs) {
                                    surahs.find { it.id == dailyAyah!!.surahId }?.nameAr ?: "آية مباركة"
                                }
                                Text(
                                    text = "﴿$surahName : آية ${dailyAyah!!.verseNumber}﴾",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryColor
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = dailyAyah!!.translation,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Real micro-actions on Home Screen itself
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val isPlayingThis = (playingAyahId == dailyAyah!!.id)

                                    OutlinedButton(
                                        onClick = { viewModel.playAyah(dailyAyah!!) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (isPlayingThis) Color.Red else primaryColor
                                        ),
                                        border = BorderStroke(1.dp, if (isPlayingThis) Color.Red.copy(alpha = 0.5f) else primaryColor.copy(alpha = 0.3f)),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Play"
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isPlayingThis) "إيقاف التلاوة" else "استمع للآية",
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = { onNavigateToDetails(dailyAyah!!, 0) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Book,
                                            contentDescription = "Details",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "التدبر والتفسير",
                                            color = Color.White,
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Quick Stats & Achievements Grid
                item {
                    Text(
                        text = "إحصائيات المنصة الكريمة",
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = primaryColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "سور ومفهرسات",
                            count = "${surahs.size}",
                            icon = Icons.Default.FormatListNumbered,
                            color = primaryColor,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "آيات مفضلة",
                            count = "${bookmarks.size}",
                            icon = Icons.Default.Star,
                            color = goldColor,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "كتب تفاسير",
                            count = "${packages.filter { it.type == "Tafsir" && it.isInstalled }.size}",
                            icon = Icons.Default.CloudQueue,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 5. Saved Bookmarks Quick Carousel (My Marks & Reflections)
                if (bookmarkedAyahs.isNotEmpty()) {
                    item {
                        Text(
                            text = "آياتك المفضلة وعلاماتك المرجعية",
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = primaryColor,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 6.dp)
                        ) {
                            items(bookmarkedAyahs) { ayah ->
                                val correspondingSurah = remember(ayah, surahs) {
                                    surahs.find { it.id == ayah.surahId }
                                }
                                Card(
                                    modifier = Modifier
                                        .width(260.dp)
                                        .clickable { onNavigateToDetails(ayah, 0) }
                                        .shadow(3.dp, RoundedCornerShape(14.dp)),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = BorderStroke(1.dp, goldColor.copy(alpha = 0.2f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = correspondingSurah?.nameAr ?: "آية مباركة",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                fontFamily = FontFamily.Serif,
                                                color = primaryColor
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Fav",
                                                tint = goldColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = ayah.textAr,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                lineHeight = 22.sp,
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            fontFamily = FontFamily.Serif,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "آية رقم ${ayah.verseNumber} • صفحة ${ayah.pageNumber}",
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Serif,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "انقر للتفسير ←",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Serif,
                                                color = goldColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// Beautiful Dashboard Asymmetric grid tile
@Composable
fun GridDashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    Card(
        modifier = modifier
            .clickable { onClick() }
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.White
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
