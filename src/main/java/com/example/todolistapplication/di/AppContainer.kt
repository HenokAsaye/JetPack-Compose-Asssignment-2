package com.example.todolistapplication.di

import android.content.Context
import androidx.room.Room
import com.example.todolistapplication.data.TodoDatabase
import com.example.todolistapplication.data.TodoRepository
import com.example.todolistapplication.data.api.NetworkModule
import com.example.todolistapplication.ui.screens.detail.TodoDetailViewModel
import com.example.todolistapplication.ui.screens.list.TodoListViewModel

class AppContainer(private val context: Context) {
    private val database = Room.databaseBuilder(
        context,
        TodoDatabase::class.java,
        "todo_database"
    )
    .fallbackToDestructiveMigration()
    .build()

    private val todoDao = database.todoDao()
    private val todoApi = NetworkModule.todoApi
    private val todoRepository = TodoRepository(todoDao)

    val todoListViewModel: TodoListViewModel by lazy {
        TodoListViewModel(todoRepository)
    }

    fun createTodoDetailViewModel(): TodoDetailViewModel {
        return TodoDetailViewModel(todoRepository)
    }
} 