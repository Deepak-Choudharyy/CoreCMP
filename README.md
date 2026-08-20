<p align="center">
  <img src="assets/logo_transparent.png" width="420"/>
</p>

<p align="center">
  Kotlin Multiplatform Toolkit for API, UI, Permissions, Media Picker & PDF Generation
</p>

<div align="center">
  <img src="https://img.shields.io/badge/Kotlin-Multiplatform-blue" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Compose-Multiplatform-green" alt="Compose" />
  <img src="https://img.shields.io/badge/Ktor-Client-orange" alt="Ktor" />
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License" />
  <img src="https://img.shields.io/badge/version-1.0.0.1--rc--001-purple" alt="Version" />
  <img src="https://img.shields.io/badge/platform-Android%20%7C%20iOS%20%7C%20Desktop-blue" alt="Platform" />
</div>

---

**CoreCmp** is a comprehensive Kotlin Multiplatform (KMP) toolkit designed to eliminate boilerplate code when building mobile (Android & iOS) and desktop applications. It provides unified, out-of-the-box abstractions for networking, storage, permission handling, file picking, and premium Compose Multiplatform UI components.

## Table of Contents
1. [Installation](#1-installation)
2. [Platform Compatibility & Target Configuration](#2-platform-compatibility--target-configuration)
3. [Setup & Dependency Injection](#3-setup--dependency-injection)
4. [CoreCmp Facade — All Services](#4-corecmp-facade--all-services)
5. [Networking (`api`)](#5-networking-module-api)
6. [Upload — Compress & Fast Upload](#6-upload--compress--fast-upload)
7. [Validation & Forms](#7-validation--forms)
8. [Storage & Cache](#8-storage--cache)
9. [Permissions & Store Compliance](#9-permissions--store-compliance)
10. [Location & Geocoder](#10-location--geocoder)
11. [Media Picker](#11-media-picker)
12. [Security & Privacy](#12-security--privacy)
13. [Theme, Display & UI Kit](#13-theme-display--ui-kit)
14. [Navigation & Deep Links](#14-navigation--deep-links)
15. [Indian / Insurance Domain](#15-indian--insurance-domain)
16. [Auth, State & Notifications](#16-auth-state--notifications)
17. [Platform Utilities](#17-platform-utilities)
18. [PDF, Modifiers & Extensions](#18-pdf-modifiers--extensions)

---

## 1. Installation

This library is hosted on a custom GitHub Pages Maven repository. Follow these steps to configure your project.

### Step 1: Add the Maven Repository
Declare the custom Maven repository URL in your project's `settings.gradle.kts` file under the `dependencyResolutionManagement` section:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // Custom CoreCmp Repository
        maven("https://deepak-choudharyy.github.io/CoreCMP/maven-repo/")
    }
}
```

### Step 2: Add the Dependency
Add the library dependency to your shared module's `build.gradle.kts` (usually under `commonMain` dependencies):

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.corecmp:shared:1.0.0.001-rc-001") // Replace with the latest version
        }
    }
}
```

---

## 2. Platform Compatibility & Target Configuration

### Supported Platforms
CoreCmp is fully compatible with the following target platforms:
* **Android** (minSdk 24, compileSdk 36)
* **JVM / Desktop**
* **iOS (Apple Silicon / Devices)**: `iosArm64` and `iosSimulatorArm64`

### IMPORTANT: Apple x86_64 (`iosX64`) Simulator Target Not Supported
Starting from **Compose Multiplatform 1.11.1+** (which this library is built on), support for the Apple x86_64 target architecture (`iosX64`) has been **fully removed** due to deprecation upstream.

Because of this, if your consumer application targets `iosX64`, dependency resolution will fail. **To resolve this, you must remove `iosX64()` from your targets in your consumer project's `build.gradle.kts`**:

```diff
 kotlin {
     androidTarget()
     jvm()
     
     iosArm64()
     iosSimulatorArm64()
-    iosX64() // Remove this line from your consumer project
 }
```

*Note: Since Intel Macs are deprecated for modern simulator runs in Compose Multiplatform, using `iosSimulatorArm64` (for Apple Silicon) and `iosArm64` (for physical devices) is the standard target configuration.*

---

## 3. Development & Local Testing

### Local Publish (Test before Tag)
To publish a test version of the library to your machine's local Maven repository (`mavenLocal`):
```bash
./gradlew :shared:publishToMavenLocal -x test --parallel --build-cache
```

Then add `mavenLocal()` to your consumer project's repositories to test the build.

---

---

## 3. Setup & Dependency Injection

CoreCmp uses **Koin** internally for Dependency Injection and **Multiplatform Settings** for local storage.

### Android: Initialize Context First (Required)
On Android, call `CoreCmp.init()` **before** `startKoin()`:
```kotlin
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.api.coreCmpModule
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreCmp.init(context = this, settingsName = "my_custom_app_pref")
        startKoin { modules(coreCmpModule()) }
    }
}
```

### iOS / Desktop: Initialize Settings Name
On iOS and JVM, configure the settings name before Koin:
```kotlin
import com.corecmp.shared.CoreCmp

CoreCmp.init(settingsName = "my_custom_app_pref")
```

### Configure Koin
To register the library's core dependencies, load `coreCmpModule()` inside your app's dependency injection startup:
```kotlin
import com.corecmp.shared.api.coreCmpModule
import org.koin.core.context.startKoin

startKoin {
    modules(
        coreCmpModule() // Registers ApiClient, SharedViewModel, Settings, etc.
    )
}
```

---

## 4. CoreCmp Facade — All Services

Single entry point for every feature:

```kotlin
import com.corecmp.shared.CoreCmp

// Core
CoreCmp.location          // GPS
CoreCmp.permission        // Runtime permissions
CoreCmp.media             // Camera / gallery / docs picker
CoreCmp.network           // Online/offline observer
CoreCmp.storage           // Encrypted secure storage
CoreCmp.haptics           // Haptic feedback
CoreCmp.share             // Native share
CoreCmp.geocoder          // Address search

// Display & theme
CoreCmp.display           // Fixed font scale
CoreCmp.theme             // Light / dark / AMOLED

// Security
CoreCmp.appLock           // PIN + biometric lock
CoreCmp.sessionTimeout    // Auto-lock on idle
CoreCmp.backgroundLock    // Lock when app backgrounds
CoreCmp.consent             // GDPR / DPDP consent

// Storage & cache
CoreCmp.formDrafts        // Crash-safe form drafts
CoreCmp.preferences       // Non-sensitive settings
CoreCmp.apiCache          // File-based GET cache
CoreCmp.localStore        // Local JSON store + migrations
CoreCmp.responseCache     // TTL response cache

// Network advanced
CoreCmp.offlineQueue      // Offline API queue
CoreCmp.requestDeduplicator

// Upload
CoreCmp.upload            // Compress + multipart upload
CoreCmp.uploadQueue       // Priority upload queue

// Navigation
CoreCmp.deepLinks         // Deep link handler

// Platform
CoreCmp.clipboard
CoreCmp.deviceInfo
CoreCmp.qrGenerator
CoreCmp.qrScanner
CoreCmp.updates           // In-app update check

// Auth & accounts
CoreCmp.googleAuth
CoreCmp.appleAuth
CoreCmp.accounts          // Multi POS account switch
CoreCmp.guestMode

// Notifications & analytics
CoreCmp.pushToken
CoreCmp.notifications
CoreCmp.analytics         // Plug Firebase / Mixpanel
CoreCmp.crashReporter     // Plug Sentry / Crashlytics
```

---

## 5. Networking Module (`api`)

CoreCmp simplifies Ktor network requests with base URL configuration, default parameter merging, custom file uploading, and priority dispatch queues.

### Base URL Registration
You can register multiple API endpoints with different names, default headers, query params, or body arguments:
```kotlin
import com.corecmp.shared.api.ApiConfig

ApiConfig.registerBaseUrl(
    name = "main_server",
    baseUrl = "https://api.example.com/v1/",
    token = "bearer_auth_token_here",
    defaultHeaders = mapOf("Accept-Language" to "en"),
    defaultQueryParams = mapOf("client" to "mobile"),
    defaultBodyParams = mapOf("version" to "1.0.0")
)
```

To update authorization tokens dynamically (e.g., after login):
```kotlin
ApiConfig.updateToken("main_server", "new_login_token")
```

### Performing API Requests
Inject `ApiClient` and use `.request<RequestType, ResponseType>()` which returns a reactive `Flow<Resource<ResponseType>>`.

#### Standard GET Request
```kotlin
import com.corecmp.shared.api.ApiClient
import com.corecmp.shared.api.Resource
import kotlinx.coroutines.flow.collect

val apiClient: ApiClient = get() // Inject via Koin

apiClient.request<Unit, UserResponse>(
    base = "main_server",
    endpoint = "profile"
).collect { resource ->
    when (resource) {
        is Resource.Loading -> println("Loading profile...")
        is Resource.Success -> println("Profile data: ${resource.data}")
        is Resource.Error -> println("Failed to load: ${resource.message}")
    }
}
```

#### POST Request with Body and Dispatch Priority
```kotlin
import com.corecmp.shared.api.ApiMethod
import com.corecmp.shared.api.ApiPriority
import com.corecmp.shared.api.RequestOptions

apiClient.request<LoginRequest, LoginResponse>(
    base = "main_server",
    endpoint = "login",
    method = ApiMethod.POST,
    body = LoginRequest(email = "user@mail.com", password = "123"),
    options = RequestOptions(priority = ApiPriority.HIGH) // HIGH, NORMAL, or LOW priority
)
```

#### Multipart Upload
```kotlin
import com.corecmp.shared.api.FilePart
import com.corecmp.shared.api.BodyType

val photoFile = PickedFile(bytes = fileBytes, fileName = "avatar.jpg", mimeType = "image/jpeg")

apiClient.request<UploadMetadata, UploadResponse>(
    base = "main_server",
    endpoint = "upload-avatar",
    method = ApiMethod.POST,
    body = UploadMetadata(userId = "123"),
    files = listOf(
        FilePart(name = "avatar_file", file = photoFile)
    ),
    bodyType = BodyType.FORM_DATA
)
```

### Real-Time Network Connectivity Checking
CoreCmp's `ApiClient` automatically monitors network connection status using `ConnectivityObserver`. If the device is offline:
- If `options.retryOnConnection = true`, the request flow suspends and automatically retries as soon as internet connectivity is restored.
- Otherwise, it immediately returns `Resource.Error("No internet")` without executing the HTTP call.

### BaseViewModel Integration
CoreCmp's `BaseViewModel` automatically handles loading states, error dialogues, and lifecycle-scoped API collections.
```kotlin
import com.corecmp.shared.api.BaseViewModel
import androidx.lifecycle.viewModelScope

class ProfileViewModel(private val api: ApiClient) : BaseViewModel() {

    fun loadProfile() {
        api.request<Unit, UserResponse>("main_server", "profile")
            .collectApi(
                scope = viewModelScope,
                showLoading = true,  // Automatically updates baseState.isLoading
                showError = true    // Automatically shows SnackBar error messages
            ) { profileData ->
                // Handle success payload safely
                println("Profile name: ${profileData?.name}")
            }
    }
}
```

---

## 6. Upload — Compress & Fast Upload

```kotlin
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.api.Resource
import com.corecmp.shared.upload.CompressionConfig

// Pick → compress → upload (one call)
CoreCmp.upload.pickCompressUpload(
    base = "main_server",
    endpoint = "upload/doc",
    file = pickedFile,
    compression = CompressionConfig(maxWidth = 1280, quality = 0.8f),
    onProgress = { progress -> println("${progress.percent}%") },
).collect { result ->
    when (result) {
        is Resource.Success -> println("Uploaded")
        is Resource.Error -> println(result.message)
        is Resource.Loading -> showLoader()
    }
}

// Upload queue (multiple files, priority)
val id = CoreCmp.uploadQueue.enqueue("main_server", "upload", file, priority = UploadPriority.HIGH)
CoreCmp.uploadQueue.processAll().collect { /* ... */ }

// Pre-signed S3/GCS URL (direct upload)
CoreCmp.upload.uploadPreSigned(preSignedUrl, pickedFile).collect { /* ... */ }
```

---

## 7. Validation & Forms

```kotlin
import com.corecmp.shared.validation.*
import com.corecmp.shared.validation.FormState
import com.corecmp.shared.ui.kit.OtpInput
import com.corecmp.shared.ui.kit.PasswordStrengthMeter

// Indian validators
isValidPan("ABCDE1234F")
isValidAadhaar("234567890123")
isValidGstin("27AABCU9603R1ZM")
isValidIndianPhone("9876543210")

// Input masks
val phone = maskPhone("9876543210")   // +91 98765 43210
val pan = maskPan("abcde1234f")       // ABCDE1234F

// Form state tracker
val form = FormState()
form.validate("pan", isValidPan(panInput), "Invalid PAN")
form.submit { api.submit() }

// UI components
OtpInput(otpLength = 6, onOtpComplete = { otp -> verifyOtp(otp) })
PasswordStrengthMeter(password = password)
```

---

## 8. Storage & Cache

```kotlin
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.storage.SchemaMigrationHelper
import com.corecmp.shared.storage.StorageReporter

// Secure storage (tokens)
CoreCmp.storage.putString("auth_token", jwt)
val token = CoreCmp.storage.getString("auth_token")

// Preferences (non-sensitive)
CoreCmp.preferences.putBoolean("dark_mode", true)

// Form drafts (survives crash)
CoreCmp.formDrafts.save("kyc_form", draftJson)
val saved = CoreCmp.formDrafts.load("kyc_form")

// Local store + schema migration
SchemaMigrationHelper(CoreCmp.localStore)
    .fromTo(1, 2) { store -> /* migrate data */ }
    .migrateTo(2)

// Cache management
StorageReporter.report()       // bytes used
StorageReporter.clearAllCaches()
```

---

## 9. Permissions & Store Compliance

```kotlin
import androidx.compose.runtime.*
import com.corecmp.shared.permission.*

@Composable
fun MyScreen() {
    val permissionManager = remember { PermissionManager() }
    permissionManager.RegisterPermissionLauncher() // required in Composable

    // GALLERY / STORAGE = system picker, no runtime prompt
    LaunchedEffect(Unit) {
        permissionManager.requestPermissions(listOf(AppPermission.CAMERA)) { results ->
            /* handle */
        }
    }
}

// 3. Generate manifest / Info.plist entries for YOUR app
PermissionManifest.androidPermissionsFor(setOf(AppPermission.CAMERA, AppPermission.LOCATION))
PermissionManifest.iosUsageStringsFor(setOf(AppPermission.CAMERA))
```

> **Store policy:** Library does NOT merge sensitive permissions. See [docs/STORE_COMPLIANCE.md](docs/STORE_COMPLIANCE.md).

---

## 10. Location & Geocoder

```kotlin
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.location.LocationPolicy
import com.corecmp.shared.location.Geocoder
import com.corecmp.shared.location.LocationPickerBottomSheet

// Foreground policy (required for Play Store)
LocationPolicy.isInForeground = true  // onResume
LocationPolicy.isInForeground = false // onPause

// Single GPS fix
CoreCmp.location.getCurrentLocation { latLng ->
    println("${latLng?.latitude}, ${latLng?.longitude}")
}

// Continuous (foreground only)
CoreCmp.location.observeLocation(5000).collect { latLng -> /* ... */ }

// Address search
Geocoder.search("Connaught Place Delhi")

// UI picker bottom sheet
LocationPickerBottomSheet(show = true, onDismiss = {}, onLocationPicked = {})
```

---

## 11. Media Picker (`picker`)

Pick files and images without requesting system permissions or writing platform-specific code.

### PlatformMediaPicker Usage
```kotlin
import androidx.compose.runtime.remember
import com.corecmp.shared.picker.PlatformMediaPicker
import com.corecmp.shared.picker.PickerType
import com.corecmp.shared.picker.DocumentConfig

@Composable
fun FilePickerDemo() {
    val mediaPicker = remember { PlatformMediaPicker() }
    
    // 1. Register Launchers
    mediaPicker.RegisterLaunchers()

    Button(onClick = {
        // 2. Launch Picker
        mediaPicker.launch(
            type = PickerType.DOCUMENT,
            documentConfig = DocumentConfig(mimeTypes = listOf("application/pdf"))
        ) { pickedFile ->
            pickedFile?.let {
                println("Picked file name: ${it.fileName}")
                println("Size: ${it.sizeInMb} MB")
                println("Is Image: ${it.isImage}")
                println("Is PDF: ${it.isPdf}")
                
                // Helper to convert to Base64 String
                val base64 = it.toBase64Image()
            }
        }
    }) {
        Text("Pick PDF File")
    }
}
```

### Complete Upload Bottom Sheet
CoreCmp provides a ready-made `CommonAttachmentBottomSheet` that automatically handles permission checks and opens CAMERA, GALLERY, or STORAGE pickers under the hood.
```kotlin
import com.corecmp.shared.picker.CommonAttachmentBottomSheet
import com.corecmp.shared.permission.AppPermission

@Composable
fun DocumentUploadScreen() {
    var showSheet by remember { mutableStateOf(false) }

    if (showSheet) {
        CommonAttachmentBottomSheet(
            permissions = listOf(AppPermission.CAMERA, AppPermission.GALLERY, AppPermission.STORAGE),
            onFilePicked = { file ->
                showSheet = false
                if (file != null) {
                    println("Ready to upload: ${file.fileName}")
                }
            }
        )
    }
}
```

---

## 12. Security & Privacy

```kotlin
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.security.*
import com.corecmp.shared.ui.AppLockGate

// App lock (PIN + biometric)
CoreCmp.appLock.setPin("1234")
AppLockGate { MainContent() }  // wraps app root

// Session timeout (5/15/30 min idle)
CoreCmp.sessionTimeout.touch()  // call on user interaction
CoreCmp.sessionTimeout.checkTimeout()

// Screenshot block on sensitive screens
setScreenshotBlocking(true)

// PII display masks
maskPan("ABCDE1234F")    // ABCDE****F
maskPhone("9876543210")   // 98****3210

// Consent (GDPR / DPDP)
CoreCmp.consent.recordConsent("marketing", version = "1.0")
CoreCmp.consent.hasConsent("marketing", "1.0")

// Data export
val json = exportUserData(listOf("theme", "language"))

// Root/jailbreak warning (warn only — don't hard-block for Play policy)
if (isDeviceCompromised()) showSecurityWarning()
```

---

## 13. Theme, Display & UI Kit

```kotlin
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.theme.*
import com.corecmp.shared.display.CoreCmpDisplayHost
import com.corecmp.shared.ui.kit.*

// Theme
CoreCmpTheme(mode = AppThemeMode.DARK) {
    CoreCmpProviders(colors = CoreCmpColors(primary = BrandBlue)) {
        AppContent()
    }
}

// Fixed font scale (same UI on all devices)
CoreCmpDisplayHost {
    CoreCmp.display.setLockFontScale(true)
    AppNavigation()
}

// New UI kit components
StatusChip("Approved", StatusChipVariant.SUCCESS)
Timeline(steps = listOf("KYC", "Payment", "Policy"))
EmptyStateView(title = "No policies", onAction = { refresh() })
ErrorRetryView(message = "Failed", onRetry = { retry() })
CoreCmpPullToRefresh(isRefreshing, onRefresh = { refresh() }) { ListContent() }
SwipeToDeleteItem(onDelete = { delete(id) }) { ListRow() }
SearchBar(query, onQueryChange = { query = it })
AmountTextField(value, onValueChange = { value = it })
RatingBar(rating = 4f, onRatingChange = {})
CountdownTimer(targetEpochMs = expiryTime)
InfoBanner("Offer ends today", variant = InfoBannerVariant.WARNING)
WhatsNewScreen(changelogItems, onDismiss = {})
```

---

## 14. Navigation & Deep Links

```kotlin
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.navigation.*
import com.corecmp.shared.deeplink.DeepLinkHandler
import androidx.navigation.NavController

// Register routes
CoreCmp.deepLinks.route("policy/{id}") { match ->
    navigate("policy/${match.pathParams["id"]}")
}

// Cold start deep link (Android: call from Activity intent)
setPlatformDeepLink(intent?.data?.toString())
readPlatformDeepLink()?.let { CoreCmp.deepLinks.handle(it) }

// Nav helpers
navController.navigateOnce("home")  // debounced
navController.popUpToRoute("login", inclusive = true)

// Push notification → screen
PushNotificationRouter().handleTap(PushPayload(route = "/renewals")) { navController.navigate(it) }

// Return result from screen
navController.setNavResult("selected_policy", policyId)
navController.rememberNavResult<String>("selected_policy") { id -> loadPolicy(id) }
```

---

## 15. Indian / Insurance Domain

```kotlin
import com.corecmp.shared.domain.*
import com.corecmp.shared.domain.PremiumBreakdownCard

rupeeInWords(125000)           // "One Lakh Twenty Five Thousand Rupees Only"
upiDeepLink("merchant@upi", amount = 1500.0)
ncbPercent(claimFreeYears = 3) // 35
gstAmount(premiumExGst = 10000.0)
emiAmount(principal = 12000.0, annualRatePercent = 12.0, tenureMonths = 6)
formatPolicyNumber("POL123456789")

// Compose UI
PremiumBreakdownCard(PremiumBreakdown(ownDamage = 5000.0, thirdParty = 2000.0))
KycChecklist(documents = listOf(KycDocument("PAN", true), KycDocument("Aadhaar", false)))
CommissionCard(premium = 10000.0, commissionPercent = 15.0)
```

---

## 16. Auth, State & Notifications

```kotlin
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.auth.*
import com.corecmp.shared.state.*
import com.corecmp.shared.api.TokenRefreshConfig
import com.corecmp.shared.api.HttpClientProvider

// Google / Apple sign-in (wire host SDK in androidMain/iosMain)
val result = CoreCmp.googleAuth.signIn()  // suspend
val apple = CoreCmp.appleAuth.signIn()    // suspend

// Role guard
RoleGuard(allowedRoles = setOf(UserRole.AGENT), currentRole = role) {
    AgentDashboard()
}

// Multi-account POS switch
CoreCmp.accounts.setAccounts(accounts, activeId = "pos_1")
CoreCmp.accounts.switchTo("pos_2")

// Guest mode
CoreCmp.guestMode.enableGuest()
CoreCmp.guestMode.requireLogin(onRequireLogin = { showLogin() }) { browse() }

// MVI / state
val state = UiState.Success(data)
Event.ShowSnackbar("Saved").consume { /* one-shot */ }
pollUntil(predicate = { it.isReady }) { api.checkStatus() }

// Token auto-refresh on 401
HttpClientProvider.installTokenRefresh(TokenRefreshConfig(
    baseName = "main_server",
    handler = TokenRefreshHandler { base -> refreshToken(base) },
))

// Notifications
CoreCmp.pushToken.register()
CoreCmp.notifications.push(
    InAppNotification(id = "1", title = "Renewed", message = "Policy renewed")
)
```

---

## 17. Platform Utilities

```kotlin
import com.corecmp.shared.CoreCmp
import com.corecmp.shared.platform.*

CoreCmp.clipboard.copy("Policy number")
val text = CoreCmp.clipboard.paste()

val info = CoreCmp.deviceInfo.snapshot()  // model, OS, app version

CoreCmp.qrGenerator.generate("https://pay.example.com", size = 256)

openMaps("Connaught Place Delhi")
openDialer("9876543210")
openWhatsApp("9876543210", "Hi, I need help with my policy")
openEmail("support@example.com", subject = "Claim query")

requestInAppReview()  // Play Store / App Store review dialog
```

---

## 18. PDF, Modifiers & Extensions

### PDF Generator (`print`)

Generate, view, download, or share PDFs dynamically from any Compose Multiplatform layout hierarchy using `PdfManager`.

```kotlin
import androidx.compose.runtime.remember
import com.corecmp.shared.print.rememberPdfManager

@Composable
fun ReceiptScreen() {
    val pdfManager = rememberPdfManager()

    Button(onClick = {
        pdfManager.generateAndShare(
            fileName = "receipt.pdf",
            onStart = { println("Generating PDF started...") },
            onComplete = { println("PDF share dialog displayed.") }
        ) {
            // Write standard Compose layout to compile directly into a PDF page
            Column(modifier = Modifier.padding(24.dp)) {
                Text("INVOICE #99827", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                Text("Thank you for your purchase!")
            }
        }
    }) {
        Text("Generate & Share PDF")
    }
}
```

### Core UI Components (also available)

| Component | Package | Use for |
|-----------|---------|---------|
| `CustomScaffold` | `ui` | Screen with back, loading, gradient |
| `CommonDropDown` | `ui` | Single/multi select dropdown |
| `EasyDatePicker` | `ui` | Date / range picker |
| `GenericBottomSheet` | `ui` | Modal bottom sheet |
| `OutLinedSimpleTextField` | `ui` | Text field with errors |
| `CommonButton` | `ui` | Debounced button |
| `GenericTabs` | `tab` | Tab selector |
| `CustomImage` | `ui` | Coil + SVG + Lottie |
| `SnackBarBoxApp` | `ui` | Global snackbar host |

### Custom Modifiers
```kotlin
Modifier.beamBorder(radius = 10.dp, brush = successBrush)
Modifier.dashedBorder(color = Color.Gray, strokeWidth = 2.dp)
Modifier.bounceClick { onClick() }
Modifier.shimmer(enabled = isLoading)
```

### Extensions
```kotlin
"2026-06-03".toDdMmmYyyy()
125000.toIndianCurrency()      // ₹1,25,000.00
"9876543210".toIndianPhone()   // +91 98765 43210
"hello world".toTitleCase()
```

### App root setup
```kotlin
CoreCmp.init(context = this)
CoreCmp.isDebugEnabled = BuildConfig.DEBUG
SnackBarBoxApp { CoreCmpDisplayHost { CoreCmpTheme { AppNavigation() } } }
```

> **Full feature list (317):** [docs/CoreCmp-Feature-Catalog.md](docs/CoreCmp-Feature-Catalog.md)

---

## Author
**Ajay Swami**

