package com.example.todolistapplication.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.todolistapplication.di.AppContainer
import com.example.todolistapplication.ui.screens.detail.TodoDetailScreen
import com.example.todolistapplication.ui.screens.list.TodoListScreen

sealed class Screen(val route: String) {
    object TodoList : Screen("todoList")
    object TodoDetail : Screen("todoDetail/{todoId}") {
        fun createRoute(todoId: Int) = "todoDetail/$todoId"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TodoNavigation(
    navController: NavHostController,
    appContainer: AppContainer
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TodoList.route
    ) {
        composable(Screen.TodoList.route) {
            TodoListScreen(
                viewModel = appContainer.todoListViewModel,
                onTodoClick = { todoId ->
                    navController.navigate(Screen.TodoDetail.createRoute(todoId))
                }
            )
        }

        composable(
            route = Screen.TodoDetail.route,
            arguments = listOf(
                navArgument("todoId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val todoId = backStackEntry.arguments?.getInt("todoId") ?: return@composable
            TodoDetailScreen(
                viewModel = appContainer.createTodoDetailViewModel(),
                todoId = todoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 