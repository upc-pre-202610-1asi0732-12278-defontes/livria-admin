package com.example.adminlivria.bookcontext.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.adminlivria.bookcontext.domain.BookFilters
import com.example.adminlivria.bookcontext.domain.SortOption

@Composable
fun FiltersSheet(
    genres: List<String>,
    languages: List<String>,
    initial: BookFilters,
    onApply: (BookFilters) -> Unit,
    onClear: () -> Unit
) {
    var selectedGenre by remember(initial.genre) { mutableStateOf(initial.genre) }
    var selectedLanguage by remember(initial.language) { mutableStateOf(initial.language) }
    var sort by remember(initial.sort) { mutableStateOf(initial.sort) }

    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        ExposedDropdown(
            label = "Genre",
            options = genres,
            selected = selectedGenre,
            onSelected = { selectedGenre = it },
            onClear = { selectedGenre = null }
        )

        ExposedDropdown(
            label = "Language",
            options = languages,
            selected = selectedLanguage,
            onSelected = { selectedLanguage = it },
            onClear = { selectedLanguage = null }
        )

        Text("Sort by", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = sort == SortOption.TITLE_ASC, onClick = {
                sort = if (sort == SortOption.TITLE_ASC) SortOption.NONE else SortOption.TITLE_ASC
            }, label = { Text("A → Z") })

            FilterChip(selected = sort == SortOption.TITLE_DESC, onClick = {
                sort = if (sort == SortOption.TITLE_DESC) SortOption.NONE else SortOption.TITLE_DESC
            }, label = { Text("Z → A") })
        }

        var showInactive by remember(initial.showInactive) { mutableStateOf(initial.showInactive) }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Show Only Inactive Books", style = MaterialTheme.typography.bodyMedium)
            Switch(checked = showInactive, onCheckedChange = { showInactive = it })
        }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text("Clear") }
            Button(
                onClick = { onApply(BookFilters(selectedGenre, selectedLanguage, sort, showInactive)) },
                modifier = Modifier.weight(1f)
            ) { Text("Apply") }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdown(
    label: String,
    options: List<String>,
    selected: String?,
    onSelected: (String?) -> Unit,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Any") }, onClick = { onClear(); expanded = false })
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelected(opt); expanded = false })
            }
        }
    }
}
