package com.example.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.di.Graph
import com.example.utils.FormatUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class RecentActivity(
    val initials: String,
    val name: String,
    val desc: String,
    val isRecent: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel : ViewModel() {
    private val memberRepository = Graph.memberRepository
    private val attendanceRepository = Graph.attendanceRepository
    
    init {
        viewModelScope.launch {
            memberRepository.checkAndExpireMembers()
        }
    }

    private fun getTodayStartTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getTodayEndTimestamp(): Long {
        return getTodayStartTimestamp() + 24 * 60 * 60 * 1000L - 1
    }

    val totalMembers = memberRepository.memberCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    val activeMembers = memberRepository.activeMemberCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val expiredMembers = memberRepository.allMembers.map { list ->
        list.count { it.status == "Expired" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    val presentToday = attendanceRepository.getPresentCountForDateRange(
        getTodayStartTimestamp(),
        getTodayEndTimestamp()
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val absentToday = attendanceRepository.getAbsentCountForDateRange(
        getTodayStartTimestamp(),
        getTodayEndTimestamp()
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    
    val upcomingExpiredCount = memberRepository.allMembers.map { members ->
        val now = System.currentTimeMillis()
        val limit = now + 7 * 24 * 60 * 60 * 1000L
        members.count { member ->
            member.status == "Active" && member.expiryDate in (now + 1)..limit
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val monthlyRevenue: StateFlow<Double> = Graph.renewalRepository.allRenewals.map { renewals ->
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        renewals.filter { it.renewalDate >= startOfMonth }
            .sumOf { it.amountPaid }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    val pastWeekCheckIns: StateFlow<List<Pair<String, Int>>> = memberRepository.allMembers.flatMapLatest {
        val end = System.currentTimeMillis()
        val start = end - 7 * 24 * 60 * 60 * 1000L
        attendanceRepository.getAttendanceForDateRange(start, end).map { attendanceList ->
            val daysList = (0..6).map { offset ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -offset)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal
            }.reversed()
            
            daysList.map { dayCal ->
                val dayStart = dayCal.timeInMillis
                val dayEnd = dayStart + 24 * 60 * 60 * 1000L - 1
                val count = attendanceList.count { it.date in dayStart..dayEnd && it.status == "Present" }
                val label = FormatUtils.formatDayOfWeek(dayCal.timeInMillis)
                Pair(label, count)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentActivities = combine(
        attendanceRepository.getRecentAttendance(),
        memberRepository.allMembers
    ) { attendanceList, members ->
        attendanceList.map { attendance ->
            val member = members.find { it.id == attendance.memberId }
            val name = member?.name ?: "Unknown Member"
            val initials = name.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .uppercase()
            
            val timeStr = FormatUtils.formatTime(attendance.date)
            
            RecentActivity(
                initials = if (initials.isNotEmpty()) initials else "??",
                name = name,
                desc = "Marked ${attendance.status} • $timeStr",
                isRecent = (System.currentTimeMillis() - attendance.date) < 60 * 60 * 1000L
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
