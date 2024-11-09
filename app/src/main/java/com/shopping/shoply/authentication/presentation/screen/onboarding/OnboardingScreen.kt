package com.shopping.shoply.authentication.presentation.screen.onboarding

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.viewmodel.OnboardingViewModel
import com.shopping.shoply.authentication.presentation.viewmodel.generateOnboardings
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = viewModel(),
    navController: NavController
) {
    val state = viewModel.state.collectAsState()

    OnboardingContent(
        state = state.value,
        onSwipe = { index ->
            viewModel.onEvent(OnboardingScreenEvent.Swipe(index))
        },
        onForward = {
            viewModel.onEvent(OnboardingScreenEvent.Forward)
            if (state.value.finish)
                navController.navigate(NavRoutes.SignUp.value)
        },
        onPrevious = {
            viewModel.onEvent(OnboardingScreenEvent.Previous)
        },
        onSkip = {
            navController.navigate(
                NavRoutes.SignUp.value
            )
        }
    )
}

@Composable
fun OnboardingContent(
    state: OnboardingScreenState,
    onSwipe: (Int) -> Unit,
    onForward: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit
) {
    val pagerState = rememberPagerState { state.onboardings.size }

    LaunchedEffect(pagerState.settledPage) {
        onSwipe(pagerState.settledPage)
    }

    LaunchedEffect(state.currentPageIndex) {
        pagerState.animateScrollToPage(state.currentPageIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        PreviousSkipBox(pagerState, onSkip, onPrevious)
        RestOfContentBox(pagerState, state, onForward)
    }
}

@Composable
private fun PreviousSkipBox(
    pagerState: PagerState,
    onSkip: () -> Unit,
    onPrevious: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(start = 10.dp, top = 20.dp, bottom = 10.dp)
            .fillMaxWidth()
    ) {
        if (pagerState.currentPage == 0) {
            SkipButton(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = onSkip
            )
        } else {
            PreviousButton(
                modifier = Modifier.align(Alignment.CenterStart),
                onClick = onPrevious
            )
        }
    }
}

@Composable
private fun SkipButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(text = stringResource(R.string.skip))
    }
}

@Composable
private fun PreviousButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(
            text = stringResource(R.string.previous),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun RestOfContentBox(
    pagerState: PagerState,
    state: OnboardingScreenState,
    onForward: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        OnboardingHorizontalPager(pagerState, state)
        PagerIndicatorBox(
            Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            state,
            pagerState
        )
        ForwardButtonBox(
            Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth(),
            onForward
        )
    }
}

@Composable
private fun OnboardingHorizontalPager(
    pagerState: PagerState,
    state: OnboardingScreenState
) {
    HorizontalPager(
        state = pagerState,
        pageSpacing = 5.dp
    ) { index ->
        OnboardingCard(
            modifier = Modifier.fillMaxSize(),
            onboarding = state.onboardings[index]
        )
    }
}

@Composable
private fun OnboardingCard(
    modifier: Modifier = Modifier,
    onboarding: Onboarding
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        if (maxWidth < 600.dp) {
            CompactCard(onboarding)
        } else {
            ExpandedCard(onboarding)
        }
    }
}

@Composable
private fun CompactCard(onboarding: Onboarding) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp)
    ) {
        CardImage(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            onboarding = onboarding
        )
        CardTitle(onboarding)
        CardDescription(onboarding)
    }
}

@Composable
private fun ExpandedCard(
    onboarding: Onboarding
) {
    Card(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier.padding(bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CardImage(onboarding = onboarding)
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                CardTitle(onboarding = onboarding)
                CardDescription(onboarding = onboarding)
            }
        }
    }
}

@Composable
private fun CardImage(
    modifier: Modifier = Modifier,
    onboarding: Onboarding
) {
    Box(
        modifier = modifier
            .padding(10.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .wrapContentWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            modifier = Modifier
                .padding(20.dp)
                .size(300.dp),
            painter = painterResource(id = onboarding.image),
            contentDescription = stringResource(
                R.string.person_shopping
            ),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun CardTitle(
    onboarding: Onboarding
) {
    Text(
        text = onboarding.title,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(10.dp)
    )
}

@Composable
private fun CardDescription(
    onboarding: Onboarding
) {
    Text(
        text = onboarding.description,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(10.dp)
    )
}

@Composable
private fun PagerIndicatorBox(
    modifier: Modifier = Modifier,
    state: OnboardingScreenState,
    pagerState: PagerState
) {
    Box(
        modifier = modifier
    ) {
        PagerIndicator(
            circlesNumber = state.onboardings.size,
            currentDisplayedPage = pagerState.currentPage
        )
    }
}

@Composable
private fun PagerIndicator(
    circlesNumber: Int,
    currentDisplayedPage: Int
) {
    Row {
        repeat(circlesNumber) { iteration ->
            val isCurrentDisplayedPage = iteration == currentDisplayedPage
            PagerIndicatorRoundedShape(isCurrentDisplayedPage)
        }
    }
}

@Composable
private fun PagerIndicatorRoundedShape(
    isCurrentDisplayedPage: Boolean
) {
    Box(
        modifier = Modifier
            .animateContentSize(
                animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
            .width(
                if (isCurrentDisplayedPage)
                    15.dp
                else
                    10.dp
            )
            .height(9.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isCurrentDisplayedPage)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.primary
            )
    )
}

@Composable
private fun ForwardButtonBox(
    modifier: Modifier = Modifier,
    onForward: () -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        ForwardButton(onClick = onForward)
    }
}

@Composable
private fun ForwardButton(
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .offset(x = 20.dp, y = 20.dp)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = stringResource(
                id = R.string.forward
            ),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview(
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun OnboardingContentPreview() {
    val state = OnboardingScreenState(onboardings = generateOnboardings())

    ShoplyTheme {
        OnboardingContent(
            state = state,
            onSwipe = {},
            onForward = {},
            onPrevious = {},
            onSkip = {}
        )
    }
}