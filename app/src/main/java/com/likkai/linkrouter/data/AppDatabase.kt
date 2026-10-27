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
    version = 3,
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

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "linkrouter_database"
                )
                    .addMigrations(MIGRATION_2_3)
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
