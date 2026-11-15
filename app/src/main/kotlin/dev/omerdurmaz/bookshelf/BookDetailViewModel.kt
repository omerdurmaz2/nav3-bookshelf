package dev.omerdurmaz.bookshelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * One instance per BookDetail entry on the back stack, cleared when the entry
 * is popped. The book id comes straight from the key.
 */
class BookDetailViewModel(private val bookId: Int) : ViewModel() {

    val book: Book = checkNotNull(Library.book(bookId)) { "No book $bookId" }

    val stars: StateFlow<Int?> = Library.ratings
        .map { it[bookId] }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Library.ratings.value[bookId])

    fun rate(stars: Int) = Library.rate(bookId, stars)
}
