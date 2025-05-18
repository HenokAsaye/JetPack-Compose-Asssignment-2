package com.example.todolistapplication.data

import com.example.todolistapplication.data.api.NetworkModule
import com.example.todolistapplication.data.dao.TodoDao
import com.example.todolistapplication.data.model.Todo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.io.IOException

class TodoRepository(private val todoDao: TodoDao) {
    private val todoApi = NetworkModule.todoApi

    // Expose todos as a Flow to automatically update UI when data changes
    val todos: Flow<List<Todo>> = todoDao.getAllTodos()

    suspend fun fetchFromApi() {
        try {
            val apiTodos = todoApi.getTodos()
            val existingTodos = todoDao.getAllTodosOneShot()
            
            // Map of existing todos by their API ID
            val existingTodosMap = existingTodos
                .filter { it.apiId != null }
                .associateBy { it.apiId!! }

            // Convert API todos to local todos
            val todos = apiTodos.map { apiTodo ->
                // Check if we already have this todo
                val existingTodo = existingTodosMap[apiTodo.id]
                Todo(
                    id = existingTodo?.id ?: 0, // Keep existing ID if we have it
                    apiId = apiTodo.id, // Store API ID separately
                    userId = apiTodo.userId,
                    title = apiTodo.title,
                    // Preserve existing description and due date if we have them
                    description = existingTodo?.description ?: "",
                    isCompleted = apiTodo.completed,
                    dueDate = existingTodo?.dueDate
                )
            }

            // Get local-only todos (todos without apiId)
            val localOnlyTodos = existingTodos.filter { it.apiId == null }

            // Update database
            todoDao.deleteAllTodos()
            
            // Insert API todos
            todos.forEach { todo ->
                todoDao.insertTodo(todo)
            }
            
            // Restore local-only todos
            localOnlyTodos.forEach { todo ->
                todoDao.insertTodo(todo)
            }
        } catch (e: Exception) {
            val message = when (e) {
                is IOException -> "Network error: Check your internet connection"
                else -> "Failed to fetch todos: ${e.message}"
            }
            throw Exception(message)
        }
    }

    suspend fun refreshTodos() {
        try {
            fetchFromApi()
        } catch (e: Exception) {
            // If refresh fails, we'll still show cached data
            // Only throw if we have no cached data
            if (todoDao.getTodoCount() == 0) {
                throw e
            }
        }
    }

    suspend fun getTodoById(id: Int): Todo {
        return todoDao.getTodoById(id)
    }

    suspend fun insertTodo(title: String, description: String, dueDate: LocalDate? = null) {
        val todo = Todo(
            title = title,
            description = description,
            dueDate = dueDate
        )
        todoDao.insertTodo(todo)
    }

    suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }

    suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo)
    }

    suspend fun deleteAllTodos() {
        todoDao.deleteAllTodos()
    }
} 