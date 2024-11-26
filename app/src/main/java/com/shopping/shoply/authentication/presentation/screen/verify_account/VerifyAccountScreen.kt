package com.shopping.shoply.authentication.presentation.screen.verify_account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shopping.shoply.R
import com.shopping.shoply.common.presentation.theme.ShoplyTheme

@Composable
fun VerifyAccountScreen() {
    VerifyAccountContent()
}

@Composable
private fun VerifyAccountContent() {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        val verificationCode = remember {
            mutableStateListOf(0, 1, 2, 3)
        }
        VerifyYourAccountText()
        Spacer(modifier = Modifier.height(20.dp))
        EnterVerificationCodeText()
        Spacer(modifier = Modifier.height(10.dp))
        EmailText()
        Spacer(modifier = Modifier.height(30.dp))
        VerificationCodeCircles(
            verificationCode = verificationCode,
            onNewDigitAdded = {

            }
        )
        Spacer(modifier = Modifier.height(30.dp))
        SendNewCode("00:45")
        Spacer(modifier = Modifier.height(50.dp))
        ContinueButton()
    }
}

@Composable
private fun ContinueButton() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                // Verify the entered code
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(text = stringResource(R.string.continue_))
        }
    }
}

@Composable
private fun SendNewCode(
    waitTime: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.send_a_new_code, waitTime),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        TextButton(
            onClick = {
                // Send a new code
            },
            enabled = waitTime == "00:00"
        ) {
            Text(
                text = stringResource(R.string.resend_code)
            )
        }
    }
}

@Composable
private fun VerificationCodeCircles(
    verificationCode: List<Int>,
    onNewDigitAdded: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VerificationCodeCircle(
            verificationCode,
            onNewDigitAdded
        )
        VerificationCodeCircle(
            verificationCode,
            onNewDigitAdded
        )
        VerificationCodeCircle(
            verificationCode,
            onNewDigitAdded
        )
        VerificationCodeCircle(
            verificationCode,
            onNewDigitAdded,
            imeAction = ImeAction.Done
        )
    }
}

@Composable
private fun VerificationCodeCircle(
    verificationCode: List<Int>,
    onNewDigitAdded: (Int) -> Unit,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        modifier = Modifier
            .size(60.dp),
        value = verificationCode[0].toString(),
        onValueChange = { newDigit ->
            onNewDigitAdded(newDigit.take(1).toInt())
        },
        shape = CircleShape,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        textStyle = TextStyle(
            textAlign = TextAlign.Center,
            fontSize = 30.sp
        ),
        singleLine = true
    )
}

@Composable
private fun EmailText() {
    Text(
        text = "****med@gmail.com",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun EnterVerificationCodeText() {
    Text(
        text = stringResource(R.string.enter_the_verification_code_sent_to),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun VerifyYourAccountText() {
    Text(
        text = stringResource(R.string.verify_your_account),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Preview(
    showSystemUi = true
)
@Composable
private fun VerifyAccountContentPreview() {
    ShoplyTheme {
        VerifyAccountContent()
    }
}
