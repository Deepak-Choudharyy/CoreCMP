package com.corecmp.shared.validation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corecmp.shared.theme.blackColor
import com.corecmp.shared.theme.borderBGColor
import com.corecmp.shared.theme.rejectedRedColor

@Composable
fun OtpInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    enabled: Boolean = true,
    isError: Boolean = false,
    boxSize: Int = 44,
    onComplete: ((String) -> Unit)? = null,
) {
    val focusRequester = remember { FocusRequester() }
    val digits = value.filter { it.isDigit() }.take(length)

    LaunchedEffect(digits) {
        if (digits.length == length) {
            onComplete?.invoke(digits)
        }
    }

    Box(modifier = modifier) {
        BasicTextField(
            value = digits,
            onValueChange = { input ->
                if (!enabled) return@BasicTextField
                val filtered = input.filter { it.isDigit() }.take(length)
                onValueChange(filtered)
            },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = TextStyle(color = Color.Transparent, fontSize = 1.sp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(length) { index ->
                val char = digits.getOrNull(index)?.toString().orEmpty()
                val isFocused = digits.length == index
                Box(
                    modifier = Modifier
                        .size(boxSize.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = when {
                                isError -> rejectedRedColor
                                isFocused -> MaterialTheme.colorScheme.primary
                                else -> borderBGColor
                            },
                            shape = RoundedCornerShape(8.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = char,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = blackColor,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
