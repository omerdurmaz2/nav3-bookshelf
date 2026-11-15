package dev.omerdurmaz.bookshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey

class MainActivity : ComponentActivity() {

    /** A deep link that hasn't been applied to the back stack yet. */
    private var deepLink by mutableStateOf<List<NavKey>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // After rotation or process death the intent is the same old one, and the
        // restored back stack already has the link in it.
        if (savedInstanceState == null) deepLink = intent.deepLinkBackStack()
        addOnNewIntentListener { deepLink = it.deepLinkBackStack() }

        setContent {
            val dark = isSystemInDarkTheme()
            MaterialTheme(if (dark) darkColorScheme() else lightColorScheme()) {
                BookshelfApp(deepLink, onDeepLinkHandled = { deepLink = null })
            }
        }
    }
}
