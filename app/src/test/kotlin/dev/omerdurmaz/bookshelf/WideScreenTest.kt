package dev.omerdurmaz.bookshelf

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// A tablet-sized window, where ListDetailSceneStrategy shows two panes.
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w1280dp-h800dp-land")
class WideScreenTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun click(text: String) = compose.onNodeWithText(text).performClick()
    private fun shows(text: String) = compose.onNodeWithText(text).assertIsDisplayed()
    private fun hides(text: String) = compose.onNodeWithText(text).assertDoesNotExist()

    @Test
    fun listAndDetailSitSideBySide() {
        shows("Pick a book")

        click("Frankenstein")
        shows("Written by Mary Shelley in 1818")
        shows("Moby-Dick") // the list is still there

        click("Emma") // replaces the detail, the list stays
        shows("Written by Jane Austen in 1815")
        hides("Written by Mary Shelley in 1818")
    }

    @Test
    fun backClosesTheDetailInsteadOfTheApp() {
        click("Frankenstein")
        shows("Written by Mary Shelley in 1818")

        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }

        shows("Pick a book")
        hides("Written by Mary Shelley in 1818")
    }

    @Test
    fun theOtherTabsEntriesStayOutOfTheScaffold() {
        click("Frankenstein")
        click("Authors")

        // Same sceneKey for both tabs would pull Frankenstein in as the detail pane.
        shows("Pick an author")
        hides("Written by Mary Shelley in 1818")
    }
}
