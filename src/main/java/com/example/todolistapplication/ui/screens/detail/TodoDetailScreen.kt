package com.example.todolistapplication.ui.screens.detail

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF001233),
            Color(0xFF0A2472)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Todo Details",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White
                        )
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
                            tint = Color(0xFFFF6B6B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(padding)
        ) {
            // Background decorative elements
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-50).dp, y = 100.dp)
                    .alpha(0.1f)
                    .blur(50.dp)
                    .background(
                        Color(0xFF4E7BFF),
                        shape = RoundedCornerShape(100.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = 250.dp, y = 300.dp)
                    .alpha(0.1f)
                    .blur(50.dp)
                    .background(
                        Color(0xFF4E7BFF),
                        shape = RoundedCornerShape(100.dp)
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            color = Color(0xFF0A2472).copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = todo.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Due date",
                        tint = Color(0xFF4E7BFF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Due: ${todo.dueDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "No due date"}",
                        fontSize = 16.sp,
                        color = Color(0xFF4E7BFF)
                    )
                }
            }
        }

        if (todo.description.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = Color(0xFF0A2472).copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Description",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = todo.description,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 24.sp
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = Color(0xFF0A2472).copy(alpha = 0.3f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Status",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (todo.isCompleted) "Completed" else "In Progress",
                        fontSize = 16.sp,
                        color = if (todo.isCompleted) Color(0xFF4CAF50) else Color(0xFFFFA726)
                    )
                }
                Switch(
                    checked = todo.isCompleted,
                    onCheckedChange = { onToggleCompleted() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF4CAF50),
                        checkedTrackColor = Color(0xFF4CAF50).copy(alpha = 0.5f),
                        uncheckedThumbColor = Color(0xFFFFA726),
                        uncheckedTrackColor = Color(0xFFFFA726).copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
} 