@file:OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.adminlivria.bookcontext.presentation

import androidx.navigation.NavController
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adminlivria.R
import com.example.adminlivria.bookcontext.presentation.components.BookGridTile
import com.example.adminlivria.common.navigation.NavDestinations
import com.example.adminlivria.common.ui.theme.*

@Composable
fun BooksScreen(
    navController: NavController,
    viewModel: BooksManagementViewModel = viewModel(factory = BooksViewModelFactory(LocalContext.current))
) {
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val refreshFlow = remember(savedStateHandle) {
        savedStateHandle?.getStateFlow("refresh_books", false)
    }
    val shouldRefresh by (refreshFlow?.collectAsState(initial = false) ?: remember { mutableStateOf(false) })

    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            viewModel.refresh()
            savedStateHandle?.set("refresh_books", false)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val search by viewModel.search.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val books by viewModel.books.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val languages by viewModel.languages.collectAsState()
    val currentFilters by viewModel.filters.collectAsState()

    var showFilters by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LivriaWhite),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {

            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        "Livria Management",
                        textAlign = TextAlign.Center,
                        color = LivriaOrange,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold, fontSize = 20.sp
                        ),
                        modifier = Modifier.fillMaxWidth().padding(6.dp)
                    )
                    Text(
                        "Statistics",
                        textAlign = TextAlign.Center,
                        color = LivriaBlack,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 18.dp)
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        MiniStats("Total Books", stats.totalBooks.toString(), Modifier.weight(1f))
                        MiniStats("Total Genres", stats.totalGenres.toString(), Modifier.weight(1f))
                        MiniStats("Average Price", "S/ %.2f".format(stats.averagePrice), Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        MiniStats("Books in Stock", stats.booksInStock.toString(), Modifier.weight(1f))
                        MiniStats("Most Reviewed", stats.totalBooksRow2.toString(), Modifier.weight(1f))
                    }

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                        thickness = 2.dp, color = LivriaSoftCyan
                    )
                }
            }

            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = LivriaWhite,
                    shadowElevation = 4.dp
                ) {
                    SearchNFilterCardBooks(
                        search = search,
                        onSearchChange = { viewModel.onSearch(it) },
                        onOpenFilters = { showFilters = true }
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            val bookChunks = books.chunked(2)
            items(bookChunks) { rowBooks ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    for (book in rowBooks) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            BookGridTile(
                                book = book,
                                onView = { navController.navigate("${NavDestinations.BOOK_DETAIL_ROUTE}/${book.id}") },
                                onStock = { navController.navigate("${NavDestinations.INVENTORY_INDIVIDUAL_STOCK_ROUTE}/${book.id}") }
                            )
                        }
                    }
                    if (rowBooks.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }

    if (showFilters) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState
        ) {
            FiltersSheet(
                genres = genres,
                languages = languages,
                initial = currentFilters,
                onApply = {
                    viewModel.applyFilters(it)
                    showFilters = false
                },
                onClear = {
                    viewModel.clearFilters()
                    showFilters = false
                }
            )
        }
    }
}

@Composable
private fun MiniStats(title: String, value: String, modifier: Modifier = Modifier) {
    OutlinedCard(
        modifier = modifier.padding(6.dp).height(85.dp),
        colors = cardColors(containerColor = LivriaWhite),
        border = BorderStroke(1.dp, LivriaLightGray)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title, textAlign = TextAlign.Center, color = LivriaBlue,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold, fontSize = 11.sp
                )
            )
            Text(
                text = value, textAlign = TextAlign.Center, color = LivriaBlack,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun SearchNFilterCardBooks(
    search: String,
    onSearchChange: (String) -> Unit,
    onOpenFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        colors = cardColors(containerColor = LivriaWhite),
        elevation = cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "BOOK COLLECTION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = LivriaAmber, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenFilters, modifier = Modifier.size(24.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_filter),
                        contentDescription = null,
                        tint = LivriaOrange
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = search,
                onValueChange = onSearchChange,
                placeholder = { Text("Enter a title to search", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = null,
                        tint = LivriaBlue,
                        modifier = Modifier.size(20.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LivriaBlue,
                    unfocusedBorderColor = LivriaLightGray
                )
            )
        }
    }
}