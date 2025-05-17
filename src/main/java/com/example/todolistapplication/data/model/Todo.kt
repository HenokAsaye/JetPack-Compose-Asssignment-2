package com.example.todolistapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = 1, // Default userId set to 1
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val dueDate: LocalDate? = null
) {
    val isOverdue: Boolean
        get() = dueDate?.let { it.isBefore(LocalDate.now()) } ?: false

    val isToday: Boolean
        get() = dueDate?.equals(LocalDate.now()) ?: false

    val isTomorrow: Boolean
        get() = dueDate?.equals(LocalDate.now().plusDays(1)) ?: false

    val isThisWeek: Boolean
        get() = dueDate?.let {
            it.isAfter(LocalDate.now().plusDays(1)) &&
            it.isBefore(LocalDate.now().plusWeeks(1))
        } ?: false

    val formattedDueDate: String
        get() = when {
            dueDate == null -> ""
            isToday -> "Today, ${formatTime(dueDate)}"
            isTomorrow -> "Tomorrow, ${formatTime(dueDate)}"
            else -> dueDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
        }

    private fun formatTime(date: LocalDate): String {
        return "9:00 AM" // Placeholder since we don't store time
    }
} 