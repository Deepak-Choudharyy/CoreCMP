# CoreCmp Future Release Backlog & Technical Roadmap

This document outlines the detailed technical strategies to improve the planned features in [futurerelease.md](file:///Users/aj/Desktop/Library/coreCmp/futurerelease.md), along with new proposed high-value features to make CoreCmp a premium, feature-rich Kotlin Multiplatform library.

---

## 1. Native iOS Permissions Implementation
*   **Current State**: Moked implementation in [PermissionManager.ios.kt](file:///Users/aj/Desktop/Library/coreCmp/shared/src/iosMain/kotlin/com/corecmp/shared/permission/PermissionManager.ios.kt) immediately returns `PermissionStatus.GRANTED`.
*   **Technical Strategy to Improve**:
    *   **Camera & Microphone**: Import `platform.AVFoundation.*` and check/request authorization using `AVCaptureDevice.authorizationStatusForMediaType()` and `AVCaptureDevice.requestAccessForMediaType()`.
    *   **Gallery/Photos**: Import `platform.Photos.*` and use `PHPhotoLibrary.authorizationStatus()` and `PHPhotoLibrary.requestAuthorization()`.
    *   **Location**: Import `platform.CoreLocation.*`, instantiate `CLLocationManager`, and override the delegate protocol (`CLLocationManagerDelegateProtocol`) to handle permission changes dynamically.
    *   **Notifications**: Import `platform.UserNotifications.*` and use `UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions()`.
*   **Info plist Note**: Provide a guide/warnings in documentation reminding developers to add corresponding NSUsageDescription keys (e.g. `NSCameraUsageDescription`, `NSLocationWhenInUseUsageDescription`) to their iOS target `Info.plist`.

---

## 2. JVM Secure Storage Encryption
*   **Current State**: Plaintext `java.util.prefs.Preferences` in [SecureStorage.jvm.kt](file:///Users/aj/Desktop/Library/coreCmp/shared/src/jvmMain/kotlin/com/corecmp/shared/storage/SecureStorage.jvm.kt).
*   **Technical Strategy to Improve**:
    *   **Keyring Integration**: Leverage native bindings to access keychains:
        *   **macOS**: Native Keychain via Security Framework.
        *   **Windows**: Data Protection API (DPAPI).
        *   **Linux**: Libsecret or Gnome Keyring.
    *   **Fallback Symmetric Encryption**: If OS Keyring access is restricted, encrypt stored settings files using AES-256-GCM. The key can be derived from system properties (e.g. OS user UUID, motherboard UUID) using PBKDF2.

---

## 3. Structured Logging (Multiplatform)
*   **Current State**: Raw `println` statements in [ApiClient.kt](file:///Users/aj/Desktop/Library/coreCmp/shared/src/commonMain/kotlin/com/corecmp/shared/api/ApiClient.kt).
*   **Technical Strategy to Improve**:
    *   **Log Adapter Engine**: Implement a plugin-like logger using `co.touchlab:kermit`. Kermit natively maps logs to Android Logcat, iOS NSLogger, and standard Java output on JVM.
    *   **Redaction Filter**: Add an interceptor to sanitize and strip sensitive authorization tokens or user credentials (such as password inputs) before printing them to the logs.

---

## 4. OAuth & Token Auto-Refresh Interceptor
*   **Current State**: Tokens must be manually updated via `ApiConfig.updateToken`.
*   **Technical Strategy to Improve**:
    *   **Ktor Auth Plugin**: Hook into Ktor's `BearerAuthProvider` which supports token refreshing automatically.
    *   **Thread-Safe Mutex**: Ensure that if multiple concurrent API calls get a `401 Unauthorized` response, only a single token-refresh API request is fired. Other suspended requests should wait for the new token and resume once it's acquired.

---

## 5. UI Customization & Theme Injection (Design System)
*   **Current State**: UI widgets use hardcoded custom theme colors (like `whiteColor`, `borderBGColor`, etc.).
*   **Technical Strategy to Improve**:
    *   **CoreCmpTheme Wrapper**: Define an `CoreCmpTheme` composable wrapper. Developers can wrap their layouts in `CoreCmpTheme(colors = CoreCmpColors(...))` to override widget colors.
    *   **Material 3 Fallback**: Ensure that if no custom colors are provided, widgets automatically fall back to the host project's default `MaterialTheme.colorScheme` properties.

---

## 6. Form Validation & Input Masks
*   **Current State**: TextFields require manual string checking.
*   **Technical Strategy to Improve**:
    *   **FormState Tracker**: Create a `FormState` manager holding rules for email validation, password strength, matching fields, and maximum/minimum length.
    *   **VisualTransformation Masks**: Implement custom `VisualTransformation` classes for formatting fields dynamically as user types (e.g. phone numbers like `(XXX) XXX-XXXX` or credit cards `XXXX XXXX XXXX XXXX`).

---

## 7. Reusable Image Cropper Component
*   **Current State**: Image picker directly returns raw picked file bytes.
*   **Technical Strategy to Improve**:
    *   **Cropping Canvas**: Build a Compose Canvas overlay showing a custom cropping box (Rectangle, Square, or Circle).
    *   **Gestures Handler**: Listen to pointer gestures (zoom, pan, drag) to translate and scale the image inside the crop bounds.
    *   **Platform Rendering**: Crop image on the platform side to avoid memory overflow (e.g., using `Bitmap.createBitmap` on Android and CoreGraphics `CGImageCreateWithImageInRect` on iOS).

---

## 8. Pagination (Paging Support)
*   **Current State**: Not implemented.
*   **Technical Strategy to Improve**:
    *   **PagingFlow Helper**: Implement a lightweight paging helper inside `ApiClient`.
    *   **LazyList Scroll Trigger**: Expose a Composable helper extension `LazyListState.OnScrollEnd(threshold = 3) { loadNextPage() }` to trigger new API requests smoothly as the user nears the end of list.

---

# New Proposed Features (High-Value Additions)

We propose adding the following new multiplatform features to increase the library's utility:

### 9. Multiplatform WebView Wrapper
*   **Goal**: Provide a uniform, ready-to-use Composable for displaying web content inside KMP apps.
*   **Implementation**:
    *   **Android**: Native `android.webkit.WebView`.
    *   **iOS**: Native `WKWebView`.
    *   **Desktop/JVM**: Embedded Chromium-based WebView (using `Compose-Html` or `CEF` bindings).

### 10. Haptic Feedback Manager
*   **Goal**: Enable simple, tactile haptic sensations (clicks, success vibrations, warning/error patterns) across Android, iOS and Desktop.
*   **Implementation**:
    *   **Android**: `Vibrator` or `VibratorManager` using custom `VibrationEffect` patterns.
    *   **iOS**: `UIFeedbackGenerator` APIs (`UINotificationFeedbackGenerator`, `UIImpactFeedbackGenerator`).
    *   **JVM**: Native haptic triggers or simple audio-vibe feedback where supported.

### 11. Sharing & Intent Module
*   **Goal**: Share texts, links, files, and images to other applications via a unified multiplatform API.
*   **Implementation**:
    *   **Android**: Start an Android sharing `Intent` chooser (`Intent.ACTION_SEND`).
    *   **iOS**: Instantiate and present `UIActivityViewController`.
    *   **JVM**: Open standard system share sheets or fallback to desktop system clipboards.

### 12. Mock API Interceptor (Prototyping & Testing)
*   **Goal**: Allow offline development and automated testing by returning mocked json payloads based on endpoint configurations.
*   **Implementation**:
    *   Add a `mockResponseMap` parameter to `ApiConfig`.
    *   Implement a Ktor mock engine or interceptor that checks if mocking is enabled for a registered server, returning local static JSON strings immediately without making network calls.

### 13. Social Authentication Modules (Google, Facebook, Apple Logins)
*   **Goal**: Provide a unified, cross-platform social login interface that prompts users and returns standardized user credentials and access tokens.
*   **Implementation**:
    *   **Google Sign-In**:
        *   **Android**: Utilize Google's new `Credential Manager` API or the `play-services-auth` SDK.
        *   **iOS**: Call Google's Swift `GoogleSignIn` SDK directly via native Interop wrapper classes.
        *   **JVM**: Open an OAuth 2.0 Web flow in the user's local web browser with a loopback receiver (`http://localhost:port`).
    *   **Facebook Login**:
        *   **Android & iOS**: Bind native SDKs (`facebook-login` on Android, `FBSDKLoginKit` on iOS) to launch native login dialogues with web views as fallback.
    *   **Sign in with Apple**:
        *   **iOS**: Use native `AuthenticationServices` (`ASAuthorizationAppleIDProvider`) for biometric and password-secured authentication.
        *   **Android & JVM**: Implement an Apple Web OAuth Flow using redirect URLs (with backend support) to verify Apple's identity token.

