package com.corecmp.shared.picker

import androidx.compose.runtime.Composable
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.PhotosUI.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

actual class PlatformMediaPicker actual constructor() {

    private var callback: ((PickedFile?) -> Unit)? = null

    private var delegateRef: NSObject? = null

    @Composable
    actual fun RegisterLaunchers() {}

    actual fun launch(
        type: PickerType,
        documentConfig: DocumentConfig?,
        onResult: (PickedFile?) -> Unit
    ) {
        callback = onResult
        val controller = com.corecmp.shared.util.topViewController() ?: return

        when (type) {
            PickerType.CAMERA -> openImagePicker(
                controller,
                UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
            )

            PickerType.IMAGE -> openPhotoPicker(controller)

            PickerType.DOCUMENT -> openDocPicker(
                controller,
                documentConfig
            )
        }
    }

    private fun openPhotoPicker(controller: UIViewController) {
        val config = PHPickerConfiguration()
        config.filter = PHPickerFilter.imagesFilter
        config.selectionLimit = 1

        val picker = PHPickerViewController(config)

        val delegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(
                picker: PHPickerViewController,
                didFinishPicking: List<*>
            ) {
                picker.dismissViewControllerAnimated(true, null)

                val result = didFinishPicking.firstOrNull() as? PHPickerResult
                if (result == null) {
                    callback?.invoke(null)
                    cleanup()
                    return
                }

                result.itemProvider.loadDataRepresentationForTypeIdentifier(
                    UTTypeImage.identifier
                ) { data, _ ->
                    dispatch_async(dispatch_get_main_queue()) {
                        val bytes = (data as? NSData)?.toByteArray()
                        callback?.invoke(
                            bytes?.let {
                                PickedFile(
                                    it,
                                    "image_${NSDate().timeIntervalSince1970}.jpg",
                                    "image/jpeg"
                                )
                            }
                        )
                        cleanup()
                    }
                }
            }
        }

        delegateRef = delegate
        picker.delegate = delegate
        controller.presentViewController(picker, true, null)
    }

    private fun openImagePicker(
        controller: UIViewController,
        type: UIImagePickerControllerSourceType
    ) {
        val picker = UIImagePickerController()
        picker.sourceType = type

        val delegate = object :
            NSObject(),
            UIImagePickerControllerDelegateProtocol,
            UINavigationControllerDelegateProtocol {

            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val image =
                    didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage

                val data = image?.let {
                    UIImageJPEGRepresentation(it, 0.8)
                }
                val bytes = data?.toByteArray()

                callback?.invoke(
                    bytes?.let {
                        PickedFile(
                            it,
                            "image_${NSDate().timeIntervalSince1970}.jpg",
                            "image/jpeg"
                        )
                    }
                )
                cleanup()
                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                callback?.invoke(null)
                cleanup()
                picker.dismissViewControllerAnimated(true, null)
            }
        }

        delegateRef = delegate
        picker.delegate = delegate
        controller.presentViewController(picker, true, null)
    }

    private fun openDocPicker(
        controller: UIViewController,
        config: DocumentConfig?
    ) {
        val types = config?.mimeTypes
            ?.mapNotNull { UTType.typeWithMIMEType(it) } ?: listOf(UTTypeData)

        val picker = UIDocumentPickerViewController(forOpeningContentTypes = types)

        val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {

            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL

                if (url != null) {
                    url.startAccessingSecurityScopedResource()
                    val data = NSData.dataWithContentsOfURL(url)
                    val bytes = data?.toByteArray()
                    callback?.invoke(
                        bytes?.let {
                            PickedFile(
                                it,
                                url.lastPathComponent,
                                null
                            )
                        }
                    )
                    url.stopAccessingSecurityScopedResource()
                } else {
                    callback?.invoke(null)
                }
                cleanup()
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                callback?.invoke(null)
                cleanup()
            }
        }

        delegateRef = delegate
        picker.delegate = delegate
        controller.presentViewController(
            picker,
            true,
            null
        )
    }

    private fun cleanup() {
        delegateRef = null
    }
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    bytes.usePinned {
        memcpy(
            it.addressOf(0),
            this.bytes,
            size.convert()
        )
    }
    return bytes
}
