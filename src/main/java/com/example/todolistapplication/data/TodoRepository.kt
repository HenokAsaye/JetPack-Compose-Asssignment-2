package com.example.todolistapplication.data

import com.example.todolistapplication.data.dao.TodoDao
import com.example.todolistapplication.data.model.Todo
import com.example.todolistapplication.data.api.RetrofitService
import com.example.todolistapplication.data.api.TodoResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.io.IOException

class TodoRepository(private val todoDao: TodoDao) {
    private val api = RetrofitService.todoApi

    // Expose todos as a Flow
    val todos: Flow<List<Todo>> = todoDao.getAllTodos()
        .catch { e ->
            throw e
        }
        .map { it }

    // This function now removes API tasks and keeps only local tasks
    suspend fun refreshTodos() {
        // Get current todos
        val currentTodos = todoDao.getAllTodosOneShot()
        
        // Keep only local todos (where description is different from title)
        val localTodos = currentTodos.filter { it.description != it.title }
        
        // Clear everything and restore only local todos
        todoDao.deleteAllTodos()
        localTodos.forEach { todo ->
            todoDao.insertTodo(todo)
        }
    }

    // Clear API tasks only when explicitly requested
    suspend fun clearApiTasks() {
        val currentTodos = todoDao.getAllTodosOneShot()
        val apiTodos = currentTodos.filter { it.description == it.title }
        apiTodos.forEach { todo ->
            todoDao.deleteTodo(todo)
        }
    }

    // Separate function for fetching from API
    suspend fun fetchFromApi() {
        try {
            // Get current local todos first
            val localTodos = todoDao.getAllTodosOneShot().filter { it.description != it.title }
            
            val response = api.getTodos()
            if (response.isSuccessful) {
                // Clear database
                todoDao.deleteAllTodos()
                
                // Restore local todos first
                localTodos.forEach { todo ->
                    todoDao.insertTodo(todo)
                }
                
                // Then add API todos
                val remoteTodos = response.body()?.map { it.toTodo() } ?: emptyList()
                remoteTodos.forEach { todo ->
                    todoDao.insertTodo(todo)
                }
            } else {
                throw IOException("Failed to fetch todos: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            throw when (e) {
                is IOException -> IOException("Network error: ${e.message}")
                else -> Exception("Failed to refresh todos: ${e.message}")
            }
        }
    }

    private fun TodoResponse.toTodo(): Todo {
        return Todo(
            id = 0, // Let Room generate new ID to avoid conflicts
            title = this.title,
            description = this.title, // This helps us identify API todos
            isCompleted = this.completed,
            dueDate = null
        )
    }

    suspend fun getTodoById(id: Int): Todo = todoDao.getTodoById(id)

    suspend fun insertTodo(title: String, description: String, dueDate: LocalDate? = null): Long {
        val todo = Todo(
            title = title,
            description = description,
            dueDate = dueDate
        )
        return todoDao.insertTodo(todo)
    }

    suspend fun updateTodo(todo: Todo) {
        todoDao.updateTodo(todo)
    }

    suspend fun deleteTodo(todo: Todo) {
        todoDao.deleteTodo(todo)
    }

    suspend fun deleteTodoById(id: Int) {
        todoDao.deleteTodoById(id)
    }

    suspend fun deleteAllTodos() {
        todoDao.deleteAllTodos()
    }
} 