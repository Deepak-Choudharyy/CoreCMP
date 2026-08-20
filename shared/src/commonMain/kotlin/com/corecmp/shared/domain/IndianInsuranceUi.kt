package com.corecmp.shared.domain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.corecmp.shared.extension.toIndianCurrency
import com.corecmp.shared.ui.kit.CountdownTimer
import com.corecmp.shared.ui.kit.KeyValueRow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

data class PremiumBreakdown(
    val ownDamage: Double,
    val thirdParty: Double,
    val addons: Double = 0.0,
    val gstPercent: Double = 18.0,
) {
    val subtotal: Double get() = ownDamage + thirdParty + addons
    val gst: Double get() = gstAmount(subtotal, gstPercent)
    val total: Double get() = (subtotal + gst * 100.0).toLong() / 100.0
}

data class KycDocument(
    val name: String,
    val isSubmitted: Boolean,
)

val NOMINEE_RELATIONSHIPS = listOf(
    "Spouse", "Father", "Mother", "Son", "Daughter", "Brother", "Sister", "Other",
)

@Composable
fun PremiumBreakdownCard(
    breakdown: PremiumBreakdown,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Premium Breakdown", style = MaterialTheme.typography.titleMedium)
            KeyValueRow("Own Damage", breakdown.ownDamage.toIndianCurrency())
            KeyValueRow("Third Party", breakdown.thirdParty.toIndianCurrency())
            if (breakdown.addons > 0) KeyValueRow("Add-ons", breakdown.addons.toIndianCurrency())
            KeyValueRow("GST (${breakdown.gstPercent.toInt()}%)", breakdown.gst.toIndianCurrency())
            KeyValueRow("Total", breakdown.total.toIndianCurrency())
        }
    }
}

@Composable
fun PolicyExpiryCountdown(
    expiryDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text("Policy Renewal", style = MaterialTheme.typography.titleSmall)
        CountdownTimer(
            targetEpochMs = expiryDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
fun KycChecklist(
    documents: List<KycDocument>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("KYC Documents", style = MaterialTheme.typography.titleMedium)
        documents.forEach { doc ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (doc.isSubmitted) "✓" else "○",
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text(doc.name)
            }
        }
    }
}

@Composable
fun CommissionCard(
    premium: Double,
    commissionPercent: Double,
    modifier: Modifier = Modifier,
) {
    val payout = (premium * commissionPercent / 100.0 * 100.0).toLong() / 100.0
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Commission", style = MaterialTheme.typography.titleMedium)
            KeyValueRow("Premium", premium.toIndianCurrency())
            KeyValueRow("Rate", "${commissionPercent.toInt()}%")
            KeyValueRow("Payout", payout.toIndianCurrency())
        }
    }
}
