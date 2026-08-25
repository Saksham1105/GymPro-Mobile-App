package com.example.di

import android.content.Context
import com.example.data.local.database.AppDatabase
import com.example.repository.AttendanceRepository
import com.example.repository.MemberRepository
import com.example.repository.MembershipPlanRepository
import com.example.repository.RenewalRepository

object Graph {
    lateinit var database: AppDatabase
        private set

    val memberRepository by lazy {
        MemberRepository(database.memberDao())
    }
    
    val attendanceRepository by lazy {
        AttendanceRepository(database.attendanceDao())
    }
    
    val membershipPlanRepository by lazy {
        MembershipPlanRepository(database.membershipPlanDao())
    }

    val renewalRepository by lazy {
        RenewalRepository(database.renewalDao())
    }

    fun provide(context: Context) {
        database = AppDatabase.getDatabase(context)
    }
}
