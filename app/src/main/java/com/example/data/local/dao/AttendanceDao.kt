package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE memberId = :memberId ORDER BY date DESC")
    fun getAttendanceForMember(memberId: Int): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date >= :startOfDay AND date <= :endOfDay")
    fun getAttendanceForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<AttendanceEntity>>
    
    @Query("SELECT COUNT(DISTINCT memberId) FROM attendance WHERE date >= :startOfDay AND date <= :endOfDay AND status = 'Present'")
    fun getPresentCountForDateRange(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT memberId) FROM attendance WHERE date >= :startOfDay AND date <= :endOfDay AND status = 'Absent'")
    fun getAbsentCountForDateRange(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query("SELECT * FROM attendance WHERE memberId = :memberId AND date >= :startOfDay AND date <= :endOfDay LIMIT 1")
    suspend fun getAttendanceForMemberForDate(memberId: Int, startOfDay: Long, endOfDay: Long): AttendanceEntity?

    @Query("SELECT * FROM attendance ORDER BY date DESC LIMIT 5")
    fun getRecentAttendance(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendance: List<AttendanceEntity>)

    @Query("SELECT * FROM attendance")
    suspend fun getAllAttendanceDirect(): List<AttendanceEntity>

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()
}
