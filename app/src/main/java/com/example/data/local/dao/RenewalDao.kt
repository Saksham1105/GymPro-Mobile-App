package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.RenewalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RenewalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRenewal(renewal: RenewalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(renewals: List<RenewalEntity>)

    @Query("SELECT * FROM renewals WHERE memberId = :memberId ORDER BY renewalDate DESC")
    fun getRenewalsForMember(memberId: Int): Flow<List<RenewalEntity>>

    @Query("SELECT * FROM renewals ORDER BY renewalDate DESC")
    fun getAllRenewals(): Flow<List<RenewalEntity>>

    @Query("SELECT * FROM renewals ORDER BY renewalDate DESC")
    suspend fun getAllRenewalsDirect(): List<RenewalEntity>

    @Query("DELETE FROM renewals")
    suspend fun deleteAll()
}
