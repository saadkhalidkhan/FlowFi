package com.saadproductlabs.mizafi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.saadproductlabs.mizafi.data.dao.SavingsGoalDao
import com.saadproductlabs.mizafi.data.dao.TransactionDao
import com.saadproductlabs.mizafi.data.entity.SavingsGoalEntity
import com.saadproductlabs.mizafi.data.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, SavingsGoalEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mizafi_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
