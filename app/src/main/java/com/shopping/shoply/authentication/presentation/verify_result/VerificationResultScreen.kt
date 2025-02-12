package com.shopping.shoply.authentication.presentation.verify_result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ContinueToHome
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun VerificationResultScreen(
    navHostController: NavHostController,
    deepLink: String
) {
    val viewModel: VerificationResultViewModel = hiltViewModel()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val isVerifying = state.value.isVerifying
    val verificationHasSucceeded = state.value.verificationHasSucceeded
    val verificationError = state.value.verificationError

    LaunchedEffect(true) {
        viewModel.verifyEmail(deepLink)
    }

    VerificationResultContent(
        isVerifying = isVerifying,
        verificationHasSucceeded = verificationHasSucceeded,
        verificationError = verificationError,
        onContinueToHome = {
            navHostController.popBackStack(
                destinationId = navHostController.graph.findStartDestination().id,
                inclusive = true
            )
            navHostController.navigate(NavRoutes.Home.route)
        }
    )
}

@Composable
private fun VerificationResultContent(
    isVerifying: Boolean,
    verificationHasSucceeded: Boolean,
    verificationError: UiText?,
    onContinueToHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isVerifying) {
            CircularProgressIndicator(Modifier.size(100.dp))
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.verifying),
                textAlign = TextAlign.Center,
                fontSize = 30.sp
            )
        }
        if (verificationHasSucceeded) {
            Image(
                modifier = Modifier
                    .size(200.dp)
                    .animateContentSize(),
                imageVector = ImageVector.vectorResource(R.drawable.congratulations),
                contentDescription = stringResource(R.string.congratulations)
            )
            Text(
                modifier = Modifier.animateContentSize(),
                text = stringResource(R.string.congratulations),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                color = Color(0xFF88AE45)
            )
        }
        if (verificationError != null) {
            Column {
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSystemInDarkTheme()) {
                        Box(
                            Modifier
                                .align(Alignment.Center)
                                .size(width = 100.dp, height = 50.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.inverseOnSurface)
                        )
                    }
                    Image(
                        painter = painterResource(R.drawable.oops),
                        contentDescription = stringResource(R.string.oops),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .size(200.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = verificationError.asString(),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(Modifier.height(40.dp))
        AnimatedVisibility(
            !isVerifying
        ) {
            ContinueToHome(
                onClick = onContinueToHome
            )
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
private fun VerificationResultContentPreview() {
    ShoplyTheme {
        VerificationResultContent(
            isVerifying = false,
            verificationHasSucceeded = true,
            verificationError = null
        ) {

        }
    }
}
