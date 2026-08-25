package com.example.repository

import com.example.data.local.dao.MemberDao
import com.example.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

class MemberRepository(private val memberDao: MemberDao) {
    val allMembers: Flow<List<MemberEntity>> = memberDao.getAllMembers()
    
    val memberCount: Flow<Int> = memberDao.getMemberCount()
    
    val activeMemberCount: Flow<Int> = memberDao.getActiveMemberCount()

    fun getMemberById(id: Int): Flow<MemberEntity?> = memberDao.getMemberById(id)

    fun searchMembers(query: String): Flow<List<MemberEntity>> = memberDao.searchMembers(query)

    suspend fun insertMember(member: MemberEntity): Long = memberDao.insertMember(member)

    suspend fun insertAll(members: List<MemberEntity>) = memberDao.insertAll(members)

    suspend fun getAllMembersDirect(): List<MemberEntity> = memberDao.getAllMembersDirect()

    suspend fun updateMember(member: MemberEntity) = memberDao.updateMember(member)

    suspend fun checkAndExpireMembers() {
        val now = System.currentTimeMillis()
        val members = memberDao.getAllMembersDirect()
        members.forEach { member ->
            if (member.status == "Active" && member.expiryDate > 0 && member.expiryDate < now) {
                memberDao.updateMember(member.copy(status = "Expired"))
            }
        }
    }

    suspend fun deleteMember(member: MemberEntity) = memberDao.deleteMember(member)

    suspend fun deleteAll() = memberDao.deleteAll()
}
