package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "members",
    indices = [
        Index("name"),
        Index("phone"),
        Index("status")
    ]
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val age: Int,
    val gender: String,
    val height: Float,
    val weight: Float,
    val planId: Int?,
    val joiningDate: Long,
    val expiryDate: Long,
    val medicalNotes: String,
    val emergencyContact: String,
    val status: String, // Active, Expired, Paused
    val membershipFeePaid: Double = 0.0
)
