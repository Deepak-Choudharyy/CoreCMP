package com.corecmp.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.corecmp.shared.theme.blackColor
import com.corecmp.shared.theme.borderBGColor
import com.corecmp.shared.theme.grayColor
import com.corecmp.shared.theme.rejectedRedColor
import com.corecmp.shared.theme.whiteColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CommonDropDown(
    label: String ?= null,
    placeholder: String = "",
    items: List<T> = emptyList(),
    selectedItem: T? = null,
    selectedItems: List<T> = emptyList(),
    isMultiSelect: Boolean = false,
    isSelectAll: Boolean = false,
    itemLabel: ((T) -> String)? = null,
    onItemSelected: (T?) -> Unit = {},
    onType: (String) -> Unit = {},
    onItemsSelected: (List<T>) -> Unit = {},
    onClick: () -> Unit = {},
    trailingIcon: ImageVector? = null,
    trailingImage: Painter? = null,
    trailingIconTint: Color = blackColor,
    placeholderColor: Color = grayColor,
    error: String? = null,
    showSearchForcefully: Boolean = false,
    showFullList: Boolean = false,
    /** Wrap dialog height to list when count is below this and search is off. Use 0 for legacy fixed height. */
    compactDialogBelowItemCount: Int = 7,
    modifier: Modifier = Modifier,
    dropDownBackGround : Color = whiteColor,
    labelColor :Color = blackColor
) {
    val rootModifier = modifier
    BoxWithConstraints(modifier = rootModifier) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        val trailingReserve = when {
            trailingImage != null -> 72.dp
            trailingIcon != null -> 64.dp
            else -> 48.dp
        }
        val availableWidthPx = with(density) {
            when {
                maxWidth == Dp.Unspecified || maxWidth == Dp.Infinity -> null
                else -> (maxWidth - trailingReserve).toPx().coerceAtLeast(0f)
            }
        }

        val valueTextStyle = TextStyle(fontSize = 12.sp, color = blackColor)

        fun measureTextWidth(text: String): Float =
            textMeasurer.measure(text = text, style = valueTextStyle).size.width.toFloat()

        fun truncateWithEllipsis(text: String, maxWidthPx: Float): String {
            if (text.isEmpty() || measureTextWidth(text) <= maxWidthPx) return text

            var truncated = text
            while (truncated.isNotEmpty()) {
                val candidate = "$truncated..."
                if (measureTextWidth(candidate) <= maxWidthPx) return candidate
                truncated = truncated.dropLast(1)
            }
            return "..."
        }

        fun formatLabel(text: String): String = text.replace("_", " ")

        data class MultiSelectDisplay(val prefix: String, val badge: String)

        fun computeMultiSelectDisplay(labels: List<String>, widthPx: Float?): MultiSelectDisplay {
            if (labels.isEmpty()) return MultiSelectDisplay("", "")
            if (labels.size == 1) return MultiSelectDisplay(labels.first(), "")

            if (widthPx == null || widthPx <= 0f) {
                val visibleCount = (labels.size - 1).coerceAtLeast(1)
                val hiddenCount = labels.size - visibleCount
                return MultiSelectDisplay(
                    prefix = labels.take(visibleCount).joinToString(", "),
                    badge = "+$hiddenCount"
                )
            }

            val allJoined = labels.joinToString(", ")
            if (measureTextWidth(allJoined) <= widthPx) {
                return MultiSelectDisplay(allJoined, "")
            }

            // Prefer the largest prefix that fits with +N badge (no mid-label ellipsis when possible).
            var bestFit: MultiSelectDisplay? = null
            for (visibleCount in 1 until labels.size) {
                val hiddenCount = labels.size - visibleCount
                val badge = "+$hiddenCount"
                val badgeWidth = measureTextWidth(badge)
                val prefixBudget = (widthPx - badgeWidth).coerceAtLeast(0f)
                val joinedPrefix = labels.take(visibleCount).joinToString(", ")

                if (measureTextWidth(joinedPrefix) <= prefixBudget) {
                    bestFit = MultiSelectDisplay(joinedPrefix, badge)
                } else {
                    break
                }
            }

            if (bestFit != null) {
                return bestFit
            }

            val badge = "+${labels.size - 1}"
            val badgeWidth = measureTextWidth(badge)
            val prefixBudget = (widthPx - badgeWidth).coerceAtLeast(0f)
            val firstLabel = truncateWithEllipsis(labels.first(), prefixBudget)
            return if (firstLabel.isNotEmpty() && firstLabel != "...") {
                MultiSelectDisplay(firstLabel, badge)
            } else {
                MultiSelectDisplay("", badge)
            }
        }

        fun formatSelectedItems(items: List<T>): String {
            val labels = items.map { formatLabel(itemLabel?.invoke(it) ?: it.toString()) }
            val display = computeMultiSelectDisplay(labels, availableWidthPx)
            return when {
                labels.isEmpty() -> ""
                labels.size == 1 -> labels.first()
                display.badge.isEmpty() -> display.prefix
                else -> "${display.prefix}${display.badge}"
            }
        }

        fun formatSingleItem(item: T): String =
            formatLabel(itemLabel?.invoke(item) ?: item.toString())

        val selectedLabels = remember(selectedItems, selectedItem, itemLabel, isMultiSelect) {
            if (isMultiSelect) {
                selectedItems.map { formatLabel(itemLabel?.invoke(it) ?: it.toString()) }
            } else {
                selectedItem?.let { listOf(formatSingleItem(it)) } ?: emptyList()
            }
        }

        val multiSelectDisplay = remember(
            selectedLabels,
            availableWidthPx,
            trailingImage,
            trailingIcon
        ) {
            computeMultiSelectDisplay(selectedLabels, availableWidthPx)
        }

        val showMultiBadge = isMultiSelect && selectedLabels.size > 1

        var showDialog by remember { mutableStateOf(false) }
        var searchText by remember { mutableStateOf("") }
        var displayText by remember { mutableStateOf("") }
        var tempSelectedItems by remember { mutableStateOf(emptySet<T>()) }
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current

        LaunchedEffect(selectedItem, selectedItems) {
            displayText = if (isMultiSelect) {
                formatSelectedItems(selectedItems)
            } else {
                selectedItem?.let { formatSingleItem(it) } ?: ""
            }
        }
        LaunchedEffect(showDialog) {
            if (showDialog) {
                tempSelectedItems = selectedItems.toMutableSet()
            }
        }

        val showSearch = items.size > 10

        val filteredItems by remember(searchText, items) {
            derivedStateOf {
                val filtered = if (searchText.isBlank()) items
                else
                    items.filter { item ->
                        val label =
                            itemLabel?.invoke(item) ?: item.toString() // Logic yahan bhi same
                        label.contains(searchText, ignoreCase = true)
                    }
                if (showFullList) filtered
                else filtered.take(50)
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutLinedSimpleTextField(
                    value = if (showMultiBadge) "" else displayText,
                    label = label,
                    onValueChange = {},
                    placeholderText = placeholder.ifBlank { if (label.isNullOrBlank()) "" else "Select ${label.lowercase()}" },
                    placeHolderColor = placeholderColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(dropDownBackGround, RoundedCornerShape(6.dp)),
                    trailingIcon = trailingIcon ?: DropdownArrowIcon,
                    trailingIconTine = trailingIconTint,
                    trailingImage = trailingImage,
                    borderColor = if (error != null) rejectedRedColor
                    else borderBGColor,
                    radius = 6,
                    enabled = false,
                    labelColor = labelColor,
                    disabledValueContent = if (showMultiBadge) {
                        {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = multiSelectDisplay.prefix.ifEmpty { " " },
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = false,
                                    style = valueTextStyle
                                )
                                if (multiSelectDisplay.badge.isNotEmpty()) {
                                    Text(
                                        text = multiSelectDisplay.badge,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = valueTextStyle
                                    )
                                }
                            }
                        }
                    } else null
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            searchText = ""
                            if (items.isNotEmpty())
                                showDialog = true
                            onClick()
                        }
                )
            }

            if (showDialog) {
                val hasSearch = showSearch || showSearchForcefully
                val useCompactDialog = compactDialogBelowItemCount > 0 &&
                    !hasSearch &&
                    filteredItems.size < compactDialogBelowItemCount
                val dialogScrollState = rememberScrollState()
                Dialog(
                    onDismissRequest = {
                        showDialog = false
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        // Match pre-merge dialog width; false + fillMaxWidth(0.9f) made rows look broken
                        usePlatformDefaultWidth = true
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize() // Pura screen cover karega
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    showDialog = false // Backdrop click par band hoga
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .then(
                                    if (hasSearch || !useCompactDialog) {
                                        Modifier.heightIn(min = 250.dp, max = 600.dp)
                                    } else {
                                        Modifier.heightIn(max = 600.dp)
                                    }
                                )
                                .background(whiteColor, RoundedCornerShape(12.dp))
                                .pointerInput(Unit) { detectTapGestures { } }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (hasSearch || !useCompactDialog) {
                                            Modifier.fillMaxHeight()
                                        } else {
                                            Modifier.wrapContentHeight()
                                        }
                                    )
                                    .padding(16.dp)
                            ) {

                                Text(
                                    text = if (label.isNullOrBlank()) {
                                        "Select Item"
                                    } else {
                                        "Select $label"
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )

                                if (hasSearch) {

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutLinedSimpleTextField(
                                        value = searchText,
                                        onValueChange = {
                                            searchText = it
                                            onType(it)
                                        },
                                        placeholderText = "Search...",
                                        modifier = Modifier.fillMaxWidth(),
                                        radius = 6
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = if (hasSearch || !useCompactDialog) {
                                        Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                    } else {
                                        Modifier
                                            .weight(1f, fill = false)
                                            .fillMaxWidth()
                                    }
                                ) {
                                    Column(
                                        modifier = if (hasSearch || !useCompactDialog) {
                                            Modifier
                                                .fillMaxSize()
                                                .verticalScroll(dialogScrollState)
                                        } else {
                                            Modifier
                                                .fillMaxWidth()
                                                .verticalScroll(dialogScrollState)
                                        }
                                    ) {

                                        if (filteredItems.isEmpty()) {

                                            Text(
                                                text = "No match found",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                color = grayColor,
                                                fontSize = 12.sp
                                            )

                                        } else {
                                            if (isMultiSelect && isSelectAll && items.size >= 3) {
                                                val isAllSelected = items.all { tempSelectedItems.contains(it) }
                                                val primaryColor = MaterialTheme.colorScheme.primary
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 6.dp)
                                                        .background(
                                                            color = primaryColor.copy(alpha = 0.06f),
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable {
                                                            tempSelectedItems = if (isAllSelected) {
                                                                emptySet()
                                                            } else {
                                                                items.toSet()
                                                            }
                                                        }
                                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {

                                                    CustomCheckbox(
                                                        checked = isAllSelected,
                                                        onCheckedChange = { checked ->
                                                            tempSelectedItems = if (checked) {
                                                                items.toSet()
                                                            } else {
                                                                emptySet()
                                                            }
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Text(
                                                        text = if (isAllSelected) "Unselect All" else "Select All",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = FontWeight.Medium,
                                                            color = primaryColor
                                                        )
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }

                                            filteredItems.forEachIndexed { index, item ->

                                                val isSelected =
                                                    if (isMultiSelect) {
                                                        tempSelectedItems.contains(item)
                                                    } else {
                                                        item == selectedItem
                                                    }

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            if (isSelected) {
                                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                                            } else {
                                                                Color.Transparent
                                                            }
                                                        )
                                                        .clickable {

                                                            if (isMultiSelect) {

                                                                tempSelectedItems =
                                                                    if (tempSelectedItems.contains(item)) {
                                                                        tempSelectedItems - item
                                                                    } else {
                                                                        tempSelectedItems + item
                                                                    }

                                                            } else {

                                                                onItemSelected(item)
                                                                displayText = formatSingleItem(item)
                                                                showDialog = false
                                                            }
                                                        }
                                                        .padding(14.dp),
                                                    horizontalArrangement = Arrangement.Start,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {

                                                    if (isMultiSelect) {

                                                        CustomCheckbox(
                                                            checked = isSelected,
                                                            onCheckedChange = {
                                                                tempSelectedItems =
                                                                    if (it) {
                                                                        tempSelectedItems + item
                                                                    } else {
                                                                        tempSelectedItems - item
                                                                    }
                                                            }
                                                        )

                                                        Spacer(modifier = Modifier.width(8.dp))
                                                    }

                                                    Text(
                                                        text = (itemLabel?.invoke(item)
                                                            ?: item.toString()).replace("_", " "),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (isSelected) {
                                                            MaterialTheme.colorScheme.primary
                                                        } else {
                                                            Color.Black
                                                        }
                                                    )
                                                }

                                                if (index < filteredItems.lastIndex) {
                                                    HorizontalDivider()
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                HorizontalDivider()

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    if (
                                        (isMultiSelect && tempSelectedItems.isNotEmpty()) ||
                                        (!isMultiSelect && selectedItem != null)
                                    ) {

                                        Text(
                                            text = "Reset",
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .clickable {

                                                    displayText = ""

                                                    if (isMultiSelect) {

                                                        tempSelectedItems = emptySet()
                                                        onItemsSelected(emptyList())

                                                    } else {

                                                        onItemSelected(null)
                                                    }

                                                    showDialog = false
                                                }
                                                .padding(8.dp)
                                        )

                                    } else {

                                        Spacer(modifier = Modifier)
                                    }

                                    Row {

                                        Text(
                                            text = "Cancel",
                                            color = Color.Gray,
                                            modifier = Modifier
                                                .clickable {
                                                    showDialog = false
                                                }
                                                .padding(8.dp)
                                        )

                                        if (isMultiSelect) {

                                            Text(
                                                text = "Done",
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .clickable {

                                                        val result =
                                                            tempSelectedItems.toList()

                                                        onItemsSelected(result)

                                                        displayText =
                                                            formatSelectedItems(result)

                                                        showDialog = false
                                                    }
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            if (error != null) {
                Text(
                    text = error,
                    color = rejectedRedColor,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                )
            }
        }
    }
}



val DropdownArrowIcon: ImageVector
    get() {
        if (_dropdownArrowIcon != null) {
            return _dropdownArrowIcon!!
        }
        _dropdownArrowIcon = ImageVector.Builder(
            name = "DropdownArrow",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {

            path(
                fill = SolidColor(Color.Black),
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(7f, 10f)
                lineTo(12f, 15f)
                lineTo(17f, 10f)
                close()
            }

        }.build()

        return _dropdownArrowIcon!!
    }

fun String.customToTitleCase(): String {
    return this.lowercase()
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
private var _dropdownArrowIcon: ImageVector? = null
