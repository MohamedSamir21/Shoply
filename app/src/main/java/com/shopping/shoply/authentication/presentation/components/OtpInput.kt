package com.shopping.shoply.authentication.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.common.presentation.theme.ShoplyShapes
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

// Test tags: the field itself is invisible (cells are drawn by the decorationBox) and the
// cells are text-free until typed into, so the semantics tree needs tags to reach both.
internal const val OTP_INPUT_TAG = "otp_input"
internal const val OTP_CELL_TAG = "otp_cell"

/**
 * One-time-code input — a hidden text field rendering [length] cells. Cells fill
 * the available width evenly (so all slots fit any orientation) and turn warm
 * orange when populated. Mirrors the handoff `OtpInput`.
 */
@Composable
fun OtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 4,
    requestFocus: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }
    if (requestFocus) {
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
    }
    BasicTextField(
        value = value,
        onValueChange = {
            if (it.length <= length && it.all(Char::isDigit)) onValueChange(it)
        },
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag(OTP_INPUT_TAG),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(length) { index ->
                    val char = value.getOrNull(index)?.toString().orEmpty()
                    val filled = char.isNotEmpty()
                    val active = index == value.length
                    OtpCell(
                        modifier = Modifier.weight(1f),
                        char = char,
                        filled = filled,
                        active = active,
                    )
                }
            }
        },
    )
}

@Composable
private fun OtpCell(
    modifier: Modifier,
    char: String,
    filled: Boolean,
    active: Boolean,
) {
    val scheme = MaterialTheme.colorScheme
    val border = if (filled || active) scheme.primary else scheme.outlineVariant
    Box(
        modifier = modifier
            .height(64.dp)
            .background(
                if (filled) scheme.primaryContainer else scheme.surfaceVariant,
                ShoplyShapes.card,
            )
            .border(2.dp, border, ShoplyShapes.card)
            .testTag(OTP_CELL_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = char,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (filled) scheme.onPrimaryContainer else scheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewLightDark
@Composable
private fun OtpInputPreview() {
    ShoplyTheme {
        var value by remember { mutableStateOf("") }
        OtpInput(
            value = value,
            onValueChange = { value = it},
            length = 6
        )
    }
}
