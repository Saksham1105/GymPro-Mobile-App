package com.example.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.data.local.entity.AttendanceEntity
import com.example.data.local.entity.MemberEntity
import com.example.data.local.entity.MembershipPlanEntity
import com.example.data.local.entity.RenewalEntity
import com.example.di.Graph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class SettingsViewModel : ViewModel() {
    private val memberRepository = Graph.memberRepository
    private val attendanceRepository = Graph.attendanceRepository
    private val renewalRepository = Graph.renewalRepository
    private val planRepository = Graph.membershipPlanRepository

    fun exportBackup(context: Context, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    val root = JSONObject()

                    // 1. Members
                    val membersArr = JSONArray()
                    memberRepository.getAllMembersDirect().forEach {
                        val m = JSONObject()
                        if (it.id > 0) m.put("id", it.id)
                        m.put("name", it.name)
                        m.put("phone", it.phone)
                        m.put("age", it.age)
                        m.put("gender", it.gender)
                        m.put("height", it.height.toDouble())
                        m.put("weight", it.weight.toDouble())
                        m.put("joiningDate", it.joiningDate)
                        m.put("expiryDate", it.expiryDate)
                        m.put("medicalNotes", it.medicalNotes)
                        m.put("emergencyContact", it.emergencyContact)
                        m.put("status", it.status)
                        m.put("membershipFeePaid", it.membershipFeePaid)
                        membersArr.put(m)
                    }
                    root.put("members", membersArr)

                    // 2. Attendance
                    val attendanceArr = JSONArray()
                    attendanceRepository.getAllAttendanceDirect().forEach {
                        val a = JSONObject()
                        if (it.id > 0) a.put("id", it.id)
                        a.put("memberId", it.memberId)
                        a.put("date", it.date)
                        a.put("status", it.status)
                        attendanceArr.put(a)
                    }
                    root.put("attendance", attendanceArr)

                    // 3. Renewals
                    val renewalsArr = JSONArray()
                    renewalRepository.getAllRenewalsDirect().forEach {
                        val r = JSONObject()
                        if (it.id > 0) r.put("id", it.id)
                        r.put("memberId", it.memberId)
                        r.put("planDuration", it.planDuration)
                        r.put("amountPaid", it.amountPaid)
                        r.put("renewalDate", it.renewalDate)
                        renewalsArr.put(r)
                    }
                    root.put("renewals", renewalsArr)

                    // 4. Plans
                    val plansArr = JSONArray()
                    planRepository.getAllPlansDirect().forEach {
                        val p = JSONObject()
                        if (it.id > 0) p.put("id", it.id)
                        p.put("name", it.name)
                        p.put("price", it.price)
                        p.put("durationMonths", it.durationMonths)
                        plansArr.put(p)
                    }
                    root.put("plans", plansArr)

                    root.toString(2)
                }

                // Copy to Clipboard
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("GymPro Backup", json)
                clipboard.setPrimaryClip(clip)

                // Share Intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "GymPro Database Backup")
                    putExtra(Intent.EXTRA_TEXT, json)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(shareIntent, "Save Backup")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)

                onComplete("Backup copied to clipboard & share options opened!")
            } catch (e: Exception) {
                onComplete("Export failed: ${e.message}")
            }
        }
    }

    fun importBackup(jsonString: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val root = JSONObject(jsonString)

                    // Parse members
                    val membersList = mutableListOf<MemberEntity>()
                    if (root.has("members")) {
                        val arr = root.getJSONArray("members")
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            membersList.add(
                                MemberEntity(
                                    id = o.optInt("id", 0),
                                    name = o.optString("name", "Unknown Member"),
                                    phone = o.optString("phone", ""),
                                    age = o.optInt("age", 0),
                                    gender = o.optString("gender", "Male"),
                                    height = o.optDouble("height", 0.0).toFloat(),
                                    weight = o.optDouble("weight", 0.0).toFloat(),
                                    planId = null,
                                    joiningDate = o.optLong("joiningDate", System.currentTimeMillis()),
                                    expiryDate = o.optLong("expiryDate", System.currentTimeMillis()),
                                    medicalNotes = o.optString("medicalNotes", ""),
                                    emergencyContact = o.optString("emergencyContact", ""),
                                    status = o.optString("status", "Active"),
                                    membershipFeePaid = o.optDouble("membershipFeePaid", 0.0)
                                )
                            )
                        }
                    }

                    // Parse attendance
                    val attendanceList = mutableListOf<AttendanceEntity>()
                    if (root.has("attendance")) {
                        val arr = root.getJSONArray("attendance")
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            attendanceList.add(
                                AttendanceEntity(
                                    id = o.optInt("id", 0),
                                    memberId = o.optInt("memberId", 0),
                                    date = o.optLong("date", System.currentTimeMillis()),
                                    status = o.optString("status", "Present")
                                )
                            )
                        }
                    }

                    // Parse renewals
                    val renewalsList = mutableListOf<RenewalEntity>()
                    if (root.has("renewals")) {
                        val arr = root.getJSONArray("renewals")
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            renewalsList.add(
                                RenewalEntity(
                                    id = o.optInt("id", 0),
                                    memberId = o.optInt("memberId", 0),
                                    planDuration = o.optString("planDuration", "Monthly"),
                                    amountPaid = o.optDouble("amountPaid", 0.0),
                                    renewalDate = o.optLong("renewalDate", System.currentTimeMillis())
                                )
                            )
                        }
                    }

                    // Parse plans
                    val plansList = mutableListOf<MembershipPlanEntity>()
                    if (root.has("plans")) {
                        val arr = root.getJSONArray("plans")
                        for (i in 0 until arr.length()) {
                            val o = arr.getJSONObject(i)
                            plansList.add(
                                MembershipPlanEntity(
                                    id = o.optInt("id", 0),
                                    name = o.optString("name", "Custom"),
                                    price = o.optDouble("price", 0.0),
                                    durationMonths = o.optInt("durationMonths", 1),
                                    description = o.optString("description", ""),
                                    benefits = o.optString("benefits", "")
                                )
                            )
                        }
                    }

                    // Reset and import all in a single, safe transaction to prevent partial data loss or duplicate states
                    Graph.database.withTransaction {
                        memberRepository.deleteAll()
                        attendanceRepository.deleteAll()
                        renewalRepository.deleteAll()
                        planRepository.deleteAll()

                        if (membersList.isNotEmpty()) memberRepository.insertAll(membersList)
                        if (attendanceList.isNotEmpty()) attendanceRepository.insertAll(attendanceList)
                        if (renewalsList.isNotEmpty()) renewalRepository.insertAll(renewalsList)
                        if (plansList.isNotEmpty()) planRepository.insertAll(plansList)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                onError("Import failed: ${e.message}")
            }
        }
    }

    fun clearAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                memberRepository.deleteAll()
                attendanceRepository.deleteAll()
                renewalRepository.deleteAll()
                planRepository.deleteAll()
            }
            onComplete()
        }
    }
}
