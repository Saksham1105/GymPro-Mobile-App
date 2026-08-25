package com.example.ui.screens.members

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.MemberEntity
import com.example.data.local.entity.RenewalEntity
import com.example.di.Graph
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class SortOption(val displayName: String) {
    NAME("Name"),
    JOINING_DATE("Joining Date"),
    EXPIRY_DATE("Expiry Date"),
    RECENTLY_ADDED("Recently Added")
}

enum class FilterOption(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    EXPIRED("Expired"),
    PRESENT_TODAY("Present Today"),
    ABSENT_TODAY("Absent Today")
}

@OptIn(ExperimentalCoroutinesApi::class)
class MembersViewModel : ViewModel() {
    private val memberRepository = Graph.memberRepository
    private val attendanceRepository = Graph.attendanceRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOption = MutableStateFlow(SortOption.NAME)
    val sortOption: StateFlow<SortOption> = _sortOption

    private val _filterOption = MutableStateFlow(FilterOption.ALL)
    val filterOption: StateFlow<FilterOption> = _filterOption

    private val _selectedMemberId = MutableStateFlow<Int?>(null)
    val selectedMemberId: StateFlow<Int?> = _selectedMemberId

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

    val members: StateFlow<List<MemberEntity>> = combine(
        memberRepository.allMembers,
        _searchQuery,
        _sortOption,
        _filterOption,
        attendanceRepository.getAttendanceForDateRange(getTodayStartTimestamp(), getTodayEndTimestamp())
    ) { allMembers, query, sort, filter, todayAttendance ->
        // 1. Search filter
        var list = if (query.isBlank()) {
            allMembers
        } else {
            allMembers.filter {
                it.name.contains(query, ignoreCase = true) || 
                it.phone.contains(query, ignoreCase = true)
            }
        }

        // 2. Status / Attendance filter
        list = when (filter) {
            FilterOption.ALL -> list
            FilterOption.ACTIVE -> list.filter { it.status == "Active" }
            FilterOption.EXPIRED -> list.filter { it.status == "Expired" }
            FilterOption.PRESENT_TODAY -> {
                val presentMemberIds = todayAttendance.filter { it.status == "Present" }.map { it.memberId }.toSet()
                list.filter { it.id in presentMemberIds }
            }
            FilterOption.ABSENT_TODAY -> {
                val absentMemberIds = todayAttendance.filter { it.status == "Absent" }.map { it.memberId }.toSet()
                list.filter { it.id in absentMemberIds }
            }
        }

        // 3. Sorting
        list = when (sort) {
            SortOption.NAME -> list.sortedBy { it.name.lowercase() }
            SortOption.JOINING_DATE -> list.sortedByDescending { it.joiningDate }
            SortOption.EXPIRY_DATE -> list.sortedBy { it.expiryDate }
            SortOption.RECENTLY_ADDED -> list.sortedByDescending { it.id }
        }

        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortOptionChange(option: SortOption) {
        _sortOption.value = option
    }

    fun onFilterOptionChange(option: FilterOption) {
        _filterOption.value = option
    }

    fun selectMember(memberId: Int?) {
        _selectedMemberId.value = memberId
    }

    val selectedMemberRenewals: StateFlow<List<RenewalEntity>> = _selectedMemberId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else Graph.renewalRepository.getRenewalsForMember(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedMemberAttendanceThisMonth: StateFlow<Int> = _selectedMemberId
        .flatMapLatest { id ->
            if (id == null) flowOf(0)
            else {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                
                attendanceRepository.getAttendanceForMember(id).map { list ->
                    list.count { it.date >= startOfMonth && it.status == "Present" }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun renewMembership(
        member: MemberEntity,
        durationMonths: Int,
        planName: String,
        amountPaid: Double,
        renewalDate: Long
    ) {
        viewModelScope.launch {
            // Early extension vs. late renewal
            val startTimestamp = maxOf(renewalDate, member.expiryDate)
            val cal = Calendar.getInstance().apply {
                timeInMillis = startTimestamp
            }
            cal.add(Calendar.MONTH, durationMonths)
            val newExpiry = cal.timeInMillis
            
            val updatedMember = member.copy(
                expiryDate = newExpiry,
                status = "Active",
                membershipFeePaid = amountPaid
            )
            memberRepository.updateMember(updatedMember)

            val renewal = RenewalEntity(
                memberId = member.id,
                planDuration = planName,
                amountPaid = amountPaid,
                renewalDate = renewalDate
            )
            Graph.renewalRepository.insertRenewal(renewal)
        }
    }

    fun updateMemberDetails(
        member: MemberEntity,
        name: String,
        phone: String,
        age: Int,
        gender: String,
        height: Float,
        weight: Float,
        status: String
    ) {
        viewModelScope.launch {
            val updated = member.copy(
                name = name,
                phone = phone,
                age = age,
                gender = gender,
                height = height,
                weight = weight,
                status = status
            )
            memberRepository.updateMember(updated)
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            memberRepository.deleteMember(member)
        }
    }
}
