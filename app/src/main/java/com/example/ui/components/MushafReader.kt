package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
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
import com.example.ui.QuranViewModel

@Composable
fun MushafReaderScreen(
    viewModel: QuranViewModel,
    onNavigateToPackages: () -> Unit,
    onNavigateToDetails: (AyahEntity, Int) -> Unit,
    onOpenSurahIndex: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentPageNumber by viewModel.currentPageNumber.collectAsState()
    val ayahs by viewModel.ayahs.collectAsState()
    val currentSurahId by viewModel.currentSurahId.collectAsState()
    val surahs by viewModel.surahs.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val playingAyahId by viewModel.playingAyahId.collectAsState()

    val currentSurah = surahs.find { it.id == currentSurahId }

    // Swipable Page (HorizontalPager from page 1 to 604)
    val pagerState = rememberPagerState(
        initialPage = (currentPageNumber - 1).coerceIn(0, 603),
        pageCount = { 604 }
    )

    // Sync from swiping page (RTL index: standard swipe increases standard page index)
    LaunchedEffect(pagerState.currentPage) {
        val targetPage = pagerState.currentPage + 1
        if (currentPageNumber != targetPage) {
            viewModel.selectPage(targetPage)
        }
    }

    // Sync from view model changes
    LaunchedEffect(currentPageNumber) {
        val targetIndex = currentPageNumber - 1
        if (pagerState.currentPage != targetIndex) {
            pagerState.scrollToPage(targetIndex)
        }
    }

    Scaffold(
        topBar = {
            ReaderTopBar(
                title = currentSurah?.let { "صفحة المصحف $currentPageNumber - ${it.nameAr}" } ?: "مصحف التبيان",
                currentMode = "mushaf",
                onModeChange = { viewModel.setReadingMode(it) },
                onIndexClick = onOpenSurahIndex,
                onPackagesClick = onNavigateToPackages
            )
        },
        bottomBar = {
            MushafBottomBar(
                pageNumber = currentPageNumber,
                onSliderChange = { viewModel.selectPage(it.toInt()) }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.background else Color(0xFFFBF9F3)) // Traditional Parchment tone
        ) {
            // Mushaf Header strip inside page
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentSurah?.nameAr ?: "",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "الجزء ${1 + (currentPageNumber / 20)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Elegant Swipable Pager container
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                // Render content ONLY for the currently active page to maintain fluid 60fps
                if (page == pagerState.currentPage) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (androidx.compose.foundation.isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else Color.White)
                            .padding(1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (ayahs.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator()
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("جاري تحميل الصفحة الكريمة...", fontFamily = FontFamily.Serif)
                                    }
                                }
                            } else {
                                // Render Basmala if start of a new Surah (verse number = 1)
                                val hasFirstVerse = ayahs.any { it.verseNumber == 1 }
                                if (hasFirstVerse && currentSurahId != 9 && currentSurahId != 1) {
                                    Text(
                                        text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        ),
                                        fontFamily = FontFamily.Serif,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                }

                                // Display Verses in continuous text format
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(14.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    ayahs.forEach { ayah ->
                                        val isBookmarked = bookmarks.contains(ayah.id)
                                        val isPlaying = playingAyahId == ayah.id
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    else if (isBookmarked) MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                                                    else Color.Transparent
                                                )
                                                .clickable { viewModel.selectAyah(ayah) }
                                                .padding(8.dp)
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                Text(
                                                    text = "${ayah.textAr} ﴿${ayah.verseNumber}﴾",
                                                    style = MaterialTheme.typography.headlineSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        lineHeight = 44.sp,
                                                        textAlign = TextAlign.Right
                                                    ),
                                                    fontFamily = FontFamily.Serif,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                if (ayah.translation.isNotEmpty()) {
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = ayah.translation,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        textAlign = TextAlign.Left,
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Page transitions indicator placeholder
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "صفحة ${page + 1}",
                            fontSize = 32.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet Manager
    val activeAyah by viewModel.selectedAyah.collectAsState()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    if (activeAyah != null) {
        AyahActionSheet(
            ayah = activeAyah!!,
            viewModel = viewModel,
            onOptionSelected = { option ->
                val selected = activeAyah!!
                viewModel.selectAyah(null) // Dismiss sheet
                when (option) {
                    "copy" -> {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(selected.textAr))
                        android.widget.Toast.makeText(context, "تم نسخ الآية الكريمة بنجاح", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    "bookmark" -> {
                        viewModel.toggleBookmark(selected.id)
                    }
                    "tafsir" -> {
                        onNavigateToDetails(selected, 0)
                    }
                    "irab" -> {
                        onNavigateToDetails(selected, 1)
                    }
                    "words" -> {
                        onNavigateToDetails(selected, 2)
                    }
                    "asbab" -> {
                        onNavigateToDetails(selected, 3)
                    }
                    else -> {
                        onNavigateToDetails(selected, 0)
                    }
                }
            },
            onDismiss = { viewModel.selectAyah(null) }
        )
    }
}

@Composable
fun MushafBottomBar(
    pageNumber: Int,
    onSliderChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onSliderChange((pageNumber - 1).coerceAtLeast(1).toFloat()) }) {
                    Icon(Icons.AutoMirrored.Filled.NavigateBefore, contentDescription = "السابق", tint = MaterialTheme.colorScheme.primary)
                }
                Text(
                    text = "صفحة $pageNumber / 604",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = { onSliderChange((pageNumber + 1).coerceAtMost(604).toFloat()) }) {
                    Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "التالي", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Slider(
                value = pageNumber.toFloat(),
                onValueChange = onSliderChange,
                valueRange = 1f..604f,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
