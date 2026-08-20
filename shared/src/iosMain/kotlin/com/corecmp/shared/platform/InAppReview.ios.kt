package com.corecmp.shared.platform

import platform.StoreKit.SKStoreReviewController

actual fun requestInAppReview() {
    SKStoreReviewController.requestReview()
}
