package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "surahs")
data class SurahEntity(
    @PrimaryKey val id: Int,
    val nameAr: String,
    val nameEn: String,
    val type: String, // "Makki" or "Madani"
    val versesCount: Int,
    val pageStart: Int
)

@Entity(tableName = "ayahs")
data class AyahEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surahId: Int,
    val verseNumber: Int,
    val textAr: String,
    val textClean: String,
    val pageNumber: Int,
    val translation: String,
    val tafsirIbnKathir: String,
    val tafsirJalalayn: String,
    val asbabNuzul: String,
    val irab: String,
    val subjects: String, // Comma separated topics
    val reciterAudioUrl: String = ""
)

@Entity(tableName = "ayah_words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ayahId: Int,
    val position: Int,
    val wordAr: String,
    val wordEn: String,
    val grammarTags: String, // e.g. "حرف جر / اسم"
    val root: String, // جذر الكلمة
    val lemma: String // الأصل
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val ayahId: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val ayahId: Int,
    val noteText: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey val packageId: String,
    val name: String,
    val type: String, // "Tafsir", "Audio", "Translation"
    val isInstalled: Boolean,
    val sizeMb: Double,
    val authorDeathHijri: Int? = null,
    val displayOrder: Int = 99
)
