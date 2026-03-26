package com.syq.lexi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WordbookEntity::class, WordEntity::class, StudyRecordEntity::class, StudyPlanEntity::class],
    version = 5,
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "DELETE FROM words WHERE id NOT IN (" +
                    "SELECT MIN(id) FROM words GROUP BY wordbookId, english)"
                )
                database.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_words_wordbookId_english " +
                    "ON words (wordbookId, english)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE words ADD COLUMN isStarred INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // WordEntity 新增复习字段
                database.execSQL("ALTER TABLE words ADD COLUMN familiarity REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE words ADD COLUMN reviewCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE words ADD COLUMN nextReviewDate INTEGER NOT NULL DEFAULT 0")
                // StudyRecordEntity 新增字段
                database.execSQL("ALTER TABLE study_records ADD COLUMN phase INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE study_records ADD COLUMN hesitationMs INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE study_records ADD COLUMN durationMs INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): LexiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LexiDatabase::class.java,
                    "lexi_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
