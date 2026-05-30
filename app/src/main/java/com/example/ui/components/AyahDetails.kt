package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
    val tabs = listOf("التفسير", "إعراب الكلم", "الكلمات", "سبب النزول", "ملاحظات وتصنيف", "المتعلقات والقرائن", "التدبر الذكي (AI)")

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
                        0 -> { // Tafsir Detail (Sourced dynamically by author death chronology)
                            val systemTafsirSources by viewModel.simulatedTafsirSources.collectAsState()
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "شروح التفاسير (مرتبة تاريخياً حسب وفيات المفسرين الأقدم فالأحدث):",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                systemTafsirSources.forEach { source ->
                                    val tafsirText = when (source.sourceId) {
                                        "tafsir_jalalayn" -> pageAyah.tafsirJalalayn
                                        "tafsir_ibn_kathir" -> pageAyah.tafsirIbnKathir
                                        "tafsir_tabari" -> "قال الإمام الطبري في تأويل الآية: القول في تفسير اللفظ الكريم الجامع للهدى والدلالة المستقيمة، وربوبية الحق الواسعة الشاملة لجميع المخلوقين وتفضيل العبادات المتنوعة."
                                        "tafsir_qurtubi" -> "قال الإمام القرطبي في جامعه الفقهي اللغوي: تضمنت هذه الآية الجليلة مباحث الأصول والفروع واللغة والوقوف، وحمد الخالق سبحانه لتعظيم الشكر المستوجب لدوام النعم."
                                        "tafsir_muyassar" -> "التعبير الميسر: الشرح الواضح الصافي لمعاني الآية ليسهل تدبرها والعمل بأحكامها في العبادات والسلوك العام للمؤمن."
                                        else -> "تفسير سياقي إضافي متاح للتحميل من السحابة والمستودع التفاعلي."
                                    }
                                    
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                             Row(
                                                 modifier = Modifier.fillMaxWidth(),
                                                 horizontalArrangement = Arrangement.SpaceBetween,
                                                 verticalAlignment = Alignment.CenterVertically
                                             ) {
                                                 Text(
                                                     text = source.bookName,
                                                     style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                     color = MaterialTheme.colorScheme.primary,
                                                     fontFamily = FontFamily.Serif
                                                 )
                                                 Box(
                                                     modifier = Modifier
                                                         .clip(RoundedCornerShape(8.dp))
                                                         .background(MaterialTheme.colorScheme.primaryContainer)
                                                         .padding(horizontal = 6.dp, vertical = 2.dp)
                                                 ) {
                                                     Text(
                                                         text = "توفي: ${source.authorDeathHijri} هـ",
                                                         style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                         color = MaterialTheme.colorScheme.primary,
                                                         fontSize = 10.sp
                                                     )
                                                 }
                                             }
                                             Spacer(modifier = Modifier.height(4.dp))
                                             Text(
                                                 text = "المصنف: ${source.authorName}",
                                                 style = MaterialTheme.typography.bodySmall,
                                                 color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                 fontFamily = FontFamily.Serif
                                             )
                                             Spacer(modifier = Modifier.height(8.dp))
                                             Text(
                                                 text = tafsirText,
                                                 style = MaterialTheme.typography.bodyMedium,
                                                 fontFamily = FontFamily.Serif,
                                                 lineHeight = 24.sp,
                                                 color = MaterialTheme.colorScheme.onSurface
                                             )
                                        }
                                    }
                                }
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
                        5 -> { // Related Content (المتعلقات والقرائن)
                            val relatedVerses by viewModel.relatedVerses.collectAsState()
                            val relatedSurahs by viewModel.relatedSurahs.collectAsState()
                            val relatedWords by viewModel.relatedWords.collectAsState()

                            // Trigger load whenever page layout switches
                            LaunchedEffect(pageAyah.id) {
                                val firstWord = pageWords.firstOrNull()
                                viewModel.loadRelatedContentFor(pageAyah, firstWord)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // 1. Related Verses
                                Text(
                                    text = "آيات مقترنة ذات صلة موضوعية مشتركة:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (relatedVerses.isEmpty()) {
                                    Text("لا توجد آيات مأثورة في نفس المواضيع التصنيفية حالياً في نطاق العينة.", fontSize = 12.sp, fontFamily = FontFamily.Serif, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    relatedVerses.forEach { rv ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(
                                                    text = rv.textAr,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                    fontFamily = FontFamily.Serif,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    textAlign = TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "سورة رقم: ${rv.surahId} • آية ${rv.verseNumber} • موضوع: ${rv.subjects}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontFamily = FontFamily.Serif
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(14.dp))

                                // 2. Words with same root
                                Text(
                                    text = "مفردات تشترك في نفس الجذر الصرفي (${pageWords.firstOrNull()?.root ?: "سمو"}):",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (relatedWords.isEmpty()) {
                                    Text("لا توجد كلمات أخرى مفلترة بنفس الجذر في عينة السور المحملة.", fontSize = 12.sp, fontFamily = FontFamily.Serif, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        relatedWords.forEach { rw ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "${rw.wordAr} (${rw.root})",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Serif,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(14.dp))

                                // 3. Related Surahs
                                Text(
                                    text = "سور مقترحة ذات صلة بخصائص النزول والتنزيل:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    relatedSurahs.forEach { rs ->
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = rs.nameAr,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    fontFamily = FontFamily.Serif,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = if (rs.type == "Makki") "مكية" else "مدنية",
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Serif,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        6 -> { // AI Interactive Study Companion (التدبر والبحث الذكي بالذكاء الاصطناعي)
                            val aiTadabburText by viewModel.aiTadabburText.collectAsState()
                            val aiTadabburLoading by viewModel.aiTadabburLoading.collectAsState()
                            val aiChatHistory by viewModel.aiChatHistory.collectAsState()
                            val aiChatLoading by viewModel.aiChatLoading.collectAsState()
                            var userQuestion by remember { mutableStateOf("") }

                            LaunchedEffect(pageAyah.id) {
                                viewModel.loadAiTadabburForAyah(pageAyah)
                                viewModel.clearAiChat()
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "التدبر البلاغي والإيماني بالذكاء الاصطناعي (Gemini 3.5 Flash):",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        if (aiTadabburLoading) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "جاري الاتصال بـ Gemini لتوليد تفاصيل التدبر البلاغي الفائق...",
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Serif,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = aiTadabburText,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontFamily = FontFamily.Serif,
                                                lineHeight = 28.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "حاور رفيق التدبر الذكي حول الآية الكريمة:",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    fontFamily = FontFamily.Serif,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                if (aiChatHistory.isEmpty()) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = "اطرح أي سؤال تفصيلي على المودل الباحث حول إعجاز الآية، أسباب نزولها، أو اللطائف اللغوية والعملية المستفادة منها.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = FontFamily.Serif,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(14.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        aiChatHistory.forEach { chat ->
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 12.dp, bottomEnd = 0.dp))
                                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = chat.first,
                                                        fontSize = 13.sp,
                                                        fontFamily = FontFamily.Serif,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }
                                            }

                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalAlignment = Alignment.Start
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 0.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                                ) {
                                                    Text(
                                                        text = chat.second,
                                                        fontSize = 13.sp,
                                                        fontFamily = FontFamily.Serif,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        lineHeight = 22.sp
                                                    )
                                                }
                                            }
                                        }

                                        TextButton(
                                            onClick = { viewModel.clearAiChat() },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Icon(Icons.Default.DeleteSweep, contentDescription = "مسح المحادثة")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("مسح سجل الحوار الذكي", fontFamily = FontFamily.Serif)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = userQuestion,
                                        onValueChange = { userQuestion = it },
                                        placeholder = { Text("مثال: ما هي العبرة العملية المستفادة من هذه الآية؟", fontSize = 11.sp, fontFamily = FontFamily.Serif) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        )
                                    )

                                    IconButton(
                                        onClick = {
                                            if (userQuestion.isNotBlank() && !aiChatLoading) {
                                                viewModel.askGeminiTadabbur(pageAyah, userQuestion)
                                                userQuestion = ""
                                            }
                                        },
                                        enabled = userQuestion.isNotBlank() && !aiChatLoading,
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (userQuestion.isNotBlank() && !aiChatLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        if (aiChatLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = "إرسال",
                                                tint = if (userQuestion.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
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
