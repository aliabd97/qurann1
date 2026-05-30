package com.example

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AyahEntity
import com.example.ui.QuranViewModel
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup highly visible uncaught exception logging to debug any background/launch crashes
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("Quran_CRASH", "FATAL CRASH on thread ${thread.name}!", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        enableEdgeToEdge()
        setContent {
            val viewModel: QuranViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            
            // Determine active theme mode dynamically of Settings Preference
            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                // Root Application state is modeled clearly below
                var activeTab by remember { mutableStateOf("home") } // "home", "quran", "packages", "ai", "more"
                var currentScreen by remember { mutableStateOf("main") } // "main", "details"
                
                var showSurahIndex by remember { mutableStateOf(false) }
                var selectedAyahForDetails by remember { mutableStateOf<AyahEntity?>(null) }
                var initialDetailsTab by remember { mutableStateOf(0) }
                
                val readingMode by viewModel.readingMode.collectAsState()
                val surahs by viewModel.surahs.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()

                // Global popup alert summarizer states
                val popupSummaryContent by viewModel.popupSummaryContent.collectAsState()
                val isPopupSummaryLoading by viewModel.isPopupSummaryLoading.collectAsState()
                val popupSummaryResult by viewModel.popupSummaryResult.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        snackbarHostState.showSnackbar(
                            message = it,
                            withDismissAction = true,
                            duration = SnackbarDuration.Long
                        )
                    }
                }

                Scaffold(
                    bottomBar = {
                        // Unify primary navigation clearly at the bottom of standard screens
                        if (currentScreen == "main" && !showSurahIndex) {
                            NavigationBar(
                                containerColor = if (darkTheme) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                tonalElevation = 8.dp
                            ) {
                                val activeColor = if (darkTheme) MaterialTheme.colorScheme.primary else ColorLightActive()
                                val inactiveColor = if (darkTheme) MaterialTheme.colorScheme.onSurfaceVariant else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)

                                NavigationBarItem(
                                    selected = (activeTab == "home"),
                                    onClick = { activeTab = "home" },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                                    label = { Text("الرئيسية", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeColor,
                                        selectedTextColor = activeColor,
                                        unselectedIconColor = inactiveColor,
                                        unselectedTextColor = inactiveColor,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (darkTheme) 0.4f else 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = (activeTab == "quran"),
                                    onClick = { activeTab = "quran" },
                                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "القرآن") },
                                    label = { Text("القرآن العظيم", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeColor,
                                        selectedTextColor = activeColor,
                                        unselectedIconColor = inactiveColor,
                                        unselectedTextColor = inactiveColor,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (darkTheme) 0.4f else 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = (activeTab == "packages"),
                                    onClick = { activeTab = "packages" },
                                    icon = { Icon(Icons.Default.CloudDownload, contentDescription = "المكتبة والحزم") },
                                    label = { Text("المكتبة والحزم", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeColor,
                                        selectedTextColor = activeColor,
                                        unselectedIconColor = inactiveColor,
                                        unselectedTextColor = inactiveColor,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (darkTheme) 0.4f else 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = (activeTab == "ai"),
                                    onClick = { activeTab = "ai" },
                                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "الذكاء الاصطناعي") },
                                    label = { Text("الذكاء الاصطناعي", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeColor,
                                        selectedTextColor = activeColor,
                                        unselectedIconColor = inactiveColor,
                                        unselectedTextColor = inactiveColor,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (darkTheme) 0.4f else 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = (activeTab == "more"),
                                    onClick = { activeTab = "more" },
                                    icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "المزيد") },
                                    label = { Text("المزيد", fontSize = 10.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeColor,
                                        selectedTextColor = activeColor,
                                        unselectedIconColor = inactiveColor,
                                        unselectedTextColor = inactiveColor,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (darkTheme) 0.4f else 0.2f)
                                    )
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        when {
                            showSurahIndex -> {
                                SurahIndexScreen(
                                    surahs = surahs,
                                    onSurahSelected = { surahId ->
                                        viewModel.selectSurah(surahId)
                                        showSurahIndex = false
                                        activeTab = "quran"
                                    },
                                    onDismiss = { showSurahIndex = false }
                                )
                            }
                            currentScreen == "details" && selectedAyahForDetails != null -> {
                                AyahDetailsScreen(
                                    ayah = selectedAyahForDetails!!,
                                    viewModel = viewModel,
                                    onBack = { currentScreen = "main" },
                                    initialTab = initialDetailsTab
                                )
                            }
                            else -> {
                                when (activeTab) {
                                    "home" -> {
                                        HomeScreen(
                                            viewModel = viewModel,
                                            onNavigateToQuran = { mode ->
                                                viewModel.setReadingMode(mode)
                                                activeTab = "quran"
                                            },
                                            onNavigateToPackages = { activeTab = "packages" },
                                            onOpenSurahIndex = { showSurahIndex = true },
                                            onNavigateToDetails = { ayah, tabIndex ->
                                                selectedAyahForDetails = ayah
                                                initialDetailsTab = tabIndex
                                                currentScreen = "details"
                                            }
                                        )
                                    }
                                    "packages" -> {
                                        LibraryAndPackagesScreen(
                                            viewModel = viewModel,
                                            onBack = { activeTab = "home" },
                                            onNavigateToDetails = { ayah, tabIndex ->
                                                selectedAyahForDetails = ayah
                                                initialDetailsTab = tabIndex
                                                currentScreen = "details"
                                            }
                                        )
                                    }
                                    "quran" -> {
                                        if (readingMode == "study") {
                                            StudyReaderScreen(
                                                viewModel = viewModel,
                                                onNavigateToPackages = { activeTab = "packages" },
                                                onNavigateToDetails = { ayah, tabIndex ->
                                                    selectedAyahForDetails = ayah
                                                    initialDetailsTab = tabIndex
                                                    currentScreen = "details"
                                                },
                                                onOpenSurahIndex = { showSurahIndex = true }
                                            )
                                        } else {
                                            MushafReaderScreen(
                                                viewModel = viewModel,
                                                onNavigateToPackages = { activeTab = "packages" },
                                                onNavigateToDetails = { ayah, tabIndex ->
                                                    selectedAyahForDetails = ayah
                                                    initialDetailsTab = tabIndex
                                                    currentScreen = "details"
                                                },
                                                onOpenSurahIndex = { showSurahIndex = true }
                                            )
                                        }
                                    }
                                    "ai" -> {
                                        AiStudioScreen(
                                            viewModel = viewModel
                                        )
                                    }
                                    "more" -> {
                                        MoreScreen(
                                            viewModel = viewModel
                                        )
                                    }
                                }
                            }
                        }

                        // ==========================================
                        // MAJESTIC UNIFIED DIALOG POP-UP AI SUMMARIZER
                        // ==========================================
                        popupSummaryContent?.let { originalText ->
                            AlertDialog(
                                onDismissRequest = { viewModel.dismissPopupSummary() },
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = "",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "تلخيص تبيان بالذكاء الاصطناعي",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontFamily = FontFamily.Serif
                                        )
                                    }
                                },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "النص الأصلي المُقترح:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .padding(10.dp)
                                        ) {
                                            Text(
                                                text = originalText,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                lineHeight = 18.sp,
                                                fontFamily = FontFamily.Serif
                                            )
                                        }
                                        
                                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        
                                        if (isPopupSummaryLoading) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                                horizontalArrangement = Arrangement.Center,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularProgressIndicator(modifier = Modifier.size(22.dp))
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text("جاري استخلاص المعاني بالذكاء الاصطناعي...", fontSize = 12.sp)
                                            }
                                        } else {
                                            popupSummaryResult?.let { result ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "خلاصة الدلالة والتدبر المقاصدي:",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    IconButton(
                                                        onClick = {
                                                            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                            val clip = android.content.ClipData.newPlainText("Popup Summary", result)
                                                            clipboard.setPrimaryClip(clip)
                                                            android.widget.Toast.makeText(this@MainActivity, "تم نسخ الخلاصة بنجاح! ✅", android.widget.Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الخلاصة", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                                Text(
                                                    text = result,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 22.sp,
                                                    fontFamily = FontFamily.Serif
                                                )
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { viewModel.dismissPopupSummary() }) {
                                        Text("إغلاق", fontWeight = FontWeight.Bold)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                        }

                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ColorLightActive(): androidx.compose.ui.graphics.Color {
        // A clean polished gold or warm ivory active label on dark-primary background in light mode
        return androidx.compose.ui.graphics.Color(0xFFFFF6D6)
    }
}
