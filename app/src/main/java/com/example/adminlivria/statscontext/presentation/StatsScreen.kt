package com.example.adminlivria.statscontext.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.adminlivria.common.components.BookCoverImage
import com.example.adminlivria.statscontext.domain.model.CapitalFlowData

val OrangePrimary = Color(0xFFF96D00)
val TextColorDark = Color(0xFF333333)

@Composable
fun StatsScreen(
    navController: NavController,
) {
    val viewModelFactory = StatsViewModelFactory(LocalContext.current)
    val viewModel: StatsViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Management and Statistics",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "Manage Livria's sales and profits",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                uiState.errorMessage != null -> ErrorMessageCard(uiState.errorMessage!!)
                else -> Unit
            }
        }

        item {
            SectionTitle(title = "TOP THREE BEST STOCK BOOKS")
            TopBooksRow(books = uiState.topSellingBooks)
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            SectionTitle(title = "INVENTORY DISTRIBUTION BY GENRES")
            GenreChart(genres = uiState.revenueByGenre)
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            SectionTitle(title = "INVENTORY MONETARY VALUE BY GENRE")
            InventoryValueChart(genres = uiState.inventoryValueByGenre)
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            color = OrangePrimary.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun TopBooksRow(books: List<BookStatsUi>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        books.take(3).forEach { book ->
            BookStatCard(book = book)
        }
    }
}

@Composable
fun BookStatCard(book: BookStatsUi) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            BookCoverImage(
                cover = book.imageUrl,
                contentDescription = book.title,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = book.title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${book.units} in stock",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary
            )
        }
    }
}

@Composable
fun GenreChart(genres: List<GenreStatsUi>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PieChartCanvas(genres = genres)

        Spacer(modifier = Modifier.height(32.dp))

        genres.forEach { genre ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(genre.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${genre.genre}: ${genre.percentageText}",
                    fontSize = 14.sp,
                    color = TextColorDark
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 12.sp, color = TextColorDark)
    }
}

@Composable
fun InventoryValueChart(genres: List<GenreStatsUi>) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            BarChartCanvas(genres = genres)
        }

        Spacer(modifier = Modifier.height(16.dp))

        genres.forEach { genre ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(genre.color)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${genre.genre}: ${genre.percentageText}",
                    fontSize = 14.sp,
                    color = TextColorDark
                )
            }
        }
    }
}

@Composable
fun ErrorMessageCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            text = "Error: $message",
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}