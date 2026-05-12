package com.example.adminlivria.clientscontext.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

private val LivriaGreen = Color(0xFF2E7D32)
private val LivriaRed   = Color(0xFFC62828)

private fun isFree(subscription: String) =
    subscription.contains("free", ignoreCase = true)

private fun formatDate(isoDate: String): String {
    if (isoDate.isBlank()) return "—"
    return try {
        val datePart = isoDate.substringBefore("T")
        val parts    = datePart.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else isoDate
    } catch (e: Exception) { isoDate }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserClientListScreen(
    navController: NavController,
    viewModel: UserClientListViewModel = viewModel(factory = UserClientListViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Filter clients locally by display name or username
    val filteredClients = remember(uiState.clients, searchQuery) {
        if (searchQuery.isBlank()) uiState.clients
        else uiState.clients.filter { client ->
            client.display.contains(searchQuery, ignoreCase = true) ||
                    client.username.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Client Users", fontWeight = FontWeight.Bold, fontFamily = AlexandriaFontFamily)
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
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
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
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {

                        // ── Search bar ────────────────────────────────────
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            placeholder = {
                                Text(
                                    "Search by name or @username",
                                    fontFamily = AlexandriaFontFamily,
                                    color = Color.Gray
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = LivriaNavyBlue)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                                    }
                                }
                            },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = LivriaNavyBlue,
                                unfocusedBorderColor = Color.LightGray,
                                focusedContainerColor   = LivriaWhite,
                                unfocusedContainerColor = LivriaWhite
                            )
                        )

                        // ── Results count ─────────────────────────────────
                        if (searchQuery.isNotBlank()) {
                            Text(
                                text = "${filteredClients.size} result(s) for \"$searchQuery\"",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontFamily = AlexandriaFontFamily
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // ── List ──────────────────────────────────────────
                        if (filteredClients.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isBlank()) "No clients found."
                                    else "No clients match \"$searchQuery\".",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray,
                                    fontFamily = AlexandriaFontFamily
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp, end = 16.dp,
                                    top = 4.dp, bottom = 16.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredClients, key = { it.id }) { client ->
                                    UserClientCard(
                                        client = client,
                                        onTogglePayment = { viewModel.toggleHasPayed(client) },
                                        onChangePlan    = { newPlan -> viewModel.updateSubscription(client, newPlan) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserClientCard(
    client: UserClient,
    onTogglePayment: () -> Unit,
    onChangePlan: (String) -> Unit
) {
    var showPlanDialog by remember { mutableStateOf(false) }

    if (showPlanDialog) {
        PlanChangeDialog(
            currentPlan = client.subscription,
            onDismiss   = { showPlanDialog = false },
            onConfirm   = { newPlan -> onChangePlan(newPlan); showPlanDialog = false }
        )
    }

    val isPaidPlan = !isFree(client.subscription)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = LivriaWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(LivriaOrange.copy(alpha = 0.2f), shape = MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = LivriaOrange)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = client.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        fontFamily = AlexandriaFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Plan badge + date ─────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Badge(
                    containerColor = if (isFree(client.subscription)) LivriaLightGray else LivriaOrange
                ) {
                    Text(
                        text = client.subscription,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = AlexandriaFontFamily
                    )
                }
                if (isPaidPlan) {
                    Text(
                        text = "Since ${formatDate(client.planChangeDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontFamily = AlexandriaFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(12.dp))

            // ── Action buttons ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isPaidPlan) {
                    val paymentColor = if (client.hasPayed) LivriaGreen else LivriaRed
                    val paymentLabel = if (client.hasPayed) "Payment up to date" else "Payment pending"
                    OutlinedButton(
                        onClick = onTogglePayment,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = paymentColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, paymentColor)
                    ) {
                        Text(
                            text = paymentLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = AlexandriaFontFamily,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = { showPlanDialog = true },
                    modifier = if (isPaidPlan) Modifier.weight(1f) else Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LivriaNavyBlue)
                ) {
                    Text(
                        text = "Change Plan",
                        style = MaterialTheme.typography.labelSmall,
                        color = LivriaWhite,
                        fontFamily = AlexandriaFontFamily,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun PlanChangeDialog(
    currentPlan: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val plans  = listOf("communityplan", "freeplan")
    val labels = mapOf("communityplan" to "Community Plan", "freeplan" to "Free Plan")
    val initial = plans.firstOrNull { it.equals(currentPlan, ignoreCase = true) } ?: plans[1]
    var selectedPlan by remember(currentPlan) { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Change Plan", fontWeight = FontWeight.Bold, fontFamily = AlexandriaFontFamily, color = LivriaBlack)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Select a plan for this user:",
                    fontFamily = AlexandriaFontFamily,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                plans.forEach { plan ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPlan == plan,
                            onClick  = { selectedPlan = plan },
                            colors   = RadioButtonDefaults.colors(selectedColor = LivriaOrange)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = labels[plan] ?: plan,
                            fontFamily = AlexandriaFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LivriaBlack
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedPlan) },
                colors  = ButtonDefaults.buttonColors(containerColor = LivriaOrange)
            ) {
                Text("Confirm", color = LivriaWhite, fontFamily = AlexandriaFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray, fontFamily = AlexandriaFontFamily)
            }
        }
    )
}