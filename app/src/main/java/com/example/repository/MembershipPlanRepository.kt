package com.example.repository

import com.example.data.local.dao.MembershipPlanDao
import com.example.data.local.entity.MembershipPlanEntity
import kotlinx.coroutines.flow.Flow

class MembershipPlanRepository(private val planDao: MembershipPlanDao) {
    val allPlans: Flow<List<MembershipPlanEntity>> = planDao.getAllPlans()

    fun getPlanById(id: Int): Flow<MembershipPlanEntity?> = planDao.getPlanById(id)

    suspend fun insertPlan(plan: MembershipPlanEntity): Long = planDao.insertPlan(plan)

    suspend fun insertAll(plans: List<MembershipPlanEntity>) = planDao.insertAll(plans)

    suspend fun getAllPlansDirect(): List<MembershipPlanEntity> = planDao.getAllPlansDirect()

    suspend fun updatePlan(plan: MembershipPlanEntity) = planDao.updatePlan(plan)

    suspend fun deletePlan(plan: MembershipPlanEntity) = planDao.deletePlan(plan)

    suspend fun deleteAll() = planDao.deleteAll()
}
