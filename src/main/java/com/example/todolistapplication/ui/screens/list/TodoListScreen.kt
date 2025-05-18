package com.example.todolistapplication.ui.screens.list

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
//import androidx.compose.material.icons.outlined.Circle
//import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

enum class TodoFilter {
    ALL,
    TODAY,
    TOMORROW,
    THIS_WEEK,
    OVERDUE
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoListViewModel,
    onTodoClick: (Int) -> Unit
) {
    var selectedFilter by remember { mutableStateOf(TodoFilter.ALL) }
    var showFilterMenu by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF1A1A1A),  // Dark gray
            Color(0xFF2D2D2D)   // Lighter gray
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = when(selectedFilter) {
                            TodoFilter.ALL -> "All Tasks"
                            TodoFilter.TODAY -> "Today"
                            TodoFilter.TOMORROW -> "Tomorrow"
                            TodoFilter.THIS_WEEK -> "This Week"
                            TodoFilter.OVERDUE -> "Overdue"
                        },
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Filter",
                                tint = Color(0xFF64B5F6)
                            )
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            modifier = Modifier.background(Color(0xFF2D2D2D))
                        ) {
                            FilterMenuItem("All Tasks", TodoFilter.ALL, selectedFilter) {
                                selectedFilter = TodoFilter.ALL
                                showFilterMenu = false
                            }
                            FilterMenuItem("Today", TodoFilter.TODAY, selectedFilter) {
                                selectedFilter = TodoFilter.TODAY
                                showFilterMenu = false
                            }
                            FilterMenuItem("Tomorrow", TodoFilter.TOMORROW, selectedFilter) {
                                selectedFilter = TodoFilter.TOMORROW
                                showFilterMenu = false
                            }
                            FilterMenuItem("This Week", TodoFilter.THIS_WEEK, selectedFilter) {
                                selectedFilter = TodoFilter.THIS_WEEK
                                showFilterMenu = false
                            }
                            FilterMenuItem("Overdue", TodoFilter.OVERDUE, selectedFilter) {
                                selectedFilter = TodoFilter.OVERDUE
                                showFilterMenu = false
                            }
                            Divider(color = Color.Gray.copy(alpha = 0.3f))
                            DropdownMenuItem(
                                text = { Text("Sample Tasks", color = Color(0xFF64B5F6)) },
                                onClick = { 
                                    viewModel.fetchFromApi()
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear All", color = Color(0xFFEF5350)) },
                                onClick = { 
                                    viewModel.clearAllTodos()
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = Color(0xFF64B5F6),
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
        ) {
            when (uiState) {
                is TodoListUiState.Loading -> {
                    LoadingState()
                }
                is TodoListUiState.Success -> {
                    val todos = (uiState as TodoListUiState.Success).todos
                    if (todos.isEmpty()) {
                        EmptyState()
                    } else {
                        val filteredTodos = when (selectedFilter) {
                            TodoFilter.ALL -> todos
                            TodoFilter.OVERDUE -> todos.filter { it.isOverdue }
                            TodoFilter.TOMORROW -> todos.filter { it.isTomorrow }
                            TodoFilter.THIS_WEEK -> todos.filter { it.isThisWeek }
                            TodoFilter.TODAY -> todos.filter { it.isToday }
                        }
                        TodoList(
                            todos = filteredTodos,
                            onTodoClick = onTodoClick,
                            onToggleCompleted = { viewModel.toggleTodoCompleted(it) },
                            onDelete = { viewModel.deleteTodo(it) }
                        )
                    }
                }
                is TodoListUiState.Error -> {
                    ErrorState(
                        message = (uiState as TodoListUiState.Error).message,
                        onRetry = { viewModel.loadTodos() }
                    )
                }
            }

            if (viewModel.showDialog.value) {
                AddEditTodoDialog(
                    onDismiss = { viewModel.hideAddDialog() },
                    onConfirm = { title, description, dueDate ->
                        viewModel.addTodo(title, description, dueDate)
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterMenuItem(
    text: String,
    filter: TodoFilter,
    selectedFilter: TodoFilter,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { 
            Text(
                text = text,
                color = if (selectedFilter == filter) Color(0xFF64B5F6) else Color.White
            )
        },
        onClick = onClick,
        leadingIcon = {
            if (selectedFilter == filter) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6)
                )
            }
        }
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF64B5F6),
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
private fun TodoList(
    todos: List<Todo>,
    onTodoClick: (Int) -> Unit,
    onToggleCompleted: (Todo) -> Unit,
    onDelete: (Todo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = todos,
            key = { todo -> todo.id }
        ) { todo ->
            TodoItem(
                todo = todo,
                onTodoClick = { onTodoClick(todo.id) },
                onToggleCompleted = { onToggleCompleted(todo) },
                onDelete = { onDelete(todo) }
            )
        }
    }
}

@Composable
private fun TodoItem(
    todo: Todo,
    onTodoClick: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTodoClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (todo.isCompleted) 
                Color(0xFF2D2D2D) 
            else 
                Color(0xFF424242)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = if (todo.isCompleted) Color(0xFF64B5F6) else Color.Transparent,
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = if (todo.isCompleted) Color(0xFF64B5F6) else Color.Gray,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (todo.isCompleted) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = todo.title,
                    color = if (todo.isCompleted) Color.Gray else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (todo.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.description,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (todo.dueDate != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Due date",
                            tint = if (todo.isOverdue) Color(0xFFEF5350) else Color(0xFF64B5F6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = todo.formattedDueDate,
                            color = if (todo.isOverdue) Color(0xFFEF5350) else Color(0xFF64B5F6),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1A1A))
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete task",
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF64B5F6),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "All Clear!",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to add your first task",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = Color(0xFFEF5350),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFEF5350),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF64B5F6)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Try Again")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditTodoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, LocalDate?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<LocalDate?>(null) }
    val calendarState = rememberUseCaseState()
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2D2D2D),
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color(0xFF64B5F6),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "New Task",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("What needs to be done?", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF64B5F6),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Add details", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF64B5F6),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                
                OutlinedButton(
                    onClick = { scope.launch { calendarState.show() } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF64B5F6)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = SolidColor(Color(0xFF64B5F6))
                    )
                ) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Calendar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        dueDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                            ?: "Set Due Date"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (title.isNotBlank()) {
                        onConfirm(title, description, dueDate)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF64B5F6),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text("Create Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )

    CalendarDialog(
        state = calendarState,
        config = CalendarConfig(
            monthSelection = true,
            yearSelection = true
        ),
        selection = CalendarSelection.Date { date ->
            dueDate = date
        }
    )
} 