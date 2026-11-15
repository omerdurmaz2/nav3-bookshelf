package dev.omerdurmaz.bookshelf

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Every screen is a key. @Serializable + NavKey let rememberNavBackStack save the
// back stack across rotation and process death. Arguments are just properties.

@Serializable
data object BookList : NavKey

@Serializable
data class BookDetail(val id: Int) : NavKey

@Serializable
data class RateBook(val bookId: Int) : NavKey

@Serializable
data object AuthorList : NavKey

@Serializable
data class AuthorDetail(val name: String) : NavKey

/** The tabs. Each one is the root of its own back stack. */
val TopLevelRoutes: Set<NavKey> = setOf(BookList, AuthorList)

/** RateBook sends the stars back to BookDetail under this key. */
fun ratingResultKey(bookId: Int) = "rating/$bookId"
