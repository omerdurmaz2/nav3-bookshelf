package dev.omerdurmaz.bookshelf

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun BookshelfApp(deepLink: List<NavKey>?, onDeepLinkHandled: () -> Unit) {
    val navigationState = rememberNavigationState(BookList, TopLevelRoutes)
    val navigator = remember(navigationState) { Navigator(navigationState) }

    LaunchedEffect(deepLink) {
        if (deepLink != null) {
            navigator.open(deepLink)
            onDeepLinkHandled()
        }
    }

    // Each tab passes its own sceneKey. Without it the list-detail strategy would
    // put the Books entries and the Authors entries into one scaffold.
    val entryProvider = entryProvider {
        entry<BookList>(
            metadata = ListDetailSceneStrategy.listPane(
                sceneKey = BookList,
                detailPlaceholder = { Placeholder("Pick a book") },
            ),
        ) {
            BookListScreen(onBookClick = { navigator.navigate(BookDetail(it)) })
        }
        entry<BookDetail>(
            metadata = ListDetailSceneStrategy.detailPane(sceneKey = BookList),
        ) { key ->
            val viewModel = viewModel { BookDetailViewModel(key.id) }
            ResultEffect<Int>(resultKey = ratingResultKey(key.id)) { stars ->
                viewModel.rate(stars)
            }
            BookDetailScreen(
                viewModel = viewModel,
                onRateClick = { navigator.navigate(RateBook(key.id)) },
            )
        }
        entry<RateBook>(metadata = DialogSceneStrategy.dialog()) { key ->
            val resultBus = LocalResultEventBus.current
            RatingDialog(
                onRated = { stars ->
                    resultBus.sendResult(
                        resultKey = ratingResultKey(key.bookId),
                        result = stars,
                    )
                    navigator.goBack()
                },
            )
        }
        entry<AuthorList>(
            metadata = ListDetailSceneStrategy.listPane(
                sceneKey = AuthorList,
                detailPlaceholder = { Placeholder("Pick an author") },
            ),
        ) {
            AuthorListScreen(onAuthorClick = { navigator.navigate(AuthorDetail(it)) })
        }
        entry<AuthorDetail>(
            metadata = ListDetailSceneStrategy.detailPane(sceneKey = AuthorList),
        ) { key ->
            AuthorDetailScreen(key.name)
        }
    }

    // The default, PopUntilScaffoldValueChange, closes the app on Back while list and
    // detail sit side by side: list + placeholder doesn't count as a change.
    val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
        backNavigationBehavior = BackNavigationBehavior.PopUntilContentChange,
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            item(
                selected = navigationState.topLevelRoute == BookList,
                onClick = { navigator.navigate(BookList) },
                icon = { Icon(Icons.Filled.Star, contentDescription = null) },
                label = { Text("Books") },
            )
            item(
                selected = navigationState.topLevelRoute == AuthorList,
                onClick = { navigator.navigate(AuthorList) },
                icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                label = { Text("Authors") },
            )
        },
    ) {
        NavDisplay(
            entries = navigationState.toDecoratedEntries(entryProvider),
            onBack = navigator::goBack,
            modifier = Modifier.statusBarsPadding(),
            // Tried in order: the dialog first, then list-detail. Anything else
            // falls back to a single pane.
            sceneStrategies = listOf(DialogSceneStrategy(), listDetailStrategy),
        )
    }
}
