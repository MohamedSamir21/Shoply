package com.shopping.shoply.catalog.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.components.ShoplyButtonVariant
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.rules.PriceRange
import com.shopping.shoply.catalog.domain.rules.ProductFilters
import com.shopping.shoply.catalog.domain.rules.ProductSort
import com.shopping.shoply.common.presentation.components.ShoplyChip
import com.shopping.shoply.common.presentation.theme.ShoplyAppIcons
import com.shopping.shoply.common.presentation.theme.ShoplyDimens

// Filters and Sort bottom sheets, shared by Search and Product Listing.

private val RATING_OPTIONS = listOf(null, 4.0, 4.5)
private const val MAX_CATEGORY_CHIPS = 5

/**
 * Filters sheet. Edits a draft copy of [filters]; nothing changes until "Show N Results", where
 * N is [countFor] the draft. Reset clears the draft. [categories] empty hides the Category row.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    filters: ProductFilters,
    categories: List<Category>,
    countFor: (ProductFilters) -> Int,
    onApply: (ProductFilters) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(filters) { mutableStateOf(filters) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ShoplyDimens.gutter),
            ) {
                SheetTitle(stringResource(R.string.filters_title))
                SheetLabel(stringResource(R.string.filters_price))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriceRange.entries.forEach { range ->
                        ShoplyChip(
                            label = stringResource(range.labelRes()),
                            selected = draft.price == range,
                            onClick = { draft = draft.copy(price = range) },
                        )
                    }
                }
                SheetLabel(stringResource(R.string.filters_rating))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RATING_OPTIONS.forEach { rating ->
                        ShoplyChip(
                            label = if (rating == null) stringResource(R.string.filters_any) else stringResource(R.string.filters_rating_up, rating),
                            selected = draft.minRating == rating,
                            icon = if (rating != null) ShoplyAppIcons.Star else null,
                            onClick = { draft = draft.copy(minRating = rating) },
                        )
                    }
                }
                if (categories.isNotEmpty()) {
                    SheetLabel(stringResource(R.string.filters_category))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.take(MAX_CATEGORY_CHIPS).forEach { category ->
                            val on = category.id in draft.categoryIds
                            ShoplyChip(
                                label = category.name,
                                selected = on,
                                onClick = {
                                    draft = draft.copy(categoryIds = if (on) draft.categoryIds - category.id else draft.categoryIds + category.id)
                                },
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(top = 22.dp), color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.filters_free_delivery), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = stringResource(R.string.filters_free_delivery_hint),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    Switch(
                        checked = draft.freeDeliveryOnly,
                        onCheckedChange = { draft = draft.copy(freeDeliveryOnly = it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                    )
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = ShoplyDimens.gutter, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space3),
            ) {
                ShoplyButton(
                    label = stringResource(R.string.filters_reset),
                    onClick = { draft = ProductFilters() },
                    variant = ShoplyButtonVariant.Outline,
                    modifier = Modifier.width(112.dp),
                )
                val count = countFor(draft)
                ShoplyButton(
                    label = pluralStringResource(R.plurals.filters_show_results, count, count),
                    onClick = { onApply(draft) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** Sort sheet: one radio row per [ProductSort]; picking one applies it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortSheet(sort: ProductSort, onSelect: (ProductSort) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.navigationBarsPadding().padding(bottom = 12.dp)) {
            SheetTitle(stringResource(R.string.sort_title), modifier = Modifier.padding(horizontal = ShoplyDimens.gutter))
            Column(modifier = Modifier.selectableGroup()) {
                ProductSort.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .selectable(selected = option == sort, role = Role.RadioButton, onClick = { onSelect(option) })
                            .padding(horizontal = ShoplyDimens.gutter),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(option.labelRes()),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        RadioButton(
                            selected = option == sort,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(bottom = 6.dp).semantics { heading() },
    )
}

@Composable
private fun SheetLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 18.dp, bottom = 10.dp),
    )
}

/** "Sort" while Relevance is picked, else the sort's name (the toolbar chip's label). */
@Composable
fun sortChipLabel(sort: ProductSort): String =
    if (sort == ProductSort.RELEVANCE) stringResource(R.string.sort_chip) else stringResource(sort.labelRes())

@StringRes
private fun PriceRange.labelRes(): Int = when (this) {
    PriceRange.ANY -> R.string.filters_any
    PriceRange.UNDER_25 -> R.string.filters_price_under_25
    PriceRange.FROM_25_TO_50 -> R.string.filters_price_25_50
    PriceRange.FROM_50_TO_100 -> R.string.filters_price_50_100
    PriceRange.OVER_100 -> R.string.filters_price_over_100
}

@StringRes
private fun ProductSort.labelRes(): Int = when (this) {
    ProductSort.RELEVANCE -> R.string.sort_relevance
    ProductSort.PRICE_LOW_HIGH -> R.string.sort_price_low_high
    ProductSort.PRICE_HIGH_LOW -> R.string.sort_price_high_low
    ProductSort.TOP_RATED -> R.string.sort_top_rated
}
