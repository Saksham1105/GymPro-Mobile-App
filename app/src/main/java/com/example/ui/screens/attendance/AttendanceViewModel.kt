package com.example.ui.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.MemberEntity
import com.example.di.Graph
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class MemberAttendanceState(
    val member: MemberEntity,
    val todayStatus: String? // "Present", "Absent", or null
)

class AttendanceViewModel : ViewModel() {
    private val memberRepository = Graph.memberRepository
    private val attendanceRepository = Graph.attendanceRepository
    
    private val _showExpired = MutableStateFlow(false)
    val showExpired: StateFlow<Boolean> = _showExpired

    init {
        viewModelScope.launch {
            memberRepository.checkAndExpireMembers()
        }
    }

    fun toggleShowExpired() {
        _showExpired.value = !_showExpired.value
    }
    
    val membersWithAttendance: StateFlow<List<MemberAttendanceState>> = combine(
        memberRepository.allMembers,
        attendanceRepository.getAttendanceForDateRange(
            getTodayStartTimestamp(),
            getTodayEndTimestamp()
        ),
        _showExpired
    ) { allMembers, todayAttendance, showExpired ->
        val filteredMembers = if (showExpired) {
            allMembers
        } else {
            allMembers.filter { it.status != "Expired" }
        }
        filteredMembers.map { member ->
            val status = todayAttendance.find { it.memberId == member.id }?.status
            MemberAttendanceState(member, status)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun markAttendance(memberId: Int, status: String) {
        viewModelScope.launch {
            val todayStart = getTodayStartTimestamp()
            val todayEnd = getTodayEndTimestamp()
            
            val existing = attendanceRepository.getAttendanceForMemberForDate(memberId, todayStart, todayEnd)
            if (existing != null) {
                if (existing.status != status) {
                    attendanceRepository.updateAttendance(existing.copy(status = status))
                }
            } else {
                val attendance = AttendanceEntity(
                    memberId = memberId,
                    date = System.currentTimeMillis(), // Store exact time of marking, but queries can filter by date range
                    status = status
                )
                attendanceRepository.insertAttendance(attendance)
            }
        }
    }

    companion object {
        fun getTodayStartTimestamp(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }

        fun getTodayEndTimestamp(): Long {
            return getTodayStartTimestamp() + 24 * 60 * 60 * 1000L - 1
        }
    }
}
