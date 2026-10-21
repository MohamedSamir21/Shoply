package com.shopping.shoply.common.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shopping.shoply.R
import com.shopping.shoply.categories.presentation.CategoriesDestination
import com.shopping.shoply.categories.presentation.CategoriesScreen
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.home.presentation.HomeDestination
import com.shopping.shoply.home.presentation.HomeScreen

/** The 5 bottom tabs. Each [route] is the root of that tab's own back stack. */
private enum class ShellTab(val route: String, @StringRes val label: Int, val icon: ImageVector) {
    HOME("tab_home", R.string.nav_home, ShoplyAppIcons.Home),
    CATEGORIES("tab_categories", R.string.nav_categories, ShoplyAppIcons.Grid),
    CART("tab_cart", R.string.nav_cart, ShoplyAppIcons.Bag),
    ORDERS("tab_orders", R.string.nav_orders, ShoplyAppIcons.Receipt),
    PROFILE("tab_profile", R.string.nav_profile, ShoplyAppIcons.User),
}

/** Screens not built yet, opened as "Coming soon" inside the current tab. The name travels in the route. */
private enum class Placeholder(@StringRes val title: Int) {
    SEARCH(R.string.screen_search),
    NOTIFICATIONS(R.string.screen_notifications),
    WISHLIST(R.string.screen_wishlist),
    ADDRESSES(R.string.screen_addresses),
    PRODUCTS(R.string.screen_products),
    ALL_STORES(R.string.screen_all_stores),
    STORE(R.string.screen_store),
    PRODUCT(R.string.screen_product),
    HELP(R.string.screen_help),
}

private const val HOME_ROUTE = "home_feed"
private const val CATEGORIES_ROUTE = "categories_grid"
private const val PLACEHOLDER_ARG = "placeholder"

// Each tab has its own placeholder route, so a "Coming soon" screen keeps its tab selected.
private fun placeholderRoute(tab: ShellTab) = "${tab.route}/coming_soon/{$PLACEHOLDER_ARG}"

/**
 * The signed-in app: a `Scaffold` with the bottom navigation (Home, Categories, Cart, Orders,
 * Profile; 80 dp tall, pill on the active tab) and its own inner `NavHost`.
 *
 * - Cart tab shows [MainShellViewModel.cartBadge]; null hides the badge.
 * - Home and Categories are built. The other tabs, and every destination not built yet, open
 *   [ComingSoonScreen]. [HomeDestination.AllCategories] and [HomeDestination.Cart] switch to their
 *   tab instead.
 * - Tapping the current tab again does nothing; switching tabs keeps each tab's back stack
 *   (`saveState` / `restoreState`).
 *
 * Replaces the empty `NavRoutes.Home` destination in [ShoplyNavGraph].
 */
@Composable
fun MainShell(
    modifier: Modifier = Modifier,
    viewModel: MainShellViewModel = hiltViewModel(),
) {
    val cartBadge by viewModel.cartBadge.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        modifier = modifier,
        // The app's outer Scaffold already pads for the system bars.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data -> Snackbar(data, shape = RoundedCornerShape(12.dp)) }
        },
        bottomBar = {
            ShoplyBottomBar(
                currentDestination = backStackEntry?.destination,
                cartBadge = cartBadge,
                onTabClick = navController::switchTab,
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ShellTab.HOME.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            // A built tab is a small graph, so screens opened from it keep that tab selected.
            navigation(startDestination = HOME_ROUTE, route = ShellTab.HOME.route) {
                composable(HOME_ROUTE) {
                    HomeScreen(
                        snackbarHostState = snackbarHostState,
                        onNavigate = { navController.openFromHome(it) },
                    )
                }
                comingSoon(ShellTab.HOME, navController)
            }
            navigation(startDestination = CATEGORIES_ROUTE, route = ShellTab.CATEGORIES.route) {
                composable(CATEGORIES_ROUTE) {
                    CategoriesScreen(onNavigate = { navController.openFromCategories(it) })
                }
                comingSoon(ShellTab.CATEGORIES, navController)
            }
            listOf(ShellTab.CART, ShellTab.ORDERS, ShellTab.PROFILE).forEach { tab ->
                composable(tab.route) { ComingSoonScreen(title = stringResource(tab.label), onBack = null) }
            }
        }
    }
}

private fun NavHostController.switchTab(tab: ShellTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** The "Coming soon" destination inside [tab]'s graph. */
private fun NavGraphBuilder.comingSoon(tab: ShellTab, navController: NavHostController) {
    composable(
        route = placeholderRoute(tab),
        arguments = listOf(navArgument(PLACEHOLDER_ARG) { type = NavType.StringType }),
    ) { entry ->
        val placeholder = Placeholder.valueOf(entry.arguments?.getString(PLACEHOLDER_ARG).orEmpty())
        ComingSoonScreen(
            title = stringResource(placeholder.title),
            onBack = { navController.popBackStack() },
        )
    }
}

private fun NavHostController.openPlaceholder(tab: ShellTab, placeholder: Placeholder) {
    navigate("${tab.route}/coming_soon/${placeholder.name}")
}

private fun NavHostController.openFromCategories(destination: CategoriesDestination) {
    val placeholder = when (destination) {
        CategoriesDestination.Search -> Placeholder.SEARCH
        CategoriesDestination.Addresses -> Placeholder.ADDRESSES
        is CategoriesDestination.ProductListing -> Placeholder.PRODUCTS
    }
    openPlaceholder(ShellTab.CATEGORIES, placeholder)
}

private fun NavHostController.openFromHome(destination: HomeDestination) {
    val placeholder = when (destination) {
        HomeDestination.AllCategories -> return switchTab(ShellTab.CATEGORIES)
        HomeDestination.Cart -> return switchTab(ShellTab.CART)
        is HomeDestination.Search -> Placeholder.SEARCH
        HomeDestination.Notifications -> Placeholder.NOTIFICATIONS
        HomeDestination.Wishlist -> Placeholder.WISHLIST
        HomeDestination.Addresses -> Placeholder.ADDRESSES
        is HomeDestination.ProductListing -> Placeholder.PRODUCTS
        HomeDestination.AllStores -> Placeholder.ALL_STORES
        is HomeDestination.Store -> Placeholder.STORE
        is HomeDestination.ProductDetails -> Placeholder.PRODUCT
        HomeDestination.Help -> Placeholder.HELP
    }
    openPlaceholder(ShellTab.HOME, placeholder)
}

/** The design's bottom navigation: 5 tabs, an orange pill behind the active icon, cart badge. */
@Composable
private fun ShoplyBottomBar(
    currentDestination: NavDestination?,
    cartBadge: String?,
    onTabClick: (ShellTab) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = ShoplyDimens.space1),
            ) {
                ShellTab.entries.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    BottomBarItem(
                        tab = tab,
                        selected = selected,
                        badge = if (tab == ShellTab.CART) cartBadge else null,
                        onClick = { if (!selected) onTabClick(tab) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    tab: ShellTab,
    selected: Boolean,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space1, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 32.dp)
                // background, not clip: a wide badge ("99+") may poke past the pill's edge.
                .background(if (selected) scheme.primaryContainer else scheme.surface, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = null,
                tint = if (selected) scheme.primary else scheme.onSurfaceVariant,
                modifier = Modifier.size(23.dp),
            )
            if (badge != null) {
                // The design's spot: the pill's top edge, starting just right of the icon's centre.
                CartBadge(
                    text = badge,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 31.dp),
                )
            }
        }
        Text(
            text = stringResource(tab.label),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
        )
    }
}

/** Orange count bubble with a ring in the bar's colour, so it reads over the pill. */
@Composable
private fun CartBadge(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .heightIn(min = 18.dp)
            .widthIn(min = 18.dp)
            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .padding(horizontal = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

/**
 * Placeholder for screens not built yet: [title] and "Coming soon". [onBack] null hides the back
 * button (tab roots have nowhere to go back to).
 */
@Composable
fun ComingSoonScreen(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ShoplyTopAppBar(title = title, onBack = onBack)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.common_coming_soon),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@PreviewLightDark
@Composable
fun ShoplyBottomBarPreview() {
    ShoplyTheme {
        ShoplyBottomBar(currentDestination = null, cartBadge = "3", onTabClick = {})
    }
}

@PreviewLightDark
@Composable
fun ComingSoonScreenPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ComingSoonScreen(title = "Wishlist", onBack = {})
        }
    }
}
