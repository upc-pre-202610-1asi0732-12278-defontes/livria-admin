package com.example.adminlivria.common.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.adminlivria.bookcontext.data.local.BookEntity
import com.example.adminlivria.bookcontext.data.local.BookDao
import com.example.adminlivria.statscontext.data.local.StatEntity
import com.example.adminlivria.statscontext.data.local.StatsDao

@Database(
    entities = [BookEntity::class, StatEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AdminDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun statsDao(): StatsDao

    companion object {
        @Volatile private var INSTANCE: AdminDatabase? = null

        fun getInstance(context: Context): AdminDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AdminDatabase::class.java,
                    "admin_livria.db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}