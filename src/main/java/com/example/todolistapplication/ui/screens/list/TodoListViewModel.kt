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
    data class Success(
        val todos: List<Todo>,
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null
    ) : TodoListUiState()
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
        // Try to fetch initial data from API
        refreshFromApi()
    }

    private fun refreshFromApi() {
        viewModelScope.launch {
            try {
                // If we have success state, set isRefreshing to true
                if (_uiState.value is TodoListUiState.Success) {
                    _uiState.value = (_uiState.value as TodoListUiState.Success).copy(isRefreshing = true)
                }
                
                repository.refreshTodos()
                
                // Clear any error message if refresh succeeds
                if (_uiState.value is TodoListUiState.Success) {
                    _uiState.value = (_uiState.value as TodoListUiState.Success).copy(
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                // If we have cached data, show error message but keep showing data
                if (_uiState.value is TodoListUiState.Success) {
                    _uiState.value = (_uiState.value as TodoListUiState.Success).copy(
                        isRefreshing = false,
                        errorMessage = e.message
                    )
                } else {
                    // If we have no data at all, show error state
                    _uiState.value = TodoListUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun loadTodos() {
        viewModelScope.launch {
            repository.todos
                .catch { e ->
                    _uiState.value = TodoListUiState.Error(e.message ?: "Unknown error")
                }
                .collect { todos ->
                    // Preserve isRefreshing and errorMessage when updating todos
                    val currentState = _uiState.value
                    _uiState.value = TodoListUiState.Success(
                        todos = todos,
                        isRefreshing = (currentState as? TodoListUiState.Success)?.isRefreshing ?: false,
                        errorMessage = (currentState as? TodoListUiState.Success)?.errorMessage
                    )
                }
        }
    }

    fun fetchFromApi() {
        viewModelScope.launch {
            try {
                if (_uiState.value is TodoListUiState.Success) {
                    _uiState.value = (_uiState.value as TodoListUiState.Success).copy(isRefreshing = true)
                } else {
                    _uiState.value = TodoListUiState.Loading
                }
                
                repository.fetchFromApi()
                
                if (_uiState.value is TodoListUiState.Success) {
                    _uiState.value = (_uiState.value as TodoListUiState.Success).copy(
                        isRefreshing = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                if (_uiState.value is TodoListUiState.Success) {
                    _uiState.value = (_uiState.value as TodoListUiState.Success).copy(
                        isRefreshing = false,
                        errorMessage = e.message
                    )
                } else {
                    _uiState.value = TodoListUiState.Error(e.message ?: "Failed to fetch from API")
                }
            }
        }
    }

    fun clearAllTodos() {
        viewModelScope.launch {
            try {
                repository.deleteAllTodos()
            } catch (e: Exception) {
                if (_uiState.value is TodoListUiState.Success) {
                    _uiState.value = (_uiState.value as TodoListUiState.Success).copy(
                        errorMessage = "Failed to clear todos: ${e.message}"
                    )
                } else {
                    _uiState.value = TodoListUiState.Error("Failed to clear todos: ${e.message}")
                }
            }
        }
    }

    fun addTodo(title: String, description: String, dueDate: LocalDate? = null) {
        viewModelScope.launch {
            try {
                repository.insertTodo(title, description, dueDate)
                hideAddDialog()
            } catch (e: Exception) {
                if (_uiState.value is TodoListUiState.Success) {
                    _uiState.value = (_uiState.value as TodoListUiState.Success).copy(
                        errorMessage = "Failed to add todo: ${e.message}"
                    )
                } else {
                    _uiState.value = TodoListUiState.Error("Failed to add todo: ${e.message}")
                }
            }
        }
    }

    fun toggleTodoCompleted(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.updateTodo(todo.copy(isCompleted = !todo.isCompleted))
            } catch (e: Exception) {
                if (_uiState.value is TodoListUiState.Success) {
                    _uiState.value = (_uiState.value as TodoListUiState.Success).copy(
                        errorMessage = "Failed to update todo: ${e.message}"
                    )
                } else {
                    _uiState.value = TodoListUiState.Error("Failed to update todo: ${e.message}")
                }
            }
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.deleteTodo(todo)
            } catch (e: Exception) {
                if (_uiState.value is TodoListUiState.Success) {
                    _uiState.value = (_uiState.value as TodoListUiState.Success).copy(
                        errorMessage = "Failed to delete todo: ${e.message}"
                    )
                } else {
                    _uiState.value = TodoListUiState.Error("Failed to delete todo: ${e.message}")
                }
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
        refreshFromApi()
    }

    fun dismissError() {
        if (_uiState.value is TodoListUiState.Success) {
            _uiState.value = (_uiState.value as TodoListUiState.Success).copy(errorMessage = null)
        }
    }
} 