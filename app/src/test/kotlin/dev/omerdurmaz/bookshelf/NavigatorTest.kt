package dev.omerdurmaz.bookshelf

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

// The back stack is plain state, so the navigation rules run on the JVM: no device,
// no Compose, no Robolectric.
class NavigatorTest {

    private val state = NavigationState(
        startRoute = BookList,
        topLevelRoute = mutableStateOf(BookList),
        backStacks = TopLevelRoutes.associateWith { NavBackStack(it) },
    )
    private val navigator = Navigator(state)

    private fun stack(root: NavKey): List<NavKey> = state.backStacks.getValue(root)

    @Test
    fun `navigate pushes onto the current tab`() {
        navigator.navigate(BookDetail(3))
        navigator.navigate(RateBook(3))

        assertEquals(listOf(BookList, BookDetail(3), RateBook(3)), stack(BookList))
    }

    @Test
    fun `a new detail replaces the open one`() {
        navigator.navigate(BookDetail(1))
        navigator.navigate(BookDetail(2))

        assertEquals(listOf(BookList, BookDetail(2)), stack(BookList))
    }

    @Test
    fun `each tab keeps its own back stack`() {
        navigator.navigate(BookDetail(3))
        navigator.navigate(AuthorList)
        navigator.navigate(AuthorDetail("Mary Shelley"))
        navigator.navigate(BookList)

        assertEquals(listOf(BookList, BookDetail(3)), stack(BookList))
        assertEquals(listOf(AuthorList, AuthorDetail("Mary Shelley")), stack(AuthorList))
        assertEquals(listOf(BookList), state.stacksInUse)
    }

    @Test
    fun `back from another tab's root goes to the start tab`() {
        navigator.navigate(AuthorList)
        navigator.navigate(AuthorDetail("Emma"))
        assertEquals(listOf(BookList, AuthorList), state.stacksInUse)

        navigator.goBack()
        navigator.goBack()

        assertEquals(BookList, state.topLevelRoute)
        assertEquals(listOf(AuthorList), stack(AuthorList))
    }

    @Test
    fun `open shows a deep link back stack in its own tab`() {
        navigator.navigate(BookDetail(1))
        navigator.navigate(AuthorList)

        navigator.open(listOf(BookList, BookDetail(5)))

        assertEquals(BookList, state.topLevelRoute)
        assertEquals(listOf(BookList, BookDetail(5)), stack(BookList))
    }
}
