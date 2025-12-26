package com.example.hronline.data

import android.content.Context
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AttendanceRecord(
    val id: String,
    val type: AttendanceType,
    val date: String,
    val time: String,
    val location: String,
    val timestamp: Long,
    val faceVerified: Boolean = true // Status verifikasi wajah
) {
    fun toJsonString(): String {
        return "$id|${type.name}|$date|$time|$location|$timestamp|$faceVerified"
    }
    
    companion object {
        fun fromJsonString(json: String): AttendanceRecord? {
            return try {
                val parts = json.split("|")
                when (parts.size) {
                    7 -> AttendanceRecord(
                        id = parts[0],
                        type = AttendanceType.valueOf(parts[1]),
                        date = parts[2],
                        time = parts[3],
                        location = parts[4],
                        timestamp = parts[5].toLong(),
                        faceVerified = parts[6].toBoolean()
                    )
                    6 -> AttendanceRecord( // Backward compatibility
                        id = parts[0],
                        type = AttendanceType.valueOf(parts[1]),
                        date = parts[2],
                        time = parts[3],
                        location = parts[4],
                        timestamp = parts[5].toLong(),
                        faceVerified = true
                    )
                    else -> null
                }
            } catch (@Suppress("UNUSED_PARAMETER") e: Exception) {
                null
            }
        }
    }
}

enum class AttendanceType {
    CHECK_IN,
    CHECK_OUT
}

object AttendanceStorage {
    private const val PREFS_NAME = "attendance_prefs"
    private const val KEY_ATTENDANCE_LIST = "attendance_list"
    
    fun saveAttendance(context: Context, record: AttendanceRecord) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = getAttendanceHistory(context).toMutableList()
        existing.add(record)

        val toSave = if (existing.size > 100) existing.takeLast(100) else existing
        val jsonList = toSave.joinToString(";;") { it.toJsonString() }
        prefs.edit {
            putString(KEY_ATTENDANCE_LIST, jsonList)
        }
    }
    
    fun getAttendanceHistory(context: Context): List<AttendanceRecord> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonList = prefs.getString(KEY_ATTENDANCE_LIST, "") ?: ""
        if (jsonList.isEmpty()) return emptyList()
        
        return jsonList.split(";;")
            .mapNotNull { AttendanceRecord.fromJsonString(it) }
            .sortedByDescending { it.timestamp }
    }
    
    fun getTodayAttendance(context: Context): List<AttendanceRecord> {
        val today = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(Date())
        return getAttendanceHistory(context).filter { it.date == today }
    }
    
    fun getLastCheckIn(context: Context): AttendanceRecord? {
        return getAttendanceHistory(context).firstOrNull { it.type == AttendanceType.CHECK_IN }
    }
    
    fun canCheckOut(context: Context): Boolean {
        val today = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID")).format(Date())
        val todayCheckIn = getAttendanceHistory(context)
            .firstOrNull { it.date == today && it.type == AttendanceType.CHECK_IN }
        val todayCheckOut = getAttendanceHistory(context)
            .firstOrNull { it.date == today && it.type == AttendanceType.CHECK_OUT }
        
        return todayCheckIn != null && todayCheckOut == null
    }
    
    // Get attendance by month and year
    fun getAttendanceByMonth(context: Context, month: Int, year: Int): List<AttendanceRecord> {
        val allRecords = getAttendanceHistory(context)
        return allRecords.filter { record ->
            try {
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
                val recordDate = dateFormat.parse(record.date)
                if (recordDate != null) {
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = recordDate
                    calendar.get(java.util.Calendar.MONTH) + 1 == month && calendar.get(java.util.Calendar.YEAR) == year
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Group records by date
    fun groupRecordsByDate(records: List<AttendanceRecord>): Map<String, Pair<AttendanceRecord?, AttendanceRecord?>> {
        val grouped = mutableMapOf<String, Pair<AttendanceRecord?, AttendanceRecord?>>()
        records.forEach { record ->
            val existing = grouped[record.date] ?: Pair(null, null)
            when (record.type) {
                AttendanceType.CHECK_IN -> grouped[record.date] = Pair(record, existing.second)
                AttendanceType.CHECK_OUT -> grouped[record.date] = Pair(existing.first, record)
            }
        }
        return grouped
    }
}

