package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
fun ContentFilterSheet(
    viewModel: QuranViewModel,
    onDismiss: () -> Unit
) {
    val showTafsir by viewModel.showTafsir.collectAsState()
    val showIrab by viewModel.showIrab.collectAsState()
    val showWords by viewModel.showWords.collectAsState()
    val showSubjects by viewModel.showSubjects.collectAsState()
    val showAsbabNuzul by viewModel.showAsbabNuzul.collectAsState()
    val showTranslation by viewModel.showTranslation.collectAsState()
    val selectedTafsirId by viewModel.selectedTafsirId.collectAsState()
    val packages by viewModel.packages.collectAsState()

    val installedTafsirs = packages.filter { it.type == "Tafsir" && it.isInstalled }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "خيارات تخصيص عرض الآيات",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "مكونات بطاقة الآية النشطة:",
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            )
            Spacer(modifier = Modifier.height(10.dp))

            FilterSwitchRow(title = "تحليل الألفاظ والكلمات (كلمة تلو أخرى)", checked = showWords) {
                viewModel.toggleFilter("words")
            }
            FilterSwitchRow(title = "إظهار الترجمة الإنجليزية الفورية", checked = showTranslation) {
                viewModel.toggleFilter("translation")
            }
            FilterSwitchRow(title = "عرض كتاب التفسير المختار بأسفل الآية", checked = showTafsir) {
                viewModel.toggleFilter("tafsir")
            }
            FilterSwitchRow(title = "عرض الإعراب النحوي المفصّل بأسفل الآية", checked = showIrab) {
                viewModel.toggleFilter("irab")
            }
            FilterSwitchRow(title = "إظهار أسباب النزول المأثورة بأسفل الآية", checked = showAsbabNuzul) {
                viewModel.toggleFilter("asbab")
            }
            FilterSwitchRow(title = "إظهار موضوعات وتصنيفات الآية الشرعية", checked = showSubjects) {
                viewModel.toggleFilter("subjects")
            }

            if (showTafsir && installedTafsirs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "كتاب التفسير للتصفح السريع:",
                    fontFamily = FontFamily.Serif,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                    items(installedTafsirs) { tafsir ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectTafsir(tafsir.packageId) }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = (selectedTafsirId == tafsir.packageId),
                                onClick = { viewModel.selectTafsir(tafsir.packageId) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = tafsir.name,
                                fontFamily = FontFamily.Serif,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("تطبيق وتحديث العرض", color = Color.White, fontFamily = FontFamily.Serif)
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun FilterSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailsSheet(
    word: WordEntity,
    allWords: List<WordEntity>,
    onDismiss: () -> Unit
) {
    // Elegant swipable word pager setup
    val initialPage = remember(allWords, word) { 
        allWords.indexOfFirst { it.id == word.id }.coerceAtLeast(0) 
    }
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { allWords.size }
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "تحليل وتفصيل مفردات الآية الكريمة",
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            Text(
                text = "« اسحب يمنة أو يسرة للتنقل بين كلمات الآية الجارية »",
                fontSize = 11.sp,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 350.dp)
            ) { page ->
                val currentWordInPage = allWords.getOrNull(page) ?: word
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Arabic Word Display Box
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = currentWordInPage.wordAr,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            fontFamily = FontFamily.Serif,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // English Translation / Root Details
                    Text(
                        text = "المعنى والسياق: ${currentWordInPage.wordEn}",
                        fontFamily = FontFamily.Serif,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Grammar analysis rows
                    DialogWordAnalysisRow(label = "البيان والوسم الصرفي:", value = currentWordInPage.grammarTags)
                    DialogWordAnalysisRow(label = "الأصل اللغوي الفصيح:", value = currentWordInPage.lemma)
                    DialogWordAnalysisRow(label = "جذر الكلمة الصرفي:", value = "« ${currentWordInPage.root} »")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action section
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("إغلاق التبيان", fontFamily = FontFamily.Serif, color = Color.White)
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
fun DialogWordAnalysisRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Serif,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyahActionSheet(
    ayah: AyahEntity,
    viewModel: QuranViewModel,
    onOptionSelected: (String) -> Unit, // "tafsir", "irab", "notes", "copy", "details", "asbab", "subjects"
    onDismiss: () -> Unit
) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    val isBookmarked = bookmarks.contains(ayah.id)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "خيارات والتدبر في الآية ${ayah.verseNumber}",
                fontFamily = FontFamily.Serif,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = ayah.textAr,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    ),
                    fontFamily = FontFamily.Serif,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Unified, premium options items grid (WITHOUT dead audio "play" options)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                val items = listOf(
                    Triple("التفسير ومأثوره", Icons.AutoMirrored.Filled.Comment, "tafsir"),
                    Triple(if (isBookmarked) "تفضيل الآية ✓" else "تفضيل الآية", Icons.Default.Star, "bookmark"),
                    Triple("كتابة تدبر", Icons.Default.Edit, "notes"),
                    Triple("نسخ الآية الكريمة", Icons.Default.ContentCopy, "copy"),
                    Triple("التبيان المفصل", Icons.AutoMirrored.Filled.List, "details"),
                    Triple("إعراب الآية", Icons.AutoMirrored.Filled.MenuBook, "irab"),
                    Triple("أسباب النزول", Icons.Default.HistoryEdu, "asbab"),
                    Triple("موضوعات الآية", Icons.Default.AutoAwesome, "subjects")
                )

                items(items) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(item.third) }
                            .height(84.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.third == "bookmark" && isBookmarked) 
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.second,
                                contentDescription = item.first,
                                tint = if (item.third == "bookmark" && isBookmarked)
                                    MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.first,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إلغاء وتراجع", fontFamily = FontFamily.Serif)
            }
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}
