package dev.omerdurmaz.bookshelf

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeepLinkLaunchTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private fun intent(uri: String) =
        Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .setData(Uri.parse(uri))

    private val bookThree get() = intent("nav3bookshelf://book/3")

    private fun shows(text: String) = compose.onNodeWithText(text).assertIsDisplayed()

    @Test
    fun coldStartOpensTheBookWithTheListBehindIt() {
        ActivityScenario.launch<MainActivity>(bookThree).use { app ->
            shows("Written by Mary Shelley in 1818")

            app.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            shows("Your books")
        }
    }

    @Test
    fun rotationDoesNotApplyTheLinkAgain() {
        ActivityScenario.launch<MainActivity>(bookThree).use { app ->
            shows("Written by Mary Shelley in 1818")
            app.onActivity { it.onBackPressedDispatcher.onBackPressed() }
            shows("Your books")

            app.recreate()

            shows("Your books")
        }
    }

    @Test
    fun linkWhileRunningSwitchesTab() {
        val app = Robolectric.buildActivity(MainActivity::class.java).setup()
        shows("Your books")

        app.newIntent(intent("nav3bookshelf://author/Jane%20Austen"))

        shows("Emma (1815)")
        app.close()
    }
}
