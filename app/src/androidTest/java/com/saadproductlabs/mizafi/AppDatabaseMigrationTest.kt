package com.saadproductlabs.mizafi

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.saadproductlabs.mizafi.data.database.AppDatabase
import com.saadproductlabs.mizafi.data.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val databaseName = "migration-test"
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrate1To2_preservesTransactionsAndCreatesSavingsGoals() = runBlocking {
        val databaseFile = context.getDatabasePath(databaseName)
        databaseFile.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { versionOne ->
            versionOne.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `transactions` (
                    `id` TEXT NOT NULL,
                    `amount` REAL NOT NULL,
                    `category` TEXT NOT NULL,
                    `date` INTEGER NOT NULL,
                    `type` TEXT NOT NULL,
                    `note` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            versionOne.execSQL(
                """
                INSERT INTO `transactions`
                    (`id`, `amount`, `category`, `date`, `type`, `note`)
                VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf("legacy-id", 12.5, "Food", 1_700_000_000_000, "EXPENSE", "Lunch")
            )
            versionOne.version = 1
        }

        database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()

        val migratedTransactions = database.transactionDao().getAllTransactions().first()
        assertEquals(1, migratedTransactions.size)
        assertEquals("legacy-id", migratedTransactions.single().id)
        assertEquals("Lunch", migratedTransactions.single().note)

        database.savingsGoalDao().insertGoal(
            SavingsGoalEntity(
                id = "goal-id",
                name = "Emergency fund",
                targetAmount = 1_000.0
            )
        )
        assertTrue(database.savingsGoalDao().getAllGoals().first().isNotEmpty())
    }
}
