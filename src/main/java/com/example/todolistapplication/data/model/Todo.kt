package com.example.todolistapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "todos")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int = 1, // Default userId set to 1
    val title: String,
    val description: String,
    val isCompleted: Boolean = false,
    val dueDate: LocalDate? = null
) 