package com.example.todolistapplication.ui.screens.list

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolistapplication.data.model.Todo
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
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

    Scaffold(
        containerColor = Color(0xFF001233),
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Todo List",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "My Tasks",
                            color = Color.White,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Filter",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                            modifier = Modifier.background(Color(0xFF0A2472))
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "All Tasks",
                                        color = if (selectedFilter == TodoFilter.ALL) Color.White else Color.Gray
                                    )
                                },
                                onClick = { 
                                    selectedFilter = TodoFilter.ALL
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Overdue",
                                        color = if (selectedFilter == TodoFilter.OVERDUE) Color.White else Color.Gray
                                    )
                                },
                                onClick = { 
                                    selectedFilter = TodoFilter.OVERDUE
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Tomorrow",
                                        color = if (selectedFilter == TodoFilter.TOMORROW) Color.White else Color.Gray
                                    )
                                },
                                onClick = { 
                                    selectedFilter = TodoFilter.TOMORROW
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "This Week",
                                        color = if (selectedFilter == TodoFilter.THIS_WEEK) Color.White else Color.Gray
                                    )
                                },
                                onClick = { 
                                    selectedFilter = TodoFilter.THIS_WEEK
                                    showFilterMenu = false
                                }
                            )
                            Divider(color = Color.Gray.copy(alpha = 0.5f))
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Fetch Sample Tasks",
                                        color = Color.White
                                    )
                                },
                                onClick = { 
                                    viewModel.fetchFromApi()
                                    showFilterMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Clear All Tasks",
                                        color = Color(0xFFE91E63)
                                    )
                                },
                                onClick = { 
                                    viewModel.clearAllTodos()
                                    showFilterMenu = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A2472)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Todo")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is TodoListUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White
                    )
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
                            TodoFilter.TODAY -> todos.filter{it.isToday}
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
                AddTodoDialogContent(
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
private fun TodoList(
    todos: List<Todo>,
    onTodoClick: (Int) -> Unit,
    onToggleCompleted: (Todo) -> Unit,
    onDelete: (Todo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        // Overdue section
        if (todos.any { it.isOverdue }) {
            item {
                Text(
                    text = "Overdue",
                    color = Color(0xFFE91E63),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(
                items = todos.filter { it.isOverdue },
                key = { todo -> todo.id }
            ) { todo ->
                TodoItem(
                    todo = todo,
                    onTodoClick = { onTodoClick(todo.id) },
                    onToggleCompleted = { onToggleCompleted(todo) },
                    onDelete = { onDelete(todo) },
                    isOverdue = true
                )
            }
        }

        // Today section
        if (todos.any { it.isToday }) {
            item {
                Text(
                    text = "Today",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(
                items = todos.filter { it.isToday },
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

        // Tomorrow section
        if (todos.any { it.isTomorrow }) {
            item {
                Text(
                    text = "Tomorrow",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(
                items = todos.filter { it.isTomorrow },
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

        // This week section
        if (todos.any { it.isThisWeek }) {
            item {
                Text(
                    text = "This week",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(
                items = todos.filter { it.isThisWeek },
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

        // Other tasks section
        val otherTodos = todos.filter { todo ->
            !todo.isOverdue && !todo.isToday && !todo.isTomorrow && !todo.isThisWeek
        }
        if (otherTodos.isNotEmpty()) {
            item {
                Text(
                    text = "Other Tasks",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            items(
                items = otherTodos,
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
}

@Composable
private fun TodoItem(
    todo: Todo,
    onTodoClick: () -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    isOverdue: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onTodoClick),
        color = Color(0xFF0A2472)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = todo.isCompleted,
                    onCheckedChange = { onToggleCompleted() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = Color.White.copy(alpha = 0.7f)
                    )
                )
                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = todo.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    if (todo.dueDate != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = todo.formattedDueDate,
                            color = if (isOverdue) Color(0xFFE91E63) else Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete todo",
                    tint = Color(0xFFE91E63)
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No todos yet",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap + to add a new todo",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTodoDialogContent(
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
        title = { Text("Add Todo") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                OutlinedButton(
                    onClick = { scope.launch { calendarState.show() } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Calendar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        dueDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                            ?: "Set Due Date",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (title.isNotBlank()) {
                        onConfirm(title, description, dueDate)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            surface = Color(0xFF001233),
            onSurface = Color.White,
            primary = MaterialTheme.colorScheme.primary,
            onPrimary = Color.White
        )
    ) {
        CalendarDialog(
            state = calendarState,
            config = CalendarConfig(
                monthSelection = true,
                yearSelection = true,
                style = CalendarStyle.MONTH
            ),
            selection = CalendarSelection.Date { date ->
                dueDate = date
            }
        )
    }
} 