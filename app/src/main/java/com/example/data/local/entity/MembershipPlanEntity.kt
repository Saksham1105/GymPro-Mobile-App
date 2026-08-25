package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "membership_plans")
data class MembershipPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val durationMonths: Int,
    val price: Double,
    val description: String,
    val benefits: String
)
