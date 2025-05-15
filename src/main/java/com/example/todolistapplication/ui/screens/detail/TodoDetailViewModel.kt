package com.example.todolistapplication.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistapplication.data.TodoRepository
import com.example.todolistapplication.data.model.Todo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class TodoDetailUiState {
    object Loading : TodoDetailUiState()
    data class Success(val todo: Todo) : TodoDetailUiState()
    data class Error(val message: String) : TodoDetailUiState()
}

class TodoDetailViewModel(
    private val todoRepository: TodoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<TodoDetailUiState>(TodoDetailUiState.Loading)
    val uiState: StateFlow<TodoDetailUiState> = _uiState.asStateFlow()

    fun loadTodo(id: Int) {
        viewModelScope.launch {
            try {
                val todo = todoRepository.getTodoById(id)
                _uiState.value = TodoDetailUiState.Success(todo)
            } catch (e: Exception) {
                _uiState.value = TodoDetailUiState.Error("Failed to load todo: ${e.message}")
            }
        }
    }

    fun updateTodo(title: String, description: String, dueDate: LocalDate?) {
        val currentState = _uiState.value
        if (currentState !is TodoDetailUiState.Success) return

        viewModelScope.launch {
            try {
                val updatedTodo = currentState.todo.copy(
                    title = title,
                    description = description,
                    dueDate = dueDate
                )
                todoRepository.updateTodo(updatedTodo)
                _uiState.value = TodoDetailUiState.Success(updatedTodo)
            } catch (e: Exception) {
                _uiState.value = TodoDetailUiState.Error("Failed to update todo: ${e.message}")
            }
        }
    }

    fun toggleTodoCompleted() {
        val currentState = _uiState.value
        if (currentState !is TodoDetailUiState.Success) return

        viewModelScope.launch {
            try {
                val updatedTodo = currentState.todo.copy(
                    isCompleted = !currentState.todo.isCompleted
                )
                todoRepository.updateTodo(updatedTodo)
                _uiState.value = TodoDetailUiState.Success(updatedTodo)
            } catch (e: Exception) {
                _uiState.value = TodoDetailUiState.Error("Failed to update todo status: ${e.message}")
            }
        }
    }

    fun deleteTodo() {
        val currentState = _uiState.value
        if (currentState !is TodoDetailUiState.Success) return

        viewModelScope.launch {
            try {
                todoRepository.deleteTodo(currentState.todo)
            } catch (e: Exception) {
                _uiState.value = TodoDetailUiState.Error("Failed to delete todo: ${e.message}")
            }
        }
    }
} 