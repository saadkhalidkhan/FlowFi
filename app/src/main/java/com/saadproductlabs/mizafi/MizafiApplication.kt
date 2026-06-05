package com.saadproductlabs.mizafi

import android.app.Application
import com.saadproductlabs.mizafi.data.database.AppDatabase
import com.saadproductlabs.mizafi.data.repository.SavingsGoalRepository
import com.saadproductlabs.mizafi.data.repository.SavingsGoalRepositoryImpl
import com.saadproductlabs.mizafi.data.repository.TransactionRepository
import com.saadproductlabs.mizafi.data.repository.TransactionRepositoryImpl

class MizafiApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: TransactionRepository by lazy { TransactionRepositoryImpl(database.transactionDao()) }
    val savingsGoalRepository: SavingsGoalRepository by lazy {
        SavingsGoalRepositoryImpl(database.savingsGoalDao())
    }
}
