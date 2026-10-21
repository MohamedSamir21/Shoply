package com.shopping.shoply.categories.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.presentation.toImageVector
import com.shopping.shoply.categories.presentation.preview.CategoriesPreviewData
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.shoplyCard

/**
 * Grid card: 48 dp orange icon box, the category name, and "128 items" below it.
 * The whole card is one button; fills the width it's given.
 */
@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shoplyCard()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(ShoplyDimens.space4),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = category.icon.toImageVector(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 14.dp),
        )
        Text(
            text = pluralStringResource(R.plurals.categories_item_count, category.itemCount, category.itemCount),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

/** Two cards side by side, as in the grid; "Books" shows the singular "1 item". */
@PreviewLightDark
@Composable
fun CategoryCardPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(ShoplyDimens.gutter),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                CategoryCard(category = CategoriesPreviewData.categories[0], onClick = {}, modifier = Modifier.weight(1f))
                CategoryCard(category = CategoriesPreviewData.categories[6], onClick = {}, modifier = Modifier.weight(1f))
            }
        }
    }
}
