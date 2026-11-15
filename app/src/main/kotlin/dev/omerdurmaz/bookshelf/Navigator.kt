package dev.omerdurmaz.bookshelf

import androidx.navigation3.runtime.NavKey

/** Turns navigation events into back stack changes. There is no NavController. */
class Navigator(private val state: NavigationState) {

    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            // A tab: switch to it and keep whatever it had open.
            state.topLevelRoute = route
            return
        }
        val stack = state.currentStack
        // A new detail replaces the open one instead of piling up behind it.
        if (stack.last()::class == route::class) stack.removeLastOrNull()
        stack.add(route)
    }

    fun goBack() {
        val stack = state.currentStack
        if (stack.last() == state.topLevelRoute) {
            // Back from a tab's root goes to the start tab.
            state.topLevelRoute = state.startRoute
        } else {
            stack.removeLastOrNull()
        }
    }

    /** Shows [backStack] in the tab it starts with, e.g. a deep link's back stack. */
    fun open(backStack: List<NavKey>) {
        val root = backStack.first()
        val stack = state.backStacks.getValue(root)
        state.topLevelRoute = root
        stack.removeAll { it != root }
        stack.addAll(backStack.drop(1))
    }
}
