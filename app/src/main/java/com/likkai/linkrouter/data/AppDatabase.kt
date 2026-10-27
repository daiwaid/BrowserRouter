package com.likkai.linkrouter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [BrowserRule::class, RedirectDomain::class, RedirectCache::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun browserRuleDao(): BrowserRuleDao
    abstract fun redirectDomainDao(): RedirectDomainDao
    abstract fun redirectCacheDao(): RedirectCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val DEFAULT_REDIRECT_DOMAINS = listOf(
            "t.co", "bit.ly", "is.gd", "goo.gl", "tinyurl.com",
            "ow.ly", "buff.ly", "rb.gy", "short.io"
        )

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE browser_rules ADD COLUMN sourcePackage TEXT")
                db.execSQL("ALTER TABLE browser_rules ADD COLUMN sourceLabel TEXT")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Old domain rules match the whole site: example.com -> *.example.com/*
                db.execSQL(
                    """
                    UPDATE browser_rules
                    SET pattern = '*.' || pattern || '/*'
                    WHERE matchType = 'DOMAIN'
                      AND pattern != ''
                      AND instr(pattern, '*') = 0
                      AND instr(pattern, '/') = 0
                      AND instr(pattern, '://') = 0
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE browser_rules
                    SET matchType = 'PATTERN'
                    WHERE matchType IN ('DOMAIN', 'EXACT_URL', 'SOURCE_APP')
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Patterns saved as *.host used to mean every path. Keep that behavior.
                db.execSQL(
                    """
                    UPDATE browser_rules
                    SET pattern = pattern || '/*'
                    WHERE pattern LIKE '*.%'
                      AND instr(pattern, '/') = 0
                      AND length(pattern) - length(replace(pattern, '*', '')) = 1
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "linkrouter_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate default redirect domains
                            DEFAULT_REDIRECT_DOMAINS.forEach { domain ->
                                db.execSQL(
                                    "INSERT INTO redirect_domains (domain) VALUES (?)",
                                    arrayOf(domain)
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
