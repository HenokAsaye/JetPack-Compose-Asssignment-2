package com.example.todolistapplication.ui.screens.list

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolistapplication.data.TodoRepository
import com.example.todolistapplication.data.model.Todo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class TodoListUiState {
    data object Loading : TodoListUiState()
    data class Success(val todos: List<Todo>) : TodoListUiState()
    data class Error(val message: String) : TodoListUiState()
}

class TodoListViewModel(
    private val repository: TodoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<TodoListUiState>(TodoListUiState.Loading)
    val uiState: StateFlow<TodoListUiState> = _uiState

    val showDialog = mutableStateOf(false)

    init {
        loadTodos()
    }

    fun loadTodos() {
        viewModelScope.launch {
            try {
                repository.refreshTodos()
            } catch (e: Exception) {
                // If refresh fails, we'll still show cached data
            }

            repository.todos
                .catch { e ->
                    _uiState.value = TodoListUiState.Error(e.message ?: "Unknown error")
                }
                .collect { todos ->
                    _uiState.value = TodoListUiState.Success(todos)
                }
        }
    }

    fun addTodo(title: String, description: String, dueDate: LocalDate? = null) {
        viewModelScope.launch {
            try {
                repository.insertTodo(title, description, dueDate)
                hideAddDialog()
            } catch (e: Exception) {
                _uiState.value = TodoListUiState.Error("Failed to add todo: ${e.message}")
            }
        }
    }

    fun toggleTodoCompleted(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
            } catch (e: Exception) {
                _uiState.value = TodoListUiState.Error("Failed to update todo: ${e.message}")
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.deleteTodo(todo)
            } catch (e: Exception) {
                _uiState.value = TodoListUiState.Error("Failed to delete todo: ${e.message}")
            }
        }
    }

    fun showAddDialog() {
        showDialog.value = true
    }

    fun hideAddDialog() {
        showDialog.value = false
    }

    fun retry() {
        _uiState.value = TodoListUiState.Loading
        loadTodos()
    }
} 