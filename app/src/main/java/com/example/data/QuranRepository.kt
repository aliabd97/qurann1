package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class QuranRepository(context: Context) {
    private val database = QuranDatabase.getDatabase(context)
    private val dao = database.quranDao()

    companion object {
        private val seedMutex = Mutex()
        @Volatile
        private var isSeeded = false
    }

    private suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (isSeeded) return@withContext
        seedMutex.withLock {
            if (isSeeded) return@withLock
            val list = dao.getSurahs()
            if (list.isEmpty()) {
                QuranDbSeeder.seedAll(dao)
                QuranDbSeeder.seedWordsForAyahs(dao)
            }
            isSeeded = true
        }
    }

    suspend fun getSurahs(): List<SurahEntity> = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.getSurahs()
    }

    suspend fun getSurahById(surahId: Int): SurahEntity? = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.getSurahById(surahId)
    }

    suspend fun getAyahsForSurah(surahId: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.getAyahsForSurah(surahId)
    }

    suspend fun getAyahsForPage(pageNumber: Int): List<AyahEntity> = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.getAyahsForPage(pageNumber)
    }

    suspend fun getAyah(ayahId: Int): AyahEntity? = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.getAyah(ayahId)
    }

    suspend fun getWordsForAyah(ayahId: Int): List<WordEntity> = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.getWordsForAyah(ayahId)
    }

    suspend fun getBookmarks(): List<BookmarkEntity> = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.getBookmarks()
    }

    suspend fun isBookmarked(ayahId: Int): Boolean = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.getBookmarks().any { it.ayahId == ayahId }
    }

    suspend fun toggleBookmark(ayahId: Int) = withContext(Dispatchers.IO) {
        ensureSeeded()
        val bookmarks = dao.getBookmarks()
        if (bookmarks.any { it.ayahId == ayahId }) {
            dao.deleteBookmark(ayahId)
        } else {
            dao.insertBookmark(BookmarkEntity(ayahId))
        }
    }

    suspend fun getNoteForAyah(ayahId: Int): String? = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.getNoteForAyah(ayahId)?.noteText
    }

    suspend fun saveNote(ayahId: Int, noteText: String) = withContext(Dispatchers.IO) {
        ensureSeeded()
        if (noteText.trim().isEmpty()) {
            dao.deleteNote(ayahId)
        } else {
            dao.insertNote(NoteEntity(ayahId = ayahId, noteText = noteText))
        }
    }

    suspend fun deleteNote(ayahId: Int) = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.deleteNote(ayahId)
    }

    suspend fun getPackages(): List<PackageEntity> = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.getPackages()
    }

    suspend fun setPackageInstalled(packageId: String, isInstalled: Boolean) = withContext(Dispatchers.IO) {
        ensureSeeded()
        dao.updatePackageInstallation(packageId, isInstalled)
    }
}
