
package com.example.adminlivria.bookcontext.presentation.stock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adminlivria.common.components.BookCoverImage
import com.example.adminlivria.bookcontext.domain.Book
import com.example.adminlivria.common.ui.theme.*
import com.example.adminlivria.profilecontext.presentation.SettingsViewModel

@Composable
fun StockScreen(
    bookId: Int,
    settingsViewModel: SettingsViewModel,
    vm: StockViewModel = viewModel(factory = StockViewModelFactory(LocalContext.current, bookId))
) {
    val book by vm.book.collectAsState()
    val qty by vm.qty.collectAsState()
    val total by vm.totalToPay.collectAsState()

    if (book == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val b: Book = book!!

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            b.title.uppercase(),
            style = MaterialTheme.typography.titleLarge.copy(color = LivriaOrange, fontWeight = FontWeight.SemiBold)
        )


        BookCoverImage(
            cover = b.cover,
            contentDescription = b.title,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(240.dp)
                .aspectRatio(3f / 4f)
        )


        LabeledPriceRow(label = "PURCHASE PRICE", value = b.purchasePrice)
        LabeledPriceRow(label = "SALE PRICE", value = b.price)

        OutlinedCard {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CURRENT STOCK", color = LivriaSoftCyan)
                Text("${b.stock}")
            }
        }


        Card(
            colors = CardDefaults.cardColors(containerColor = LivriaYellowLight.copy(alpha = .25f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("QUANTITY")
                    QuantityPicker(qty = qty, onChange = vm::setQty)
                }


                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL TO PAY")
                    Text("S/ %.2f".format(total), fontWeight = FontWeight.SemiBold, color = LivriaBlue)
                }

                Button(
                    onClick = {

                        settingsViewModel.spend(total)

                        vm.confirm(onDone = {  })
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(containerColor = LivriaBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CONTINUE")
                }
            }
        }
    }
}

@Composable
private fun LabeledPriceRow(label: String, value: Double) {
    Divider()
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = LivriaOrange)
        Text("S/ %.2f".format(value))
    }
}

@Composable
private fun QuantityPicker(qty: Int, onChange: (Int) -> Unit) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledTonalButton(onClick = { onChange((qty - 1).coerceAtLeast(1)) }) { Text("-") }
        Spacer(Modifier.width(10.dp))
        Text("$qty")
        Spacer(Modifier.width(10.dp))
        FilledTonalButton(onClick = { onChange(qty + 1) }) { Text("+") }
    }
}
