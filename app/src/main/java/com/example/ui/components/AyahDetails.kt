package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.data.WordEntity
import com.example.ui.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahDetailsScreen(
    ayah: AyahEntity,
    viewModel: QuranViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialTab: Int = 0
) {
    val ayahs by viewModel.ayahs.collectAsState()
    val allWords by viewModel.allAyahWords.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    // Slide/Swipe Ayat implementation
    val initialPage = remember(ayahs) { ayahs.indexOfFirst { it.id == ayah.id }.coerceAtLeast(0) }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { ayahs.size }
    )

    var selectedTabIndex by remember(initialTab) { mutableStateOf(initialTab) }
    val tabs = listOf("التفسير", "إعراب الكلم", "الكلمات", "سبب النزول", "ملاحظات وتصنيف")

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val barColor = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val contentColor = if (isDark) MaterialTheme.colorScheme.primary else Color.White

    // Get the current active Ayah based on swiping position
    val currentAyah = ayahs.getOrNull(pagerState.currentPage) ?: ayah
    val words = allWords[currentAyah.id] ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "تفاصيل الآية ${currentAyah.verseNumber}", 
                        color = contentColor, 
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = contentColor)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmark(currentAyah.id) }) {
                        Icon(
                            imageVector = if (bookmarks.contains(currentAyah.id)) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "تفضيل",
                            tint = if (bookmarks.contains(currentAyah.id)) Color(0xFFD4AF37) else contentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = barColor)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Display Verse Card header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentAyah.textAr,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 36.sp
                        ),
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "﴿آية ${currentAyah.verseNumber}﴾",
                        fontFamily = FontFamily.Serif,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tab Rows
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = (selectedTabIndex == index),
                        onClick = { selectedTabIndex = index },
                        text = { 
                            Text(
                                title, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Serif
                            ) 
                        }
                    )
                }
            }

            // Swipable horizontal container for details
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                val pageAyah = ayahs.getOrNull(page) ?: currentAyah
                val pageWords = allWords[pageAyah.id] ?: emptyList()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> { // Tafsir Detail
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "تفسير الجلالين للآية الكريمة:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pageAyah.tafsirJalalayn, 
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 26.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "تفسير الحافظ ابن كثير:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = pageAyah.tafsirIbnKathir, 
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 26.sp
                                )
                            }
                        }
                        1 -> { // Irab Grammar Details
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "البيان النحوي الصرفي للآية:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(
                                        text = if (pageAyah.irab.isNotEmpty()) pageAyah.irab else "لم يتم تحميل حزمة الإعراب لهذه السورة الكريمة بعد.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = FontFamily.Serif,
                                        modifier = Modifier.padding(16.dp),
                                        lineHeight = 28.sp
                                    )
                                }
                            }
                        }
                        2 -> { // Words analysis cards list
                            if (pageWords.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("لا توجد مفردات مخزنة لهذه الآية الكريمة.", fontFamily = FontFamily.Serif)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(pageWords) { word ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = word.wordAr,
                                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                        fontFamily = FontFamily.Serif,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "معناها: ${word.wordEn}",
                                                        fontFamily = FontFamily.Serif,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                DetailsWordAnalysisRow(label = "النوع والصرف:", value = word.grammarTags)
                                                DetailsWordAnalysisRow(label = "الأصل اللغوي:", value = word.lemma)
                                                DetailsWordAnalysisRow(label = "الجذر الصرفي:", value = word.root)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> { // Revelation Causes
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "أسباب النزول المأثورة للآية الكريمة:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Text(
                                        text = if (pageAyah.asbabNuzul.isNotEmpty()) pageAyah.asbabNuzul else "لم تذكر روايات النزول سبباً مخصوصاً نزلت لأجله هذه الآية الكريمة بعينها.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = FontFamily.Serif,
                                        modifier = Modifier.padding(16.dp),
                                        lineHeight = 28.sp
                                    )
                                }
                            }
                        }
                        4 -> { // Notes & Topics check lists
                            // Load Note scoped to current page tab
                            var localNoteText by remember(pageAyah.id) { mutableStateOf("") }
                            val activeNote by viewModel.activeAyahNote.collectAsState()

                            LaunchedEffect(pageAyah.id) {
                                viewModel.loadNoteForSelectedAyah(pageAyah.id)
                            }
                            
                            LaunchedEffect(activeNote) {
                                localNoteText = activeNote ?: ""
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "مواضيع وتصنيفات الآية الكريمة:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    pageAyah.subjects.split(",").forEach { label ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = label.trim(),
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                fontFamily = FontFamily.Serif,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "كتابة ملاحظة وتدبر حول الآية الكريمة:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = localNoteText,
                                    onValueChange = { localNoteText = it },
                                    placeholder = { Text("اكتب تدبراتك وملاحظاتك الشخصية حول الآية هنا...", fontFamily = FontFamily.Serif) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        viewModel.saveNote(pageAyah.id, localNoteText)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = "حفظ", tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("حفظ الملاحظة الكريمة", color = Color.White, fontFamily = FontFamily.Serif)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailsWordAnalysisRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
