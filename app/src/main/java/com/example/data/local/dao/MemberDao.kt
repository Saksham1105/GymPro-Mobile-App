package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id")
    fun getMemberById(id: Int): Flow<MemberEntity?>

    @Query("SELECT * FROM members WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%'")
    fun searchMembers(query: String): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<MemberEntity>)

    @Query("SELECT * FROM members")
    suspend fun getAllMembersDirect(): List<MemberEntity>

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("DELETE FROM members")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM members")
    fun getMemberCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM members WHERE status = 'Active'")
    fun getActiveMemberCount(): Flow<Int>
}
