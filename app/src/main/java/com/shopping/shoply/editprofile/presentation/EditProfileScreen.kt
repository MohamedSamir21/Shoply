package com.shopping.shoply.editprofile.presentation

import android.net.Uri
import java.util.TimeZone
import java.util.Date
import java.text.DateFormat
import com.shopping.shoply.common.presentation.theme.shoplyExtra
import com.shopping.shoply.common.presentation.theme.shoplyCard
import com.shopping.shoply.common.presentation.components.ShoplyTopAppBar
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.FilledTonalButton
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextOverflow
import com.shopping.shoply.common.presentation.adaptive.isWideScreen
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shopping.shoply.R
import com.shopping.shoply.editprofile.domain.parseDateOfBirth
import com.shopping.shoply.editprofile.domain.todayAsUtcMidnight
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.authentication.presentation.components.ShoplyButton
import com.shopping.shoply.common.presentation.components.Avatar
import com.shopping.shoply.common.presentation.components.FormTextField
import com.shopping.shoply.common.presentation.theme.ShoplyTheme
import com.shopping.shoply.home.domain.rules.nameParts

@Composable
fun EditProfileScreen(
    snackHostState: SnackbarHostState,
    onBack: () -> Unit,
    onChangeEmailClicked: () -> Unit,
    onChangePhoneClicked: () -> Unit
) {
    val viewModel = hiltViewModel<EditProfileViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditProfileEvent.ProfileUpdateError -> {
                    val result = snackHostState.showSnackbar(
                        message = event.error.asString(context),
                        actionLabel = if (event.canRetry) context.getString(R.string.try_again) else null,
                        duration = SnackbarDuration.Long
                    )
                    when(result) {
                        SnackbarResult.ActionPerformed -> {
                            viewModel.onIntent(EditProfileIntent.SaveUserInfo)
                        }
                        SnackbarResult.Dismissed -> Unit
                    }
                }

                EditProfileEvent.ProfileUpdated -> {
                    snackHostState.showSnackbar(
                        message = context.getString(R.string.profile_gets_updated_successfully)
                    )
                }
            }
        }
    }

    // Landscape phones and tablets get two columns, like the sign-in screens.
    val isWide = isWideScreen()

    EditProfileContent(
        state = state.value,
        isWide = isWide,
        onBack = onBack,
        onPick = { uri ->
            uri?.let {
                viewModel.onIntent(EditProfileIntent.ChangeAvatar(uri.toString()))
            }
        },
        onChangeFullName = {
            viewModel.onIntent(EditProfileIntent.ChangeFullName(it))
        },
        onChangeDateOfBirth = {
            viewModel.onIntent(EditProfileIntent.ChangeDateOfBirth(it))
        },
        onSaveUserInfo = {
            viewModel.onIntent(EditProfileIntent.SaveUserInfo)
        },
        onChangeEmailClicked = onChangeEmailClicked,
        onChangePhoneClicked = onChangePhoneClicked
    )
}

@Composable
private fun EditProfileContent(
    state: EditProfileState,
    isWide: Boolean,
    onBack: () -> Unit,
    onPick: (Uri?) -> Unit,
    onChangeFullName: (String) -> Unit,
    onChangeDateOfBirth: (Long) -> Unit,
    onSaveUserInfo: () -> Unit,
    onChangeEmailClicked: () -> Unit,
    onChangePhoneClicked: () -> Unit
) {
    // Snackbars show in MainShell's host; a second host here would draw each one twice.
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        val avatar = @Composable {
            AvatarWithPhotoUpload(
                // FR-010: just-picked photo, then saved custom photo, then the provider's photo.
                photo = state.pickedPhotoUri ?: state.customPhoto ?: state.photoUrl,
                initials = nameParts(state.fullName.orEmpty()).second,
                onPick = onPick
            )
        }
        val personInfo = @Composable {
            PersonInfoSection(
                fullName = state.newFullName ?: state.fullName ?: "",
                nameError = state.nameError?.asString(),
                dateOfBirth = state.newDateOfBirth ?: state.dateOfBirth ?: "",
                isSaving = state.isSaving,
                onChangeFullName = onChangeFullName,
                onSaveUserInfo = onSaveUserInfo,
                onChangeDateOfBirth = onChangeDateOfBirth
            )
        }
        val contact = @Composable {
            ContactAndSignIn(
                email = state.email ?: "",
                isEmailVerified = state.emailVerified,
                phone = state.phoneNumber,
                isPhoneVerified = state.phoneVerified,
                onChangeEmailClicked = onChangeEmailClicked,
                onChangePhoneClicked = onChangePhoneClicked
            )
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // The top bar stays put; the content below it scrolls, so nothing is cut off.
            ShoplyTopAppBar(title = stringResource(R.string.edit_profile), onBack = onBack)
            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 24.dp)
                    ) {
                        avatar()
                        personInfo()
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(top = 12.dp, bottom = 24.dp)
                    ) {
                        contact()
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                ) {
                    avatar()
                    personInfo()
                    Spacer(Modifier.height(30.dp))
                    contact()
                }
            }
        }
    }
}

@Composable
private fun AvatarWithPhotoUpload(
    photo: Any?,
    initials: String?,
    onPick: (Uri?) -> Unit
) {
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onPick
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 26.dp),
        contentAlignment = Alignment.Center
    ) {
        Box {
            Avatar(size = 100.dp, initials = initials, photo = photo)
            // The whole 36 dp circle is the touch target, not just the icon.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .clickable(onClickLabel = stringResource(R.string.camera)) {
                        photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(17.dp),
                    painter = painterResource(R.drawable.ic_camera),
                    contentDescription = stringResource(R.string.camera),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 18.dp)
    )
}

@Composable
private fun PersonInfoSection(
    fullName: String,
    nameError: String?,
    dateOfBirth: String,
    isSaving: Boolean,
    onChangeFullName: (String) -> Unit,
    onChangeDateOfBirth: (Long) -> Unit,
    onSaveUserInfo: () -> Unit
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    if (showDatePicker) {
        DateOfBirthPicker(
            initialDate = parseDateOfBirth(dateOfBirth),
            onConfirm = { picked ->
                onChangeDateOfBirth(picked)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        SectionLabel(stringResource(R.string.personal_info))
        FormTextField(
            modifier = Modifier.fillMaxWidth(),
            value = fullName,
            onValueChange = onChangeFullName,
            leadingIcon = Icons.Outlined.Person,
            label = stringResource(R.string.full_name),
            showSupportingText = nameError != null,
            supportingText = nameError
        )
        // Read-only: the whole field opens the calendar, so only real, past dates can be entered.
        Box(Modifier.fillMaxWidth()) {
            FormTextField(
                modifier = Modifier.fillMaxWidth(),
                value = remember(dateOfBirth) { displayDate(dateOfBirth) },
                onValueChange = {},
                leadingIcon = Icons.Outlined.DateRange,
                label = stringResource(R.string.date_of_birth),
                showSupportingText = false
            )
            Box(
                Modifier
                    .matchParentSize()
                    .clickable(onClickLabel = stringResource(R.string.choose_date_of_birth)) {
                        showDatePicker = true
                    }
            )
        }
        Spacer(Modifier.height(16.dp))
        ShoplyButton(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.save),
            onClick = onSaveUserInfo,
            enable = !isSaving
        )
        // FR-023: Save shows progress while the change is being saved.
        if (isSaving) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ContactAndSignIn(
    email: String,
    isEmailVerified: Boolean,
    phone: String?,
    isPhoneVerified: Boolean,
    onChangeEmailClicked: () -> Unit,
    onChangePhoneClicked: () -> Unit,
) {
    Column {
        SectionLabel(stringResource(R.string.contact_sign_in))
        Column(
            Modifier
                .fillMaxWidth()
                .shoplyCard()
        ) {
            ContactRow(
                icon = Icons.Outlined.MailOutline,
                label = stringResource(R.string.email),
                value = email,
                isVerified = isEmailVerified,
                onAction = onChangeEmailClicked
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ContactRow(
                icon = Icons.Outlined.Phone,
                label = stringResource(R.string.phone_number),
                value = phone,
                isVerified = isPhoneVerified,
                onAction = onChangePhoneClicked
            )
        }
        Text(
            text = stringResource(R.string.each_one_is_verified_on_its_own_your_current_details),
            fontSize = 12.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp)
        )
    }
}

/**
 * One sign-in method: icon, label, value and a Change button. With no [value] it reads
 * "Not added" and offers Add instead. The Verified tag shows only when [isVerified] (FR-004/005).
 */
@Composable
private fun ContactRow(
    icon: ImageVector,
    label: String,
    value: String?,
    isVerified: Boolean,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            // Long emails or numbers stay on one line and end with an ellipsis.
            Text(
                text = value ?: stringResource(R.string.not_added),
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (value == null) MaterialTheme.shoplyExtra.inkFaint else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 3.dp)
            )
            if (value != null && isVerified) VerifiedTag(Modifier.padding(top = 6.dp))
        }
        if (value == null) {
            Button(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.add))
            }
        } else {
            FilledTonalButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(stringResource(R.string.change))
            }
        }
    }
}

@Composable
private fun VerifiedTag(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.verified),
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.shoplyExtra.onGreenSoft,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.shoplyExtra.greenSoft)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** A stored `yyyy-MM-dd` date shown the user's way, e.g. "Apr 25, 2001"; other text as is. */
private fun displayDate(isoDate: String): String {
    val utcMillis = parseDateOfBirth(isoDate) ?: return isoDate
    return DateFormat.getDateInstance(DateFormat.MEDIUM).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(utcMillis))
}

/** Calendar for the date of birth; days after today can't be picked (spec FR-021). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthPicker(
    initialDate: Long?,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate,
        selectableDates = remember {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) =
                    utcTimeMillis <= todayAsUtcMidnight(System.currentTimeMillis())
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { pickerState.selectedDateMillis?.let(onConfirm) ?: onDismiss() },
                enabled = pickerState.selectedDateMillis != null
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        DatePicker(state = pickerState)
    }
}

private val previewState = EditProfileState(
    fullName = "Ahmed Mostafa",
    dateOfBirth = "2001-04-25",
    email = "ahmed@gmail.com",
    emailVerified = true,
    phoneNumber = null,
    isLoading = false
)

@Composable
private fun EditProfilePreview(state: EditProfileState, isWide: Boolean = false) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            EditProfileContent(
                state = state,
                isWide = isWide,
                onBack = {},
                onPick = {},
                onChangeFullName = {},
                onChangeDateOfBirth = {},
                onSaveUserInfo = {},
                onChangeEmailClicked = {},
                onChangePhoneClicked = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EditProfileLoadedPreview() = EditProfilePreview(previewState)

@PreviewLightDark
@Composable
private fun EditProfileVerifiedPhonePreview() = EditProfilePreview(
    previewState.copy(emailVerified = false, phoneNumber = "+201001234567", phoneVerified = true)
)

@PreviewLightDark
@Composable
private fun EditProfileLoadingPreview() = EditProfilePreview(EditProfileState(isLoading = true))

@PreviewLightDark
@Composable
private fun EditProfileSavingPreview() = EditProfilePreview(previewState.copy(isSaving = true))

@PreviewLightDark
@Composable
private fun EditProfileNameErrorPreview() = EditProfilePreview(
    previewState.copy(
        newFullName = "",
        nameError = UiText.StringResource(R.string.full_name_required)
    )
)

private val landscapeState = previewState.copy(email = "a.very.long.email.address@example-company.com")

// One @Preview per function: kapt can't read repeated @Preview annotations.
@Preview(name = "Landscape - light", widthDp = 891, heightDp = 411)
@Composable
private fun EditProfileLandscapeLightPreview() = EditProfilePreview(landscapeState, isWide = true)

@Preview(
    name = "Landscape - dark",
    widthDp = 891,
    heightDp = 411,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
private fun EditProfileLandscapeDarkPreview() = EditProfilePreview(landscapeState, isWide = true)
