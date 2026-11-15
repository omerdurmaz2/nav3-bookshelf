package dev.omerdurmaz.bookshelf

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Runs the real app on the JVM with Robolectric (phone-sized window by default).
@RunWith(RobolectricTestRunner::class)
class BookshelfAppTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun click(text: String) = compose.onNodeWithText(text).performClick()
    private fun shows(text: String) = compose.onNodeWithText(text).assertIsDisplayed()
    private fun hides(text: String) = compose.onNodeWithText(text).assertDoesNotExist()

    private fun pressBack() {
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.waitForIdle()
    }

    @Test
    fun ratingDialogReturnsTheStars() {
        click("Moby-Dick")
        shows("Not rated yet")

        click("Rate")
        click("4★")

        shows("Your rating: ★★★★")
        hides("How many stars?")
    }

    @Test
    fun everyDetailEntryGetsItsOwnViewModel() {
        click("Pride and Prejudice")
        shows("Written by Jane Austen in 1813")
        pressBack()

        // Without rememberViewModelStoreNavEntryDecorator, viewModel() would hand
        // back the first BookDetailViewModel and this would still say 1813.
        click("Emma")
        shows("Written by Jane Austen in 1815")
    }

    @Test
    fun tabsKeepTheirBackStacks() {
        click("Frankenstein")
        click("Authors")
        click("Charles Dickens")
        shows("Great Expectations (1861)")

        click("Books")
        shows("Written by Mary Shelley in 1818")

        click("Authors")
        shows("Great Expectations (1861)")
    }

    @Test
    fun backFromAnotherTabsRootGoesToTheStartTab() {
        click("Authors")
        click("Herman Melville")
        shows("Moby-Dick (1851)")

        pressBack()
        hides("Moby-Dick (1851)")
        pressBack()

        shows("Pride and Prejudice")
    }
}
