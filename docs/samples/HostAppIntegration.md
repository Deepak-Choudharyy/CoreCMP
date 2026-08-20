# CoreCmp — Host App Integration Starter (PLN-186)

Copy-paste template to integrate CoreCmp without store-policy mistakes.

## 1. Gradle (`shared/build.gradle.kts`)

```kotlin
commonMain.dependencies {
    implementation("com.corecmp:coreCmp:1.0.03-alpha-11")
}
```

## 2. Android Application

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CoreCmp.init(context = this)
        startKoin { modules(coreCmpModule()) }
    }
}
```

### AndroidManifest (your app module)

See [`docs/templates/AndroidManifest.permissions.xml`](../templates/AndroidManifest.permissions.xml).  
Declare **only** permissions you use.

## 3. iOS

### Info.plist

See [`docs/templates/Info.plist.usage-strings.xml`](../templates/Info.plist.usage-strings.xml).

### AppDelegate / SwiftUI

```kotlin
// In your KMP iOS entry:
CoreCmp.init()
```

## 4. Location (foreground policy)

```kotlin
// Android: in Activity/Fragment lifecycle
LocationPolicy.isInForeground = true  // onResume
LocationPolicy.isInForeground = false // onPause
```

## 5. Permissions helper

```kotlin
val features = setOf(AppPermission.CAMERA, AppPermission.GALLERY)
PermissionManifest.androidPermissionsFor(features).forEach { println(it.permission) }
PermissionManifest.iosUsageStringsFor(features).forEach { println("${it.key}: ${it.example}") }
```

## 6. Attachment upload with compress

```kotlin
CoreCmp.upload.pickCompressUpload(
    base = "main",
    endpoint = "/upload",
    file = pickedFile,
).collect { result -> /* handle Resource */ }
```

## 7. Store compliance

Read [`docs/STORE_COMPLIANCE.md`](../STORE_COMPLIANCE.md) before Play/App Store submission.
