package com.example.adminlivria.clientscontext.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.adminlivria.clientscontext.domain.model.UserClient
import com.example.adminlivria.common.ui.theme.AlexandriaFontFamily
import com.example.adminlivria.common.ui.theme.LivriaBlack
import com.example.adminlivria.common.ui.theme.LivriaLightGray
import com.example.adminlivria.common.ui.theme.LivriaNavyBlue
import com.example.adminlivria.common.ui.theme.LivriaOrange
import com.example.adminlivria.common.ui.theme.LivriaWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserClientListScreen(
    navController: NavController,
    viewModel: UserClientListViewModel = viewModel(factory = UserClientListViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Client Users", 
                        fontWeight = FontWeight.Bold,
                        fontFamily = AlexandriaFontFamily
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LivriaNavyBlue,
                    titleContentColor = LivriaWhite,
                    navigationIconContentColor = LivriaWhite
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = LivriaOrange
                    )
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = AlexandriaFontFamily
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadClients() },
                            colors = ButtonDefaults.buttonColors(containerColor = LivriaOrange)
                        ) {
                            Text("Retry", color = LivriaWhite, fontFamily = AlexandriaFontFamily)
                        }
                    }
                }
                uiState.clients.isEmpty() -> {
                    Text(
                        text = "No clients found.",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        fontFamily = AlexandriaFontFamily
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.clients) { client ->
                            UserClientCard(client = client)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserClientCard(client: UserClient) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = LivriaWhite)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LivriaOrange.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = LivriaOrange
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = client.display.ifEmpty { "No Name" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LivriaBlack,
                    fontFamily = AlexandriaFontFamily
                )
                Text(
                    text = "@${client.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    fontFamily = AlexandriaFontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = client.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    fontFamily = AlexandriaFontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Badge(containerColor = if (client.subscription.contains("free", ignoreCase = true)) LivriaLightGray else LivriaOrange) {
                    Text(text = client.subscription, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontFamily = AlexandriaFontFamily)
                }
            }
        }
    }
}
