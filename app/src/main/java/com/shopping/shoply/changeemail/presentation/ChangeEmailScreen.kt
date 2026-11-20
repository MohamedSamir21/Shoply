package com.shopping.shoply.changeemail.presentation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.components.FormPassword
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.components.ShoplyButtonVariant
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.presentation.components.FormTextField
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.darkRing
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import kotlinx.coroutines.launch

/**
 * Change Email: enter the new address and password, then wait for the confirmation link
 * (spec FR-030 to FR-038). [onSignedOut] runs when the change ended this session.
 */
@Composable
fun ChangeEmailScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSignedOut: () -> Unit
) {
    val viewModel = hiltViewModel<ChangeEmailViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            val message = when (event) {
                is ChangeEmailEvent.Error -> event.message.asString(context)
                is ChangeEmailEvent.Info -> event.message.asString(context)
                ChangeEmailEvent.LinkResent -> context.getString(R.string.verification_link_sent)
            }
            snackbarHostState.showSnackbar(message)
        }
    }
    // Back from the email app: see whether the link was clicked.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onIntent(ChangeEmailIntent.CheckIfConfirmed)
    }

    // FR-038: once the change ended the session, every way out leads to Sign In.
    val exit = if (state.mustSignInAgain) onSignedOut else onBack
    BackHandler(enabled = state.mustSignInAgain, onBack = onSignedOut)

    ChangeEmailContent(
        state = state,
        onIntent = viewModel::onIntent,
        onBack = exit,
        onOpenEmailApp = {
            if (!context.openEmailApp()) {
                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.no_email_app)) }
            }
        },
        onDone = exit
    )
}

/** Opens the user's email app; false when the phone has none. */
private fun Context.openEmailApp(): Boolean = try {
    startActivity(
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_APP_EMAIL)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    true
} catch (e: ActivityNotFoundException) {
    false
}

@Composable
private fun ChangeEmailContent(
    state: ChangeEmailState,
    onIntent: (ChangeEmailIntent) -> Unit,
    onBack: () -> Unit,
    onOpenEmailApp: () -> Unit,
    onDone: () -> Unit
) {
    Scaffold(
        topBar = { ShoplyTopAppBar(title = stringResource(R.string.screen_change_email), onBack = onBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (state.step) {
                    ChangeEmailStep.ENTER -> {
                        ShoplyButton(
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(R.string.send_verification_link),
                            onClick = { onIntent(ChangeEmailIntent.SendLink) },
                            enable = !state.isSending
                        )
                        if (state.isSending) LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    ChangeEmailStep.CHECK_INBOX -> {
                        ShoplyButton(
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(R.string.open_email_app),
                            onClick = onOpenEmailApp
                        )
                        ShoplyButton(
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(R.string.use_a_different_email),
                            variant = ShoplyButtonVariant.Ghost,
                            onClick = { onIntent(ChangeEmailIntent.UseDifferentEmail) }
                        )
                    }
                    ChangeEmailStep.UPDATED -> ShoplyButton(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(if (state.mustSignInAgain) R.string.sign_in else R.string.done),
                        onClick = onDone
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            // Phone-width column, centred on tablets and in landscape.
            Column(
                Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                when (state.step) {
                    ChangeEmailStep.ENTER -> EnterStep(state, onIntent)
                    ChangeEmailStep.CHECK_INBOX -> CheckInboxStep(state, onIntent)
                    ChangeEmailStep.UPDATED -> UpdatedStep(state)
                }
            }
        }
    }
}

@Composable
private fun EnterStep(state: ChangeEmailState, onIntent: (ChangeEmailIntent) -> Unit) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        CurrentEmail(state.currentEmail)
        FormTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.newEmail,
            onValueChange = { onIntent(ChangeEmailIntent.ChangeNewEmail(it)) },
            leadingIcon = Icons.Outlined.MailOutline,
            label = stringResource(R.string.new_email),
            showSupportingText = state.emailError != null,
            supportingText = state.emailError?.asString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Column {
            FormPassword(
                modifier = Modifier.fillMaxWidth(),
                password = state.password,
                showPassword = showPassword,
                onPasswordChanged = { onIntent(ChangeEmailIntent.ChangePassword(it)) },
                label = stringResource(R.string.current_password),
                showSupportingText = state.passwordError != null,
                errorMessage = state.passwordError?.asString(),
                onVisibilityChanged = { showPassword = it }
            )
            Text(
                text = stringResource(R.string.enter_password_to_continue),
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }
    }
}

@Composable
private fun CurrentEmail(email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Column(Modifier.padding(start = 12.dp)) {
            Text(
                text = stringResource(R.string.current_email),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = email,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CheckInboxStep(state: ChangeEmailState, onIntent: (ChangeEmailIntent) -> Unit) {
    CenterMessage(
        icon = Icons.Outlined.MailOutline,
        success = false,
        title = stringResource(R.string.check_your_inbox),
        message = stringResource(R.string.confirmation_link_sent_to, state.newEmail)
    )
    Spacer(Modifier.height(8.dp))
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (state.resendSecondsLeft > 0) {
            Text(
                text = stringResource(R.string.resend_link_in, "00:%02d".format(state.resendSecondsLeft)),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            TextButton(onClick = { onIntent(ChangeEmailIntent.ResendLink) }, enabled = !state.isSending) {
                Text(stringResource(R.string.resend_link), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun UpdatedStep(state: ChangeEmailState) {
    CenterMessage(
        icon = Icons.Outlined.CheckCircle,
        success = true,
        title = stringResource(R.string.email_updated),
        message = stringResource(
            if (state.mustSignInAgain) R.string.email_updated_sign_in_again else R.string.email_updated_message,
            state.newEmail
        )
    )
}

/** The design's big round badge with a title and a line of text under it. */
@Composable
private fun CenterMessage(icon: ImageVector, success: Boolean, title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(if (success) MaterialTheme.shoplyExtra.greenSoft else MaterialTheme.colorScheme.primaryContainer)
                .darkRing(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (success) MaterialTheme.shoplyExtra.onGreenSoft else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(50.dp)
            )
        }
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 28.dp)
        )
        Text(
            text = message,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 10.dp)
                .widthIn(max = 300.dp)
        )
    }
}

@Composable
private fun ChangeEmailPreview(state: ChangeEmailState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            ChangeEmailContent(state = state, onIntent = {}, onBack = {}, onOpenEmailApp = {}, onDone = {})
        }
    }
}

private val previewState = ChangeEmailState(
    currentEmail = "ahmed@gmail.com",
    newEmail = "ahmed.mansour@outlook.com"
)

@PreviewLightDark
@Composable
private fun ChangeEmailEnterPreview() = ChangeEmailPreview(previewState)

@PreviewLightDark
@Composable
private fun ChangeEmailErrorsPreview() = ChangeEmailPreview(
    previewState.copy(
        emailError = UiText.StringResource(R.string.enter_valid_email),
        passwordError = UiText.StringResource(R.string.wrong_password)
    )
)

@PreviewLightDark
@Composable
private fun ChangeEmailSendingPreview() = ChangeEmailPreview(previewState.copy(password = "secret", isSending = true))

@PreviewLightDark
@Composable
private fun ChangeEmailCheckInboxPreview() = ChangeEmailPreview(
    previewState.copy(step = ChangeEmailStep.CHECK_INBOX, resendSecondsLeft = 24)
)

@PreviewLightDark
@Composable
private fun ChangeEmailCheckInboxResendPreview() = ChangeEmailPreview(
    previewState.copy(step = ChangeEmailStep.CHECK_INBOX)
)

@PreviewLightDark
@Composable
private fun ChangeEmailUpdatedPreview() = ChangeEmailPreview(previewState.copy(step = ChangeEmailStep.UPDATED))

@PreviewLightDark
@Composable
private fun ChangeEmailSignInAgainPreview() = ChangeEmailPreview(
    previewState.copy(step = ChangeEmailStep.UPDATED, mustSignInAgain = true)
)
