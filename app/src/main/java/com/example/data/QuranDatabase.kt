package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.concurrent.Executors

@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        WordEntity::class,
        BookmarkEntity::class,
        NoteEntity::class,
        PackageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuranDatabase : RoomDatabase() {

    abstract fun quranDao(): QuranDao

    companion object {
        @Volatile
        private var INSTANCE: QuranDatabase? = null

        fun getDatabase(context: Context): QuranDatabase {
            return INSTANCE ?: synchronized(this) {
                val assetManager = context.assets
                val hasAssetDb = try {
                    assetManager.list("")?.contains("quran_study_db.db") == true
                } catch (e: Exception) {
                    false
                }

                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    QuranDatabase::class.java,
                    "quran_study_db"
                ).fallbackToDestructiveMigration(dropAllTables = true)

                if (hasAssetDb) {
                    builder.createFromAsset("quran_study_db.db")
                }

                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
