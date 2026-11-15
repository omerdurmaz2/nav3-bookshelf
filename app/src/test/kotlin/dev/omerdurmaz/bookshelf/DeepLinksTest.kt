package dev.omerdurmaz.bookshelf

import android.content.Intent
import android.net.Uri
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Robolectric only because DeepLinkUri is android.net.Uri on Android.
@RunWith(RobolectricTestRunner::class)
class DeepLinksTest {

    private fun link(uri: String): List<NavKey>? =
        Intent(Intent.ACTION_VIEW, Uri.parse(uri)).deepLinkBackStack()

    @Test
    fun `book link builds the list behind the detail`() {
        assertEquals(listOf(BookList, BookDetail(3)), link("nav3bookshelf://book/3"))
    }

    @Test
    fun `author link decodes the name`() {
        assertEquals(
            listOf(AuthorList, AuthorDetail("Jane Austen")),
            link("nav3bookshelf://author/Jane%20Austen"),
        )
    }

    @Test
    fun `links to things that don't exist are ignored`() {
        assertNull(link("nav3bookshelf://book/99"))
        assertNull(link("nav3bookshelf://book/abc"))
        assertNull(link("nav3bookshelf://author/Nobody"))
        assertNull(link("nav3bookshelf://shelf/3"))
        assertNull(link("https://example.com/book/3"))
    }

    @Test
    fun `a launch intent without a uri doesn't crash`() {
        assertNull(Intent().deepLinkBackStack())
    }
}
