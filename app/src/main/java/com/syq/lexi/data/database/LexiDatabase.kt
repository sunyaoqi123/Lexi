package com.syq.lexi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WordbookEntity::class, WordEntity::class, StudyRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LexiDatabase : RoomDatabase() {
    abstract fun wordbookDao(): WordbookDao
    abstract fun wordDao(): WordDao
    abstract fun studyRecordDao(): StudyRecordDao

    companion object {
        @Volatile
        private var INSTANCE: LexiDatabase? = null

        fun getDatabase(context: Context): LexiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LexiDatabase::class.java,
                    "lexi_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
