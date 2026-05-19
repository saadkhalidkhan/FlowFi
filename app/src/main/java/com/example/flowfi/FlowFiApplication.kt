package com.example.flowfi

import android.app.Application
import com.example.flowfi.data.database.AppDatabase
import com.example.flowfi.data.repository.TransactionRepository
import com.example.flowfi.data.repository.TransactionRepositoryImpl

class FlowFiApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: TransactionRepository by lazy { TransactionRepositoryImpl(database.transactionDao()) }
}
