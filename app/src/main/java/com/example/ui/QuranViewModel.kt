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

    fun loadAiTadabburForAyah(ayah: AyahEntity) {
        viewModelScope.launch {
            _aiTadabburLoading.value = true
            _aiTadabburText.value = ""
            try {
                if (isGeminiAvailable()) {
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
                } else {
                    _aiTadabburText.value = """
                        ⚠️ ميزة التدبر الإيماني والتحليل البلاغي بالذكاء الاصطناعي معطلة لأن مفتاح API غير مفعّل. 
                        
                        لتفعيل هذه الميزة الفائقة:
                        ١. يرجى إدخال مفتاح Gemini API الخاص بك بأمان في لوحة "Secrets" بداخل واجهة Google AI Studio.
                        ٢. سيقوم التطبيق تلقائياً بربط الخدمة وتوليد ملخصات وتفاسير وإعجاز بياني فريد لكل آية كريمة بضغطة زر واحدة!
                        
                        نظرة سريعة على الآية:
                        • الآية الكريمة تأمر بالهداية والخير والتدبر وتوضح معاني التوحيد الإلهي في التفسير الميسر (${ayah.tafsirJalalayn}).
                    """.trimIndent()
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

                if (isGeminiAvailable()) {
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
                } else {
                    val updatedHistory = _aiChatHistory.value.toMutableList()
                    if (updatedHistory.isNotEmpty()) {
                        updatedHistory[updatedHistory.lastIndex] = Pair(question, "⚠️ تعذر الاتصال بموديل الذكاء الاصطناعي (Gemini 3.5 Flash) لعدم توفر مفتاح الـ API. يرجى توفير المفتاح في لوحة الـ Secrets في AI Studio لتشغيل المساعد الذكي بكامل طاقته!")
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
