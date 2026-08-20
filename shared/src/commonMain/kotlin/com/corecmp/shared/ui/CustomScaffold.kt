package com.corecmp.shared.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.corecmp.shared.theme.blackColor
import com.corecmp.shared.theme.transparentColor
import com.corecmp.shared.theme.whiteColor
import androidx.compose.ui.platform.LocalInspectionMode
import com.corecmp.shared.CoreCmp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomScaffold(
    title: String ?= null,
    showBack: Boolean = false,
    onBackClick: () -> Unit = {},
    action1Click: () -> Unit = {},
    action2Click: () -> Unit = {},
    action1Img: Any? = null,
    action2Img: Any? = null,
    backImg: Any? = null,
    isLoading: Boolean = false,
    titleColor: Color = blackColor,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    iconSize: Int = 35,
    backIconStartPadding : PaddingValues = PaddingValues(start = 4.dp),
    actionImg1Padding : PaddingValues = PaddingValues(start = 4.dp),
    actionImg2Padding : PaddingValues = PaddingValues(start = 4.dp),
    gradient: Brush = screenGradientColor,
    loading: (@Composable () -> Unit)? = null,
    loadingLottieUrl : Placeholder = CoreCmp.defaultApiLoadingPlaceholder,
    bottomBar: (@Composable () -> Unit)? = null,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {

    val isPreview = LocalInspectionMode.current
    val density = LocalDensity.current

    if (!isPreview) {
        LocalWindowInfo.current.containerSize.height.dp

        val keyboardHeightPx = WindowInsets.ime.getBottom(density)

        val screenHeightPx = LocalWindowInfo.current.containerSize.height

        with(density) {

            (screenHeightPx - keyboardHeightPx).toDp()

        }
    }



    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if(title != null) {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            color = titleColor,
                            style = titleStyle
                        )
                    },
                    navigationIcon = {
                        if (showBack) {
                            CustomImage(
                                model = backImg,
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onBackClick
                                    )
                                    .size(iconSize.dp).padding(backIconStartPadding)
                            )
                        }
                    },
                    actions = {
                        Row(
                            modifier = Modifier,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (action1Img != null) {
                                CustomImage(
                                    model = action1Img,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = action1Click
                                        )
                                        .size(iconSize.dp).padding(actionImg1Padding)
                                )
                            }
                            if (action2Img != null) {
                                CustomImage(
                                    model = action2Img,
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = action2Click
                                        )
                                        .size(iconSize.dp).padding(actionImg2Padding)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = transparentColor,
                        titleContentColor = whiteColor
                    )
                )
            }
        },
        bottomBar = {
            if (bottomBar != null) {
                bottomBar()
            }
        },
        floatingActionButton = {
            if (floatingActionButton != null) {
                floatingActionButton()
            }
        },
        containerColor = transparentColor,
        modifier = Modifier.background(gradient)
    ) { padding ->
        val imeBottom = WindowInsets.ime
            .asPaddingValues()
            .calculateBottomPadding()

        val bottomSpace = if (imeBottom > 0.dp) 0.dp
        else padding.calculateBottomPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(
                        bottom = bottomSpace,
                        start = padding.calculateRightPadding(LayoutDirection.Rtl),
                        end = padding.calculateEndPadding(LayoutDirection.Rtl)
                    )
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    content(PaddingValues(0.dp))
                }
            }
            if (isLoading) {
                if (loading == null) {
                    CustomLoading(loadingLottieUrl)
                } else {
                    loading()
                }
            }
        }
    }
}