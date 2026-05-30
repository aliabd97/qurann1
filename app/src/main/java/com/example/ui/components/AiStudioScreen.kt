package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudioScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var subTab by remember { mutableStateOf("tafsir") } // "tafsir", "search", "text", "engines"
    
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val barColor = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val contentColor = if (isDark) MaterialTheme.colorScheme.primary else Color.White

    // Helper to copy text with Toast alert
    val copyToClipboard = { text: String ->
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Tebyan Copied Text", text)
        clipboard.setPrimaryClip(clip)
        android.widget.Toast.makeText(context, "تم النسخ بنجاح إلى الحافظة! ✅", android.widget.Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مساعد التبيان بالذكاء الاصطناعي", color = contentColor, fontWeight = FontWeight.Bold) },
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
            // Horizontal Segmented Sub-Tabs
            ScrollableTabRow(
                selectedTabIndex = when (subTab) {
                    "tafsir" -> 0
                    "search" -> 1
                    "text" -> 2
                    else -> 3
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 8.dp
            ) {
                Tab(
                    selected = (subTab == "tafsir"),
                    onClick = { subTab = "tafsir" },
                    text = { Text("دمج وتلخيص التفاسير", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = (subTab == "search"),
                    onClick = { subTab = "search" },
                    text = { Text("البحث الذكي الدلالي", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = (subTab == "text"),
                    onClick = { subTab = "text" },
                    text = { Text("تلخيص وتحليل النصوص", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = (subTab == "engines"),
                    onClick = { subTab = "engines" },
                    text = { Text("محركات الموديلات", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                when (subTab) {
                    "tafsir" -> TafsirAggregationSubView(viewModel, copyToClipboard)
                    "search" -> SmartSemanticSearchSubView(viewModel, copyToClipboard)
                    "text" -> CustomTextSummarizerSubView(viewModel, copyToClipboard)
                    "engines" -> ModelsCabinetSubView(viewModel)
                }
            }
        }
    }
}

@Composable
fun TafsirAggregationSubView(
    viewModel: QuranViewModel,
    onCopy: (String) -> Unit
) {
    val surahs by viewModel.surahs.collectAsState()
    val ayahs by viewModel.ayahs.collectAsState()
    val summaryResult by viewModel.aggregatedTafsirSummary.collectAsState()
    val isLoading by viewModel.aggregationLoading.collectAsState()

    var selectedSurahId by remember { mutableStateOf(2) } // Default Bakarah
    var verseNumberString by remember { mutableStateOf("155") } // Default verse
    
    var surahMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Book, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "قم باختيار السورة ورقم الآية لدمج وتلخيص تفاسير السلف الخمسة المعتمدة (ابن كثير، الطبري، القرطبي، الجلالين، الميسر) في خلاصة موحدة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Selection elements (Surah & Verse Input)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Dropdown selector
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { surahMenuExpanded = true }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val selectedSurahName = surahs.find { it.id == selectedSurahId }?.nameAr ?: "سورة البقرة"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedSurahName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "")
                }
                
                DropdownMenu(
                    expanded = surahMenuExpanded,
                    onDismissRequest = { surahMenuExpanded = false }
                ) {
                    surahs.forEach { surah ->
                        DropdownMenuItem(
                            text = { Text("${surah.id}. ${surah.nameAr}") },
                            onClick = {
                                selectedSurahId = surah.id
                                surahMenuExpanded = false
                            }
                        )
                    }
                }
            }

            // Verse textfield Input
            OutlinedTextField(
                value = verseNumberString,
                onValueChange = { verseNumberString = it },
                label = { Text("رقم الآية") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                val cleanedVerseNum = verseNumberString.toIntOrNull() ?: 1
                viewModel.aggregateAndSummarizeTafsir(selectedSurahId, cleanedVerseNum)
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = "")
                Spacer(modifier = Modifier.width(8.dp))
                Text("توليد التلخيص والدمج الموحد عينة تفاسير الآية", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (summaryResult.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الخلاصة التفسيرية المدمجة:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row {
                            IconButton(onClick = { onCopy(summaryResult) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الخلاصة", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = summaryResult,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}

@Composable
fun SmartSemanticSearchSubView(
    viewModel: QuranViewModel,
    onCopy: (String) -> Unit
) {
    val searchResult by viewModel.smartSearchResult.collectAsState()
    val isLoading by viewModel.smartSearchLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Psychology, contentDescription = "", tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ابحث دلالياً وسلوكياً عن قضايا في القرآن الكريم (مثال: 'حث الوالدين ومكانتهم'، 'أحكام الصبر والشدائد'، 'العدل والصدقة').",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("اكتب موضوعاً أو مبدأ قرآني للسؤال عنه...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "")
                    }
                }
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Suggestion chips
            val suggestions = listOf("آيات الصبر", "الإنفاق والإحسان", "التفكر والتدبر")
            suggestions.forEach { sug ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { searchQuery = sug }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(sug, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Button(
            onClick = { viewModel.executeSmartSearch(searchQuery) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.Search, contentDescription = "")
                Spacer(modifier = Modifier.width(8.dp))
                Text("إجراء البحث الدلالي بالذكاء الاصطناعي", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (searchResult.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تقرير الدلالة والإرشاد المستنبط:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        IconButton(onClick = { onCopy(searchResult) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ نتائج البحث", tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = searchResult,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}

@Composable
fun CustomTextSummarizerSubView(
    viewModel: QuranViewModel,
    onCopy: (String) -> Unit
) {
    val textToSummarize by viewModel.customTextToSummarize.collectAsState()
    val summaryResult by viewModel.customSummaryResult.collectAsState()
    val isLoading by viewModel.customSummaryLoading.collectAsState()

    var inputString by remember { mutableStateOf(textToSummarize) }
    var selectedStyle by remember { mutableStateOf("simple") } // "simple", "bullet", "deep"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Summarize, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "قم بلصق أو إدخال أي نص شرعي أو مقالة أو تفسير خارجي وتلخيصه بالذكاء الاصطناعي على الفور بالنمط الذي ترغب فيه.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = inputString,
            onValueChange = { 
                inputString = it
                viewModel.setCustomTextToSummarize(it)
            },
            placeholder = { Text("الصق النص هنا للتلخيص بالذكاء الاصطناعي...") },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Summarization Style Selector
        Text("اختر نمط صياغة التلخيص:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val styles = listOf(
                Triple("simple", "شرح ميسر ومختصر", Icons.Default.ChatBubbleOutline),
                Triple("bullet", "نقاط أساسية بليغة", Icons.Default.FormatListNumbered),
                Triple("deep", "دراسة تحليلية عميقة", Icons.Default.FindInPage)
            )

            styles.forEach { (mode, label, icon) ->
                val isSelected = (selectedStyle == mode)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedStyle = mode },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(icon, contentDescription = "", tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.summarizeCustomText(inputString, selectedStyle) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = "")
                Spacer(modifier = Modifier.width(8.dp))
                Text("بدء التحليل والتلخيص الفوري", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (summaryResult.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تقرير التلخيص الناتج:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { onCopy(summaryResult) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ التلخيص", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = summaryResult,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Serif
                    )
                }
            }
        }
    }
}

@Composable
fun ModelsCabinetSubView(
    viewModel: QuranViewModel
) {
    val currentAiMode by viewModel.aiEngineMode.collectAsState()
    
    // Nano compatibility state
    val isNanoCompatible by viewModel.isNanoCompatible.collectAsState()
    val isCheckingNano by viewModel.nanoCheckingState.collectAsState()

    // Qwen model state
    val qwenDownloadState by viewModel.qwenDownloadState.collectAsState()
    val qwenProgress by viewModel.qwenDownloadProgress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "بيئة الذكاء الاصطناعي الأوفلاين (بدون إنترنت)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "يعمل مساعد التبيان بالكامل على هاتفك دون حاجة للاتصال بالإنترنت مطلقاً لضمان السرية التامة والاستقلالية. يقوم التطبيق بفحص مواصفات وموديل هاتفك لتفعيل أفضل محرك متاح لمشابك أو تفسير ولطائف الآية الكريمة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }

        Text("اختر محرك الذكاء الاصطناعي الفعال حالياً:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.secondary)

        // 1. Gemini Nano Engine (AICore Native Core)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setAiEngineMode("local_nano") },
            colors = CardDefaults.cardColors(
                containerColor = if (currentAiMode == "local_nano" || currentAiMode == "local_gemini_nano") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, if (currentAiMode == "local_nano" || currentAiMode == "local_gemini_nano") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Memory, contentDescription = "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("محرك النظام المدمج: Gemini Nano", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text("خفيف وموفر للغاية ومدمج بنظام الأندرويد سلفاً.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    RadioButton(
                        selected = (currentAiMode == "local_nano" || currentAiMode == "local_gemini_nano"),
                        onClick = { viewModel.setAiEngineMode("local_nano") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(bottom = 8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("بحجم تطبيق إضافي: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("0 ميجابايت (مدمج أصلياً)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }

                    Button(
                        onClick = { viewModel.checkNanoCompatibility() },
                        enabled = !isCheckingNano,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("إعادة فحص التوافقية", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (isCheckingNano) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isNanoCompatible == true) Color(0xFF2E7D32).copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                            )
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isNanoCompatible == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "",
                                tint = if (isNanoCompatible == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isNanoCompatible == true)
                                    "تهانينا! هاتفك يدعم تشغيل الـ Gemini Nano وموفّر فوراً بالكامل دون أي حجم إضافي."
                                else
                                    "الهاتف لا يدعم تشغيل Gemini Nano محلياً (هاتف متوسط أو قديم). نوصي بتنشيط وتحميل الموديل Qwen بالكرت أدناه.",
                                fontSize = 11.sp,
                                color = if (isNanoCompatible == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Qwen-2.5-0.5B Engine (On-Demand 300MB download)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    if (qwenDownloadState == "downloaded") {
                        viewModel.setAiEngineMode("local_qwen")
                    }
                },
            colors = CardDefaults.cardColors(
                containerColor = if (currentAiMode == "local_qwen") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, if (currentAiMode == "local_qwen") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.DownloadForOffline, contentDescription = "", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("بديل التحميل عند الطلب: Qwen-2.5-0.5B", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("خفيف ومحكم (للهواتف المتوسطة والقديمة لتوفر نفس القيمة مجاناً أوفلاين).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    RadioButton(
                        selected = (currentAiMode == "local_qwen"),
                        enabled = (qwenDownloadState == "downloaded"),
                        onClick = { viewModel.setAiEngineMode("local_qwen") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(bottom = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("بحجم تحميل تنزيل: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("300 ميجابايت تقريباً", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    if (qwenDownloadState == "downloaded") {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2E7D32).copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("جاهز ومثبت ✓", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedVisibility(visible = qwenDownloadState == "not_downloaded") {
                    Button(
                        onClick = { viewModel.startDownloadingQwen() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تنزيل نموذج Qwen-2.5-0.5B المحلي (300MB) ⬇", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                AnimatedVisibility(visible = qwenDownloadState == "downloading") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("جاري تنزيل الموديل الذكي ليعمل بذاكرة الجهاز الحرة...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { qwenProgress / 100f },
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("اكتمال النزول: $qwenProgress%", fontSize = 10.sp, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                    }
                }

                AnimatedVisibility(visible = qwenDownloadState == "downloaded") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الموديل يعمل كآلة معالجة ذكية محلياً تماماً.", fontSize = 11.sp, color = Color(0xFF2E7D32))
                        TextButton(
                            onClick = { viewModel.deleteQwenModel() },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف الملف وتحرير المساحة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
