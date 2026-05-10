package com.example.adminlivria.bookcontext.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import com.example.adminlivria.common.components.BookCoverImage
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.adminlivria.bookcontext.domain.Book
import com.example.adminlivria.common.ui.theme.*

@Composable
fun BookGridTile(
    book: Book,
    onView: () -> Unit,
    onStock: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = LivriaWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            BookCoverImage(
                cover = book.cover,
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(12.dp))
            )

            Text(
                text = book.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = AlexandriaFontFamily,
                    fontWeight = FontWeight.SemiBold
                ),
                color = LivriaBlack,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = book.author,
                style = MaterialTheme.typography.bodySmall,
                color = LivriaBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "S/ ${"%.2f".format(book.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LivriaOrange
                )


                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onView,
                        colors = ButtonDefaults.buttonColors(containerColor = LivriaBlue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.width(70.dp).height(28.dp)
                    ) {
                        Text("VIEW", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onStock,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, LivriaBlue),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.width(70.dp).height(28.dp)
                    ) {
                        Text(
                            "STOCK",
                            color = LivriaBlue,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
