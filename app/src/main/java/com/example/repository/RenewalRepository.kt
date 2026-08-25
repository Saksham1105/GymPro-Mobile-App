package com.example.repository

import com.example.data.local.dao.RenewalDao
import com.example.data.local.entity.RenewalEntity
import kotlinx.coroutines.flow.Flow

class RenewalRepository(private val renewalDao: RenewalDao) {
    val allRenewals: Flow<List<RenewalEntity>> = renewalDao.getAllRenewals()

    suspend fun insertRenewal(renewal: RenewalEntity) = renewalDao.insertRenewal(renewal)

    suspend fun insertAll(renewals: List<RenewalEntity>) = renewalDao.insertAll(renewals)

    fun getRenewalsForMember(memberId: Int): Flow<List<RenewalEntity>> = renewalDao.getRenewalsForMember(memberId)

    suspend fun getAllRenewalsDirect(): List<RenewalEntity> = renewalDao.getAllRenewalsDirect()

    suspend fun deleteAll() = renewalDao.deleteAll()
}
