package com.example.todolistapplication.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

@Composable
fun TodoNavigation(
    appContainer: AppContainer,
    navController: NavHostController = rememberNavController()
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
            val detailViewModel = remember { appContainer.createTodoDetailViewModel() }
            TodoDetailScreen(
                todoId = todoId,
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
} 