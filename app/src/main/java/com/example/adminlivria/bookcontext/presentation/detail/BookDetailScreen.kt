package com.example.adminlivria.bookcontext.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adminlivria.common.components.BookCoverImage
import com.example.adminlivria.common.ui.theme.AsapCondensedFontFamily
import com.example.adminlivria.common.ui.theme.LivriaAmber
import com.example.adminlivria.common.ui.theme.LivriaBlack
import com.example.adminlivria.common.ui.theme.LivriaBlue
import com.example.adminlivria.common.ui.theme.LivriaOrange
import com.example.adminlivria.common.ui.theme.LivriaWhite

@Composable
fun BookDetailScreen(
    bookId: Int,
    viewModel: BookDetailViewModel = viewModel(
        factory = BookDetailViewModelFactory(LocalContext.current, bookId)
    )
) {
    val book by viewModel.book.collectAsState()

    if (book == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val b = book!!

    Scaffold { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = b.title.uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = AsapCondensedFontFamily,
                    color = LivriaAmber,
                    letterSpacing = 2.sp
                )
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                BookCoverImage(
                    cover = b.cover,
                    contentDescription = b.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(220.dp)
                        .aspectRatio(3f/4f)
                        .clip(RoundedCornerShape(16.dp))
                )
            }
            PriceRow("PURCHASE PRICE", b.purchasePrice)
            Divider()
            PriceRow("SALE PRICE", b.price)

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = LivriaWhite)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Synopsis", style = MaterialTheme.typography.titleMedium, color = LivriaBlack)
                    Spacer(Modifier.height(8.dp))
                    Text(b.description, style = MaterialTheme.typography.bodyMedium, color = LivriaBlack)
                }
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: Double) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = LivriaOrange, style = MaterialTheme.typography.labelLarge)
        Text("S/ ${"%.2f".format(value)}", color = LivriaBlue, style = MaterialTheme.typography.bodyLarge)
    }
}