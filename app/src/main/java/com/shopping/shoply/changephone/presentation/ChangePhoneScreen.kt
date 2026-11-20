package com.shopping.shoply.changephone.presentation

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.presentation.components.CountryCodePicker
import com.shopping.shoply.authentication.presentation.components.FormPassword
import com.shopping.shoply.authentication.presentation.components.OtpInput
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.presentation.components.FormTextField
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.common.presentation.theme.darkRing
import com.shopping.shoply.common.presentation.theme.shoplyExtra

/** Add or change the phone number: number (+ password) → 6-digit SMS code → done (spec FR-040 to FR-046). */
@Composable
fun ChangePhoneScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit
) {
    val viewModel = hiltViewModel<ChangePhoneViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ChangePhoneEvent.Message -> snackbarHostState.showSnackbar(event.text.asString(context))
            }
        }
    }

    ChangePhoneContent(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}

@Composable
private fun ChangePhoneContent(
    state: ChangePhoneState,
    onIntent: (ChangePhoneIntent) -> Unit,
    onBack: () -> Unit
) {
    val title = stringResource(if (state.isAdding) R.string.add_phone_number_title else R.string.screen_change_phone)
    Scaffold(
        topBar = { ShoplyTopAppBar(title = title, onBack = onBack) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val (label, intent) = when (state.step) {
                    ChangePhoneStep.ENTER -> R.string.send_code to ChangePhoneIntent.SendCode
                    ChangePhoneStep.CODE -> R.string.verify to ChangePhoneIntent.Verify
                    ChangePhoneStep.UPDATED -> R.string.done to null
                }
                ShoplyButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(label),
                    onClick = { if (intent != null) onIntent(intent) else onBack() },
                    enable = !state.isBusy
                )
                if (state.isBusy) LinearProgressIndicator(Modifier.fillMaxWidth())
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
                    ChangePhoneStep.ENTER -> EnterStep(state, onIntent)
                    ChangePhoneStep.CODE -> CodeStep(state, onIntent)
                    ChangePhoneStep.UPDATED -> UpdatedStep(state)
                }
            }
        }
    }
}

@Composable
private fun EnterStep(state: ChangePhoneState, onIntent: (ChangePhoneIntent) -> Unit) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        state.currentPhone?.let { CurrentNumber(it) }
        FormTextField(
            modifier = Modifier.fillMaxWidth(),
            value = state.number,
            onValueChange = { onIntent(ChangePhoneIntent.ChangeNumber(it)) },
            label = stringResource(R.string.new_phone_number),
            leadingContent = {
                state.selectedCountry?.let { country ->
                    Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        CountryCodePicker(
                            bare = true,
                            selectedCountryInfo = country,
                            countries = state.shownCountries,
                            keyword = state.countryKeyword,
                            showMenu = state.showCountryMenu,
                            isClickable = state.countries.size > 1,
                            onToggleMenu = { onIntent(ChangePhoneIntent.ToggleCountryMenu) },
                            onKeywordChange = { onIntent(ChangePhoneIntent.SearchCountry(it)) },
                            onSelect = { onIntent(ChangePhoneIntent.SelectCountry(it)) },
                            onDismiss = { onIntent(ChangePhoneIntent.ToggleCountryMenu) }
                        )
                    }
                }
            },
            showSupportingText = state.numberError != null,
            supportingText = state.numberError?.asString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        if (state.needsPassword) {
            Column {
                FormPassword(
                    modifier = Modifier.fillMaxWidth(),
                    password = state.password,
                    showPassword = showPassword,
                    onPasswordChanged = { onIntent(ChangePhoneIntent.ChangePassword(it)) },
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
}

@Composable
private fun CurrentNumber(number: String) {
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
                text = stringResource(R.string.current_number),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = number, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CodeStep(state: ChangePhoneState, onIntent: (ChangePhoneIntent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.enter_code_sent_to, maskPhone(state.sentTo)),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        OtpInput(
            value = state.code,
            onValueChange = { onIntent(ChangePhoneIntent.ChangeCode(it)) },
            length = 6
        )
        state.codeError?.let {
            Text(text = it.asString(), color = MaterialTheme.colorScheme.error, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
        if (state.resendSecondsLeft > 0) {
            Text(
                text = stringResource(R.string.resend_code_in_time, "00:%02d".format(state.resendSecondsLeft)),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            TextButton(onClick = { onIntent(ChangePhoneIntent.ResendCode) }, enabled = !state.isBusy) {
                Text(stringResource(R.string.resend_code), fontWeight = FontWeight.Bold)
            }
        }
        // Disabled while a send or verify runs, so it can't interrupt one half-way.
        TextButton(onClick = { onIntent(ChangePhoneIntent.UseDifferentNumber) }, enabled = !state.isBusy) {
            Text(stringResource(R.string.use_a_different_number), fontWeight = FontWeight.Bold)
        }
    }
}

/** "+20 *** *** **67": enough to recognise the number without showing all of it. */
private fun maskPhone(e164: String): String {
    val digits = e164.filter(Char::isDigit)
    return if (digits.length <= 2) e164 else "+*** *** **" + digits.takeLast(2)
}

@Composable
private fun UpdatedStep(state: ChangePhoneState) {
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
                .background(MaterialTheme.shoplyExtra.greenSoft)
                .darkRing(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.shoplyExtra.onGreenSoft,
                modifier = Modifier.size(50.dp)
            )
        }
        Text(
            text = stringResource(if (state.isAdding) R.string.phone_number_added else R.string.phone_number_updated),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 28.dp)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.phone_updated_message, state.sentTo),
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp)
        )
    }
}

private val previewCountry = CountryInfo(codeNumber = "+20", name = "Egypt", flag = "🇪🇬", format = "100 123 4567")

private val previewState = ChangePhoneState(
    currentPhone = "+201001234567",
    needsPassword = true,
    countries = listOf(previewCountry),
    shownCountries = listOf(previewCountry),
    selectedCountry = previewCountry,
    number = "1009876543",
    sentTo = "+201009876543"
)

@Composable
private fun ChangePhonePreview(state: ChangePhoneState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            ChangePhoneContent(state = state, onIntent = {}, onBack = {})
        }
    }
}

@PreviewLightDark
@Composable
private fun ChangePhoneEnterPreview() = ChangePhonePreview(previewState)

@PreviewLightDark
@Composable
private fun AddPhoneEnterPreview() = ChangePhonePreview(previewState.copy(currentPhone = null, needsPassword = false, number = ""))

@PreviewLightDark
@Composable
private fun ChangePhoneErrorsPreview() = ChangePhonePreview(
    previewState.copy(
        numberError = UiText.StringResource(R.string.phone_already_in_use),
        passwordError = UiText.StringResource(R.string.wrong_password)
    )
)

@PreviewLightDark
@Composable
private fun ChangePhoneSendingPreview() = ChangePhonePreview(previewState.copy(isBusy = true))

@PreviewLightDark
@Composable
private fun ChangePhoneCodePreview() = ChangePhonePreview(
    previewState.copy(step = ChangePhoneStep.CODE, code = "382", resendSecondsLeft = 21)
)

@PreviewLightDark
@Composable
private fun ChangePhoneWrongCodePreview() = ChangePhonePreview(
    previewState.copy(step = ChangePhoneStep.CODE, code = "382911", codeError = UiText.StringResource(R.string.invalid_code))
)

@PreviewLightDark
@Composable
private fun ChangePhoneUpdatedPreview() = ChangePhonePreview(previewState.copy(step = ChangePhoneStep.UPDATED))
