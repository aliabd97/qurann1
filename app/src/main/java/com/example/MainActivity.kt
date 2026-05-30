package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AyahEntity
import com.example.ui.QuranViewModel
import com.example.ui.components.AyahDetailsScreen
import com.example.ui.components.HomeScreen
import com.example.ui.components.MushafReaderScreen
import com.example.ui.components.PackageManagerScreen
import com.example.ui.components.StudyReaderScreen
import com.example.ui.components.SurahIndexScreen
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
            MyApplicationTheme {
                val viewModel: QuranViewModel = viewModel()
                
                // Root Application state is modeled clearly below
                var activeTab by remember { mutableStateOf("home") } // "home", "quran", "packages"
                var currentScreen by remember { mutableStateOf("main") } // "main", "details"
                
                var showSurahIndex by remember { mutableStateOf(false) }
                var selectedAyahForDetails by remember { mutableStateOf<AyahEntity?>(null) }
                var initialDetailsTab by remember { mutableStateOf(0) }
                
                val readingMode by viewModel.readingMode.collectAsState()
                val surahs by viewModel.surahs.collectAsState()
                val errorMessage by viewModel.errorMessage.collectAsState()

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
                                containerColor = if (androidx.compose.foundation.isSystemInDarkTheme()) {
                                    MaterialTheme.colorScheme.surface
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                tonalElevation = 8.dp
                            ) {
                                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                                val activeColor = if (isDark) MaterialTheme.colorScheme.primary else ColorLightActive()
                                val inactiveColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f)

                                NavigationBarItem(
                                    selected = (activeTab == "home"),
                                    onClick = { activeTab = "home" },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "الرئيسية") },
                                    label = { Text("الرئيسية") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeColor,
                                        selectedTextColor = activeColor,
                                        unselectedIconColor = inactiveColor,
                                        unselectedTextColor = inactiveColor,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.4f else 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = (activeTab == "quran"),
                                    onClick = { activeTab = "quran" },
                                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "القرآن") },
                                    label = { Text("القرآن الكافي") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeColor,
                                        selectedTextColor = activeColor,
                                        unselectedIconColor = inactiveColor,
                                        unselectedTextColor = inactiveColor,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.4f else 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    selected = (activeTab == "packages"),
                                    onClick = { activeTab = "packages" },
                                    icon = { Icon(Icons.Default.CloudDownload, contentDescription = "المكتبة والحزم") },
                                    label = { Text("المكتبة والحزم") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = activeColor,
                                        selectedTextColor = activeColor,
                                        unselectedIconColor = inactiveColor,
                                        unselectedTextColor = inactiveColor,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDark) 0.4f else 0.2f)
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
                                            onOpenSurahIndex = { showSurahIndex = true }
                                        )
                                    }
                                    "packages" -> {
                                        PackageManagerScreen(
                                            viewModel = viewModel,
                                            onBack = { activeTab = "home" }
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
                                }
                            }
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
