package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MembershipPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MembershipPlanDao {
    @Query("SELECT * FROM membership_plans ORDER BY price ASC")
    fun getAllPlans(): Flow<List<MembershipPlanEntity>>

    @Query("SELECT * FROM membership_plans WHERE id = :id")
    fun getPlanById(id: Int): Flow<MembershipPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: MembershipPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<MembershipPlanEntity>)

    @Query("SELECT * FROM membership_plans")
    suspend fun getAllPlansDirect(): List<MembershipPlanEntity>

    @Update
    suspend fun updatePlan(plan: MembershipPlanEntity)

    @Delete
    suspend fun deletePlan(plan: MembershipPlanEntity)

    @Query("DELETE FROM membership_plans")
    suspend fun deleteAll()
}
