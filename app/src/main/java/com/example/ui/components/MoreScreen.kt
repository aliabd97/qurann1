package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
fun MoreScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val currentTheme by viewModel.themeMode.collectAsState()
    val currentFontSize by viewModel.fontSizeMultiplier.collectAsState()

    val showTafsir by viewModel.showTafsir.collectAsState()
    val showIrab by viewModel.showIrab.collectAsState()
    val showWords by viewModel.showWords.collectAsState()
    val showSubjects by viewModel.showSubjects.collectAsState()
    val showAsbabNuzul by viewModel.showAsbabNuzul.collectAsState()

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val barColor = if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
    val contentColor = if (isDark) MaterialTheme.colorScheme.primary else Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المزيد وإعدادات التطبيق", color = contentColor, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section 1: Appearance & Theme
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مظهر ونمط التطبيق والدارك لايت:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    // Theme selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val themes = listOf(
                            Triple("system", "النظام وتلقائي", Icons.Default.SettingsSuggest),
                            Triple("light", "نمط مضيء", Icons.Default.LightMode),
                            Triple("dark", "نمط مظلم", Icons.Default.DarkMode)
                        )

                        themes.forEach { (mode, label, icon) ->
                            val isSelected = (currentTheme == mode)
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setThemeMode(mode) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(icon, contentDescription = "", tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Text Sizing Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FormatSize, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حجم خط القراءة القرآني والتفسير:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("معاينة الحجم الحالي:", style = MaterialTheme.typography.bodySmall)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "عجباً لأمر المؤمن (نسبة: %.1fx)".format(currentFontSize), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Preset Buttons to change font multiplier (e.g. 0.8x, 1.0x, 1.2x, 1.4x, 1.6x, 1.8x)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val presets = listOf(0.81f, 1.0f, 1.25f, 1.5f, 1.76f)
                        presets.forEach { scale ->
                            val isSelected = (kotlin.math.abs(currentFontSize - scale) < 0.05f)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { viewModel.setFontSizeMultiplier(scale) }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = when (scale) {
                                        0.81f -> "صغير"
                                        1.0f -> "طبيعي"
                                        1.25f -> "وسط"
                                        1.5f -> "كبير"
                                        else -> "ضخم"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Essential study Display Preferences (الإعدادات الأساسية)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Visibility, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الإعدادات الأساسية وحالات العرض دائمًا:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    SettingsToggleRow(
                        label = "إظهار كتب الهوامش والتفاسير تلقائياً",
                        checked = showTafsir,
                        onCheckedChange = { viewModel.toggleFilter("tafsir") }
                    )
                    SettingsToggleRow(
                        label = "إظهار إعراب كل كلمة مباشرة",
                        checked = showIrab,
                        onCheckedChange = { viewModel.toggleFilter("irab") }
                    )
                    SettingsToggleRow(
                        label = "تقسيم القراءة كلمة بكلمة تفاعلياً",
                        checked = showWords,
                        onCheckedChange = { viewModel.toggleFilter("words") }
                    )
                    SettingsToggleRow(
                        label = "إظهار المواضيع والروابط البيانية للآية",
                        checked = showSubjects,
                        onCheckedChange = { viewModel.toggleFilter("subjects") }
                    )
                    SettingsToggleRow(
                        label = "إظهار أسباب النزول المأثورة للآية",
                        checked = showAsbabNuzul,
                        onCheckedChange = { viewModel.toggleFilter("asbab") }
                    )
                }
            }

            // Section 4: About Tebyan App Credits
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CompassCalibration, contentDescription = "", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                    Text("تطبيق تَبْيَان الكافي لعلوم الآيات", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Serif)
                    Text("الإصدار الثاني المطور 2.4 (عام 1447 هـ)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "يهدف تطبيق تبيان لتوطين دراسة علوم القرآن والتفسير المتقدمة المدمجة بالذكاء الاصطناعي الهجين بطريقة علمية مسندة ومنسقة.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
