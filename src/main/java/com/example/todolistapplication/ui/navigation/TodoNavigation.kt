package com.example.todolistapplication.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.todolistapplication.ui.screens.detail.TodoDetailScreen
import com.example.todolistapplication.ui.screens.detail.TodoDetailViewModel
import com.example.todolistapplication.ui.screens.list.TodoListScreen
import com.example.todolistapplication.ui.screens.list.TodoListViewModel

private object Routes {
    const val TODO_LIST = "todoList"
    const val TODO_DETAIL = "todoDetail/{todoId}"
    
    fun todoDetail(todoId: Int) = "todoDetail/$todoId"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodoNavigation(
    todoListViewModel: TodoListViewModel,
    todoDetailViewModel: TodoDetailViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.TODO_LIST
    ) {
        composable(Routes.TODO_LIST) {
            TodoListScreen(
                viewModel = todoListViewModel,
                onTodoClick = { todoId ->
                    navController.navigate(Routes.todoDetail(todoId))
                }
            )
        }
        composable(
            route = Routes.TODO_DETAIL,
            arguments = listOf(
                navArgument("todoId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getInt("todoId") ?: return@composable
            TodoDetailScreen(
                todoId = todoId,
                viewModel = todoDetailViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
} 