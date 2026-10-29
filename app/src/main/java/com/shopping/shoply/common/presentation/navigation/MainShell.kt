package com.shopping.shoply.common.presentation.navigation

import android.net.Uri
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
import com.shopping.shoply.cart.presentation.CartDestination
import com.shopping.shoply.cart.presentation.CartScreen
import com.shopping.shoply.checkout.presentation.CheckoutDestination
import com.shopping.shoply.checkout.presentation.CheckoutScreen
import com.shopping.shoply.checkout.presentation.ORDER_ID_ARG
import com.shopping.shoply.checkout.presentation.OrderPlacedScreen
import com.shopping.shoply.checkout.presentation.PROMO_CODE_ARG
import com.shopping.shoply.categories.presentation.CategoriesDestination
import com.shopping.shoply.categories.presentation.CategoriesScreen
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.help.presentation.HelpScreen
import com.shopping.shoply.home.presentation.HomeDestination
import com.shopping.shoply.home.presentation.HomeScreen
import com.shopping.shoply.notifications.presentation.NotificationDestination
import com.shopping.shoply.notifications.presentation.NotificationsScreen
import com.shopping.shoply.product.presentation.PRODUCT_ID_ARG
import com.shopping.shoply.product.presentation.ProductDetailsDestination
import com.shopping.shoply.product.presentation.ProductDetailsScreen
import com.shopping.shoply.orders.presentation.ORDER_DETAILS_ID_ARG
import com.shopping.shoply.orders.presentation.OrderDetailsScreen
import com.shopping.shoply.orders.presentation.OrdersDestination
import com.shopping.shoply.orders.presentation.OrdersScreen
import com.shopping.shoply.products.presentation.LISTING_CATEGORY_ID_ARG
import com.shopping.shoply.products.presentation.ProductListingDestination
import com.shopping.shoply.products.presentation.ProductListingScreen
import com.shopping.shoply.search.presentation.SEARCH_OPEN_FILTERS_ARG
import com.shopping.shoply.search.presentation.SearchDestination
import com.shopping.shoply.search.presentation.SearchScreen
import com.shopping.shoply.store.presentation.STORE_ID_ARG
import com.shopping.shoply.store.presentation.StoreDestination
import com.shopping.shoply.store.presentation.StoreScreen
import com.shopping.shoply.profile.presentation.ProfileDestination
import com.shopping.shoply.profile.presentation.ProfileScreen
import com.shopping.shoply.wishlist.presentation.WishlistDestination
import com.shopping.shoply.wishlist.presentation.WishlistScreen

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
    ADDRESSES(R.string.screen_addresses),
    ALL_STORES(R.string.screen_all_stores),
    SUPPORT_CHAT(R.string.screen_support_chat),
    EDIT_PROFILE(R.string.screen_edit_profile),
    SETTINGS(R.string.screen_settings),
    FAVORITE_STORES(R.string.screen_favorite_stores),
    PAYMENT_METHODS(R.string.screen_payment_methods),
}

private const val HOME_ROUTE = "home_feed"
private const val CATEGORIES_ROUTE = "categories_grid"
private const val PROFILE_ROUTE = "profile_home"
private const val CART_ROUTE = "cart_items"
private const val ORDERS_ROUTE = "orders_list"
private const val CHECKOUT_ROUTE = "tab_cart/checkout?$PROMO_CODE_ARG={$PROMO_CODE_ARG}"
private const val ORDER_PLACED_ROUTE = "tab_cart/order_placed/{$ORDER_ID_ARG}"

// Checkout and its success screen are full-screen flows: the bottom navigation hides there.
private val ROUTES_WITHOUT_BOTTOM_BAR = setOf(CHECKOUT_ROUTE, ORDER_PLACED_ROUTE) + ShellTab.entries.map(::searchRoute)
private const val PLACEHOLDER_ARG = "placeholder"

// Each tab has its own placeholder and Help Center routes, so the screen keeps its tab selected
// (one route can't sit in two tab graphs).
private fun placeholderRoute(tab: ShellTab) = "${tab.route}/coming_soon/{$PLACEHOLDER_ARG}"
private fun helpRoute(tab: ShellTab) = "${tab.route}/help_center"
private fun wishlistRoute(tab: ShellTab) = "${tab.route}/wishlist"
private fun notificationsRoute(tab: ShellTab) = "${tab.route}/notifications"
private fun productRoute(tab: ShellTab) = "${tab.route}/product/{$PRODUCT_ID_ARG}"
private fun orderRoute(tab: ShellTab) = "${tab.route}/order/{$ORDER_DETAILS_ID_ARG}"
private fun storeRoute(tab: ShellTab) = "${tab.route}/store/{$STORE_ID_ARG}"
private fun listingRoute(tab: ShellTab) = "${tab.route}/products/{$LISTING_CATEGORY_ID_ARG}"
private fun searchRoute(tab: ShellTab) = "${tab.route}/search?$SEARCH_OPEN_FILTERS_ARG={$SEARCH_OPEN_FILTERS_ARG}"

/**
 * The signed-in app: a `Scaffold` with the bottom navigation (Home, Categories, Cart, Orders,
 * Profile; 80 dp tall, pill on the active tab) and its own inner `NavHost`.
 *
 * - Cart tab shows [MainShellViewModel.cartBadge]; null hides the badge.
 * - All 5 tabs, and the Help Center, Wishlist, Notifications, Product Details, Order Details and
 *   Checkout (inside the tabs that open them) are built. Every destination not built yet opens
 *   [ComingSoonScreen].
 *   [HomeDestination.AllCategories], [HomeDestination.Cart] and [ProfileDestination.Orders] switch
 *   to their tab instead.
 * - [onSignedOut] runs after Log Out is confirmed on Profile.
 * - Tapping the current tab again does nothing; switching tabs keeps each tab's back stack
 *   (`saveState` / `restoreState`).
 *
 * Replaces the empty `NavRoutes.Home` destination in [ShoplyNavGraph].
 */
@Composable
fun MainShell(
    onSignedOut: () -> Unit,
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
            if (backStackEntry?.destination?.route !in ROUTES_WITHOUT_BOTTOM_BAR) {
                ShoplyBottomBar(
                    currentDestination = backStackEntry?.destination,
                    cartBadge = cartBadge,
                    onTabClick = navController::switchTab,
                )
            }
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
                helpCenter(ShellTab.HOME, navController)
                wishlist(ShellTab.HOME, navController, snackbarHostState)
                notifications(ShellTab.HOME, navController, snackbarHostState)
                orderDetails(ShellTab.HOME, navController, snackbarHostState)
                productDetails(ShellTab.HOME, navController, snackbarHostState)
                store(ShellTab.HOME, navController, snackbarHostState)
                search(ShellTab.HOME, navController, snackbarHostState)
                productListing(ShellTab.HOME, navController, snackbarHostState)
                comingSoon(ShellTab.HOME, navController)
            }
            navigation(startDestination = CATEGORIES_ROUTE, route = ShellTab.CATEGORIES.route) {
                composable(CATEGORIES_ROUTE) {
                    CategoriesScreen(onNavigate = { navController.openFromCategories(it) })
                }
                wishlist(ShellTab.CATEGORIES, navController, snackbarHostState)
                productDetails(ShellTab.CATEGORIES, navController, snackbarHostState)
                store(ShellTab.CATEGORIES, navController, snackbarHostState)
                search(ShellTab.CATEGORIES, navController, snackbarHostState)
                productListing(ShellTab.CATEGORIES, navController, snackbarHostState)
                comingSoon(ShellTab.CATEGORIES, navController)
            }
            navigation(startDestination = CART_ROUTE, route = ShellTab.CART.route) {
                composable(CART_ROUTE) {
                    CartScreen(
                        snackbarHostState = snackbarHostState,
                        onNavigate = { navController.openFromCart(it) },
                    )
                }
                wishlist(ShellTab.CART, navController, snackbarHostState)
                productDetails(ShellTab.CART, navController, snackbarHostState)
                store(ShellTab.CART, navController, snackbarHostState)
                search(ShellTab.CART, navController, snackbarHostState)
                comingSoon(ShellTab.CART, navController)
                checkout(navController, snackbarHostState)
            }
            navigation(startDestination = PROFILE_ROUTE, route = ShellTab.PROFILE.route) {
                composable(PROFILE_ROUTE) {
                    ProfileScreen(
                        onNavigate = { navController.openFromProfile(it) },
                        onSignedOut = onSignedOut,
                    )
                }
                helpCenter(ShellTab.PROFILE, navController)
                wishlist(ShellTab.PROFILE, navController, snackbarHostState)
                notifications(ShellTab.PROFILE, navController, snackbarHostState)
                orderDetails(ShellTab.PROFILE, navController, snackbarHostState)
                productDetails(ShellTab.PROFILE, navController, snackbarHostState)
                store(ShellTab.PROFILE, navController, snackbarHostState)
                search(ShellTab.PROFILE, navController, snackbarHostState)
                comingSoon(ShellTab.PROFILE, navController)
            }
            navigation(startDestination = ORDERS_ROUTE, route = ShellTab.ORDERS.route) {
                composable(ORDERS_ROUTE) {
                    OrdersScreen(
                        snackbarHostState = snackbarHostState,
                        onNavigate = { navController.openFromOrders(ShellTab.ORDERS, it) },
                    )
                }
                orderDetails(ShellTab.ORDERS, navController, snackbarHostState)
                wishlist(ShellTab.ORDERS, navController, snackbarHostState)
                productDetails(ShellTab.ORDERS, navController, snackbarHostState)
                store(ShellTab.ORDERS, navController, snackbarHostState)
                search(ShellTab.ORDERS, navController, snackbarHostState)
                comingSoon(ShellTab.ORDERS, navController)
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

/** The Help Center inside [tab]'s graph; chat opens "Coming soon" in the same tab. */
private fun NavGraphBuilder.helpCenter(tab: ShellTab, navController: NavHostController) {
    composable(helpRoute(tab)) {
        HelpScreen(
            onBack = { navController.popBackStack() },
            onOpenChat = { navController.openPlaceholder(tab, Placeholder.SUPPORT_CHAT) },
        )
    }
}

/** The Wishlist inside [tab]'s graph; a product opens its page in the same tab. */
private fun NavGraphBuilder.wishlist(
    tab: ShellTab,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    composable(wishlistRoute(tab)) {
        WishlistScreen(
            snackbarHostState = snackbarHostState,
            onBack = { navController.popBackStack() },
            onNavigate = { destination ->
                when (destination) {
                    is WishlistDestination.Product -> navController.openProduct(tab, destination.id)
                    WishlistDestination.Cart -> navController.goToCart(tab)
                    WishlistDestination.ExploreProducts -> navController.goHome(tab)
                }
            },
        )
    }
}

/** Notifications inside [tab]'s graph; an order opens Order Details, payment methods "Coming soon". */
private fun NavGraphBuilder.notifications(
    tab: ShellTab,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    composable(notificationsRoute(tab)) {
        NotificationsScreen(
            snackbarHostState = snackbarHostState,
            onBack = { navController.popBackStack() },
            onNavigate = { destination ->
                when (destination) {
                    is NotificationDestination.Order -> navController.openOrder(tab, destination.id)
                    is NotificationDestination.Product -> navController.openProduct(tab, destination.id)
                    NotificationDestination.PaymentMethods -> navController.openPlaceholder(tab, Placeholder.PAYMENT_METHODS)
                    NotificationDestination.Home -> navController.goHome(tab)
                }
            },
        )
    }
}

/** Product Details inside [tab]'s graph; the store and similar products open in the same tab. */
private fun NavGraphBuilder.productDetails(
    tab: ShellTab,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    composable(
        route = productRoute(tab),
        arguments = listOf(navArgument(PRODUCT_ID_ARG) { type = NavType.StringType }),
    ) {
        ProductDetailsScreen(
            snackbarHostState = snackbarHostState,
            onBack = { navController.popBackStack() },
            onNavigate = { destination ->
                when (destination) {
                    is ProductDetailsDestination.Store -> navController.openStore(tab, destination.id)
                    is ProductDetailsDestination.Product -> navController.openProduct(tab, destination.id)
                    ProductDetailsDestination.Cart -> navController.goToCart(tab)
                }
            },
        )
    }
}

/**
 * Checkout and Order Placed inside the Cart tab. A placed order replaces Checkout, so back from
 * Order Placed returns to the (now empty) cart. Its buttons first clear it from the Cart tab, so
 * coming back to Cart later shows the cart, not the old success screen.
 */
private fun NavGraphBuilder.checkout(navController: NavHostController, snackbarHostState: SnackbarHostState) {
    composable(
        route = CHECKOUT_ROUTE,
        arguments = listOf(navArgument(PROMO_CODE_ARG) { type = NavType.StringType; nullable = true }),
    ) {
        CheckoutScreen(
            snackbarHostState = snackbarHostState,
            onNavigate = { destination ->
                when (destination) {
                    CheckoutDestination.Back -> navController.popBackStack()
                    is CheckoutDestination.OrderPlaced -> navController.navigate("tab_cart/order_placed/${destination.orderId}") {
                        popUpTo(CART_ROUTE)
                    }
                    CheckoutDestination.AddAddress -> navController.openPlaceholder(ShellTab.CART, Placeholder.ADDRESSES)
                    CheckoutDestination.AddCard -> navController.openPlaceholder(ShellTab.CART, Placeholder.PAYMENT_METHODS)
                }
            },
        )
    }
    composable(
        route = ORDER_PLACED_ROUTE,
        arguments = listOf(navArgument(ORDER_ID_ARG) { type = NavType.StringType }),
    ) { entry ->
        OrderPlacedScreen(
            orderId = entry.arguments?.getString(ORDER_ID_ARG).orEmpty(),
            onTrackOrder = {
                val orderId = entry.arguments?.getString(ORDER_ID_ARG).orEmpty()
                navController.popBackStack(CART_ROUTE, inclusive = false)
                navController.switchTab(ShellTab.ORDERS)
                navController.openOrder(ShellTab.ORDERS, orderId)
            },
            onContinueShopping = {
                navController.popBackStack(CART_ROUTE, inclusive = false)
                navController.switchTab(ShellTab.HOME)
            },
        )
    }
}

/** Order Details inside [tab]'s graph; its items open Product Details in the same tab. */
private fun NavGraphBuilder.orderDetails(
    tab: ShellTab,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    composable(
        route = orderRoute(tab),
        arguments = listOf(navArgument(ORDER_DETAILS_ID_ARG) { type = NavType.StringType }),
    ) {
        OrderDetailsScreen(
            snackbarHostState = snackbarHostState,
            onNavigate = { navController.openFromOrders(tab, it) },
        )
    }
}

/** Store inside [tab]'s graph; products open Product Details in the same tab. */
private fun NavGraphBuilder.store(
    tab: ShellTab,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    composable(
        route = storeRoute(tab),
        arguments = listOf(navArgument(STORE_ID_ARG) { type = NavType.StringType }),
    ) {
        StoreScreen(
            snackbarHostState = snackbarHostState,
            onNavigate = { destination ->
                when (destination) {
                    StoreDestination.Back -> navController.popBackStack()
                    is StoreDestination.Product -> navController.openProduct(tab, destination.productId)
                    StoreDestination.Search -> navController.openSearch(tab)
                    StoreDestination.Home -> navController.goHome(tab)
                    StoreDestination.Wishlist -> navController.navigate(wishlistRoute(tab))
                    StoreDestination.Cart -> navController.goToCart(tab)
                }
            },
        )
    }
}

/** Search inside [tab]'s graph (no bottom bar); results open Product Details and Store in the same tab. */
private fun NavGraphBuilder.search(
    tab: ShellTab,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    composable(
        route = searchRoute(tab),
        arguments = listOf(navArgument(SEARCH_OPEN_FILTERS_ARG) { type = NavType.BoolType; defaultValue = false }),
    ) {
        SearchScreen(
            snackbarHostState = snackbarHostState,
            onNavigate = { destination ->
                when (destination) {
                    SearchDestination.Back -> navController.popBackStack()
                    is SearchDestination.Product -> navController.openProduct(tab, destination.productId)
                    is SearchDestination.Store -> navController.openStore(tab, destination.storeId)
                    SearchDestination.Wishlist -> navController.navigate(wishlistRoute(tab))
                    SearchDestination.Cart -> navController.goToCart(tab)
                }
            },
        )
    }
}

/** Product Listing inside [tab]'s graph; products open Product Details in the same tab. */
private fun NavGraphBuilder.productListing(
    tab: ShellTab,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    composable(
        route = listingRoute(tab),
        arguments = listOf(navArgument(LISTING_CATEGORY_ID_ARG) { type = NavType.StringType }),
    ) {
        ProductListingScreen(
            snackbarHostState = snackbarHostState,
            onNavigate = { destination ->
                when (destination) {
                    ProductListingDestination.Back -> navController.popBackStack()
                    ProductListingDestination.Search -> navController.openSearch(tab)
                    is ProductListingDestination.Product -> navController.openProduct(tab, destination.productId)
                    ProductListingDestination.Categories ->
                        if (tab == ShellTab.CATEGORIES) navController.popBackStack(CATEGORIES_ROUTE, inclusive = false)
                        else navController.switchTab(ShellTab.CATEGORIES)
                    ProductListingDestination.Wishlist -> navController.navigate(wishlistRoute(tab))
                    ProductListingDestination.Cart -> navController.goToCart(tab)
                }
            },
        )
    }
}

private fun NavHostController.openListing(tab: ShellTab, categoryId: String) {
    navigate("${tab.route}/products/$categoryId")
}

private fun NavHostController.openSearch(tab: ShellTab, openFilters: Boolean = false) {
    navigate("${tab.route}/search?$SEARCH_OPEN_FILTERS_ARG=$openFilters")
}

private fun NavHostController.openStore(tab: ShellTab, storeId: String) {
    navigate("${tab.route}/store/$storeId")
}

private fun NavHostController.openOrder(tab: ShellTab, orderId: String) {
    navigate("${tab.route}/order/$orderId")
}

private fun NavHostController.openFromOrders(tab: ShellTab, destination: OrdersDestination) {
    when (destination) {
        is OrdersDestination.OrderDetails -> openOrder(tab, destination.id)
        is OrdersDestination.Product -> openProduct(tab, destination.id)
        OrdersDestination.Cart -> goToCart(tab)
        OrdersDestination.StartShopping -> goHome(tab)
        OrdersDestination.Back -> popBackStack()
    }
}

private fun NavHostController.openProduct(tab: ShellTab, productId: String) {
    navigate("${tab.route}/product/$productId")
}

// Inside Home, go back to the feed; switching to the tab you're in would restore this same back stack.
private fun NavHostController.goHome(from: ShellTab) {
    if (from == ShellTab.HOME) popBackStack(HOME_ROUTE, inclusive = false) else switchTab(ShellTab.HOME)
}

// Inside Cart, go back to the cart list, for the same reason as goHome.
private fun NavHostController.goToCart(from: ShellTab) {
    if (from == ShellTab.CART) popBackStack(CART_ROUTE, inclusive = false) else switchTab(ShellTab.CART)
}

private fun NavHostController.openPlaceholder(tab: ShellTab, placeholder: Placeholder) {
    navigate("${tab.route}/coming_soon/${placeholder.name}")
}

private fun NavHostController.openFromCart(destination: CartDestination) {
    when (destination) {
        is CartDestination.Product -> openProduct(ShellTab.CART, destination.id)
        CartDestination.Wishlist -> navigate(wishlistRoute(ShellTab.CART))
        is CartDestination.Checkout -> navigate(
            "tab_cart/checkout" + destination.promoCode?.let { "?$PROMO_CODE_ARG=${Uri.encode(it)}" }.orEmpty(),
        )
        CartDestination.StartShopping -> switchTab(ShellTab.HOME)
    }
}

private fun NavHostController.openFromCategories(destination: CategoriesDestination) {
    val placeholder = when (destination) {
        CategoriesDestination.Search -> return openSearch(ShellTab.CATEGORIES)
        CategoriesDestination.Addresses -> Placeholder.ADDRESSES
        is CategoriesDestination.ProductListing -> return openListing(ShellTab.CATEGORIES, destination.categoryId)
    }
    openPlaceholder(ShellTab.CATEGORIES, placeholder)
}

private fun NavHostController.openFromHome(destination: HomeDestination) {
    val placeholder = when (destination) {
        HomeDestination.AllCategories -> return switchTab(ShellTab.CATEGORIES)
        HomeDestination.Cart -> return switchTab(ShellTab.CART)
        is HomeDestination.Search -> return openSearch(ShellTab.HOME, destination.openFilters)
        HomeDestination.Notifications -> return navigate(notificationsRoute(ShellTab.HOME))
        HomeDestination.Wishlist -> return navigate(wishlistRoute(ShellTab.HOME))
        HomeDestination.Addresses -> Placeholder.ADDRESSES
        is HomeDestination.ProductListing -> return openListing(ShellTab.HOME, destination.categoryId)
        HomeDestination.AllStores -> Placeholder.ALL_STORES
        is HomeDestination.Store -> return openStore(ShellTab.HOME, destination.storeId)
        is HomeDestination.ProductDetails -> return openProduct(ShellTab.HOME, destination.productId)
        HomeDestination.Help -> return navigate(helpRoute(ShellTab.HOME))
    }
    openPlaceholder(ShellTab.HOME, placeholder)
}

private fun NavHostController.openFromProfile(destination: ProfileDestination) {
    val placeholder = when (destination) {
        ProfileDestination.Orders -> return switchTab(ShellTab.ORDERS)
        ProfileDestination.Help -> return navigate(helpRoute(ShellTab.PROFILE))
        ProfileDestination.EditProfile -> Placeholder.EDIT_PROFILE
        ProfileDestination.Settings -> Placeholder.SETTINGS
        ProfileDestination.Wishlist -> return navigate(wishlistRoute(ShellTab.PROFILE))
        ProfileDestination.FavoriteStores -> Placeholder.FAVORITE_STORES
        ProfileDestination.Addresses -> Placeholder.ADDRESSES
        ProfileDestination.Payments -> Placeholder.PAYMENT_METHODS
        ProfileDestination.Notifications -> return navigate(notificationsRoute(ShellTab.PROFILE))
    }
    openPlaceholder(ShellTab.PROFILE, placeholder)
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
