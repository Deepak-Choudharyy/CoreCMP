package com.corecmp.shared.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corecmp.shared.theme.blackColor
import com.corecmp.shared.theme.borderBGColor
import com.corecmp.shared.theme.grayColor
import com.corecmp.shared.theme.rejectedRedColor
import com.corecmp.shared.theme.transparentColor


@ExperimentalMaterial3Api
@Composable
fun OutLinedSimpleTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String = "",
    isTransparent: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    leadingImage: Painter? = null,
    onLeadingClicked: () -> Unit = {},
    onTrailingClicked: () -> Unit = {},
    onClick: () -> Unit = {},
    keyBoardOption: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done
    ),
    isTextAlignEnd: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    fontSize: Int = 12,
    trailingIcon: ImageVector? = null,
    trailingImage: Painter? = null,
    borderColor: Color = borderBGColor,
    radius: Int = 10,
    placeHolderColor: Color = grayColor,
    trailingIconTine: Color? = grayColor,
    leadingIconTine: Color? = grayColor,
    placeHolderFontWeight: FontWeight = FontWeight.W400,
    maxLines: Int = 1,
    leadingIconSize: Int = 17,
    trailingIconSize: Int = 20,
    error: String? = null,
    label: String? = null,
    columnPadding: PaddingValues = PaddingValues(0.dp),
    isTextAlignCenter: Boolean = false,
    columnModifier: Modifier = Modifier,
    labelColor: Color = blackColor,
    disabledValueContent: (@Composable () -> Unit)? = null
) {

    val startPadding = if (leadingImage != null || leadingIcon != null) 10.dp else 0.dp
    val endPadding = if (trailingImage != null || trailingIcon != null) 30.dp else 0.dp
    Column(
        modifier = columnModifier.padding(columnPadding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {

        label?.let { title ->
            Text(
                text = title,
                color = labelColor,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = fontSize.sp)
            )
        }
        BasicTextField(
            value = value,
            onValueChange = { if (enabled) onValueChange(it) },
            modifier = modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isTransparent) Color.Transparent else borderColor,
                    shape = RoundedCornerShape(radius.dp)
                )
                .then(
                    Modifier.clickable(
                        enabled = true, // 🔥 IMPORTANT
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onClick()
                    }
                )
                .background(transparentColor),
            textStyle = TextStyle(
                color = blackColor,
                fontSize = fontSize.sp,
                textAlign = if (isTextAlignEnd) TextAlign.End else TextAlign.Start
            ),
            maxLines = maxLines,
            readOnly = !enabled,
            keyboardOptions = keyBoardOption,
            visualTransformation = visualTransformation,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = endPadding),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (leadingImage != null) {
                            if (leadingIconTine != null) {
                                Image(
                                    painter = leadingImage,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(leadingIconSize.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                onLeadingClicked.invoke()
                                            }
                                        ),
                                    colorFilter = ColorFilter.tint(leadingIconTine)
                                )
                            } else {
                                Image(
                                    painter = leadingImage,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(leadingIconSize.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                onLeadingClicked.invoke()
                                            }
                                        )
                                )
                            }
                        } else {
                            if (leadingIcon != null) {
                                if (leadingIconTine != null) {
                                    Image(
                                        imageVector = leadingIcon,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(leadingIconSize.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = {
                                                    onLeadingClicked.invoke()
                                                }
                                            ),
                                        colorFilter = ColorFilter.tint(leadingIconTine)
                                    )
                                } else {
                                    Image(
                                        imageVector = leadingIcon,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(leadingIconSize.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = {
                                                    onLeadingClicked.invoke()
                                                }
                                            )
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = startPadding),
                            contentAlignment = if (isTextAlignEnd) Alignment.CenterEnd else if (isTextAlignCenter) Alignment.Center else Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isTextAlignEnd) Alignment.CenterEnd
                                else if (isTextAlignCenter) Alignment.Center
                                else Alignment.CenterStart
                            ) {
                                when {
                                    !enabled && disabledValueContent != null -> disabledValueContent()
                                    value.isEmpty() -> {
                                        Text(
                                            text = placeholderText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = TextStyle(
                                                color = placeHolderColor,
                                                fontSize = fontSize.sp,
                                                fontWeight = placeHolderFontWeight,
                                                letterSpacing = 0.05.sp
                                            )
                                        )
                                    }
                                    !enabled -> {
                                        Text(
                                            text = value,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = TextStyle(
                                                color = blackColor,
                                                fontSize = fontSize.sp,
                                                textAlign = if (isTextAlignEnd) TextAlign.End
                                                else if (isTextAlignCenter) TextAlign.Center
                                                else TextAlign.Start
                                            )
                                        )
                                    }
                                    else -> innerTextField()
                                }
                            }
                        }
                    }

                    if (trailingImage != null) {
                        if (trailingIconTine != null) {
                            Image(
                                painter = trailingImage,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(trailingIconSize.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            onTrailingClicked.invoke()
                                        }
                                    ),
                                colorFilter = ColorFilter.tint(trailingIconTine)
                            )
                        } else {
                            Image(
                                painter = trailingImage,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(trailingIconSize.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            onTrailingClicked.invoke()
                                        }
                                    )
                            )
                        }
                    } else {
                        if (trailingIcon != null) {
                            if (trailingIconTine != null) {
                                Image(
                                    imageVector = trailingIcon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(trailingIconSize.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                onTrailingClicked.invoke()
                                            }
                                        ),
                                    colorFilter = ColorFilter.tint(trailingIconTine)
                                )
                            } else {
                                Image(
                                    imageVector = trailingIcon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(trailingIconSize.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = {
                                                onTrailingClicked.invoke()
                                            }
                                        )
                                )
                            }
                        }
                    }
                }
            }
        )
        error?.let { msg ->
            Text(
                text = msg,
                color = rejectedRedColor,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
            )
        }
    }
}