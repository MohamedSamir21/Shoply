package com.shopping.shoply.home.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.shopping.shoply.common.presentation.components.SkeletonBone
import com.shopping.shoply.common.presentation.components.SkeletonPulse
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

/**
 * Loading placeholder below the search bar: a 148 dp promo block, 4 circle-and-label placeholders
 * and a 2-card product row. Pulses gently ([SkeletonPulse]).
 */
@Composable
fun HomeSkeleton(modifier: Modifier = Modifier) {
    SkeletonPulse(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(top = ShoplyDimens.space5)) {
            SkeletonBone(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(horizontal = ShoplyDimens.gutter)
                    .fillMaxWidth()
                    .height(148.dp),
            )
            Row(
                modifier = Modifier.padding(start = ShoplyDimens.gutter, end = ShoplyDimens.gutter, top = 28.dp, bottom = 26.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                repeat(4) {
                    Column(
                        modifier = Modifier.width(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(ShoplyDimens.space2),
                    ) {
                        SkeletonBone(shape = CircleShape, modifier = Modifier.size(60.dp))
                        SkeletonBone(modifier = Modifier.size(width = 44.dp, height = 10.dp))
                    }
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = ShoplyDimens.gutter),
                horizontalArrangement = Arrangement.spacedBy(ShoplyDimens.space4),
            ) {
                repeat(2) {
                    Column(modifier = Modifier.weight(1f)) {
                        SkeletonBone(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().height(152.dp))
                        SkeletonBone(modifier = Modifier.padding(top = ShoplyDimens.space3).fillMaxWidth(0.88f).height(14.dp))
                        SkeletonBone(modifier = Modifier.padding(top = ShoplyDimens.space2).fillMaxWidth(0.56f).height(14.dp))
                        SkeletonBone(modifier = Modifier.padding(top = 10.dp).fillMaxWidth(0.4f).height(16.dp))
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun HomeSkeletonPreview() {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            HomeSkeleton(modifier = Modifier.padding(bottom = ShoplyDimens.space4))
        }
    }
}
