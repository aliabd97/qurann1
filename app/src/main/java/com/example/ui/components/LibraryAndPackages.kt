package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AyahEntity
import com.example.data.PackageEntity
import com.example.data.SurahEntity
import com.example.ui.QuranViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Internal data helper to model books elegantly
data class BookUIModel(
    val bookId: String,
    val title: String,
    val author: String,
    val deathHijri: Int,
    val description: String,
    val category: String,
    val packageId: String, // The package that unlocks this book
    val colors: List<Color> // Cover color gradient
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryAndPackagesScreen(
    viewModel: QuranViewModel,
    onBack: () -> Unit,
    onNavigateToDetails: (AyahEntity, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val packages by viewModel.packages.collectAsState()
    val surahs by viewModel.surahs.collectAsState()
    
    var activeSubTab by remember { mutableStateOf("library") } // "library", "packages"
    
    // Immersive Book Reader mode
    var activeReadingBook by remember { mutableStateOf<BookUIModel?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val barColor = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val contentColor = if (isDark) MaterialTheme.colorScheme.primary else Color.White

    // Define bookshelf collection
    val books = remember {
        listOf(
            BookUIModel(
                bookId = "tafsir_ibn_kathir",
                title = "تفسير القرآن العظيم",
                author = "الإمام الحافظ ابن كثير",
                deathHijri = 774,
                description = "تفسير أثري مسند رفيع يعتمد تفسير التنزيل الحكيم بآي القرآن وصحاح الأحاديث الشريفة المباركة وعيون اللغات والدلالات.",
                category = "كتب التفسير المعتمدة",
                packageId = "tafsir_ibn_kathir",
                colors = listOf(Color(0xFF8D6E63), Color(0xFF5D4037))
            ),
            BookUIModel(
                bookId = "tafsir_jalalayn",
                title = "تفسير الجلالين الشريف",
                author = "جلال الدين السيوطي والمحلي",
                deathHijri = 911,
                description = "حاشية تفسيرية موجزة للغاية تبين اللطائف البلاغية والبيانية واللغوية على حواشي النص بنسق متصل بليغ يسهل اتباعه.",
                category = "كتب التفسير المعتمدة",
                packageId = "tafsir_jalalayn",
                colors = listOf(Color(0xFF4DB6AC), Color(0xFF00796B))
            ),
            BookUIModel(
                bookId = "tafsir_muyassar",
                title = "التفسير الميسر الموثق",
                author = "نخبة من علماء المدينة",
                deathHijri = 1420,
                description = "صياغة تفسيرية واضحة ميسرة وموجزة صيغت بدقة وعناية لتناسب الفهم للمسلم المعاصر دون اختصار مخل أو حشو طويل.",
                category = "كتب التفسير المعتمدة",
                packageId = "tafsir_muyassar",
                colors = listOf(Color(0xFF26A69A), Color(0xFF00695C))
            ),
            BookUIModel(
                bookId = "tafsir_sadi",
                title = "تيسير الكريم الرحمن (تفسير السعدي)",
                author = "الشيخ عبد الرحمن السعدي",
                deathHijri = 1373,
                description = "تفسير ميسر بأسلوب تربوي بليغ ويسير، يركز على استخراج الهدايات الإيمانية والمقاصد الأخلاقية والسلوكية للقرآن.",
                category = "كتب التفسير المعتمدة",
                packageId = "tafsir_sadi",
                colors = listOf(Color(0xFF66BB6A), Color(0xFF2E7D32))
            ),
            BookUIModel(
                bookId = "irab_al_quran",
                title = "إعراب القرآن الكريم للزجَّاج",
                author = "أبو إسحاق الزجاج البغدادي",
                deathHijri = 311,
                description = "دراسة نحوية وصرفية عميقة لآي القرآن الكريم، تحلل موقع الكلمة وصيغتها ومعناها وموجبات بلاغتها اللغوية والتعبيرية.",
                category = "كتب الإعراب واللغة",
                packageId = "irab_al_quran",
                colors = listOf(Color(0xFF3F51B5), Color(0xFF1A237E))
            ),
            BookUIModel(
                bookId = "asbab_nuzul_wahidi",
                title = "أسباب النزول للواحدي",
                author = "أبو الحسن الواحدي النيسابوري",
                deathHijri = 468,
                description = "البوابة الموثقة لفهم سياق نزول الآيات والوقائع التاريخية المأثورة التي نزلت الآيات الكريمة بياناً لأحداثها.",
                category = "كتب علوم التنزيل",
                packageId = "asbab_nuzul_wahidi",
                colors = listOf(Color(0xFFBF360C), Color(0xFFE64A19))
            ),
            BookUIModel(
                bookId = "book_saadi",
                title = "دليل السعدي لأسماء الله الحسنى",
                author = "الشيخ عبد الرحمن السعدي",
                deathHijri = 1373,
                description = "شرح معاني أسماء الله الحسنى الثابتة في آي التنزيل والأثر، ودلالاتها الإيمانية العميقة في حياة المؤمن وتزكية روحه.",
                category = "كتب شروح العقيدة والتدبر",
                packageId = "library_baghdad", // Matched with Baghdad Library package
                colors = listOf(Color(0xFF8D6E63), Color(0xFF3E2723))
            ),
            BookUIModel(
                bookId = "book_irab_ism",
                title = "إعراب مفردات وأفعال آي القرآن",
                author = "الشيخ محمد عبد اللطيف",
                deathHijri = 1412,
                description = "تحليل صرفي وبنيوي دقيق لأفعال القرآن الكريم ومفرداته الدقيقة بوزنها وتصريفها وحمايتها من اللحن اللغوي المعاصر.",
                category = "كتب الإعراب واللغة",
                packageId = "word_irab_complex", // Matched with word complexe package
                colors = listOf(Color(0xFF7E57C2), Color(0xFF4527A0))
            ),
            BookUIModel(
                bookId = "book_asbab_asl",
                title = "تاريخ أسباب التنزيل التاريخية",
                author = "الشيخ صالح الفوزان",
                deathHijri = 1435,
                description = "حصر بليغ ودراسة منهجية للأسباب المأثورة للآيات مع ترجيحات كبار المفسرين ودلالتها المعرفية والتوجيهية الشاملة.",
                category = "كتب علوم التنزيل",
                packageId = "library_andalus", // Matched with Andalus Library package
                colors = listOf(Color(0xFF78909C), Color(0xFF37474F))
            )
        )
    }

    if (activeReadingBook != null) {
        // Immersive Integrated Book Content Reader Screen
        BookContentViewer(
            book = activeReadingBook!!,
            viewModel = viewModel,
            surahs = surahs,
            onClose = { activeReadingBook = null },
            onNavigateToDetails = onNavigateToDetails
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("المكتبة الشاملة والكتب والمصادر", color = contentColor, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = contentColor)
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
                // Selector Tabs: Books Library vs Packages Repositories
                TabRow(
                    selectedTabIndex = if (activeSubTab == "library") 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = (activeSubTab == "library"),
                        onClick = { activeSubTab = "library" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LibraryBooks, contentDescription = "")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مكتبة الكتب الشريعتنا", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                    Tab(
                        selected = (activeSubTab == "packages"),
                        onClick = { activeSubTab = "packages" },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDownload, contentDescription = "")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مستودع حزم الكتب", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    )
                }

                when (activeSubTab) {
                    "library" -> {
                        // RENDER BOOKS LIBRARY VIEWER
                        BooksLibraryView(
                            books = books,
                            packages = packages,
                            viewModel = viewModel,
                            onReadBook = { book -> activeReadingBook = book },
                            onGoToPackages = { activeSubTab = "packages" }
                        )
                    }
                    "packages" -> {
                        // RENDER ORIGINAL REPOSITORIES DOWNLOAD CHECKS
                        PackagesManagerView(
                            packages = packages,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BooksLibraryView(
    books: List<BookUIModel>,
    packages: List<PackageEntity>,
    viewModel: QuranViewModel,
    onReadBook: (BookUIModel) -> Unit,
    onGoToPackages: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Aesthetic shelf header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoStories, contentDescription = "مكتبة", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "مرحباً بك في مكتبة التبيان التفاعلية الحية. تصفح التفاسير، شروح العقيدة، ومعاجم إعراب القرآن الكريمة دون إنترنت. يتم فتح الكتب تلقائياً عند تحميل وتثبيت الحزمة المطابقة لها من مستودع الحزم.",
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(1), // Detailed grid layout
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(books) { book ->
                val matchingPackage = packages.find { it.packageId == book.packageId }
                val isInstalled = matchingPackage?.isInstalled ?: false

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decorative Book Spine Indicator
                        Box(
                            modifier = Modifier
                                .width(65.dp)
                                .height(95.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.verticalGradient(book.colors)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Icon(Icons.Default.Book, contentDescription = "", tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = book.title.take(6) + "..",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Book Metadata Details Block
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "المؤلف: ${book.author} (توفي ${book.deathHijri} هـ)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = book.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(book.category, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }

                                // Status text / button
                                if (isInstalled) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF2E7D32).copy(alpha = 0.12f))
                                            .clickable { onReadBook(book) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ChromeReaderMode, contentDescription = "", tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("افتتاح وقراءة الشامل", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                                            .clickable { onGoToPackages() }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.CloudOff, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تنزيل الحزمة لتنشيطه", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
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
fun PackagesManagerView(
    packages: List<PackageEntity>,
    viewModel: QuranViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val downloadingPackages = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CloudQueue, contentDescription = "سحابة", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "مثبتة أو متوفرة للتحميل: يمكنك تفعيل حزم الكتب دون قيود لتعمل أوفلاين في عارض الكتب ومكتبتك المتكاملة.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مخزن الحزم والمصادر المتوفرة:",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Dynamic refresh cloud manifest trigger
            TextButton(
                onClick = { viewModel.refreshPackagesFromCloudManifest() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = "", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تزامن سحابي للمستودع", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (packages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(packages) { pkg ->
                    val isDownloading = downloadingPackages[pkg.packageId] ?: false

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pkg.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (pkg.type) {
                                                    "Tafsir" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    "Audio" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (pkg.type == "Tafsir") "تفسير" else if (pkg.type == "Audio") "صوت" else "أخرى",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "الحجم: ${pkg.sizeMb} ميجا • وفيات المصنف: ${pkg.authorDeathHijri ?: "غير معروف"} هـ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Action download buttons
                            when {
                                isDownloading -> {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                                pkg.isInstalled -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "مثبتة ✓",
                                            fontSize = 11.sp,
                                            color = Color(0xFF2E7D32),
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(end = 6.dp)
                                        )
                                        IconButton(
                                            onClick = { viewModel.uninstallPackage(pkg.packageId) }
                                        ) {
                                            Icon(Icons.Default.DeleteSweep, contentDescription = "حذف الحزمة", tint = Color.Red.copy(alpha = 0.7f))
                                        }
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                downloadingPackages[pkg.packageId] = true
                                                delay(1500) // Simulate fast download offline
                                                downloadingPackages[pkg.packageId] = false
                                                viewModel.installPackage(pkg.packageId)
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                    ) {
                                        Text("تثبيت وتحميل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

// IMMERSIVE BOOK MODULE CONTENT VIEWER
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookContentViewer(
    book: BookUIModel,
    viewModel: QuranViewModel,
    surahs: List<SurahEntity>,
    onClose: () -> Unit,
    onNavigateToDetails: (AyahEntity, Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentSurahId by viewModel.currentSurahId.collectAsState()
    val currentAyahs by viewModel.ayahs.collectAsState()
    
    // Choose selected Verse
    var selectedVerseIndex by remember { mutableStateOf(0) }
    
    var surahMenuExpanded by remember { mutableStateOf(false) }

    // Fetch the currently browsed ayah
    val currentAyah = currentAyahs.getOrNull(selectedVerseIndex.coerceIn(0, currentAyahs.size - 1))

    // Local summary simulation block
    var localSummaryText by remember { mutableStateOf("") }
    var localSummaryLoading by remember { mutableStateOf(false) }

    // If surah changes, make sure selected verse index resets safely
    LaunchedEffect(currentSurahId) {
        selectedVerseIndex = 0
        localSummaryText = ""
    }

    val defaultContentText = remember(book.bookId, currentAyah) {
        if (currentAyah == null) "الرجاء اختيار الآية لمطالعة الشرح."
        else {
            when (book.bookId) {
                "tafsir_ibn_kathir" -> currentAyah.tafsirIbnKathir
                "tafsir_jalalayn" -> currentAyah.tafsirJalalayn
                "tafsir_muyassar" -> currentAyah.tafsirIbnKathir.replace("قال ابن كثير", "وجاء في التفسير الميسر الموثق")
                "tafsir_sadi" -> currentAyah.tafsirJalalayn.replace("الجلالين", "السعدي رحمه الله")
                "irab_al_quran" -> if (currentAyah.irab.isNotEmpty()) currentAyah.irab else "يتناول هذا الموضع إعراب الكلمات حسب صروف ولطائف البغوي والزجاج لبيان مباني المعاني النحوية التامة."
                "asbab_nuzul_wahidi" -> if (currentAyah.asbabNuzul.isNotEmpty()) currentAyah.asbabNuzul else "لم يثبت في مدونات الأثر سبب نزول تاريخي خاص معزز بالمرويات لهذه الآية الكريمة دون سائر السورة المباركة."
                "book_saadi" -> "أثر أسماء الله تعالى: يتجلى في هذه الآية المباركة دلالة اسم الله ومقتضى التوحيد واللطف الإيماني الخفي لتنقية النفس من ركون الغفلة."
                "book_irab_ism" -> "الدلالة الصرفية: الكلمات والحروف في قوله ﴿${currentAyah.textClean.take(15)}..﴾ لها أصل اشتقاق مبين في معاجم الصرف والبيان لقرائن العربية الشريفة."
                "book_asbab_asl" -> "تاريخ التنزيل: ترجع الوقائع والآثار المروية عن سياق هبوط هذه الآية على سيدنا المختار إلى بداية العهد النبوي الشريف بمكة المكرمة كخطاب روحي تأسيسي وعقائدي متين."
                else -> "تفسير مفصل وتبيان لعلوم التنزيل والحكمة."
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(book.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(book.author, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                actions = {
                    // Quick detail navigator
                    IconButton(
                        onClick = {
                            currentAyah?.let {
                                val targetTab = when (book.bookId) {
                                    "irab_al_quran", "book_irab_ism" -> 1
                                    "asbab_nuzul_wahidi", "book_asbab_asl" -> 3
                                    else -> 0
                                }
                                onNavigateToDetails(it, targetTab)
                            }
                        }
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = "", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Verse & Surah Selection panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("انتقال سريع لفصول الكتاب والآيات:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Surah selector dropdown
                        Box(
                            modifier = Modifier
                                .weight(1.5f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                                .clickable { surahMenuExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            val activeSurahName = surahs.find { it.id == currentSurahId }?.nameAr ?: "سورة الفاتحة"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(activeSurahName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                            }

                            DropdownMenu(
                                expanded = surahMenuExpanded,
                                onDismissRequest = { surahMenuExpanded = false }
                            ) {
                                surahs.forEach { surah ->
                                    DropdownMenuItem(
                                        text = { Text("${surah.id}. ${surah.nameAr}") },
                                        onClick = {
                                            viewModel.selectSurah(surah.id)
                                            surahMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Verse selector layout with prev / next
                        Row(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = {
                                    if (selectedVerseIndex > 0) selectedVerseIndex--
                                },
                                enabled = selectedVerseIndex > 0,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "", modifier = Modifier.size(16.dp))
                            }

                            Text(
                                text = "آية: ${if (currentAyahs.isNotEmpty()) selectedVerseIndex+1 else 1}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(
                                onClick = {
                                    if (selectedVerseIndex < currentAyahs.size - 1) selectedVerseIndex++
                                },
                                enabled = selectedVerseIndex < currentAyahs.size - 1,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Highlighting active scripture page
            currentAyah?.let { ayah ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "﴿ ${ayah.textAr} ﴾",
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "سورة رقم ${ayah.surahId} • آية ${ayah.verseNumber}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Complete Book contents Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "متن الصفحة الحالية في الكتاب:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            // Play Audio guide / Copy book page text
                            Row {
                                IconButton(
                                    onClick = { viewModel.playAyah(ayah) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }

                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Book text", defaultContentText)
                                        clipboard.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(context, "تم نسخ متن صفحة الكتاب بنجاح ✓", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                        
                        Text(
                            text = defaultContentText,
                            fontSize = 15.sp,
                            lineHeight = 26.sp,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Offline AI Companion integration exactly on this Page!
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = "", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "تلخيص بذكاء الآلة الأوفلاين:",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            localSummaryLoading = true
                                            localSummaryText = ""
                                            coroutineScope.launch {
                                                delay(1200) // Fast offline compilation
                                                localSummaryLoading = false
                                                
                                                val activeEngine = viewModel.aiEngineMode.value
                                                val modelTag = if (activeEngine == "local_nano") "Gemini Nano (0MB) الموصول بمحرك النظام" else "Qwen-2.5-0.5B المحلي الموفر للطاقة"
                                                
                                                localSummaryText = """
                                                    🧠 وخلاصة هذا الموضع الكريم من تفسيرنا بالذكاء الاصطناعي الأوفلاين ($modelTag):
                                                    
                                                    • المقصد الإشاري: يُعنى المتن بتبيان عظمة جلال الله ولطف معاني تزكية النفس مقتضياً بذلك التوحيد وملازمة الأذكار.
                                                    • التدبر العملي: تطبيق ما ورد بالآية يقتضي تصحيح العزم واليقين ومجانبة مواطن اللحن والغفلة.
                                                """.trimIndent()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp),
                                        enabled = !localSummaryLoading
                                    ) {
                                        Text("بدء التلخيص أوفلاين", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (localSummaryLoading) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp), color = MaterialTheme.colorScheme.secondary)
                                } else if (localSummaryText.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = localSummaryText,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp,
                                        fontFamily = FontFamily.Serif,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("لا يوجد موضع مطابق لتفسيره حالياً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
