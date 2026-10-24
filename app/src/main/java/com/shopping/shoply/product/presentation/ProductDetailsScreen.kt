package com.shopping.shoply.product.presentation

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.common.presentation.components.QuantityStepper
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.components.StateMessage
import com.shopping.shoply.common.presentation.components.StateMessageTone
import com.shopping.shoply.common.presentation.components.TopBarIconButton
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.common.presentation.util.formatPrice
import com.shopping.shoply.home.domain.rules.discountPercent
import com.shopping.shoply.home.presentation.components.ProductImage
import com.shopping.shoply.home.presentation.components.Rating
import com.shopping.shoply.product.presentation.components.ColorSwatches
import com.shopping.shoply.product.presentation.components.ProductGallery
import com.shopping.shoply.product.presentation.components.SizeChips
import com.shopping.shoply.product.presentation.preview.ProductPreviewData
import kotlinx.coroutines.launch

/**
 * Product Details. Snackbars go to [snackbarHostState] (from MainShell, so they sit above the
 * bottom navigation); "Added to cart" has a View action → [ProductDetailsDestination.Cart].
 */
@Composable
fun ProductDetailsScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onNavigate: (ProductDetailsDestination) -> Unit,
    viewModel: ProductDetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navigate by rememberUpdatedState(onNavigate)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ProductDetailsEvent.ShowMessage -> launch {
                    // A new message replaces the one on screen instead of waiting behind it.
                    snackbarHostState.currentSnackbarData?.dismiss()
                    val toCart = event.message == ProductDetailsMessage.ADDED_TO_CART
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(event.message.textRes()),
                        actionLabel = if (toCart) context.getString(R.string.common_view) else null,
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed && toCart) navigate(ProductDetailsDestination.Cart)
                }
            }
        }
    }

    ProductDetailsContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = onBack,
        onShare = { product -> context.shareProduct(product) },
        onNavigate = onNavigate,
    )
}

/**
 * Product Details without a ViewModel (previews and UI tests use this), by [ProductDetailsState.body]:
 * - Loading → skeleton
 * - Offline → "You're Offline" + Retry → [ProductDetailsIntent.Retry]
 * - Content → gallery, info, colour and size pickers, description, delivery card, "You May Also
 *   Like" rail, and the quantity + Add to Cart bar at the bottom.
 */
@Composable
fun ProductDetailsContent(
    state: ProductDetailsState,
    onIntent: (ProductDetailsIntent) -> Unit,
    onBack: () -> Unit,
    onShare: (Product) -> Unit,
    onNavigate: (ProductDetailsDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val body = state.body) {
        ProductDetailsBody.Loading -> ProductDetailsSkeleton(onBack = onBack, modifier = modifier)
        ProductDetailsBody.Offline -> Column(modifier = modifier.fillMaxSize()) {
            TopBarIconButton(
                icon = ShoplyAppIcons.Back,
                label = stringResource(R.string.cd_go_back),
                onClick = onBack,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
            StateMessage(
                icon = ShoplyAppIcons.WifiOff,
                title = stringResource(R.string.common_offline_title),
                message = stringResource(R.string.common_offline_message),
                actionLabel = stringResource(R.string.common_offline_action),
                onAction = { onIntent(ProductDetailsIntent.Retry) },
                tone = StateMessageTone.Neutral,
            )
        }
        is ProductDetailsBody.Content -> Column(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                ProductGallery(
                    icon = body.product.icon,
                    isSaved = state.isSaved,
                    onBack = onBack,
                    onShare = { onShare(body.product) },
                    onToggleWishlist = { onIntent(ProductDetailsIntent.ToggleWishlist) },
                )
                ProductInfo(state = state, product = body.product, store = body.store, onIntent = onIntent, onNavigate = onNavigate)
                if (body.similar.isNotEmpty()) {
                    SimilarProducts(products = body.similar, onOpen = { onNavigate(ProductDetailsDestination.Product(it)) })
                }
            }
            AddToCartBar(
                quantity = state.quantity,
                adding = state.addingToCart,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
private fun ProductInfo(
    state: ProductDetailsState,
    product: Product,
    store: Store?,
    onIntent: (ProductDetailsIntent) -> Unit,
    onNavigate: (ProductDetailsDestination) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 18.dp)) {
        if (store != null) StoreLink(store = store, onClick = { onNavigate(ProductDetailsDestination.Store(store.id)) })
        Text(
            text = product.name,
            fontSize = 22.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.Bold,
            color = scheme.onSurface,
            modifier = Modifier.padding(top = 12.dp).semantics { heading() },
        )
        Row(
            modifier = Modifier.padding(top = ShoplyDimens.space2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Rating(rating = product.rating, reviewCount = null, fontSize = 13.5.sp)
            Text(
                text = pluralStringResource(R.plurals.product_reviews, product.reviewCount, product.reviewCount),
                fontSize = 13.sp,
                color = scheme.onSurfaceVariant,
            )
            Box(modifier = Modifier.size(3.dp).background(MaterialTheme.shoplyExtra.inkFaint, CircleShape))
            Text(
                text = stringResource(R.string.product_in_stock),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.shoplyExtra.onGreenSoft,
            )
        }
        PriceRow(product = product)
        Text(
            // Rounded up so 3 payments never fall short of the price.
            text = stringResource(R.string.product_installments, formatPrice((product.priceCents + 2) / 3)),
            fontSize = 12.5.sp,
            color = scheme.onSurfaceVariant,
            modifier = Modifier.padding(top = ShoplyDimens.space1),
        )

        if (product.colors.isNotEmpty()) {
            SectionLabel(
                text = stringResource(R.string.product_color),
                detail = product.colors.firstOrNull { it.name == state.color }?.name,
            )
            ColorSwatches(
                colors = product.colors,
                selected = state.color,
                onSelect = { onIntent(ProductDetailsIntent.SelectColor(it)) },
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        if (product.sizes.isNotEmpty()) {
            SectionLabel(text = stringResource(R.string.product_size), detail = null)
            SizeChips(
                sizes = product.sizes,
                selected = state.size,
                onSelect = { onIntent(ProductDetailsIntent.SelectSize(it)) },
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        if (product.description.isNotBlank()) Description(text = product.description)
        if (store != null) DeliveryCard(store = store)
    }
}

/** 26 dp store glyph, name and ›. */
@Composable
private fun StoreLink(store: Store, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductImage(icon = store.icon, cornerRadius = 8.dp, circleRatio = 0.8f, modifier = Modifier.size(26.dp))
        Text(text = store.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Icon(
            imageVector = ShoplyAppIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
    }
}

/** $89.00, the old price struck through, and a green "Save 18%" tag when discounted. */
@Composable
private fun PriceRow(product: Product) {
    Row(
        modifier = Modifier.padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                text = formatPrice(product.priceCents),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            product.oldPriceCents?.let { old ->
                Text(
                    text = formatPrice(old),
                    fontSize = 16.sp,
                    color = MaterialTheme.shoplyExtra.inkFaint,
                    textDecoration = TextDecoration.LineThrough,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
        }
        discountPercent(product.priceCents, product.oldPriceCents)?.let { percent ->
            Box(
                modifier = Modifier
                    .height(22.dp)
                    .background(MaterialTheme.shoplyExtra.greenSoft, RoundedCornerShape(7.dp))
                    .padding(horizontal = ShoplyDimens.space2),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.product_save_percent, percent),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.shoplyExtra.onGreenSoft,
                )
            }
        }
    }
}

/** "Color · Sand": bold label, muted detail. */
@Composable
private fun SectionLabel(text: String, detail: String?) {
    Row(modifier = Modifier.padding(top = 22.dp)) {
        Text(text = text, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        if (detail != null) {
            Text(text = " · $detail", fontSize = 14.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Two lines with Read more / Show less. */
@Composable
private fun Description(text: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Text(
        text = stringResource(R.string.product_description),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 24.dp),
    )
    Text(
        text = text,
        fontSize = 14.5.sp,
        lineHeight = 23.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = if (expanded) Int.MAX_VALUE else 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = ShoplyDimens.space2),
    )
    Text(
        text = stringResource(if (expanded) R.string.product_show_less else R.string.product_read_more),
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clickable(role = Role.Button) { expanded = !expanded }
            .padding(vertical = 6.dp),
    )
}

/** 🚚 "Delivery in 25–35 min" + "Free on orders over $35 · Easy 14-day returns". */
@Composable
private fun DeliveryCard(store: Store) {
    Row(
        modifier = Modifier
            .padding(top = 14.dp)
            .fillMaxWidth()
            .shoplyCard()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ShoplyAppIcons.Truck,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(21.dp),
            )
        }
        Column {
            Text(
                text = stringResource(R.string.product_delivery_title, store.etaMinutes.first, store.etaMinutes.last),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.product_delivery_note),
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

/** "You May Also Like": 144 dp cards with image, name and price. */
@Composable
private fun SimilarProducts(products: List<Product>, onOpen: (String) -> Unit) {
    Text(
        text = stringResource(R.string.product_similar),
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 28.dp, bottom = 14.dp)
            .semantics { heading() },
    )
    LazyRow(
        contentPadding = PaddingValues(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, bottom = ShoplyDimens.gutter),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        items(products, key = { it.id }) { product ->
            Column(
                modifier = Modifier
                    .width(144.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(role = Role.Button) { onOpen(product.id) },
            ) {
                ProductImage(icon = product.icon, modifier = Modifier.fillMaxWidth().height(140.dp))
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 10.dp, start = 2.dp, end = 2.dp),
                )
                Text(
                    text = formatPrice(product.priceCents),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 6.dp, start = 2.dp),
                )
            }
        }
    }
}

/** Pinned bottom: the stepper and Add to Cart (disabled while adding). */
@Composable
private fun AddToCartBar(quantity: Int, adding: Boolean, onIntent: (ProductDetailsIntent) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuantityStepper(
                    value = quantity,
                    onDecrease = { onIntent(ProductDetailsIntent.Decrease) },
                    onIncrease = { onIntent(ProductDetailsIntent.Increase) },
                )
                ShoplyButton(
                    label = stringResource(R.string.product_add_to_cart),
                    onClick = { onIntent(ProductDetailsIntent.AddToCart) },
                    enable = !adding,
                    leadingIcon = ShoplyAppIcons.Bag,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Image block, store line, title, rating, price and two picker rows. */
@Composable
private fun ProductDetailsSkeleton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        SkeletonPulse {
            Column {
                SkeletonBone(modifier = Modifier.fillMaxWidth().height(300.dp), shape = RoundedCornerShape(0.dp))
                Column(modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 18.dp)) {
                    SkeletonBone(modifier = Modifier.fillMaxWidth(0.4f).height(20.dp))
                    SkeletonBone(modifier = Modifier.padding(top = 14.dp).fillMaxWidth(0.85f).height(24.dp))
                    SkeletonBone(modifier = Modifier.padding(top = 10.dp).fillMaxWidth(0.5f).height(14.dp))
                    SkeletonBone(modifier = Modifier.padding(top = 18.dp).fillMaxWidth(0.35f).height(26.dp))
                    SkeletonBone(modifier = Modifier.padding(top = 26.dp).fillMaxWidth(0.6f).height(42.dp))
                    SkeletonBone(modifier = Modifier.padding(top = 22.dp).fillMaxWidth(0.7f).height(44.dp))
                }
            }
        }
        TopBarIconButton(
            icon = ShoplyAppIcons.Back,
            label = stringResource(R.string.cd_go_back),
            onClick = onBack,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

private fun Context.shareProduct(product: Product) {
    val text = getString(R.string.product_share_text, product.name, formatPrice(product.priceCents))
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(send, null))
}

private fun ProductDetailsMessage.textRes(): Int = when (this) {
    ProductDetailsMessage.ADDED_TO_CART -> R.string.snackbar_cart_added
    ProductDetailsMessage.CART_FAILED -> R.string.snackbar_cart_failed
    ProductDetailsMessage.SAVED -> R.string.snackbar_wishlist_saved
    ProductDetailsMessage.REMOVED -> R.string.snackbar_wishlist_removed
    ProductDetailsMessage.WISHLIST_FAILED -> R.string.snackbar_wishlist_failed
}

@Composable
private fun ProductDetailsContentPreview(state: ProductDetailsState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProductDetailsContent(state = state, onIntent = {}, onBack = {}, onShare = {}, onNavigate = {})
        }
    }
}

@PreviewLightDark
@Composable
fun ProductDetailsFilledPreview() = ProductDetailsContentPreview(
    ProductDetailsState(
        body = ProductDetailsBody.Content(
            product = ProductPreviewData.headphones,
            store = ProductPreviewData.store,
            similar = ProductPreviewData.similar,
        ),
        color = "Sand",
        size = "M",
        quantity = 2,
        isSaved = true,
    ),
)

@PreviewLightDark
@Composable
fun ProductDetailsPlainPreview() = ProductDetailsContentPreview(
    ProductDetailsState(
        body = ProductDetailsBody.Content(product = ProductPreviewData.mug, store = null, similar = emptyList()),
    ),
)

@PreviewLightDark
@Composable
fun ProductDetailsLoadingPreview() = ProductDetailsContentPreview(ProductDetailsState(body = ProductDetailsBody.Loading))

@PreviewLightDark
@Composable
fun ProductDetailsOfflinePreview() = ProductDetailsContentPreview(ProductDetailsState(body = ProductDetailsBody.Offline))
