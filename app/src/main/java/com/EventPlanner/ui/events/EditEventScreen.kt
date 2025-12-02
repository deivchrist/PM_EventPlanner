package com.EventPlanner.ui.events

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.EventPlanner.data.repository.EventRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    onNavigateBack: () -> Unit,
    onEventUpdated: () -> Unit,
    viewModel: EventViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoadingEvent by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val repository = EventRepository()
    val scope = rememberCoroutineScope()
    var eventUpdated by remember { mutableStateOf(false) }

    // Cargar el evento al iniciar
    LaunchedEffect(eventId) {
        scope.launch {
            isLoadingEvent = true
            loadError = null
            val event = repository.getEventById(eventId)
            if (event != null) {
                title = event.title
                date = event.date
                description = event.description
                isLoadingEvent = false
            } else {
                loadError = "No se pudo cargar el evento"
                isLoadingEvent = false
            }
        }
    }

    // Navegar de vuelta cuando se actualice el evento exitosamente
    LaunchedEffect(uiState.isUpdating, uiState.errorMessage) {
        if (!uiState.isUpdating && uiState.errorMessage == null && title.isNotBlank() && !eventUpdated) {
            // El evento se actualizó exitosamente
            eventUpdated = true
            onEventUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar Evento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoadingEvent -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            loadError != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = loadError ?: "Error desconocido",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Volver")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mostrar error si existe
                    uiState.errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isUpdating
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Fecha *") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej: 2024-12-25 o 25/12/2024") },
                        singleLine = true,
                        enabled = !uiState.isUpdating
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        maxLines = 5,
                        enabled = !uiState.isUpdating
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            viewModel.clearError()
                            viewModel.updateEvent(eventId, title, date, description)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isUpdating && title.isNotBlank() && date.isNotBlank()
                    ) {
                        if (uiState.isUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Actualizando...")
                        } else {
                            Text("Actualizar Evento")
                        }
                    }
                }
            }
        }
    }
}
