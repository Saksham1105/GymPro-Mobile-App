package com.example.repository

import com.example.data.local.dao.AttendanceDao
import com.example.data.local.entity.AttendanceEntity
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    fun getAttendanceForMember(memberId: Int): Flow<List<AttendanceEntity>> = attendanceDao.getAttendanceForMember(memberId)

    fun getAttendanceForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForDateRange(startOfDay, endOfDay)
        
    suspend fun getAttendanceForMemberForDate(memberId: Int, startOfDay: Long, endOfDay: Long): AttendanceEntity? =
        attendanceDao.getAttendanceForMemberForDate(memberId, startOfDay, endOfDay)
        
    fun getPresentCountForDateRange(startOfDay: Long, endOfDay: Long): Flow<Int> =
        attendanceDao.getPresentCountForDateRange(startOfDay, endOfDay)

    fun getAbsentCountForDateRange(startOfDay: Long, endOfDay: Long): Flow<Int> =
        attendanceDao.getAbsentCountForDateRange(startOfDay, endOfDay)

    fun getRecentAttendance(): Flow<List<AttendanceEntity>> =
        attendanceDao.getRecentAttendance()

    suspend fun insertAttendance(attendance: AttendanceEntity): Long = attendanceDao.insertAttendance(attendance)

    suspend fun insertAll(attendance: List<AttendanceEntity>) = attendanceDao.insertAll(attendance)

    suspend fun getAllAttendanceDirect(): List<AttendanceEntity> = attendanceDao.getAllAttendanceDirect()

    suspend fun updateAttendance(attendance: AttendanceEntity) = attendanceDao.updateAttendance(attendance)

    suspend fun deleteAttendance(attendance: AttendanceEntity) = attendanceDao.deleteAttendance(attendance)

    suspend fun deleteAll() = attendanceDao.deleteAll()
}
