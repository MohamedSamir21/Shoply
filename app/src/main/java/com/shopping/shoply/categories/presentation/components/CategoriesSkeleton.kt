package com.shopping.shoply.categories.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/** Loading placeholder: 3 rows of 2 pulsing 124 dp blocks, laid out like the category grid. */
@Composable
fun CategoriesSkeleton(modifier: Modifier = Modifier) {
    SkeletonPulse(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = ShoplyDimens.space2),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            repeat(3) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    repeat(2) {
                        SkeletonBone(
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(124.dp),
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun CategoriesSkeletonPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CategoriesSkeleton(modifier = Modifier.padding(bottom = ShoplyDimens.space4))
        }
    }
}
