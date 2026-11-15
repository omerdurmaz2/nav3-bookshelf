package dev.omerdurmaz.bookshelf

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Book(val id: Int, val title: String, val author: String, val year: Int)

/** The app's data. In memory on purpose: a real app would use Room or DataStore. */
object Library {
    val books = listOf(
        Book(1, "Pride and Prejudice", "Jane Austen", 1813),
        Book(2, "Emma", "Jane Austen", 1815),
        Book(3, "Frankenstein", "Mary Shelley", 1818),
        Book(4, "Moby-Dick", "Herman Melville", 1851),
        Book(5, "A Tale of Two Cities", "Charles Dickens", 1859),
        Book(6, "Great Expectations", "Charles Dickens", 1861),
        Book(7, "Crime and Punishment", "Fyodor Dostoevsky", 1866),
        Book(8, "The Time Machine", "H. G. Wells", 1895),
    )
    val authors = books.map { it.author }.distinct().sorted()

    fun book(id: Int) = books.find { it.id == id }
    fun booksBy(author: String) = books.filter { it.author == author }

    private val _ratings = MutableStateFlow(emptyMap<Int, Int>())
    val ratings: StateFlow<Map<Int, Int>> = _ratings.asStateFlow()

    fun rate(bookId: Int, stars: Int) = _ratings.update { it + (bookId to stars) }
}
