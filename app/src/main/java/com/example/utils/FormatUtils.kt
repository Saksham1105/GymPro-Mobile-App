package com.example.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val defaultDateFormatter by lazy { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    private val dayOfWeekFormatter by lazy { SimpleDateFormat("EEE", Locale.getDefault()) }
    private val timeFormatter by lazy { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "N/A"
        return defaultDateFormatter.format(Date(timestamp))
    }

    fun formatDayOfWeek(timestamp: Long): String {
        if (timestamp <= 0) return ""
        return dayOfWeekFormatter.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        if (timestamp <= 0) return ""
        return timeFormatter.format(Date(timestamp))
    }

    fun formatCurrency(amount: Double): String {
        return "₹" + String.format(Locale.getDefault(), "%,.2f", amount)
    }

    fun formatCurrencyNoDecimals(amount: Double): String {
        return "₹" + String.format(Locale.getDefault(), "%,.0f", amount)
    }

    fun sanitizeAndFormatPhone(phone: String): String {
        val cleanPhone = phone.filter { it.isDigit() }
        val finalPhone = if (cleanPhone.length > 10 && cleanPhone.startsWith("91")) {
            cleanPhone.substring(cleanPhone.length - 10)
        } else {
            cleanPhone
        }
        return "+91 $finalPhone"
    }

    fun isValidPhone(phone: String): Boolean {
        val cleanPhone = phone.filter { it.isDigit() }
        val finalPhone = if (cleanPhone.length > 10 && cleanPhone.startsWith("91")) {
            cleanPhone.substring(cleanPhone.length - 10)
        } else {
            cleanPhone
        }
        return finalPhone.length == 10
    }
}
