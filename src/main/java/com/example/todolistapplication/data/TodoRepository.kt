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

    suspend fun refreshTodos() {
        try {
            val response = api.getTodos()
            if (response.isSuccessful) {
                val remoteTodos = response.body()?.map { it.toTodo() } ?: emptyList()
                // Clear existing todos before inserting new ones
                todoDao.deleteAllTodos()
                remoteTodos.forEach { todo ->
                    todoDao.insertTodo(todo)
                }
            } else {
                throw IOException("Failed to fetch todos: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            // If network call fails and we have no cached data, add sample data
            if (todoDao.getTodoCount() == 0) {
                val initialTodos = listOf(
                    Todo(
                        title = "Welcome to Todo App",
                        description = "This is your first todo item. You're offline now.",
                        isCompleted = false
                    )
                )
                initialTodos.forEach { todo ->
                    todoDao.insertTodo(todo)
                }
            }
            // Rethrow the exception to be handled by the ViewModel
            throw when (e) {
                is IOException -> IOException("Network error: ${e.message}")
                else -> Exception("Failed to refresh todos: ${e.message}")
            }
        }
    }

    private fun TodoResponse.toTodo(): Todo {
        return Todo(
            id = this.id,  // Preserve the API ID
            title = this.title,
            description = "Task ${this.id} from JSONPlaceholder API", // Create a meaningful description
            isCompleted = this.completed,
            dueDate = null // API doesn't provide due date
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
} 