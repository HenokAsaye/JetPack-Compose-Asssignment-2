package com.example.todolistapplication.ui.screens.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.todolistapplication.data.model.Todo
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState

import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    todoId: Int,
    viewModel: TodoDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val calendarState = rememberUseCaseState()
    var showEditDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(todoId) {
        viewModel.loadTodo(todoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todo Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, "Edit Todo")
                    }
                    IconButton(
                        onClick = {
                            viewModel.deleteTodo()
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is TodoDetailUiState.Success -> {
                    val todo = state.todo
                    DetailContent(
                        todo = todo,
                        onToggleCompleted = { viewModel.toggleTodoCompleted() },
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit Todo")
                    }

                    if (showEditDialog) {
                        EditTodoDialog(
                            todo = todo,
                            onDismiss = { showEditDialog = false },
                            onConfirm = { title, description, dueDate ->
                                viewModel.updateTodo(
                                    title = title,
                                    description = description,
                                    dueDate = dueDate
                                )
                                showEditDialog = false
                            }
                        )
                    }
                }
                is TodoDetailUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is TodoDetailUiState.Error -> {
                    Text(
                        text = "Error loading todo: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            monthSelection = true,
            yearSelection = true
        ),
        selection = CalendarSelection.Date { date ->
            when (val state = uiState) {
                is TodoDetailUiState.Success -> {
                    viewModel.updateTodo(
                        title = state.todo.title,
                        description = state.todo.description,
                        dueDate = date
                    )
                }
                else -> {}
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DetailContent(
    todo: Todo,
    onToggleCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Due Date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Due: ${todo.dueDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "No due date"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = todo.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (todo.isCompleted)
                    MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (todo.isCompleted) "Completed" else "In Progress",
                    style = MaterialTheme.typography.titleMedium
                )
                Checkbox(
                    checked = todo.isCompleted,
                    onCheckedChange = { onToggleCompleted() }
                )
            }
        }
    }
} 