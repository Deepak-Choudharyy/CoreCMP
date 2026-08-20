package com.corecmp.shared.ui.kit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.LocalCoreCmpColors
import com.corecmp.shared.validation.PasswordStrength
import com.corecmp.shared.validation.evaluatePasswordStrength

@Composable
fun PasswordStrengthMeter(
    password: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalCoreCmpColors.current
    val strength = evaluatePasswordStrength(password)
    val (label, filledBars, barColor) = when (strength) {
        PasswordStrength.WEAK -> Triple("Weak", 1, colors.error)
        PasswordStrength.MEDIUM -> Triple("Medium", 2, colors.warning)
        PasswordStrength.STRONG -> Triple("Strong", 3, colors.success)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            if (index < filledBars) barColor
                            else colors.skeletonBase,
                        ),
                )
            }
        }
        if (password.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = barColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
