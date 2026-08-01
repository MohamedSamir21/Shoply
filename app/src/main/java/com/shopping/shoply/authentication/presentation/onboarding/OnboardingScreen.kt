package com.shopping.shoply.authentication.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.CircleFab
import com.shopping.shoply.authentication.presentation.components.Dots
import com.shopping.shoply.common.presentation.components.HeroArt
import com.shopping.shoply.common.presentation.components.HeroKind
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyDimens
import com.shopping.shoply.common.presentation.theme.ShoplyIcons
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

private fun heroKindFor(index: Int): HeroKind = when (index) {
    0 -> HeroKind.Store
    1 -> HeroKind.Delivery
    else -> HeroKind.Payment
}

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    navController: NavController
) {
    val state = viewModel.state.collectAsState()

    OnboardingContent(
        state = state.value,
        onSwipe = { index -> viewModel.onEvent(OnboardingScreenEvent.Swipe(index)) },
        onForward = { viewModel.onEvent(OnboardingScreenEvent.Forward) },
        onPrevious = { viewModel.onEvent(OnboardingScreenEvent.Previous) },
        onSkip = { navController.navigate(NavRoutes.SignUp.route) },
        onFinish = { navController.navigate(NavRoutes.SignUp.route) }
    )
}

@Composable
fun OnboardingContent(
    state: OnboardingScreenState,
    onSwipe: (Int) -> Unit,
    onForward: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState { state.onboardings.size }
    val page = pagerState.currentPage
    val isLast = page == state.onboardings.lastIndex

    LaunchedEffect(pagerState.settledPage) { onSwipe(pagerState.settledPage) }
    LaunchedEffect(state.currentPageIndex) {
        pagerState.animateScrollToPage(state.currentPageIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // —— Orange hero panel with a rounded bottom ——
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = ShoplyDimens.radiusPanel, bottomEnd = ShoplyDimens.radiusPanel))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = ShoplyDimens.space5)
                ) {
                    if (page > 0) {
                        OnboardingTextButton(
                            text = stringResource(R.string.previous),
                            onClick = onPrevious,
                            modifier = Modifier.align(Alignment.CenterStart),
                        )
                    }
                    if (!isLast) {
                        OnboardingTextButton(
                            text = stringResource(R.string.skip),
                            onClick = onSkip,
                            modifier = Modifier.align(Alignment.CenterEnd),
                        )
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                ) { index ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        HeroArt(kind = heroKindFor(index))
                    }
                }
            }
        }

        // —— Bottom content ——
        Column(
            modifier = Modifier.padding(
                start = 28.dp,
                end = 28.dp,
                top = ShoplyDimens.space8,
                bottom = ShoplyDimens.space6,
            )
        ) {
            Text(
                text = state.onboardings[page].title.asString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer8()
            Text(
                text = state.onboardings[page].description.asString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(Modifier.height(ShoplyDimens.space8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Dots(count = state.onboardings.size, index = page)
                CircleFab(
                    icon = if (isLast) ShoplyIcons.Check else ShoplyIcons.ArrowRight,
                    onClick = { if (isLast) onFinish() else onForward() },
                )
            }
        }
    }
}

@Composable
private fun Spacer8() = Box(Modifier.height(ShoplyDimens.space3))

@Composable
private fun OnboardingTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(modifier = modifier, onClick = onClick) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewLightDark
@Composable
private fun OnboardingContentPreview() {
    val state = OnboardingScreenState(onboardings = generateOnboardings())
    ShoplyTheme {
        OnboardingContent(
            state = state,
            onSwipe = {},
            onForward = {},
            onPrevious = {},
            onSkip = {},
            onFinish = {}
        )
    }
}
