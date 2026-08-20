package com.corecmp.shared.location

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.permission.AppPermission
import com.corecmp.shared.permission.PermissionManager
import com.corecmp.shared.permission.PermissionStatus
import com.corecmp.shared.ui.CommonButton
import com.corecmp.shared.ui.CommonWebView
import com.corecmp.shared.ui.GenericBottomSheet
import com.corecmp.shared.ui.OutLinedSimpleTextField
import com.corecmp.shared.theme.borderBGColor
import com.corecmp.shared.theme.grayColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun formatCoordinate(value: Double): String =
    ((value * 100000).toLong() / 100000.0).toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerBottomSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    onLocationPicked: (PlaceResult) -> Unit,
    initialLatLng: LatLng? = null,
    permissionManager: PermissionManager? = null
) {
    val scope = rememberCoroutineScope()
    val localPermissionManager = permissionManager ?: remember { PermissionManager() }

    if (permissionManager == null) {
        localPermissionManager.RegisterPermissionLauncher()
    }

    var searchQuery by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<PlaceResult>>(emptyList()) }
    var selectedPlace by remember { mutableStateOf<PlaceResult?>(null) }
    var latLng by remember { mutableStateOf<LatLng?>(initialLatLng) }
    var isSearching by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(initialLatLng) {
        initialLatLng?.let {
            when (val result = Geocoder.reverse(it.latitude, it.longitude)) {
                is GeocoderResult.Success -> {
                    selectedPlace = result.data ?: PlaceResult(
                        name = "Selected Location",
                        displayName = "Coordinates: ${formatCoordinate(it.latitude)}, ${formatCoordinate(it.longitude)}",
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                }
                is GeocoderResult.Error -> {
                    selectedPlace = PlaceResult(
                        name = "Selected Location",
                        displayName = "Coordinates: ${formatCoordinate(it.latitude)}, ${formatCoordinate(it.longitude)}",
                        latitude = it.latitude,
                        longitude = it.longitude
                    )
                }
            }
        }
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            suggestions = emptyList()
            searchError = null
            return@LaunchedEffect
        }
        val query = searchQuery
        delay(500)
        if (query != searchQuery) return@LaunchedEffect

        isSearching = true
        searchError = null
        when (val result = Geocoder.search(query)) {
            is GeocoderResult.Success -> {
                if (query == searchQuery) {
                    suggestions = result.data
                }
            }
            is GeocoderResult.Error -> {
                if (query == searchQuery) {
                    suggestions = emptyList()
                    searchError = result.message
                }
            }
        }
        isSearching = false
    }

    GenericBottomSheet(
        show = show,
        title = "Pick Location",
        onDismiss = onDismiss,
        skipPartiallyExpanded = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutLinedSimpleTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholderText = "Search for an address or place...",
                label = "Search Place",
                trailingIcon = Icons.Default.LocationOn
            )

            if (isSearching) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                }
            } else if (searchError != null) {
                Text(
                    text = searchError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            } else if (suggestions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(suggestions) { item ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedPlace = item
                                        latLng = LatLng(item.latitude, item.longitude)
                                        searchQuery = ""
                                        suggestions = emptyList()
                                    }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = item.displayName,
                                    color = grayColor,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            val currentLatLng = latLng
            if (currentLatLng != null) {
                val deltaLat = 0.003
                val deltaLon = 0.005
                val left = currentLatLng.longitude - deltaLon
                val bottom = currentLatLng.latitude - deltaLat
                val right = currentLatLng.longitude + deltaLon
                val top = currentLatLng.latitude + deltaLat

                val mapUrl = "https://www.openstreetmap.org/export/embed.html?bbox=${left}%2C${bottom}%2C${right}%2C${top}&layer=mapnik&marker=${currentLatLng.latitude}%2C${currentLatLng.longitude}"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, borderBGColor, RoundedCornerShape(10.dp))
                ) {
                    CommonWebView(
                        url = mapUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLocating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text("Locating...", fontSize = 12.sp, color = grayColor)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                isLocating = true
                                scope.launch {
                                    localPermissionManager.requestPermissions(
                                        permissions = listOf(AppPermission.LOCATION)
                                    ) { results ->               
                                        val granted = results.firstOrNull { it.permission == AppPermission.LOCATION }?.status == PermissionStatus.GRANTED
                                        if (granted) {
                                            scope.launch {
                                                CoreCmp.location.getCurrentLocation { loc ->
                                                    isLocating = false
                                                    if (loc != null) {
                                                        latLng = loc
                                                        scope.launch {
                                                            when (val reverseResult = Geocoder.reverse(loc.latitude, loc.longitude)) {
                                                                is GeocoderResult.Success -> {
                                                                    selectedPlace = reverseResult.data ?: PlaceResult(
                                                                        name = "My Location",
                                                                        displayName = "Coordinates: ${formatCoordinate(loc.latitude)}, ${formatCoordinate(loc.longitude)}",
                                                                        latitude = loc.latitude,
                                                                        longitude = loc.longitude
                                                                    )
                                                                }
                                                                is GeocoderResult.Error -> {
                                                                    selectedPlace = PlaceResult(
                                                                        name = "My Location",
                                                                        displayName = "Coordinates: ${formatCoordinate(loc.latitude)}, ${formatCoordinate(loc.longitude)}",
                                                                        latitude = loc.latitude,
                                                                        longitude = loc.longitude
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            isLocating = false
                                        }
                                    }
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "My Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Use Current Location",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                currentLatLng?.let {
                    Text(
                        text = "${formatCoordinate(it.latitude)}, ${formatCoordinate(it.longitude)}",
                        fontSize = 11.sp,
                        color = grayColor
                    )
                }
            }

            selectedPlace?.let { place ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = place.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = place.displayName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }

                CommonButton(
                    label = "Confirm Location",
                    onClick = {
                        selectedPlace?.let { onLocationPicked(it) }
                    },
                    modifier = Modifier.weight(1f).height(40.dp),
                    enabled = selectedPlace != null
                )
            }
        }
    }
}
