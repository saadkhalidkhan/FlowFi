package com.saadproductlabs.mizafi

import com.saadproductlabs.mizafi.data.entity.SavingsGoalEntity
import com.saadproductlabs.mizafi.data.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSavingsGoalRepository : SavingsGoalRepository {
    private val goals = mutableListOf<SavingsGoalEntity>()
    private val flow = MutableStateFlow<List<SavingsGoalEntity>>(emptyList())

    private fun publish() {
        flow.value = goals.toList()
    }

    override fun getAllGoals(): Flow<List<SavingsGoalEntity>> = flow

    override suspend fun insertGoal(goal: SavingsGoalEntity) {
        goals.removeAll { it.id == goal.id }
        goals.add(goal)
        publish()
    }

    override suspend fun updateGoal(goal: SavingsGoalEntity) {
        insertGoal(goal)
    }

    override suspend fun deleteGoal(goal: SavingsGoalEntity) {
        goals.removeAll { it.id == goal.id }
        publish()
    }
}
