package com.corecmp.shared.picker

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.corecmp.shared.permission.AppPermission
import com.corecmp.shared.permission.PermissionManager
import com.corecmp.shared.permission.PermissionStatus
import com.corecmp.shared.ui.GenericBottomSheet
import com.corecmp.shared.generated.resources.Res
import com.corecmp.shared.generated.resources.ic_camera
import com.corecmp.shared.generated.resources.ic_documents
import com.corecmp.shared.generated.resources.ic_photos
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonAttachmentBottomSheet(
    permissions: List<AppPermission>,
    onFilePicked: (PickedFile?) -> Unit
) {
    val scope = rememberCoroutineScope()
    val picker = remember { PlatformMediaPicker() }
    picker.RegisterLaunchers()
    val permissionManager = remember { PermissionManager() }
    permissionManager.RegisterPermissionLauncher()
    GenericBottomSheet(
        show = true,
        onDismiss = {onFilePicked(null)},
        title = "Upload Document"
    ){
        AttachmentOptionsUI(
            showCamera = permissions.contains(AppPermission.CAMERA),
            showGallery = permissions.contains(AppPermission.GALLERY),
            showDocument = permissions.contains(AppPermission.STORAGE),
            onCamera = {
                scope.launch {
                    requestPermissionAndPick(
                        permissionManager,
                        AppPermission.CAMERA
                    ) { picker.launch(PickerType.CAMERA) { onFilePicked(it) } }
                }
            },
            onGallery = {
                picker.launch(PickerType.IMAGE) { onFilePicked(it) }
            },
            onDocument = {
                picker.launch(
                    PickerType.DOCUMENT,
                    DocumentConfig(
                        mimeTypes = listOf(
                            "application/pdf",
                            "image/*"
                        )
                    )
                ) { onFilePicked(it) }
            }
        )
    }
}

suspend fun requestPermissionAndPick(
    manager: PermissionManager,
    permission: AppPermission,
    onGranted: () -> Unit
) {
    manager.requestPermissions(
        listOf(permission)
    ) { result ->
        val granted = result.firstOrNull()?.status == PermissionStatus.GRANTED
        if (granted) {
            onGranted()
        }
    }
}

@Composable
fun AttachmentOptionsUI(
    showCamera: Boolean,
    showGallery: Boolean,
    showDocument: Boolean,
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onDocument: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        if (showCamera)
            AttachmentItem(Res.drawable.ic_camera, "Take Photo", onCamera)


        if (showGallery)
            AttachmentItem(Res.drawable.ic_photos, "Photos", onGallery)

        if (showDocument)
            AttachmentItem(Res.drawable.ic_documents, "Documents", onDocument)
    }
}

@Composable
fun AttachmentItem(icon: DrawableResource, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(54.dp)
        )
        Text(label, fontSize = 14.sp, color = Color.Black)
    }
}