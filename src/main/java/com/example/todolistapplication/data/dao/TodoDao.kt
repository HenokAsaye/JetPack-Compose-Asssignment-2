package com.example.todolistapplication.data.dao

import androidx.room.*
import com.example.todolistapplication.data.model.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, dueDate ASC")
    fun getAllTodos(): Flow<List<Todo>>

    @Query("SELECT * FROM todos ORDER BY CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, dueDate ASC")
    suspend fun getAllTodosOneShot(): List<Todo>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Int): Todo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: Todo): Long

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteTodoById(id: Int)

    @Query("DELETE FROM todos")
    suspend fun deleteAllTodos()

    @Query("SELECT COUNT(*) FROM todos")
    suspend fun getTodoCount(): Int
} 