package com.corecmp.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.bottomSheetHeaderBackGround
import com.corecmp.shared.theme.whiteColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericBottomSheet(
    show: Boolean,
    title: String? = null,
    titleStyle : TextStyle = MaterialTheme.typography.bodyMedium,
    skipPartiallyExpanded: Boolean = true,
    titleBackGround : Color = bottomSheetHeaderBackGround,
    background : Color = whiteColor,
    onDismiss: () -> Unit,
    closeIcon : Any = Placeholder.VectorResource(Icons.Default.Close),
    content: @Composable () -> Unit
) {
    if (!show) return
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var sheetY by remember { mutableStateOf(0f) }
    var showPopup by remember { mutableStateOf(false) }

    val bottomSheetSnackbarListener = remember {
        { snackbar: AppSnackbar? ->
            if (snackbar != null) {
                showPopup = true
            }
        }
    }

    DisposableEffect(bottomSheetSnackbarListener) {
        AppSnackbarManager.registerListener(bottomSheetSnackbarListener)
        onDispose {
            AppSnackbarManager.unregisterListener(bottomSheetSnackbarListener)
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y > 0f) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }
                return Offset.Zero
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = background,
        dragHandle = null,
        sheetGesturesEnabled = false,
        shape = RoundedCornerShape(topEnd = 14.dp, topStart = 14.dp),
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = false,
            shouldDismissOnClickOutside = false
        ),
        scrimColor = BottomSheetDefaults.ScrimColor
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    sheetY = coordinates.positionInWindow().y
                }
                .nestedScroll(nestedScrollConnection)
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                title?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(titleBackGround)
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = it,
                            style = titleStyle,
                            modifier = Modifier.fillMaxWidth(.8f),
                            textAlign = TextAlign.Start,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        CustomImage(
                            model = closeIcon,
                            placeholder = Placeholder.VectorResource(Icons.Default.Close),
                            modifier = Modifier
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onDismiss
                                ),
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                ) {
                    content()
                }
            }
        }

        if (showPopup) {
             androidx.compose.ui.window.Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(x = 0, y = -sheetY.toInt()),
                properties = androidx.compose.ui.window.PopupProperties(
                    focusable = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    SnackBarBoxApp(
                        brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
                        onVisibilityChanged = { isVisible ->
                            if (!isVisible) {
                                showPopup = false
                            }
                        }
                    ) {
                        // Empty content - SnackBarBoxApp manages displaying the snackbars internally
                    }
                }
            }
        }
    }
}


val CloseIcon: ImageVector
    get() {
        if (_closeIcon != null) {
            return _closeIcon!!
        }

        _closeIcon = ImageVector.Builder(
            name = "CloseIcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {

            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                pathFillType = PathFillType.NonZero
            ) {

                moveTo(18f, 6f)
                lineTo(6f, 18f)

                moveTo(6f, 6f)
                lineTo(18f, 18f)
            }

        }.build()

        return _closeIcon!!
    }

private var _closeIcon: ImageVector? = null
