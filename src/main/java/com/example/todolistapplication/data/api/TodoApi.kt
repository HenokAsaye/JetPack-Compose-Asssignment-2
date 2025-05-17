package com.example.todolistapplication.data.api

import retrofit2.http.GET
import retrofit2.Response

/**
 * Data class representing a Todo item from the JSONPlaceholder API
 */
data class TodoResponse(
    val id: Int,
    val userId: Int,
    val title: String,
    val completed: Boolean
)

/**
 * Retrofit API interface for interacting with the JSONPlaceholder API
 */
interface TodoApi {
    /**
     * Fetches all todos from the API
     * @return Response containing a list of TodoResponse objects
     */
    @GET("todos")
    suspend fun getTodos(): Response<List<TodoResponse>>
} 