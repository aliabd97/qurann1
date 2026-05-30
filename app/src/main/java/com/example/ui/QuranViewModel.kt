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

    private val _showTranslation = MutableStateFlow(true)
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

    init {
        loadSurahs()
        loadBookmarks()
        loadPackages()
        loadDailyAyah()
        
        // Restore last read position (Default to Al-Fatihah)
        val lastSurahId = prefs.getInt("last_surah_id", 1)
        val lastPageNumber = prefs.getInt("last_page_number", 1)
        _currentPageNumber.value = lastPageNumber
        
        selectSurah(lastSurahId)
    }

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
}
