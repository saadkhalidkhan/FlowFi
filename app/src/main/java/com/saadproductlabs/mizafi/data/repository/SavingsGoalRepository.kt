package com.saadproductlabs.mizafi.data.repository

import com.saadproductlabs.mizafi.data.dao.SavingsGoalDao
import com.saadproductlabs.mizafi.data.entity.SavingsGoalEntity
import kotlinx.coroutines.flow.Flow

interface SavingsGoalRepository {
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>
    suspend fun insertGoal(goal: SavingsGoalEntity)
    suspend fun updateGoal(goal: SavingsGoalEntity)
    suspend fun deleteGoal(goal: SavingsGoalEntity)
}

class SavingsGoalRepositoryImpl(private val savingsGoalDao: SavingsGoalDao) : SavingsGoalRepository {
    override fun getAllGoals(): Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()

    override suspend fun insertGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.insertGoal(goal)
    }

    override suspend fun updateGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.updateGoal(goal)
    }

    override suspend fun deleteGoal(goal: SavingsGoalEntity) {
        savingsGoalDao.deleteGoal(goal)
    }
}
