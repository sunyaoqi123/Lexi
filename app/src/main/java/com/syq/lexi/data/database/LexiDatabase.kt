package com.syq.lexi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WordbookEntity::class, WordEntity::class, StudyRecordEntity::class, StudyPlanEntity::class],
    version = 2,
    exportSchema = false
)
abstract class LexiDatabase : RoomDatabase() {
    abstract fun wordbookDao(): WordbookDao
    abstract fun wordDao(): WordDao
    abstract fun studyRecordDao(): StudyRecordDao
    abstract fun studyPlanDao(): StudyPlanDao

    companion object {
        @Volatile
        private var INSTANCE: LexiDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS study_plans (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "wordbookId INTEGER NOT NULL, " +
                    "dailyWords INTEGER NOT NULL, " +
                    "createdDate INTEGER NOT NULL)"
                )
            }
        }

        fun getDatabase(context: Context): LexiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LexiDatabase::class.java,
                    "lexi_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
