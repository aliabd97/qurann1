package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuranDao {

    @Query("SELECT * FROM surahs ORDER BY id ASC")
    fun getSurahs(): List<SurahEntity>

    @Query("SELECT * FROM surahs WHERE id = :surahId")
    fun getSurahById(surahId: Int): SurahEntity?

    @Query("SELECT * FROM ayahs WHERE surahId = :surahId ORDER BY verseNumber ASC")
    fun getAyahsForSurah(surahId: Int): List<AyahEntity>

    @Query("SELECT * FROM ayahs WHERE pageNumber = :pageNumber ORDER BY surahId ASC, verseNumber ASC")
    fun getAyahsForPage(pageNumber: Int): List<AyahEntity>

    @Query("SELECT * FROM ayahs WHERE id = :ayahId")
    fun getAyah(ayahId: Int): AyahEntity?

    @Query("SELECT * FROM ayah_words WHERE ayahId = :ayahId ORDER BY position ASC")
    fun getWordsForAyah(ayahId: Int): List<WordEntity>

    @Query("SELECT * FROM bookmarks")
    fun getBookmarks(): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE ayahId = :ayahId")
    fun deleteBookmark(ayahId: Int)

    @Query("SELECT * FROM notes WHERE ayahId = :ayahId")
    fun getNoteForAyah(ayahId: Int): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE ayahId = :ayahId")
    fun deleteNote(ayahId: Int)

    @Query("SELECT * FROM packages ORDER BY COALESCE(authorDeathHijri, 999999) ASC, displayOrder ASC, name ASC")
    fun getPackages(): List<PackageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPackages(packages: List<PackageEntity>)

    @Query("UPDATE packages SET isInstalled = :isInstalled WHERE packageId = :packageId")
    fun updatePackageInstallation(packageId: String, isInstalled: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSurahs(surahs: List<SurahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAyahs(ayahs: List<AyahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWords(words: List<WordEntity>)
}
