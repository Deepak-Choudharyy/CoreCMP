package com.corecmp.shared.ui.kit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.corecmp.shared.theme.LocalCoreCmpTypography
import com.corecmp.shared.ui.OutLinedSimpleTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "Amount",
    currencySymbol: String = "₹",
    enabled: Boolean = true,
    error: String? = null,
) {
    val typography = LocalCoreCmpTypography.current
    var display by remember(value) { mutableStateOf(formatAmountDisplay(value, currencySymbol)) }

    OutLinedSimpleTextField(
        modifier = modifier.fillMaxWidth(),
        value = display,
        onValueChange = { input ->
            val digits = input.filter { it.isDigit() || it == '.' }
            display = formatAmountDisplay(digits.removePrefix(currencySymbol), currencySymbol)
            onValueChange(digits.removePrefix(currencySymbol))
        },
        label = label,
        enabled = enabled,
        error = error,
        isTextAlignEnd = true,
        keyBoardOption = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        fontSize = typography.amount.fontSize.value.toInt(),
    )
}

private fun formatAmountDisplay(raw: String, symbol: String): String {
    if (raw.isBlank()) return ""
    return "$symbol$raw"
}
