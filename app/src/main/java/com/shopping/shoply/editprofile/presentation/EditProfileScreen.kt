package com.shopping.shoply.editprofile.presentation

import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    EditProfileContent(
        state = state.value,
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(20.dp)
        ) {
            HeaderSection(
                onBack = onBack
            )
            AvatarWithPhotoUpload(
                // FR-010: just-picked photo, then saved custom photo, then the provider's photo.
                photo = state.pickedPhotoUri ?: state.customPhoto ?: state.photoUrl,
                initials = nameParts(state.fullName.orEmpty()).second,
                onPick = onPick
            )
            Spacer(Modifier.height(10.dp))
            PersonInfoSection(
                fullName = state.newFullName ?: state.fullName ?: "",
                nameError = state.nameError?.asString(),
                dateOfBirth = state.newDateOfBirth ?: state.dateOfBirth ?: "",
                isSaving = state.isSaving,
                onChangeFullName = onChangeFullName,
                onSaveUserInfo = onSaveUserInfo,
                onChangeDateOfBirth = onChangeDateOfBirth
            )
            Spacer(Modifier.height(25.dp))
            ContactAndSignIn(
                email = state.email ?: "",
                isEmailVerified = state.emailVerified,
                phone = state.phoneNumber,
                isPhoneVerified = state.phoneVerified,
                onChangeEmailClicked = onChangeEmailClicked,
                onChangePhoneClicked = onChangePhoneClicked
            )
        }

    }
}

@Composable
private fun HeaderSection(
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            modifier = Modifier
                .size(38.dp)
                .clickable {
                    onBack()
                },
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.back),
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.edit_profile),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Composable
private fun AvatarWithPhotoUpload(
    photo: Any?,
    initials: String?,
    onPick: (Uri?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Avatar(
                size = 100.dp,
                initials = initials,
                photo = photo
            )
            PhotoPicker(
                modifier = Modifier.align(Alignment.BottomEnd),
                onPick = onPick
            )
        }
    }
}

@Composable
private fun PhotoPicker(
    modifier: Modifier = Modifier,
    onPick: (Uri?) -> Unit
) {
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onPick
    )
    Box(
        modifier
            .size(32.dp)
            .padding(end = 4.dp, bottom = 4.dp)
            .clip(CircleShape)
            .border(.5.dp, MaterialTheme.colorScheme.onPrimary, CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier
                .size(16.dp)
                .clickable {
                    photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            painter = painterResource(R.drawable.ic_camera),
            contentDescription = stringResource(R.string.camera),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
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
        Text(
            text = stringResource(R.string.personal_info)
        )
        Spacer(Modifier.padding(bottom = 10.dp))
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
                value = dateOfBirth,
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
        Text(stringResource(R.string.contact_sign_in))
        Spacer(Modifier.height(20.dp))
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                AuthMethod(
                    authLabel = stringResource(R.string.email),
                    authValue = email,
                    isVerified = isEmailVerified,
                    icon = Icons.Outlined.MailOutline,
                    onChangeClicked = onChangeEmailClicked
                )
                HorizontalDivider(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                )
                AuthMethod(
                    authLabel = stringResource(R.string.phone_number),
                    // FR-005: no number yet, so nothing to verify.
                    authValue = phone ?: stringResource(R.string.add_phone_number),
                    isVerified = if (phone == null) null else isPhoneVerified,
                    icon = Icons.Outlined.Phone,
                    onChangeClicked = onChangePhoneClicked
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.each_one_is_verified_on_its_own_your_current_details),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f)
        )
    }
}

@Composable
fun AuthMethod(
    modifier: Modifier = Modifier,
    authLabel: String,
    authValue: String,
    isVerified: Boolean?,
    icon: ImageVector,
    onChangeClicked: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                modifier = Modifier.padding(8.dp),
                imageVector = icon,
                contentDescription = authLabel,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = authLabel,
                color = Color.Gray
            )
            Text(text = authValue)
            if (isVerified != null) VerificationBadge(isVerified)
        }
        Spacer(Modifier.width(10.dp))
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.LightGray.copy(alpha = .5f),
                contentColor = Color.Black
            ),
            onClick = onChangeClicked
        ) {
            Text(stringResource(R.string.change))
        }
    }
}

@Composable
private fun VerificationBadge(isVerified: Boolean) {
    Box(
        Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (isVerified) Color(0xFFEEF2DC) else Color(0xFFFBE3DC))
            .padding(4.dp)
    ) {
        Text(
            text = stringResource(if (isVerified) R.string.verified else R.string.not_verified),
            color = if (isVerified) Color(0xFF7C9E36) else Color(0xFFCF5740),
            style = MaterialTheme.typography.bodySmall
        )
    }
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
private fun EditProfilePreview(state: EditProfileState) {
    ShoplyTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            EditProfileContent(
                state = state,
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
