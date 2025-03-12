package com.shopping.shoply.authentication.presentation.reset_password.forgot_password

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.reset_password.forgot_password.model.RestMethod
import com.shopping.shoply.authentication.presentation.utility.getRestMethods
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

private const val TAG = "ForgotPasswordScreen"
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    isExpandedScreen: Boolean
) {
    val viewModel: ForgotPasswordViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    ForgotPasswordContent(
        isExpandedScreen = isExpandedScreen,
        restMethods = state.restMethods,
        selectedRestMethodIndex = state.selectedRestMethodIndex,
        onResetMethodChecked = viewModel::onSelectRestMethod,
        onBack = {

        }
    )
}

@Composable
private fun ForgotPasswordContent(
    modifier: Modifier = Modifier,
    isExpandedScreen: Boolean,
    restMethods: List<RestMethod>,
    selectedRestMethodIndex: Int,
    onResetMethodChecked: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        BackButton(onBack = onBack)
        Spacer(Modifier.height(30.dp))
        ForgotPasswordText()
        Spacer(Modifier.height(15.dp))
        GuidingMessageText()
        Spacer(Modifier.height(30.dp))
        restMethods.forEachIndexed { index, restMethod ->
            ResetMethodCard(
                modifier = Modifier.padding(10.dp),
                methodName = restMethod.name.asString(),
                methodMessage = restMethod.message,
                methodIcon = restMethod.icon,
                checked = index == selectedRestMethodIndex,
                onChecked = onResetMethodChecked
            )
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height(30.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            ShoplyButton(
                modifier = if (!isExpandedScreen) Modifier
                    .fillMaxWidth()
                    .padding(10.dp) else Modifier,
                shape = RoundedCornerShape(10.dp),
                label = stringResource(R.string.next),
                onClick = {

                }
            )
        }
    }
}

@Composable
private fun ResetMethodCard(
    modifier: Modifier = Modifier,
    methodName: String,
    methodMessage: String?,
    checked: Boolean,
    onChecked: () -> Unit,
    methodIcon: ImageVector
) {
    val isInDarkTheme = isSystemInDarkTheme()

    OutlinedCard(
        modifier = modifier,
        onClick = onChecked,
        colors = CardDefaults.cardColors(
            containerColor = if (checked) {
                if (isInDarkTheme) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            } else {
                if (isInDarkTheme) {
                    MaterialTheme.colorScheme.surfaceContainerLow
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                }
            }
        ),
        border = BorderStroke(if (checked) 1.dp else 0.dp, MaterialTheme.colorScheme.onSurface)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ResetMethodCircleIcon(
                checked = checked,
                methodIcon = methodIcon,
                backgroundColor = if (isInDarkTheme) {
                    if (checked) {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    }
                } else {
                    if (checked) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = .4f)
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = .4f)
                    }
                },
                methodName = methodName
            )
            if (checked) {
                CheckedCircleIcon()
            }
        }
        Text(
            modifier = Modifier.padding(start = 20.dp),
            text = methodName,
            style = MaterialTheme.typography.titleMedium,
            color = if (isInDarkTheme) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
        Spacer(Modifier.height(5.dp))
        if (methodMessage != null) {
            Text(
                modifier = Modifier.padding(start = 20.dp, end = 10.dp, bottom = 20.dp),
                text = methodMessage,
                style = MaterialTheme.typography.titleMedium,
                color = if (isInDarkTheme) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(.8f)
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = .5f)
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        } else {
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ResetMethodCircleIcon(
    checked: Boolean,
    methodIcon: ImageVector,
    backgroundColor: Color,
    methodName: String
) {
    Box(
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(50))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = methodIcon,
            contentDescription = methodName,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun CheckedCircleIcon() {
    Box(
        Modifier
            .size(25.dp)
            .clip(RoundedCornerShape(50))
            .background(
                color = MaterialTheme.colorScheme.primaryContainer
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = Icons.Default.Check,
            contentDescription = stringResource(R.string.checked_circle),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun GuidingMessageText() {
    Text(
        text = stringResource(R.string.forgot_password_guiding_message),
        modifier = Modifier.padding(start = 20.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.tertiary
    )
}

@Composable
private fun ForgotPasswordText() {
    Text(
        text = stringResource(R.string.forgot_password),
        modifier = Modifier.padding(start = 20.dp),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun BackButton(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(
            onClick = onBack
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back_arrow),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ForgotPasswordContentPreview() {
    ShoplyTheme {
        ForgotPasswordContent(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surface
            ),
            isExpandedScreen = false,
            restMethods = getRestMethods(),
            selectedRestMethodIndex = 0,
            onResetMethodChecked = {},
            onBack = {}
        )
    }
}



