package dev.omerdurmaz.bookshelf

import android.content.Intent
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.BackStackMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.invoke
import androidx.navigation3.runtime.deeplink.withBackStack
import kotlinx.serialization.serializer

// nav3bookshelf://book/3 opens Frankenstein with the book list behind it,
// so Back goes to the list instead of leaving the app.
private val deepLinks: List<BackStackMatcher<NavKey, NavKey>> = listOf(
    UriDeepLinkMatcher(
        DeepLinkUri("nav3bookshelf://book/{id}"),
        serializer<BookDetail>(),
    ).withBackStack { listOf(BookList, it.key) },
    UriDeepLinkMatcher(
        DeepLinkUri("nav3bookshelf://author/{name}"),
        serializer<AuthorDetail>(),
    ).withBackStack { listOf(AuthorList, it.key) },
)

/** The back stack this intent links to, or null if it isn't one of our links. */
fun Intent.deepLinkBackStack(): List<NavKey>? {
    // A launch intent with no uri and no extras makes DeepLinkRequest(intent) throw.
    if (data == null) return null
    val request = DeepLinkRequest(this)
    val match = deepLinks.mapNotNull { it.match(request) }.maxOrNull() ?: return null
    // The link comes from outside the app, so only accept things that exist.
    return match.backStack.takeIf { it.last().exists() }
}

private fun NavKey.exists(): Boolean = when (this) {
    is BookDetail -> Library.book(id) != null
    is AuthorDetail -> name in Library.authors
    else -> true
}
