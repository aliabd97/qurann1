package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AyahEntity
import com.example.data.SurahEntity
import com.example.data.WordEntity
import com.example.ui.QuranViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyReaderScreen(
    viewModel: QuranViewModel,
    onNavigateToPackages: () -> Unit,
    onNavigateToDetails: (AyahEntity, Int) -> Unit,
    onOpenSurahIndex: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSurahId by viewModel.currentSurahId.collectAsState()
    val surahs by viewModel.surahs.collectAsState()
    val ayahs by viewModel.ayahs.collectAsState()
    val allWords by viewModel.allAyahWords.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    val currentSurah = surahs.find { it.id == currentSurahId }

    var showFilterSheet by remember { mutableStateOf(false) }

    // Slide/Swipe Surah implementation (HorizontalPager over Surahs list)
    val initialPage = remember(surahs) { surahs.indexOfFirst { it.id == currentSurahId }.coerceAtLeast(0) }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { surahs.size }
    )

    // Sync from swipe gesture inside HorizontalPager to selection
    LaunchedEffect(pagerState.currentPage) {
        if (surahs.isNotEmpty() && pagerState.currentPage in surahs.indices) {
            val selectedSurah = surahs[pagerState.currentPage]
            if (currentSurahId != selectedSurah.id) {
                viewModel.selectSurah(selectedSurah.id)
            }
        }
    }

    // Sync from ViewModel (e.g., if surah is chosen from Index lists) back to pagerState
    LaunchedEffect(currentSurahId) {
        if (surahs.isNotEmpty()) {
            val targetPage = surahs.indexOfFirst { it.id == currentSurahId }
            if (targetPage >= 0 && pagerState.currentPage != targetPage) {
                pagerState.scrollToPage(targetPage)
            }
        }
    }

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val barColor = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val contentColor = if (isDark) MaterialTheme.colorScheme.primary else Color.White

    Scaffold(
        topBar = {
            ReaderTopBar(
                title = currentSurah?.let { "${it.id}. ${it.nameAr}" } ?: "دراسة القرآن",
                currentMode = "study",
                onModeChange = { viewModel.setReadingMode(it) },
                onIndexClick = onOpenSurahIndex,
                onPackagesClick = onNavigateToPackages
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFilterSheet = true },
                containerColor = if (isDark) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
                contentColor = if (isDark) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
            ) {
                Icon(Icons.Default.Tune, contentDescription = "تخصيص العرض")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Page Summary Banner
            currentSurah?.let {
                PageInfoStrip(
                    juz = 1 + (it.id / 4),
                    hizb = 1 + (it.id / 2),
                    page = it.pageStart,
                    onInfoClick = {}
                )
            }

            // Beautiful Swipable Pager container
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { page ->
                // Render list contents ONLY for the currently active page to maintain ultra performance
                if (page == pagerState.currentPage) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            currentSurah?.let {
                                SurahHeaderCard(surah = it)
                            }
                        }

                        items(ayahs) { ayah ->
                            StudyAyahCard(
                                ayah = ayah,
                                words = allWords[ayah.id] ?: emptyList(),
                                viewModel = viewModel,
                                isBookmarked = bookmarks.contains(ayah.id),
                                onWordClick = { word -> viewModel.selectWord(word) },
                                onNavigateToDetails = onNavigateToDetails
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // Leave room for FAB
                        }
                    }
                } else {
                    // Beautiful loading placeholder card while sliding
                    val nextSurah = surahs.getOrNull(page)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = nextSurah?.nameAr ?: "",
                                fontSize = 32.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            CircularProgressIndicator(
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Managers
    val activeWord by viewModel.selectedWord.collectAsState()
    if (activeWord != null && currentSurah != null) {
        val currentAyahWords = allWords[activeWord!!.ayahId] ?: emptyList()
        WordDetailsSheet(
            word = activeWord!!,
            allWords = currentAyahWords,
            onDismiss = { viewModel.clearSelectedWord() }
        )
    }

    if (showFilterSheet) {
        ContentFilterSheet(
            viewModel = viewModel,
            onDismiss = { showFilterSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderTopBar(
    title: String,
    currentMode: String,
    onModeChange: (String) -> Unit,
    onIndexClick: () -> Unit,
    onPackagesClick: () -> Unit
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val barColor = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val contentColor = if (isDark) MaterialTheme.colorScheme.primary else Color.White
    val modeActiveColor = if (isDark) MaterialTheme.colorScheme.primary else Color.White
    val modeInactiveColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color.White.copy(alpha = 0.6f)

    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                fontFamily = FontFamily.Serif,
                color = contentColor
            )
        },
        navigationIcon = {
            IconButton(onClick = onIndexClick) {
                Icon(Icons.Default.Menu, contentDescription = "سرد السور", tint = contentColor)
            }
        },
        actions = {
            // Mode switcher
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isDark) MaterialTheme.colorScheme.surfaceVariant 
                        else MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    )
            ) {
                Text(
                    text = "دراسة",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = if (currentMode == "study") modeActiveColor else modeInactiveColor,
                    modifier = Modifier
                        .clickable { onModeChange("study") }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Text(
                    text = "المصحف",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = if (currentMode == "mushaf") modeActiveColor else modeInactiveColor,
                    modifier = Modifier
                        .clickable { onModeChange("mushaf") }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            IconButton(onClick = onPackagesClick) {
                Icon(Icons.Default.DownloadForOffline, contentDescription = "الحزم", tint = contentColor)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = barColor
        )
    )
}

@Composable
fun PageInfoStrip(
    juz: Int,
    hizb: Int,
    page: Int,
    onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الْجُزْء $juz • الْحِزْب $hizb • الصَّفْحَة $page",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "معلومات الجزء 🛈",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.clickable { onInfoClick() }
            )
        }
    }
}

@Composable
fun SurahHeaderCard(surah: SurahEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = surah.nameAr,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "سورة ${surah.type} • آياتها ${surah.versesCount}",
                fontSize = 13.sp,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyAyahCard(
    ayah: AyahEntity,
    words: List<WordEntity>,
    viewModel: QuranViewModel,
    isBookmarked: Boolean,
    onWordClick: (WordEntity) -> Unit,
    onNavigateToDetails: (AyahEntity, Int) -> Unit
) {
    val showTafsir by viewModel.showTafsir.collectAsState()
    val showIrab by viewModel.showIrab.collectAsState()
    val showWords by viewModel.showWords.collectAsState()
    val showSubjects by viewModel.showSubjects.collectAsState()
    val showAsbabNuzul by viewModel.showAsbabNuzul.collectAsState()
    val showTranslation by viewModel.showTranslation.collectAsState()
    val selectedTafsirId by viewModel.selectedTafsirId.collectAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Circular Indicator & Bookmark Button Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = "${ayah.verseNumber}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { viewModel.toggleBookmark(ayah.id) }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "تفضيل",
                        tint = if (isBookmarked) Color(0xFFD4AF37) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Arabic text of the Verse: word-by-word or complete
            if (showWords && words.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    words.forEach { word ->
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { onWordClick(word) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = word.wordAr,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = word.wordEn,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "${ayah.textAr} ﴿${ayah.verseNumber}﴾",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 44.sp,
                        textAlign = TextAlign.Right
                    ),
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Display translations
            if (showTranslation) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ayah.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tafsir Box inside card without brackets or unpolished emojis
            if (showTafsir) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "تفسير الآية",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تفسير ${if (selectedTafsirId == "tafsir_ibn_kathir") "ابن كثير" else "الجلالين"}:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedTafsirId == "tafsir_ibn_kathir") ayah.tafsirIbnKathir else ayah.tafsirJalalayn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }

            // Grammar details inside item card
            if (showIrab && ayah.irab.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Spellcheck,
                        contentDescription = "الإعراب النحوي",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الإعراب النحوي الصرفي للآية:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ayah.irab,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }

            // Reasons of Revelation box inside card
            if (showAsbabNuzul && ayah.asbabNuzul.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "سبب النزول",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "سبب النزول المأثور للآية الكريمة:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ayah.asbabNuzul,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }

            // Classification & Subjects
            if (showSubjects && ayah.subjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmarks,
                        contentDescription = "موضوعات الآية",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "تصنيفات وموضوعات الآية الكريمة:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ayah.subjects.split(",").forEach { topic ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = topic.trim(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                fontFamily = FontFamily.Serif,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            // Traditional horizontal Quick Actions bar containing ONLY the 4 requested features
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Tafsir Button
                ActionPill(
                    icon = Icons.Default.MenuBook,
                    label = "التفسير",
                    onClick = { onNavigateToDetails(ayah, 0) }
                )

                // 2. Irab Button
                ActionPill(
                    icon = Icons.Default.Spellcheck,
                    label = "الإعراب",
                    onClick = { onNavigateToDetails(ayah, 1) }
                )

                // 3. Asbab Nuzul Button
                ActionPill(
                    icon = Icons.Default.History,
                    label = "أسباب النزول",
                    onClick = { onNavigateToDetails(ayah, 3) }
                )

                // 4. Words breakdown Button
                ActionPill(
                    icon = Icons.Default.Translate,
                    label = "مفردات الآية",
                    onClick = { onNavigateToDetails(ayah, 2) }
                )
            }
        }
    }
}

// Custom Premium Action Pill component to replace the bulky unpolished AssistChips
@Composable
fun ActionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.MenuBook, // Fallback if needed but we specify fully qualified or generic
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
