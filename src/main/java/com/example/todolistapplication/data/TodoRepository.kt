package com.example.todolistapplication.data

import com.example.todolistapplication.data.dao.TodoDao
import com.example.todolistapplication.data.model.Todo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class TodoRepository(private val todoDao: TodoDao) {
    // Expose todos as a Flow
    val todos: Flow<List<Todo>> = todoDao.getAllTodos()
        .catch { e ->
            // Log error or handle it
            throw e
        }
        .map { it }

    suspend fun refreshTodos() {
        // In a real app, this would fetch from a remote source
        // For now, we'll just ensure the database has some initial data
        if (todoDao.getTodoCount() == 0) {
            val initialTodos = listOf(
                Todo(
                    title = "Welcome to Todo App",
                    description = "This is your first todo item",
                    isCompleted = false
                ),
                Todo(
                    title = "Swipe to delete",
                    description = "Try swiping a todo item to delete it",
                    isCompleted = false
                ),
                Todo(
                    title = "Tap to edit",
                    description = "Tap on a todo item to edit it",
                    isCompleted = false
                )
            )
            initialTodos.forEach { todo ->
                todoDao.insertTodo(todo)
            }
        }
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