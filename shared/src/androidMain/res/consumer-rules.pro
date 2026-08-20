# CoreCmp consumer ProGuard/R8 rules — safe defaults for host apps.
# Add to your app: buildTypes { release { consumerProguardFiles(...) } } is automatic via AAR.

# Keep public facade API
-keep class com.corecmp.shared.CoreCmp { *; }
-keep class com.corecmp.shared.api.** { *; }
-keep class com.corecmp.shared.picker.** { *; }
-keep class com.corecmp.shared.permission.** { *; }

# Kotlin serialization models used by host apps
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Ktor / OkHttp — standard keeps
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Play In-App Review / Update
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# ZXing (QR generation)
-dontwarn com.google.zxing.**
