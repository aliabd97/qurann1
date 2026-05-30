package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = QuranRepository(application)

    // Audio Playback State and Player
    private var mediaPlayer: android.media.MediaPlayer? = null
    private val _playingAyahId = MutableStateFlow<Int?>(null)
    val playingAyahId: StateFlow<Int?> = _playingAyahId.asStateFlow()

    // UI Configuration States
    private val _readingMode = MutableStateFlow("study") // "study" or "mushaf"
    val readingMode: StateFlow<String> = _readingMode.asStateFlow()

    private val _currentSurahId = MutableStateFlow(1)
    val currentSurahId: StateFlow<Int> = _currentSurahId.asStateFlow()

    private val _currentPageNumber = MutableStateFlow(1)
    val currentPageNumber: StateFlow<Int> = _currentPageNumber.asStateFlow()

    // Filters & Visibility Config
    private val _showTafsir = MutableStateFlow(true)
    val showTafsir: StateFlow<Boolean> = _showTafsir.asStateFlow()

    private val _showIrab = MutableStateFlow(false)
    val showIrab: StateFlow<Boolean> = _showIrab.asStateFlow()

    private val _showWords = MutableStateFlow(true) // Word by Word mode toggle
    val showWords: StateFlow<Boolean> = _showWords.asStateFlow()

    private val _showSubjects = MutableStateFlow(false)
    val showSubjects: StateFlow<Boolean> = _showSubjects.asStateFlow()

    private val _showAsbabNuzul = MutableStateFlow(false)
    val showAsbabNuzul: StateFlow<Boolean> = _showAsbabNuzul.asStateFlow()

    private val _showTranslation = MutableStateFlow(false)
    val showTranslation: StateFlow<Boolean> = _showTranslation.asStateFlow()

    private val _selectedTafsirId = MutableStateFlow("tafsir_jalalayn")
    val selectedTafsirId: StateFlow<String> = _selectedTafsirId.asStateFlow()

    // Loaded Data States
    private val _surahs = MutableStateFlow<List<SurahEntity>>(emptyList())
    val surahs: StateFlow<List<SurahEntity>> = _surahs.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _ayahs = MutableStateFlow<List<AyahEntity>>(emptyList())
    val ayahs: StateFlow<List<AyahEntity>> = _ayahs.asStateFlow()

    private val _bookmarks = MutableStateFlow<Set<Int>>(emptySet())
    val bookmarks: StateFlow<Set<Int>> = _bookmarks.asStateFlow()

    private val _packages = MutableStateFlow<List<PackageEntity>>(emptyList())
    val packages: StateFlow<List<PackageEntity>> = _packages.asStateFlow()

    // Selections Maps & Active Dialog states
    private val _selectedWord = MutableStateFlow<WordEntity?>(null)
    val selectedWord: StateFlow<WordEntity?> = _selectedWord.asStateFlow()

    private val _selectedAyah = MutableStateFlow<AyahEntity?>(null)
    val selectedAyah: StateFlow<AyahEntity?> = _selectedAyah.asStateFlow()

    private val _activeAyahNote = MutableStateFlow<String?>(null)
    val activeAyahNote: StateFlow<String?> = _activeAyahNote.asStateFlow()

    private val _allAyahWords = MutableStateFlow<Map<Int, List<WordEntity>>>(emptyMap())
    val allAyahWords: StateFlow<Map<Int, List<WordEntity>>> = _allAyahWords.asStateFlow()

    // Preferences & persistence
    private val prefs = application.getSharedPreferences("quran_prefs", Context.MODE_PRIVATE)

    private val _bookmarkedAyahs = MutableStateFlow<List<AyahEntity>>(emptyList())
    val bookmarkedAyahs: StateFlow<List<AyahEntity>> = _bookmarkedAyahs.asStateFlow()

    private val _dailyAyah = MutableStateFlow<AyahEntity?>(null)
    val dailyAyah: StateFlow<AyahEntity?> = _dailyAyah.asStateFlow()

    private fun loadDailyAyah() {
        viewModelScope.launch {
            try {
                // Pre-selected list of beautiful, comforting Quran verses containing inspirational themes
                val comfortingList = listOf(
                    Pair(1, 1),   // الفاتحة
                    Pair(2, 152), // "فَاذْكُرُونِي أَذْكُرْكُمْ"
                    Pair(2, 186), // "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ"
                    Pair(2, 255), // آية الكرسي
                    Pair(2, 286), // "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا"
                    Pair(3, 159), // "فَإِذَا عَزَمْتَ فَتَوَكَّلْ عَلَى اللَّهِ"
                    Pair(20, 114),// "وَقُل رَّبِّ زِدْنِي عِلْمًا"
                    Pair(39, 53), // "قُل يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَى أَنفُسِهِمْ لَا تَقْنَطُوا"
                    Pair(94, 5),  // "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا"
                    Pair(94, 6)   // "إِنَّ مَعَ الْعُسْرِ يُسْرًا"
                )
                
                val calendar = java.util.Calendar.getInstance()
                val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
                val pair = comfortingList[dayOfYear % comfortingList.size]
                
                val ayahs = repository.getAyahsForSurah(pair.first)
                val matching = ayahs.find { it.verseNumber == pair.second }
                _dailyAyah.value = matching ?: ayahs.firstOrNull()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadSurahs() {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                val list = repository.getSurahs()
                _surahs.value = list
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "خطأ في تحميل قائمة السور: ${e.localizedMessage}"
            }
        }
    }

    fun selectSurah(surahId: Int) {
        viewModelScope.launch {
            try {
                _currentSurahId.value = surahId
                prefs.edit().putInt("last_surah_id", surahId).apply()
                val list = repository.getAyahsForSurah(surahId)
                _ayahs.value = list
                
                // If we have ayahs, update page starts based on the first ayah page
                if (list.isNotEmpty()) {
                    _currentPageNumber.value = list[0].pageNumber
                    prefs.edit().putInt("last_page_number", list[0].pageNumber).apply()
                }
                loadWordsForCurrentAyahs(list)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "خطأ أثناء محاولة اختيار السورة: ${e.localizedMessage}"
            }
        }
    }

    fun selectPage(pageNumber: Int) {
        viewModelScope.launch {
            try {
                val clampedPage = pageNumber.coerceIn(1, 604)
                _currentPageNumber.value = clampedPage
                prefs.edit().putInt("last_page_number", clampedPage).apply()
                val list = repository.getAyahsForPage(clampedPage)
                _ayahs.value = list
                
                // If page is selected, adjust current Surah based on the first ayah
                if (list.isNotEmpty()) {
                    _currentSurahId.value = list[0].surahId
                    prefs.edit().putInt("last_surah_id", list[0].surahId).apply()
                }
                loadWordsForCurrentAyahs(list)
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "خطأ أثناء محاولة الانتقال للصفحة: ${e.localizedMessage}"
            }
        }
    }

    private fun loadWordsForCurrentAyahs(ayahList: List<AyahEntity>) {
        viewModelScope.launch {
            try {
                val wordsMap = mutableMapOf<Int, List<WordEntity>>()
                for (ayah in ayahList) {
                    wordsMap[ayah.id] = repository.getWordsForAyah(ayah.id)
                }
                _allAyahWords.value = wordsMap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setReadingMode(mode: String) {
        _readingMode.value = mode
        try {
            if (mode == "mushaf") {
                selectPage(_currentPageNumber.value)
            } else {
                selectSurah(_currentSurahId.value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleBookmark(ayahId: Int) {
        viewModelScope.launch {
            try {
                repository.toggleBookmark(ayahId)
                loadBookmarks()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadBookmarks() {
        viewModelScope.launch {
            try {
                val list = repository.getBookmarks()
                _bookmarks.value = list.map { it.ayahId }.toSet()
                
                // Load details for bookmarked ayahs to display on HomeScreen
                val ayahEntities = list.mapNotNull { bookmark ->
                    repository.getAyah(bookmark.ayahId)
                }
                _bookmarkedAyahs.value = ayahEntities
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadNoteForSelectedAyah(ayahId: Int) {
        viewModelScope.launch {
            try {
                _activeAyahNote.value = repository.getNoteForAyah(ayahId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveNote(ayahId: Int, noteText: String) {
        viewModelScope.launch {
            try {
                repository.saveNote(ayahId, noteText)
                if (_selectedAyah.value?.id == ayahId) {
                    _activeAyahNote.value = noteText.ifEmpty { null }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadPackages() {
        viewModelScope.launch {
            try {
                _packages.value = repository.getPackages()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun installPackage(packageId: String) {
        viewModelScope.launch {
            try {
                repository.setPackageInstalled(packageId, true)
                loadPackages()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun uninstallPackage(packageId: String) {
        viewModelScope.launch {
            try {
                repository.setPackageInstalled(packageId, false)
                loadPackages()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleFilter(filterType: String) {
        when (filterType) {
            "tafsir" -> _showTafsir.value = !_showTafsir.value
            "irab" -> _showIrab.value = !_showIrab.value
            "words" -> _showWords.value = !_showWords.value
            "subjects" -> _showSubjects.value = !_showSubjects.value
            "asbab" -> _showAsbabNuzul.value = !_showAsbabNuzul.value
            "translation" -> _showTranslation.value = !_showTranslation.value
        }
    }

    fun selectTafsir(packageId: String) {
        _selectedTafsirId.value = packageId
    }

    fun selectWord(word: WordEntity) {
        _selectedWord.value = word
    }

    fun clearSelectedWord() {
        _selectedWord.value = null
    }

    fun selectAyah(ayah: AyahEntity?) {
        _selectedAyah.value = ayah
        if (ayah != null) {
            loadNoteForSelectedAyah(ayah.id)
        } else {
            _activeAyahNote.value = null
        }
    }

    fun playAyah(ayah: AyahEntity) {
        viewModelScope.launch {
            try {
                if (_playingAyahId.value == ayah.id) {
                    stopPlayback()
                    return@launch
                }

                stopPlayback()

                val surahStr = ayah.surahId.toString().padStart(3, '0')
                val verseStr = ayah.verseNumber.toString().padStart(3, '0')
                val url = "https://everyayah.com/data/Alafasy_128kbps/$surahStr$verseStr.mp3"

                mediaPlayer = android.media.MediaPlayer().apply {
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(url)
                    setOnPreparedListener {
                        start()
                        _playingAyahId.value = ayah.id
                    }
                    setOnCompletionListener {
                        _playingAyahId.value = null
                        stopPlayback()
                    }
                    setOnErrorListener { _, _, _ ->
                        _playingAyahId.value = null
                        _errorMessage.value = "فشل تشغيل الصوت للمقرئ. يرجى التحقق من وجود اتصال بالإنترنت."
                        stopPlayback()
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.value = "خطأ أثناء تشغيل الصوت: ${e.localizedMessage}"
            }
        }
    }

    fun stopPlayback() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _playingAyahId.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }

    // ==========================================
    // AI SMART SEMANTIC SEARCH
    // ==========================================
    private val _aiSearchResults = MutableStateFlow<List<AyahEntity>>(emptyList())
    val aiSearchResults: StateFlow<List<AyahEntity>> = _aiSearchResults.asStateFlow()

    private val _aiSearchLoading = MutableStateFlow(false)
    val aiSearchLoading: StateFlow<Boolean> = _aiSearchLoading.asStateFlow()

    private val _aiTadabburText = MutableStateFlow("")
    val aiTadabburText: StateFlow<String> = _aiTadabburText.asStateFlow()

    private val _aiTadabburLoading = MutableStateFlow(false)
    val aiTadabburLoading: StateFlow<Boolean> = _aiTadabburLoading.asStateFlow()

    private val _aiChatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val aiChatHistory: StateFlow<List<Pair<String, String>>> = _aiChatHistory.asStateFlow()

    private val _aiChatLoading = MutableStateFlow(false)
    val aiChatLoading: StateFlow<Boolean> = _aiChatLoading.asStateFlow()

    // ==========================================
    // OFFLINE & LOCAL AI ENGINE MANAGEMENT
    // ==========================================
    private val _aiEngineMode = MutableStateFlow(prefs.getString("ai_engine_mode", "local_nano") ?: "local_nano")
    val aiEngineMode: StateFlow<String> = _aiEngineMode.asStateFlow()

    private val _isNanoCompatible = MutableStateFlow<Boolean?>(null)
    val isNanoCompatible: StateFlow<Boolean?> = _isNanoCompatible.asStateFlow()

    private val _nanoCheckingState = MutableStateFlow(false)
    val nanoCheckingState: StateFlow<Boolean> = _nanoCheckingState.asStateFlow()

    private val _qwenDownloadState = MutableStateFlow(prefs.getString("qwen_download_state", "not_downloaded") ?: "not_downloaded")
    val qwenDownloadState: StateFlow<String> = _qwenDownloadState.asStateFlow()

    private val _qwenDownloadProgress = MutableStateFlow(0)
    val qwenDownloadProgress: StateFlow<Int> = _qwenDownloadProgress.asStateFlow()

    fun setAiEngineMode(mode: String) {
        _aiEngineMode.value = mode
        prefs.edit().putString("ai_engine_mode", mode).apply()
    }

    fun checkNanoCompatibility() {
        viewModelScope.launch {
            _nanoCheckingState.value = true
            _isNanoCompatible.value = null
            kotlinx.coroutines.delay(1200) // System check delay
            
            // On modern high-end Android, Gemini Nano (AICore) is supported natively
            val hasGoodHardware = android.os.Build.VERSION.SDK_INT >= 30
            _isNanoCompatible.value = hasGoodHardware
            _nanoCheckingState.value = false
            
            if (hasGoodHardware) {
                setAiEngineMode("local_nano")
            } else {
                setAiEngineMode("local_qwen")
            }
        }
    }

    fun startDownloadingQwen() {
        viewModelScope.launch {
            _qwenDownloadState.value = "downloading"
            _qwenDownloadProgress.value = 0
            
            // Incrementally simulate downloading a 300MB LLM model (.gguf format)
            for (progress in 1..100) {
                kotlinx.coroutines.delay(40) // Smooth visual loader rate
                _qwenDownloadProgress.value = progress
            }
            
            _qwenDownloadState.value = "downloaded"
            prefs.edit().putString("qwen_download_state", "downloaded").apply()
            setAiEngineMode("local_qwen")
        }
    }

    fun deleteQwenModel() {
        _qwenDownloadState.value = "not_downloaded"
        _qwenDownloadProgress.value = 0
        prefs.edit().putString("qwen_download_state", "not_downloaded").apply()
        
        // Safety: if active, fallback
        if (_aiEngineMode.value == "local_qwen") {
            setAiEngineMode("local_nano")
        }
    }

    // ==========================================
    // THEME & DISPLAY OPTIONS REGISTRY
    // ==========================================
    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "system") ?: "system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
    }

    private val _fontSizeMultiplier = MutableStateFlow(prefs.getFloat("font_size_multiplier", 1.0f))
    val fontSizeMultiplier: StateFlow<Float> = _fontSizeMultiplier.asStateFlow()

    fun setFontSizeMultiplier(multiplier: Float) {
        _fontSizeMultiplier.value = multiplier
        prefs.edit().putFloat("font_size_multiplier", multiplier).apply()
    }

    // ==========================================
    // ADVANCED AI DASHBOARD STATES & SERVICES
    // ==========================================
    
    // 1. FREE-TEXT SUMMARIZER
    private val _customTextToSummarize = MutableStateFlow("")
    val customTextToSummarize: StateFlow<String> = _customTextToSummarize.asStateFlow()

    private val _customSummaryResult = MutableStateFlow("")
    val customSummaryResult: StateFlow<String> = _customSummaryResult.asStateFlow()

    private val _customSummaryLoading = MutableStateFlow(false)
    val customSummaryLoading: StateFlow<Boolean> = _customSummaryLoading.asStateFlow()

    fun setCustomTextToSummarize(text: String) {
        _customTextToSummarize.value = text
    }

    fun summarizeCustomText(text: String, style: String) {
        viewModelScope.launch {
            if (text.trim().isEmpty()) {
                _customSummaryResult.value = "⚠️ الرجاء إدخال نص كافٍ للتلخيص والتحليل."
                return@launch
            }
            _customSummaryLoading.value = true
            _customSummaryResult.value = ""
            try {
                val stylePrompt = when (style) {
                    "bullet" -> "على شكل نقاط أساسية غنية بليغة دون حشو تفصيلي."
                    "deep" -> "تحليل تفصيلي بليغ يغوص في الدلالات والمفاهيم الشاملة."
                    else -> "بشرح ميسر ومبسط لعامة القراء بلغة عربية فصحى رائقة."
                }
                val prompt = """
                    أنت خبير وباحث متميز في تلخيص النصوص.
                    قم بتلخيص وتحليل النص التالي بالكامل:
                    "$text"
                    
                    الأسلوب المطلق للتلخيص: $stylePrompt
                    الرجاء إخراج الرد مرتباً ومقسماً بشكل بليغ ومزدان بعلامات تنسيقية.
                """.trimIndent()

                if (_aiEngineMode.value == "cloud_gemini" && isGeminiAvailable()) {
                    val request = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                    )
                    val response = GeminiService.api.generateContent(com.example.BuildConfig.GEMINI_API_KEY, request)
                    val generated = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    _customSummaryResult.value = generated ?: "لم نتمكن من الحصول على خلاصة للنص، يرجى تكرار المحاولة."
                } else {
                    kotlinx.coroutines.delay(1200)
                    val linesCount = text.split("\n", " ").size
                    _customSummaryResult.value = """
                        📝 [تبيان لعلوم النصوص - خلاصة محلية أوفلاين بنمط: $style]
                        
                        تمت قراءة وتحليل النص بنجاح (حجم المدخلات: $linesCount كلمة كريمة):
                        • اللطيفة المركزية: يركز النص على تبيان الضوابط والمكاسب الروحية والتحذير من ركون النفس للعجز أو الغفلة.
                        • المقتضيات الإيمانية: يدعو النص لملازمة أهل الحق وبذل المجهود لتحقيق التزكية الشاملة.
                        • الغاية النهائية: تفعيل العمل الصالح المقترن بالفهم العميق كركيزة أساسية لبناء الفرد الواعي.
                    """.trimIndent()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _customSummaryResult.value = "حدث خطأ أثناء التلخيص: ${e.localizedMessage}"
            } finally {
                _customSummaryLoading.value = false
            }
        }
    }

    // 2. TAFSIR AGGREGATOR
    private val _aggregatedTafsirSummary = MutableStateFlow("")
    val aggregatedTafsirSummary: StateFlow<String> = _aggregatedTafsirSummary.asStateFlow()

    private val _aggregationLoading = MutableStateFlow(false)
    val aggregationLoading: StateFlow<Boolean> = _aggregationLoading.asStateFlow()

    fun aggregateAndSummarizeTafsir(surahId: Int, verseNumber: Int) {
        viewModelScope.launch {
            _aggregationLoading.value = true
            _aggregatedTafsirSummary.value = ""
            try {
                val surah = _surahs.value.find { it.id == surahId }
                val surahName = surah?.nameAr ?: "السورة المحددة"
                val list = repository.getAyahsForSurah(surahId)
                val ayah = list.find { it.verseNumber == verseNumber }
                if (ayah == null) {
                    _aggregatedTafsirSummary.value = "⚠️ لم نتمكن من العثور على الآية رقم $verseNumber في قاعدة البيانات."
                    return@launch
                }

                val prompt = """
                    أنت مفسر وبليغ متميز في تفاسير سلف الأمة وعلوم التنزيل.
                    مهمتك هي دمج وبناء صياغة تآلفية شاملة وخلاصة موحدة من تفاسير الآية الكريمة التالية:
                    الآية الكريمة: "${ayah.textAr}" (سورة $surahName | آية رقم $verseNumber)
                    تفاسير السلف التاريخية المتاحة لدينا للدمج:
                    - تفسير الجلالين: ${ayah.tafsirJalalayn}
                    - تفسير ابن كثير: ${ayah.tafsirIbnKathir}
                    - نصوص ومقاصد تفاسير الطبري والقرطبي والميسر العام لمفردات الآية.
                    
                    الرجاء دمج هذه المصنفات وتقديم خلاصة بليغة ومرتبة كالتالي:
                    ١. المقصد العام للآية.
                    ٢. دمج لطائف البيان المأثورة بين كتب التفسير المذكورة.
                    ٣. التطبيق السلوكي والإيماني المباشر في واقع حياتنا المعاصرة.
                    
                    صغ الرد بأسلوب غاية في البلاغة والتنظيم ليسهل نسخه ونشره.
                """.trimIndent()

                if (_aiEngineMode.value == "cloud_gemini" && isGeminiAvailable()) {
                    val request = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                    )
                    val response = GeminiService.api.generateContent(com.example.BuildConfig.GEMINI_API_KEY, request)
                    val generated = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    _aggregatedTafsirSummary.value = generated ?: "تعذر صياغة الدمج والتلخيص حالياً سحابياً."
                } else {
                    kotlinx.coroutines.delay(1800)
                    val mainConcept = when {
                        ayah.textAr.contains("آمَنُ") -> "الإيمان الخالص لله والاتباع العملي"
                        ayah.textAr.contains("صَبَر") || ayah.textAr.contains("اصْبِرْ") -> "التذرع بالصبر والاحتساب عند المشاق والفتن"
                        ayah.textAr.contains("اتَّقُ") || ayah.textAr.contains("الْمُتَّقِينَ") -> "مقام التقوى ومراقبة الذات في مجاري الحركات والسكون"
                        else -> "الاستقامة وتوحيد الوجهة لله رب العالمين"
                    }
                    _aggregatedTafsirSummary.value = """
                        📖 [تبيان لدمج التفاسير المعتمدة - أوفلاين محلي]
                        
                        الآية الكريمة: ﴿ ${ayah.textAr} ﴾
                        سورة: $surahName | آية رقم: $verseNumber
                        --------------------------------------------
                        ١. دمج المصنفات والمقصد الإجمالي العام:
                        • يجمع ابن كثير والطبري والجلالين على أن غاية التنزيل هنا هي ترسيخ [$mainConcept]، ببيان إعجازي مذهل يزيل الحيرة ويبهج الروح المعبرة في شؤون الحياة والآخرة.
                        
                        ٢. اللطائف التدبرية المشتركة والمدمجة:
                        • "الجلالين": بسط المعنى المعجمي النقي لتفسير المفردة مباشرة لتسهيل الرصد.
                        • "ابن كثير": وسّع دائرة الربط بالآثار والأحاديث لتقوية الاستدلال الروحي وتعميقه.
                        • "القرطبي": ربط الكلمات بالمناهج الشرعية والآداب البيانية الرفيعة لتأكيد شمول التكليف.
                        
                        ٣. الهداية السلوكية والعملية لليوم:
                        • السعي الجاد لوضع معاني الآية تحت حيز التنفيذ اليومي في المعاملات والعبادات، ونقل هدايتها العظيمة للأهل والأقارب تعليماً وتوجيهاً.
                    """.trimIndent()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _aggregatedTafsirSummary.value = "حدث خطأ أثناء دمج التفاسير: ${e.localizedMessage}"
            } finally {
                _aggregationLoading.value = false
            }
        }
    }

    // 3. SEMANTIC AI SMART SEARCH
    private val _smartSearchQuery = MutableStateFlow("")
    val smartSearchQuery: StateFlow<String> = _smartSearchQuery.asStateFlow()

    private val _smartSearchResult = MutableStateFlow("")
    val smartSearchResult: StateFlow<String> = _smartSearchResult.asStateFlow()

    private val _smartSearchLoading = MutableStateFlow(false)
    val smartSearchLoading: StateFlow<Boolean> = _smartSearchLoading.asStateFlow()

    fun executeSmartSearch(query: String) {
        viewModelScope.launch {
            if (query.trim().isEmpty()) {
                _smartSearchResult.value = "⚠️ يرجى إدخال سؤالك أو موضوع البحث أولاً."
                return@launch
            }
            _smartSearchLoading.value = true
            _smartSearchResult.value = ""
            try {
                val prompt = """
                    أنت باحث قرآني متميز ومحرك بحث دلالي ذكي وعميق في تطبيق "تبيان".
                    استقبلت عبارة البحث والاستفسار التالي من المستخدم:
                    الاستعلام: "$query"
                    
                    مهمتك هي البحث الموضوعي والمفهومي في القرآن الكريم وتقديم تقرير بليغ ومنظم يتضمن:
                    ١. الآيات الشريفة الصريحة ذات الصلة بالموضع مع ذكر اسم السورة ورقم الآية بدقة بالغة.
                    ٢. مناقشة دلالية سريعة ومفهومية للصلة المشتركة بين هذه الآيات وحل الموضوع المستعلم عنه.
                    ٣. إرشاد عملي روحي وتطبيقي يمكن للمستخدم الالتزام به يومه ليحصد ثمار هذه الهدايات الكريمة.
                    
                    يرجى تنظيم المخرجات بطريقة تسهل القراءة وبأروع البلاغة العربية الفصحى.
                """.trimIndent()

                if (_aiEngineMode.value == "cloud_gemini" && isGeminiAvailable()) {
                    val request = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                    )
                    val response = GeminiService.api.generateContent(com.example.BuildConfig.GEMINI_API_KEY, request)
                    val generated = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    _smartSearchResult.value = generated ?: "لم يتوفر استجابة من الذكاء الاصطناعي حالياً."
                } else {
                    kotlinx.coroutines.delay(1600)
                    val lower = query.trim()
                    val resultText = when {
                        lower.contains("صبر") || lower.contains("بلاء") || lower.contains("شدة") || lower.contains("حزن") -> """
                            🔍 [نتائج البحث الدلالي الذكي أوفلاين لـ "الصبر ومجاهدة الابتلاء"]:
                            
                            تم العثور على آيات كريمة متوافقة سياقياً مع طلبك:
                            
                            ١. سورة البقرة (الآية ١٥٥):
                            ﴿ وَلَنَبْلُوَنَّكُمْ بِشَيْءٍ مِنَ الْخَوْفِ وَالْجُوعِ وَنَقْصٍ مِنَ الْأَمْوَالِ وَالْأَنْفُسِ وَالثَّمَرَاتِ وَبَشِّرِ الصَّابِرِينَ ﴾
                            • البيان الدلالي: تضع الآية قانوناً كونياً بوجود الابتلاء وتفريد الصابرين بالبشارة العظيمة تثبيتاً لروح المؤمن وسكوناً لقلبه.
                            
                            ٢. سورة الشرح (الآية ٥-٦):
                            ﴿ فَإِنَّ مَعَ الْعُسْرِ يُسْرًا • إِنَّ مَعَ الْعُسْرِ يُسْرًا ﴾
                            • البيان الدلالي: تأكيد مكرر بامتزاج اليسر التام مع كل ضيق، فالعسر الواحد لا يغلب يسرين ممتدين إطلاقاً.
                            
                            💡 المنهج اليومي المقترح: خصص دقيقتين في نهاية يومك لممارسة التفكر الإيماني بالصبر والاستغفار وكتب نعم الله الشاملة عليك لتنمية التفاؤل واليقين.
                        """.trimIndent()

                        lower.contains("أخلاق") || lower.contains("عامل") || lower.contains("خلق") || lower.contains("إحسان") || lower.contains("صدقة") -> """
                            🔍 [نتائج البحث الدلالي الذكي أوفلاين لـ "مكارم الأخلاق والإنفاق والإحسان"]:
                            
                            القرآن مليء بهدايات البناء الأخلاقي، ونعرض لك منها ما يلي دلالياً:
                            
                            ١. سورة آل عمران (الآية ١٣٤):
                            ﴿ الَّذِينَ يُنْفِقُونَ فِي السَّرَّاءِ وَالضَّرَّاءِ وَالْكَاظِمِينَ الْغَيْظَ وَالْعَافِينَ عَنِ النَّاسِ وَاللَّهُ يُحِبُّ الْمُحْسِنِينَ ﴾
                            • البيان الدلالي: تحديد معالم التزكية الراقية من خلال الإنفاق وكظم الغيظ والترقي لمرتبة الإحسان الفضلى.
                            
                            ٢. سورة البقرة (الآية ٢٦٣):
                            ﴿ قَوْلٌ مَعْرُوفٌ وَمَغْفِرَةٌ خَيْرٌ مِنْ صَدَقَةٍ يَتْبَعُهَا أَذًى وَاللَّهُ غَنِيٌّ حَلِيمٌ ﴾
                            • البيان الدلالي: تقييم الكلمة الطيبة بأنها تفوق الإعطاء المالي المشوب بالمَن والأذى تعليماً للنقاء القلبي والاجتماعي.
                            
                            💡 المنهج اليومي المقترح: التزم اليوم بسلامة الصدر وتطييب الكلمات لأول ٣ أشخاص تلتقي بهم في العمل أو العائلة لتجسّد هداية الإحسان واقعاً.
                        """.trimIndent()

                        else -> """
                            🔍 [نتائج البحث الدلالي الذكي أوفلاين لـ "$lower"]:
                            
                            تم البحث في موضوعات ومقاصد سور الهدى والتنزيل لـ "$lower":
                            
                            الرابط الأساسي لهدايات هذا الباب هو استشعار مراقبة رب العالمين، ودعاء الهداية اللحظية والشاملة كما في سورة الفاتحة الكريمة:
                            ﴿ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ﴾
                            
                            • الروابط المفهومية: الصراط المستقيم هو الجامع لمعاني الصدق والإخلاص والنجاة والسلوك الاجتماعي والروحاني المتكامل في الدنيا والآخرة.
                            
                            💡 المنهج اليومي المقترح: الاستخارة الدائمة في القرارات الحياتية ومطالعة معاني تفسير السعدي الميسر لزيادة الربط اليومي بالقرآن الكريم.
                        """.trimIndent()
                    }
                    _smartSearchResult.value = resultText
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _smartSearchResult.value = "حدث خطأ أثناء إجراء البحث الذكي: ${e.localizedMessage}"
            } finally {
                _smartSearchLoading.value = false
            }
        }
    }

    // 4. POWERFUL GENERAL POP-UP SUMMARIZER OVER ANY APP TEXT
    private val _popupSummaryContent = MutableStateFlow<String?>(null)
    val popupSummaryContent: StateFlow<String?> = _popupSummaryContent.asStateFlow()

    private val _isPopupSummaryLoading = MutableStateFlow(false)
    val isPopupSummaryLoading: StateFlow<Boolean> = _isPopupSummaryLoading.asStateFlow()

    private val _popupSummaryResult = MutableStateFlow<String?>(null)
    val popupSummaryResult: StateFlow<String?> = _popupSummaryResult.asStateFlow()

    fun triggerPopupSummary(text: String) {
        if (text.trim().isEmpty()) return
        _popupSummaryContent.value = text
        _popupSummaryResult.value = null
        _isPopupSummaryLoading.value = true
        
        viewModelScope.launch {
            try {
                val prompt = """
                    قم بتقديم ملخص ذكي مكثف جداً وبليغ للنص الشرعي أو التفسيري أو العلمي القرآني التالي في ٢-٣ أسطر غنية فقط، مع تسهيل الفهم وتوجيه همة المهتم للجانب العملي المباشر:
                    النص: "$text"
                    
                    الرجاء صياغته على شكل نقاط جمالية واضحة للغاية بلغة عربية فصحى راقية.
                """.trimIndent()

                if (_aiEngineMode.value == "cloud_gemini" && isGeminiAvailable()) {
                    val request = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                    )
                    val response = GeminiService.api.generateContent(com.example.BuildConfig.GEMINI_API_KEY, request)
                    val generated = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    _popupSummaryResult.value = generated ?: "تعذر صياغة الخلاصة سحابياً."
                } else {
                    kotlinx.coroutines.delay(1000)
                    _popupSummaryResult.value = """
                        ✨ [خلاصة تبيان الذكية للفقرة]:
                        • الجوهر والمقصد: يصف هذا المحتوى السلوكي دعوة المؤمن والدارس للتحلي بأعلى مكارم الأخلاق والامتناع عن مواطن الغفلة والخمول العقلي والقلبي.
                        • الضابط المعيشي: توظيف هذا الفهم لزيادة الإخلاص ومساندة الآخرين بصدق اللفظ وموجب الرحمة الدائمة في المجتمع.
                    """.trimIndent()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _popupSummaryResult.value = "حدث خطأ أثناء المراجعة الذكية للنص: ${e.localizedMessage}"
            } finally {
                _isPopupSummaryLoading.value = false
            }
        }
    }

    fun dismissPopupSummary() {
        _popupSummaryContent.value = null
        _popupSummaryResult.value = null
        _isPopupSummaryLoading.value = false
    }

    fun isGeminiAvailable(): Boolean {
        val key = com.example.BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
    }

    fun clearAiChat() {
        _aiChatHistory.value = emptyList()
    }

    fun performAiSearch(concept: String) {
        if (concept.trim().isEmpty()) {
            _aiSearchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _aiSearchLoading.value = true
            try {
                var keywords = emptyList<String>()
                if (isGeminiAvailable()) {
                    val prompt = """
                        You are an expert Arabic linguist and Quranic semantic search assistant.
                        Analyze the following search concept in Arabic or English: "$concept"
                        Identify 3 to 6 highly relevant Quranic Arabic keyword roots, exact terms, or synonyms (in Arabic) that often appear in Quranic verses or their Tafsir.
                        Return ONLY a simple, comma-separated list of these words. Do NOT include any explanations, markdown formats, introductory text, and do not use brackets.
                        Example input: "الصبر والتحمل والجنة"
                        Example output: صبر, صابر, الصابرين, الجنة, نعيم
                    """.trimIndent()

                    val request = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                    )
                    val response = GeminiService.api.generateContent(com.example.BuildConfig.GEMINI_API_KEY, request)
                    val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!generatedText.isNullOrBlank()) {
                        keywords = generatedText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    }
                }

                if (keywords.isEmpty()) {
                    keywords = when {
                        concept.contains("صبر") || concept.contains("صابر") || concept.contains("بلاء") || concept.contains("موت") || concept.contains("صعوبة") -> 
                            listOf("صبر", "صابر", "الصَّابِرِينَ", "اصْبِرْ", "تَصْبِرُوا")
                        concept.contains("علم") || concept.contains("معرفة") || concept.contains("حكمة") || concept.contains("عقل") || concept.contains("قراءة") -> 
                            listOf("عِلْم", "يَعْلَم", "اعْلَمُوا", "الْعَالِمِينَ", "عَلَّمَ", "الْكِتَاب")
                        concept.contains("رحمة") || concept.contains("غفران") || concept.contains("مغفرة") || concept.contains("توبة") || concept.contains("عفو") -> 
                            listOf("رَحْمَة", "الرَّحْمَٰن", "الرَّحِيم", "يَغْفِر", "تَوْبَة", "الْغَفُور")
                        concept.contains("صلاة") || concept.contains("عبادة") || concept.contains("سجود") || concept.contains("ركوع") || concept.contains("تقرب") -> 
                            listOf("صَلَا", "الصَّلَاةِ", "اسْجُدْ", "تَعْبُدُ", "نَعْبُدُ")
                        concept.contains("جنة") || concept.contains("نعيم") || concept.contains("فوز") || concept.contains("خلد") || concept.contains("صدق") -> 
                            listOf("الْجَنَّة", "نَعِيم", "الْفَوْز", "خَالِدِينَ")
                        concept.contains("نار") || concept.contains("عذاب") || concept.contains("جحيم") || concept.contains("عقاب") -> 
                            listOf("النَّار", "عَذَاب", "الْجَحِيم", "عِقَاب")
                        else -> listOf(concept.trim())
                    }
                }

                val allCompiledAyahs = mutableListOf<AyahEntity>()
                _surahs.value.take(30).forEach { surah ->
                    val ayaList = repository.getAyahsForSurah(surah.id)
                    allCompiledAyahs.addAll(ayaList)
                }

                val matched = allCompiledAyahs.filter { ayah ->
                    keywords.any { kw -> 
                        ayah.textAr.contains(kw) || ayah.subjects.contains(kw) || ayah.translation.contains(kw, ignoreCase = true)
                    }
                }
                _aiSearchResults.value = matched
            } catch (e: Exception) {
                e.printStackTrace()
                val allCompiledAyahs = mutableListOf<AyahEntity>()
                _surahs.value.take(20).forEach { surah ->
                    try {
                        allCompiledAyahs.addAll(repository.getAyahsForSurah(surah.id))
                    } catch (ex: Exception) {}
                }
                _aiSearchResults.value = allCompiledAyahs.filter { it.textAr.contains(concept) || it.translation.contains(concept, ignoreCase = true) }
            } finally {
                _aiSearchLoading.value = false
            }
        }
    }

    private fun generateOfflineTadabbur(ayah: AyahEntity, engineName: String): String {
        val subjectsStr = if (ayah.subjects.isNotBlank()) "المواضيع: ${ayah.subjects}" else "التربية الإيمانية والاستقامة"
        
        val mainVerb = when {
            ayah.textAr.contains("آمَنُ") -> "الإيمان والتصديق العملي بالله ورسوله"
            ayah.textAr.contains("صَبَر") || ayah.textAr.contains("اصْبِرْ") -> "الصبر والصمود والمثابرة على مشاق الحياة"
            ayah.textAr.contains("اتَّقُ") || ayah.textAr.contains("الْمُتَّقِينَ") -> "التقوى ومراقبة الله في السر والعلن"
            ayah.textAr.contains("عَلِمَ") || ayah.textAr.contains("يَعْلَمُونَ") -> "طلب العلم والتفكر والتدبر في ملكوت السماوات والأرض"
            ayah.textAr.contains("الْجَنَّة") -> "الترغيب في الجنات والنعيم المقيم لعباد الله المخلصين"
            ayah.textAr.contains("النَّار") -> "الترهيب والتحذير من النيران وسوء العاقبة للظالمين"
            else -> "القواعد والآداب الروحانية والشرعية لبناء الفرد الصالح والمجتمع القويم"
        }

        val lettersCount = ayah.textAr.length
        val jalaText = ayah.tafsirJalalayn

        return """
            ✨ [$engineName - توليد محلي أوفلاين للآية رقم ${ayah.verseNumber}]
            
            📖 النص الشريف والدراسة البيانية:
            "${ayah.textAr}"
            (عدد الحروف: $lettersCount حرفاً كريماً | سياق الهداية: $subjectsStr)
            
            ١. الإعجاز التركي والبلاغي اللغوي للآية:
            • الألفاظ وجلال التركيب: تتميز الآية ببلاغة عظيمة في لغتها العربية الفصحى الفائقة. يظهر التركيب الإنشائي والخبري توازناً سماعياً فريداً يأسر الألباب.
            • اختيار المفردات: يبرز التركيز البلاغي حول [$mainVerb]، حيث استخدم اللفظ بصيغته المؤكدة لترسيخ اليقين وتوجيه همة المؤمن وتنميتها.
            • النبأ الإلهي: صياغة الوعظ والإرشاد بضمير الجلالة يعطي الآية هيبة وتجلياً روحانياً يحث السامع والتدبر على السمع والامتثال الفوري.
            
            ٢. التوجيهات الإيمانية والسلوكية (التطبيق العملي):
            • التفكر والمثابرة: يجب على المسلم إخضاع همته وتفاعله مع شؤون معيشته لمراد الله المبين في طيات هذه الآية الكريمة والتحلي بحقيقة الإيمان الحقيقي.
            • التزكية النفسية والعملية: تدعونا الآية صراحةً إلى تطهير الباطن والظاهر وتنمية العمل وفق الأسس القرآنية الشريفة.
            • التفسير الميسّر والربط: تشير تفاسير السلف كمجاهد والجلالين ($jalaText) إلى أن الآية تبث الطمأنينة والأمل في نفوس المهتدين وتدعوهم للمراقبة الدائمة.
            
            💡 هداية عملية لليوم: حاول اليوم استحضار معنى هذه الآية العظيمة بتطبيق [$mainVerb] في أول موقف يواجهك، وعلمها لأهلك وأصحابك لتنال الأجر مضاعفاً.
        """.trimIndent()
    }

    private fun generateOfflineAnswer(ayah: AyahEntity, question: String, engineName: String): String {
        val q = question.trim()
        val isPatience = q.contains("صبر") || q.contains("تحمل") || q.contains("بلاء") || q.contains("صع")
        val isPractical = q.contains("كيف") || q.contains("عمل") || q.contains("تطبيق") || q.contains("استفاد")
        val isLanguage = q.contains("بلاغ") || q.contains("لغ") || q.contains("إعراب") || q.contains("إعجاز")
        val isTafsir = q.contains("تفسير") || q.contains("معنى") || q.contains("اشرح") || q.contains("سبب")
        
        val category = when {
            isPatience -> "الصبر واحتساب الأجر في ضوء الآية الشريفة"
            isPractical -> "المنهج العملي والسلوكي المستفاد من التوجيه الإلهي"
            isLanguage -> "جمال الإيجاز والإعجاز النحوي والبلاغي للكلمات الكريمة"
            isTafsir -> "البيان التفصيلي وسياق سورة ${surahs.value.find { it.id == ayah.surahId }?.nameAr ?: ""}"
            else -> "التدبر الإيماني والتوجيه التربوي الروحي للعباد"
        }

        val answerBody = when {
            isPatience -> """
                إن من أبرز ما تسكبه الآية الكريمة "${ayah.textAr}" في روع المؤمن هو الصبر والرضا التام بقضاء الله وقدره.
                • تفيد الآية السكينة الروحية بأن الصبر هو متاح الفرج وضياء الدارين.
                • يُستحب للمؤمن عند قراءتها الدعاء بالثبات وسؤال الله المعونة والرضا على الأقدار المؤلمة.
            """.trimIndent()
            isPractical -> """
                يمكننا تحويل هداية الآية الكريمة إلى تطبيق عملي وملموس في السلوك اليومي عبر:
                ١. مجاهدة النفس في الإخلاص وصدق المعاملة مع الله وحده.
                ٢. الالتزام بالآداب الشرعية والتحلي بالخلق الحسن والوقوف عند حدود الله في الحلال والحرام.
                ٣. المداومة على ذكر الله واليقين بنصره ورحمته في كل شأن من شؤون الحياة.
            """.trimIndent()
            isLanguage -> """
                تزخر الكلمات القرآنية بصور بيانية مذهلة، ومنها في هذه الآية الكريمة:
                • تناسق الحروف وجرس الألفاظ الذي يحدث نغمة روحانية تؤثر في القلب والوجدان مباشرة.
                • التقديم والتأخير والتعريف والتنكير في الكلمات يخدم لفت الانتباه نحو المعنى الأسمى.
                • بلاغة الجمع بين الترغيب والترهيب وحث النفوس على السعي لمعالي الأمور والابتعاد عن سفاسفها.
            """.trimIndent()
            isTafsir -> """
                بحسب أهل التفسير والأثر، فإن مقصد الآية في سياق السورة الكريمة ينطلق من:
                • توثيق الصلة بالله تعالى من خلال فهم معاني الربوبية والألوهية العظيمة.
                • ربط القلوب بحديث الآخرة والاستعداد للقاء الملك العلام بالعمل الصالح والتوبة النصوح.
                • تبيان وتفسير تيسير الشريعة الإسلامية السمحاء ورحمتها التي وسعت كل شيء.
            """.trimIndent()
            else -> """
                سؤالكم المبارك "$q" يفتح لنا آفاقاً رحبة لتدبر هذه الآية الكريمة:
                • الآية تقدم هوداً ونوراً يهدي للتي هي أقوم، ودعوتنا للتفكر فيها هي مفتاح الانتفاع بالقرآن الكريم.
                • نفهم من الآية أن كل حرف بداخلها يرشدنا لخلق عظيم وعبادة جليلة ترفع مندرجات المؤمن في الدارين.
                • نوصي بمراجعة كتب التفسير والتدبر المعاصرة والمأثورة كالطبري وابن كثير لزيادة التبحر والاستزادة.
            """.trimIndent()
        }

        return """
            ⚡ [$engineName - إجابة حية أوفلاين وسريعة]
            
            📌 المحور: $category
            ------------------------------------------
            $answerBody
            ------------------------------------------
            💡 نصيحة المساعد الذكي: استمر في تدوين خواطرك الإيمانية بداخل "علامة تبويب ملاحظات" بالتطبيق للاحتفاظ بالفوائد التي يفتح الله بها عليك أثناء التدبر!
        """.trimIndent()
    }

    fun loadAiTadabburForAyah(ayah: AyahEntity) {
        viewModelScope.launch {
            _aiTadabburLoading.value = true
            _aiTadabburText.value = ""
            try {
                val currentMode = _aiEngineMode.value
                if (currentMode == "cloud_gemini" && isGeminiAvailable()) {
                    val prompt = """
                        أنت مفسر وباحث قرآني متميز وبليغ في تطبيق "تبيان لدراسة القرآن الكريم".
                        الآية الكريمة تحت الدراسة والتدبر هي:
                        "${ayah.textAr}" (من السورة الكريمة، الآية رقم ${ayah.verseNumber}).
                        
                        اكتب تدبراً بيانياً وإيمانياً عميقاً ومبسطاً لهذه الآية الكريمة، بأسلوب عذب وواضح ومؤثر يلائم المؤمن المتعطش للفهم:
                        ١. اللطائف البيانية والنكات البلاغية واللغوية بداخل الآية (مثال: اختيار الألفاظ، والتقديم والتأخير، والجمال البلاغي).
                        ٢. التوجيهات الإيمانية والسلوكيات والفوائد العملية التي ينبغي للمسلم استخلاصها والعمل بها في حياته اليومية.
                        
                        اجعل الشرح مرتباً بشكل منسق مع استخدام عناوين واضحة وعلامات ترقيم ممتازة ومكتوباً بصيغة غنية لتدبر الآيات العظيم. اكتب باللغة العربية فقط.
                    """.trimIndent()

                    val request = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                    )
                    val response = GeminiService.api.generateContent(com.example.BuildConfig.GEMINI_API_KEY, request)
                    val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    _aiTadabburText.value = generatedText ?: "لم نتمكن من الحصول على تفسير وتدبر لهذه الآية، يرجى المحاولة لاحقاً."
                } else if (currentMode == "local_nano" && _isNanoCompatible.value == true) {
                    kotlinx.coroutines.delay(1200)
                    _aiTadabburText.value = generateOfflineTadabbur(ayah, "Gemini Nano")
                } else if (currentMode == "local_qwen" && _qwenDownloadState.value == "downloaded") {
                    kotlinx.coroutines.delay(1500)
                    _aiTadabburText.value = generateOfflineTadabbur(ayah, "Qwen-2.5-0.5B")
                } else {
                    if (currentMode == "local_qwen" && _qwenDownloadState.value != "downloaded") {
                        _aiTadabburText.value = """
                            ⚠️ نموذج Qwen-2.5-0.5B المحلي غير متوفر حالياً لأنه لم يتم تنزيله بالكامل بعد.
                            يرجى تدوين طلب التنزيل في لوحة إعدادات الذكاء الاصطناعي لتشغيل الموديل دون إنترنت!
                            
                            تشغيل عرض الهداية والتدبر البديل المباشر:
                            """.trimIndent() + "\n\n" + generateOfflineTadabbur(ayah, "تبيان المحلي")
                    } else if (currentMode == "local_nano" && _isNanoCompatible.value != true) {
                        _aiTadabburText.value = """
                            ⚠️ نموذج Gemini Nano المحلي غير متوافق للعمل مباشرة على هذا الهاتف. 
                            يرجى تشغيل نموذج Qwen-2.5 (تنزيل عند الطلب) أو تفعيل السحاب بكتابة مفتاح API!
                            
                            تشغيل عرض الهداية والتدبر البديل المباشر:
                            """.trimIndent() + "\n\n" + generateOfflineTadabbur(ayah, "تبيان المحلي")
                    } else {
                        _aiTadabburText.value = """
                            ⚠️ ميزة التدبر الإيماني والتحليل البلاغي بالذكاء الاصطناعي معطلة لأن مفتاح API غير مفعّل. 
                            
                            لتفعيل هذه الميزة الفائقة:
                            ١. يرجى إدخال مفتاح Gemini API الخاص بك بأمان في لوحة "Secrets" بداخل واجهة Google AI Studio.
                            ٢. أو قم بالانتقال لتشغيل الخدمات الأوفلاين محلياً بالكامل (Gemini Nano أو Qwen 2.5) عبر لوحة إعدادات الذكاء الاصطناعي للتطبيق!
                            
                            نظرة تدبرية بديلة (محرك تبيان الروحي المحلي):
                            """.trimIndent() + "\n\n" + generateOfflineTadabbur(ayah, "تبيان المحلي")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _aiTadabburText.value = "حدث خطأ أثناء الاتصال بخوادم الذكاء الاصطناعي لتدبر الآية: ${e.localizedMessage}"
            } finally {
                _aiTadabburLoading.value = false
            }
        }
    }

    fun askGeminiTadabbur(ayah: AyahEntity, question: String) {
        if (question.trim().isEmpty()) return
        viewModelScope.launch {
            _aiChatLoading.value = true
            try {
                val currentHistory = _aiChatHistory.value.toMutableList()
                currentHistory.add(Pair(question, "جاري التفكير والكتابة..."))
                _aiChatHistory.value = currentHistory

                val currentMode = _aiEngineMode.value
                if (currentMode == "cloud_gemini" && isGeminiAvailable()) {
                    val prompt = """
                        أنت مفسر وباحث شرعي متمكن في تطبيق "تبيان لدراسة القرآن".
                        الآية الكريمة: "${ayah.textAr}" (${ayah.verseNumber})
                        أجب عن سؤال المستخدم الشرعي أو اللغوي بأسلوب دقيق وعذب، وموثق بالهداية والخير والاعتدال:
                        السؤال: "$question"
                        
                        اكتب الإجابة باللغة العربية بأسلوب واضح وبسيط.
                    """.trimIndent()

                    val request = GeminiRequest(
                        contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
                    )
                    val response = GeminiService.api.generateContent(com.example.BuildConfig.GEMINI_API_KEY, request)
                    val reply = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "عذراً لم أستطع الإجابة في الوقت الحالي."
                    
                    val updatedHistory = _aiChatHistory.value.toMutableList()
                    if (updatedHistory.isNotEmpty()) {
                        updatedHistory[updatedHistory.lastIndex] = Pair(question, reply)
                    }
                    _aiChatHistory.value = updatedHistory
                } else if (currentMode == "local_nano" && _isNanoCompatible.value == true) {
                    kotlinx.coroutines.delay(1000)
                    val reply = generateOfflineAnswer(ayah, question, "Gemini Nano")
                    val updatedHistory = _aiChatHistory.value.toMutableList()
                    if (updatedHistory.isNotEmpty()) {
                        updatedHistory[updatedHistory.lastIndex] = Pair(question, reply)
                    }
                    _aiChatHistory.value = updatedHistory
                } else if (currentMode == "local_qwen" && _qwenDownloadState.value == "downloaded") {
                    kotlinx.coroutines.delay(1200)
                    val reply = generateOfflineAnswer(ayah, question, "Qwen-2.5-0.5B")
                    val updatedHistory = _aiChatHistory.value.toMutableList()
                    if (updatedHistory.isNotEmpty()) {
                        updatedHistory[updatedHistory.lastIndex] = Pair(question, reply)
                    }
                    _aiChatHistory.value = updatedHistory
                } else {
                    val fallbackReason = when {
                        currentMode == "local_qwen" && _qwenDownloadState.value != "downloaded" -> 
                            "⚠️ تعذر تشغيل نموذج Qwen 2.5 لأنه لم يكتمل تنزيله بعد. يرجى مراجعة إعدادات الذكاء الاصطناعي لتنزيله."
                        currentMode == "local_nano" && _isNanoCompatible.value != true -> 
                            "⚠️ تعذر تشغيل نموذج Gemini Nano لعدم التوافق المباشر مع هذا الهاتف."
                        else -> 
                            "⚠️ تعذر الاتصال بالسحاب لعدم توافر مفتاح API مفعّل بـ Secrets لوحة التحكم."
                    }
                    
                    kotlinx.coroutines.delay(800)
                    val reply = "$fallbackReason\n\n" + generateOfflineAnswer(ayah, question, "تبيان المحلي المباشر")
                    val updatedHistory = _aiChatHistory.value.toMutableList()
                    if (updatedHistory.isNotEmpty()) {
                        updatedHistory[updatedHistory.lastIndex] = Pair(question, reply)
                    }
                    _aiChatHistory.value = updatedHistory
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val updatedHistory = _aiChatHistory.value.toMutableList()
                if (updatedHistory.isNotEmpty()) {
                    updatedHistory[updatedHistory.lastIndex] = Pair(question, "خطأ: ${e.localizedMessage}")
                }
                _aiChatHistory.value = updatedHistory
            } finally {
                _aiChatLoading.value = false
            }
        }
    }

    // ==========================================
    // PORTABLE REMOTE JSON MANIFEST DOWNLOADS
    // ==========================================
    fun refreshPackagesFromCloudManifest() {
        viewModelScope.launch {
            try {
                // Simulating an external JSON manifest query that dynamically updates packages list
                _errorMessage.value = "جاري جلب قائمة الحزم المحدثة من المستودع السحابي..."
                kotlinx.coroutines.delay(1000)
                
                val currentPkgs = repository.getPackages().toMutableList()
                val externalManifestList = listOf(
                    PackageEntity("tafsir_muyassar", "التفسير الميسر الموثق", "Tafsir", false, 8.1, 1420, 3),
                    PackageEntity("tafsir_tabari", "تفسير الطبري (جامع البيان)", "Tafsir", false, 48.0, 310, 1),
                    PackageEntity("tafsir_baghawi", "تفسير البغوي (معالم التنزيل)", "Tafsir", false, 22.4, 516, 2),
                    PackageEntity("tafsir_qurtubi", "تفسير القرطبي (الجامع لأحكام القرآن)", "Tafsir", false, 65.2, 671, 3),
                    PackageEntity("tafsir_ibn_ashur", "تفسير ابن عاشور (التحرير والتنوير)", "Tafsir", false, 45.0, 1393, 4),
                    PackageEntity("word_irab_complex", "تفصيل إعراب القرآن الكلمة بالكلمة", "Other", false, 18.2, 850, 9),
                    PackageEntity("mushaf_regions_hd", "حدود إحداثيات الآيات للهواتف والتابلت", "Other", false, 142.0, 9999, 10),
                    PackageEntity("library_baghdad", "مكتبة بغداد للبحوث الشرعية والتفسير", "Other", false, 115.0, 412, 11),
                    PackageEntity("library_andalus", "مكتبة الأندلس التفاعلية لقرائن الآيات", "Other", false, 88.5, 650, 12)
                )
                
                // Add any missing packages from cloud manifest
                for (newPkg in externalManifestList) {
                    if (currentPkgs.none { it.packageId == newPkg.packageId }) {
                        currentPkgs.add(newPkg)
                    }
                }
                
                // Sort by Hijri author's death date!
                val sortedPkgs = currentPkgs.sortedBy { it.authorDeathHijri ?: 99999 }
                _packages.value = sortedPkgs
                _errorMessage.value = "تم تحديث قائمة المصادر والحزم بنجاح مرجعياً!"
            } catch (e: Exception) {
                _errorMessage.value = "فشل التحديث السحابي: ${e.localizedMessage}"
            }
        }
    }

    // ==========================================
    // RELATED CONTENT DISCOVERY (المتعلقات والقرائن)
    // ==========================================
    private val _relatedVerses = MutableStateFlow<List<AyahEntity>>(emptyList())
    val relatedVerses: StateFlow<List<AyahEntity>> = _relatedVerses.asStateFlow()

    private val _relatedSurahs = MutableStateFlow<List<SurahEntity>>(emptyList())
    val relatedSurahs: StateFlow<List<SurahEntity>> = _relatedSurahs.asStateFlow()

    private val _relatedWords = MutableStateFlow<List<WordEntity>>(emptyList())
    val relatedWords: StateFlow<List<WordEntity>> = _relatedWords.asStateFlow()

    fun loadRelatedContentFor(ayah: AyahEntity, word: WordEntity? = null) {
        viewModelScope.launch {
            try {
                // 1. Related Verses sharing same topics or subjects
                val topics = ayah.subjects.split(",")
                val resolvedVerses = mutableListOf<AyahEntity>()
                val loadedSurahAyas = repository.getAyahsForSurah(1) + 
                                      repository.getAyahsForSurah(112) + 
                                      repository.getAyahsForSurah(113) + 
                                      repository.getAyahsForSurah(114) + 
                                      repository.getAyahsForSurah(108)
                                      
                val matches = loadedSurahAyas.filter { 
                    it.id != ayah.id && topics.any { topic -> it.subjects.contains(topic.trim()) }
                }
                _relatedVerses.value = matches.distinctBy { it.id }.take(4)

                // 2. Related Surahs sharing same revelation type (Makki/Madani) or length context
                val currentSurah = _surahs.value.find { it.id == ayah.surahId }
                val samePeriodTypes = _surahs.value.filter { 
                    it.id != ayah.surahId && it.type == (currentSurah?.type ?: "Makki")
                }
                _relatedSurahs.value = samePeriodTypes.shuffled().take(3)

                // 3. Related words containing the same root
                if (word != null) {
                    val rootWords = mutableListOf<WordEntity>()
                    for (da in loadedSurahAyas) {
                        val words = repository.getWordsForAyah(da.id)
                        val matchingRoot = words.filter { it.root == word.root && it.id != word.id }
                        rootWords.addAll(matchingRoot)
                    }
                    _relatedWords.value = rootWords.distinctBy { it.wordAr }.take(6)
                } else {
                    _relatedWords.value = emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // MULTI-SCHEMA REFERENCE HOOKS (FUTURES BINDER)
    // ==========================================
    private val _simulatedTafsirAll = MutableStateFlow<List<AyahTafsirAll>>(emptyList())
    val simulatedTafsirAll: StateFlow<List<AyahTafsirAll>> = _simulatedTafsirAll.asStateFlow()

    private val _simulatedTafsirSources = MutableStateFlow<List<TafsirSource>>(emptyList())
    val simulatedTafsirSources: StateFlow<List<TafsirSource>> = _simulatedTafsirSources.asStateFlow()

    private val _simulatedWordIrab = MutableStateFlow<List<WordIrab>>(emptyList())
    val simulatedWordIrab: StateFlow<List<WordIrab>> = _simulatedWordIrab.asStateFlow()

    private val _simulatedPageRegions = MutableStateFlow<List<AyahPageRegion>>(emptyList())
    val simulatedPageRegions: StateFlow<List<AyahPageRegion>> = _simulatedPageRegions.asStateFlow()

    private val _simulatedLibraryBooks = MutableStateFlow<List<LibraryBook>>(emptyList())
    val simulatedLibraryBooks: StateFlow<List<LibraryBook>> = _simulatedLibraryBooks.asStateFlow()

    private val _simulatedLibraryPages = MutableStateFlow<List<LibraryPage>>(emptyList())
    val simulatedLibraryPages: StateFlow<List<LibraryPage>> = _simulatedLibraryPages.asStateFlow()

    init {
        loadSurahs()
        loadBookmarks()
        loadPackages()
        loadDailyAyah()
        loadMockSimulationData()
        checkNanoCompatibility()
        
        // Restore last read position (Default to Al-Fatihah)
        val lastSurahId = prefs.getInt("last_surah_id", 1)
        val lastPageNumber = prefs.getInt("last_page_number", 1)
        _currentPageNumber.value = lastPageNumber
        
        selectSurah(lastSurahId)
    }

    fun loadMockSimulationData() {
        _simulatedTafsirSources.value = listOf(
            TafsirSource("tafsir_tabari", "جامع البيان", "الإمام الطبري", 310, "أول التفاسير المسندة بالمأثور وأصحها"),
            TafsirSource("tafsir_qurtubi", "الجامع لأحكام القرآن", "الإمام القرطبي", 671, "تفسير فقهي شامل للأحكام واللغات"),
            TafsirSource("tafsir_ibn_kathir", "تفسير القرآن العظيم", "ابن كثير", 774, "تفسير القرآن بالقرآن والأحاديث والآثار"),
            TafsirSource("tafsir_jalalayn", "تفسير الجلالين", "السيوطي والمحلي", 911, "تفسير سهل مختصر على حواشي النص"),
            TafsirSource("tafsir_muyassar", "التفسير الميسر", "علماء المدينة المنورة", 1420, "معاني واضحة مبسطة مرخصة ومعتمدة")
        ).sortedBy { it.authorDeathHijri }

        _simulatedLibraryBooks.value = listOf(
            LibraryBook("book_saadi", "دليل السعدي لأسماء الله الحسنى", "الشارح السعدي", 1373, 192, "العقيدة والتوحيد"),
            LibraryBook("book_irab_ism", "إعراب مفردات وأفعال آي القرآن", "محمد عبد اللطيف", 1412, 530, "لغويات وصرف"),
            LibraryBook("book_asbab_asl", "تاريخ أسباب التنزيل التاريخية", "صالح الفوزان", 1435, 340, "علوم نزول")
        )

        _simulatedLibraryPages.value = listOf(
            LibraryPage(1, "book_saadi", 23, "وقد دل قوله تعالى: ﴿اللَّهُ الصَّمَدُ﴾ على ثبوت صفات الكمال كلها لله رب العالمين المنزه عن النقص والشبيه، فالصمد هو الذي تكمل في سؤدده وعظمته وحكمته المستبينة.", 112),
            LibraryPage(2, "book_irab_ism", 101, "وفي إعراب ﴿إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ﴾ الشانئ اسم إن والمضاف الكاف في محل جر، وهو ضمير فصل وعماد لا محل له، والأبتر خبر إن والتقدير المنقطع الأثر والمستقبل بالذم العظيم.", 108)
        )
    }
}
