# Article notes

Notes for writing the Navigation 3 guide from this repo. Line numbers match the first commit on
`main`; paths are relative to `app/src/main/kotlin/dev/omerdurmaz/bookshelf/`
unless they start with `app/`. Every snippet is at most 90 columns wide, so it fits a gist.

Facts with sources (versions, API history, docs quotes):
`StudioWorkspace/medium/research_notes/Navigation 3/navigation3_facts.md` (outside this repo).

## Concept → code

| Article section | File:lines | What to quote |
|---|---|---|
| Setup / dependencies | `app/build.gradle.kts` | The four Nav3 artifacts + serialization plugin. Material adaptive needs `@OptIn(ExperimentalMaterial3AdaptiveApi::class)` even in 1.3.0 stable. |
| Typed keys, arguments | `Keys.kt:9-22` | `@Serializable data object/data class … : NavKey`. Args are properties (`BookDetail(val id: Int)`). |
| Back stack as state (one tab) | `NavigationState.kt:23-39` | `rememberNavBackStack(it)` per tab; `rememberSerializable(... MutableStateSerializer(NavKeySerializer()))` for the selected tab. |
| "Navigation is list editing" | `Navigator.kt:8-37` | `navigate` (switch tab / replace detail / push), `goBack`, `open`. No NavController. |
| Testing nav logic without a device | `app/src/test/.../NavigatorTest.kt:13-20` | `NavigationState(..., NavBackStack(it))` built directly; 5 plain JUnit tests. |
| entryProvider DSL | `BookshelfApp.kt:41-87` | `entry<BookList>(metadata = …) { }`, `entry<BookDetail> { key -> }`. |
| ViewModel per entry + args | `NavigationState.kt:64-82` (decorators), `BookshelfApp.kt:53`, `BookDetailViewModel.kt` | `viewModel { BookDetailViewModel(key.id) }`; decorator order: saveable state first, then ViewModel store. |
| Returning a result | `BookshelfApp.kt:54-56` (receive), `BookshelfApp.kt:62-73` (send), `Keys.kt:28` (explicit key), `NavigationState.kt:69,76` (hoisted bus) | `ResultEffect<Int>(resultKey = …)` → `viewModel.rate(stars)`; `resultBus.sendResult(resultKey = …, result = stars)` then `navigator.goBack()`. |
| Dialog destination | `BookshelfApp.kt:62`, `BookshelfApp.kt:117` | `DialogSceneStrategy.dialog()` metadata + `DialogSceneStrategy()` first in `sceneStrategies`. |
| Bottom nav, back stack per tab | `NavigationState.kt:41-82`, `BookshelfApp.kt:95-119` | `stacksInUse` ("exit through home"), `toDecoratedEntries` decorates hidden stacks too, `NavDisplay(entries = …)`. `NavigationSuiteScaffold` = bar on phones, rail on tablets. |
| Adaptive list-detail | `BookshelfApp.kt:42-47, 50-52, 89-93, 117` | `listPane(sceneKey, detailPlaceholder)`, `detailPane(sceneKey)`, `rememberListDetailSceneStrategy(backNavigationBehavior = PopUntilContentChange)`. |
| Deep links | `DeepLinks.kt:15-34`, `MainActivity.kt:25-28`, `app/src/main/AndroidManifest.xml` | `UriDeepLinkMatcher(DeepLinkUri(…), serializer<BookDetail>()).withBackStack { … }`, `mapNotNull { it.match(request) }.maxOrNull()`, `savedInstanceState == null` guard, `addOnNewIntentListener`, `singleTop`. |
| Applying a deep link to state | `BookshelfApp.kt:32-37`, `Navigator.kt:31-37` | `LaunchedEffect(deepLink) { navigator.open(it) }`. |
| Predictive back | `app/src/main/AndroidManifest.xml:5-8`, `Screens.kt:99-106` | Built into `NavDisplay`; manifest flag for Android 13-15; opaque `Page` surface. |
| Tests worth showing | `app/src/test/.../WideScreenTest.kt:15, 39-48`, `BookshelfAppTest.kt:41-50`, `DeepLinkLaunchTest.kt:42-52` | Robolectric `@Config(qualifiers = "w1280dp-h800dp-land")` for a tablet window on the JVM. |

## Traps (verified, with how)

1. **Back closes the app on tablets with Material list-detail.**
   `rememberListDetailSceneStrategy()` defaults to `BackNavigationBehavior.PopUntilScaffoldValueChange`.
   With list + detail visible, popping the detail gives list + placeholder, which is the same
   scaffold value, so `ThreePaneScaffoldScene.previousEntries` is empty, `NavDisplay` disables its
   back handler (`isBackEnabled = currentScene.previousEntries.isNotEmpty()`) and the system
   finishes the activity. On the start tab that means leaving the app with a detail open.
   - Seen on the Medium Tablet emulator (Android 15): cross-task back animation, then the previous
     app was on top. Fixed with `PopUntilContentChange` (`BookshelfApp.kt:89-93`).
   - Test `WideScreenTest.backClosesTheDetailInsteadOfTheApp`; switching back to the default makes
     it fail. Source: `ThreePaneScaffoldScene.kt` `getPreviousDestinationIndex` (adaptive-navigation3 1.3.0).
2. **Two tabs end up in one scaffold.** Material `ListDetailSceneStrategy.calculateScene` walks back
   over every entry that has pane metadata and keeps the ones with the same `sceneKey` (default
   `Unit`). With all tabs' entries flattened into one `NavDisplay`, the Authors tab picked up
   Frankenstein as its detail pane. Fix: `sceneKey = BookList` / `AuthorList`.
   Test `WideScreenTest.theOtherTabsEntriesStayOutOfTheScaffold` fails with a shared key.
3. **`DeepLinkRequest(intent)` throws** `IllegalArgumentException: DeepLinkRequest must have either
   a uri or extras.` for an intent with no data, type, action or extras (Robolectric's default
   launch intent, `Intent()`, some launchers). The docs snippet calls it unconditionally in
   `onCreate`. Fix: `if (data == null) return null` (`DeepLinks.kt:28-29`). Test
   `DeepLinksTest` "a launch intent without a uri doesn't crash"; removing the guard makes it throw.
4. **Transparent entries during predictive back.** Entries have no background. Mid-gesture on the
   Pixel 9 emulator the list's text showed through the detail. Fix: wrap each screen in a
   `Surface` (`Screens.kt:99-106`). Before/after screenshots:
   `StudioWorkspace/medium/research_notes/Navigation 3/predictive-back-transparent.png` and
   `predictive-back-fixed.png`.
5. **`viewModel()` without the decorator is Activity-scoped.** Then `BookDetail(2)` gets the
   `BookDetailViewModel` created for `BookDetail(1)` and shows the wrong book. Test
   `BookshelfAppTest.everyDetailEntryGetsItsOwnViewModel` fails when
   `rememberViewModelStoreNavEntryDecorator()` is removed.
6. **Deep link applied again after rotation.** The Activity's `intent` is still the link after a
   config change or process death, and the restored back stack already holds it. Without the
   `savedInstanceState == null` check, rotating after pressing Back reopened the book. Test
   `DeepLinkLaunchTest.rotationDoesNotApplyTheLinkAgain`.
7. **Passing `entryDecorators` replaces the default.** `NavDisplay`'s default is
   `listOf(rememberSaveableStateHolderNavEntryDecorator())`; your list replaces it, so add the
   saveable decorator yourself, first (source: `NavDisplay.kt`, navigation3-ui 1.2.1).
8. **Hidden tabs must still be decorated.** The recipe decorates every stack with
   `rememberDecoratedNavEntries` and only then picks the stacks in use. Decorators clear state on
   pop, so a stack that stops being decorated loses its ViewModels and saved state.
   (`NavigationState.kt:64-82`, comment in the official recipe.)
9. **Results are in memory.** `ResultEventBus` = one `Channel(BUFFERED)` per key, read with
   `receiveAsFlow()`: each result goes to one collector once, and nothing survives process death.
   `conflateAsState` is `collectAsState(default)` over the same channel, so its value goes back to
   the default when the entry leaves composition. This app forwards the stars to a ViewModel in
   `ResultEffect` (source: `ResultEventBus.kt`, navigation3-runtime 1.2.1).
10. **One decorator set per stack means one result bus per stack.** `rememberResultEventBusNavEntryDecorator()`
    creates its own bus. Hoist one with `rememberResultEventBus()` if a result may cross tabs
    (`NavigationState.kt:69`).
11. **Back on a tab's root.** `NavDisplay` only takes back when the current scene has previous
    entries. Here the start tab's stack is always below the current tab, so Back from the Authors
    root lands on Books (`Navigator.goBack`), and Back on the Books root leaves the app.
12. **Within one list-detail scene there is no predictive scene animation.** On the tablet the
    detail→placeholder change happens when you lift your finger; the slide/scale preview only
    plays when the scene changes (phone: detail scene → list scene). Observed, not a bug.
13. **Robolectric timing.** Pressing back right after a click, before the UI settles, hit the old
    scene's back state. Assert the new screen first (`shows(...)`), then press back.
14. **Version pages disagree.** The Get started page pins `lifecycle-viewmodel-navigation3
    2.12.0-alpha05` and `adaptive-navigation3 1.4.0-alpha03`; the migration guide and Maven have
    stable `2.11.0` and `1.3.0`. Pin stable.
15. **`singleTop` for links while the app is open.** Without it a new `MainActivity` would start
    on top of the old one. With it, `adb … am start -d …` says "intent has been delivered to
    currently running top-most instance" and `addOnNewIntentListener` handles it.

## Verified on

- `./gradlew clean assembleDebug testDebugUnitTest lint`: BUILD SUCCESSFUL, 19/19 tests, lint
  "No issues found".
- Pixel 9 emulator, Android 16 (API 36.1 image): list → detail → rate (result) → predictive back
  (opaque after fix), deep links cold and warm, back across tabs, rotation (portrait single pane →
  landscape list-detail, same back stack), invalid link ignored.
- Medium Tablet emulator, Android 15 (API 35, 2560x1600 @320dpi): navigation rail, list +
  placeholder, list + detail, dialog over list-detail, Authors tab placeholder, Back closes the
  detail (after fix 1).

## Versions

| | |
|---|---|
| androidx.navigation3 runtime/ui | 1.2.1 |
| androidx.lifecycle:lifecycle-viewmodel-navigation3 | 2.11.0 |
| androidx.compose.material3.adaptive:adaptive-navigation3 | 1.3.0 |
| Compose BOM | 2026.09.00 (material3 1.4.0, ui 1.12.1, navigation-suite 1.4.0) |
| activity-compose / lifecycle-*-compose | 1.13.0 / 2.11.0 |
| AGP / Gradle / Kotlin | 9.5.1 / 9.9.1 / 2.4.21 |
| Robolectric / androidx.test core | 4.18 / 1.7.0 |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 |
