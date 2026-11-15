package dev.omerdurmaz.bookshelf

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Plain screens: they get data and callbacks and know nothing about navigation.

@Composable
fun BookListScreen(onBookClick: (Int) -> Unit) = Page {
    LazyColumn {
        item { Heading("Your books") }
        items(Library.books, key = { it.id }) { book ->
            ListItem(
                headlineContent = { Text(book.title) },
                supportingContent = { Text("${book.author}, ${book.year}") },
                modifier = Modifier.clickable { onBookClick(book.id) },
            )
        }
    }
}

@Composable
fun BookDetailScreen(viewModel: BookDetailViewModel, onRateClick: () -> Unit) = Page {
    val stars by viewModel.stars.collectAsStateWithLifecycle()
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(viewModel.book.title, style = MaterialTheme.typography.headlineMedium)
        Text("Written by ${viewModel.book.author} in ${viewModel.book.year}")
        Text(stars?.let { "Your rating: ${"★".repeat(it)}" } ?: "Not rated yet")
        Button(onClick = onRateClick) { Text("Rate") }
    }
}

@Composable
fun RatingDialog(onRated: (Int) -> Unit) {
    Surface(
        modifier = Modifier.widthIn(max = 360.dp),
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 6.dp,
    ) {
        Column(Modifier.padding(24.dp)) {
            Text("How many stars?", style = MaterialTheme.typography.titleLarge)
            Row {
                for (stars in 1..5) {
                    TextButton(onClick = { onRated(stars) }, Modifier.weight(1f)) {
                        Text("$stars★")
                    }
                }
            }
        }
    }
}

@Composable
fun AuthorListScreen(onAuthorClick: (String) -> Unit) = Page {
    LazyColumn {
        item { Heading("Authors A to Z") }
        items(Library.authors) { name ->
            ListItem(
                headlineContent = { Text(name) },
                modifier = Modifier.clickable { onAuthorClick(name) },
            )
        }
    }
}

@Composable
fun AuthorDetailScreen(name: String) = Page {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(name, style = MaterialTheme.typography.headlineMedium)
        Library.booksBy(name).forEach { Text("${it.title} (${it.year})") }
    }
}

@Composable
fun Placeholder(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text) }
}

/**
 * Entries are transparent by default. Without a background the screen underneath
 * shows through while a predictive back gesture moves this one aside.
 */
@Composable
private fun Page(content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize(), content = content)
}

@Composable
private fun Heading(text: String) {
    Text(text, Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
}
