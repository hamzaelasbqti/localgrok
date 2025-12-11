package com.localgrok.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.localgrok.data.local.dao.ChatDao
import com.localgrok.data.local.dao.MessageDao
import com.localgrok.data.local.entity.ChatEntity
import com.localgrok.data.local.entity.MessageEntity

@Database(
    entities = [ChatEntity::class, MessageEntity::class],
    version = 3,
    exportSchema = false
)
abstract class LocalGrokDatabase : RoomDatabase() {
    
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    
    companion object {
        @Volatile
        private var INSTANCE: LocalGrokDatabase? = null
        
        // Migration from version 1 to 2: Add isThinking column to messages table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN isThinking INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        // Migration from version 2 to 3: Add reasoningContent column to messages table
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE messages ADD COLUMN reasoningContent TEXT NOT NULL DEFAULT ''")
            }
        }
        
        fun getDatabase(context: Context): LocalGrokDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalGrokDatabase::class.java,
                    "localgrok_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

